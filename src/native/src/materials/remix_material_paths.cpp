// Material asset path resolution.

#include "mcrtx/core/remix_renderer.hpp"
#include "mcrtx/materials/remix_material_common.hpp"
#include "mcrtx/core/remix_render_common.hpp"
#include "mcrtx/core/runtime_config.hpp"

#include <sstream>
#include <vector>

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
  const bool preferDds = prefersDdsTerrainAtlas();

  const std::filesystem::path moduleDirectory = getCurrentModuleDirectory();
  if (!moduleDirectory.empty()) {
    appendAtlasCandidates(attemptedPaths, moduleDirectory / L"mcrtx_assets", L"terrain", preferDds);
    appendAtlasCandidates(attemptedPaths, moduleDirectory, L"terrain", preferDds);
  }

  appendAtlasCandidates(attemptedPaths, std::filesystem::current_path() / L"mcrtx_assets", L"terrain", preferDds);
  appendAtlasCandidates(attemptedPaths, std::filesystem::current_path(), L"terrain", preferDds);

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

  stripDynamicEntityTextureAliasPrefix(normalized, kFirstPersonShadowTextureAliasPrefix);
  stripDynamicEntityTextureAliasPrefix(normalized, kEntityFireOverlayTextureAliasPrefix);
  stripDynamicEntityTextureAliasPrefix(normalized, kSignTextTextureAliasPrefix);

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
}  // namespace mcrtx
