#include "mcrtx/core/remix_renderer.hpp"
#include "mcrtx/chunks/remix_chunk_policy.hpp"
#include "mcrtx/core/remix_render_common.hpp"

#include <cstdlib>
#include <iostream>
#include <set>

namespace mcrtx {

class UndergroundVisibilityTest {
  inline static std::set<remixapi_MeshHandle> live;
  inline static int creates = 0;
  inline static int destroys = 0;

  static void require(bool condition, const char* message) {
    if (!condition) {
      std::cerr << message << '\n';
      std::exit(1);
    }
  }
  static remixapi_ErrorCode REMIXAPI_CALL create(const remixapi_MeshInfo* info, remixapi_MeshHandle* result) {
    *result = reinterpret_cast<remixapi_MeshHandle>(info->hash);
    require(live.insert(*result).second, "Duplicate live mesh hash");
    ++creates;
    return REMIXAPI_ERROR_CODE_SUCCESS;
  }
  static remixapi_ErrorCode REMIXAPI_CALL destroy(remixapi_MeshHandle handle) {
    require(live.erase(handle) == 1, "Destroyed unknown/in-flight mesh");
    ++destroys;
    return REMIXAPI_ERROR_CODE_SUCCESS;
  }
public:
  static void run() {
    RemixRenderer renderer;
    renderer.initialized_ = true;
    renderer.standaloneOutputWindow_ = true;
    renderer.heldTorchLightsEnabled_ = false;
    renderer.remix_.CreateMesh = create;
    renderer.remix_.DestroyMesh = destroy;
    renderer.terrainMaterialHandles_.fill(reinterpret_cast<remixapi_MaterialHandle>(2));
    auto section = std::make_shared<UndergroundSection>();
    auto labels = std::make_shared<UndergroundSection::Labels>();
    section->revision = 1;
    std::array<std::uint64_t, 64> hidden {};
    for (int i = 0; i < 4096; ++i) {
      (*labels)[i] = (i & 15) < 8 ? 0 : 1;
    }
    section->labels = labels;
    hidden[0] = 1;
    const ChunkKey key {0, 0, 0, 0};
    renderer.setUndergroundCullingEnabled(true);
    renderer.updateUndergroundTopology(key, section);
    renderer.updateUndergroundVisibility(key, 1, hidden);
    auto& mesh = renderer.chunkMeshes_[key];
    mesh.meshHandle = reinterpret_cast<remixapi_MeshHandle>(1);
    mesh.meshFingerprint = 123;
    mesh.undergroundCaptureRevision = 1;
    mesh.blockCount = 2;
    mesh.hasOccupancy = true;
    for (int x : {2, 12}) {
      const int i = chunk::blockIndex(x, 2, 2);
      mesh.occupancy[i] = 1;
      mesh.cells[i].blockId = 1;
    }
    renderer.torchLights_[{2, 2, 2}].handle = reinterpret_cast<remixapi_LightHandle>(10);
    renderer.torchLights_[{12, 2, 2}].handle = reinterpret_cast<remixapi_LightHandle>(11);
    DynamicEntityFrameInstance entity;
    entity.entityId = 42;
    entity.meshHandle = reinterpret_cast<remixapi_MeshHandle>(3);
    entity.boneTransforms.resize(1);
    entity.boundsMin = {2,2,2}; entity.boundsMax = {3,3,3};
    renderer.dynamicEntityFrameInstances_ = {entity};
    renderer.dynamicEntityFrameInstanceCount_ = 1;
    ParticleQuad particle;
    particle.positions.fill(3);
    renderer.particleQuads_.push_back(particle);

    const auto prepare = [&]() {
      FrameRenderSnapshot snapshot;
      bool empty = false;
      require(renderer.prepareFrameSnapshotLocked(snapshot, empty), "Snapshot failed");
      return snapshot;
    };
    const auto finish = [&]() {
      renderer.renderSubmissionInFlight_ = false;
      renderer.flushDeferredDestroyQueuesLocked();
    };
    renderer.present();
    const auto publishedSections = renderer.publishedUndergroundFrame_.sections;
    const auto publishedLabels = publishedSections->at(key)->labels;
    auto snapshot = prepare();
    require(creates == 2 && mesh.visibilityGroups.size() == 2, "Mixed section did not split by pocket");
    require(snapshot.chunkMeshes.size() == 1 && renderer.undergroundHiddenGroups_ == 1, "Hidden group submitted");
    require(snapshot.torchLights.size() == 1 && renderer.undergroundHiddenLights_ == 1, "Geometry/light mismatch");
    require(snapshot.dynamicEntities.empty() && renderer.undergroundHiddenEntities_ == 1, "Hidden mob/sign submitted");
    require(renderer.undergroundHiddenParticles_ == 1, "Hidden particle submitted");
    const auto oldVisible = snapshot.chunkMeshes.front().meshHandle;
    renderer.updateUndergroundVisibility(key, 1, {});
    require(renderer.undergroundFrame_.sections != publishedSections, "Mask write did not detach published map");
    require(renderer.undergroundFrame_.sections->at(key)->labels == publishedLabels, "Mask write copied topology");
    require(publishedSections->at(key)->hiddenPockets == hidden, "Mask write mutated published state");
    const auto unchangedSections = renderer.undergroundFrame_.sections;
    const auto unchangedRevision = renderer.undergroundFrame_.revision;
    renderer.updateUndergroundVisibility(key, 1, {});
    require(renderer.undergroundFrame_.sections == unchangedSections && renderer.undergroundFrame_.revision == unchangedRevision,
      "Identical mask changed the frame revision or copied the map");
    require(creates == 2 && destroys == 0 && live.count(oldVisible), "Input update mutated in-flight meshes");
    finish();
    snapshot = prepare();
    require(renderer.activeUndergroundFrame_.sections == publishedSections, "Unchanged frame copied section map");
    require(snapshot.chunkMeshes.size() == 1 && snapshot.torchLights.size() == 1, "Unpublished visibility leaked");
    finish();
    renderer.particleQuads_.clear();
    renderer.present();
    snapshot = prepare();
    require(snapshot.chunkMeshes.size() == 1 && snapshot.chunkMeshes.front().meshHandle == mesh.meshHandle
      && snapshot.torchLights.size() == 2, "Reveal did not reuse full mesh");
    require(creates == 2 && destroys == 0, "Visibility changes rebuilt meshes");
    finish();

    renderer.updateUndergroundTopology({16, 0, 0, 0}, section);
    renderer.present(); snapshot = prepare();
    require(creates == 2 && destroys == 0, "New neighbor changed identical geometry handles");
    require(mesh.visibilityCheckedTopology == renderer.activeUndergroundFrame_.topologyRevision && mesh.visibilityCurrent,
      "Newly known neighbor did not refresh ownership validation");
    finish();

    auto changed = std::make_shared<UndergroundSection>(*section);
    changed->revision = 2;
    renderer.updateUndergroundTopology(key, changed);
    renderer.updateUndergroundVisibility(key, 1, hidden);
    renderer.present(); snapshot = prepare();
    require(snapshot.chunkMeshes.size() == 1 && snapshot.chunkMeshes.front().meshHandle == mesh.meshHandle, "Geometry/topology mismatch must retain full mesh");
    finish();
    mesh.undergroundCaptureRevision = 2;
    renderer.present(); snapshot = prepare();
    require(snapshot.chunkMeshes.size() == 1 && snapshot.chunkMeshes.front().meshHandle == mesh.meshHandle, "Stale visibility hid new topology");
    require(creates == 2 && destroys == 0, "Regrouping identical geometry changed handles");
    finish();
    renderer.setUndergroundCullingEnabled(false);
    renderer.present(); snapshot = prepare();
    require(snapshot.chunkMeshes.size() == 1 && snapshot.chunkMeshes.front().meshHandle == mesh.meshHandle, "OFF did not restore full mesh");
    finish();

    UndergroundFrame frame;
    frame.enabled = true;
    auto negative = std::make_shared<UndergroundSection>(*section);
    negative->hiddenPockets.fill(~std::uint64_t {0});
    frame.editSections()[{-16, 0, -16, 0}] = negative;
    require(frame.isHidden({-14,2,-14},{-13,3,-13}), "Negative coordinates");
    require(!frame.isHidden({-1,2,-1},{1,3,1}), "Unknown neighbor must retain straddling bounds");
    require(!frame.isHidden({0,0,0},{1000000,1,1}), "Unbounded queries must retain");
    frame.enabled = false;
    require(!frame.isHidden({-14,2,-14},{-13,3,-13}), "Disabled bounds culling");

    renderer.destroyUndergroundMeshes(mesh);
    renderer.activeUndergroundFrame_.enabled = true;
    renderer.fireMaterialHandle_ = reinterpret_cast<remixapi_MaterialHandle>(12);
    const int fireIndex = chunk::blockIndex(12, 2, 2);
    mesh.cells[fireIndex].renderType = detail::kFireBlockRenderType;
    mesh.fireCellIndices.push_back(static_cast<std::uint16_t>(fireIndex));
    mesh.occupancy[chunk::blockIndex(12, 1, 2)] = 1;
    require(renderer.rebuildFireMesh({}), "Fire build failed");
    require(renderer.fireMeshHandle_ != nullptr, "Fire fixture emitted no geometry");
    bool reused = false;
    for (int attempt = 0; attempt < 8 && !reused; ++attempt) {
      const auto frameIndex = renderer.lastFireAnimationFrame_;
      const auto handle = renderer.fireMeshHandle_;
      require(renderer.rebuildFireMesh({}), "Fire rebuild failed");
      if (frameIndex == renderer.lastFireAnimationFrame_) {
        require(handle == renderer.fireMeshHandle_, "Unchanged visibility rebuilt fire");
        reused = true;
      }
    }
    require(reused, "Could not verify fire reuse within an animation frame");
    ++renderer.activeUndergroundFrame_.revision;
    const int beforeVisibilityChange = creates;
    renderer.rebuildFireMesh({});
    require(creates == beforeVisibilityChange + 1, "Visibility change failed to refresh fire");
    renderer.destroyFireMesh();
    renderer.resetUndergroundCulling();
    require(renderer.undergroundFrame_.sections->empty() && !renderer.undergroundFrame_.enabled, "Toggle reset retained stale topology");
    require(!publishedSections->empty(), "Reset changed an older published snapshot");
    require(live.empty() && destroys == creates, "Visibility mesh leak");
    std::cout << "Underground native visibility and publication tests passed\n";
  }
};
} // namespace mcrtx

int main() { mcrtx::UndergroundVisibilityTest::run(); }
