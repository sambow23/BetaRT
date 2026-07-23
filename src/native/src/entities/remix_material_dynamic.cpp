// Dynamic-entity material policy and cache acquisition.

#include "mcrtx/core/remix_renderer.hpp"
#include "mcrtx/entities/remix_material_dynamic_policy.hpp"
#include "mcrtx/materials/remix_material_common.hpp"
#include "mcrtx/core/remix_render_common.hpp"
#include "mcrtx/lifecycle/perf_log.hpp"

#include <algorithm>
#include <functional>

namespace mcrtx {

using namespace mcrtx::detail;
using namespace mcrtx::material;

namespace {

constexpr std::uint64_t kDynamicEntityTranslucentMaterialHashMask = 0x54524E5300000000ull;
constexpr std::uint64_t kDynamicEntityHurtMaterialHashMask = 0x4852540000000000ull;
constexpr std::uint64_t kDynamicEntityCreeperFuseMaterialHashMask = 0x4655534500000000ull;


constexpr float kDynamicEntityHurtMaxEmissiveIntensity = 0.1f;
constexpr float kDynamicEntitySpiderEyesEmissiveIntensity = 0.5f;
inline constexpr remixapi_Float3D kDynamicEntityHurtEmissiveColor = {1.0f, 0.15f, 0.15f};
inline constexpr remixapi_Float3D kDynamicEntityCreeperFuseEmissiveColor = {1.0f, 0.98f, 0.95f};


std::size_t dynamicEntityMaterialClassIndex(DynamicEntityMaterialClass materialClass) {
  return materialClass == DynamicEntityMaterialClass::Translucent ? 1u : 0u;
}

std::uint32_t clampDynamicEntityHurtStage(std::uint32_t hurtStage) {
  return std::min(hurtStage, kDynamicEntityMaxHurtStage);
}

std::size_t dynamicEntityMaterialVariantIndex(
    DynamicEntityMaterialClass materialClass,
    std::uint32_t hurtStage,
    std::uint32_t creeperFuseStage) {
  const std::size_t hurtIndex = static_cast<std::size_t>(clampDynamicEntityHurtStage(hurtStage));
  const std::size_t creeperFuseIndex = static_cast<std::size_t>(clampDynamicEntityCreeperFuseStage(creeperFuseStage));
  return ((hurtIndex * (static_cast<std::size_t>(kDynamicEntityMaxCreeperFuseStage) + 1u)) + creeperFuseIndex) * 2u
      + dynamicEntityMaterialClassIndex(materialClass);
}

float dynamicEntityHurtEmissiveIntensity(std::uint32_t hurtStage) {
  const std::uint32_t clampedHurtStage = clampDynamicEntityHurtStage(hurtStage);
  if (clampedHurtStage == 0) {
    return 0.0f;
  }

  return kDynamicEntityHurtMaxEmissiveIntensity
      * (static_cast<float>(clampedHurtStage) / static_cast<float>(kDynamicEntityMaxHurtStage));
}

}  // namespace

remixapi_MaterialHandle RemixRenderer::acquireDynamicEntityMaterial(
    const std::string& texturePath,
    DynamicEntityMaterialClass materialClass,
    std::uint32_t hurtStage,
    std::uint32_t creeperFuseStage) {
  MCRTX_PERF_SCOPE(::mcrtx::perf::Side::Native, "RemixRenderer::acquireDynamicEntityMaterial");
  const std::uint32_t clampedHurtStage = clampDynamicEntityHurtStage(hurtStage);
  const std::uint32_t clampedCreeperFuseStage = clampDynamicEntityCreeperFuseStage(creeperFuseStage);
  const std::size_t materialIndex = dynamicEntityMaterialVariantIndex(
      materialClass,
      clampedHurtStage,
      clampedCreeperFuseStage);
  const auto existing = dynamicEntityMaterialHandles_.find(texturePath);
  if (existing != dynamicEntityMaterialHandles_.end()) {
    if (existing->second[materialIndex] != nullptr) {
      return existing->second[materialIndex];
    }
  }

  const std::filesystem::path resolvedTexturePath = resolveDynamicEntityTexturePath(texturePath);
  if (resolvedTexturePath.empty()) {
    return nullptr;
  }

  std::string normalizedTexturePath = texturePath;
  if (!normalizedTexturePath.empty() && normalizedTexturePath.front() == '/') {
    normalizedTexturePath.erase(normalizedTexturePath.begin());
  }

  stripDynamicEntityTextureAliasPrefix(normalizedTexturePath, kFirstPersonShadowTextureAliasPrefix);
  const bool isEntityFireOverlay = stripDynamicEntityTextureAliasPrefix(
      normalizedTexturePath,
      kEntityFireOverlayTextureAliasPrefix);
  const bool isSignText = stripDynamicEntityTextureAliasPrefix(
      normalizedTexturePath,
      kSignTextTextureAliasPrefix);
  const bool isSpiderBody = normalizedTexturePath == "mob/spider.png";

  std::filesystem::path spiderEyesTexturePath;
  const std::filesystem::path* emissiveTexturePath = nullptr;
  const std::filesystem::path* materialTexturePath = &resolvedTexturePath;
  if (normalizedTexturePath == "terrain.png") {
    if (!terrainEmissiveTexturePath_.empty()) {
      emissiveTexturePath = &terrainEmissiveTexturePath_;
    } else if (!redstoneEmissiveTexturePath_.empty()) {
      emissiveTexturePath = &redstoneEmissiveTexturePath_;
    }
  }

  const wchar_t* emissiveTexture = emissiveTexturePath == nullptr ? nullptr : emissiveTexturePath->c_str();
  float emissiveIntensity = emissiveTexture == nullptr ? 0.0f : kTerrainEmissiveIntensity;
  remixapi_Float3D emissiveColor = emissiveTexture == nullptr
      ? remixapi_Float3D {0.0f, 0.0f, 0.0f}
      : kTerrainEmissiveColor;
  if (isEntityFireOverlay) {
    if (!fireTexturePath_.empty()) {
      materialTexturePath = &fireTexturePath_;
    }
    emissiveTexture = materialTexturePath->c_str();
    emissiveIntensity = kFireEmissiveIntensity;
    emissiveColor = kFireEmissiveColor;
  }
  if (isSpiderBody) {
    spiderEyesTexturePath = resolveDynamicEntityTexturePath("/mob/spider_eyes.png");
    if (!spiderEyesTexturePath.empty()) {
      emissiveTexture = spiderEyesTexturePath.c_str();
      emissiveIntensity = kDynamicEntitySpiderEyesEmissiveIntensity;
      emissiveColor = {1.0f, 1.0f, 1.0f};
    }
  }
  if (clampedHurtStage != 0) {
    emissiveTexture = nullptr;
    emissiveIntensity = dynamicEntityHurtEmissiveIntensity(clampedHurtStage);
    emissiveColor = kDynamicEntityHurtEmissiveColor;
  } else if (isDynamicEntityCreeperFuseFlashStage(clampedCreeperFuseStage)) {
    emissiveTexture = nullptr;
    emissiveIntensity = dynamicEntityCreeperFuseEmissiveIntensity(clampedCreeperFuseStage);
    emissiveColor = kDynamicEntityCreeperFuseEmissiveColor;
  }
  const OptionalPbrTextures pbrTextures = resolveOptionalPbrTextures(*materialTexturePath);
  const OptionalSssTextures sssTextures = resolveOptionalSssTextures(*materialTexturePath);
  const OpaqueSubsurfaceSettings subsurfaceSettings {
      subsurfaceMeasurementDistance_,
      subsurfaceRadiusScale_,
      subsurfaceMaxSampleRadius_,
      subsurfaceVolumetricAnisotropy_,
      subsurfaceDiffusionProfileEnabled_};

  remixapi_MaterialInfoOpaqueEXT opaqueInfo {};
  remixapi_MaterialInfoOpaqueSubsurfaceEXT subsurfaceInfo {};
  remixapi_MaterialInfoTranslucentEXT translucentInfo {};

  remixapi_MaterialInfo materialInfo {};
  materialInfo.sType = REMIXAPI_STRUCT_TYPE_MATERIAL_INFO;
  materialInfo.hash = kDynamicEntityMaterialHashSeed
      ^ static_cast<std::uint64_t>(std::hash<std::string> {}(texturePath))
      ^ (materialClass == DynamicEntityMaterialClass::Translucent ? kDynamicEntityTranslucentMaterialHashMask : 0ull)
      ^ (static_cast<std::uint64_t>(clampedHurtStage) * kDynamicEntityHurtMaterialHashMask)
      ^ (static_cast<std::uint64_t>(clampedCreeperFuseStage) * kDynamicEntityCreeperFuseMaterialHashMask);
  materialInfo.albedoTexture = materialTexturePath->c_str();
  materialInfo.emissiveTexture = emissiveTexture;
  materialInfo.emissiveIntensity = emissiveIntensity;
  materialInfo.emissiveColorConstant = emissiveColor;
  materialInfo.filterMode = 0;
  materialInfo.wrapModeU = 1;
  materialInfo.wrapModeV = 1;

  if (materialClass == DynamicEntityMaterialClass::Translucent) {
    translucentInfo.sType = REMIXAPI_STRUCT_TYPE_MATERIAL_INFO_TRANSLUCENT_EXT;
    translucentInfo.refractiveIndex = 1.0f;
    translucentInfo.transmittanceColor = {1.0f, 1.0f, 1.0f};
    translucentInfo.transmittanceMeasurementDistance = 1.0f;
    translucentInfo.thinWallThickness_hasvalue = FALSE;
    translucentInfo.useDiffuseLayer = TRUE;
    translucentInfo.diffuseLayerOpacity = 1.0f;
    translucentInfo.transmittanceTexture = materialTexturePath->c_str();
    materialInfo.pNext = &translucentInfo;
  } else {
    opaqueInfo.sType = REMIXAPI_STRUCT_TYPE_MATERIAL_INFO_OPAQUE_EXT;
    opaqueInfo.albedoConstant = {1.0f, 1.0f, 1.0f};
    opaqueInfo.opacityConstant = 1.0f;
    opaqueInfo.roughnessConstant = 1.0f;
    opaqueInfo.metallicConstant = 0.0f;
    opaqueInfo.useDrawCallAlphaState = FALSE;
    opaqueInfo.alphaTestType = 4;
    opaqueInfo.alphaReferenceValue = isSignText ? 64 : 1;
    materialInfo.pNext = &opaqueInfo;
    applyOptionalOpaqueMaterialTextures(
      materialInfo,
      opaqueInfo,
      subsurfaceInfo,
      pbrTextures,
      sssTextures,
      displacementFactor_,
      subsurfaceSettings);
  }

  remixapi_MaterialHandle materialHandle = nullptr;
  const remixapi_ErrorCode result = [&]() {
    MCRTX_PERF_SCOPE(::mcrtx::perf::Side::Remix, "CreateMaterial.entity");
    return remix_.CreateMaterial(&materialInfo, &materialHandle);
  }();
  if (result != REMIXAPI_ERROR_CODE_SUCCESS) {
    setError("CreateMaterial failed: " + errorCodeToString(result));
    return nullptr;
  }

  dynamicEntityMaterialHandles_[texturePath][materialIndex] = materialHandle;
  return materialHandle;
}
}  // namespace mcrtx
