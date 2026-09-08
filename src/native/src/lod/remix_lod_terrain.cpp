// Distant terrain LOD: region intake, meshing, and Remix submission.

#include "mcrtx/core/remix_renderer.hpp"
#include "mcrtx/core/remix_geometry_common.hpp"
#include "mcrtx/core/remix_render_common.hpp"
#include "mcrtx/lifecycle/perf_log.hpp"
#include "mcrtx/lod/lod_geometry.hpp"
#include "mcrtx/lod/lod_types.hpp"

#include <cstdint>
#include <string>
#include <vector>

namespace mcrtx {

using namespace mcrtx::detail;
using namespace mcrtx::lod;

namespace {

// Distinct from the chunk mesh hash space so a region and a chunk section that
// happen to share an origin cannot collide inside Remix's mesh cache.
constexpr std::uint64_t kLodMeshHashSeed = 0x4D435254584C4F44ull;  // "MCRTXLOD"

std::uint64_t makeLodMeshHash(const RegionKey& key, std::uint64_t fingerprint) {
  std::uint64_t hash = kLodMeshHashSeed;
  const auto mix = [&hash](std::uint64_t value) {
    hash ^= value + 0x9E3779B97F4A7C15ull + (hash << 6) + (hash >> 2);
  };
  mix(static_cast<std::uint32_t>(key.originX));
  mix(static_cast<std::uint32_t>(key.originZ));
  mix(static_cast<std::uint32_t>(key.step));
  mix(static_cast<std::uint32_t>(key.renderPass));
  mix(fingerprint);
  return hash;
}

}  // namespace

// Distant terrain never destroys a region mesh where it stands. Both reasons
// would be enough on their own.
//
// Region meshes are built on the store's worker thread now, and DestroyMesh
// reaches Remix's command stream through EmitCs without taking the device lock
// -- which is safe from the thread that owns the device and a data race from
// any other. And a region mesh is registered through CreateMeshBatched, which
// only takes effect when Remix next reaches a DrawInstance: destroying a handle
// before that point makes Remix destroy first and register second, leaving a
// mesh alive that nothing will ever draw or free.
//
// The deferred queue answers both. It is drained at present -- on the thread
// that owns the device, and after the frame's DrawInstance calls have flushed
// every batched create.
void RemixRenderer::deferLodMeshDestroy(remixapi_MeshHandle& meshHandle) {
  if (meshHandle == nullptr) {
    return;
  }
  deferredMeshDestroys_.push_back(meshHandle);
  meshHandle = nullptr;
}

bool RemixRenderer::submitLodRegion(
    const RegionKey& key,
    const std::vector<LodColumn>& columns) {
  MCRTX_PERF_SCOPE(::mcrtx::perf::Side::Native, "RemixRenderer::submitLodRegion");
  MCRTX_TRACY_SCOPE("RemixRenderer::submitLodRegion");

  if (columns.size() != static_cast<std::size_t>(kPaddedColumnsPerRegion)) {
    if (!loggedLodColumnCountMismatch_) {
      loggedLodColumnCountMismatch_ = true;
      log("LOD region refused: got " + std::to_string(columns.size())
          + " columns, expected " + std::to_string(kPaddedColumnsPerRegion));
    }
    return false;
  }

  // Which material a region gets and how its UVs are laid out both follow from
  // its detail level, because the atlas is pre-tiled for one cell width. Read
  // under the lock, and with the token that says which set of materials it came
  // from: this no longer runs on the thread that rebuilds materials, so the
  // handle could otherwise be destroyed between being read and being used.
  remixapi_MaterialHandle materialHandle = nullptr;
  bool useLodAtlas = false;
  std::uint32_t materialToken = 0;
  {
    std::scoped_lock lock(mutex_);
    materialHandle = lodMaterialForPass(key.renderPass, key.step);
    useLodAtlas = lodAtlasActiveForStep(key.step);
    materialToken = lodRebuildToken_.load(std::memory_order_relaxed);
    if (materialHandle == nullptr) {
      if (!loggedLodMaterialMissing_) {
        loggedLodMaterialMissing_ = true;
        log("LOD region dropped: no material for render pass "
            + std::to_string(key.renderPass)
            + " at step " + std::to_string(key.step)
            + " (lod atlas active: " + (useLodAtlas ? "yes" : "no") + ")");
      }
      return false;
    }
  }

  // Region builds run one at a time on the store's worker thread, so the
  // scratch buffers are kept alive across them. A step 2 region meshes to the
  // better part of a megabyte of vertices; allocating and freeing that for every
  // build -- and for every rebuild the data sweep schedules -- is pure churn.
  thread_local geometry::SurfaceBuildBuffers surfaceBuild;
  surfaceBuild.vertices.clear();
  surfaceBuild.indices.clear();
  surfaceBuild.materialHandle = materialHandle;

  buildRegionSurface(key, columns, surfaceBuild, useLodAtlas);

  // Hashed before the lock is taken. It reads nothing but the buffers this
  // thread owns, and it walks every vertex, so leaving it inside would hold the
  // renderer's mutex for the length of a mesh the render thread has no interest
  // in yet.
  const std::uint64_t fingerprint = computeRegionFingerprint(surfaceBuild);

  // Two things have to be settled before the mesh is handed to Remix, and both
  // need the lock: that the materials it was built against still exist, and
  // that it differs from the mesh already standing there. The handover itself
  // is done with the lock released -- see below -- and the lock taken again to
  // install the result.
  {
    std::scoped_lock lock(mutex_);

    // Materials were rebuilt while this region was being meshed, so the handle
    // it was built against no longer exists. Dropping the build is the whole of
    // the fix: the rebuild bumped the token, and the scheduler requeues every
    // region when it sees that.
    if (lodRebuildToken_.load(std::memory_order_relaxed) != materialToken) {
      if (!loggedLodMaterialsChangedUnderBuild_) {
        loggedLodMaterialsChangedUnderBuild_ = true;
        log("LOD region refused: materials were rebuilt while it meshed (token "
            + std::to_string(materialToken) + " -> "
            + std::to_string(lodRebuildToken_.load(std::memory_order_relaxed))
            + "); the scheduler requeues on the new token, so this is only a"
            " problem if it repeats");
      }
      return false;
    }

    if (!loggedLodFirstBuild_) {
      loggedLodFirstBuild_ = true;
      log("LOD first region meshed: step=" + std::to_string(key.step)
          + " vertices=" + std::to_string(surfaceBuild.vertices.size())
          + " indices=" + std::to_string(surfaceBuild.indices.size()));
    }

    if (surfaceBuild.indices.empty()) {
      const auto existing = lodRegions_.find(key);
      if (existing != lodRegions_.end()) {
        deferLodMeshDestroy(existing->second.meshHandle);
        lodRegions_.erase(existing);
      }
      return true;
    }

    const auto existing = lodRegions_.find(key);
    if (existing != lodRegions_.end()
        && existing->second.meshHandle != nullptr
        && existing->second.fingerprint == fingerprint) {
      // Terrain that did not actually change keeps its mesh, so Remix is not
      // asked to rebuild an identical BLAS.
      return true;
    }
  }

  remixapi_MeshInfoSurfaceTriangles surface {};
  surface.vertices_values = surfaceBuild.vertices.data();
  surface.vertices_count = surfaceBuild.vertices.size();
  surface.indices_values = surfaceBuild.indices.data();
  surface.indices_count = surfaceBuild.indices.size();
  surface.skinning_hasvalue = FALSE;
  surface.material = surfaceBuild.materialHandle;

  remixapi_MeshInfo meshInfo {};
  meshInfo.sType = REMIXAPI_STRUCT_TYPE_MESH_INFO;
  meshInfo.hash = makeLodMeshHash(key, fingerprint);
  meshInfo.surfaces_values = &surface;
  meshInfo.surfaces_count = 1;

  // Batched creation, where the loaded runtime offers it.
  //
  // CreateMesh allocates the DXVK vertex and index buffers, copies into them and
  // emits a command-stream packet, all inline on the calling thread. A step 2
  // region is the better part of a megabyte, so revealing a stretch of new
  // ground pays for all of that inside one frame -- which is exactly the spike
  // this removes. CreateMeshBatched instead deep-copies the info and leaves the
  // buffer allocation and asset-replacer registration to the render thread's
  // next flush point, which Remix reaches on its next DrawInstance. The field
  // draws every frame, so a region is never more than one frame late.
  //
  // The pointer is tested rather than assumed. Remix is loaded at runtime and an
  // older build simply will not have written this field, which stays null
  // because the interface struct is value-initialised. Falling back to the
  // unbatched call keeps the mod working against whatever runtime is installed.
  //
  // Called with the renderer's mutex released. CreateMeshBatched deep-copies the
  // whole surface -- the better part of a megabyte at step 2 -- and the render
  // thread takes that same mutex every frame to snapshot what it will draw, so
  // a copy made under it was a stall on the frame for every region the worker
  // finished. The copy reads nothing but this thread's own buffers, and Remix
  // guards its pending list with a lock of its own. The mutex is taken again
  // below to install the handle, and the token is checked a second time there
  // because the materials could have been rebuilt in the gap.
  remixapi_MeshHandle newMeshHandle = nullptr;
  const bool batched = remix_.CreateMeshBatched != nullptr;
  const remixapi_ErrorCode result = [&]() -> remixapi_ErrorCode {
    if (batched) {
      MCRTX_PERF_SCOPE(::mcrtx::perf::Side::Remix, "CreateMeshBatched.lod");
      return remix_.CreateMeshBatched(&meshInfo, &newMeshHandle);
    }
    MCRTX_PERF_SCOPE(::mcrtx::perf::Side::Remix, "CreateMesh.lod");
    return remix_.CreateMesh(&meshInfo, &newMeshHandle);
  }();
  if (result != REMIXAPI_ERROR_CODE_SUCCESS) {
    setError(std::string(batched ? "CreateMeshBatched" : "CreateMesh")
        + "(lod) failed: " + errorCodeToString(result));
    return false;
  }

  std::scoped_lock lock(mutex_);
  if (lodRebuildToken_.load(std::memory_order_relaxed) != materialToken) {
    // The same refusal as above, one step later: the handle just made points
    // at a material that no longer exists, so it goes on the destroy queue
    // rather than into the field. The scheduler requeues the region.
    deferLodMeshDestroy(newMeshHandle);
    return false;
  }

  RegionMeshData& meshData = lodRegions_[key];
  deferLodMeshDestroy(meshData.meshHandle);
  meshData.meshHandle = newMeshHandle;
  meshData.meshHash = meshInfo.hash;
  meshData.fingerprint = fingerprint;
  meshData.triangleCount = static_cast<std::uint32_t>(surfaceBuild.indices.size() / 3);
  if (!loggedLodFirstMesh_) {
    loggedLodFirstMesh_ = true;
    log(std::string("LOD first mesh submitted to Remix (")
        + (batched ? "batched" : "immediate")
        + "); resident regions: " + std::to_string(lodRegions_.size()));
  }
  return true;
}

void RemixRenderer::unloadLodRegion(int originX, int originZ, int step) {
  MCRTX_PERF_SCOPE(::mcrtx::perf::Side::Native, "RemixRenderer::unloadLodRegion");
  std::scoped_lock lock(mutex_);

  for (int renderPass = 0; renderPass <= 1; ++renderPass) {
    const RegionKey key {originX, originZ, step, renderPass};
    const auto it = lodRegions_.find(key);
    if (it != lodRegions_.end()) {
      deferLodMeshDestroy(it->second.meshHandle);
      lodRegions_.erase(it);
    }
  }
}

void RemixRenderer::setLodRegionHidden(int originX, int originZ, int step, bool hidden) {
  std::scoped_lock lock(mutex_);

  for (int renderPass = 0; renderPass <= 1; ++renderPass) {
    const RegionKey key {originX, originZ, step, renderPass};
    const auto it = lodRegions_.find(key);
    if (it != lodRegions_.end()) {
      it->second.hidden = hidden;
    }
  }
}

void RemixRenderer::clearLodRegions() {
  std::scoped_lock lock(mutex_);
  clearLodRegionsLocked();
}

// Callers that already hold the mutex use this directly. mutex_ is not
// recursive, so locking it again from a *Locked path throws.
void RemixRenderer::clearLodRegionsLocked() {
  for (auto& entry : lodRegions_) {
    deferLodMeshDestroy(entry.second.meshHandle);
  }
  lodRegions_.clear();
}

std::size_t RemixRenderer::lodRegionCount() const {
  std::scoped_lock lock(mutex_);
  return lodRegions_.size();
}

// Triangles the field is currently costing. Region counts do not answer that:
// an ocean region and a mountain region are one region each and an order of
// magnitude apart, and a hidden region costs memory but no frame time. This is
// the number to watch when changing how the field is meshed.
std::size_t RemixRenderer::lodTriangleCount(bool visibleOnly) const {
  std::scoped_lock lock(mutex_);
  std::size_t total = 0;
  for (const auto& entry : lodRegions_) {
    if (visibleOnly && (entry.second.hidden || entry.second.meshHandle == nullptr)) {
      continue;
    }
    total += entry.second.triangleCount;
  }
  return total;
}

std::uint32_t RemixRenderer::lodRebuildToken() const {
  return lodRebuildToken_.load(std::memory_order_relaxed);
}

}  // namespace mcrtx
