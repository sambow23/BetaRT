#pragma once

#include <array>
#include <filesystem>
#include <string>
#include <string_view>

namespace mcrtx {

inline constexpr std::string_view kAtmosphereSunTextureGameValue = "__atmosphere.sun.texture";
inline constexpr std::string_view kAtmosphereMoon0TextureGameValue = "__atmosphere.moon0.texture";

enum class CelestialTextureKind {
  Sun,
  Moon0,
};

struct CelestialTexturePaths {
  std::filesystem::path sun;
  std::filesystem::path moon0;
};

std::wstring_view celestialTextureBaseName(CelestialTextureKind kind);

std::filesystem::path resolveCelestialTexturePath(
    CelestialTextureKind kind,
    const std::filesystem::path& moduleDirectory,
    const std::filesystem::path& currentDirectory);

std::array<std::pair<std::string_view, std::string>, 2> makeCelestialTextureGameValues(
    const CelestialTexturePaths& paths);

}  // namespace mcrtx
