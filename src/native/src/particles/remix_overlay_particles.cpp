// Particle capture, material grouping, geometry, and mesh lifecycle.

#include "mcrtx/core/remix_renderer.hpp"
#include "mcrtx/core/remix_geometry_common.hpp"
#include "mcrtx/core/remix_render_common.hpp"
#include "mcrtx/lifecycle/perf_log.hpp"

#include <cstddef>
#include <cstdint>
#include <unordered_map>
#include <vector>

namespace mcrtx {

using namespace mcrtx::detail;
using namespace mcrtx::geometry;

namespace {

constexpr std::uint64_t kParticleMeshHashSeed = 0x4D43525458505100ull;

std::uint64_t makeParticleMeshHash(std::uint64_t sequence) {
  return kParticleMeshHashSeed | (sequence & 0x0000FFFFFFFFFFFFull);
}

}  // namespace

void RemixRenderer::beginParticleFrame() {
  MCRTX_PERF_SCOPE(::mcrtx::perf::Side::Native, "RemixRenderer::beginParticleFrame");
  std::scoped_lock lock(mutex_);

  if (!initialized_) {
    return;
  }

  particleQuads_.clear();
}

void RemixRenderer::captureParticleQuad(
    float x0,
    float y0,
    float z0,
    float u0,
    float v0,
    float x1,
    float y1,
    float z1,
    float u1,
    float v1,
    float x2,
    float y2,
    float z2,
    float u2,
    float v2,
    float x3,
    float y3,
    float z3,
    float u3,
    float v3,
    std::uint32_t colorRgba,
    std::uint32_t textureKind) {
  MCRTX_PERF_SCOPE(::mcrtx::perf::Side::Native, "RemixRenderer::captureParticleQuad");
  std::scoped_lock lock(mutex_);

  if (!initialized_) {
    return;
  }

  ParticleQuad quad;
  quad.positions = {
      x0, y0, z0,
      x1, y1, z1,
      x2, y2, z2,
      x3, y3, z3,
  };
  quad.texcoords = {
      u0, v0,
      u1, v1,
      u2, v2,
      u3, v3,
  };
  quad.color = colorRgba;
  quad.textureKind = textureKind;
  particleQuads_.push_back(std::move(quad));
}

void RemixRenderer::destroyParticleMesh() {
  MCRTX_PERF_SCOPE(::mcrtx::perf::Side::Native, "RemixRenderer::destroyParticleMesh");
  destroyMeshHandle(particleMeshHandle_);
  particleQuadCount_ = 0;
}

bool RemixRenderer::rebuildParticleMesh(const WorldRenderOrigin& renderOrigin) {
  MCRTX_PERF_SCOPE(::mcrtx::perf::Side::Native, "RemixRenderer::rebuildParticleMesh");
  MCRTX_TRACY_SCOPE("RemixRenderer::rebuildParticleMesh");
  if (particleQuads_.empty()) {
    destroyParticleMesh();
    return true;
  }
  MCRTX_TRACY_VALUE(particleQuads_.size());

  std::vector<SurfaceBuildBuffers> surfacesToBuild;
  surfacesToBuild.reserve(4);
  std::unordered_map<std::uintptr_t, std::size_t> surfaceIndexByHandle;

  const auto acquireSurface = [&surfacesToBuild, &surfaceIndexByHandle](remixapi_MaterialHandle materialHandle) -> SurfaceBuildBuffers& {
    const std::uintptr_t materialKey = reinterpret_cast<std::uintptr_t>(materialHandle);
    const auto it = surfaceIndexByHandle.find(materialKey);
    if (it != surfaceIndexByHandle.end()) {
      return surfacesToBuild[it->second];
    }

    SurfaceBuildBuffers surface;
    surface.materialHandle = materialHandle;
    surface.vertices.reserve(256);
    surface.indices.reserve(384);
    const std::size_t surfaceIndex = surfacesToBuild.size();
    surfacesToBuild.push_back(std::move(surface));
    surfaceIndexByHandle.emplace(materialKey, surfaceIndex);
    return surfacesToBuild.back();
  };

  std::size_t quadCount = 0;
  {
    MCRTX_TRACY_SCOPE("rebuildParticleMesh.buildSurfaces");
    for (const ParticleQuad& quad : particleQuads_) {
      remixapi_MaterialHandle materialHandle = acquireParticleMaterial(quad.textureKind);
      if (materialHandle == nullptr) {
        continue;
      }

      const auto normal = computeQuadNormal(
          quad.positions[0], quad.positions[1], quad.positions[2],
          quad.positions[3], quad.positions[4], quad.positions[5],
          quad.positions[6], quad.positions[7], quad.positions[8]);
      SurfaceBuildBuffers& surface = acquireSurface(materialHandle);
      const WorldRenderPosition p0 = rebaseWorldPosition(
          quad.positions[0], quad.positions[1], quad.positions[2], renderOrigin);
      const WorldRenderPosition p1 = rebaseWorldPosition(
          quad.positions[3], quad.positions[4], quad.positions[5], renderOrigin);
      const WorldRenderPosition p2 = rebaseWorldPosition(
          quad.positions[6], quad.positions[7], quad.positions[8], renderOrigin);
      const WorldRenderPosition p3 = rebaseWorldPosition(
          quad.positions[9], quad.positions[10], quad.positions[11], renderOrigin);
      appendCloudQuad(
          p0.x, p0.y, p0.z, quad.texcoords[0], quad.texcoords[1],
          p1.x, p1.y, p1.z, quad.texcoords[2], quad.texcoords[3],
          p2.x, p2.y, p2.z, quad.texcoords[4], quad.texcoords[5],
          p3.x, p3.y, p3.z, quad.texcoords[6], quad.texcoords[7],
          normal[0], normal[1], normal[2],
          quad.color,
          surface.vertices,
          surface.indices);
      ++quadCount;
    }
  }
  MCRTX_TRACY_VALUE(quadCount);

  std::vector<remixapi_MeshInfoSurfaceTriangles> surfaces;
  {
    MCRTX_TRACY_SCOPE("rebuildParticleMesh.finalizeSurfaces");
    surfaces.reserve(surfacesToBuild.size());
    for (const SurfaceBuildBuffers& surfaceBuild : surfacesToBuild) {
      if (surfaceBuild.indices.empty()) {
        continue;
      }

      remixapi_MeshInfoSurfaceTriangles surface {};
      surface.vertices_values = surfaceBuild.vertices.data();
      surface.vertices_count = surfaceBuild.vertices.size();
      surface.indices_values = surfaceBuild.indices.data();
      surface.indices_count = surfaceBuild.indices.size();
      surface.skinning_hasvalue = FALSE;
      surface.material = surfaceBuild.materialHandle;
      surfaces.push_back(surface);
    }
  }
  MCRTX_TRACY_VALUE(surfaces.size());

  if (surfaces.empty()) {
    destroyParticleMesh();
    return true;
  }

  remixapi_MeshInfo meshInfo {};
  meshInfo.sType = REMIXAPI_STRUCT_TYPE_MESH_INFO;
  meshInfo.hash = makeParticleMeshHash(nextParticleMeshHash_++);
  meshInfo.surfaces_values = surfaces.data();
  meshInfo.surfaces_count = static_cast<std::uint32_t>(surfaces.size());

  remixapi_MeshHandle newMeshHandle = nullptr;
  const remixapi_ErrorCode result = [&]() {
    MCRTX_PERF_SCOPE(::mcrtx::perf::Side::Remix, "CreateMesh.particle");
    return remix_.CreateMesh(&meshInfo, &newMeshHandle);
  }();
  if (result != REMIXAPI_ERROR_CODE_SUCCESS) {
    setError("CreateMesh failed: " + errorCodeToString(result));
    return false;
  }

  destroyParticleMesh();
  particleMeshHandle_ = newMeshHandle;
  particleQuadCount_ = quadCount;
  return true;
}

}  // namespace mcrtx
