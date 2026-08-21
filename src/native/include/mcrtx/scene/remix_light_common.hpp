#pragma once

#include <cstdint>
#include <vector>

#include <remix/remix_c.h>

#include "mcrtx/core/remix_renderer.hpp"

namespace mcrtx::light {

constexpr std::uint64_t kTorchLightHashSeed = 0x4D435254584C4954ull;
constexpr std::uint64_t kHeldTorchLightHash = 0x4D4352545848544Cull;
constexpr std::uint64_t kEntityHeldTorchLightHashSeed = 0x4D4352545845544Cull;
constexpr float kTorchLightOffsetX = 0.5f;
constexpr float kTorchLightOffsetY = 0.70f;
constexpr float kTorchLightOffsetZ = 0.5f;
constexpr float kTorchLightRadius = 0.06f;
constexpr float kHeldTorchLightForwardOffset = 0.35f;
constexpr float kHeldTorchLightRightOffset = 0.18f;
constexpr float kHeldTorchLightUpOffset = -0.18f;
constexpr float kWallTorchLightHorizontalOffset = 0.27f;
constexpr float kWallTorchLightVerticalOffset = 0.22f;
inline constexpr remixapi_Float3D kTorchLightRadiance = {540.0f, 331.5f, 121.5f};
inline constexpr remixapi_Float3D kRedstoneTorchLightRadiance = {220.0f, 36.0f, 24.0f};
inline constexpr remixapi_Float3D kLavaBucketLightRadiance = {35.0f, 7.0f, 1.5f};

constexpr std::uint64_t kPortalLightHashSeed = 0x4D43525458505254ull;
inline constexpr remixapi_Float3D kPortalLightRadiance = {300.0f, 100.0f, 800.0f}; // Much brighter

std::uint64_t makeTorchLightHash(const WorldBlockPosition& position);
std::uint64_t makePortalLightHash(const WorldBlockPosition& position);

bool containsWorldBlockPosition(
    const std::vector<WorldBlockPosition>& positions,
    const WorldBlockPosition& position);
const TorchLightPlacement* findTorchLightPlacement(
    const std::vector<TorchLightPlacement>& placements,
    const WorldBlockPosition& position);
const PortalLightPlacement* findPortalLightPlacement(
    const std::vector<PortalLightPlacement>& placements,
    const WorldBlockPosition& position);
TorchLightPlacement makeTorchLightPlacement(
    const ChunkBlockCell& cell,
    int worldX,
    int worldY,
    int worldZ);
PortalLightPlacement makePortalLightPlacement(
    const ChunkBlockCell& cell,
    int worldX,
    int worldY,
    int worldZ);

}  // namespace mcrtx::light
