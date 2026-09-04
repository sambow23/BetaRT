// Shared material texture discovery and extension-chain helpers.

#include "mcrtx/materials/remix_material_common.hpp"
#include "mcrtx/core/runtime_config.hpp"
#include "mcrtx/core/remix_renderer.hpp"

#include <cwctype>
#include <cctype>
#include <fstream>
#include <string>

namespace mcrtx::material {

using namespace mcrtx::detail;

namespace {
constexpr float kDefaultHeightMapDisplaceIn = 0.05f;
constexpr float kDefaultHeightMapDisplaceOut = 0.0f;
}  // namespace

std::filesystem::path getCurrentTexturePackCacheDir() {
  std::filesystem::path cacheBase = std::filesystem::current_path() / L"mcrtx_texturepack_cache";
  std::filesystem::path currentTxt = cacheBase / L"current.txt";
  if (std::filesystem::exists(currentTxt)) {
    std::ifstream file(currentTxt);
    std::string currentId;
    if (std::getline(file, currentId) && !currentId.empty()) {
      return cacheBase / currentId;
    }
  }
  return {};
}

std::filesystem::path resolveOptionalPbrSibling(const std::filesystem::path& texturePath, const wchar_t* suffix) {
  if (texturePath.empty()) {
    return {};
  }

  const std::filesystem::path parentPath = texturePath.parent_path();
  const std::wstring stem = texturePath.stem().wstring();

  std::vector<std::wstring> stemCandidates;
  stemCandidates.push_back(stem + suffix);

  // If the stem ends with _<timestamp> (digits only), also try matching without the timestamp suffix (e.g. skin_Ver_1_emissive.dds)
  const std::size_t lastUnderscore = stem.rfind(L'_');
  if (lastUnderscore != std::wstring::npos && lastUnderscore + 1 < stem.size()) {
    bool isTimestamp = true;
    for (std::size_t i = lastUnderscore + 1; i < stem.size(); ++i) {
      if (!::iswdigit(stem[i])) {
        isTimestamp = false;
        break;
      }
    }
    if (isTimestamp) {
      const std::wstring baseStem = stem.substr(0, lastUnderscore);
      stemCandidates.push_back(baseStem + suffix);
    }
  }

  std::filesystem::path relativeToAssets;
  bool foundAssets = false;
  for (auto it = parentPath.begin(); it != parentPath.end(); ++it) {
    if (foundAssets) {
      relativeToAssets /= *it;
    } else if (*it == L"mcrtx_assets" || *it == L"mcrtx_texturepack_cache") {
      foundAssets = true;
    }
  }

  const std::filesystem::path moduleDirectory = getCurrentModuleDirectory();

  for (const std::wstring& candidateStem : stemCandidates) {
    std::filesystem::path filename = std::filesystem::path(candidateStem).replace_extension(L".dds");
    
    // Check directly in parentPath first
    std::filesystem::path candidatePath = (parentPath / filename).lexically_normal();
    if (std::filesystem::exists(candidatePath)) {
      return candidatePath;
    }

    std::vector<std::filesystem::path> attemptedPaths;
    if (foundAssets) {
      pushAssetCandidates(attemptedPaths, moduleDirectory, relativeToAssets / filename);
    }
    pushAssetCandidates(attemptedPaths, moduleDirectory, filename);

    for (const auto& path : attemptedPaths) {
      if (std::filesystem::exists(path)) {
        return path;
      }
    }
  }

  return {};
}

namespace {
std::filesystem::path resolveOptionalHeightTexture(const std::filesystem::path& texturePath) {
  if (const std::filesystem::path heightPath = resolveOptionalPbrSibling(texturePath, L"_height"); !heightPath.empty()) {
    return heightPath;
  }

  if (const std::filesystem::path depthPath = resolveOptionalPbrSibling(texturePath, L"_depth"); !depthPath.empty()) {
    return depthPath;
  }

  return resolveOptionalPbrSibling(texturePath, L"_displacement");
}

std::filesystem::path resolveOptionalSingleScatteringAlbedoTexture(const std::filesystem::path& texturePath) {
  if (const std::filesystem::path snakeCasePath = resolveOptionalPbrSibling(texturePath, L"_single_scattering_albedo");
      !snakeCasePath.empty()) {
    return snakeCasePath;
  }

  return resolveOptionalPbrSibling(texturePath, L"_singleScatteringAlbedo");
}

}  // namespace

bool stripDynamicEntityTextureAliasPrefix(std::string& texturePath, std::string_view prefix) {
  if (texturePath.rfind(prefix, 0) != 0) {
    return false;
  }

  texturePath.erase(0, prefix.size());
  return true;
}

bool prefersDdsTerrainAtlas() {
  const std::string configuredPreference = readEnvironmentVariable("MCRTX_TERRAIN_ATLAS_SOURCE");
  if (configuredPreference.empty()) {
    return true;
  }

  std::string normalizedPreference;
  normalizedPreference.reserve(configuredPreference.size());
  for (const unsigned char ch : configuredPreference) {
    normalizedPreference.push_back(static_cast<char>(std::tolower(ch)));
  }

  return normalizedPreference == "dds";
}

remixapi_Path optionalTexturePath(const std::filesystem::path& path) {
  return path.empty() ? nullptr : path.c_str();
}

OptionalPbrTextures resolveOptionalPbrTextures(const std::filesystem::path& texturePath) {
  return {
      resolveOptionalPbrSibling(texturePath, L"_normal"),
      resolveOptionalPbrSibling(texturePath, L"_roughness"),
      resolveOptionalPbrSibling(texturePath, L"_metallic"),
      resolveOptionalHeightTexture(texturePath)};
}

OptionalSssTextures resolveOptionalSssTextures(const std::filesystem::path& texturePath) {
  return {
      resolveOptionalPbrSibling(texturePath, L"_transmittance"),
      resolveOptionalPbrSibling(texturePath, L"_thickness"),
      resolveOptionalSingleScatteringAlbedoTexture(texturePath),
      resolveOptionalPbrSibling(texturePath, L"_radius")};
}

void applyOptionalOpaqueMaterialTextures(
    remixapi_MaterialInfo& materialInfo,
    remixapi_MaterialInfoOpaqueEXT& opaqueInfo,
    remixapi_MaterialInfoOpaqueSubsurfaceEXT& subsurfaceInfo,
    const OptionalPbrTextures& pbrTextures,
    const OptionalSssTextures& sssTextures,
    float displacementFactor,
    const OpaqueSubsurfaceSettings& subsurfaceSettings) {
  materialInfo.normalTexture = optionalTexturePath(pbrTextures.normal);
  opaqueInfo.roughnessTexture = optionalTexturePath(pbrTextures.roughness);
  opaqueInfo.metallicTexture = optionalTexturePath(pbrTextures.metallic);
  opaqueInfo.heightTexture = optionalTexturePath(pbrTextures.height);
  opaqueInfo.displaceIn = pbrTextures.height.empty() ? 0.0f : (kDefaultHeightMapDisplaceIn * displacementFactor);
  opaqueInfo.displaceOut = pbrTextures.height.empty() ? 0.0f : kDefaultHeightMapDisplaceOut;
  opaqueInfo.pNext = nullptr;

  if (!sssTextures.any()) {
    return;
  }

  subsurfaceInfo.sType = REMIXAPI_STRUCT_TYPE_MATERIAL_INFO_OPAQUE_SUBSURFACE_EXT;
  subsurfaceInfo.pNext = nullptr;
  subsurfaceInfo.subsurfaceTransmittanceTexture = optionalTexturePath(sssTextures.transmittance);
  subsurfaceInfo.subsurfaceThicknessTexture = optionalTexturePath(sssTextures.thickness);
  subsurfaceInfo.subsurfaceSingleScatteringAlbedoTexture = optionalTexturePath(sssTextures.singleScatteringAlbedo);
  subsurfaceInfo.subsurfaceTransmittanceColor = {1.0f, 1.0f, 1.0f};
  subsurfaceInfo.subsurfaceMeasurementDistance = subsurfaceSettings.measurementDistance;
  subsurfaceInfo.subsurfaceSingleScatteringAlbedo = {1.0f, 1.0f, 1.0f};
  subsurfaceInfo.subsurfaceVolumetricAnisotropy = subsurfaceSettings.volumetricAnisotropy;
  subsurfaceInfo.subsurfaceDiffusionProfile = subsurfaceSettings.diffusionProfileEnabled ? TRUE : FALSE;
  subsurfaceInfo.subsurfaceRadius = {1.0f, 1.0f, 1.0f};
  subsurfaceInfo.subsurfaceRadiusScale = subsurfaceSettings.radiusScale;
  subsurfaceInfo.subsurfaceMaxSampleRadius = subsurfaceSettings.maxSampleRadius;
  subsurfaceInfo.subsurfaceRadiusTexture = optionalTexturePath(sssTextures.radius);
  opaqueInfo.pNext = &subsurfaceInfo;
}

}  // namespace mcrtx::material
