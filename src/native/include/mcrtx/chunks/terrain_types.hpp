#pragma once
#include <array>
#include <cstdint>
#include <cstddef>
namespace mcrtx {
struct ChunkBlockCell {
  std::array<std::int16_t, 6> terrainTiles {};
  std::uint8_t materialClass {0};
  std::uint8_t blockId {0};
  std::uint8_t blockMetadata {0};
  std::uint8_t renderType {0};
  std::uint8_t renderPass {0};
  std::array<float, 6> bounds {0.0f, 0.0f, 0.0f, 1.0f, 1.0f, 1.0f};
  std::uint8_t liquidVisibilityMask {0x3F};
  std::array<float, 4> liquidHeights {1.0f, 1.0f, 1.0f, 1.0f};
  float liquidFlowAngle {-1000.0f};
  std::uint32_t blockColor {0x00FFFFFFu};
  bool operator==(const ChunkBlockCell&) const = default;
};

struct TerrainSectionData {
  std::array<std::uint8_t, 4096> occupancy {};
  std::array<ChunkBlockCell, 4096> cells {};
};

struct ChunkKey {
  int originX {0};
  int originY {0};
  int originZ {0};
  int renderPass {0};

  bool operator==(const ChunkKey& other) const noexcept {
    return originX == other.originX
        && originY == other.originY
        && originZ == other.originZ
        && renderPass == other.renderPass;
  }
};

struct ChunkKeyHash {
  std::size_t operator()(const ChunkKey& key) const noexcept {
    std::size_t hash = 0;
    for (int value : {key.originX, key.originY, key.originZ, key.renderPass})
      hash ^= static_cast<std::size_t>(value) + 0x9e3779b9u + (hash << 6) + (hash >> 2);
    return hash;
  }
};

}
