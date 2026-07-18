#include "mcrtx/lifecycle/perf_log.hpp"

#include "mcrtx/core/remix_renderer.hpp"

#include <algorithm>
#include <atomic>
#include <chrono>
#include <condition_variable>
#include <cstdio>
#include <cstdlib>
#include <cstring>
#include <deque>
#include <filesystem>
#include <fstream>
#include <iomanip>
#include <mutex>
#include <sstream>
#include <string>
#include <string_view>
#include <thread>
#include <unordered_map>
#include <vector>

#include <windows.h>

namespace mcrtx::perf {
namespace {

std::atomic<bool> g_initialized {false};
std::atomic<bool> g_enabled {false};
std::atomic<bool> g_traceEnabled {false};
std::atomic<std::uint32_t> g_flushIntervalFrames {60};

std::mutex g_mutex;
std::ofstream g_logStream;
std::ofstream g_traceStream;
bool g_logStreamReady {false};
bool g_traceStreamReady {false};
std::atomic<bool> g_shutdown {false};

struct AggregateKey {
  Side side;
  std::string site;

  bool operator==(const AggregateKey& other) const noexcept {
    return side == other.side && site == other.site;
  }
};

struct AggregateKeyHash {
  std::size_t operator()(const AggregateKey& key) const noexcept {
    std::size_t hash = std::hash<std::string>{}(key.site);
    hash ^= static_cast<std::size_t>(key.side) + 0x9e3779b9u + (hash << 6) + (hash >> 2);
    return hash;
  }
};

struct DurationAggregate {
  std::uint64_t samples {0};
  std::uint64_t totalNanos {0};
  std::uint64_t maxNanos {0};
};

struct CountAggregate {
  std::uint64_t samples {0};
  std::uint64_t totalCount {0};
  std::uint64_t maxCount {0};
};

std::unordered_map<AggregateKey, DurationAggregate, AggregateKeyHash> g_durations;
std::unordered_map<AggregateKey, CountAggregate, AggregateKeyHash> g_counts;

// Interned site registry used by the batched Java API. A deque keeps element
// references stable across push_back, letting the trace writer thread read
// past entries without holding g_mutex. Growth only happens under g_mutex.
std::deque<AggregateKey> g_siteRegistry;
std::vector<DurationAggregate> g_siteDurations;
std::unordered_map<AggregateKey, int, AggregateKeyHash> g_siteIndex;
// Monotonic registry size observable without g_mutex. Readers treat any id
// less than this value as safe to dereference on g_siteRegistry.
std::atomic<std::size_t> g_siteRegistrySize {0};

std::uint64_t g_frameCounter {0};
std::uint64_t g_flushIndex {0};

// --- Async trace writer ---------------------------------------------------
// Producers enqueue raw sample records under g_traceMutex. A single writer
// thread swaps buffers and serializes them to disk off the hot path. This
// keeps the trace-on cost close to the trace-off cost.
struct TraceRecord {
  std::uint64_t tMillis;
  Side side;
  // Either siteId (>= 0 and site is empty) or a string site (siteId == -1).
  int siteId;
  std::string site;
  std::uint64_t nanos;
  std::uint32_t threadId;
};

std::mutex g_traceMutex;
std::condition_variable g_traceCv;
std::vector<TraceRecord> g_traceBuffer;       // producer-facing
std::vector<TraceRecord> g_traceWriterBuffer; // writer-owned scratch
std::thread g_traceThread;
std::atomic<bool> g_traceThreadRunning {false};
std::atomic<bool> g_traceShutdownRequested {false};
std::atomic<std::uint64_t> g_traceDroppedCount {0};
constexpr std::size_t kTraceBufferMaxRecords = 262144;  // ~cap memory; drop excess

const char* sideName(Side side) noexcept {
  switch (side) {
    case Side::Hook: return "hook";
    case Side::Call: return "call";
    case Side::Jni: return "jni";
    case Side::Native: return "native";
    case Side::Remix: return "remix";
  }
  return "unknown";
}

std::string readEnv(const char* name) {
  char* value = nullptr;
  std::size_t length = 0;
  if (_dupenv_s(&value, &length, name) != 0 || value == nullptr) {
    return {};
  }
  std::string result(value);
  std::free(value);
  return result;
}

bool envTruthy(const std::string& v) {
  if (v.empty()) return false;
  if (v == "0" || v == "false" || v == "False" || v == "FALSE" || v == "off" || v == "OFF") {
    return false;
  }
  return true;
}

std::filesystem::path resolveDllDirectory() {
  HMODULE module = nullptr;
  if (!GetModuleHandleExW(
          GET_MODULE_HANDLE_EX_FLAG_FROM_ADDRESS | GET_MODULE_HANDLE_EX_FLAG_UNCHANGED_REFCOUNT,
          reinterpret_cast<LPCWSTR>(&resolveDllDirectory),
          &module)) {
    return {};
  }
  wchar_t buffer[MAX_PATH] = {0};
  const DWORD length = GetModuleFileNameW(module, buffer, MAX_PATH);
  if (length == 0 || length == MAX_PATH) {
    return {};
  }
  return std::filesystem::path(buffer).parent_path();
}

std::filesystem::path resolveLogPath(const char* envName, const char* defaultName) {
  const std::string override = readEnv(envName);
  if (!override.empty()) {
    return std::filesystem::path(override);
  }
  const std::filesystem::path dllDir = resolveDllDirectory();
  if (!dllDir.empty()) {
    return dllDir / defaultName;
  }
  return std::filesystem::path(defaultName);
}

void initializeIfNeeded() noexcept {
  if (g_initialized.load(std::memory_order_acquire)) {
    return;
  }
  std::scoped_lock lock(g_mutex);
  if (g_initialized.load(std::memory_order_relaxed)) {
    return;
  }
  const std::string perfFlag = readEnv("MCRTX_PERF");
  const std::string traceFlag = readEnv("MCRTX_PERF_TRACE");
  const std::string intervalFlag = readEnv("MCRTX_PERF_INTERVAL");

  g_enabled.store(envTruthy(perfFlag), std::memory_order_relaxed);
  g_traceEnabled.store(g_enabled.load(std::memory_order_relaxed) && envTruthy(traceFlag),
                       std::memory_order_relaxed);
  if (!intervalFlag.empty()) {
    try {
      const unsigned long parsed = std::stoul(intervalFlag);
      if (parsed > 0) {
        g_flushIntervalFrames.store(static_cast<std::uint32_t>(parsed), std::memory_order_relaxed);
      }
    } catch (...) {
      // keep default
    }
  }
  g_initialized.store(true, std::memory_order_release);
}

std::uint64_t unixMillisNow() noexcept {
  using namespace std::chrono;
  return static_cast<std::uint64_t>(
      duration_cast<milliseconds>(system_clock::now().time_since_epoch()).count());
}

void openLogStreamLocked() {
  if (g_logStreamReady || g_shutdown) {
    return;
  }
  const std::filesystem::path path = resolveLogPath("MCRTX_PERF_LOG", "mcrtx-perf.log");
  g_logStream.open(path, std::ios::out | std::ios::trunc);
  if (g_logStream.is_open()) {
    g_logStream << "# mcrtx perf log  schema=aggregate  startUnixMs=" << unixMillisNow()
                << "  intervalFrames=" << g_flushIntervalFrames.load(std::memory_order_relaxed)
                << "  fields=t,frames,side,site,calls,avgUs,maxUs\n";
    g_logStream.flush();
    g_logStreamReady = true;
  }
}

void openTraceStreamLocked() {
  if (g_traceStreamReady || g_shutdown) {
    return;
  }
  const std::filesystem::path path = resolveLogPath("MCRTX_PERF_TRACE_LOG", "mcrtx-perf-trace.jsonl");
  g_traceStream.open(path, std::ios::out | std::ios::trunc);
  if (g_traceStream.is_open()) {
    g_traceStreamReady = true;
  }
}

void traceWriterLoop() {
  std::vector<TraceRecord> local;
  local.reserve(kTraceBufferMaxRecords);
  while (true) {
    {
      std::unique_lock lock(g_traceMutex);
      g_traceCv.wait(lock, [] {
        return !g_traceBuffer.empty() || !g_traceThreadRunning.load(std::memory_order_relaxed);
      });
      local.swap(g_traceBuffer);
      if (local.empty() && !g_traceThreadRunning.load(std::memory_order_relaxed)) {
        return;
      }
    }
    if (local.empty()) continue;

    // The trace stream is writer-owned — producers never touch it, so no
    // lock is needed. Site registry entries at indices below the published
    // atomic size are stable (std::deque never invalidates existing refs on
    // push_back), so we can resolve ids without g_mutex.
    if (!g_traceStreamReady && !g_shutdown.load(std::memory_order_acquire)) {
      const std::filesystem::path path =
          resolveLogPath("MCRTX_PERF_TRACE_LOG", "mcrtx-perf-trace.jsonl");
      g_traceStream.open(path, std::ios::out | std::ios::trunc);
      if (g_traceStream.is_open()) {
        g_traceStreamReady = true;
      }
    }

    if (g_traceStreamReady) {
      const std::size_t registrySize =
          g_siteRegistrySize.load(std::memory_order_acquire);
      for (const auto& record : local) {
        const double us = static_cast<double>(record.nanos) / 1000.0;
        const std::string* siteName = nullptr;
        Side side = record.side;
        if (record.siteId >= 0 &&
            static_cast<std::size_t>(record.siteId) < registrySize) {
          const auto& registered =
              g_siteRegistry[static_cast<std::size_t>(record.siteId)];
          siteName = &registered.site;
          side = registered.side;
        }
        g_traceStream << "{\"t\":" << record.tMillis
                      << ",\"side\":\"" << sideName(side) << "\""
                      << ",\"site\":\""
                      << (siteName ? std::string_view(*siteName)
                                   : std::string_view(record.site))
                      << "\""
                      << ",\"us\":" << std::fixed << std::setprecision(3) << us
                      << ",\"thread\":" << record.threadId
                      << "}\n";
      }
      g_traceStream.flush();
    }
    local.clear();
  }
}

void ensureTraceThreadStarted() {
  if (g_traceThreadRunning.load(std::memory_order_acquire)) return;
  if (g_traceShutdownRequested.load(std::memory_order_acquire)) return;
  bool expected = false;
  if (!g_traceThreadRunning.compare_exchange_strong(expected, true)) {
    return;
  }
  g_traceThread = std::thread(traceWriterLoop);
}

void enqueueTraceRecord(TraceRecord&& record) {
  {
    std::scoped_lock lock(g_traceMutex);
    if (g_traceBuffer.size() >= kTraceBufferMaxRecords) {
      g_traceDroppedCount.fetch_add(1, std::memory_order_relaxed);
      return;
    }
    g_traceBuffer.push_back(std::move(record));
  }
  g_traceCv.notify_one();
}

void flushLocked() {
  openLogStreamLocked();
  if (!g_logStreamReady) {
    g_durations.clear();
    g_counts.clear();
    // Reset per-id accumulators too so a log-open failure doesn't pile up
    // bounded memory forever.
    for (auto& agg : g_siteDurations) {
      agg.samples = 0;
      agg.totalNanos = 0;
      agg.maxNanos = 0;
    }
    return;
  }
  const std::uint64_t t = unixMillisNow();
  const std::uint64_t frames = g_frameCounter;
  ++g_flushIndex;

  // Fold registry-keyed samples into the main aggregate map.
  for (std::size_t i = 0; i < g_siteRegistry.size(); ++i) {
    auto& siteAgg = g_siteDurations[i];
    if (siteAgg.samples == 0) continue;
    auto& target = g_durations[g_siteRegistry[i]];
    target.samples += siteAgg.samples;
    target.totalNanos += siteAgg.totalNanos;
    if (siteAgg.maxNanos > target.maxNanos) target.maxNanos = siteAgg.maxNanos;
    siteAgg.samples = 0;
    siteAgg.totalNanos = 0;
    siteAgg.maxNanos = 0;
  }

  for (const auto& [key, agg] : g_durations) {
    if (agg.samples == 0) continue;
    const double avgUs = (static_cast<double>(agg.totalNanos) / static_cast<double>(agg.samples)) / 1000.0;
    const double maxUs = static_cast<double>(agg.maxNanos) / 1000.0;
    g_logStream << "[mcrtx-perf] t=" << t
                << " frames=" << frames
                << " side=" << sideName(key.side)
                << " site=" << key.site
                << " calls=" << agg.samples
                << " avgUs=" << std::fixed << std::setprecision(3) << avgUs
                << " maxUs=" << std::fixed << std::setprecision(3) << maxUs << '\n';
  }
  for (const auto& [key, agg] : g_counts) {
    if (agg.samples == 0) continue;
    const double avg = static_cast<double>(agg.totalCount) / static_cast<double>(agg.samples);
    g_logStream << "[mcrtx-perf] t=" << t
                << " frames=" << frames
                << " side=" << sideName(key.side)
                << " site=" << key.site
                << " samples=" << agg.samples
                << " avgCount=" << std::fixed << std::setprecision(3) << avg
                << " maxCount=" << agg.maxCount << '\n';
  }
  g_logStream.flush();
  g_durations.clear();
  g_counts.clear();
  g_frameCounter = 0;
}

int registerSiteLocked(Side side, std::string_view name) {
  AggregateKey key{side, std::string(name)};
  auto it = g_siteIndex.find(key);
  if (it != g_siteIndex.end()) {
    return it->second;
  }
  const int id = static_cast<int>(g_siteRegistry.size());
  g_siteRegistry.push_back(key);
  g_siteDurations.emplace_back();
  g_siteIndex.emplace(std::move(key), id);
  // Publish with release semantics so the writer thread sees the new entry
  // fully initialized before it observes the incremented size.
  g_siteRegistrySize.store(static_cast<std::size_t>(id) + 1,
                           std::memory_order_release);
  return id;
}

}  // namespace

bool isEnabled() noexcept {
  initializeIfNeeded();
  return g_enabled.load(std::memory_order_relaxed);
}

bool isTraceEnabled() noexcept {
  initializeIfNeeded();
  return g_traceEnabled.load(std::memory_order_relaxed);
}

void recordDuration(Side side, std::string_view site, std::uint64_t nanoseconds) noexcept {
  if (!isEnabled()) return;
  const bool traceOn = g_traceEnabled.load(std::memory_order_relaxed);
  try {
    {
      std::scoped_lock lock(g_mutex);
      if (g_shutdown) return;
      AggregateKey key{side, std::string(site)};
      auto& agg = g_durations[key];
      ++agg.samples;
      agg.totalNanos += nanoseconds;
      if (nanoseconds > agg.maxNanos) agg.maxNanos = nanoseconds;
    }
    if (traceOn) {
      ensureTraceThreadStarted();
      enqueueTraceRecord(TraceRecord{unixMillisNow(), side, -1, std::string(site),
                                     nanoseconds, ::GetCurrentThreadId()});
    }
  } catch (...) {
    // swallow; profiler must never throw
  }
}

void recordCount(Side side, std::string_view site, std::uint64_t count) noexcept {
  if (!isEnabled()) return;
  try {
    std::scoped_lock lock(g_mutex);
    if (g_shutdown) return;
    AggregateKey key{side, std::string(site)};
    auto& agg = g_counts[key];
    ++agg.samples;
    agg.totalCount += count;
    if (count > agg.maxCount) agg.maxCount = count;
  } catch (...) {
    // swallow
  }
}

int registerSite(Side side, std::string_view name) noexcept {
  if (!isEnabled()) return -1;
  try {
    std::scoped_lock lock(g_mutex);
    if (g_shutdown) return -1;
    return registerSiteLocked(side, name);
  } catch (...) {
    return -1;
  }
}

void recordDurationById(int siteId, std::uint64_t nanoseconds) noexcept {
  if (!isEnabled() || siteId < 0) return;
  const bool traceOn = g_traceEnabled.load(std::memory_order_relaxed);
  try {
    {
      std::scoped_lock lock(g_mutex);
      if (g_shutdown) return;
      if (static_cast<std::size_t>(siteId) >= g_siteDurations.size()) return;
      auto& agg = g_siteDurations[static_cast<std::size_t>(siteId)];
      ++agg.samples;
      agg.totalNanos += nanoseconds;
      if (nanoseconds > agg.maxNanos) agg.maxNanos = nanoseconds;
    }
    if (traceOn) {
      ensureTraceThreadStarted();
      enqueueTraceRecord(TraceRecord{unixMillisNow(), Side::Hook, siteId, {},
                                     nanoseconds, ::GetCurrentThreadId()});
    }
  } catch (...) {
    // swallow
  }
}

void recordDurationsBatch(const int* siteIds,
                          const std::uint64_t* nanos,
                          std::size_t count) noexcept {
  if (!isEnabled() || count == 0 || siteIds == nullptr || nanos == nullptr) return;
  const bool traceOn = g_traceEnabled.load(std::memory_order_relaxed);
  try {
    std::size_t registrySize;
    {
      std::scoped_lock lock(g_mutex);
      if (g_shutdown) return;
      registrySize = g_siteDurations.size();
      for (std::size_t i = 0; i < count; ++i) {
        const int id = siteIds[i];
        if (id < 0 || static_cast<std::size_t>(id) >= registrySize) continue;
        const std::uint64_t ns = nanos[i];
        auto& agg = g_siteDurations[static_cast<std::size_t>(id)];
        ++agg.samples;
        agg.totalNanos += ns;
        if (ns > agg.maxNanos) agg.maxNanos = ns;
      }
    }
    if (traceOn) {
      ensureTraceThreadStarted();
      const std::uint64_t traceTimestamp = unixMillisNow();
      const DWORD traceThreadId = ::GetCurrentThreadId();
      // Single lock acquisition for the whole batch keeps producer contention
      // with the writer thread short even when count is large.
      std::scoped_lock lock(g_traceMutex);
      const std::size_t available =
          (g_traceBuffer.size() >= kTraceBufferMaxRecords)
              ? 0
              : (kTraceBufferMaxRecords - g_traceBuffer.size());
      const std::size_t emitCount = std::min<std::size_t>(available, count);
      if (emitCount < count) {
        g_traceDroppedCount.fetch_add(count - emitCount, std::memory_order_relaxed);
      }
      for (std::size_t i = 0; i < emitCount; ++i) {
        const int id = siteIds[i];
        if (id < 0 || static_cast<std::size_t>(id) >= registrySize) continue;
        g_traceBuffer.push_back(TraceRecord{traceTimestamp, Side::Hook, id, {},
                                            nanos[i], traceThreadId});
      }
      if (emitCount > 0) {
        g_traceCv.notify_one();
      }
    }
  } catch (...) {
    // swallow
  }
}

void onFramePresented() noexcept {
  if (!isEnabled()) return;
  try {
    std::scoped_lock lock(g_mutex);
    if (g_shutdown) return;
    ++g_frameCounter;
    const std::uint32_t interval = g_flushIntervalFrames.load(std::memory_order_relaxed);
    if (g_frameCounter >= interval) {
      flushLocked();
    }
  } catch (...) {
    // swallow
  }
}

void shutdown() noexcept {
  try {
    // Stop the trace thread first so we can safely close the trace stream.
    g_traceShutdownRequested.store(true, std::memory_order_release);
    if (g_traceThreadRunning.exchange(false, std::memory_order_acq_rel)) {
      {
        std::scoped_lock lock(g_traceMutex);
      }
      g_traceCv.notify_all();
      if (g_traceThread.joinable()) {
        g_traceThread.join();
      }
    }

    std::scoped_lock lock(g_mutex);
    if (g_shutdown) return;
    if (g_enabled.load(std::memory_order_relaxed) && g_frameCounter > 0) {
      flushLocked();
    }
    if (g_logStream.is_open()) {
      g_logStream.flush();
      g_logStream.close();
    }
    if (g_traceStream.is_open()) {
      const std::uint64_t dropped =
          g_traceDroppedCount.load(std::memory_order_relaxed);
      if (dropped > 0) {
        g_traceStream << "# dropped " << dropped
                      << " trace samples due to queue overflow\n";
      }
      g_traceStream.flush();
      g_traceStream.close();
    }
    g_logStreamReady = false;
    g_traceStreamReady = false;
    g_shutdown = true;
  } catch (...) {
    // swallow
  }
}

}  // namespace mcrtx::perf
