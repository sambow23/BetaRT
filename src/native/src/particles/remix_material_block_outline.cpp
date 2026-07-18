// Block-outline material palette and lifecycle.

#include "mcrtx/core/remix_renderer.hpp"
#include "mcrtx/materials/remix_material_common.hpp"
#include "mcrtx/core/remix_render_common.hpp"
#include "mcrtx/lifecycle/perf_log.hpp"

namespace mcrtx {

using namespace mcrtx::detail;
using namespace mcrtx::material;

namespace {

constexpr std::array<remixapi_Float3D, 6> kBlockOutlineRgbPalette {{
  remixapi_Float3D {1.0f, 0.2f, 0.2f},
  remixapi_Float3D {1.0f, 0.7f, 0.15f},
  remixapi_Float3D {0.25f, 1.0f, 0.25f},
  remixapi_Float3D {0.2f, 1.0f, 1.0f},
  remixapi_Float3D {0.3f, 0.45f, 1.0f},
  remixapi_Float3D {1.0f, 0.2f, 1.0f},
}};

}  // namespace

void RemixRenderer::createBlockOutlineMaterials() {
  destroyBlockOutlineMaterials();

  if (terrainAtlasPath_.empty() || remix_.CreateMaterial == nullptr) {
    return;
  }

  const OptionalPbrTextures terrainPbrTextures = resolveOptionalPbrTextures(terrainAtlasPath_);
  const OptionalSssTextures terrainSssTextures = resolveOptionalSssTextures(terrainAtlasPath_);
  const OpaqueSubsurfaceSettings terrainSubsurfaceSettings {
      subsurfaceMeasurementDistance_,
      subsurfaceRadiusScale_,
      subsurfaceMaxSampleRadius_,
      subsurfaceVolumetricAnisotropy_,
      subsurfaceDiffusionProfileEnabled_};

  remixapi_MaterialInfoOpaqueEXT blockOutlineGlowOpaqueInfo {};
  remixapi_MaterialInfoOpaqueSubsurfaceEXT blockOutlineGlowSubsurfaceInfo {};
  blockOutlineGlowOpaqueInfo.sType = REMIXAPI_STRUCT_TYPE_MATERIAL_INFO_OPAQUE_EXT;
  blockOutlineGlowOpaqueInfo.albedoConstant = {1.0f, 1.0f, 1.0f};
  blockOutlineGlowOpaqueInfo.opacityConstant = 1.0f;
  blockOutlineGlowOpaqueInfo.roughnessConstant = 1.0f;
  blockOutlineGlowOpaqueInfo.metallicConstant = 0.0f;
  blockOutlineGlowOpaqueInfo.useDrawCallAlphaState = TRUE;
  blockOutlineGlowOpaqueInfo.alphaTestType = 4;
  blockOutlineGlowOpaqueInfo.alphaReferenceValue = 1;

  remixapi_MaterialInfo blockOutlineGlowMaterialInfo {};
  blockOutlineGlowMaterialInfo.sType = REMIXAPI_STRUCT_TYPE_MATERIAL_INFO;
  blockOutlineGlowMaterialInfo.pNext = &blockOutlineGlowOpaqueInfo;
  blockOutlineGlowMaterialInfo.hash = kBlockOutlineGlowMaterialHash;
  blockOutlineGlowMaterialInfo.albedoTexture = terrainAtlasPath_.c_str();
  blockOutlineGlowMaterialInfo.emissiveIntensity = blockOutlineEmissiveIntensity_;
  blockOutlineGlowMaterialInfo.emissiveColorConstant = {1.0f, 1.0f, 1.0f};
  blockOutlineGlowMaterialInfo.filterMode = 0;
  blockOutlineGlowMaterialInfo.wrapModeU = 1;
  blockOutlineGlowMaterialInfo.wrapModeV = 1;
  applyOptionalOpaqueMaterialTextures(
      blockOutlineGlowMaterialInfo,
      blockOutlineGlowOpaqueInfo,
      blockOutlineGlowSubsurfaceInfo,
      terrainPbrTextures,
      terrainSssTextures,
      displacementFactor_,
      terrainSubsurfaceSettings);

  const remixapi_ErrorCode blockOutlineGlowMaterialResult = [&]() {
    MCRTX_PERF_SCOPE(::mcrtx::perf::Side::Remix, "CreateMaterial.blockOutlineGlow");
    return remix_.CreateMaterial(&blockOutlineGlowMaterialInfo, &blockOutlineGlowMaterialHandle_);
  }();
  if (blockOutlineGlowMaterialResult != REMIXAPI_ERROR_CODE_SUCCESS) {
    setError("CreateMaterial failed: " + errorCodeToString(blockOutlineGlowMaterialResult));
    log("Glow block outline material unavailable; falling back to destroy overlay material");
    blockOutlineGlowMaterialHandle_ = nullptr;
  } else {
    log("Initialized glow block outline material from " + terrainAtlasPath_.string() + " using draw-call alpha state");
  }

  for (std::size_t rgbIndex = 0; rgbIndex < blockOutlineRgbMaterialHandles_.size(); ++rgbIndex) {
    remixapi_MaterialInfoOpaqueEXT blockOutlineRgbOpaqueInfo {};
    remixapi_MaterialInfoOpaqueSubsurfaceEXT blockOutlineRgbSubsurfaceInfo {};
    blockOutlineRgbOpaqueInfo.sType = REMIXAPI_STRUCT_TYPE_MATERIAL_INFO_OPAQUE_EXT;
    blockOutlineRgbOpaqueInfo.albedoConstant = {1.0f, 1.0f, 1.0f};
    blockOutlineRgbOpaqueInfo.opacityConstant = 1.0f;
    blockOutlineRgbOpaqueInfo.roughnessConstant = 1.0f;
    blockOutlineRgbOpaqueInfo.metallicConstant = 0.0f;
    blockOutlineRgbOpaqueInfo.useDrawCallAlphaState = TRUE;
    blockOutlineRgbOpaqueInfo.alphaTestType = 4;
    blockOutlineRgbOpaqueInfo.alphaReferenceValue = 1;

    remixapi_MaterialInfo blockOutlineRgbMaterialInfo {};
    blockOutlineRgbMaterialInfo.sType = REMIXAPI_STRUCT_TYPE_MATERIAL_INFO;
    blockOutlineRgbMaterialInfo.pNext = &blockOutlineRgbOpaqueInfo;
    blockOutlineRgbMaterialInfo.hash = kBlockOutlineRgbMaterialHashSeed | static_cast<std::uint64_t>(rgbIndex);
    blockOutlineRgbMaterialInfo.albedoTexture = terrainAtlasPath_.c_str();
    blockOutlineRgbMaterialInfo.emissiveIntensity = blockOutlineEmissiveIntensity_;
    blockOutlineRgbMaterialInfo.emissiveColorConstant = kBlockOutlineRgbPalette[rgbIndex];
    blockOutlineRgbMaterialInfo.filterMode = 0;
    blockOutlineRgbMaterialInfo.wrapModeU = 1;
    blockOutlineRgbMaterialInfo.wrapModeV = 1;
    applyOptionalOpaqueMaterialTextures(
      blockOutlineRgbMaterialInfo,
      blockOutlineRgbOpaqueInfo,
      blockOutlineRgbSubsurfaceInfo,
      terrainPbrTextures,
      terrainSssTextures,
      displacementFactor_,
      terrainSubsurfaceSettings);

    remixapi_MaterialHandle& rgbMaterialHandle = blockOutlineRgbMaterialHandles_[rgbIndex];
    const remixapi_ErrorCode blockOutlineRgbMaterialResult = [&]() {
      MCRTX_PERF_SCOPE(::mcrtx::perf::Side::Remix, "CreateMaterial.blockOutlineRgb");
      return remix_.CreateMaterial(&blockOutlineRgbMaterialInfo, &rgbMaterialHandle);
    }();
    if (blockOutlineRgbMaterialResult != REMIXAPI_ERROR_CODE_SUCCESS) {
      setError("CreateMaterial failed: " + errorCodeToString(blockOutlineRgbMaterialResult));
      log("RGB block outline material unavailable; falling back to glow block outline material");
      rgbMaterialHandle = nullptr;
    }
  }
  log("Initialized RGB block outline material palette from " + terrainAtlasPath_.string() + " using draw-call alpha state");
}

void RemixRenderer::destroyBlockOutlineMaterials() {
  if (remix_.DestroyMaterial != nullptr && blockOutlineGlowMaterialHandle_ != nullptr) {
    MCRTX_PERF_SCOPE(::mcrtx::perf::Side::Remix, "DestroyMaterial.blockOutlineGlow");
    remix_.DestroyMaterial(blockOutlineGlowMaterialHandle_);
    blockOutlineGlowMaterialHandle_ = nullptr;
  }

  if (remix_.DestroyMaterial != nullptr) {
    for (remixapi_MaterialHandle& materialHandle : blockOutlineRgbMaterialHandles_) {
      if (materialHandle != nullptr) {
        MCRTX_PERF_SCOPE(::mcrtx::perf::Side::Remix, "DestroyMaterial.blockOutlineRgb");
        remix_.DestroyMaterial(materialHandle);
        materialHandle = nullptr;
      }
    }
  }
}
}  // namespace mcrtx
