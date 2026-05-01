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
std::filesystem::path RemixRenderer::resolveRemixDllPath() {
  std::vector<std::filesystem::path> attemptedPaths;

  const std::string explicitRemixDllPath = readEnvironmentVariable("MCRTX_REMIX_DLL");
  if (!explicitRemixDllPath.empty()) {
    std::filesystem::path envPath(explicitRemixDllPath);
    attemptedPaths.push_back(envPath);
    if (std::filesystem::exists(envPath)) {
      log("Using Remix runtime from MCRTX_REMIX_DLL: " + envPath.string());
      return envPath;
    }
  }

  const std::filesystem::path moduleDirectory = getCurrentModuleDirectory();
  if (!moduleDirectory.empty()) {
    attemptedPaths.push_back(moduleDirectory / "d3d9.dll");
    attemptedPaths.push_back(moduleDirectory / "bin" / "d3d9.dll");
  }

  attemptedPaths.push_back(std::filesystem::path(L"d3d9.dll"));
  attemptedPaths.push_back(std::filesystem::path(L"bin") / "d3d9.dll");

  for (const auto& path : attemptedPaths) {
    if (std::filesystem::exists(path)) {
      log("Using Remix runtime DLL: " + path.string());
      return path;
    }
  }

  std::ostringstream stream;
  stream << "Could not find Remix runtime d3d9.dll. Tried:";
  for (const auto& path : attemptedPaths) {
    stream << " " << path.string();
  }
  log(stream.str());

  return std::filesystem::path(L"d3d9.dll");
}

std::filesystem::path RemixRenderer::resolveTerrainAtlasPath() {
  std::vector<std::filesystem::path> attemptedPaths;

  const std::filesystem::path moduleDirectory = getCurrentModuleDirectory();
  if (!moduleDirectory.empty()) {
    attemptedPaths.push_back(moduleDirectory / L"mcrtx_assets" / L"terrain.dds");
    attemptedPaths.push_back(moduleDirectory / L"mcrtx_assets" / L"terrain.png");
    attemptedPaths.push_back(moduleDirectory / L"terrain.dds");
    attemptedPaths.push_back(moduleDirectory / L"terrain.png");
  }

  attemptedPaths.push_back(std::filesystem::current_path() / L"mcrtx_assets" / L"terrain.dds");
  attemptedPaths.push_back(std::filesystem::current_path() / L"mcrtx_assets" / L"terrain.png");
  attemptedPaths.push_back(std::filesystem::current_path() / L"terrain.dds");
  attemptedPaths.push_back(std::filesystem::current_path() / L"terrain.png");

  for (const auto& path : attemptedPaths) {
    if (std::filesystem::exists(path)) {
      return path;
    }
  }

  return {};
}

std::filesystem::path RemixRenderer::resolveTerrainEmissiveTexturePath() {
  std::vector<std::filesystem::path> attemptedPaths;

  const std::filesystem::path moduleDirectory = getCurrentModuleDirectory();
  if (!moduleDirectory.empty()) {
    attemptedPaths.push_back(moduleDirectory / L"mcrtx_assets" / L"terrain_emissive.dds");
    attemptedPaths.push_back(moduleDirectory / L"mcrtx_assets" / L"terrain_emissive.png");
    attemptedPaths.push_back(moduleDirectory / L"terrain_emissive.dds");
    attemptedPaths.push_back(moduleDirectory / L"terrain_emissive.png");
  }

  attemptedPaths.push_back(std::filesystem::current_path() / L"mcrtx_assets" / L"terrain_emissive.dds");
  attemptedPaths.push_back(std::filesystem::current_path() / L"mcrtx_assets" / L"terrain_emissive.png");
  attemptedPaths.push_back(std::filesystem::current_path() / L"terrain_emissive.dds");
  attemptedPaths.push_back(std::filesystem::current_path() / L"terrain_emissive.png");

  for (const auto& path : attemptedPaths) {
    if (std::filesystem::exists(path)) {
      return path;
    }
  }

  return {};
}

