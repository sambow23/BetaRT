#pragma once

#include <array>
#include <cstddef>
#include <cstdint>
#include <vector>

#include <remix/remix_c.h>

#include "mcrtx/core/world_origin.hpp"

namespace mcrtx {

struct ChunkBuildState {
  int origin[3] {0, 0, 0};
  int size[3] {0, 0, 0};
  int dirtyMin[3] {0, 0, 0};
  int dirtyMax[3] {0, 0, 0};
  int renderPass {0};
  std::uint64_t blockCount {0};
  std::array<std::uint32_t, 256> blockIdCounts {};
};

struct ChunkBlockCell {
  std::array<std::int16_t, 6> terrainTiles {};
  std::uint8_t materialClass {0};
  std::uint8_t blockId {0};
  std::uint8_t blockMetadata {0};
  std::uint8_t renderType {0};
  std::array<float, 6> bounds {0.0f, 0.0f, 0.0f, 1.0f, 1.0f, 1.0f};
  std::uint8_t liquidVisibilityMask {0x3F};
  std::array<float, 4> liquidHeights {1.0f, 1.0f, 1.0f, 1.0f};
  float liquidFlowAngle {-1000.0f};
  std::uint32_t blockColor {0x00FFFFFFu};
};

struct CapturedBlockInstance {
  int position[3] {0, 0, 0};
  int blockId {0};
  int blockMetadata {0};
  int renderType {0};
  std::array<std::int16_t, 6> terrainTiles {};
  std::uint8_t materialClass {0};
  std::array<float, 6> bounds {0.0f, 0.0f, 0.0f, 1.0f, 1.0f, 1.0f};
  std::uint8_t liquidVisibilityMask {0x3F};
  std::array<float, 4> liquidHeights {1.0f, 1.0f, 1.0f, 1.0f};
  float liquidFlowAngle {-1000.0f};
  std::uint32_t blockColor {0x00FFFFFFu};
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
  std::size_t operator()(const ChunkKey& key) const noexcept;
};

struct WorldBlockPosition {
  int x {0};
  int y {0};
  int z {0};

  bool operator==(const WorldBlockPosition& other) const noexcept {
    return x == other.x && y == other.y && z == other.z;
  }
};

struct WorldBlockPositionHash {
  std::size_t operator()(const WorldBlockPosition& position) const noexcept;
};

struct TorchLightPlacement {
  WorldBlockPosition blockPosition {};
  float lightX {0.0f};
  float lightY {0.0f};
  float lightZ {0.0f};
  remixapi_Float3D radiance {0.0f, 0.0f, 0.0f};
};

struct EntityHeldTorchLightState {
  remixapi_LightHandle handle {nullptr};
  double worldX {0.0};
  double worldY {0.0};
  double worldZ {0.0};
  WorldRenderOrigin renderOrigin {};
  int itemId {-1};
};

struct TorchLightState {
  remixapi_LightHandle handle {nullptr};
  WorldRenderOrigin renderOrigin {};
  std::uint64_t apiHash {0};
  WorldRenderPosition submittedPosition {};
};

struct ChunkMeshData {
  remixapi_MeshHandle meshHandle {nullptr};
  std::uint64_t meshHash {0};
  std::uint64_t geometryFingerprint {0};
  std::uint64_t meshFingerprint {0};
  std::size_t blockCount {0};
  std::array<std::uint8_t, 4096> occupancy {};
  std::array<ChunkBlockCell, 4096> cells {};
  std::vector<std::uint16_t> fireCellIndices {};
  std::vector<TorchLightPlacement> torchLights {};
  bool hasOccupancy {false};
  bool hidden {false};
  std::array<bool, 6> faceCovered {};
};

} // namespace mcrtx