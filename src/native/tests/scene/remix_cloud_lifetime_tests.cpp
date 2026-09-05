#include "mcrtx/core/remix_renderer.hpp"

#include <algorithm>
#include <cmath>
#include <cstdlib>
#include <iostream>

namespace mcrtx {

class CloudMeshTest {
  inline static remixapi_MeshHandle liveMesh = nullptr;
  inline static int creates = 0;
  inline static int destroys = 0;

  static void require(bool condition, const char* message) {
    if (!condition) {
      std::cerr << message << '\n';
      std::exit(1);
    }
  }

  static remixapi_ErrorCode REMIXAPI_CALL createMesh(const remixapi_MeshInfo* info, remixapi_MeshHandle* handle) {
    require(liveMesh == nullptr, "Stable hash registered before old mesh was destroyed");
    require(info->surfaces_count == 1 && info->surfaces_values[0].indices_count > 0, "Empty API mesh");
    *handle = reinterpret_cast<remixapi_MeshHandle>(info->hash);
    liveMesh = *handle;
    ++creates;
    return REMIXAPI_ERROR_CODE_SUCCESS;
  }

  static remixapi_ErrorCode REMIXAPI_CALL destroyMesh(remixapi_MeshHandle handle) {
    require(handle == liveMesh, "Deferred destroy targeted another generation");
    liveMesh = nullptr;
    ++destroys;
    return REMIXAPI_ERROR_CODE_SUCCESS;
  }

public:
  static void run() {
    RemixRenderer renderer;
    renderer.initialized_ = true;
    renderer.standaloneOutputWindow_ = true;
    renderer.remix_.CreateMesh = createMesh;
    renderer.remix_.DestroyMesh = destroyMesh;
    renderer.fancyCloudMaterialHandle_ = reinterpret_cast<remixapi_MaterialHandle>(1);
    renderer.cloudMaterialHandle_ = reinterpret_cast<remixapi_MaterialHandle>(2);
    renderer.cloudMask_ = {256, 256, std::vector<std::uint8_t>(256 * 256, 255)};
    const auto finish = [&]() {
      renderer.renderSubmissionInFlight_ = false;
      renderer.flushDeferredDestroyQueuesLocked();
    };
    const auto prepare = [&]() {
      FrameRenderSnapshot snapshot;
      bool logEmpty = false;
      require(renderer.prepareFrameSnapshotLocked(snapshot, logEmpty), "Snapshot preparation failed");
      return snapshot;
    };

    renderer.updateCloudLayer(true, 0, 0, 0, 128, 0, 0, 1, 1, 1);
    require(creates == 0, "Game update created a mesh before publication");
    require(renderer.present(), "Frame publication failed");
    auto snapshot = prepare();
    const auto fancyHash = snapshot.cloudMeshHandle;
    require(fancyHash != nullptr && creates == 1 && destroys == 0, "First cloud mesh missing");
    require(snapshot.submittedCloudQuads == 6, "Solid mask is not an exterior-only shell");

    renderer.updateCloudLayer(true, 0, 0, 0, 130, 0.5f, 0, 1, 1, 1);
    require(creates == 1 && destroys == 0, "In-flight mesh modified by game update");
    finish();
    snapshot = prepare();
    require(snapshot.cloudTransformY == 128, "Unpublished cloud state leaked into snapshot");
    finish();
    renderer.present();
    snapshot = prepare();
    require(creates == 1 && destroys == 0 && snapshot.cloudMeshHandle == fancyHash, "Drift recreated mesh or changed hash");
    require(snapshot.cloudTransformX == -0.5f && snapshot.cloudTransformY == 130, "Instance translation did not follow clouds");

    renderer.updateCloudLayer(true, 24, 0, -24, 130, 0.5f, 0, 1, 1, 1);
    renderer.present();
    require(creates == 1 && destroys == 0, "Publishing replaced an in-flight handle");
    finish();
    snapshot = prepare();
    require(creates == 2 && destroys == 1 && snapshot.cloudMeshHandle == fancyHash, "Pattern update must replace, then reuse hash");
    finish();

    renderer.worldOriginRebaseEnabled_ = true;
    renderer.camera_.position[0] = 1200;
    renderer.present();
    snapshot = prepare();
    require(creates == 2 && destroys == 1, "Origin rebase rebuilt local cloud geometry");
    require(std::abs(snapshot.cloudTransformX + snapshot.renderOrigin.x - 23.5f) < 0.001f, "Origin rebase changed cloud world position");

    renderer.clearCloudLayer();
    require(destroys == 1, "Clearing live input destroyed the submitted mesh");
    finish();
    renderer.present();
    snapshot = prepare();
    require(snapshot.cloudMeshHandle == nullptr && destroys == 2, "Published cloud clear ignored");
    finish();

    renderer.updateCloudLayer(false, 0, 0, 0, 128, 0, 0, 1, 1, 1);
    renderer.present();
    snapshot = prepare();
    require(snapshot.cloudMeshHandle != nullptr && snapshot.cloudMeshHandle != fancyHash, "Fast/fancy hashes collide");
    finish();
    renderer.updateCloudLayer(true, 0, 0, 0, 128, 0, 0, 1, 1, 1);
    renderer.present();
    snapshot = prepare();
    require(snapshot.cloudMeshHandle == fancyHash, "Mode switch changed stable fancy hash");
    renderer.clearWorldScene();
    require(liveMesh == fancyHash, "World clear destroyed an in-flight mesh");
    finish();
    require(liveMesh == nullptr, "World clear did not retire the submitted mesh");
    require(!renderer.cloudLayer_.enabled && !renderer.publishedCloudLayer_.enabled, "World clear retained cloud input");

    std::fill(renderer.cloudMask_.alpha.begin(), renderer.cloudMask_.alpha.end(), 0);
    renderer.updateCloudLayer(true, 0, 0, 0, 128, 0, 0, 1, 1, 1);
    renderer.present();
    snapshot = prepare();
    require(snapshot.cloudMeshHandle == nullptr && renderer.cloudMeshPrepared_, "Empty pattern not cached");
    finish();
    renderer.initialized_ = false;
    std::cout << "Cloud publication, reuse, stable hash and deferred lifetime tests passed\n";
  }
};

}  // namespace mcrtx

int main() {
  mcrtx::CloudMeshTest::run();
}
