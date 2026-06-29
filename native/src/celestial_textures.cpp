#include "mcrtx/celestial_textures.hpp"

#include <vector>

namespace mcrtx {
namespace {

void appendCandidates(
    std::vector<std::filesystem::path>& candidates,
    const std::filesystem::path& root,
    std::wstring_view baseName) {
  if (root.empty()) {
    return;
  }

  candidates.push_back(root / L"mcrtx_assets" / (std::wstring(baseName) + L".dds"));
  candidates.push_back(root / L"mcrtx_assets" / (std::wstring(baseName) + L".png"));
  candidates.push_back(root / (std::wstring(baseName) + L".dds"));
  candidates.push_back(root / (std::wstring(baseName) + L".png"));
}

std::string pathToGameValue(const std::filesystem::path& path) {
  return path.empty() ? std::string() : path.string();
}

}  // namespace

std::wstring_view celestialTextureBaseName(CelestialTextureKind kind) {
  switch (kind) {
  case CelestialTextureKind::Sun:
    return L"sun";
  case CelestialTextureKind::Moon0:
    return L"moon";
  }

  return L"";
}

std::filesystem::path resolveCelestialTexturePath(
    CelestialTextureKind kind,
    const std::filesystem::path& moduleDirectory,
    const std::filesystem::path& currentDirectory) {
  std::vector<std::filesystem::path> candidates;
  const std::wstring_view baseName = celestialTextureBaseName(kind);
  appendCandidates(candidates, moduleDirectory, baseName);
  appendCandidates(candidates, currentDirectory, baseName);

  for (const auto& candidate : candidates) {
    if (std::filesystem::exists(candidate)) {
      return candidate;
    }
  }

  return {};
}

std::array<std::pair<std::string_view, std::string>, 2> makeCelestialTextureGameValues(
    const CelestialTexturePaths& paths) {
  return {{
      {kAtmosphereSunTextureGameValue, pathToGameValue(paths.sun)},
      {kAtmosphereMoon0TextureGameValue, pathToGameValue(paths.moon0)},
  }};
}

}  // namespace mcrtx