std::filesystem::path RemixRenderer::resolveRedstoneEmissiveTexturePath() {
  std::vector<std::filesystem::path> attemptedPaths;

  const std::filesystem::path moduleDirectory = getCurrentModuleDirectory();
  if (!moduleDirectory.empty()) {
    attemptedPaths.push_back(moduleDirectory / L"mcrtx_assets" / L"redstone_emissive.dds");
    attemptedPaths.push_back(moduleDirectory / L"mcrtx_assets" / L"redstone_emissive.png");
    attemptedPaths.push_back(moduleDirectory / L"redstone_emissive.dds");
    attemptedPaths.push_back(moduleDirectory / L"redstone_emissive.png");
  }

  attemptedPaths.push_back(std::filesystem::current_path() / L"mcrtx_assets" / L"redstone_emissive.dds");
  attemptedPaths.push_back(std::filesystem::current_path() / L"mcrtx_assets" / L"redstone_emissive.png");
  attemptedPaths.push_back(std::filesystem::current_path() / L"redstone_emissive.dds");
  attemptedPaths.push_back(std::filesystem::current_path() / L"redstone_emissive.png");

  for (const auto& path : attemptedPaths) {
    if (std::filesystem::exists(path)) {
      return path;
    }
  }

  return {};
}

std::filesystem::path RemixRenderer::resolveCloudTexturePath() {
  std::vector<std::filesystem::path> attemptedPaths;

  const std::filesystem::path moduleDirectory = getCurrentModuleDirectory();
  if (!moduleDirectory.empty()) {
    attemptedPaths.push_back(moduleDirectory / L"mcrtx_assets" / L"clouds.dds");
    attemptedPaths.push_back(moduleDirectory / L"mcrtx_assets" / L"clouds.png");
    attemptedPaths.push_back(moduleDirectory / L"clouds.dds");
    attemptedPaths.push_back(moduleDirectory / L"clouds.png");
  }

  attemptedPaths.push_back(std::filesystem::current_path() / L"mcrtx_assets" / L"clouds.dds");
  attemptedPaths.push_back(std::filesystem::current_path() / L"mcrtx_assets" / L"clouds.png");
  attemptedPaths.push_back(std::filesystem::current_path() / L"clouds.dds");
  attemptedPaths.push_back(std::filesystem::current_path() / L"clouds.png");

  for (const auto& path : attemptedPaths) {
    if (std::filesystem::exists(path)) {
      return path;
    }
  }

  return {};
}

std::filesystem::path RemixRenderer::resolveFireTexturePath() {
  std::vector<std::filesystem::path> attemptedPaths;

  const std::filesystem::path moduleDirectory = getCurrentModuleDirectory();
  if (!moduleDirectory.empty()) {
    attemptedPaths.push_back(moduleDirectory / L"mcrtx_assets" / L"fire.dds");
    attemptedPaths.push_back(moduleDirectory / L"mcrtx_assets" / L"fire.png");
    attemptedPaths.push_back(moduleDirectory / L"fire.dds");
    attemptedPaths.push_back(moduleDirectory / L"fire.png");
  }

  attemptedPaths.push_back(std::filesystem::current_path() / L"mcrtx_assets" / L"fire.dds");
  attemptedPaths.push_back(std::filesystem::current_path() / L"mcrtx_assets" / L"fire.png");
  attemptedPaths.push_back(std::filesystem::current_path() / L"fire.dds");
  attemptedPaths.push_back(std::filesystem::current_path() / L"fire.png");

  for (const auto& path : attemptedPaths) {
    if (std::filesystem::exists(path)) {
      return path;
    }
  }

  return {};
}

