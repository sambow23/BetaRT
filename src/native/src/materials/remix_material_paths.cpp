// Material asset path resolution.

#include "mcrtx/core/remix_renderer.hpp"
#include "mcrtx/materials/remix_material_common.hpp"
#include "mcrtx/core/remix_render_common.hpp"
#include "mcrtx/core/runtime_config.hpp"

#include <sstream>
#include <string>
#include <vector>
#include <fstream>

namespace mcrtx::material {

void pushAssetCandidates(
    std::vector<std::filesystem::path>& attemptedPaths,
    const std::filesystem::path& moduleDirectory,
    const std::filesystem::path& relativePath) {
  std::filesystem::path cacheDir = getCurrentTexturePackCacheDir();
  if (!cacheDir.empty()) {
    attemptedPaths.push_back((cacheDir / relativePath).lexically_normal());
  }
  attemptedPaths.push_back((std::filesystem::current_path() / L"mcrtx_texturepack_cache" / relativePath).lexically_normal());
  if (!moduleDirectory.empty()) {
    attemptedPaths.push_back((moduleDirectory / L"mcrtx_assets" / relativePath).lexically_normal());
    attemptedPaths.push_back((moduleDirectory / relativePath).lexically_normal());
  }
  attemptedPaths.push_back((std::filesystem::current_path() / L"mcrtx_assets" / relativePath).lexically_normal());
  attemptedPaths.push_back((std::filesystem::current_path() / L".." / L"libraries" / L"mcrtx_assets" / relativePath).lexically_normal());
  attemptedPaths.push_back((std::filesystem::current_path() / relativePath).lexically_normal());
}

}  // namespace mcrtx::material

namespace mcrtx {

using namespace mcrtx::detail;
using namespace mcrtx::material;

namespace {

void appendAtlasCandidates(
    std::vector<std::filesystem::path>& attemptedPaths,
    const std::filesystem::path& baseDirectory,
    const wchar_t* stem,
    bool preferDds) {
  if (baseDirectory.empty()) {
    return;
  }

  const std::filesystem::path ddsPath = baseDirectory / (std::wstring(stem) + L".dds");
  const std::filesystem::path pngPath = baseDirectory / (std::wstring(stem) + L".png");
  if (preferDds) {
    attemptedPaths.push_back(ddsPath);
    attemptedPaths.push_back(pngPath);
  } else {
    attemptedPaths.push_back(pngPath);
    attemptedPaths.push_back(ddsPath);
  }
}

void appendAtlasCandidatesWithCache(
    std::vector<std::filesystem::path>& attemptedPaths,
    const std::filesystem::path& baseDirectory,
    const wchar_t* stem,
    bool preferDds) {
  std::filesystem::path cacheDir = getCurrentTexturePackCacheDir();
  if (!cacheDir.empty()) {
    const std::filesystem::path ddsPath = cacheDir / (std::wstring(stem) + L".dds");
    const std::filesystem::path pngPath = cacheDir / (std::wstring(stem) + L".png");
    if (preferDds) {
      attemptedPaths.push_back(ddsPath);
      attemptedPaths.push_back(pngPath);
    } else {
      attemptedPaths.push_back(pngPath);
      attemptedPaths.push_back(ddsPath);
    }
  }
  
  appendAtlasCandidates(attemptedPaths, baseDirectory, stem, preferDds);
}

}  // namespace

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
    attemptedPaths.push_back((moduleDirectory / "d3d9.dll").lexically_normal());
    attemptedPaths.push_back((moduleDirectory / "bin" / "d3d9.dll").lexically_normal());
    attemptedPaths.push_back((moduleDirectory / ".." / "libraries" / "d3d9.dll").lexically_normal());
    attemptedPaths.push_back((moduleDirectory / "libraries" / "d3d9.dll").lexically_normal());
  }

  const std::filesystem::path currentPath = std::filesystem::current_path();
  attemptedPaths.push_back((currentPath / "d3d9.dll").lexically_normal());
  attemptedPaths.push_back((currentPath / "bin" / "d3d9.dll").lexically_normal());
  attemptedPaths.push_back((currentPath / "libraries" / "d3d9.dll").lexically_normal());
  attemptedPaths.push_back((currentPath / ".." / "libraries" / "d3d9.dll").lexically_normal());

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
  const bool preferDds = prefersDdsTerrainAtlas();

  const std::filesystem::path moduleDirectory = getCurrentModuleDirectory();
  if (!moduleDirectory.empty()) {
    appendAtlasCandidatesWithCache(attemptedPaths, moduleDirectory / L"mcrtx_assets", L"terrain", preferDds);
    appendAtlasCandidatesWithCache(attemptedPaths, moduleDirectory, L"terrain", preferDds);
  }

  appendAtlasCandidatesWithCache(attemptedPaths, std::filesystem::current_path() / L"mcrtx_assets", L"terrain", preferDds);
  appendAtlasCandidatesWithCache(attemptedPaths, std::filesystem::current_path() / L".." / L"libraries" / L"mcrtx_assets", L"terrain", preferDds);
  appendAtlasCandidatesWithCache(attemptedPaths, std::filesystem::current_path(), L"terrain", preferDds);

  for (const auto& path : attemptedPaths) {
    if (std::filesystem::exists(path)) {
      return path;
    }
  }

  return {};
}

