#pragma once

#include <filesystem>
#include <string>
#include <string_view>

#include <remix/remix_c.h>

namespace mcrtx::material {

inline constexpr std::string_view kFirstPersonShadowTextureAliasPrefix = "mcrtx_alias/firstperson_shadow/";
inline constexpr std::string_view kEntityFireOverlayTextureAliasPrefix = "mcrtx_alias/entity_fire_overlay/";
inline constexpr std::string_view kSignTextTextureAliasPrefix = "mcrtx_alias/sign_text/";

struct OptionalPbrTextures {
  std::filesystem::path normal {};
  std::filesystem::path roughness {};
  std::filesystem::path metallic {};
  std::filesystem::path height {};
};

struct OptionalSssTextures {
  std::filesystem::path transmittance {};
  std::filesystem::path thickness {};
  std::filesystem::path singleScatteringAlbedo {};
  std::filesystem::path radius {};

  bool any() const {
    return !transmittance.empty()
        || !thickness.empty()
        || !singleScatteringAlbedo.empty()
        || !radius.empty();
  }
};

struct OpaqueSubsurfaceSettings {
  float measurementDistance {1.0f};
  float radiusScale {1.0f};
  float maxSampleRadius {16.0f};
  float volumetricAnisotropy {0.0f};
  bool diffusionProfileEnabled {true};
};

bool stripDynamicEntityTextureAliasPrefix(std::string& texturePath, std::string_view prefix);
bool prefersDdsTerrainAtlas();
const wchar_t* optionalTexturePath(const std::filesystem::path& path);
OptionalPbrTextures resolveOptionalPbrTextures(const std::filesystem::path& texturePath);
OptionalSssTextures resolveOptionalSssTextures(const std::filesystem::path& texturePath);
void applyOptionalOpaqueMaterialTextures(
    remixapi_MaterialInfo& materialInfo,
    remixapi_MaterialInfoOpaqueEXT& opaqueInfo,
    remixapi_MaterialInfoOpaqueSubsurfaceEXT& subsurfaceInfo,
    const OptionalPbrTextures& pbrTextures,
    const OptionalSssTextures& sssTextures,
    float displacementFactor,
    const OpaqueSubsurfaceSettings& subsurfaceSettings);

}  // namespace mcrtx::material