std::filesystem::path RemixRenderer::resolveWaterTexturePath() {
  std::vector<std::filesystem::path> attemptedPaths;

  const std::filesystem::path moduleDirectory = getCurrentModuleDirectory();
  if (!moduleDirectory.empty()) {
    attemptedPaths.push_back(moduleDirectory / L"mcrtx_assets" / L"water.dds");
    attemptedPaths.push_back(moduleDirectory / L"mcrtx_assets" / L"water.png");
    attemptedPaths.push_back(moduleDirectory / L"water.dds");
    attemptedPaths.push_back(moduleDirectory / L"water.png");
  }

  attemptedPaths.push_back(std::filesystem::current_path() / L"mcrtx_assets" / L"water.dds");
  attemptedPaths.push_back(std::filesystem::current_path() / L"mcrtx_assets" / L"water.png");
  attemptedPaths.push_back(std::filesystem::current_path() / L"water.dds");
  attemptedPaths.push_back(std::filesystem::current_path() / L"water.png");

  for (const auto& path : attemptedPaths) {
    if (std::filesystem::exists(path)) {
      return path;
    }
  }

  return {};
}

std::filesystem::path RemixRenderer::resolveLavaTexturePath() {
  std::vector<std::filesystem::path> attemptedPaths;

  const std::filesystem::path moduleDirectory = getCurrentModuleDirectory();
  if (!moduleDirectory.empty()) {
    attemptedPaths.push_back(moduleDirectory / L"mcrtx_assets" / L"lava.dds");
    attemptedPaths.push_back(moduleDirectory / L"mcrtx_assets" / L"lava.png");
    attemptedPaths.push_back(moduleDirectory / L"lava.dds");
    attemptedPaths.push_back(moduleDirectory / L"lava.png");
  }

  attemptedPaths.push_back(std::filesystem::current_path() / L"mcrtx_assets" / L"lava.dds");
  attemptedPaths.push_back(std::filesystem::current_path() / L"mcrtx_assets" / L"lava.png");
  attemptedPaths.push_back(std::filesystem::current_path() / L"lava.dds");
  attemptedPaths.push_back(std::filesystem::current_path() / L"lava.png");

  for (const auto& path : attemptedPaths) {
    if (std::filesystem::exists(path)) {
      return path;
    }
  }

  return {};
}

std::filesystem::path RemixRenderer::resolvePortalTexturePath() {
  std::vector<std::filesystem::path> attemptedPaths;

  const std::filesystem::path moduleDirectory = getCurrentModuleDirectory();
  if (!moduleDirectory.empty()) {
    attemptedPaths.push_back(moduleDirectory / L"mcrtx_assets" / L"portal.dds");
    attemptedPaths.push_back(moduleDirectory / L"mcrtx_assets" / L"portal.png");
    attemptedPaths.push_back(moduleDirectory / L"portal.dds");
    attemptedPaths.push_back(moduleDirectory / L"portal.png");
  }

  attemptedPaths.push_back(std::filesystem::current_path() / L"mcrtx_assets" / L"portal.dds");
  attemptedPaths.push_back(std::filesystem::current_path() / L"mcrtx_assets" / L"portal.png");
  attemptedPaths.push_back(std::filesystem::current_path() / L"portal.dds");
  attemptedPaths.push_back(std::filesystem::current_path() / L"portal.png");

  for (const auto& path : attemptedPaths) {
    if (std::filesystem::exists(path)) {
      return path;
    }
  }

  return {};
}

std::filesystem::path RemixRenderer::resolveLavaEmissiveTexturePath() {
  std::vector<std::filesystem::path> attemptedPaths;

  const std::filesystem::path moduleDirectory = getCurrentModuleDirectory();
  if (!moduleDirectory.empty()) {
    attemptedPaths.push_back(moduleDirectory / L"mcrtx_assets" / L"lava_emissive.dds");
    attemptedPaths.push_back(moduleDirectory / L"mcrtx_assets" / L"lava_emissive.png");
    attemptedPaths.push_back(moduleDirectory / L"lava_emissive.dds");
    attemptedPaths.push_back(moduleDirectory / L"lava_emissive.png");
  }

  attemptedPaths.push_back(std::filesystem::current_path() / L"mcrtx_assets" / L"lava_emissive.dds");
  attemptedPaths.push_back(std::filesystem::current_path() / L"mcrtx_assets" / L"lava_emissive.png");
  attemptedPaths.push_back(std::filesystem::current_path() / L"lava_emissive.dds");
  attemptedPaths.push_back(std::filesystem::current_path() / L"lava_emissive.png");

  for (const auto& path : attemptedPaths) {
    if (std::filesystem::exists(path)) {
      return path;
    }
  }

  return {};
}

