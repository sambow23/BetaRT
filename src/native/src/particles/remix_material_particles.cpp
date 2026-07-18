// Particle material cache acquisition.

#include "mcrtx/core/remix_renderer.hpp"
#include "mcrtx/materials/remix_material_common.hpp"
#include "mcrtx/core/remix_render_common.hpp"
#include "mcrtx/lifecycle/perf_log.hpp"

namespace mcrtx {

using namespace mcrtx::detail;
using namespace mcrtx::material;

remixapi_MaterialHandle RemixRenderer::acquireParticleMaterial(std::uint32_t textureKind) {
  MCRTX_PERF_SCOPE(::mcrtx::perf::Side::Native, "RemixRenderer::acquireParticleMaterial");
  if (textureKind == 1 || textureKind == 3) {
    return terrainMaterialHandles_[kCutoutTerrainMaterialClass];
  }

  const auto existing = particleMaterialHandles_.find(textureKind);
  if (existing != particleMaterialHandles_.end()) {
    return existing->second;
  }

  const std::filesystem::path resolvedTexturePath = resolveParticleTexturePath(textureKind);
  if (resolvedTexturePath.empty()) {
    return nullptr;
  }
  const OptionalPbrTextures pbrTextures = resolveOptionalPbrTextures(resolvedTexturePath);
  const OptionalSssTextures sssTextures = resolveOptionalSssTextures(resolvedTexturePath);
  const OpaqueSubsurfaceSettings subsurfaceSettings {
      subsurfaceMeasurementDistance_,
      subsurfaceRadiusScale_,
      subsurfaceMaxSampleRadius_,
      subsurfaceVolumetricAnisotropy_,
      subsurfaceDiffusionProfileEnabled_};

  remixapi_MaterialInfoOpaqueEXT opaqueInfo {};
  remixapi_MaterialInfoOpaqueSubsurfaceEXT subsurfaceInfo {};
  opaqueInfo.sType = REMIXAPI_STRUCT_TYPE_MATERIAL_INFO_OPAQUE_EXT;
  opaqueInfo.albedoConstant = {1.0f, 1.0f, 1.0f};
  opaqueInfo.opacityConstant = 1.0f;
  opaqueInfo.roughnessConstant = 1.0f;
  opaqueInfo.metallicConstant = 0.0f;
  opaqueInfo.useDrawCallAlphaState = FALSE;
  opaqueInfo.alphaTestType = 4;
  opaqueInfo.alphaReferenceValue = 1;

  remixapi_MaterialInfo materialInfo {};
  materialInfo.sType = REMIXAPI_STRUCT_TYPE_MATERIAL_INFO;
  materialInfo.pNext = &opaqueInfo;
  materialInfo.hash = kParticleMaterialHashSeed ^ static_cast<std::uint64_t>(textureKind);
  materialInfo.albedoTexture = resolvedTexturePath.c_str();
  materialInfo.emissiveIntensity = 0.0f;
  materialInfo.emissiveColorConstant = {0.0f, 0.0f, 0.0f};
  materialInfo.filterMode = 0;
  materialInfo.wrapModeU = 1;
  materialInfo.wrapModeV = 1;
  applyOptionalOpaqueMaterialTextures(
      materialInfo,
      opaqueInfo,
      subsurfaceInfo,
      pbrTextures,
      sssTextures,
      displacementFactor_,
      subsurfaceSettings);

  remixapi_MaterialHandle materialHandle = nullptr;
  const remixapi_ErrorCode result = [&]() {
    MCRTX_PERF_SCOPE(::mcrtx::perf::Side::Remix, "CreateMaterial.particle");
    return remix_.CreateMaterial(&materialInfo, &materialHandle);
  }();
  if (result != REMIXAPI_ERROR_CODE_SUCCESS) {
    setError("CreateMaterial failed: " + errorCodeToString(result));
    return nullptr;
  }

  particleMaterialHandles_.emplace(textureKind, materialHandle);
  return materialHandle;
}

}  // namespace mcrtx
