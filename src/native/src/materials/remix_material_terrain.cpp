// Terrain-family material initialization and destruction.

#include "mcrtx/core/remix_renderer.hpp"
#include "mcrtx/materials/remix_material_common.hpp"
#include "mcrtx/core/remix_render_common.hpp"
#include "mcrtx/lifecycle/perf_log.hpp"

namespace mcrtx {

using namespace mcrtx::detail;
using namespace mcrtx::material;

bool RemixRenderer::initializeTerrainMaterials() {
  MCRTX_PERF_SCOPE(::mcrtx::perf::Side::Native, "RemixRenderer::initializeTerrainMaterials");
  destroyTerrainMaterials();
  terrainAtlasPath_ = resolveTerrainAtlasPath();
  const OptionalPbrTextures terrainPbrTextures = resolveOptionalPbrTextures(terrainAtlasPath_);
  const OptionalSssTextures terrainSssTextures = resolveOptionalSssTextures(terrainAtlasPath_);
  terrainEmissiveTexturePath_ = resolveTerrainEmissiveTexturePath();
  cloudTexturePath_ = resolveCloudTexturePath();
  fireTexturePath_ = resolveFireTexturePath();
  waterTexturePath_ = resolveWaterTexturePath();
  const OptionalPbrTextures waterPbrTextures = resolveOptionalPbrTextures(waterTexturePath_);
  lavaTexturePath_ = resolveLavaTexturePath();
  const OptionalPbrTextures lavaPbrTextures = resolveOptionalPbrTextures(lavaTexturePath_);
  const OptionalSssTextures lavaSssTextures = resolveOptionalSssTextures(lavaTexturePath_);
  portalTexturePath_ = resolvePortalTexturePath();
  lavaEmissiveTexturePath_ = resolveLavaEmissiveTexturePath();
  redstoneEmissiveTexturePath_ = resolveRedstoneEmissiveTexturePath();
  const OptionalPbrTextures firePbrTextures = resolveOptionalPbrTextures(fireTexturePath_);
  const OptionalSssTextures fireSssTextures = resolveOptionalSssTextures(fireTexturePath_);
  const OptionalPbrTextures cloudPbrTextures = resolveOptionalPbrTextures(cloudTexturePath_);
  const OptionalSssTextures cloudSssTextures = resolveOptionalSssTextures(cloudTexturePath_);
  if (terrainAtlasPath_.empty()) {
    log("Terrain atlas asset not found; continuing without Remix materials");
    return false;
  }

  const OpaqueSubsurfaceSettings terrainSubsurfaceSettings {
      subsurfaceMeasurementDistance_,
      subsurfaceRadiusScale_,
      subsurfaceMaxSampleRadius_,
      subsurfaceVolumetricAnisotropy_,
      subsurfaceDiffusionProfileEnabled_};

  log(
      std::string("Terrain atlas selected: ") + terrainAtlasPath_.string()
      + " sourcePreference=" + (prefersDdsTerrainAtlas() ? std::string("dds") : std::string("png")));

  const auto createTerrainMaterial = [this, &terrainSubsurfaceSettings](
                                       std::size_t materialClass,
                                       bool cutout,
                                       bool useDrawCallAlphaState,
                                       bool isTranslucent,
                                       remixapi_Float3D transmittanceColor,
                                       float transmittanceMeasurementDistance,
                                       float refractiveIndex,
                                       const std::filesystem::path& texturePath,
                                       std::uint64_t materialHash,
                                       const wchar_t* emissiveTexturePath,
                                       float emissiveIntensity,
                                       remixapi_Float3D emissiveColor,
                                       std::uint8_t spriteSheetColumns,
                                       std::uint8_t spriteSheetRows,
                                       std::uint8_t spriteSheetFps,
                                       const OptionalPbrTextures& pbrTextures,
                                       const OptionalSssTextures& sssTextures) {
    remixapi_MaterialInfoOpaqueEXT opaqueInfo {};
    remixapi_MaterialInfoOpaqueSubsurfaceEXT subsurfaceInfo {};
    remixapi_MaterialInfoTranslucentEXT translucentInfo {};
    void* pNext = nullptr;

    if (isTranslucent) {
      const bool isWaterMaterial = materialHash == kWaterTerrainMaterialHash;
      const bool useThinWalledTranslucency = isWaterMaterial && waterThinWalledEnabled_;
      translucentInfo.sType = REMIXAPI_STRUCT_TYPE_MATERIAL_INFO_TRANSLUCENT_EXT;
      translucentInfo.refractiveIndex = refractiveIndex;
      translucentInfo.transmittanceColor = transmittanceColor;
      translucentInfo.transmittanceMeasurementDistance = transmittanceMeasurementDistance;
      translucentInfo.thinWallThickness_hasvalue = useThinWalledTranslucency ? TRUE : FALSE;
      translucentInfo.thinWallThickness_value = useThinWalledTranslucency ? waterMaterialThickness_ : kWaterThinWallThickness;
      translucentInfo.useDiffuseLayer = isWaterMaterial ? waterDiffuseLayerEnabled_ : TRUE;
      translucentInfo.transmittanceTexture = texturePath.c_str();
      pNext = &translucentInfo;
    } else {
      opaqueInfo.sType = REMIXAPI_STRUCT_TYPE_MATERIAL_INFO_OPAQUE_EXT;
      opaqueInfo.albedoConstant = {1.0f, 1.0f, 1.0f};
      opaqueInfo.opacityConstant = 1.0f;
      opaqueInfo.roughnessConstant = 1.0f;
      opaqueInfo.metallicConstant = 0.0f;
      opaqueInfo.useDrawCallAlphaState = useDrawCallAlphaState ? TRUE : FALSE;
      opaqueInfo.alphaTestType = cutout ? 4 : 7;
      opaqueInfo.alphaReferenceValue = cutout ? 1 : 0;
      pNext = &opaqueInfo;
    }

    remixapi_MaterialInfo materialInfo {};
    materialInfo.sType = REMIXAPI_STRUCT_TYPE_MATERIAL_INFO;
    materialInfo.pNext = pNext;
    materialInfo.hash = materialHash;
    materialInfo.albedoTexture = texturePath.c_str();
    materialInfo.emissiveTexture = emissiveTexturePath;
    materialInfo.emissiveIntensity = emissiveIntensity;
    materialInfo.emissiveColorConstant = emissiveColor;
    materialInfo.spriteSheetCol = spriteSheetColumns;
    materialInfo.spriteSheetRow = spriteSheetRows;
    materialInfo.spriteSheetFps = spriteSheetFps;
    materialInfo.filterMode = 0;
    materialInfo.wrapModeU = 1;
    materialInfo.wrapModeV = 1;
    materialInfo.normalTexture = optionalTexturePath(pbrTextures.normal);
    if (!isTranslucent) {
      applyOptionalOpaqueMaterialTextures(
          materialInfo,
          opaqueInfo,
          subsurfaceInfo,
          pbrTextures,
          sssTextures,
          displacementFactor_,
          terrainSubsurfaceSettings);
    }

    remixapi_MaterialHandle materialHandle = nullptr;
    const remixapi_ErrorCode result = [&]() {
      MCRTX_PERF_SCOPE(::mcrtx::perf::Side::Remix, "CreateMaterial.terrain");
      return remix_.CreateMaterial(&materialInfo, &materialHandle);
    }();
    if (result != REMIXAPI_ERROR_CODE_SUCCESS) {
      setError("CreateMaterial failed: " + errorCodeToString(result));
      return false;
    }

    terrainMaterialHandles_[materialClass] = materialHandle;
    if (cutout && !isTranslucent) {
      log(
          std::string("Initialized cutout terrain material from ") + texturePath.string()
          + " alphaTestType=" + std::to_string(opaqueInfo.alphaTestType)
          + " alphaReferenceValue=" + std::to_string(opaqueInfo.alphaReferenceValue)
          + " useDrawCallAlphaState=" + (opaqueInfo.useDrawCallAlphaState == TRUE ? std::string("true") : std::string("false")));
    }
    return true;
  };

  const wchar_t* terrainEmissiveTexture = terrainEmissiveTexturePath_.empty()
      ? nullptr
      : terrainEmissiveTexturePath_.c_str();
  const float terrainEmissiveIntensity = terrainEmissiveTexturePath_.empty()
      ? 0.0f
      : kTerrainEmissiveIntensity;
  const remixapi_Float3D terrainEmissiveColor = terrainEmissiveTexturePath_.empty()
      ? remixapi_Float3D {0.0f, 0.0f, 0.0f}
      : kTerrainEmissiveColor;

  const bool opaqueCreated = createTerrainMaterial(
      kOpaqueTerrainMaterialClass,
      false,
      false,
      false,
      {1.0f, 1.0f, 1.0f},
      1.0f,
      1.0f,
      terrainAtlasPath_,
      kOpaqueTerrainMaterialHash,
      terrainEmissiveTexture,
      terrainEmissiveIntensity,
      terrainEmissiveColor,
      0,
      0,
      0,
      terrainPbrTextures,
      terrainSssTextures);
  const bool cutoutCreated = createTerrainMaterial(
      kCutoutTerrainMaterialClass,
      true,
      false,
      false,
      {1.0f, 1.0f, 1.0f},
      1.0f,
      1.0f,
      terrainAtlasPath_,
      kCutoutTerrainMaterialHash,
      terrainEmissiveTexture,
      terrainEmissiveIntensity,
      terrainEmissiveColor,
      0,
      0,
      0,
      terrainPbrTextures,
      terrainSssTextures);
  remixapi_MaterialInfoOpaqueEXT destroyOverlayOpaqueInfo {};
      remixapi_MaterialInfoOpaqueSubsurfaceEXT destroyOverlaySubsurfaceInfo {};
  destroyOverlayOpaqueInfo.sType = REMIXAPI_STRUCT_TYPE_MATERIAL_INFO_OPAQUE_EXT;
  destroyOverlayOpaqueInfo.albedoConstant = {1.0f, 1.0f, 1.0f};
  destroyOverlayOpaqueInfo.opacityConstant = 1.0f;
  destroyOverlayOpaqueInfo.roughnessConstant = 1.0f;
  destroyOverlayOpaqueInfo.metallicConstant = 0.0f;
  destroyOverlayOpaqueInfo.useDrawCallAlphaState = TRUE;
  destroyOverlayOpaqueInfo.alphaTestType = 4;
  destroyOverlayOpaqueInfo.alphaReferenceValue = 1;

  remixapi_MaterialInfo destroyOverlayMaterialInfo {};
  destroyOverlayMaterialInfo.sType = REMIXAPI_STRUCT_TYPE_MATERIAL_INFO;
  destroyOverlayMaterialInfo.pNext = &destroyOverlayOpaqueInfo;
  destroyOverlayMaterialInfo.hash = kDestroyOverlayMaterialHash;
  destroyOverlayMaterialInfo.albedoTexture = terrainAtlasPath_.c_str();
  destroyOverlayMaterialInfo.emissiveTexture = terrainEmissiveTexture;
  destroyOverlayMaterialInfo.emissiveIntensity = terrainEmissiveIntensity;
  destroyOverlayMaterialInfo.emissiveColorConstant = terrainEmissiveColor;
  destroyOverlayMaterialInfo.filterMode = 0;
  destroyOverlayMaterialInfo.wrapModeU = 1;
  destroyOverlayMaterialInfo.wrapModeV = 1;
  applyOptionalOpaqueMaterialTextures(
      destroyOverlayMaterialInfo,
      destroyOverlayOpaqueInfo,
      destroyOverlaySubsurfaceInfo,
      terrainPbrTextures,
      terrainSssTextures,
      displacementFactor_,
      terrainSubsurfaceSettings);

  const remixapi_ErrorCode destroyOverlayMaterialResult = [&]() {
    MCRTX_PERF_SCOPE(::mcrtx::perf::Side::Remix, "CreateMaterial.destroyOverlay");
    return remix_.CreateMaterial(&destroyOverlayMaterialInfo, &destroyOverlayMaterialHandle_);
  }();
  if (destroyOverlayMaterialResult != REMIXAPI_ERROR_CODE_SUCCESS) {
    setError("CreateMaterial failed: " + errorCodeToString(destroyOverlayMaterialResult));
    log("Destroy overlay material unavailable; falling back to cutout terrain material");
    destroyOverlayMaterialHandle_ = nullptr;
  } else {
    log("Initialized destroy overlay material from " + terrainAtlasPath_.string() + " using draw-call alpha state");
  }

  createBlockOutlineMaterials();

      const bool poweredRedstoneCreated = createTerrainMaterial(
        kPoweredRedstoneTerrainMaterialClass,
        true,
        false,
        false,
        {1.0f, 1.0f, 1.0f},
        1.0f,
        1.0f,
        terrainAtlasPath_,
        kPoweredRedstoneTerrainMaterialHash,
        redstoneEmissiveTexturePath_.empty() ? nullptr : redstoneEmissiveTexturePath_.c_str(),
        redstoneEmissiveTexturePath_.empty() ? 0.0f : kTerrainEmissiveIntensity,
        redstoneEmissiveTexturePath_.empty() ? remixapi_Float3D {0.0f, 0.0f, 0.0f} : kTerrainEmissiveColor,
        0,
        0,
        0,
        terrainPbrTextures,
        terrainSssTextures);
  const bool waterCreated = !waterTexturePath_.empty() && createTerrainMaterial(
      kWaterTerrainMaterialClass,
      false,
      false,
      true,
      waterTransmittanceColor_,
      waterTransmittanceDistance_,
      waterRefractiveIndex_,
      waterTexturePath_,
      kWaterTerrainMaterialHash,
      nullptr,
      0.0f,
      {0.0f, 0.0f, 0.0f},
      kLiquidAnimationFrameCount,
      1,
      kLiquidAnimationFramesPerSecond,
        waterPbrTextures,
        {});
  const bool lavaCreated = !lavaTexturePath_.empty() && createTerrainMaterial(
      kLavaTerrainMaterialClass,
      false,
      false,
      false,
      {1.0f, 1.0f, 1.0f},
      1.0f,
      1.0f,
      lavaTexturePath_,
      kLavaTerrainMaterialHash,
      lavaEmissiveTexturePath_.empty() ? nullptr : lavaEmissiveTexturePath_.c_str(),
      kLavaEmissiveIntensity,
      kLavaEmissiveColor,
      kLiquidAnimationFrameCount,
      1,
      kLiquidAnimationFramesPerSecond,
      lavaPbrTextures,
      lavaSssTextures);
      const bool iceCreated = createTerrainMaterial(
        kIceTerrainMaterialClass,
        false,
        false,
        true,
        kIceTransmittanceColor,
        kIceTransmittanceDistance,
        kIceRefractiveIndex,
        terrainAtlasPath_,
        kIceTerrainMaterialHash,
        nullptr,
        0.0f,
        {0.0f, 0.0f, 0.0f},
        0,
        0,
        0,
        {},
        {});
  const bool portalCreated = !portalTexturePath_.empty() && createTerrainMaterial(
      kPortalTerrainMaterialClass,
      false,
      false,
      true,
      kPortalTransmittanceColor,
      kPortalTransmittanceDistance,
      kPortalRefractiveIndex,
      portalTexturePath_,
      kPortalTerrainMaterialHash,
      portalTexturePath_.c_str(),
      kPortalEmissiveIntensity,
      kPortalEmissiveColor,
      kPortalAnimationFrameCount,
      1,
      kPortalAnimationFramesPerSecond,
      {},
      {});
  if (opaqueCreated) {
    log("Initialized terrain atlas materials from " + terrainAtlasPath_.string());
  }
  if (!terrainPbrTextures.normal.empty()) {
    log("Terrain normal map loaded from " + terrainPbrTextures.normal.string());
  }
  if (!terrainPbrTextures.roughness.empty()) {
    log("Terrain roughness map loaded from " + terrainPbrTextures.roughness.string());
  }
  if (!terrainPbrTextures.metallic.empty()) {
    log("Terrain metallic map loaded from " + terrainPbrTextures.metallic.string());
  }
  if (!terrainPbrTextures.height.empty()) {
    log("Terrain height map loaded from " + terrainPbrTextures.height.string());
  }
  if (!terrainSssTextures.transmittance.empty()) {
    log("Terrain transmittance map loaded from " + terrainSssTextures.transmittance.string());
  }
  if (!terrainSssTextures.thickness.empty()) {
    log("Terrain thickness map loaded from " + terrainSssTextures.thickness.string());
  }
  if (!terrainSssTextures.singleScatteringAlbedo.empty()) {
    log("Terrain single-scattering albedo map loaded from " + terrainSssTextures.singleScatteringAlbedo.string());
  }
  if (!terrainSssTextures.radius.empty()) {
    log("Terrain subsurface radius map loaded from " + terrainSssTextures.radius.string());
  }
  if (!terrainEmissiveTexturePath_.empty()) {
    log("Terrain emissive map loaded from " + terrainEmissiveTexturePath_.string());
  }
  if (!cutoutCreated) {
    log("Cutout terrain material unavailable; cutout faces will use fallback material");
  }
  if (!redstoneEmissiveTexturePath_.empty()) {
    log("Redstone emissive map loaded from " + redstoneEmissiveTexturePath_.string());
  }
  if (!poweredRedstoneCreated) {
    log("Powered redstone material unavailable; powered dust will fall back to cutout terrain");
  }
  if (waterTexturePath_.empty()) {
    log("Water texture asset not found; water faces will be skipped");
  }
  if (!waterCreated) {
    log("Water terrain material unavailable; water faces will be skipped");
  }
  if (lavaTexturePath_.empty()) {
    log("Lava texture asset not found; lava faces will be skipped");
  }
  if (lavaEmissiveTexturePath_.empty()) {
    log("Lava emissive texture asset not found; lava will fall back to uniform emissive");
  }
  if (!lavaCreated) {
    log("Lava terrain material unavailable; lava faces will be skipped");
  }
  if (!iceCreated) {
    log("Ice terrain material unavailable; ice will fall back to opaque terrain");
    terrainMaterialHandles_[kIceTerrainMaterialClass] = terrainMaterialHandles_[kOpaqueTerrainMaterialClass];
  } else {
    log("Initialized ice material from " + terrainAtlasPath_.string());
  }
  if (portalTexturePath_.empty()) {
    log("Portal texture asset not found; portals will fall back to cutout terrain");
    terrainMaterialHandles_[kPortalTerrainMaterialClass] = terrainMaterialHandles_[kCutoutTerrainMaterialClass];
  } else if (!portalCreated) {
    log("Portal material unavailable; portals will fall back to cutout terrain");
    terrainMaterialHandles_[kPortalTerrainMaterialClass] = terrainMaterialHandles_[kCutoutTerrainMaterialClass];
  } else {
    log("Portal emissive map loaded from " + portalTexturePath_.string());
    log("Initialized portal material from " + portalTexturePath_.string());
  }

  if (fireTexturePath_.empty()) {
    log("Fire texture asset not found; animated fire will be skipped");
  } else {
    remixapi_MaterialInfoOpaqueEXT fireOpaqueInfo {};
    remixapi_MaterialInfoOpaqueSubsurfaceEXT fireSubsurfaceInfo {};
    fireOpaqueInfo.sType = REMIXAPI_STRUCT_TYPE_MATERIAL_INFO_OPAQUE_EXT;
    fireOpaqueInfo.albedoConstant = {1.0f, 1.0f, 1.0f};
    fireOpaqueInfo.opacityConstant = 1.0f;
    fireOpaqueInfo.roughnessConstant = 1.0f;
    fireOpaqueInfo.metallicConstant = 0.0f;
    fireOpaqueInfo.useDrawCallAlphaState = FALSE;
    fireOpaqueInfo.alphaTestType = 4;
    fireOpaqueInfo.alphaReferenceValue = 1;

    remixapi_MaterialInfo fireMaterialInfo {};
    fireMaterialInfo.sType = REMIXAPI_STRUCT_TYPE_MATERIAL_INFO;
    fireMaterialInfo.pNext = &fireOpaqueInfo;
    fireMaterialInfo.hash = kFireMaterialHash;
    fireMaterialInfo.albedoTexture = fireTexturePath_.c_str();
    fireMaterialInfo.emissiveTexture = fireTexturePath_.c_str();
    fireMaterialInfo.emissiveIntensity = kFireEmissiveIntensity;
    fireMaterialInfo.emissiveColorConstant = kFireEmissiveColor;
    fireMaterialInfo.filterMode = 0;
    fireMaterialInfo.wrapModeU = 1;
    fireMaterialInfo.wrapModeV = 1;
    applyOptionalOpaqueMaterialTextures(
      fireMaterialInfo,
      fireOpaqueInfo,
      fireSubsurfaceInfo,
      firePbrTextures,
      fireSssTextures,
      displacementFactor_,
      terrainSubsurfaceSettings);

    const remixapi_ErrorCode fireResult = [&]() {
      MCRTX_PERF_SCOPE(::mcrtx::perf::Side::Remix, "CreateMaterial.fire");
      return remix_.CreateMaterial(&fireMaterialInfo, &fireMaterialHandle_);
    }();
    if (fireResult != REMIXAPI_ERROR_CODE_SUCCESS) {
      setError("CreateMaterial failed: " + errorCodeToString(fireResult));
      fireMaterialHandle_ = nullptr;
    } else {
      log("Initialized animated fire material from " + fireTexturePath_.string());
    }
  }

  if (cloudTexturePath_.empty()) {
    log("Cloud texture asset not found; cloud layer will be skipped");
    return opaqueCreated;
  }

  remixapi_MaterialInfoOpaqueEXT cloudOpaqueInfo {};
  remixapi_MaterialInfoOpaqueSubsurfaceEXT cloudSubsurfaceInfo {};
  cloudOpaqueInfo.sType = REMIXAPI_STRUCT_TYPE_MATERIAL_INFO_OPAQUE_EXT;
  cloudOpaqueInfo.albedoConstant = {1.0f, 1.0f, 1.0f};
  cloudOpaqueInfo.opacityConstant = 1.0f;
  cloudOpaqueInfo.roughnessConstant = 1.0f;
  cloudOpaqueInfo.metallicConstant = 0.0f;
  cloudOpaqueInfo.useDrawCallAlphaState = FALSE;
  cloudOpaqueInfo.alphaTestType = 4;
  cloudOpaqueInfo.alphaReferenceValue = 2;

  remixapi_MaterialInfo cloudMaterialInfo {};
  cloudMaterialInfo.sType = REMIXAPI_STRUCT_TYPE_MATERIAL_INFO;
  cloudMaterialInfo.pNext = &cloudOpaqueInfo;
  cloudMaterialInfo.hash = kCloudMaterialHash;
  cloudMaterialInfo.albedoTexture = cloudTexturePath_.c_str();
  cloudMaterialInfo.emissiveIntensity = 0.0f;
  cloudMaterialInfo.emissiveColorConstant = {0.0f, 0.0f, 0.0f};
  cloudMaterialInfo.filterMode = 0;
  cloudMaterialInfo.wrapModeU = 1;
  cloudMaterialInfo.wrapModeV = 1;
  applyOptionalOpaqueMaterialTextures(
      cloudMaterialInfo,
      cloudOpaqueInfo,
      cloudSubsurfaceInfo,
      cloudPbrTextures,
      cloudSssTextures,
      displacementFactor_,
      terrainSubsurfaceSettings);

  const remixapi_ErrorCode cloudResult = [&]() {
    MCRTX_PERF_SCOPE(::mcrtx::perf::Side::Remix, "CreateMaterial.cloud");
    return remix_.CreateMaterial(&cloudMaterialInfo, &cloudMaterialHandle_);
  }();
  if (cloudResult != REMIXAPI_ERROR_CODE_SUCCESS) {
    setError("CreateMaterial failed: " + errorCodeToString(cloudResult));
    log("Cloud material unavailable; cloud layer will be skipped");
    cloudMaterialHandle_ = nullptr;
  } else {
    log("Initialized cloud material from " + cloudTexturePath_.string());
  }
  return opaqueCreated;
}

void RemixRenderer::destroyTerrainMaterials() {
  MCRTX_PERF_SCOPE(::mcrtx::perf::Side::Native, "RemixRenderer::destroyTerrainMaterials");
  destroyCloudMesh();
  destroyDestroyOverlayMesh();
  destroyParticleMesh();
  destroyDynamicEntityMeshes();

  if (remix_.DestroyMaterial != nullptr) {
    for (auto& [texturePath, materialHandles] : dynamicEntityMaterialHandles_) {
      (void)texturePath;
      for (remixapi_MaterialHandle& materialHandle : materialHandles) {
        if (materialHandle != nullptr) {
          MCRTX_PERF_SCOPE(::mcrtx::perf::Side::Remix, "DestroyMaterial.entity");
          remix_.DestroyMaterial(materialHandle);
        }
      }
    }
    for (auto& [textureKind, materialHandle] : particleMaterialHandles_) {
      (void)textureKind;
      if (materialHandle != nullptr) {
        MCRTX_PERF_SCOPE(::mcrtx::perf::Side::Remix, "DestroyMaterial.particle");
        remix_.DestroyMaterial(materialHandle);
      }
    }
  }
  dynamicEntityMaterialHandles_.clear();
  particleMaterialHandles_.clear();

  if (remix_.DestroyMaterial != nullptr && fireMaterialHandle_ != nullptr) {
    MCRTX_PERF_SCOPE(::mcrtx::perf::Side::Remix, "DestroyMaterial.fire");
    remix_.DestroyMaterial(fireMaterialHandle_);
    fireMaterialHandle_ = nullptr;
  }

  destroyBlockOutlineMaterials();

  if (remix_.DestroyMaterial != nullptr && cloudMaterialHandle_ != nullptr) {
    MCRTX_PERF_SCOPE(::mcrtx::perf::Side::Remix, "DestroyMaterial.cloud");
    remix_.DestroyMaterial(cloudMaterialHandle_);
    cloudMaterialHandle_ = nullptr;
  }

  if (remix_.DestroyMaterial != nullptr && destroyOverlayMaterialHandle_ != nullptr) {
    MCRTX_PERF_SCOPE(::mcrtx::perf::Side::Remix, "DestroyMaterial.destroyOverlay");
    remix_.DestroyMaterial(destroyOverlayMaterialHandle_);
    destroyOverlayMaterialHandle_ = nullptr;
  }

  if (remix_.DestroyMaterial == nullptr) {
    terrainMaterialHandles_ = {};
    return;
  }

  for (remixapi_MaterialHandle& materialHandle : terrainMaterialHandles_) {
    if (materialHandle != nullptr) {
      MCRTX_PERF_SCOPE(::mcrtx::perf::Side::Remix, "DestroyMaterial.terrain");
      remix_.DestroyMaterial(materialHandle);
      materialHandle = nullptr;
    }
  }

  fireTexturePath_.clear();
  waterTexturePath_.clear();
  lavaTexturePath_.clear();
  portalTexturePath_.clear();
  lavaEmissiveTexturePath_.clear();
  terrainEmissiveTexturePath_.clear();
  redstoneEmissiveTexturePath_.clear();
}
}  // namespace mcrtx