std::filesystem::path RemixRenderer::resolveDynamicEntityTexturePath(const std::string& texturePath) {
  if (texturePath.empty()) {
    return {};
  }

  std::string normalized = texturePath;
  if (!normalized.empty() && normalized.front() == '/') {
    normalized.erase(normalized.begin());
  }

  constexpr std::string_view kFirstPersonShadowTextureAliasPrefix = "mcrtx_alias/firstperson_shadow/";
  if (normalized.rfind(kFirstPersonShadowTextureAliasPrefix, 0) == 0) {
    normalized.erase(0, kFirstPersonShadowTextureAliasPrefix.size());
  }

  std::filesystem::path relativePath(normalized);
  relativePath.make_preferred();
  std::filesystem::path ddsPath = relativePath;
  ddsPath.replace_extension(L".dds");

  std::vector<std::filesystem::path> attemptedPaths;
  const std::filesystem::path moduleDirectory = getCurrentModuleDirectory();
  if (!moduleDirectory.empty()) {
    attemptedPaths.push_back(moduleDirectory / L"mcrtx_assets" / ddsPath);
    attemptedPaths.push_back(moduleDirectory / L"mcrtx_assets" / relativePath);
    attemptedPaths.push_back(moduleDirectory / L"mcrtx_assets" / L"entities" / ddsPath);
    attemptedPaths.push_back(moduleDirectory / L"mcrtx_assets" / L"entities" / relativePath);
  }

  attemptedPaths.push_back(std::filesystem::current_path() / L"mcrtx_assets" / ddsPath);
  attemptedPaths.push_back(std::filesystem::current_path() / L"mcrtx_assets" / relativePath);
  attemptedPaths.push_back(std::filesystem::current_path() / L"mcrtx_assets" / L"entities" / ddsPath);
  attemptedPaths.push_back(std::filesystem::current_path() / L"mcrtx_assets" / L"entities" / relativePath);

  const bool hasDirectory = relativePath.has_parent_path();
  if (!hasDirectory) {
    const std::filesystem::path mobDdsPath = std::filesystem::path(L"mob") / ddsPath;
    const std::filesystem::path mobRelativePath = std::filesystem::path(L"mob") / relativePath;
    if (!moduleDirectory.empty()) {
      attemptedPaths.push_back(moduleDirectory / L"mcrtx_assets" / L"entities" / mobDdsPath);
      attemptedPaths.push_back(moduleDirectory / L"mcrtx_assets" / L"entities" / mobRelativePath);
    }
    attemptedPaths.push_back(std::filesystem::current_path() / L"mcrtx_assets" / L"entities" / mobDdsPath);
    attemptedPaths.push_back(std::filesystem::current_path() / L"mcrtx_assets" / L"entities" / mobRelativePath);
  }

  for (const auto& path : attemptedPaths) {
    if (std::filesystem::exists(path)) {
      return path;
    }
  }

  return {};
}

