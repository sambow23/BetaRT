#include "mcrtx/core/remix_renderer.hpp"
#include "mcrtx/chunks/remix_chunk_policy.hpp"

#include <iostream>
#include <stdexcept>
#include <unordered_set>

namespace mcrtx {
class TerrainPublicationTest {
  inline static int meshCalls = 0, lightCalls = 0, failMesh = 0, failLight = 0;
  inline static std::unordered_set<remixapi_MeshHandle> meshes;
  inline static std::unordered_set<remixapi_LightHandle> lights;
  inline static std::thread::id rendererThread;
  static void require(bool value, const char* message) { if (!value) { throw std::runtime_error(message); } }
  static remixapi_ErrorCode REMIXAPI_CALL createMesh(const remixapi_MeshInfo* info, remixapi_MeshHandle* handle) {
    require(std::this_thread::get_id() == rendererThread, "worker called Remix");
    if (++meshCalls == failMesh) { return REMIXAPI_ERROR_CODE_GENERAL_FAILURE; }
    *handle = reinterpret_cast<remixapi_MeshHandle>(info->hash);
    meshes.insert(*handle);
    return REMIXAPI_ERROR_CODE_SUCCESS;
  }
  static remixapi_ErrorCode REMIXAPI_CALL destroyMesh(remixapi_MeshHandle handle) {
    require(meshes.erase(handle) == 1, "mesh destroyed twice or still in flight");
    return REMIXAPI_ERROR_CODE_SUCCESS;
  }
  static remixapi_ErrorCode REMIXAPI_CALL createLight(const remixapi_LightInfo* info, remixapi_LightHandle* handle) {
    require(std::this_thread::get_id() == rendererThread, "worker created light");
    if (++lightCalls == failLight) { return REMIXAPI_ERROR_CODE_GENERAL_FAILURE; }
    *handle = reinterpret_cast<remixapi_LightHandle>(info->hash);
    lights.insert(*handle);
    return REMIXAPI_ERROR_CODE_SUCCESS;
  }
  static remixapi_ErrorCode REMIXAPI_CALL updateLight(remixapi_LightHandle, const remixapi_LightInfo*) {
    return REMIXAPI_ERROR_CODE_SUCCESS;
  }
  static remixapi_ErrorCode REMIXAPI_CALL destroyLight(remixapi_LightHandle handle) {
    require(lights.erase(handle) == 1, "light destroyed twice or still in flight");
    return REMIXAPI_ERROR_CODE_SUCCESS;
  }
  static std::unique_ptr<TerrainResult> result(RemixRenderer& renderer) {
    const auto deadline = std::chrono::steady_clock::now() + std::chrono::seconds(10);
    do {
      renderer.terrain_.pump({0, 0, 0});
      if (auto next = renderer.terrain_.takeCompleted()) { return next; }
      std::this_thread::yield();
    } while (std::chrono::steady_clock::now() < deadline);
    throw std::runtime_error("terrain result timeout");
  }
  static ChunkBlockCell cell(int id, int type, int pass) {
    ChunkBlockCell result;
    result.blockId = id;
    result.renderType = type;
    result.renderPass = pass;
    result.materialClass = chunk::materialClassForBlock(id, 0, type);
    return result;
  }
public:
  static void run() {
    RemixRenderer renderer;
    rendererThread = std::this_thread::get_id();
    renderer.initialized_ = true;
    renderer.remix_.CreateMesh = createMesh;
    renderer.remix_.DestroyMesh = destroyMesh;
    renderer.remix_.CreateLight = createLight;
    renderer.remix_.DestroyLight = destroyLight;
    renderer.remix_.UpdateLightDefinition = updateLight;
    renderer.terrainMaterialHandles_.fill(reinterpret_cast<remixapi_MaterialHandle>(1));
    const ChunkKey key {0, 0, 0, 0}, pass1 {0, 0, 0, 1};
    const std::array<int, 6> bounds {0, 0, 0, 15, 15, 15};
    renderer.terrain_.reset(1);
    renderer.terrain_.allocate(key, 1, 1);
    renderer.terrain_.takeResidencyChanges();
    std::vector<ChunkBlockCell> cells(4096);
    cells[0] = cell(50, 2, 0);
    cells[15] = cell(8, 4, 1);
    const auto capture = [&](std::uint64_t revision) {
      const auto deadline = std::chrono::steady_clock::now() + std::chrono::seconds(10);
      do {
        if (renderer.terrain_.update(key, 1, 1, revision, bounds, cells)) { return true; }
        std::this_thread::yield();
      } while (std::chrono::steady_clock::now() < deadline);
      return false;
    };
    require(capture(1), "capture rejected");
    auto first = result(renderer);
    require(renderer.publishTerrainSection(*first), "initial publication failed");
    auto old0 = renderer.chunkMeshes_.at(key).meshHandle;
    auto old1 = renderer.chunkMeshes_.at(pass1).meshHandle;
    auto oldLight = renderer.torchLights_.begin()->second.handle;
    require(meshes.size() == 2 && lights.size() == 1, "two passes/light not published together");
    renderer.renderSubmissionInFlight_ = true;
    cells[0].blockMetadata = 1;
    cells[1] = cell(50, 2, 0);
    cells[15].liquidHeights.fill(0.5f);
    require(capture(2), "edit rejected");
    auto edited = result(renderer);
    failMesh = meshCalls + 2;
    require(!renderer.publishTerrainSection(*edited), "second-pass mesh failure ignored");
    require(renderer.chunkMeshes_.at(key).meshHandle == old0 && renderer.chunkMeshes_.at(pass1).meshHandle == old1,
            "first pass replaced before second pass ready");
    require(renderer.torchLights_.begin()->second.handle == oldLight, "mesh failure modified lights");
    failMesh = 0;
    failLight = lightCalls + 2;
    require(!renderer.publishTerrainSection(*edited), "light creation failure ignored");
    require(renderer.torchLights_.size() == 1 && renderer.torchLights_.begin()->second.handle == oldLight,
            "light rollback failed");
    require(meshes.count(old0) && meshes.count(old1) && lights.count(oldLight), "in-flight resource destroyed");
    failLight = 0;
    require(renderer.publishTerrainSection(*edited), "retry failed");
    require(renderer.chunkMeshes_.at(key).meshHandle != old0 && renderer.chunkMeshes_.at(pass1).meshHandle != old1,
            "complete replacement not committed");
    require(meshes.count(old0) && meshes.count(old1) && lights.count(oldLight), "publication destroyed in-flight resource");
    renderer.renderSubmissionInFlight_ = false;
    renderer.flushDeferredDestroyQueuesLocked();
    require(!meshes.count(old0) && !meshes.count(old1) && !lights.count(oldLight), "old resources were not retired");
    require(meshes.count(renderer.chunkMeshes_.at(key).meshHandle)
            && meshes.count(renderer.chunkMeshes_.at(pass1).meshHandle), "retry retired newly reused mesh handle");
    const auto preservedLight = renderer.torchLights_.at({0, 0, 0}).handle;
    cells[2] = cell(1, 0, 0);
    require(capture(3), "unrelated edit rejected");
    auto unrelated = result(renderer);
    require(renderer.publishTerrainSection(*unrelated), "unrelated edit failed");
    require(renderer.torchLights_.at({0, 0, 0}).handle == preservedLight, "unchanged light handle recreated");
    renderer.terrainSubmissionsDirty_ = true;
    FrameRenderSnapshot snapshot;
    bool logEmpty = false;
    require(renderer.prepareFrameSnapshotLocked(snapshot, logEmpty), "snapshot failed");
    const auto creates = meshCalls;
    renderer.camera_.forward[0] = 1;
    renderer.camera_.forward[2] = -1;
    require(renderer.prepareFrameSnapshotLocked(snapshot, logEmpty), "rotated snapshot failed");
    require(snapshot.chunkMeshes.size() == 2 && meshCalls == creates, "rotation changed residency or rebuilt geometry");
    renderer.terrain_.remove(key, 1, 1);
    renderer.publishTerrain();
    require(renderer.chunkMeshes_.empty(), "unload did not remove both passes");
    renderer.renderSubmissionInFlight_ = false;
    renderer.flushDeferredDestroyQueuesLocked();
    require(meshes.empty() && lights.empty(), "resource leaked on unload");
    renderer.initialized_ = false;
  }
};
}
int main() {
  try { mcrtx::TerrainPublicationTest::run(); std::cout << "Atomic publication and lifetime checks passed\n"; }
  catch (const std::exception& error) { std::cerr << error.what() << '\n'; return 1; }
}
