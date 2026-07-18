#include "mcrtx/scene/celestial_textures.hpp"

#include <cstdlib>
#include <filesystem>
#include <fstream>
#include <iostream>
#include <string>
#include <string_view>

namespace {

void require(bool condition, const char* message) {
  if (!condition) {
    std::cerr << message << '\n';
    std::exit(1);
  }
}

void requireValue(std::string_view actual, std::string_view expected, const char* message) {
  if (actual != expected) {
    std::cerr << message << ": expected " << expected << ", got " << actual << '\n';
    std::exit(1);
  }
}

void touchFile(const std::filesystem::path& path) {
  std::filesystem::create_directories(path.parent_path());
  std::ofstream file(path, std::ios::binary);
  file << "x";
}

std::filesystem::path cleanTestRoot() {
  const auto root = std::filesystem::temp_directory_path() / L"mcrtx_celestial_texture_tests";
  std::filesystem::remove_all(root);
  std::filesystem::create_directories(root);
  return root;
}

}  // namespace

int main() {
  const auto root = cleanTestRoot();
  const auto moduleDir = root / L"module";
  const auto currentDir = root / L"cwd";

  touchFile(currentDir / L"mcrtx_assets" / L"sun.png");
  touchFile(currentDir / L"mcrtx_assets" / L"moon.png");
  require(
      mcrtx::resolveCelestialTexturePath(mcrtx::CelestialTextureKind::Sun, moduleDir, currentDir)
          == currentDir / L"mcrtx_assets" / L"sun.png",
      "sun png fallback in current mcrtx_assets");
  require(
      mcrtx::resolveCelestialTexturePath(mcrtx::CelestialTextureKind::Moon0, moduleDir, currentDir)
          == currentDir / L"mcrtx_assets" / L"moon.png",
      "moon png fallback in current mcrtx_assets");

  touchFile(currentDir / L"mcrtx_assets" / L"sun.dds");
  touchFile(currentDir / L"mcrtx_assets" / L"moon.dds");
  require(
      mcrtx::resolveCelestialTexturePath(mcrtx::CelestialTextureKind::Sun, moduleDir, currentDir)
          == currentDir / L"mcrtx_assets" / L"sun.dds",
      "sun prefers dds over png");
  require(
      mcrtx::resolveCelestialTexturePath(mcrtx::CelestialTextureKind::Moon0, moduleDir, currentDir)
          == currentDir / L"mcrtx_assets" / L"moon.dds",
      "moon prefers dds over png");

  touchFile(moduleDir / L"mcrtx_assets" / L"sun.dds");
  require(
      mcrtx::resolveCelestialTexturePath(mcrtx::CelestialTextureKind::Sun, moduleDir, currentDir)
          == moduleDir / L"mcrtx_assets" / L"sun.dds",
      "module mcrtx_assets has highest priority");

  const auto values = mcrtx::makeCelestialTextureGameValues({
      moduleDir / L"mcrtx_assets" / L"sun.dds",
      currentDir / L"mcrtx_assets" / L"moon.dds",
  });
  require(values.size() == 2, "two game values");
  requireValue(values[0].first, "__atmosphere.sun.texture", "sun game value key");
  requireValue(values[1].first, "__atmosphere.moon0.texture", "moon game value key");
  require(values[0].second.find("sun.dds") != std::string::npos, "sun game value contains path");
  require(values[1].second.find("moon.dds") != std::string::npos, "moon game value contains path");

  const auto missingRoot = root / L"missing";
  require(
      mcrtx::resolveCelestialTexturePath(mcrtx::CelestialTextureKind::Sun, missingRoot, missingRoot).empty(),
      "missing sun path returns empty");

  std::filesystem::remove_all(root);
  return 0;
}