std::filesystem::path RemixRenderer::resolveParticleTexturePath(std::uint32_t textureKind) {
  std::vector<std::filesystem::path> attemptedPaths;

  const bool useParticlesAtlas = textureKind == 0;
  const bool useItemAtlas = textureKind == 2;
  const bool useWeatherRainTexture = textureKind == 4;
  if (!useParticlesAtlas && !useItemAtlas && !useWeatherRainTexture) {
    return {};
  }

  const std::filesystem::path ddsPath = useParticlesAtlas
      ? std::filesystem::path(L"particles.dds")
      : (useItemAtlas
          ? std::filesystem::path(L"gui") / L"items.dds"
          : std::filesystem::path(L"rain.dds"));
  const std::filesystem::path pngPath = useParticlesAtlas
      ? std::filesystem::path(L"particles.png")
      : (useItemAtlas
          ? std::filesystem::path(L"gui") / L"items.png"
          : std::filesystem::path(L"rain.png"));

  const std::filesystem::path moduleDirectory = getCurrentModuleDirectory();
  if (!moduleDirectory.empty()) {
    attemptedPaths.push_back(moduleDirectory / L"mcrtx_assets" / ddsPath);
    attemptedPaths.push_back(moduleDirectory / L"mcrtx_assets" / pngPath);
    attemptedPaths.push_back(moduleDirectory / ddsPath);
    attemptedPaths.push_back(moduleDirectory / pngPath);
  }

  attemptedPaths.push_back(std::filesystem::current_path() / L"mcrtx_assets" / ddsPath);
  attemptedPaths.push_back(std::filesystem::current_path() / L"mcrtx_assets" / pngPath);
  attemptedPaths.push_back(std::filesystem::current_path() / ddsPath);
  attemptedPaths.push_back(std::filesystem::current_path() / pngPath);

  for (const auto& path : attemptedPaths) {
    if (std::filesystem::exists(path)) {
      return path;
    }
  }

  return {};
}

