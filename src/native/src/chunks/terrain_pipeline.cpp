#include "mcrtx/chunks/terrain_pipeline.hpp"
#include "mcrtx/chunks/remix_chunk_policy.hpp"
#include "mcrtx/core/remix_render_common.hpp"
#include "mcrtx/lifecycle/perf_log.hpp"

#include <algorithm>
#include <bit>
#include <limits>

namespace mcrtx {
namespace {
std::uint64_t nanos(std::chrono::steady_clock::duration duration) {
  return std::chrono::duration_cast<std::chrono::nanoseconds>(duration).count();
}
std::uint64_t fingerprint(const std::vector<TerrainSurface>& surfaces) {
  std::uint64_t fingerprint = 0x4D435254584D4553ull;
  fingerprint = detail::mixHashComponent(fingerprint, static_cast<std::uint32_t>(surfaces.size()));

  for (const TerrainSurface& surface : surfaces) {
    const std::uintptr_t materialKey = surface.materialClass;
    fingerprint = detail::mixHashComponent(fingerprint, static_cast<std::uint32_t>(materialKey));
    fingerprint = detail::mixHashComponent(fingerprint, static_cast<std::uint32_t>(materialKey >> 32));
    fingerprint = detail::mixHashComponent(fingerprint, static_cast<std::uint32_t>(surface.vertices.size()));
    fingerprint = detail::mixHashComponent(fingerprint, static_cast<std::uint32_t>(surface.indices.size()));

    for (const remixapi_HardcodedVertex& vertex : surface.vertices) {
      for (float position : vertex.position) {
        fingerprint = detail::mixHashComponent(fingerprint, std::bit_cast<std::uint32_t>(position));
      }
      for (float normal : vertex.normal) {
        fingerprint = detail::mixHashComponent(fingerprint, std::bit_cast<std::uint32_t>(normal));
      }
      for (float texcoord : vertex.texcoord) {
        fingerprint = detail::mixHashComponent(fingerprint, std::bit_cast<std::uint32_t>(texcoord));
      }
      fingerprint = detail::mixHashComponent(fingerprint, vertex.color);
    }

    for (std::uint32_t index : surface.indices) {
      fingerprint = detail::mixHashComponent(fingerprint, index);
    }
  }

  return fingerprint;
}

}

const TerrainSectionData* TerrainInputs::neighbor(int x, int y, int z) const {
  const int dx = (x - key.originX) / 16 + 1;
  const int dy = (y - key.originY) / 16 + 1;
  const int dz = (z - key.originZ) / 16 + 1;
  if (dx < 0 || dx > 2 || dy < 0 || dy > 2 || dz < 0 || dz > 2) {
    return nullptr;
  }
  return sections[dx + 3 * dz + 9 * dy].get();
}

TerrainResult buildTerrainGeometry(TerrainInputs inputs) {
  MCRTX_PERF_SCOPE(::mcrtx::perf::Side::Native, "terrain.mesh");
  const auto start = std::chrono::steady_clock::now();
  TerrainResult result;
  result.inputs = std::move(inputs);
  result.bytes = sizeof(TerrainResult) + sizeof(TerrainSectionData)
      + result.inputs.sections[13]->fireCellIndices.capacity() * sizeof(std::uint16_t);
  try {
    for (int pass = 0; pass < 2; ++pass) {
      emitTerrainGeometry(result.inputs, pass, result.passes[pass]);
      result.passes[pass].fingerprint = detail::mixHashComponent(fingerprint(result.passes[pass].surfacesToBuild), result.inputs.settings);
      for (const auto& surface : result.passes[pass].surfacesToBuild) {
        result.bytes += surface.vertices.capacity() * sizeof(remixapi_HardcodedVertex)
                      + surface.indices.capacity() * sizeof(std::uint32_t);
      }
      const auto& build = result.passes[pass];
      result.bytes += build.desiredTorchLights.capacity() * sizeof(TorchLightPlacement)
                    + build.desiredPortalLights.capacity() * sizeof(PortalLightPlacement)
                    + build.desiredGlowstoneLights.capacity() * sizeof(GlowstoneLightPlacement);
    }
    for (const auto& cell : result.inputs.sections[13]->cells) {
      if (cell.blockId != 0) {
        ++result.blockCounts[cell.renderPass];
      }
    }
  } catch (...) {
    result.passes = {};
    result.bytes = 0;
    result.failed = true;
  }
  result.workerNanos = nanos(std::chrono::steady_clock::now() - start);
  return result;
}

TerrainPipeline::TerrainPipeline(unsigned workers, std::size_t completedLimit)
: m_workerCount(workers), m_completedLimit(completedLimit) { }

TerrainPipeline::~TerrainPipeline() { stop(); }

void TerrainPipeline::stop() {
  {
    std::lock_guard lock(m_mutex);
    m_stopping = true;
    clearLocked();
  }
  m_event.notify_all();
  for (auto& worker : m_workers) {
    worker.join();
  }
  m_workers.clear();
}

void TerrainPipeline::clearLocked() {
  ++m_epoch;
  for (const auto& [key, section] : m_sections) {
    m_residencyChanges.insert(key);
  }
  m_sections.clear();
  m_pending = 0;
  m_dispatched -= m_jobs.size();
  m_jobs.clear();
  m_completed.clear();
  m_completedBytes = 0;
}

void TerrainPipeline::clear() {
  std::lock_guard lock(m_mutex);
  clearLocked();
  m_event.notify_all();
}

void TerrainPipeline::reset(std::uint64_t world) {
  std::lock_guard lock(m_mutex);
  if (world > m_world) {
    clearLocked();
    m_world = world;
    m_event.notify_all();
  }
}

void TerrainPipeline::allocate(const ChunkKey& key, std::uint64_t world, std::uint64_t lifetime) {
  std::lock_guard lock(m_mutex);
  if (world != m_world || lifetime == 0 || key.renderPass != 0) {
    return;
  }
  auto& section = m_sections[key];
  if (section.lifetime >= lifetime) {
    return;
  }
  if (section.pending) {
    --m_pending;
  }
  section = {};
  section.lifetime = lifetime;
  m_residencyChanges.insert(key);
  markNeighbors(key);
  m_event.notify_all();
}

void TerrainPipeline::remove(const ChunkKey& key, std::uint64_t world, std::uint64_t lifetime) {
  std::lock_guard lock(m_mutex);
  const auto it = m_sections.find(key);
  if (world != m_world || it == m_sections.end() || it->second.lifetime != lifetime) {
    return;
  }
  if (it->second.pending) {
    --m_pending;
  }
  m_sections.erase(it);
  m_residencyChanges.insert(key);
  markNeighbors(key);
  m_event.notify_all();
}

void TerrainPipeline::mark(Section& section) {
  if (!section.data || section.pending) {
    return;
  }
  section.pending = true;
  ++m_pending;
  section.order = ++m_order;
  section.queued = Clock::now();
}

void TerrainPipeline::markNeighbors(const ChunkKey& key, std::uint32_t affected) {
  // Redstone steps sample diagonal sections at horizontal and vertical boundaries.
  for (int y = -1; y <= 1; ++y) {
    for (int z = -1; z <= 1; ++z) {
      for (int x = -1; x <= 1; ++x) {
        if ((affected & (1u << (x + 1 + 3 * (z + 1) + 9 * (y + 1)))) == 0) {
          continue;
        }
        const auto it = m_sections.find({key.originX + x * 16, key.originY + y * 16, key.originZ + z * 16, 0});
        if (it != m_sections.end()) {
          mark(it->second);
        }
      }
    }
  }
}

bool TerrainPipeline::update(const ChunkKey& key, std::uint64_t world, std::uint64_t lifetime,
                             std::uint64_t revision, const std::array<int, 6>& bounds,
                             const std::vector<ChunkBlockCell>& cells) {
  for (int axis = 0; axis < 3; ++axis) {
    if (bounds[axis] < 0 || bounds[axis + 3] > 15 || bounds[axis] > bounds[axis + 3]) {
      return false;
    }
  }
  const auto volume = (bounds[3] - bounds[0] + 1) * (bounds[4] - bounds[1] + 1) * (bounds[5] - bounds[2] + 1);
  if (cells.size() != static_cast<std::size_t>(volume)) {
    return false;
  }
  const auto start = Clock::now();
  std::unique_lock lock(m_mutex, std::try_to_lock);
  if (!lock.owns_lock()) {
    return false;
  }
  m_statistics.lockNanos += nanos(Clock::now() - start);
  auto it = m_sections.find(key);
  if (m_stopping || world != m_world || it == m_sections.end() || it->second.lifetime != lifetime) {
    return false;
  }
  auto& section = it->second;
  if (revision <= section.revision) {
    return true;
  }
  if (!section.data && volume != 4096) {
    return false;
  }
  // Ownership is transferred into immutable storage before the JNI call returns.
  auto data = section.data ? std::make_shared<TerrainSectionData>(*section.data)
                           : std::make_shared<TerrainSectionData>();
  std::size_t record = 0;
  std::uint32_t affected = section.data ? 0 : (1u << 27) - 1;
  for (int y = bounds[1]; y <= bounds[4]; ++y) {
    for (int z = bounds[2]; z <= bounds[5]; ++z) {
      for (int x = bounds[0]; x <= bounds[3]; ++x) {
        auto cell = cells[record++];
        if (cell.renderPass > 1) {
          return false;
        }
        const auto index = chunk::blockIndex(x, y, z);
        // These emitters use properties already resolved by Java, not raw metadata.
        if (!chunk::isRepeaterBlockId(cell.blockId)) {
          switch (cell.renderType) {
            case detail::kCubeBlockRenderType:
            case detail::kCrossedQuadBlockRenderType:
            case detail::kFireBlockRenderType:
            case detail::kLiquidBlockRenderType:
            case detail::kCropBlockRenderType:
            case detail::kFenceBlockRenderType:
            case detail::kCactusBlockRenderType:
              cell.blockMetadata = 0;
              break;
            default:
              break;
          }
        }
        if (!(data->cells[index] == cell)) {
          for (int dy = y == 0 ? -1 : 0; dy <= (y == 15 ? 1 : 0); ++dy) {
            for (int dz = z == 0 ? -1 : 0; dz <= (z == 15 ? 1 : 0); ++dz) {
              for (int dx = x == 0 ? -1 : 0; dx <= (x == 15 ? 1 : 0); ++dx) {
                affected |= 1u << (dx + 1 + 3 * (dz + 1) + 9 * (dy + 1));
              }
            }
          }
        }
        data->cells[index] = cell;
        data->occupancy[index] = cell.blockId != 0;
      }
    }
  }
  ++m_statistics.captures;
  section.revision = revision;
  if (affected == 0) {
    ++m_statistics.unchanged;
    return true;
  }
  data->fireCellIndices.clear();
  for (int i = 0; i < 4096; ++i) {
    if (data->occupancy[i] && chunk::isFireRenderType(data->cells[i].renderType)) {
      data->fireCellIndices.push_back(static_cast<std::uint16_t>(i));
    }
  }
  section.data = std::move(data);
  for (int i = 0; i < 27; ++i) {
    if (affected & (1u << i)) {
      section.boundaryRevisions[i] = revision;
    }
  }
  markNeighbors(key, affected);
  m_event.notify_all();
  return true;
}

TerrainInputs TerrainPipeline::inputs(const ChunkKey& key, const Section& section) const {
  TerrainInputs result;
  result.key = key;
  result.world = m_world;
  result.epoch = m_epoch;
  result.lifetime = section.lifetime;
  result.revision = section.revision;
  result.settings = m_settings;
  result.queued = section.queued;
  int index = 0;
  for (int y = -1; y <= 1; ++y) {
    for (int z = -1; z <= 1; ++z) {
      for (int x = -1; x <= 1; ++x) {
        const auto it = m_sections.find({key.originX + x * 16, key.originY + y * 16, key.originZ + z * 16, 0});
        if (it != m_sections.end()) {
          result.sections[index] = it->second.data;
          result.neighborLifetimes[index] = it->second.lifetime;
          result.neighborRevisions[index] = it->second.boundaryRevisions[26 - index];
        }
        ++index;
      }
    }
  }
  return result;
}

bool TerrainPipeline::current(const TerrainInputs& job) const {
  const auto it = m_sections.find(job.key);
  if (job.epoch != m_epoch || job.world != m_world || job.settings != m_settings
      || it == m_sections.end() || it->second.lifetime != job.lifetime) {
    return false;
  }
  for (int index = 0; index < 27; ++index) {
    const auto neighbor = m_sections.find({job.key.originX + (index % 3 - 1) * 16,
        job.key.originY + (index / 9 - 1) * 16, job.key.originZ + (index / 3 % 3 - 1) * 16, 0});
    const auto lifetime = neighbor == m_sections.end() ? 0 : neighbor->second.lifetime;
    const auto revision = neighbor == m_sections.end() ? 0 : neighbor->second.boundaryRevisions[26 - index];
    if (job.neighborLifetimes[index] != lifetime || job.neighborRevisions[index] != revision) {
      return false;
    }
  }
  return true;
}

void TerrainPipeline::release(const TerrainInputs& job, bool retry) {
  const auto it = m_sections.find(job.key);
  if (job.epoch == m_epoch && it != m_sections.end() && it->second.lifetime == job.lifetime) {
    it->second.busy = false;
    if (retry) {
      mark(it->second);
    }
  }
}

void TerrainPipeline::invalidateSettings() {
  std::lock_guard lock(m_mutex);
  ++m_settings;
  for (auto& [key, section] : m_sections) {
    mark(section);
  }
  m_event.notify_all();
}

void TerrainPipeline::pump(const std::array<double, 3>& camera) {
  std::unique_lock lock(m_mutex);
  if (m_stopping) {
    // A renderer can be initialized again after a completed shutdown.
    if (!m_workers.empty()) {
      return;
    }
    m_stopping = false;
  }
  if (m_workers.empty()) {
    for (unsigned i = 0; i < m_workerCount; ++i) {
      m_workers.emplace_back(&TerrainPipeline::work, this);
    }
  }
  const auto limit = std::max(1u, m_workerCount * 2);
  unsigned selected = 0;
  while (m_pending != 0 && m_dispatched < limit && selected < limit && m_completedBytes < m_completedLimit) {
    auto best = m_sections.end();
    double bestScore = std::numeric_limits<double>::infinity();
    const bool oldest = (m_selection + 1) % 8 == 0;
    for (auto it = m_sections.begin(); it != m_sections.end(); ++it) {
      const auto& section = it->second;
      if (!section.pending || section.busy || !section.data) {
        continue;
      }
      const double dx = it->first.originX + 8 - camera[0];
      const double dy = it->first.originY + 8 - camera[1];
      const double dz = it->first.originZ + 8 - camera[2];
      const double score = dx * dx + dy * dy + dz * dz;
      if (best == m_sections.end() || (oldest ? section.order < best->second.order : score < bestScore)) {
        best = it;
        bestScore = score;
      }
    }
    if (best == m_sections.end()) {
      break;
    }
    ++m_selection;
    ++selected;
    auto job = inputs(best->first, best->second);
    best->second.pending = false;
    --m_pending;
    best->second.busy = true;
    ++m_dispatched;
    if (m_workerCount == 0) {
      lock.unlock();
      complete(buildTerrainGeometry(std::move(job)));
      lock.lock();
    } else {
      m_jobs.push_back(std::move(job));
    }
  }
  if (selected != 0) {
    m_event.notify_all();
  }
}

void TerrainPipeline::work() {
  for (;;) {
    TerrainInputs job;
    {
      std::unique_lock lock(m_mutex);
      m_event.wait(lock, [this] { return m_stopping || !m_jobs.empty(); });
      if (m_stopping) {
        return;
      }
      job = std::move(m_jobs.front());
      m_jobs.pop_front();
      if (!current(job)) {
        --m_dispatched;
        ++m_statistics.stale;
        release(job, true);
        continue;
      }
    }
    complete(buildTerrainGeometry(std::move(job)));
  }
}

void TerrainPipeline::complete(TerrainResult result) {
  // Completed geometry only needs its own canonical section for publication.
  for (int i = 0; i < 27; ++i) {
    if (i != 13) {
      result.inputs.sections[i].reset();
    }
  }
  std::unique_lock lock(m_mutex);
  m_event.wait(lock, [&] {
    return m_stopping || !current(result.inputs) || m_completedBytes == 0
        || m_completedBytes + result.bytes <= m_completedLimit;
  });
  --m_dispatched;
  ++m_statistics.builds;
  m_statistics.workerNanos += result.workerNanos;
  if (m_stopping || !current(result.inputs) || result.failed) {
    ++(result.failed ? m_statistics.failures : m_statistics.stale);
    release(result.inputs, true);
    return;
  }
  m_completedBytes += result.bytes;
  m_completed.push_back(std::make_unique<TerrainResult>(std::move(result)));
}

std::unique_ptr<TerrainResult> TerrainPipeline::takeCompleted() {
  std::lock_guard lock(m_mutex);
  while (!m_completed.empty()) {
    auto result = std::move(m_completed.front());
    m_completed.pop_front();

    if (current(result->inputs)) {
      return result;
    }
    m_completedBytes -= result->bytes;
    m_event.notify_all();
    ++m_statistics.stale;
    release(result->inputs, true);
  }
  return nullptr;
}

bool TerrainPipeline::finish(const TerrainResult& result, const std::function<void()>& commit) {
  std::lock_guard lock(m_mutex);
  if (result.inputs.epoch == m_epoch) {
    m_completedBytes -= result.bytes;
  }
  m_event.notify_all();
  const bool valid = current(result.inputs);
  if (valid) {
    commit();
    auto& section = m_sections.at(result.inputs.key);
    section.published = true;
    if (section.pending) {
      section.pending = false;
      --m_pending;
    }
  } else {
    ++m_statistics.stale;
  }
  release(result.inputs, !valid);
  return valid;
}

void TerrainPipeline::retry(std::unique_ptr<TerrainResult> result) {
  std::lock_guard lock(m_mutex);
  ++m_statistics.failures;
  if (current(result->inputs)) {
    m_completed.push_front(std::move(result));
  } else {
    if (result->inputs.epoch == m_epoch) {
      m_completedBytes -= result->bytes;
    }
    release(result->inputs, true);
    m_event.notify_all();
  }
}

std::vector<ChunkKey> TerrainPipeline::takeResidencyChanges() {
  std::lock_guard lock(m_mutex);
  std::vector<ChunkKey> result(m_residencyChanges.begin(), m_residencyChanges.end());
  m_residencyChanges.clear();
  return result;
}

std::uint64_t TerrainPipeline::lifetime(const ChunkKey& key) const {
  std::lock_guard lock(m_mutex);
  const auto it = m_sections.find(key);
  return it == m_sections.end() ? 0 : it->second.lifetime;
}

TerrainPipeline::Statistics TerrainPipeline::statistics() const {
  std::lock_guard lock(m_mutex);
  auto result = m_statistics;
  result.resident = m_sections.size();
  result.dispatched = m_dispatched;
  result.completed = m_completed.size();
  result.completedBytes = m_completedBytes;
  for (const auto& [key, section] : m_sections) {
    if (section.published) {
      ++result.published;
    }
    if (section.data) {
      result.canonicalBytes += sizeof(TerrainSectionData);
    }
    if (section.pending || section.busy) {
      ++result.pending;
      result.oldestNanos = std::max(result.oldestNanos, nanos(Clock::now() - section.queued));
    }
  }
  return result;
}

} // namespace mcrtx
