#include "terrain_reference.hpp"
#include "mcrtx/chunks/remix_chunk_policy.hpp"

#include <cstring>
#include <cmath>
#include <iostream>
#include <random>
#include <stdexcept>

using namespace mcrtx;
namespace {
void require(bool value, const char* message) {
  if (!value) { throw std::runtime_error(message); }
}
ChunkBlockCell cell(int id = 1, int type = 0, int pass = 0) {
  ChunkBlockCell value;
  value.blockId = id;
  value.renderType = type;
  value.renderPass = pass;
  value.terrainTiles.fill(id);
  value.materialClass = chunk::materialClassForBlock(id, 0, type);
  return value;
}
const ChunkKey kKey {0, 0, 0, 0};
const std::array<int, 6> kFull {0, 0, 0, 15, 15, 15};
std::unique_ptr<TerrainResult> waitResult(TerrainPipeline& pipeline) {
  const auto deadline = std::chrono::steady_clock::now() + std::chrono::seconds(10);
  do {
    pipeline.pump({0, 0, 0});
    if (auto result = pipeline.takeCompleted()) { return result; }
    std::this_thread::yield();
  } while (std::chrono::steady_clock::now() < deadline);
  throw std::runtime_error("Timed out waiting for terrain result");
}
bool update(TerrainPipeline& pipeline, const ChunkKey& key, std::uint64_t world,
            std::uint64_t lifetime, std::uint64_t revision, const std::array<int, 6>& bounds,
            const std::vector<ChunkBlockCell>& cells) {
  const auto deadline = std::chrono::steady_clock::now() + std::chrono::seconds(10);
  do {
    if (pipeline.update(key, world, lifetime, revision, bounds, cells)) { return true; }
    std::this_thread::yield();
  } while (std::chrono::steady_clock::now() < deadline);
  return false;
}
void ownershipAndRevisions() {
  TerrainPipeline pipeline(2);
  pipeline.reset(1);
  pipeline.allocate(kKey, 1, 1);
  std::vector<ChunkBlockCell> blocks(4096);
  blocks[0] = cell();
  require(update(pipeline, kKey, 1, 1, 1, kFull, blocks), "full update rejected");
  blocks[0] = {};
  auto first = waitResult(pipeline);
  require(first->blockCounts[0] == 1 && first->inputs.sections[13]->cells[0].blockId == 1, "capture did not own input");
  require(pipeline.finish(*first, [] {}), "first publication rejected");
  blocks[0] = cell();
  require(update(pipeline, kKey, 1, 1, 2, kFull, blocks), "identical capture rejected");
  pipeline.pump({0, 0, 0});
  require(pipeline.statistics().builds == 1 && !pipeline.takeCompleted(), "identical input regenerated geometry");
  blocks[0].blockMetadata = 8;
  require(update(pipeline, kKey, 1, 1, 3, kFull, blocks), "resolved metadata update rejected");
  pipeline.pump({0, 0, 0});
  require(pipeline.statistics().builds == 1 && !pipeline.takeCompleted(), "resolved cube metadata regenerated geometry");
  require(update(pipeline, kKey, 1, 1, 4, {1, 0, 0, 1, 0, 0}, {cell(20)}), "partial update rejected");
  auto partial = waitResult(pipeline);
  require(partial->blockCounts[0] == 2, "partial update lost untouched cell");
  require(first->inputs.sections[13]->cells[1].blockId == 0, "published storage was mutated");
  require(update(pipeline, kKey, 1, 1, 5, {0, 0, 0, 0, 0, 0}, {ChunkBlockCell {}}), "partial deletion rejected");
  require(!pipeline.finish(*partial, [] { throw std::runtime_error("stale content published"); }), "obsolete result accepted");
  auto changed = waitResult(pipeline);
  require(changed->blockCounts[0] == 1, "explicit partial empty ignored");
  require(pipeline.finish(*changed, [] {}), "changed result rejected");
  blocks.assign(4096, {});
  require(update(pipeline, kKey, 1, 1, 6, kFull, blocks), "empty update rejected");
  auto empty = waitResult(pipeline);
  require(empty->blockCounts[0] == 0 && empty->passes[0].surfacesToBuild.empty(), "explicit empty retained geometry");
  require(pipeline.finish(*empty, [] {}), "empty publication rejected");
  require(update(pipeline, kKey, 1, 1, 1, kFull, std::vector<ChunkBlockCell>(4096, cell())), "old packet was not acknowledged");
  require(pipeline.statistics().pending == 0, "out of order packet changed storage");
}
void emissionOwnership() {
  TerrainPipeline pipeline(2);pipeline.reset(1);pipeline.allocate(kKey,1,1);
  std::vector<ChunkBlockCell> cells(4096);cells[0]=cell(89);cells[0].emissionLevel=15;cells[3]=cell(89);
  require(update(pipeline,kKey,1,1,1,kFull,cells),"Emissive capture rejected");
  auto a=waitResult(pipeline);require(a->passes[0].surfacesToBuild.size()==2,"Luminous and dark surfaces merged");
  bool found=false;for(const auto &surface:a->passes[0].surfacesToBuild)found|=surface.emissionKey==89*16+15;
  require(found,"Intrinsic light missing from material key");
  require(pipeline.finish(*a,[]{}),"Emission build not published");
  cells[0].emissionLevel=0;require(update(pipeline,kKey,1,1,2,kFull,cells),"Emission removal rejected");
  auto b=waitResult(pipeline);require(b->passes[0].fingerprint!=a->passes[0].fingerprint,"Emission change reused stale mesh");
  require(a->inputs.sections[13]->cells[0].emissionLevel==15,"Emissive snapshot mutated");
}
void neighborAndLifetimes() {
  TerrainPipeline pipeline(2);
  pipeline.reset(1);
  pipeline.allocate(kKey, 1, 1);
  std::vector<ChunkBlockCell> blocks(4096);
  blocks[15] = cell();
  require(update(pipeline, kKey, 1, 1, 1, kFull, blocks), "initial update rejected");
  auto old = waitResult(pipeline);
  ChunkKey neighbor {16, 0, 0, 0};
  pipeline.allocate(neighbor, 1, 2);
  require(!pipeline.finish(*old, [] {}), "unknown neighbor lifetime was not validated");
  blocks.assign(4096, {});
  blocks[0] = cell();
  require(update(pipeline, neighbor, 1, 2, 1, kFull, blocks), "neighbor update rejected");
  bool sawKey = false;
  for (int i = 0; i < 2; ++i) {
    auto result = waitResult(pipeline);
    if (result->inputs.key == kKey) {
      sawKey = true;
      require(result->passes[0].surfacesToBuild[0].indices.size() == 30, "boundary face not eliminated");
    }
    require(pipeline.finish(*result, [] {}), "neighbor result rejected");
  }
  require(sawKey, "neighbor edit did not rebuild existing section");
  const auto before = pipeline.statistics().builds;
  require(update(pipeline, neighbor, 1, 2, 2, {8, 8, 8, 8, 8, 8}, {cell()}), "interior edit rejected");
  auto interior = waitResult(pipeline);
  require(interior->inputs.key == neighbor && pipeline.finish(*interior, [] {}), "interior edit not published");
  pipeline.pump({0, 0, 0});
  require(pipeline.statistics().builds == before + 1 && pipeline.statistics().pending == 0,
          "interior edit regenerated neighboring geometry");
  require(update(pipeline, kKey, 1, 1, 2, {8, 8, 8, 8, 8, 8}, {cell()}), "center edit rejected");
  auto independent = waitResult(pipeline);
  require(update(pipeline, neighbor, 1, 2, 3, {8, 8, 8, 8, 8, 8}, {cell(20)}), "second interior edit rejected");
  auto later = waitResult(pipeline);
  require(pipeline.finish(*later, [] {}) && pipeline.finish(*independent, [] {}),
          "out-of-order completion invalidated unchanged neighbor boundary");
  pipeline.invalidateSettings();
  old = waitResult(pipeline);
  pipeline.invalidateSettings();
  require(!pipeline.finish(*old, [] {}), "stale settings published");
  old = waitResult(pipeline);
  auto reused = old->inputs.key;
  pipeline.remove(reused, 1, old->inputs.lifetime);
  pipeline.allocate(reused, 1, 10);
  require(!pipeline.finish(*old, [] {}), "coordinate reuse accepted prior lifetime");
  require(!pipeline.update(reused, 1, 2, 99, kFull, blocks), "old lifetime update accepted");
  require(!pipeline.update(reused, 1, 10, 99, {0, 0, 0, 0, 0, 0}, {cell()}), "partial update filled unavailable section");
  pipeline.reset(2);
  require(!pipeline.update(reused, 1, 10, 99, kFull, blocks), "old world update accepted");
}
void pressureAndShutdown() {
  TerrainPipeline pipeline(2, 4096);
  pipeline.reset(1);
  std::vector<ChunkBlockCell> blocks(4096, cell());
  for (int i = 0; i < 12; ++i) {
    ChunkKey key {i * 48, 0, 0, 0};
    pipeline.allocate(key, 1, i + 1);
    require(update(pipeline, key, 1, i + 1, 1, kFull, blocks), "pressure update rejected");
  }
  auto first = waitResult(pipeline);
  require(first->bytes > 4096, "oversized test has no oversized result");
  const auto stats = pipeline.statistics();
  require(stats.dispatched <= 4 && stats.completed <= 1, "dispatch/completion bound exceeded");
  pipeline.retry(std::move(first));
  auto retried = pipeline.takeCompleted();
  require(retried != nullptr && pipeline.finish(*retried, [] {}), "publication retry lost completed work");
  pipeline.pump({0, 0, 0});
  pipeline.stop();
  require(pipeline.statistics().dispatched == 0, "shutdown did not join active workers");
}
void fairnessAndReset() {
  TerrainPipeline pipeline(0);
  pipeline.reset(1);
  const ChunkKey oldest {100000, 0, 0, 0};
  std::vector<ChunkBlockCell> blocks(4096);
  pipeline.allocate(oldest, 1, 1);
  require(update(pipeline, oldest, 1, 1, 1, kFull, blocks), "oldest admission failed");
  for (int i = 0; i < 10; ++i) {
    ChunkKey near {i * 48, 0, 0, 0};
    pipeline.allocate(near, 1, i + 2);
    require(update(pipeline, near, 1, i + 2, 1, kFull, blocks), "near admission failed");
  }
  for (int i = 0; i < 8; ++i) {
    auto next = waitResult(pipeline);
    require((next->inputs.key == oldest) == (i == 7), "every eighth dispatch did not select oldest work");
    require(pipeline.finish(*next, [] {}), "fairness publication rejected");
  }
  auto held = waitResult(pipeline);
  pipeline.reset(2);
  require(!pipeline.finish(*held, [] {}), "world reset accepted held result");
  require(pipeline.statistics().completedBytes == 0, "reset underflowed held result accounting");
  pipeline.stop();
  pipeline.reset(3);
  pipeline.allocate(kKey, 3, 30);
  pipeline.pump({0, 0, 0});
  require(update(pipeline, kKey, 3, 30, 1, kFull, blocks), "renderer reinitialization rejected new world");
  auto restarted = waitResult(pipeline);
  require(pipeline.finish(*restarted, [] {}), "renderer restart did not publish");
}
void geometryReference() {
  const int shapes[][3] = {{1,0,0}, {2,0,0}, {8,4,1}, {10,4,0}, {18,0,0}, {20,0,0}, {79,0,1}, {90,0,1},
    {85,11,0}, {55,5,0}, {37,1,0}, {59,6,0}, {50,2,0}, {65,8,0}, {66,9,0}, {53,10,0}, {69,12,0},
    {77,12,0}, {64,7,0}, {81,13,0}, {26,14,0}, {93,15,0}, {29,16,0}, {34,17,0}, {89,0,0}};
  std::mt19937 random(12345);
  TerrainInputs inputs;
  inputs.key = {-16, 16, -16, 0};
  for (auto& data : inputs.sections) {
    auto next = std::make_shared<TerrainSectionData>();
    for (int i = 0; i < 160; ++i) {
      const auto& shape = shapes[random() % std::size(shapes)];
      const int index = random() % 4096;
      auto value = cell(shape[0], shape[1], shape[2]);
      value.blockMetadata = random() % 16;
      value.terrainTiles = {1, 2, 3, 4, 5, 6};
      value.blockColor = random() & 0xFFFFFF;
      value.liquidHeights = {0.6f, 0.7f, 0.8f, 0.9f};
      value.liquidFlowAngle = 0.5f;
      next->cells[index] = value;
      next->occupancy[index] = 1;
    }
    // Exercise fence/redstone connections on every section corner.
    for (int index : {0, 15, 240, 255, 3840, 3855, 4080, 4095}) {
      next->occupancy[index] = 1;
      next->cells[index] = cell(index & 1 ? 85 : 55, index & 1 ? 11 : 5);
    }
    data = next;
  }
  TerrainPipeline pipeline(0);
  pipeline.reset(1);
  for (int i = 0; i < 27; ++i) {
    ChunkKey key {inputs.key.originX + (i % 3 - 1) * 16, inputs.key.originY + (i / 9 - 1) * 16,
                  inputs.key.originZ + (i / 3 % 3 - 1) * 16, 0};
    pipeline.allocate(key, 1, i + 1);
    const auto& cells = inputs.sections[i]->cells;
    require(update(pipeline, key, 1, i + 1, 1, kFull, {cells.begin(), cells.end()}), "fixture admission failed");
  }
  pipeline.pump({double(inputs.key.originX + 8), double(inputs.key.originY + 8), double(inputs.key.originZ + 8)});
  auto actual = pipeline.takeCompleted();
  require(actual != nullptr && actual->inputs.key == inputs.key, "fixture center not selected");
  for (int pass = 0; pass < 2; ++pass) {
    ReferenceTerrainEmitter reference;
    for (std::size_t i = 0; i < reference.terrainMaterialHandles_.size(); ++i) {
      reference.terrainMaterialHandles_[i] = reinterpret_cast<remixapi_MaterialHandle>(i + 1);
    }
    for (int i = 0; i < 27; ++i) {
      ChunkKey key {inputs.key.originX + (i % 3 - 1) * 16, inputs.key.originY + (i / 9 - 1) * 16,
                    inputs.key.originZ + (i / 3 % 3 - 1) * 16, pass};
      auto& section = reference.chunkMeshes_[key];
      section.cells = inputs.sections[i]->cells;
      for (int j = 0; j < 4096; ++j) {
        section.occupancy[j] = inputs.sections[i]->occupancy[j] && section.cells[j].renderPass == pass;
      }
    }
    auto key = inputs.key;
    key.renderPass = pass;
    ReferenceTerrainBuild expected;
    reference.emit(key, reference.chunkMeshes_.at(key), expected);
    const auto& built = actual->passes[pass];
    require(built.surfacesToBuild.size() == expected.surfacesToBuild.size(), "surface count changed");
    for (std::size_t i = 0; i < built.surfacesToBuild.size(); ++i) {
      const auto& a = built.surfacesToBuild[i];
      const auto& b = expected.surfacesToBuild[i];
      require(a.materialClass + 1 == reinterpret_cast<std::uintptr_t>(b.materialHandle), "material mapping changed");
      require(a.indices == b.indices && a.vertices.size() == b.vertices.size(), "indices/winding changed");
      for (std::size_t j = 0; j < a.vertices.size(); ++j) {
        require(std::memcmp(&a.vertices[j], &b.vertices[j], sizeof(remixapi_HardcodedVertex)) == 0, "vertex/UV/color changed");
      }
    }

  }
}
void capturePolicy() {
  require(chunk::shouldCaptureBlock(8, 4, 0), "opaque water pass was rejected");
  require(chunk::shouldCaptureBlock(9, 4, 0), "opaque flowing water pass was rejected");
  require(chunk::shouldCaptureBlock(8, 4, 1), "transparent water pass was rejected");
  require(chunk::shouldCaptureBlock(79, 0, 0), "opaque ice pass was rejected");
  require(chunk::shouldCaptureBlock(79, 0, 1), "transparent ice pass was rejected");
  require(!chunk::shouldCaptureBlock(8, 99, 0), "unsupported water render type was accepted");
}
void directFireGeometry() {
  TerrainInputs inputs;
  auto section = std::make_shared<TerrainSectionData>();
  section->cells[0] = cell(1, 0, 0);
  section->cells[256] = cell(51, 3, 0);
  section->occupancy[0] = section->occupancy[256] = 1;
  inputs.sections[13] = section;
  ChunkGeometryBuild result;
  emitTerrainGeometry(inputs, 0, result);
  bool found = false;
  for (const auto &surface : result.surfacesToBuild) {
    if (surface.materialClass != 1) continue;
    found = !surface.indices.empty();
    for (const auto &vertex : surface.vertices) {
      require(vertex.texcoord[0] >= 15.f / 16 && vertex.texcoord[0] <= 1,
          "fire does not use vanilla atlas column");
      require(vertex.texcoord[1] >= 1.f / 16 && vertex.texcoord[1] <= 3.f / 16,
          "fire does not use vanilla animated atlas rows");
    }
  }
  require(found, "fire above a solid block emitted no geometry");
}
}
void torchLighting() {
  TerrainSectionData section;
  for (unsigned i = 0; i < 5; ++i) {
    section.cells[17 + 256 * i] = cell(50, 2);
    section.cells[17 + 256 * i].blockMetadata = i + 1;
  }
  section.cells[3] = cell(75, 2);
  section.cells[4] = cell(76, 2);
  section.cells[5] = cell(10); // Lava is outside the torch-light scope.
  const auto torches = terrainTorches(section);
  require(torches.size() == 7, "torch extraction included a non-torch emitter");
  require(torches[0].cell == 3 && torches[0].radiance[0] == 0, "unlit redstone lost its zero-power slot");
  require(torches[1].radiance[0] > torches[1].radiance[1] * 10, "redstone is not red");
  const float x[] = {1.25f, 1.75f, 1.5f, 1.5f, 1.5f};
  const float z[] = {1.5f, 1.5f, 1.25f, 1.75f, 1.5f};
  for (unsigned i = 0; i < 5; ++i) {
    const auto& torch = torches[i + 2];
    require(torch.position[0] == x[i] && torch.position[2] == z[i] &&
            std::abs(torch.position[1] - (i + (i < 4 ? .945f : .745f))) < 1.e-5f,
            "torch flame disagrees with block orientation or cell coordinates");
    require(torch.radius > 0 && torch.radiance[0] > torches[1].radiance[0], "normal torch intensity/radius invalid");
  }
  TerrainPipeline pipeline(1);
  pipeline.reset(1); pipeline.allocate(kKey, 1, 1);
  require(update(pipeline, kKey, 1, 1, 1, kFull, {section.cells.begin(), section.cells.end()}), "torch admission failed");
  auto first = waitResult(pipeline);
  require(first->torches == torches, "worker lost captured torches");
  require(update(pipeline, kKey, 1, 1, 2, {3, 0, 0, 3, 0, 0}, {cell(76, 2)}), "redstone toggle rejected");
  require(!pipeline.finish(*first, [] { throw std::runtime_error("stale torch result committed"); }), "stale torches accepted");
  auto next = waitResult(pipeline);
  require(next->torches[0].cell == 3 && next->torches[0].radiance[0] > 0, "redstone toggle did not update same cell");
  require(pipeline.finish(*next, [] {}), "torch update not committed");
  pipeline.remove(kKey, 1, 1);
  require(pipeline.lifetime(kKey) == 0, "unloaded torch section survived");
}

int main() {
  emissionOwnership();
  try {
    capturePolicy();
    torchLighting();
    geometryReference();
    directFireGeometry();
    ownershipAndRevisions();
    neighborAndLifetimes();
    pressureAndShutdown();
    fairnessAndReset();
    std::cout << "Terrain ownership, revisions, geometry, bounds and shutdown passed\n";
  } catch (const std::exception& error) {
    std::cerr << error.what() << '\n';
    return 1;
  }
}