bool RemixRenderer::initializeTerrainMaterials() {
  MCRTX_PERF_SCOPE(::mcrtx::perf::Side::Native, "RemixRenderer::initializeTerrainMaterials");
  destroyTerrainMaterials();
  terrainAtlasPath_ = resolveTerrainAtlasPath();
  terrainEmissiveTexturePath_ = resolveTerrainEmissiveTexturePath();
  cloudTexturePath_ = resolveCloudTexturePath();
  fireTexturePath_ = resolveFireTexturePath();
  waterTexturePath_ = resolveWaterTexturePath();
  lavaTexturePath_ = resolveLavaTexturePath();
  portalTexturePath_ = resolvePortalTexturePath();
  lavaEmissiveTexturePath_ = resolveLavaEmissiveTexturePath();
  redstoneEmissiveTexturePath_ = resolveRedstoneEmissiveTexturePath();
  if (terrainAtlasPath_.empty()) {
    log("Terrain atlas asset not found; continuing without Remix materials");
    return false;
  }

  const auto createTerrainMaterial = [this](
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
                                       std::uint8_t spriteSheetFps) {
    remixapi_MaterialInfoOpaqueEXT opaqueInfo {};
    remixapi_MaterialInfoTranslucentEXT translucentInfo {};
    void* pNext = nullptr;

    if (isTranslucent) {
      translucentInfo.sType = REMIXAPI_STRUCT_TYPE_MATERIAL_INFO_TRANSLUCENT_EXT;
      translucentInfo.refractiveIndex = refractiveIndex;
      translucentInfo.transmittanceColor = transmittanceColor;
      translucentInfo.transmittanceMeasurementDistance = transmittanceMeasurementDistance;
      translucentInfo.thinWallThickness_hasvalue = FALSE;
      translucentInfo.useDiffuseLayer = TRUE;
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
      0);
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
      0);
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
        0);
  const bool waterCreated = !waterTexturePath_.empty() && createTerrainMaterial(
      kWaterTerrainMaterialClass,
      false,
      false,
      true,
      kWaterTransmittanceColor,
      kWaterTransmittanceDistance,
      kWaterRefractiveIndex,
      waterTexturePath_,
      kWaterTerrainMaterialHash,
      nullptr,
      0.0f,
      {0.0f, 0.0f, 0.0f},
      kLiquidAnimationFrameCount,
      1,
      kLiquidAnimationFramesPerSecond);
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
      kLiquidAnimationFramesPerSecond);
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
        0);
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
      kPortalAnimationFramesPerSecond);
  if (opaqueCreated) {
    log("Initialized terrain atlas materials from " + terrainAtlasPath_.string());
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
    for (auto& [texturePath, materialHandle] : dynamicEntityMaterialHandles_) {
      if (materialHandle != nullptr) {
        MCRTX_PERF_SCOPE(::mcrtx::perf::Side::Remix, "DestroyMaterial.entity");
        remix_.DestroyMaterial(materialHandle);
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

  if (remix_.DestroyMaterial != nullptr && cloudMaterialHandle_ != nullptr) {
    MCRTX_PERF_SCOPE(::mcrtx::perf::Side::Remix, "DestroyMaterial.cloud");
    remix_.DestroyMaterial(cloudMaterialHandle_);
    cloudMaterialHandle_ = nullptr;
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

remixapi_MaterialHandle RemixRenderer::acquireDynamicEntityMaterial(const std::string& texturePath) {
  MCRTX_PERF_SCOPE(::mcrtx::perf::Side::Native, "RemixRenderer::acquireDynamicEntityMaterial");
  const auto existing = dynamicEntityMaterialHandles_.find(texturePath);
  if (existing != dynamicEntityMaterialHandles_.end()) {
    return existing->second;
  }

  const std::filesystem::path resolvedTexturePath = resolveDynamicEntityTexturePath(texturePath);
  if (resolvedTexturePath.empty()) {
    return nullptr;
  }

  std::string normalizedTexturePath = texturePath;
  if (!normalizedTexturePath.empty() && normalizedTexturePath.front() == '/') {
    normalizedTexturePath.erase(normalizedTexturePath.begin());
  }

  constexpr std::string_view kFirstPersonShadowTextureAliasPrefix = "mcrtx_alias/firstperson_shadow/";
  if (normalizedTexturePath.rfind(kFirstPersonShadowTextureAliasPrefix, 0) == 0) {
    normalizedTexturePath.erase(0, kFirstPersonShadowTextureAliasPrefix.size());
  }

  const std::filesystem::path* emissiveTexturePath = nullptr;
  if (normalizedTexturePath == "terrain.png") {
    if (!terrainEmissiveTexturePath_.empty()) {
      emissiveTexturePath = &terrainEmissiveTexturePath_;
    } else if (!redstoneEmissiveTexturePath_.empty()) {
      emissiveTexturePath = &redstoneEmissiveTexturePath_;
    }
  }

  const wchar_t* emissiveTexture = emissiveTexturePath == nullptr ? nullptr : emissiveTexturePath->c_str();
  const float emissiveIntensity = emissiveTexture == nullptr ? 0.0f : kTerrainEmissiveIntensity;
  const remixapi_Float3D emissiveColor = emissiveTexture == nullptr
      ? remixapi_Float3D {0.0f, 0.0f, 0.0f}
      : kTerrainEmissiveColor;

  remixapi_MaterialInfoOpaqueEXT opaqueInfo {};
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
  materialInfo.hash = kDynamicEntityMaterialHashSeed ^ static_cast<std::uint64_t>(std::hash<std::string> {}(texturePath));
  materialInfo.albedoTexture = resolvedTexturePath.c_str();
  materialInfo.emissiveTexture = emissiveTexture;
  materialInfo.emissiveIntensity = emissiveIntensity;
  materialInfo.emissiveColorConstant = emissiveColor;
  materialInfo.filterMode = 0;
  materialInfo.wrapModeU = 1;
  materialInfo.wrapModeV = 1;

  remixapi_MaterialHandle materialHandle = nullptr;
  const remixapi_ErrorCode result = [&]() {
    MCRTX_PERF_SCOPE(::mcrtx::perf::Side::Remix, "CreateMaterial.entity");
    return remix_.CreateMaterial(&materialInfo, &materialHandle);
  }();
  if (result != REMIXAPI_ERROR_CODE_SUCCESS) {
    setError("CreateMaterial failed: " + errorCodeToString(result));
    return nullptr;
  }

  dynamicEntityMaterialHandles_.emplace(texturePath, materialHandle);
  return materialHandle;
}

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

  remixapi_MaterialInfoOpaqueEXT opaqueInfo {};
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