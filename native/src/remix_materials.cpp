// Auto-extracted from remix_renderer.cpp during the monolith split.
// Keep edits here; do not re-merge into remix_renderer.cpp.

#include "mcrtx/remix_renderer.hpp"
#include "mcrtx/render_internals.hpp"

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
  if (!useParticlesAtlas && !useItemAtlas) {
    return {};
  }

  const std::filesystem::path ddsPath = useParticlesAtlas
      ? std::filesystem::path(L"particles.dds")
      : std::filesystem::path(L"gui") / L"items.dds";
  const std::filesystem::path pngPath = useParticlesAtlas
      ? std::filesystem::path(L"particles.png")
      : std::filesystem::path(L"gui") / L"items.png";

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
  destroyTerrainMaterials();
  terrainAtlasPath_ = resolveTerrainAtlasPath();
  terrainEmissiveTexturePath_ = resolveTerrainEmissiveTexturePath();
  cloudTexturePath_ = resolveCloudTexturePath();
  fireTexturePath_ = resolveFireTexturePath();
  waterTexturePath_ = resolveWaterTexturePath();
  lavaTexturePath_ = resolveLavaTexturePath();
  lavaEmissiveTexturePath_ = resolveLavaEmissiveTexturePath();
  redstoneEmissiveTexturePath_ = resolveRedstoneEmissiveTexturePath();
  if (terrainAtlasPath_.empty()) {
    log("Terrain atlas asset not found; continuing without Remix materials");
    return false;
  }

  const auto createTerrainMaterial = [this](
                                       std::size_t materialClass,
                                       bool cutout,
                                       const std::filesystem::path& texturePath,
                                       std::uint64_t materialHash,
                                       const wchar_t* emissiveTexturePath,
                                       float emissiveIntensity,
                                       remixapi_Float3D emissiveColor,
                                       std::uint8_t spriteSheetColumns,
                                       std::uint8_t spriteSheetRows,
                                       std::uint8_t spriteSheetFps) {
    remixapi_MaterialInfoOpaqueEXT opaqueInfo {};
    opaqueInfo.sType = REMIXAPI_STRUCT_TYPE_MATERIAL_INFO_OPAQUE_EXT;
    opaqueInfo.albedoConstant = {1.0f, 1.0f, 1.0f};
    opaqueInfo.opacityConstant = 1.0f;
    opaqueInfo.roughnessConstant = 1.0f;
    opaqueInfo.metallicConstant = 0.0f;
    opaqueInfo.useDrawCallAlphaState = FALSE;
    opaqueInfo.alphaTestType = cutout ? 4 : 7;
    opaqueInfo.alphaReferenceValue = cutout ? 1 : 0;

    remixapi_MaterialInfo materialInfo {};
    materialInfo.sType = REMIXAPI_STRUCT_TYPE_MATERIAL_INFO;
    materialInfo.pNext = &opaqueInfo;
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
    const remixapi_ErrorCode result = remix_.CreateMaterial(&materialInfo, &materialHandle);
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
      lavaTexturePath_,
      kLavaTerrainMaterialHash,
      lavaEmissiveTexturePath_.empty() ? nullptr : lavaEmissiveTexturePath_.c_str(),
      kLavaEmissiveIntensity,
      kLavaEmissiveColor,
      kLiquidAnimationFrameCount,
      1,
      kLiquidAnimationFramesPerSecond);
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

    const remixapi_ErrorCode fireResult = remix_.CreateMaterial(&fireMaterialInfo, &fireMaterialHandle_);
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

  const remixapi_ErrorCode cloudResult = remix_.CreateMaterial(&cloudMaterialInfo, &cloudMaterialHandle_);
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
  destroyCloudMesh();
  destroyDestroyOverlayMesh();
  destroyParticleMesh();
  destroyDynamicEntityMeshes();

  if (remix_.DestroyMaterial != nullptr) {
    for (auto& [texturePath, materialHandle] : dynamicEntityMaterialHandles_) {
      if (materialHandle != nullptr) {
        remix_.DestroyMaterial(materialHandle);
      }
    }
    for (auto& [textureKind, materialHandle] : particleMaterialHandles_) {
      (void)textureKind;
      if (materialHandle != nullptr) {
        remix_.DestroyMaterial(materialHandle);
      }
    }
  }
  dynamicEntityMaterialHandles_.clear();
  particleMaterialHandles_.clear();

  if (remix_.DestroyMaterial != nullptr && fireMaterialHandle_ != nullptr) {
    remix_.DestroyMaterial(fireMaterialHandle_);
    fireMaterialHandle_ = nullptr;
  }

  if (remix_.DestroyMaterial != nullptr && cloudMaterialHandle_ != nullptr) {
    remix_.DestroyMaterial(cloudMaterialHandle_);
    cloudMaterialHandle_ = nullptr;
  }

  if (remix_.DestroyMaterial == nullptr) {
    terrainMaterialHandles_ = {};
    return;
  }

  for (remixapi_MaterialHandle& materialHandle : terrainMaterialHandles_) {
    if (materialHandle != nullptr) {
      remix_.DestroyMaterial(materialHandle);
      materialHandle = nullptr;
    }
  }

  fireTexturePath_.clear();
  waterTexturePath_.clear();
  lavaTexturePath_.clear();
  lavaEmissiveTexturePath_.clear();
  terrainEmissiveTexturePath_.clear();
  redstoneEmissiveTexturePath_.clear();
}

remixapi_MaterialHandle RemixRenderer::acquireDynamicEntityMaterial(const std::string& texturePath) {
  const auto existing = dynamicEntityMaterialHandles_.find(texturePath);
  if (existing != dynamicEntityMaterialHandles_.end()) {
    return existing->second;
  }

  const std::filesystem::path resolvedTexturePath = resolveDynamicEntityTexturePath(texturePath);
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
  materialInfo.hash = kDynamicEntityMaterialHashSeed ^ static_cast<std::uint64_t>(std::hash<std::string> {}(texturePath));
  materialInfo.albedoTexture = resolvedTexturePath.c_str();
  materialInfo.emissiveIntensity = 0.0f;
  materialInfo.emissiveColorConstant = {0.0f, 0.0f, 0.0f};
  materialInfo.filterMode = 0;
  materialInfo.wrapModeU = 1;
  materialInfo.wrapModeV = 1;

  remixapi_MaterialHandle materialHandle = nullptr;
  const remixapi_ErrorCode result = remix_.CreateMaterial(&materialInfo, &materialHandle);
  if (result != REMIXAPI_ERROR_CODE_SUCCESS) {
    setError("CreateMaterial failed: " + errorCodeToString(result));
    return nullptr;
  }

  dynamicEntityMaterialHandles_.emplace(texturePath, materialHandle);
  return materialHandle;
}

remixapi_MaterialHandle RemixRenderer::acquireParticleMaterial(std::uint32_t textureKind) {
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
  const remixapi_ErrorCode result = remix_.CreateMaterial(&materialInfo, &materialHandle);
  if (result != REMIXAPI_ERROR_CODE_SUCCESS) {
    setError("CreateMaterial failed: " + errorCodeToString(result));
    return nullptr;
  }

  particleMaterialHandles_.emplace(textureKind, materialHandle);
  return materialHandle;
}


}  // namespace mcrtx