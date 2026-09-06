#pragma once

#include <array>
#include <chrono>
#include <condition_variable>
#include <deque>
#include <functional>
#include <memory>
#include <mutex>
#include <thread>
#include <unordered_map>
#include <unordered_set>

#include "mcrtx/chunks/remix_chunk_build.hpp"

namespace mcrtx {

struct TerrainInputs {
  ChunkKey key;
  std::uint64_t world {0}, epoch {0}, lifetime {0}, revision {0}, settings {0};
  std::array<std::shared_ptr<const TerrainSectionData>, 27> sections;
  std::array<std::uint64_t, 27> neighborLifetimes {};
  std::array<std::uint64_t, 27> neighborRevisions {};
  std::chrono::steady_clock::time_point queued;
  const TerrainSectionData* neighbor(int x, int y, int z) const;
};

struct TerrainResult {
  TerrainInputs inputs;
  std::array<ChunkGeometryBuild, 2> passes;
  std::array<std::size_t, 2> blockCounts {};
  std::size_t bytes {0};
  std::uint64_t workerNanos {0};
  bool failed {false};
};

void emitTerrainGeometry(const TerrainInputs& inputs, int pass, ChunkGeometryBuild& build);
TerrainResult buildTerrainGeometry(TerrainInputs inputs);

class TerrainPipeline {
public:
  explicit TerrainPipeline(unsigned workers = std::thread::hardware_concurrency() < 4 ? 1 : 2,
                           std::size_t completedLimit = 128 * 1024 * 1024);
  ~TerrainPipeline();
  TerrainPipeline(const TerrainPipeline&) = delete;
  TerrainPipeline& operator=(const TerrainPipeline&) = delete;

  void allocate(const ChunkKey& key, std::uint64_t world, std::uint64_t lifetime);
  void remove(const ChunkKey& key, std::uint64_t world, std::uint64_t lifetime);
  void reset(std::uint64_t world);
  void clear();
  void stop();
  bool update(const ChunkKey& key, std::uint64_t world, std::uint64_t lifetime, std::uint64_t revision,
              const std::array<int, 6>& bounds, const std::vector<ChunkBlockCell>& cells);
  void invalidateSettings();
  void pump(const std::array<double, 3>& camera);
  std::unique_ptr<TerrainResult> takeCompleted();
  bool finish(const TerrainResult& result, const std::function<void()>& commit);
  void retry(std::unique_ptr<TerrainResult> result);
  std::vector<ChunkKey> takeResidencyChanges();
  std::uint64_t lifetime(const ChunkKey& key) const;
  struct Statistics {
    std::uint64_t captures {0}, unchanged {0}, builds {0}, stale {0}, failures {0}, workerNanos {0}, lockNanos {0};
    std::size_t resident {0}, published {0}, pending {0}, dispatched {0}, completed {0}, completedBytes {0}, canonicalBytes {0};
    std::uint64_t oldestNanos {0};
  };
  Statistics statistics() const;

private:
  struct Section {
    std::uint64_t lifetime {0}, revision {0};
    std::shared_ptr<const TerrainSectionData> data;
    std::array<std::uint64_t, 27> boundaryRevisions {};
    bool pending {false}, busy {false}, published {false};
    std::uint64_t order {0};
    std::chrono::steady_clock::time_point queued;
  };
  using Clock = std::chrono::steady_clock;
  void mark(Section& section);
  void markNeighbors(const ChunkKey& key, std::uint32_t affected = (1u << 27) - 1);
  TerrainInputs inputs(const ChunkKey& key, const Section& section) const;
  bool current(const TerrainInputs& inputs) const;
  void release(const TerrainInputs& inputs, bool retry);
  void work();
  void complete(TerrainResult result);
  void clearLocked();

  mutable std::mutex m_mutex;
  std::condition_variable m_event;
  std::unordered_map<ChunkKey, Section, ChunkKeyHash> m_sections;
  std::unordered_set<ChunkKey, ChunkKeyHash> m_residencyChanges;
  std::deque<TerrainInputs> m_jobs;
  std::deque<std::unique_ptr<TerrainResult>> m_completed;
  std::vector<std::thread> m_workers;
  unsigned m_workerCount;
  std::size_t m_completedLimit, m_completedBytes {0}, m_dispatched {0}, m_pending {0};
  std::uint64_t m_world {0}, m_epoch {0}, m_settings {0}, m_order {0}, m_selection {0};
  bool m_stopping {false};
  Statistics m_statistics;
};

} // namespace mcrtx