// One atlas per detail level: terrain_lod2, terrain_lod4, terrain_lod8,
// terrain_lod16. They share a layout -- 2048 px, 128 px slots, a 64 px content
// window -- and differ only in how many block tiles that window holds, so the
// UV constants are the same for all of them and only the file changes.
//
// Step 32 has no atlas of its own and is not meant to. The window holds
// 64 / step block tiles, so a step-32 atlas would resample every 16 px terrain
// tile down to two pixels -- a flat average with one bit of variation left in
// it -- and then repeat that thirty-two times across the window. Borrowing
// step 16's instead draws the pattern at twice its true size, which is the one
// thing wrong with it, from between two and four kilometres away, where a cell
// is a couple of pixels and the whole pattern is below the mip chain either
// way. The alternative costs 22 MB of DDS in every install and every release
// package to buy nothing anyone can see, so lodMaterialForPass's
// nearest-level-with-an-atlas fallback is the answer here rather than a
// shortcut around one.
std::filesystem::path RemixRenderer::resolveLodAtlasPath(int step) {
  std::vector<std::filesystem::path> attemptedPaths;
  const bool preferDds = prefersDdsTerrainAtlas();
  const std::wstring stemText = L"terrain_lod" + std::to_wstring(step);
  const wchar_t* stem = stemText.c_str();

  const std::filesystem::path moduleDirectory = getCurrentModuleDirectory();
  if (!moduleDirectory.empty()) {
    appendAtlasCandidatesWithCache(attemptedPaths, moduleDirectory / L"mcrtx_assets", stem, preferDds);
    appendAtlasCandidatesWithCache(attemptedPaths, moduleDirectory, stem, preferDds);
  }

  appendAtlasCandidatesWithCache(attemptedPaths, std::filesystem::current_path() / L"mcrtx_assets", stem, preferDds);
  appendAtlasCandidatesWithCache(attemptedPaths, std::filesystem::current_path() / L".." / L"libraries" / L"mcrtx_assets", stem, preferDds);
  appendAtlasCandidatesWithCache(attemptedPaths, std::filesystem::current_path(), stem, preferDds);

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
  pushAssetCandidates(attemptedPaths, moduleDirectory, L"terrain_emissive.dds");
  pushAssetCandidates(attemptedPaths, moduleDirectory, L"terrain_emissive.png");
  for (const auto& path : attemptedPaths) {
    if (std::filesystem::exists(path)) return path;
  }
  return {};
}

std::filesystem::path RemixRenderer::resolveItemsEmissiveTexturePath() {
  std::vector<std::filesystem::path> attemptedPaths;
  const std::filesystem::path moduleDirectory = getCurrentModuleDirectory();
  pushAssetCandidates(attemptedPaths, moduleDirectory, L"gui/items_emissive.dds");
  pushAssetCandidates(attemptedPaths, moduleDirectory, L"gui/items_emissive.png");
  for (const auto& path : attemptedPaths) {
    if (std::filesystem::exists(path)) return path;
  }
  return {};
}

std::filesystem::path RemixRenderer::resolveRedstoneEmissiveTexturePath() {
  std::vector<std::filesystem::path> attemptedPaths;
  const std::filesystem::path moduleDirectory = getCurrentModuleDirectory();
  pushAssetCandidates(attemptedPaths, moduleDirectory, L"redstone_emissive.dds");
  pushAssetCandidates(attemptedPaths, moduleDirectory, L"redstone_emissive.png");
  for (const auto& path : attemptedPaths) {
    if (std::filesystem::exists(path)) return path;
  }
  return {};
}

std::filesystem::path RemixRenderer::resolveCloudTexturePath() {
  std::vector<std::filesystem::path> attemptedPaths;
  const std::filesystem::path moduleDirectory = getCurrentModuleDirectory();
  pushAssetCandidates(attemptedPaths, moduleDirectory, L"clouds.dds");
  pushAssetCandidates(attemptedPaths, moduleDirectory, L"clouds.png");
  for (const auto& path : attemptedPaths) {
    if (std::filesystem::exists(path)) return path;
  }
  return {};
}

std::filesystem::path RemixRenderer::resolveSunTexturePath() {
  return resolveCelestialTexturePath(
      CelestialTextureKind::Sun,
      getCurrentModuleDirectory(),
      std::filesystem::current_path());
}

std::filesystem::path RemixRenderer::resolveMoonTexturePath() {
  return resolveCelestialTexturePath(
      CelestialTextureKind::Moon0,
      getCurrentModuleDirectory(),
      std::filesystem::current_path());
}

