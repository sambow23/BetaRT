// Auto-extracted from remix_renderer.cpp during the monolith split.
// Keep edits here; do not re-merge into remix_renderer.cpp.

#include "mcrtx/core/remix_renderer.hpp"
#include "mcrtx/core/remix_geometry_common.hpp"
#include "mcrtx/core/remix_render_common.hpp"
#include "mcrtx/lifecycle/perf_log.hpp"

#include <algorithm>
#include <atomic>
#include <bit>
#include <cctype>
#include <cmath>
#include <cstdlib>
#include <cstddef>
#include <cstdint>
#include <iomanip>
#include <iostream>
#include <sstream>
#include <string_view>
#include <vector>

namespace mcrtx {

using namespace mcrtx::detail;
using namespace mcrtx::geometry;

namespace {
constexpr std::uint64_t kDynamicEntityMeshHashSeed = 0x4D43525458454E00ull;

std::uint64_t makeDynamicEntityMeshKey(int entityId, std::uint64_t geometryFingerprint) {
  if (entityId == kFirstPersonDynamicEntityId) {
    return geometryFingerprint ^ 0x564945574D4F4445ull;
  }
  return geometryFingerprint;
}

std::uint64_t makeDynamicEntityMeshHash(std::uint64_t geometryFingerprint) {
  return kDynamicEntityMeshHashSeed ^ geometryFingerprint;
}


std::uint64_t beginDynamicEntityFingerprint(std::uint32_t hurtStage, std::uint32_t creeperFuseStage) {
  std::uint64_t fingerprint = 1469598103934665603ull;

  fingerprint ^= static_cast<std::uint64_t>(std::min(hurtStage, kDynamicEntityMaxHurtStage));
  fingerprint *= 1099511628211ull;
  fingerprint ^= static_cast<std::uint64_t>(std::min(creeperFuseStage, kDynamicEntityMaxCreeperFuseStage));
  fingerprint *= 1099511628211ull;
  return fingerprint;
}

std::uint64_t computeDynamicEntityTextureFingerprint(std::string_view value) {
  std::uint64_t fingerprint = 1469598103934665603ull;
  for (const unsigned char character : value) {
    fingerprint ^= static_cast<std::uint64_t>(character);
    fingerprint *= 1099511628211ull;
  }
  fingerprint ^= 0xFFull;
  fingerprint *= 1099511628211ull;
  return fingerprint;
}

void hashDynamicEntityString(std::uint64_t& fingerprint, const std::string& value) {
  for (const unsigned char character : value) {
    fingerprint ^= static_cast<std::uint64_t>(character);
    fingerprint *= 1099511628211ull;
  }
  fingerprint ^= 0xFFull;
  fingerprint *= 1099511628211ull;
}

void hashDynamicEntityQuad(std::uint64_t& fingerprint, const DynamicEntityQuad& quad) {
  fingerprint ^= static_cast<std::uint64_t>(quad.boneIndex);
  fingerprint *= 1099511628211ull;
  for (const float position : quad.positions) {
    fingerprint ^= static_cast<std::uint64_t>(std::bit_cast<std::uint32_t>(position));
    fingerprint *= 1099511628211ull;
  }
  for (const float texcoord : quad.texcoords) {
    fingerprint ^= static_cast<std::uint64_t>(std::bit_cast<std::uint32_t>(texcoord));
    fingerprint *= 1099511628211ull;
  }
  fingerprint ^= static_cast<std::uint64_t>(quad.color);
  fingerprint *= 1099511628211ull;
  fingerprint ^= quad.blendEnabled ? 1ull : 0ull;
  fingerprint *= 1099511628211ull;
  fingerprint = mixHashComponent(fingerprint, static_cast<std::uint32_t>(quad.textureIndex));
  fingerprint = mixHashComponent(fingerprint, static_cast<std::uint32_t>(quad.textureFingerprint));
  fingerprint = mixHashComponent(fingerprint, static_cast<std::uint32_t>(quad.textureFingerprint >> 32));
}

std::uint64_t finalizeDynamicEntityFingerprint(std::uint64_t quadFingerprint, std::uint32_t boneCount) {
  std::uint64_t fingerprint = quadFingerprint;
  fingerprint ^= static_cast<std::uint64_t>(boneCount);
  fingerprint *= 1099511628211ull;
  return fingerprint;
}
void clearActiveDynamicEntityState(DynamicEntityBuildState& state) {
  state.entityId = -1;
  state.hurtStage = 0;
  state.creeperFuseStage = 0;
  state.maxBoneCount = 0;
  state.quadFingerprint = 0;
  state.currentTextureIndex = 0xFFFFFFFFu;
  state.currentTextureFingerprint = 0;
  state.texturePaths.clear();
  state.quadCount = 0;
  state.boneTransforms.clear();
  state.active = false;
}

// Appends one quad to the active dynamic entity build. Vertices are packed as
// four interleaved [x, y, z, u, v] tuples (20 floats), matching the capture
// hook's argument order. Caller must hold the renderer mutex and have already
// validated active-entity preconditions. Returns false when the quad buffer is
// full so batch callers can stop early.
bool appendDynamicEntityQuadLocked(DynamicEntityBuildState& state,
                                   const float* vertices,
                                   std::uint32_t colorRgba,
                                   bool blendEnabled,
                                   std::uint32_t boneIndex) {
  if (state.quadCount >= state.quads.size()) {
    return false;
  }

  DynamicEntityQuad& quad = state.quads[state.quadCount++];
  quad.positions = {
      vertices[0], vertices[1], vertices[2],
      vertices[5], vertices[6], vertices[7],
      vertices[10], vertices[11], vertices[12],
      vertices[15], vertices[16], vertices[17],
  };
  quad.texcoords = {
      vertices[3], vertices[4],
      vertices[8], vertices[9],
      vertices[13], vertices[14],
      vertices[18], vertices[19],
  };
  quad.color = colorRgba;
  quad.textureIndex = state.currentTextureIndex;
  quad.textureFingerprint = state.currentTextureFingerprint;
  quad.blendEnabled = blendEnabled;
  quad.boneIndex = boneIndex;
  state.maxBoneCount = std::max(state.maxBoneCount, boneIndex + 1);
  hashDynamicEntityQuad(state.quadFingerprint, quad);
  return true;
}
std::uint8_t dynamicEntityQuadAlpha(std::uint32_t colorRgba) {
  return static_cast<std::uint8_t>((colorRgba >> 24) & 0xFFu);
}

DynamicEntityMaterialClass dynamicEntityMaterialClassForQuad(const DynamicEntityQuad& quad) {
  return quad.blendEnabled || dynamicEntityQuadAlpha(quad.color) < 0xFFu
      ? DynamicEntityMaterialClass::Translucent
      : DynamicEntityMaterialClass::Cutout;
}
}  // namespace
void RemixRenderer::beginDynamicEntityFrame() {
  MCRTX_PERF_SCOPE(::mcrtx::perf::Side::Native, "RemixRenderer::beginDynamicEntityFrame");
  MCRTX_TRACY_SCOPE("RemixRenderer::beginDynamicEntityFrame");
  std::scoped_lock lock(mutex_);

  if (!initialized_) {
    return;
  }

  MCRTX_TRACY_VALUE(dynamicEntityFrameInstanceCount_);
  clearDynamicEntityFrameInstances();
  clearActiveDynamicEntityState(activeDynamicEntity_);
  heldItemId_ = -1;
  entityHeldTorchLightsSeenThisFrame_.clear();
}

void RemixRenderer::beginDynamicEntity(int entityId, std::uint32_t hurtStage, std::uint32_t creeperFuseStage) {
  MCRTX_PERF_SCOPE(::mcrtx::perf::Side::Native, "RemixRenderer::beginDynamicEntity");
  MCRTX_TRACY_SCOPE("RemixRenderer::beginDynamicEntity");
  std::scoped_lock lock(mutex_);

  if (!initialized_ || !dynamicEntityRenderingEnabled_) {
    clearActiveDynamicEntityState(activeDynamicEntity_);
    return;
  }

  clearActiveDynamicEntityState(activeDynamicEntity_);
  activeDynamicEntity_.entityId = entityId;
  activeDynamicEntity_.hurtStage = std::min(hurtStage, kDynamicEntityMaxHurtStage);
  activeDynamicEntity_.creeperFuseStage = std::min(creeperFuseStage, kDynamicEntityMaxCreeperFuseStage);
  activeDynamicEntity_.quadFingerprint = beginDynamicEntityFingerprint(
      activeDynamicEntity_.hurtStage,
      activeDynamicEntity_.creeperFuseStage);
  activeDynamicEntity_.active = entityId >= 0;
  activeDynamicEntity_.texturePaths.reserve(4);
  activeDynamicEntity_.boneTransforms.reserve(32);
}

void RemixRenderer::setDynamicEntityTexture(const std::string& texturePath) {
  MCRTX_PERF_SCOPE(::mcrtx::perf::Side::Native, "RemixRenderer::setDynamicEntityTexture");
  std::scoped_lock lock(mutex_);

  if (!initialized_ || !dynamicEntityRenderingEnabled_ || !activeDynamicEntity_.active) {
    return;
  }

  for (std::size_t index = 0; index < activeDynamicEntity_.texturePaths.size(); ++index) {
    if (activeDynamicEntity_.texturePaths[index] == texturePath) {
      activeDynamicEntity_.currentTextureIndex = static_cast<std::uint32_t>(index);
      activeDynamicEntity_.currentTextureFingerprint = computeDynamicEntityTextureFingerprint(texturePath);
      return;
    }
  }

  activeDynamicEntity_.currentTextureIndex = static_cast<std::uint32_t>(activeDynamicEntity_.texturePaths.size());
  activeDynamicEntity_.currentTextureFingerprint = computeDynamicEntityTextureFingerprint(texturePath);
  activeDynamicEntity_.texturePaths.push_back(texturePath);
}

void RemixRenderer::setDynamicEntityBoneTransform(
    std::uint32_t boneIndex,
    const remixapi_Transform& transform,
    double worldX,
    double worldY,
    double worldZ) {
  MCRTX_PERF_SCOPE(::mcrtx::perf::Side::Native, "RemixRenderer::setDynamicEntityBoneTransform");
  std::scoped_lock lock(mutex_);

  if (!initialized_ || !dynamicEntityRenderingEnabled_ || !activeDynamicEntity_.active || boneIndex >= REMIXAPI_INSTANCE_INFO_MAX_BONES_COUNT) {
    return;
  }

  if (boneIndex >= activeDynamicEntity_.boneTransforms.size()) {
    activeDynamicEntity_.boneTransforms.resize(static_cast<std::size_t>(boneIndex) + 1);
  }
  DynamicEntityBoneTransform& boneTransform = activeDynamicEntity_.boneTransforms[boneIndex];
  boneTransform.transform = transform;
  boneTransform.worldX = worldX;
  boneTransform.worldY = worldY;
  boneTransform.worldZ = worldZ;
}

void RemixRenderer::captureDynamicEntityQuad(
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
    bool blendEnabled,
    std::uint32_t boneIndex) {
  MCRTX_PERF_SCOPE(::mcrtx::perf::Side::Native, "RemixRenderer::captureDynamicEntityQuad");
  std::scoped_lock lock(mutex_);

  if (!initialized_ || !dynamicEntityRenderingEnabled_ || !activeDynamicEntity_.active || activeDynamicEntity_.currentTextureIndex == 0xFFFFFFFFu) {
    return;
  }

  if (boneIndex >= activeDynamicEntity_.boneTransforms.size() || boneIndex >= REMIXAPI_INSTANCE_INFO_MAX_BONES_COUNT) {
    return;
  }

  const float vertices[20] = {
      x0, y0, z0, u0, v0,
      x1, y1, z1, u1, v1,
      x2, y2, z2, u2, v2,
      x3, y3, z3, u3, v3,
  };
  appendDynamicEntityQuadLocked(activeDynamicEntity_, vertices, colorRgba, blendEnabled, boneIndex);
}

void RemixRenderer::captureDynamicEntityQuadBatch(
    const float* vertices,
    std::uint32_t quadCount,
    std::uint32_t colorRgba,
    bool blendEnabled,
    std::uint32_t boneIndex) {
  MCRTX_PERF_SCOPE(::mcrtx::perf::Side::Native, "RemixRenderer::captureDynamicEntityQuadBatch");
  std::scoped_lock lock(mutex_);

  if (!initialized_ || !dynamicEntityRenderingEnabled_ || !activeDynamicEntity_.active || activeDynamicEntity_.currentTextureIndex == 0xFFFFFFFFu) {
    return;
  }

  if (vertices == nullptr || quadCount == 0) {
    return;
  }

  if (boneIndex >= activeDynamicEntity_.boneTransforms.size() || boneIndex >= REMIXAPI_INSTANCE_INFO_MAX_BONES_COUNT) {
    return;
  }

  for (std::uint32_t quadIndex = 0; quadIndex < quadCount; ++quadIndex) {
    if (!appendDynamicEntityQuadLocked(activeDynamicEntity_, vertices + static_cast<std::size_t>(quadIndex) * 20u, colorRgba, blendEnabled, boneIndex)) {
      break;
    }
  }
}

void RemixRenderer::endDynamicEntity() {
  MCRTX_PERF_SCOPE(::mcrtx::perf::Side::Native, "RemixRenderer::endDynamicEntity");
  MCRTX_TRACY_SCOPE("RemixRenderer::endDynamicEntity");
  std::scoped_lock lock(mutex_);

  if (!dynamicEntityRenderingEnabled_) {
    clearActiveDynamicEntityState(activeDynamicEntity_);
    return;
  }

  if (!activeDynamicEntity_.active) {
    return;
  }

  MCRTX_TRACY_VALUE(activeDynamicEntity_.quadCount);
  if (activeDynamicEntity_.quadCount != 0) {
    const std::uint32_t boneCount = activeDynamicEntity_.maxBoneCount;
    MCRTX_TRACY_VALUE(boneCount);
    if (boneCount != 0 && boneCount <= activeDynamicEntity_.boneTransforms.size()) {
      {
        MCRTX_TRACY_SCOPE("endDynamicEntity.findOrCreateMesh");
      if (DynamicEntityMeshData* meshData = findOrCreateDynamicEntityMesh(activeDynamicEntity_); meshData != nullptr) {
        DynamicEntityFrameInstance* frameInstance = nullptr;
        if (dynamicEntityFrameInstanceCount_ < dynamicEntityFrameInstances_.size()) {
          frameInstance = &dynamicEntityFrameInstances_[dynamicEntityFrameInstanceCount_];
        } else {
          dynamicEntityFrameInstances_.emplace_back();
          frameInstance = &dynamicEntityFrameInstances_.back();
        }
        frameInstance->entityId = activeDynamicEntity_.entityId;
        frameInstance->meshHandle = meshData->meshHandle;
        frameInstance->quadCount = meshData->quadCount;
        activeDynamicEntity_.boneTransforms.resize(boneCount);
        frameInstance->boneTransforms = std::move(activeDynamicEntity_.boneTransforms);
        ++dynamicEntityFrameInstanceCount_;
      }
      }
    }
  }

  clearActiveDynamicEntityState(activeDynamicEntity_);
}

DynamicEntityMeshData* RemixRenderer::findOrCreateDynamicEntityMesh(const DynamicEntityBuildState& buildState) {
  MCRTX_PERF_SCOPE(::mcrtx::perf::Side::Native, "RemixRenderer::findOrCreateDynamicEntityMesh");
  MCRTX_TRACY_SCOPE("RemixRenderer::findOrCreateDynamicEntityMesh");
  if (buildState.quadCount == 0) {
    return nullptr;
  }

  MCRTX_TRACY_VALUE(buildState.quadCount);

  const std::uint32_t boneCount = buildState.maxBoneCount;
  if (boneCount == 0
      || boneCount > buildState.boneTransforms.size()
      || boneCount > REMIXAPI_INSTANCE_INFO_MAX_BONES_COUNT) {
    return nullptr;
  }

  const std::uint64_t geometryFingerprint = finalizeDynamicEntityFingerprint(buildState.quadFingerprint, boneCount);
  const std::uint64_t meshKey = makeDynamicEntityMeshKey(buildState.entityId, geometryFingerprint);
  if (const auto existing = dynamicEntityMeshes_.find(meshKey); existing != dynamicEntityMeshes_.end()) {
    return &existing->second;
  }

  std::vector<SurfaceBuildBuffers> surfacesToBuild;
  surfacesToBuild.reserve(8);
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
    MCRTX_TRACY_SCOPE("findOrCreateDynamicEntityMesh.buildSurfaces");
    for (std::size_t quadIndex = 0; quadIndex < buildState.quadCount; ++quadIndex) {
      const DynamicEntityQuad& quad = buildState.quads[quadIndex];
      if (quad.textureIndex >= buildState.texturePaths.size()) {
        continue;
      }
      const DynamicEntityMaterialClass materialClass = dynamicEntityMaterialClassForQuad(quad);
      remixapi_MaterialHandle materialHandle = acquireDynamicEntityMaterial(
          buildState.texturePaths[quad.textureIndex],
          materialClass,
        buildState.hurtStage,
        buildState.creeperFuseStage);
      if (materialHandle == nullptr) {
        continue;
      }

      const auto normal = computeQuadNormal(
          quad.positions[0], quad.positions[1], quad.positions[2],
          quad.positions[3], quad.positions[4], quad.positions[5],
          quad.positions[6], quad.positions[7], quad.positions[8]);
      SurfaceBuildBuffers& surface = acquireSurface(materialHandle);
      const std::size_t vertexBase = surface.vertices.size();
      appendCloudQuad(
          quad.positions[0], quad.positions[1], quad.positions[2], quad.texcoords[0], quad.texcoords[1],
          quad.positions[3], quad.positions[4], quad.positions[5], quad.texcoords[2], quad.texcoords[3],
          quad.positions[6], quad.positions[7], quad.positions[8], quad.texcoords[4], quad.texcoords[5],
          quad.positions[9], quad.positions[10], quad.positions[11], quad.texcoords[6], quad.texcoords[7],
          normal[0], normal[1], normal[2],
          quad.color,
          surface.vertices,
          surface.indices);
      const std::size_t addedVertices = surface.vertices.size() - vertexBase;
      surface.blendWeights.insert(surface.blendWeights.end(), addedVertices, 1.0f);
      surface.blendIndices.insert(surface.blendIndices.end(), addedVertices, quad.boneIndex);
      ++quadCount;
    }
  }
  MCRTX_TRACY_VALUE(quadCount);

