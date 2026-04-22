// Auto-extracted from remix_renderer.cpp during the monolith split.
// Keep edits here; do not re-merge into remix_renderer.cpp.

#include "mcrtx/remix_renderer.hpp"
#include "mcrtx/render_internals.hpp"
#include "mcrtx/perf_log.hpp"

#include <algorithm>
#include <atomic>
#include <bit>
#include <cctype>
#include <cmath>
#include <cstdlib>
#include <cstddef>
#include <iostream>
#include <sstream>
#include <string_view>
#include <vector>

namespace mcrtx {

using namespace mcrtx::detail;

void RemixRenderer::updateCloudLayer(
    bool fancy,
    float cameraX,
    float cameraY,
    float cameraZ,
    float cloudHeight,
    float cloudScroll,
  float,
    float colorR,
    float colorG,
    float colorB) {
  MCRTX_PERF_SCOPE(::mcrtx::perf::Side::Native, "RemixRenderer::updateCloudLayer");
  std::scoped_lock lock(mutex_);

  if (!initialized_) {
    return;
  }

  cloudTransformX_ = 0.0f;
  cloudTransformY_ = 0.0f;
  cloudTransformZ_ = 0.0f;

  if (cloudMeshHandle_ == nullptr || cloudMeshFancy_ != fancy) {
    log(std::string("Rebuilding cloud mesh: mode=") + (fancy ? "fancy" : "fast")
        + (cloudMeshHandle_ == nullptr ? " reason=missing" : " reason=mode-switch"));
  }

  rebuildCloudMesh(fancy, cameraX, cameraY, cameraZ, cloudHeight, cloudScroll, colorR, colorG, colorB);
}

void RemixRenderer::updateAtmosphereState(float celestialAngle, bool forceDarkAtmosphere) {
  MCRTX_PERF_SCOPE(::mcrtx::perf::Side::Native, "RemixRenderer::updateAtmosphereState");
  std::scoped_lock lock(mutex_);

  if (!initialized_) {
    return;
  }

  updateAtmosphereConfigLocked(celestialAngle, forceDarkAtmosphere);
}

void RemixRenderer::clearCloudLayer() {
  MCRTX_PERF_SCOPE(::mcrtx::perf::Side::Native, "RemixRenderer::clearCloudLayer");
  std::scoped_lock lock(mutex_);
  if (cloudMeshHandle_ != nullptr) {
    log("Clearing cloud mesh cache");
  }
  destroyCloudMesh();
}

void RemixRenderer::beginDynamicEntityFrame() {
  MCRTX_PERF_SCOPE(::mcrtx::perf::Side::Native, "RemixRenderer::beginDynamicEntityFrame");
  std::scoped_lock lock(mutex_);

  if (!initialized_) {
    return;
  }

  clearDynamicEntityFrameInstances();
  activeDynamicEntity_ = {};
}

void RemixRenderer::beginDynamicEntity(int entityId) {
  MCRTX_PERF_SCOPE(::mcrtx::perf::Side::Native, "RemixRenderer::beginDynamicEntity");
  std::scoped_lock lock(mutex_);

  if (!initialized_) {
    return;
  }

  activeDynamicEntity_ = {};
  activeDynamicEntity_.entityId = entityId;
  activeDynamicEntity_.active = entityId >= 0;
  activeDynamicEntity_.quads.reserve(256);
  activeDynamicEntity_.boneTransforms.reserve(32);
}

void RemixRenderer::setDynamicEntityTexture(const std::string& texturePath) {
  MCRTX_PERF_SCOPE(::mcrtx::perf::Side::Native, "RemixRenderer::setDynamicEntityTexture");
  std::scoped_lock lock(mutex_);

  if (!initialized_ || !activeDynamicEntity_.active) {
    return;
  }

  activeDynamicEntity_.currentTexturePath = texturePath;
}

void RemixRenderer::setDynamicEntityBoneTransform(std::uint32_t boneIndex, const remixapi_Transform& transform) {
  MCRTX_PERF_SCOPE(::mcrtx::perf::Side::Native, "RemixRenderer::setDynamicEntityBoneTransform");
  std::scoped_lock lock(mutex_);

  if (!initialized_ || !activeDynamicEntity_.active || boneIndex >= REMIXAPI_INSTANCE_INFO_MAX_BONES_COUNT) {
    return;
  }

  if (boneIndex >= activeDynamicEntity_.boneTransforms.size()) {
    activeDynamicEntity_.boneTransforms.resize(static_cast<std::size_t>(boneIndex) + 1);
  }
  activeDynamicEntity_.boneTransforms[boneIndex] = transform;
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
    std::uint32_t boneIndex) {
  MCRTX_PERF_SCOPE(::mcrtx::perf::Side::Native, "RemixRenderer::captureDynamicEntityQuad");
  std::scoped_lock lock(mutex_);

  if (!initialized_ || !activeDynamicEntity_.active || activeDynamicEntity_.currentTexturePath.empty()) {
    return;
  }

  if (boneIndex >= activeDynamicEntity_.boneTransforms.size() || boneIndex >= REMIXAPI_INSTANCE_INFO_MAX_BONES_COUNT) {
    return;
  }

  DynamicEntityQuad quad;
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
  quad.texturePath = activeDynamicEntity_.currentTexturePath;
  quad.boneIndex = boneIndex;
  activeDynamicEntity_.quads.push_back(std::move(quad));
}

void RemixRenderer::endDynamicEntity() {
  MCRTX_PERF_SCOPE(::mcrtx::perf::Side::Native, "RemixRenderer::endDynamicEntity");
  std::scoped_lock lock(mutex_);

  if (!activeDynamicEntity_.active) {
    return;
  }

  if (!activeDynamicEntity_.quads.empty()) {
    const std::uint32_t boneCount = computeDynamicEntityBoneCount(activeDynamicEntity_.quads);
    if (boneCount != 0 && boneCount <= activeDynamicEntity_.boneTransforms.size()) {
      if (DynamicEntityMeshData* meshData = findOrCreateDynamicEntityMesh(activeDynamicEntity_); meshData != nullptr) {
        DynamicEntityFrameInstance frameInstance;
        frameInstance.entityId = activeDynamicEntity_.entityId;
        frameInstance.meshHandle = meshData->meshHandle;
        frameInstance.quadCount = meshData->quadCount;
        frameInstance.boneTransforms.assign(
            activeDynamicEntity_.boneTransforms.begin(),
            activeDynamicEntity_.boneTransforms.begin() + boneCount);
        dynamicEntityFrameInstances_.push_back(std::move(frameInstance));
      }
    }
  }

  activeDynamicEntity_ = {};
}

void RemixRenderer::beginDestroyOverlayFrame() {
  MCRTX_PERF_SCOPE(::mcrtx::perf::Side::Native, "RemixRenderer::beginDestroyOverlayFrame");
  std::scoped_lock lock(mutex_);

  if (!initialized_) {
    return;
  }

  destroyOverlayInstances_.clear();
}

void RemixRenderer::captureDestroyOverlay(
    int blockX,
    int blockY,
    int blockZ,
    int blockId,
    int blockMetadata,
    int renderType,
    int destroyStage) {
  MCRTX_PERF_SCOPE(::mcrtx::perf::Side::Native, "RemixRenderer::captureDestroyOverlay");
  std::scoped_lock lock(mutex_);

  if (!initialized_) {
    return;
  }

  DestroyOverlayInstance overlay;
  overlay.blockX = blockX;
  overlay.blockY = blockY;
  overlay.blockZ = blockZ;
  overlay.blockId = blockId;
  overlay.blockMetadata = blockMetadata;
  overlay.renderType = renderType;
  overlay.destroyStage = destroyStage;
  destroyOverlayInstances_.push_back(overlay);
}

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

void RemixRenderer::clearWorldScene() {
  MCRTX_PERF_SCOPE(::mcrtx::perf::Side::Native, "RemixRenderer::clearWorldScene");
  std::scoped_lock lock(mutex_);

  if (!initialized_) {
    return;
  }

  for (auto& [chunkKey, meshData] : chunkMeshes_) {
    (void)chunkKey;
    destroyChunkMesh(meshData);
  }
  chunkMeshes_.clear();

  destroyFireMesh();
  destroyDestroyOverlayMesh();
  destroyParticleMesh();
  destroyDynamicEntityMeshes();
  clearDynamicEntityFrameInstances();
  destroyOverlayInstances_.clear();
  particleQuads_.clear();
  activeDynamicEntity_ = {};
  activeChunkBlocks_.clear();
  activeChunkBuild_ = {};
  chunkBuildActive_ = false;
  lastSubmittedChunkCount_ = 0;
  lastSubmittedBlockCount_ = 0;
  lastSubmittedCloudQuadCount_ = 0;
  lastSubmittedFireQuadCount_ = 0;
  lastSubmittedDynamicEntityQuadCount_ = 0;
  lastSubmittedDestroyOverlayCount_ = 0;
  lastSubmittedParticleQuadCount_ = 0;
  lastSubmittedTorchLightCount_ = 0;
  lastFireAnimationFrame_ = 0xFFFFFFFFu;
  lastFireChunkBuildCount_ = 0xFFFFFFFFFFFFFFFFull;

  log("Cleared cached world scene state");
}

bool RemixRenderer::createTorchLight(const TorchLightPlacement& placement) {
  MCRTX_PERF_SCOPE(::mcrtx::perf::Side::Native, "RemixRenderer::createTorchLight");
  remixapi_LightInfoSphereEXT sphereInfo {};
  sphereInfo.sType = REMIXAPI_STRUCT_TYPE_LIGHT_INFO_SPHERE_EXT;
  sphereInfo.position = {
      placement.lightX,
      placement.lightY,
      placement.lightZ,
  };
  sphereInfo.radius = kTorchLightRadius;
  sphereInfo.shaping_hasvalue = FALSE;
  sphereInfo.volumetricRadianceScale = 1.0f;

  remixapi_LightInfo lightInfo {};
  lightInfo.sType = REMIXAPI_STRUCT_TYPE_LIGHT_INFO;
  lightInfo.pNext = &sphereInfo;
  lightInfo.hash = makeTorchLightHash(placement.blockPosition);
  lightInfo.radiance = placement.radiance;
  lightInfo.isDynamic = FALSE;
  lightInfo.ignoreViewModel = FALSE;

  remixapi_LightHandle lightHandle = nullptr;
  remixapi_ErrorCode result;
  if (remix_.CreateLightBatched != nullptr) {
    result = [&]() {
      MCRTX_PERF_SCOPE(::mcrtx::perf::Side::Remix, "CreateLightBatched.torch");
      return remix_.CreateLightBatched(&lightInfo, &lightHandle);
    }();
  } else {
    result = [&]() {
      MCRTX_PERF_SCOPE(::mcrtx::perf::Side::Remix, "CreateLight.torch");
      return remix_.CreateLight(&lightInfo, &lightHandle);
    }();
  }
  if (result != REMIXAPI_ERROR_CODE_SUCCESS) {
    setError("CreateLight failed: " + errorCodeToString(result));
    return false;
  }

  torchLights_[placement.blockPosition] = lightHandle;
  return true;
}

bool RemixRenderer::updateTorchLight(const TorchLightPlacement& placement) {
  MCRTX_PERF_SCOPE(::mcrtx::perf::Side::Native, "RemixRenderer::updateTorchLight");
  const auto lightIt = torchLights_.find(placement.blockPosition);
  if (lightIt == torchLights_.end() || lightIt->second == nullptr) {
    return createTorchLight(placement);
  }

  if (remix_.UpdateLightDefinition == nullptr) {
    destroyTorchLight(placement.blockPosition);
    return createTorchLight(placement);
  }

  remixapi_LightInfoSphereEXT sphereInfo {};
  sphereInfo.sType = REMIXAPI_STRUCT_TYPE_LIGHT_INFO_SPHERE_EXT;
  sphereInfo.position = {placement.lightX, placement.lightY, placement.lightZ};
  sphereInfo.radius = kTorchLightRadius;
  sphereInfo.shaping_hasvalue = FALSE;
  sphereInfo.volumetricRadianceScale = 1.0f;

  remixapi_LightInfo lightInfo {};
  lightInfo.sType = REMIXAPI_STRUCT_TYPE_LIGHT_INFO;
  lightInfo.pNext = &sphereInfo;
  lightInfo.hash = makeTorchLightHash(placement.blockPosition);
  lightInfo.radiance = placement.radiance;
  lightInfo.isDynamic = FALSE;
  lightInfo.ignoreViewModel = FALSE;

  const remixapi_ErrorCode result = [&]() {
    MCRTX_PERF_SCOPE(::mcrtx::perf::Side::Remix, "UpdateLightDefinition.torch");
    return remix_.UpdateLightDefinition(lightIt->second, &lightInfo);
  }();
  if (result != REMIXAPI_ERROR_CODE_SUCCESS) {
    setError("UpdateLightDefinition failed: " + errorCodeToString(result));
    return false;
  }

  return true;
}

bool RemixRenderer::reconcileChunkTorchLights(
    ChunkMeshData& meshData,
    const std::vector<TorchLightPlacement>& desiredTorchLights) {
  MCRTX_PERF_SCOPE(::mcrtx::perf::Side::Native, "RemixRenderer::reconcileChunkTorchLights");
  if (remix_.CreateLight == nullptr) {
    destroyChunkTorchLights(meshData);
    return true;
  }

  std::vector<WorldBlockPosition> createdLights;
  createdLights.reserve(desiredTorchLights.size());
  for (const TorchLightPlacement& placement : desiredTorchLights) {
    const bool existed = torchLights_.find(placement.blockPosition) != torchLights_.end();
    if (!updateTorchLight(placement)) {
      for (const WorldBlockPosition& createdPosition : createdLights) {
        destroyTorchLight(createdPosition);
      }
      return false;
    }
    if (!existed) {
      createdLights.push_back(placement.blockPosition);
    }
  }

  for (const TorchLightPlacement& placement : meshData.torchLights) {
    if (findTorchLightPlacement(desiredTorchLights, placement.blockPosition) == nullptr) {
      destroyTorchLight(placement.blockPosition);
    }
  }

  meshData.torchLights = desiredTorchLights;
  return true;
}

bool RemixRenderer::rebuildCloudMesh(
    bool fancy,
    float cameraX,
    float cameraY,
    float cameraZ,
    float cloudHeight,
    float cloudScroll,
    float colorR,
    float colorG,
    float colorB) {
  MCRTX_PERF_SCOPE(::mcrtx::perf::Side::Native, "RemixRenderer::rebuildCloudMesh");
  if (cloudMaterialHandle_ == nullptr) {
    destroyCloudMesh();
    return true;
  }

  std::vector<remixapi_HardcodedVertex> vertices;
  std::vector<std::uint32_t> indices;
  vertices.reserve(fancy ? 4096 : 2048);
  indices.reserve(fancy ? 6144 : 3072);

  if (fancy) {
    appendFancyCloudGeometry(cameraX, cameraY, cameraZ, cloudHeight, cloudScroll, colorR, colorG, colorB, vertices, indices);
  } else {
    appendFastCloudGeometry(cameraX, cameraZ, cloudHeight, cloudScroll, colorR, colorG, colorB, vertices, indices);
  }

  if (indices.empty()) {
    destroyCloudMesh();
    return true;
  }

  remixapi_MeshInfoSurfaceTriangles surface {};
  surface.vertices_values = vertices.data();
  surface.vertices_count = vertices.size();
  surface.indices_values = indices.data();
  surface.indices_count = indices.size();
  surface.skinning_hasvalue = FALSE;
  surface.material = cloudMaterialHandle_;

  remixapi_MeshInfo meshInfo {};
  meshInfo.sType = REMIXAPI_STRUCT_TYPE_MESH_INFO;
  meshInfo.hash = makeCloudMeshHash(nextCloudMeshHash_++);
  meshInfo.surfaces_values = &surface;
  meshInfo.surfaces_count = 1;

  remixapi_MeshHandle newMeshHandle = nullptr;
  const remixapi_ErrorCode result = [&]() {
    MCRTX_PERF_SCOPE(::mcrtx::perf::Side::Remix, "CreateMesh.cloud");
    return remix_.CreateMesh(&meshInfo, &newMeshHandle);
  }();
  if (result != REMIXAPI_ERROR_CODE_SUCCESS) {
    setError("CreateMesh failed: " + errorCodeToString(result));
    return false;
  }

  destroyCloudMesh();
  cloudMeshHandle_ = newMeshHandle;
  cloudMeshFancy_ = fancy;
  cloudMeshPhaseX_ = 0;
  cloudMeshPhaseZ_ = 0;
  cloudQuadCount_ = indices.size() / 6;
  log(std::string("Cloud mesh ready: mode=") + (fancy ? "fancy" : "fast")
      + " quads=" + std::to_string(cloudQuadCount_)
      + " hash=0x" + [&]() {
          std::ostringstream stream;
          stream << std::hex << meshInfo.hash;
          return stream.str();
        }());
  return true;
}

DynamicEntityMeshData* RemixRenderer::findOrCreateDynamicEntityMesh(const DynamicEntityBuildState& buildState) {
  MCRTX_PERF_SCOPE(::mcrtx::perf::Side::Native, "RemixRenderer::findOrCreateDynamicEntityMesh");
  if (buildState.quads.empty()) {
    return nullptr;
  }

  const std::uint32_t boneCount = computeDynamicEntityBoneCount(buildState.quads);
  if (boneCount == 0
      || boneCount > buildState.boneTransforms.size()
      || boneCount > REMIXAPI_INSTANCE_INFO_MAX_BONES_COUNT) {
    return nullptr;
  }

  const std::uint64_t geometryFingerprint = computeDynamicEntityFingerprint(buildState.quads, boneCount);
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
  for (const DynamicEntityQuad& quad : buildState.quads) {
    remixapi_MaterialHandle materialHandle = acquireDynamicEntityMaterial(quad.texturePath);
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

  std::vector<remixapi_MeshInfoSurfaceTriangles> surfaces;
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

void RemixRenderer::destroyCloudMesh() {
  MCRTX_PERF_SCOPE(::mcrtx::perf::Side::Native, "RemixRenderer::destroyCloudMesh");
  destroyMeshHandle(cloudMeshHandle_);
  cloudMeshFancy_ = false;
  cloudQuadCount_ = 0;
}

void RemixRenderer::destroyFireMesh() {
  MCRTX_PERF_SCOPE(::mcrtx::perf::Side::Native, "RemixRenderer::destroyFireMesh");
  destroyMeshHandle(fireMeshHandle_);
  fireQuadCount_ = 0;
}

void RemixRenderer::destroyDestroyOverlayMesh() {
  MCRTX_PERF_SCOPE(::mcrtx::perf::Side::Native, "RemixRenderer::destroyDestroyOverlayMesh");
  destroyMeshHandle(destroyOverlayMeshHandle_);
  destroyOverlayCount_ = 0;
}

void RemixRenderer::destroyParticleMesh() {
  MCRTX_PERF_SCOPE(::mcrtx::perf::Side::Native, "RemixRenderer::destroyParticleMesh");
  destroyMeshHandle(particleMeshHandle_);
  particleQuadCount_ = 0;
}

void RemixRenderer::destroyTorchLight(const WorldBlockPosition& position) {
  MCRTX_PERF_SCOPE(::mcrtx::perf::Side::Native, "RemixRenderer::destroyTorchLight");
  const auto lightIt = torchLights_.find(position);
  if (lightIt == torchLights_.end()) {
    return;
  }

  destroyLightHandle(lightIt->second);
  torchLights_.erase(lightIt);
}

void RemixRenderer::destroyChunkTorchLights(ChunkMeshData& meshData) {
  MCRTX_PERF_SCOPE(::mcrtx::perf::Side::Native, "RemixRenderer::destroyChunkTorchLights");
  for (const TorchLightPlacement& placement : meshData.torchLights) {
    destroyTorchLight(placement.blockPosition);
  }
  meshData.torchLights.clear();
}

void RemixRenderer::clearDynamicEntityFrameInstances() {
  dynamicEntityFrameInstances_.clear();
}

void RemixRenderer::destroyDynamicEntityMeshes() {
  MCRTX_PERF_SCOPE(::mcrtx::perf::Side::Native, "RemixRenderer::destroyDynamicEntityMeshes");
  clearDynamicEntityFrameInstances();
  for (auto& [geometryFingerprint, meshData] : dynamicEntityMeshes_) {
    (void)geometryFingerprint;
    destroyDynamicEntityMesh(meshData);
  }
  dynamicEntityMeshes_.clear();
}

void RemixRenderer::destroyDynamicEntityMesh(DynamicEntityMeshData& meshData) {
  MCRTX_PERF_SCOPE(::mcrtx::perf::Side::Native, "RemixRenderer::destroyDynamicEntityMesh");
  destroyMeshHandle(meshData.meshHandle);
  meshData.meshHash = 0;
  meshData.geometryFingerprint = 0;
  meshData.quadCount = 0;
  meshData.boneCount = 0;
}

bool RemixRenderer::rebuildDestroyOverlayMesh() {
  MCRTX_PERF_SCOPE(::mcrtx::perf::Side::Native, "RemixRenderer::rebuildDestroyOverlayMesh");
  if (destroyOverlayInstances_.empty()) {
    destroyDestroyOverlayMesh();
    return true;
  }

  remixapi_MaterialHandle overlayMaterial = terrainMaterialHandles_[kCutoutTerrainMaterialClass];
  if (overlayMaterial == nullptr) {
    destroyDestroyOverlayMesh();
    return true;
  }

  const auto chunkOriginForWorld = [](int coordinate) {
    return coordinate >= 0
        ? (coordinate / kChunkDimension) * kChunkDimension
        : (((coordinate + 1) / kChunkDimension) - 1) * kChunkDimension;
  };

  const auto findWorldCell = [this, &chunkOriginForWorld](int worldX, int worldY, int worldZ) -> const ChunkBlockCell* {
    const int originX = chunkOriginForWorld(worldX);
    const int originY = chunkOriginForWorld(worldY);
    const int originZ = chunkOriginForWorld(worldZ);
    const int localX = worldX - originX;
    const int localY = worldY - originY;
    const int localZ = worldZ - originZ;
    const int cellIndex = blockIndex(localX, localY, localZ);

    for (int renderPass = 0; renderPass <= 1; ++renderPass) {
      const ChunkKey chunkKey {originX, originY, originZ, renderPass};
      const auto it = chunkMeshes_.find(chunkKey);
      if (it == chunkMeshes_.end() || !it->second.hasOccupancy || it->second.occupancy[cellIndex] == 0) {
        continue;
      }
      return &it->second.cells[cellIndex];
    }

    return nullptr;
  };

  const auto hasFenceNeighbor = [&findWorldCell](int worldX, int worldY, int worldZ) {
    const ChunkBlockCell* neighborCell = findWorldCell(worldX, worldY, worldZ);
    return neighborCell != nullptr
        && neighborCell->blockId == kFenceBlockId
        && neighborCell->renderType == kFenceBlockRenderType;
  };

  const auto hasSolidSupport = [&findWorldCell](int worldX, int worldY, int worldZ) {
    const ChunkBlockCell* neighborCell = findWorldCell(worldX, worldY, worldZ);
    return neighborCell != nullptr && isSolidSupportBlock(*neighborCell);
  };

  const auto hasRedstoneConnection = [&findWorldCell](int worldX, int worldY, int worldZ, int direction) {
    const ChunkBlockCell* neighborCell = findWorldCell(worldX, worldY, worldZ);
    return neighborCell != nullptr && isRedstoneConnectionCell(*neighborCell, direction);
  };

  std::vector<remixapi_HardcodedVertex> vertices;
  std::vector<std::uint32_t> indices;
  vertices.reserve(destroyOverlayInstances_.size() * 24);
  indices.reserve(destroyOverlayInstances_.size() * 36);

  std::size_t overlayCount = 0;
  for (const DestroyOverlayInstance& overlay : destroyOverlayInstances_) {
    const int destroyTile = 240 + std::clamp(overlay.destroyStage, 0, 9);
    ChunkBlockCell resolvedCell {};
    if (const ChunkBlockCell* worldCell = findWorldCell(overlay.blockX, overlay.blockY, overlay.blockZ); worldCell != nullptr) {
      resolvedCell = *worldCell;
    } else {
      resolvedCell.blockId = static_cast<std::uint8_t>(overlay.blockId);
      resolvedCell.blockMetadata = static_cast<std::uint8_t>(overlay.blockMetadata);
      resolvedCell.renderType = static_cast<std::uint8_t>(overlay.renderType);
    }
    resolvedCell.terrainTiles.fill(static_cast<std::int16_t>(destroyTile));

    const float localX = static_cast<float>(overlay.blockX);
    const float localY = static_cast<float>(overlay.blockY);
    const float localZ = static_cast<float>(overlay.blockZ);

    if (resolvedCell.renderType == kLiquidBlockRenderType) {
      continue;
    }

    if (isCrossedQuadRenderType(resolvedCell.renderType)) {
      appendCrossedQuadGeometry(
          resolvedCell,
          overlay.blockX,
          overlay.blockY,
          overlay.blockZ,
          localX,
          localY,
          localZ,
          vertices,
          indices);
      ++overlayCount;
      continue;
    }

    if (isTorchRenderType(resolvedCell.renderType)) {
      appendTorchGeometry(
          resolvedCell,
          localX,
          localY,
          localZ,
          vertices,
          indices);
      ++overlayCount;
      continue;
    }

    if (isLadderRenderType(resolvedCell.renderType)) {
      appendLadderGeometry(
          resolvedCell,
          localX,
          localY,
          localZ,
          vertices,
          indices);
      ++overlayCount;
      continue;
    }

        if (isRedstoneDustRenderType(resolvedCell.renderType) && isRedstoneDustBlockId(resolvedCell.blockId)) {
          const bool blockedAbove = hasSolidSupport(overlay.blockX, overlay.blockY + 1, overlay.blockZ);
          bool connectWest = hasRedstoneConnection(overlay.blockX - 1, overlay.blockY, overlay.blockZ, 1)
            || (!hasSolidSupport(overlay.blockX - 1, overlay.blockY, overlay.blockZ)
              && hasRedstoneConnection(overlay.blockX - 1, overlay.blockY - 1, overlay.blockZ, -1));
          bool connectEast = hasRedstoneConnection(overlay.blockX + 1, overlay.blockY, overlay.blockZ, 3)
            || (!hasSolidSupport(overlay.blockX + 1, overlay.blockY, overlay.blockZ)
              && hasRedstoneConnection(overlay.blockX + 1, overlay.blockY - 1, overlay.blockZ, -1));
          bool connectNorth = hasRedstoneConnection(overlay.blockX, overlay.blockY, overlay.blockZ - 1, 2)
            || (!hasSolidSupport(overlay.blockX, overlay.blockY, overlay.blockZ - 1)
              && hasRedstoneConnection(overlay.blockX, overlay.blockY - 1, overlay.blockZ - 1, -1));
          bool connectSouth = hasRedstoneConnection(overlay.blockX, overlay.blockY, overlay.blockZ + 1, 0)
            || (!hasSolidSupport(overlay.blockX, overlay.blockY, overlay.blockZ + 1)
              && hasRedstoneConnection(overlay.blockX, overlay.blockY - 1, overlay.blockZ + 1, -1));

          const bool climbWest = !blockedAbove && hasSolidSupport(overlay.blockX - 1, overlay.blockY, overlay.blockZ)
            && hasRedstoneConnection(overlay.blockX - 1, overlay.blockY + 1, overlay.blockZ, -1);
          const bool climbEast = !blockedAbove && hasSolidSupport(overlay.blockX + 1, overlay.blockY, overlay.blockZ)
            && hasRedstoneConnection(overlay.blockX + 1, overlay.blockY + 1, overlay.blockZ, -1);
          const bool climbNorth = !blockedAbove && hasSolidSupport(overlay.blockX, overlay.blockY, overlay.blockZ - 1)
            && hasRedstoneConnection(overlay.blockX, overlay.blockY + 1, overlay.blockZ - 1, -1);
          const bool climbSouth = !blockedAbove && hasSolidSupport(overlay.blockX, overlay.blockY, overlay.blockZ + 1)
            && hasRedstoneConnection(overlay.blockX, overlay.blockY + 1, overlay.blockZ + 1, -1);

          connectWest = connectWest || climbWest;
          connectEast = connectEast || climbEast;
          connectNorth = connectNorth || climbNorth;
          connectSouth = connectSouth || climbSouth;

          appendRedstoneDustGeometry(
            resolvedCell,
            connectWest,
            connectEast,
            connectNorth,
            connectSouth,
            climbWest,
            climbEast,
            climbNorth,
            climbSouth,
            localX,
            localY,
            localZ,
            vertices,
            indices);
          ++overlayCount;
          continue;
        }

    if (isRailRenderType(resolvedCell.renderType) && isRailBlockId(resolvedCell.blockId)) {
      appendRailGeometry(
          resolvedCell,
          localX,
          localY,
          localZ,
          vertices,
          indices);
      ++overlayCount;
      continue;
    }

    if (isStairRenderType(resolvedCell.renderType) && isStairBlockId(resolvedCell.blockId)) {
      appendStairGeometry(
          resolvedCell,
          localX,
          localY,
          localZ,
          vertices,
          indices);
      ++overlayCount;
      continue;
    }

    if (isLeverOrButtonRenderType(resolvedCell.renderType)) {
      if (isLeverBlockId(resolvedCell.blockId)) {
        appendLeverGeometry(
            resolvedCell,
            localX,
            localY,
            localZ,
            vertices,
            indices);
      } else if (isButtonBlockId(resolvedCell.blockId)) {
        appendBoxGeometry(
            localX + resolvedCell.bounds[0],
            localY + resolvedCell.bounds[1],
            localZ + resolvedCell.bounds[2],
            localX + resolvedCell.bounds[3],
            localY + resolvedCell.bounds[4],
            localZ + resolvedCell.bounds[5],
            resolvedCell.terrainTiles,
            kDefaultVertexColor,
            vertices,
            indices);
      }
      ++overlayCount;
      continue;
    }

    if (isDoorRenderType(resolvedCell.renderType) && isDoorBlockId(resolvedCell.blockId)) {
      const ChunkBlockCell* pairedDoorCell = (resolvedCell.blockMetadata & 8) != 0
          ? findWorldCell(overlay.blockX, overlay.blockY - 1, overlay.blockZ)
          : findWorldCell(overlay.blockX, overlay.blockY + 1, overlay.blockZ);
      int resolvedDoorMetadata = resolvedCell.blockMetadata & 0xF;
      if ((resolvedDoorMetadata & 8) != 0) {
        if (pairedDoorCell != nullptr && pairedDoorCell->blockId == resolvedCell.blockId) {
          resolvedDoorMetadata = pairedDoorCell->blockMetadata & 0xF;
        } else {
          resolvedDoorMetadata &= 7;
        }
      } else if (pairedDoorCell != nullptr
          && pairedDoorCell->blockId == resolvedCell.blockId
          && (pairedDoorCell->blockMetadata & 4) != 0) {
        resolvedDoorMetadata = (resolvedDoorMetadata & 3) | 4;
      }

      appendDoorGeometry(
          resolvedCell,
          resolvedDoorMetadata,
          localX,
          localY,
          localZ,
          vertices,
          indices);
      ++overlayCount;
      continue;
    }

    if (isFenceRenderType(resolvedCell.renderType) && resolvedCell.blockId == kFenceBlockId) {
      appendFenceGeometry(
          hasFenceNeighbor(overlay.blockX - 1, overlay.blockY, overlay.blockZ),
          hasFenceNeighbor(overlay.blockX + 1, overlay.blockY, overlay.blockZ),
          hasFenceNeighbor(overlay.blockX, overlay.blockY, overlay.blockZ - 1),
          hasFenceNeighbor(overlay.blockX, overlay.blockY, overlay.blockZ + 1),
          resolvedCell,
          localX,
          localY,
          localZ,
          vertices,
          indices);
      ++overlayCount;
      continue;
    }

    if (isCactusRenderType(resolvedCell.renderType) && isCactusBlockId(resolvedCell.blockId)) {
      const ChunkBlockCell* belowCell = findWorldCell(overlay.blockX, overlay.blockY - 1, overlay.blockZ);
      const ChunkBlockCell* aboveCell = findWorldCell(overlay.blockX, overlay.blockY + 1, overlay.blockZ);
      const ChunkBlockCell* northCell = findWorldCell(overlay.blockX, overlay.blockY, overlay.blockZ - 1);
      const ChunkBlockCell* southCell = findWorldCell(overlay.blockX, overlay.blockY, overlay.blockZ + 1);
      const ChunkBlockCell* westCell = findWorldCell(overlay.blockX - 1, overlay.blockY, overlay.blockZ);
      const ChunkBlockCell* eastCell = findWorldCell(overlay.blockX + 1, overlay.blockY, overlay.blockZ);

      appendCactusGeometry(
          belowCell == nullptr || !isSolidSupportBlock(*belowCell),
          aboveCell == nullptr || !isSolidSupportBlock(*aboveCell),
          northCell == nullptr || !isSolidSupportBlock(*northCell),
          southCell == nullptr || !isSolidSupportBlock(*southCell),
          westCell == nullptr || !isSolidSupportBlock(*westCell),
          eastCell == nullptr || !isSolidSupportBlock(*eastCell),
          resolvedCell,
          localX,
          localY,
          localZ,
          vertices,
          indices);
      ++overlayCount;
      continue;
    }

    if (isBedRenderType(resolvedCell.renderType) && isBedBlockId(resolvedCell.blockId)) {
      appendBedGeometry(
          resolvedCell,
          localX,
          localY,
          localZ,
          vertices,
          indices);
      ++overlayCount;
      continue;
    }

    if (resolvedCell.renderType == kCubeBlockRenderType && usesPartialCubeBounds(resolvedCell)) {
      appendBoxGeometry(
          localX + resolvedCell.bounds[0],
          localY + resolvedCell.bounds[1],
          localZ + resolvedCell.bounds[2],
          localX + resolvedCell.bounds[3],
          localY + resolvedCell.bounds[4],
          localZ + resolvedCell.bounds[5],
          resolvedCell.terrainTiles,
          kDefaultVertexColor,
          vertices,
          indices);
      ++overlayCount;
      continue;
    }

    bool emittedFace = false;
    for (int faceIndex = 0; faceIndex < 6; ++faceIndex) {
      const ChunkBlockCell* neighborCell = findWorldCell(
          overlay.blockX + kNeighborOffsets[faceIndex][0],
          overlay.blockY + kNeighborOffsets[faceIndex][1],
          overlay.blockZ + kNeighborOffsets[faceIndex][2]);
      if (neighborCell != nullptr && shouldCullFaceAgainstNeighbor(resolvedCell, *neighborCell)) {
        continue;
      }

      const int minecraftSide = kNativeFaceToMinecraftSide[faceIndex];
      appendFaceGeometry(
          faceIndex,
          localX,
          localY,
          localZ,
          resolvedCell.terrainTiles[minecraftSide],
          kDefaultVertexColor,
          kFaceOverlayBias,
          vertices,
          indices);
      emittedFace = true;
    }

    if (emittedFace) {
      ++overlayCount;
    }
  }

  if (indices.empty()) {
    destroyDestroyOverlayMesh();
    return true;
  }

  remixapi_MeshInfoSurfaceTriangles surface {};
  surface.vertices_values = vertices.data();
  surface.vertices_count = vertices.size();
  surface.indices_values = indices.data();
  surface.indices_count = indices.size();
  surface.skinning_hasvalue = FALSE;
  surface.material = overlayMaterial;

  remixapi_MeshInfo meshInfo {};
  meshInfo.sType = REMIXAPI_STRUCT_TYPE_MESH_INFO;
  meshInfo.hash = makeDestroyOverlayMeshHash(nextDestroyOverlayMeshHash_++);
  meshInfo.surfaces_values = &surface;
  meshInfo.surfaces_count = 1;

  remixapi_MeshHandle newMeshHandle = nullptr;
  const remixapi_ErrorCode result = [&]() {
    MCRTX_PERF_SCOPE(::mcrtx::perf::Side::Remix, "CreateMesh.destroy");
    return remix_.CreateMesh(&meshInfo, &newMeshHandle);
  }();
  if (result != REMIXAPI_ERROR_CODE_SUCCESS) {
    setError("CreateMesh failed: " + errorCodeToString(result));
    return false;
  }

  destroyDestroyOverlayMesh();
  destroyOverlayMeshHandle_ = newMeshHandle;
  destroyOverlayCount_ = overlayCount;
  return true;
}

bool RemixRenderer::rebuildFireMesh() {
  MCRTX_PERF_SCOPE(::mcrtx::perf::Side::Native, "RemixRenderer::rebuildFireMesh");
  if (fireMaterialHandle_ == nullptr) {    destroyFireMesh();
    return true;
  }

  const std::uint32_t frameIndex = static_cast<std::uint32_t>((GetTickCount64() / kFireAnimationFrameIntervalMilliseconds) % kFireAnimationFrameCount);
  if (fireMeshHandle_ != nullptr
      && lastFireAnimationFrame_ == frameIndex
      && lastFireChunkBuildCount_ == capturedChunkBuilds_) {
    return true;
  }

  const auto chunkOriginForWorld = [](int coordinate) {
    return coordinate >= 0
        ? (coordinate / kChunkDimension) * kChunkDimension
        : (((coordinate + 1) / kChunkDimension) - 1) * kChunkDimension;
  };

  const auto findWorldCell = [this, &chunkOriginForWorld](int worldX, int worldY, int worldZ) -> const ChunkBlockCell* {
    const int originX = chunkOriginForWorld(worldX);
    const int originY = chunkOriginForWorld(worldY);
    const int originZ = chunkOriginForWorld(worldZ);
    const int localX = worldX - originX;
    const int localY = worldY - originY;
    const int localZ = worldZ - originZ;
    const int cellIndex = blockIndex(localX, localY, localZ);

    for (int renderPass = 0; renderPass <= 1; ++renderPass) {
      const ChunkKey chunkKey {originX, originY, originZ, renderPass};
      const auto it = chunkMeshes_.find(chunkKey);
      if (it == chunkMeshes_.end() || !it->second.hasOccupancy || it->second.occupancy[cellIndex] == 0) {
        continue;
      }
      return &it->second.cells[cellIndex];
    }

    return nullptr;
  };

  std::vector<remixapi_HardcodedVertex> vertices;
  std::vector<std::uint32_t> indices;
  vertices.reserve(256);
  indices.reserve(384);

  std::size_t fireCount = 0;
  for (const auto& [chunkKey, meshData] : chunkMeshes_) {
    if (chunkKey.renderPass != 0 || !meshData.hasOccupancy || meshData.fireCellIndices.empty()) {
      continue;
    }

    for (std::uint16_t fireCellIndex : meshData.fireCellIndices) {
      const ChunkBlockCell& cell = meshData.cells[fireCellIndex];
      if (!isFireRenderType(cell.renderType)) {
        continue;
      }

      const int localY = fireCellIndex / (kChunkDimension * kChunkDimension);
      const int localPlaneIndex = fireCellIndex % (kChunkDimension * kChunkDimension);
      const int localZ = localPlaneIndex / kChunkDimension;
      const int localX = localPlaneIndex % kChunkDimension;
      const int worldX = chunkKey.originX + localX;
      const int worldY = chunkKey.originY + localY;
      const int worldZ = chunkKey.originZ + localZ;
      appendFireGeometry(
          worldX,
          worldY,
          worldZ,
          findWorldCell(worldX, worldY - 1, worldZ) != nullptr,
          findWorldCell(worldX - 1, worldY, worldZ) != nullptr,
          findWorldCell(worldX + 1, worldY, worldZ) != nullptr,
          findWorldCell(worldX, worldY, worldZ - 1) != nullptr,
          findWorldCell(worldX, worldY, worldZ + 1) != nullptr,
          findWorldCell(worldX, worldY + 1, worldZ) != nullptr,
          static_cast<float>(worldX),
          static_cast<float>(worldY),
          static_cast<float>(worldZ),
          frameIndex,
          vertices,
          indices);
      ++fireCount;
    }
  }

  if (indices.empty()) {
    destroyFireMesh();
    lastFireAnimationFrame_ = frameIndex;
    lastFireChunkBuildCount_ = capturedChunkBuilds_;
    return true;
  }

  remixapi_MeshInfoSurfaceTriangles surface {};
  surface.vertices_values = vertices.data();
  surface.vertices_count = vertices.size();
  surface.indices_values = indices.data();
  surface.indices_count = indices.size();
  surface.skinning_hasvalue = FALSE;
  surface.material = fireMaterialHandle_;

  remixapi_MeshInfo meshInfo {};
  meshInfo.sType = REMIXAPI_STRUCT_TYPE_MESH_INFO;
  meshInfo.hash = makeFireMeshHash(nextFireMeshHash_++);
  meshInfo.surfaces_values = &surface;
  meshInfo.surfaces_count = 1;

  remixapi_MeshHandle newMeshHandle = nullptr;
  const remixapi_ErrorCode result = [&]() {
    MCRTX_PERF_SCOPE(::mcrtx::perf::Side::Remix, "CreateMesh.fire");
    return remix_.CreateMesh(&meshInfo, &newMeshHandle);
  }();
  if (result != REMIXAPI_ERROR_CODE_SUCCESS) {
    setError("CreateMesh failed: " + errorCodeToString(result));
    return false;
  }

  destroyFireMesh();
  fireMeshHandle_ = newMeshHandle;
  fireQuadCount_ = fireCount;
  lastFireAnimationFrame_ = frameIndex;
  lastFireChunkBuildCount_ = capturedChunkBuilds_;
  return true;
}

bool RemixRenderer::rebuildParticleMesh() {
  MCRTX_PERF_SCOPE(::mcrtx::perf::Side::Native, "RemixRenderer::rebuildParticleMesh");
  if (particleQuads_.empty()) {
    destroyParticleMesh();
    return true;
  }

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
    appendCloudQuad(
        quad.positions[0], quad.positions[1], quad.positions[2], quad.texcoords[0], quad.texcoords[1],
        quad.positions[3], quad.positions[4], quad.positions[5], quad.texcoords[2], quad.texcoords[3],
        quad.positions[6], quad.positions[7], quad.positions[8], quad.texcoords[4], quad.texcoords[5],
        quad.positions[9], quad.positions[10], quad.positions[11], quad.texcoords[6], quad.texcoords[7],
        normal[0], normal[1], normal[2],
        quad.color,
        surface.vertices,
        surface.indices);
    ++quadCount;
  }

  std::vector<remixapi_MeshInfoSurfaceTriangles> surfaces;
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