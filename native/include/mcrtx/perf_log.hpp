#pragma once

#include <atomic>
#include <chrono>
#include <cstdint>
#include <string>
#include <string_view>

namespace mcrtx::perf {

// Side marker written into aggregated rows and trace lines.
enum class Side : std::uint8_t {
  Hook,    // Java-side entry point
  Call,    // Java-side JNI call site
  Jni,     // JNI export function (jni_bridge.cpp)
  Native,  // RemixRenderer method
  Remix,   // remix_.X API call
};

// Fast-path check. All record* functions additionally short-circuit internally,
// but exposing this lets callers avoid building site strings when disabled.
bool isEnabled() noexcept;
bool isTraceEnabled() noexcept;

// Appends one duration sample for `site` with the given side classification.
void recordDuration(Side side, std::string_view site, std::uint64_t nanoseconds) noexcept;

// Appends one count sample (queue depths, per-frame counters).
void recordCount(Side side, std::string_view site, std::uint64_t count) noexcept;

// Interns a (side, name) pair and returns a dense integer id for use with the
// batch APIs below. Thread-safe; names are copied. Returns -1 when the
// profiler is disabled. Subsequent calls with the same (side, name) return the
// same id.
int registerSite(Side side, std::string_view name) noexcept;

// Records a single duration sample for a previously registered site id.
// Out-of-range ids are silently dropped.
void recordDurationById(int siteId, std::uint64_t nanoseconds) noexcept;

// Batched variant. Under a single lock, records `count` (id, nanos) pairs.
void recordDurationsBatch(const int* siteIds,
                          const std::uint64_t* nanos,
                          std::size_t count) noexcept;

// Called once per presented frame. Flushes the aggregated window to the log
// file when the frame counter reaches MCRTX_PERF_INTERVAL (default 60).
void onFramePresented() noexcept;

// Final flush + file close. Safe to call more than once.
void shutdown() noexcept;

// RAII timer. Disabled runs: both ctor and dtor are trivial.
class ScopedTimer {
 public:
  ScopedTimer(Side side, std::string_view site) noexcept
      : side_(side),
        site_(site),
        start_(isEnabled() ? std::chrono::steady_clock::now()
                           : std::chrono::steady_clock::time_point{}),
        enabled_(isEnabled()) {}

  ScopedTimer(const ScopedTimer&) = delete;
  ScopedTimer& operator=(const ScopedTimer&) = delete;
  ScopedTimer(ScopedTimer&&) = delete;
  ScopedTimer& operator=(ScopedTimer&&) = delete;

  ~ScopedTimer() noexcept {
    if (!enabled_) {
      return;
    }
    const auto end = std::chrono::steady_clock::now();
    const auto nanos = static_cast<std::uint64_t>(
        std::chrono::duration_cast<std::chrono::nanoseconds>(end - start_).count());
    recordDuration(side_, site_, nanos);
  }

 private:
  Side side_;
  std::string_view site_;
  std::chrono::steady_clock::time_point start_;
  bool enabled_;
};

}  // namespace mcrtx::perf

// Zero-overhead timing macro. Expands to nothing of interest when MCRTX_PERF=0
// thanks to the ScopedTimer's enabled_ short-circuit. Pass only string literals
// as site_literal so the stored std::string_view never dangles.
#define MCRTX_PERF_CONCAT_INNER(a, b) a##b
#define MCRTX_PERF_CONCAT(a, b) MCRTX_PERF_CONCAT_INNER(a, b)
#define MCRTX_PERF_SCOPE(side, site_literal) \
  ::mcrtx::perf::ScopedTimer MCRTX_PERF_CONCAT(_mcrtx_perf_scope_, __LINE__)((side), (site_literal))
