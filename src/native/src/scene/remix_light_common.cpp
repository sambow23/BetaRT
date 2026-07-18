// Shared torch-light hashing and placement policy.

#include "mcrtx/scene/remix_light_common.hpp"
#include "mcrtx/core/remix_render_common.hpp"

#include <algorithm>
#include <bit>

namespace mcrtx::light {

using mcrtx::detail::kRedstoneTorchOnBlockId;
using mcrtx::detail::mixHashComponent;

std::uint64_t makeTorchLightHash(const WorldBlockPosition& position) {

  std::uint64_t hash = kTorchLightHashSeed;
  hash = mixHashComponent(hash, std::bit_cast<std::uint32_t>(position.x));
  hash = mixHashComponent(hash, std::bit_cast<std::uint32_t>(position.y));
  hash = mixHashComponent(hash, std::bit_cast<std::uint32_t>(position.z));
  return hash;
}

bool containsWorldBlockPosition(const std::vector<WorldBlockPosition>& positions, const WorldBlockPosition& position) {
  return std::find(positions.begin(), positions.end(), position) != positions.end();
}

const TorchLightPlacement* findTorchLightPlacement(
    const std::vector<TorchLightPlacement>& placements,
    const WorldBlockPosition& position) {
  const auto it = std::find_if(
      placements.begin(),
      placements.end(),
      [&position](const TorchLightPlacement& placement) {
        return placement.blockPosition == position;
      });
  return it == placements.end() ? nullptr : &(*it);
}

TorchLightPlacement makeTorchLightPlacement(
    const ChunkBlockCell& cell,
    int worldX,
    int worldY,
    int worldZ) {
  TorchLightPlacement placement;
  placement.blockPosition = WorldBlockPosition {
      .x = worldX,
      .y = worldY,
      .z = worldZ,
  };
  placement.lightX = static_cast<float>(worldX) + kTorchLightOffsetX;
  placement.lightY = static_cast<float>(worldY) + kTorchLightOffsetY;
  placement.lightZ = static_cast<float>(worldZ) + kTorchLightOffsetZ;
  placement.radiance = cell.blockId == kRedstoneTorchOnBlockId
      ? kRedstoneTorchLightRadiance
      : kTorchLightRadiance;

  switch (cell.blockMetadata & 7) {
    case 1:
      placement.lightX -= kWallTorchLightHorizontalOffset;
      placement.lightY += kWallTorchLightVerticalOffset;
      break;
    case 2:
      placement.lightX += kWallTorchLightHorizontalOffset;
      placement.lightY += kWallTorchLightVerticalOffset;
      break;
    case 3:
      placement.lightZ -= kWallTorchLightHorizontalOffset;
      placement.lightY += kWallTorchLightVerticalOffset;
      break;
    case 4:
      placement.lightZ += kWallTorchLightHorizontalOffset;
      placement.lightY += kWallTorchLightVerticalOffset;
      break;
    default:
      break;
  }

  return placement;
}

}  // namespace mcrtx::light
