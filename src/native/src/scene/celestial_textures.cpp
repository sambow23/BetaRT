#include "mcrtx/scene/celestial_textures.hpp"
#include <vector>

namespace mcrtx {
namespace {

void appendFileCandidates(
    std::vector<std::filesystem::path>& candidates,
    const std::filesystem::path& directory,
    std::wstring_view baseName) {
  if (directory.empty()) {
    return;
  }

  candidates.push_back(directory / (std::wstring(baseName) + L".dds"));
#if defined(_WIN32)
  candidates.push_back(directory / (std::wstring(baseName) + L".png"));
#endif
}

void appendCandidates(
    std::vector<std::filesystem::path>& candidates,
    const std::filesystem::path& root,
    std::wstring_view baseName) {
  if (root.empty()) {
    return;
  }

  appendFileCandidates(candidates, root / L"mcrtx_assets", baseName);
  appendFileCandidates(candidates, root, baseName);
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
    const std::filesystem::path& currentDirectory,
    const std::filesystem::path& texturePackCacheDirectory) {
  std::vector<std::filesystem::path> candidates;
  const std::wstring_view baseName = celestialTextureBaseName(kind);
  appendFileCandidates(candidates, texturePackCacheDirectory, baseName);
  appendCandidates(candidates, moduleDirectory, baseName);
  appendCandidates(candidates, currentDirectory, baseName);
  appendCandidates(candidates, currentDirectory / L".." / L"libraries", baseName);

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
