// Materials for distant terrain, drawn from the pre-tiled LOD atlases.

#include "mcrtx/core/remix_renderer.hpp"
#include "mcrtx/core/remix_render_common.hpp"
#include "mcrtx/lifecycle/perf_log.hpp"
#include "mcrtx/lod/lod_types.hpp"

#include <string>

namespace mcrtx {

using namespace mcrtx::detail;

namespace {

constexpr std::uint64_t kLodOpaqueMaterialHash = 0x4D435254584C4F41ull;  // "MCRTXLOA"
constexpr std::uint64_t kLodWaterMaterialHash = 0x4D435254584C4F57ull;   // "MCRTXLOW"
constexpr std::uint64_t kLodDebugMaterialHash = 0x4D435254584C4F44ull;   // "MCRTXLOD"

// Debug view colour per detail level, finest first. A single colour tells you
// where the LOD field is; one per level tells you where each ring starts and
// stops, which is the thing that is otherwise impossible to see from inside the
// game.
constexpr remixapi_Float3D kLodDebugColors[lod::kLodStepCount] = {
    {1.0f, 0.0f, 0.85f},  // step 2, magenta
    {0.0f, 0.9f, 1.0f},   // step 4, cyan
    {1.0f, 0.55f, 0.0f},  // step 8, amber
    {0.25f, 1.0f, 0.25f}, // step 16, green
    {0.9f, 0.9f, 0.15f},  // step 32, yellow
};

// How a LOD material shades, as opposed to which atlas it samples. Every level
// registers the same three looks, so this is what separates them.
struct LodMaterialLook {
  remixapi_Float3D albedoConstant {1.0f, 1.0f, 1.0f};
  float roughnessConstant {1.0f};
  // False draws the material from albedoConstant alone, with no atlas bound.
  bool useAtlasTexture {true};
  bool emissive {false};
};

// Ground: the atlas is the whole appearance, so the constant is white and the
// surface stays fully rough. Distant terrain has no specular character worth
// spending a lobe on.
constexpr LodMaterialLook kLodOpaqueLook {{1.0f, 1.0f, 1.0f}, 1.0f, true, false};

// Water is what it reflects. Nearly all of what makes an ocean read as an ocean
// at range is the sky in it, and inheriting the ground's fully rough look is
// how distant water came to look like poured concrete.
//
// Two things change together and both are needed. A near-mirror roughness gives
// the surface something to reflect with; kept slightly off zero because a
// perfect mirror turns the sun into a single-pixel firefly the denoiser then
// smears, where a little roughness spreads it into the glint that actually
// reads as water.
constexpr float kLodWaterRoughness = 0.06f;

// And an albedo of water rather than of whatever lies under it. Dropping the
// atlas here is the point rather than a shortcut: a column carries one tile
// index and it is the ground's, so the water pass has always drawn sand and
// gravel with a water material over it. A 16 px water tile is sub-pixel at LOD
// range in any case, so the constant costs nothing that was visible. The value
// is deep water's true diffuse albedo -- nearly black; what you see instead is
// the Fresnel reflection, which is what makes a surface look wet.
constexpr LodMaterialLook kLodWaterLook {
    {0.015f, 0.035f, 0.055f}, kLodWaterRoughness, false, false};

// Every detail level needs a hash of its own. Remix refuses to register a hash
// it already holds -- "Ignoring repeated material registration" in its log --
// and the levels are otherwise identical, differing only in which atlas they
// sample. The index goes above the ASCII bytes so the constants stay readable.
std::uint64_t lodMaterialHashForStep(std::uint64_t baseHash, int stepIndex) {
  return baseHash ^ (static_cast<std::uint64_t>(stepIndex + 1) << 32);
}

}  // namespace

bool RemixRenderer::initializeLodMaterials() {
  MCRTX_PERF_SCOPE(::mcrtx::perf::Side::Native, "RemixRenderer::initializeLodMaterials");

  // Release the previous set before registering again. This runs from several
  // lifecycle points, and re-registering a hash Remix already holds is refused
  // outright, leaving distant terrain pointing at materials the renderer never
  // took.
  if (remix_.DestroyMaterial != nullptr) {
    const auto destroy = [this](remixapi_MaterialHandle& materialHandle) {
      if (materialHandle != nullptr) {
        MCRTX_PERF_SCOPE(::mcrtx::perf::Side::Remix, "DestroyMaterial.lod");
        remix_.DestroyMaterial(materialHandle);
        materialHandle = nullptr;
      }
    };
    for (LodStepMaterials& stepMaterials : lodStepMaterials_) {
      destroy(stepMaterials.opaque);
      destroy(stepMaterials.water);
      destroy(stepMaterials.debug);
    }
  }
  lodStepMaterials_ = {};

  const auto createLodMaterial = [this](
                                     remixapi_MaterialHandle& target,
                                     std::uint64_t materialHash,
                                     const std::filesystem::path& atlasPath,
                                     const LodMaterialLook& look) {
    remixapi_MaterialInfoOpaqueEXT opaqueInfo {};
    opaqueInfo.sType = REMIXAPI_STRUCT_TYPE_MATERIAL_INFO_OPAQUE_EXT;
    opaqueInfo.albedoConstant = look.albedoConstant;
    opaqueInfo.opacityConstant = 1.0f;
    opaqueInfo.roughnessConstant = look.roughnessConstant;
    opaqueInfo.metallicConstant = 0.0f;
    // Water is registered here too, and stays opaque: refraction and
    // transmittance cost real time in the path tracer and resolve to nothing
    // across a surface a few pixels wide, so the reflection is the whole of
    // what a distant sea needs.
    opaqueInfo.useDrawCallAlphaState = FALSE;
    opaqueInfo.alphaTestType = 7;
    opaqueInfo.alphaReferenceValue = 0;

    remixapi_MaterialInfo materialInfo {};
    materialInfo.sType = REMIXAPI_STRUCT_TYPE_MATERIAL_INFO;
    materialInfo.pNext = &opaqueInfo;
    materialInfo.hash = materialHash;
    if (look.useAtlasTexture) {
      materialInfo.albedoTexture = atlasPath.c_str();
    }
    if (look.emissive) {
      // Emission keeps the debug view readable in shadow, and the constant
      // colour still shows even if Remix declines to emit without an emissive
      // texture.
      materialInfo.emissiveIntensity = 5.0f;
      materialInfo.emissiveColorConstant = look.albedoConstant;
    }
    // Linear, unlike the full-detail terrain material, which is deliberately
    // nearest so blocks stay crisp under the player's nose. A LOD cell is never
    // magnified -- four blocks across at two hundred blocks away is a handful
    // of pixels -- so nearest sampling here only buys shimmer as the camera
    // moves. Paired with the atlas mip chain this is what makes distant terrain
    // settle instead of crawl.
    materialInfo.filterMode = 1;
    materialInfo.wrapModeU = 1;
    materialInfo.wrapModeV = 1;

    remixapi_MaterialHandle materialHandle = nullptr;
    const remixapi_ErrorCode result = [&]() {
      MCRTX_PERF_SCOPE(::mcrtx::perf::Side::Remix, "CreateMaterial.lod");
      return remix_.CreateMaterial(&materialInfo, &materialHandle);
    }();
    if (result != REMIXAPI_ERROR_CODE_SUCCESS) {
      setError("CreateMaterial(lod) failed: " + errorCodeToString(result));
      return false;
    }

    target = materialHandle;
    return true;
  };

  // Collected rather than concatenated as it goes, because how a level without
  // an atlas should be described depends on whether any level found one. Step
  // 32 deliberately ships without its own -- see resolveLodAtlasPath -- so
  // "missing" would put a line in every session's log that reads like a broken
  // install rather than a decision.
  std::string atlasNames[lod::kLodStepCount];
  bool anyAtlasFound = false;

  for (int stepIndex = 0; stepIndex < lod::kLodStepCount; ++stepIndex) {
    const int step = lod::kLodSteps[stepIndex];
    LodStepMaterials& stepMaterials = lodStepMaterials_[stepIndex];

    // The debug view needs no texture, so it is created whether or not this
    // level found an atlas -- a level drawing nothing and a level drawing from
    // the wrong atlas look the same until you can see which ring is which.
    // No albedo texture: a flat constant colour makes the LOD field, and the
    // exact line where it hands over to chunk geometry, unmistakable.
    if (!createLodMaterial(
            stepMaterials.debug,
            lodMaterialHashForStep(kLodDebugMaterialHash, stepIndex),
            {},
            LodMaterialLook {kLodDebugColors[stepIndex], 1.0f, false, true})) {
      return false;
    }

    stepMaterials.atlasPath = resolveLodAtlasPath(step);
    if (stepMaterials.atlasPath.empty()) {
      // Not fatal: regions at this level borrow the nearest level that does
      // have an atlas, and fall back to the full-detail terrain atlas -- which
      // renders the block texture at cell scale -- when none does.
      continue;
    }
    anyAtlasFound = true;
    atlasNames[stepIndex] = stepMaterials.atlasPath.filename().string();

    if (!createLodMaterial(
            stepMaterials.opaque,
            lodMaterialHashForStep(kLodOpaqueMaterialHash, stepIndex),
            stepMaterials.atlasPath,
            kLodOpaqueLook)) {
      return false;
    }
    if (!createLodMaterial(
            stepMaterials.water,
            lodMaterialHashForStep(kLodWaterMaterialHash, stepIndex),
            stepMaterials.atlasPath,
            kLodWaterLook)) {
      return false;
    }
  }

  std::string atlasSummary;
  for (int stepIndex = 0; stepIndex < lod::kLodStepCount; ++stepIndex) {
    if (!atlasSummary.empty()) {
      atlasSummary += ", ";
    }
    atlasSummary += "step" + std::to_string(lod::kLodSteps[stepIndex]) + "=";
    if (!atlasNames[stepIndex].empty()) {
      atlasSummary += atlasNames[stepIndex];
    } else {
      atlasSummary += anyAtlasFound ? "borrowed" : "missing";
    }
  }
  log("LOD atlases: " + atlasSummary);

  // Existing region meshes still reference the handles just destroyed, so drop
  // them and let the scheduler rebuild against the new materials. This runs
  // from rebuildMaterialDependentMeshesLocked, which already holds the mutex,
  // so it must not take it again.
  clearLodRegionsLocked();
  // The scheduler still believes those regions are resident and would never
  // submit them again. Bumping the token is what tells it otherwise; without
  // it, a texture pack or material change blanks distant terrain for the rest
  // of the session.
  lodRebuildToken_.fetch_add(1, std::memory_order_relaxed);
  return true;
}

// Rebuilds every material the atlases feed, under the lock.
//
// The lock is what makes this safe against a region build in flight: the build
// reads its material handle and the rebuild token together under the same lock,
// and checks the token again before it installs anything, so a build that was
// meshing while this ran is dropped rather than left holding a destroyed
// handle. The body already assumed the lock in any case -- initializeLodMaterials
// calls clearLodRegionsLocked, which must not take it a second time -- so the
// JNI reload path reaching it without one was wrong before there was a worker
// thread to make it visible.
void RemixRenderer::reloadMaterials() {
  std::scoped_lock lock(mutex_);
  initializeTerrainMaterials();
  initializeLodMaterials();
}

void RemixRenderer::setLodDebugViewEnabled(bool enabled) {
  std::scoped_lock lock(mutex_);
  lodDebugViewEnabled_ = enabled;
}

remixapi_MaterialHandle RemixRenderer::lodMaterialForPass(int renderPass, int step) const {
  const int stepIndex = lod::lodStepIndex(step);

  if (lodDebugViewEnabled_) {
    if (stepIndex >= 0 && lodStepMaterials_[stepIndex].debug != nullptr) {
      return lodStepMaterials_[stepIndex].debug;
    }
    for (const LodStepMaterials& stepMaterials : lodStepMaterials_) {
      if (stepMaterials.debug != nullptr) {
        return stepMaterials.debug;
      }
    }
  }

  if (stepIndex >= 0) {
    // The level's own atlas first, then the nearest level that has one. A
    // neighbouring level's atlas draws the block texture at twice or half its
    // true size, which is wrong but far closer than the full-detail atlas,
    // where one tile is stretched across the whole cell.
    for (int distance = 0; distance < lod::kLodStepCount; ++distance) {
      for (int direction = -1; direction <= 1; direction += 2) {
        const int candidate = stepIndex + direction * distance;
        if (candidate < 0 || candidate >= lod::kLodStepCount) {
          continue;
        }
        const LodStepMaterials& stepMaterials = lodStepMaterials_[candidate];
        remixapi_MaterialHandle handle =
            renderPass == 0 ? stepMaterials.opaque : stepMaterials.water;
        if (handle != nullptr) {
          return handle;
        }
        if (distance == 0) {
          break;
        }
      }
    }
  }

  return renderPass == 0
      ? terrainMaterialHandles_[kOpaqueTerrainMaterialClass]
      : terrainMaterialHandles_[kWaterTerrainMaterialClass];
}

// True when this level is meshed against a pre-tiled LOD atlas, which decides
// how the geometry lays out its UVs. A level borrowing a neighbour's atlas
// still counts: the layout is identical, only the tiling density differs.
bool RemixRenderer::lodAtlasActiveForStep(int step) const {
  const int stepIndex = lod::lodStepIndex(step);
  if (stepIndex < 0) {
    return false;
  }
  if (lodStepMaterials_[stepIndex].opaque != nullptr) {
    return true;
  }
  for (const LodStepMaterials& stepMaterials : lodStepMaterials_) {
    if (stepMaterials.opaque != nullptr) {
      return true;
    }
  }
  return false;
}

}  // namespace mcrtx