std::filesystem::path RemixRenderer::resolveFireTexturePath() {
  std::vector<std::filesystem::path> attemptedPaths;
  const std::filesystem::path moduleDirectory = getCurrentModuleDirectory();
  pushAssetCandidates(attemptedPaths, moduleDirectory, L"fire.dds");
  pushAssetCandidates(attemptedPaths, moduleDirectory, L"fire.png");
  for (const auto& path : attemptedPaths) {
    if (std::filesystem::exists(path)) return path;
  }
  return {};
}

std::filesystem::path RemixRenderer::resolveWaterTexturePath() {
  std::vector<std::filesystem::path> attemptedPaths;
  const std::filesystem::path moduleDirectory = getCurrentModuleDirectory();
  pushAssetCandidates(attemptedPaths, moduleDirectory, L"water.dds");
  pushAssetCandidates(attemptedPaths, moduleDirectory, L"water.png");
  for (const auto& path : attemptedPaths) {
    if (std::filesystem::exists(path)) return path;
  }
  return {};
}

std::filesystem::path RemixRenderer::resolveLavaTexturePath() {
  std::vector<std::filesystem::path> attemptedPaths;
  const std::filesystem::path moduleDirectory = getCurrentModuleDirectory();
  pushAssetCandidates(attemptedPaths, moduleDirectory, L"lava.dds");
  pushAssetCandidates(attemptedPaths, moduleDirectory, L"lava.png");
  for (const auto& path : attemptedPaths) {
    if (std::filesystem::exists(path)) return path;
  }
  return {};
}

std::filesystem::path RemixRenderer::resolvePortalTexturePath() {
  std::vector<std::filesystem::path> attemptedPaths;
  const std::filesystem::path moduleDirectory = getCurrentModuleDirectory();
  pushAssetCandidates(attemptedPaths, moduleDirectory, L"portal.dds");
  pushAssetCandidates(attemptedPaths, moduleDirectory, L"portal.png");
  for (const auto& path : attemptedPaths) {
    if (std::filesystem::exists(path)) return path;
  }
  return {};
}

std::filesystem::path RemixRenderer::resolveLavaEmissiveTexturePath() {
  std::vector<std::filesystem::path> attemptedPaths;
  const std::filesystem::path moduleDirectory = getCurrentModuleDirectory();
  pushAssetCandidates(attemptedPaths, moduleDirectory, L"lava_emissive.dds");
  pushAssetCandidates(attemptedPaths, moduleDirectory, L"lava_emissive.png");
  for (const auto& path : attemptedPaths) {
    if (std::filesystem::exists(path)) return path;
  }
  return {};
}

std::filesystem::path RemixRenderer::resolveDynamicEntityTexturePath(const std::string& texturePath) {
    if (texturePath.empty()) {
      return {};
    }
  
    std::string normalized = texturePath;
    auto queryPos = normalized.find('?');
    if (queryPos != std::string::npos) {
        normalized = normalized.substr(0, queryPos);
    }
    
    if (!normalized.empty() && normalized.front() == '/') {
      normalized.erase(normalized.begin());
    }

  stripDynamicEntityTextureAliasPrefix(normalized, kFirstPersonShadowTextureAliasPrefix);
  stripDynamicEntityTextureAliasPrefix(normalized, kEntityFireOverlayTextureAliasPrefix);
  stripDynamicEntityTextureAliasPrefix(normalized, kSignTextTextureAliasPrefix);
  stripDynamicEntityTextureAliasPrefix(normalized, kLapisTextureAliasPrefix);

  std::filesystem::path relativePath(normalized);
  relativePath.make_preferred();
  std::filesystem::path ddsPath = relativePath;
  ddsPath.replace_extension(L".dds");

  std::vector<std::filesystem::path> attemptedPaths;
  const std::filesystem::path moduleDirectory = getCurrentModuleDirectory();
  pushAssetCandidates(attemptedPaths, moduleDirectory, ddsPath);
  pushAssetCandidates(attemptedPaths, moduleDirectory, relativePath);
  pushAssetCandidates(attemptedPaths, moduleDirectory, std::filesystem::path(L"entities") / ddsPath);
  pushAssetCandidates(attemptedPaths, moduleDirectory, std::filesystem::path(L"entities") / relativePath);

  const bool hasDirectory = relativePath.has_parent_path();
  if (!hasDirectory) {
    const std::filesystem::path mobDdsPath = std::filesystem::path(L"mob") / ddsPath;
    const std::filesystem::path mobRelativePath = std::filesystem::path(L"mob") / relativePath;
    pushAssetCandidates(attemptedPaths, moduleDirectory, std::filesystem::path(L"entities") / mobDdsPath);
    pushAssetCandidates(attemptedPaths, moduleDirectory, std::filesystem::path(L"entities") / mobRelativePath);
  }

  for (const auto& path : attemptedPaths) {
    if (std::filesystem::exists(path)) return path;
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
  pushAssetCandidates(attemptedPaths, moduleDirectory, ddsPath);
  pushAssetCandidates(attemptedPaths, moduleDirectory, pngPath);

  for (const auto& path : attemptedPaths) {
    if (std::filesystem::exists(path)) return path;
  }

  return {};
}
}  // namespace mcrtx