  std::vector<remixapi_MeshInfoSurfaceTriangles> surfaces;
  {
    MCRTX_TRACY_SCOPE("findOrCreateDynamicEntityMesh.finalizeSurfaces");
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
      surface.skinning_hasvalue = TRUE;
      surface.skinning_value.bonesPerVertex = 1;
      surface.skinning_value.blendWeights_values = surfaceBuild.blendWeights.data();
      surface.skinning_value.blendWeights_count = static_cast<std::uint32_t>(surfaceBuild.blendWeights.size());
      surface.skinning_value.blendIndices_values = surfaceBuild.blendIndices.data();
      surface.skinning_value.blendIndices_count = static_cast<std::uint32_t>(surfaceBuild.blendIndices.size());
      surface.material = surfaceBuild.materialHandle;
      surfaces.push_back(surface);
    }
  }
  MCRTX_TRACY_VALUE(surfaces.size());

  if (surfaces.empty()) {
    return nullptr;
  }

  remixapi_MeshInfo meshInfo {};
  meshInfo.sType = REMIXAPI_STRUCT_TYPE_MESH_INFO;
  meshInfo.hash = makeDynamicEntityMeshHash(meshKey);
  meshInfo.surfaces_values = surfaces.data();
  meshInfo.surfaces_count = static_cast<std::uint32_t>(surfaces.size());

  remixapi_MeshHandle meshHandle = nullptr;
  const remixapi_ErrorCode result = [&]() {
    MCRTX_TRACY_SCOPE("findOrCreateDynamicEntityMesh.createMesh");
    MCRTX_PERF_SCOPE(::mcrtx::perf::Side::Remix, "CreateMesh.entity");
    return remix_.CreateMesh(&meshInfo, &meshHandle);
  }();
  if (result != REMIXAPI_ERROR_CODE_SUCCESS) {
    setError("CreateMesh failed: " + errorCodeToString(result));
    return nullptr;
  }

  DynamicEntityMeshData meshData;
  meshData.meshHandle = meshHandle;
  meshData.meshHash = meshInfo.hash;
  meshData.geometryFingerprint = meshKey;
  meshData.quadCount = quadCount;
  meshData.boneCount = boneCount;
  const auto [it, inserted] = dynamicEntityMeshes_.emplace(meshKey, std::move(meshData));
  if (!inserted) {
    destroyMeshHandle(meshHandle);
  }
  return &it->second;
}

void RemixRenderer::clearDynamicEntityFrameInstances() {
  dynamicEntityFrameInstanceCount_ = 0;
}

void RemixRenderer::destroyDynamicEntityMeshes() {
  MCRTX_PERF_SCOPE(::mcrtx::perf::Side::Native, "RemixRenderer::destroyDynamicEntityMeshes");
  clearDynamicEntityFrameInstances();
  for (auto& [geometryFingerprint, meshData] : dynamicEntityMeshes_) {
    (void)geometryFingerprint;
    destroyDynamicEntityMesh(meshData);
  }
  dynamicEntityMeshes_.clear();
  dynamicEntityFrameInstances_.clear();
}

void RemixRenderer::destroyDynamicEntityMesh(DynamicEntityMeshData& meshData) {
  MCRTX_PERF_SCOPE(::mcrtx::perf::Side::Native, "RemixRenderer::destroyDynamicEntityMesh");
  destroyMeshHandle(meshData.meshHandle);
  meshData.meshHash = 0;
  meshData.geometryFingerprint = 0;
  meshData.quadCount = 0;
  meshData.boneCount = 0;
}

}  // namespace mcrtx
