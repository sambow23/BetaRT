// Shared material texture discovery and extension-chain helpers.

#include "mcrtx/materials/remix_material_common.hpp"
#include "mcrtx/core/runtime_config.hpp"

#include <cctype>

namespace mcrtx::material {

using namespace mcrtx::detail;

namespace {

constexpr float kDefaultHeightMapDisplaceIn = 0.05f;
constexpr float kDefaultHeightMapDisplaceOut = 0.0f;

std::filesystem::path resolveOptionalPbrSibling(const std::filesystem::path& texturePath, const wchar_t* suffix) {
  if (texturePath.empty()) {
    return {};
  }

  const std::filesystem::path parentPath = texturePath.parent_path();
  const std::wstring stemWithSuffix = texturePath.stem().wstring() + suffix;
  for (const wchar_t* extension : {L".dds", L".png"}) {
    std::filesystem::path candidatePath = parentPath / stemWithSuffix;
    candidatePath.replace_extension(extension);
    if (std::filesystem::exists(candidatePath)) {
      return candidatePath;
    }
  }

  return {};
}

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

const wchar_t* optionalTexturePath(const std::filesystem::path& path) {
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
