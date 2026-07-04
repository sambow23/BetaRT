#pragma once

#include <array>
#include <cstddef>
#include <cstdint>
#include <string>
#include <vector>

#include <remix/remix_c.h>

namespace mcrtx {

struct DynamicEntityQuad {
  std::array<float, 12> positions {};
  std::array<float, 8> texcoords {};
  std::uint32_t color {0xFFFFFFFFu};
  std::uint32_t textureIndex {0};
  std::uint64_t textureFingerprint {0};
  bool blendEnabled {false};
  std::uint32_t boneIndex {0};
};

struct DynamicEntityBoneTransform {
  remixapi_Transform transform {};
  double worldX {0.0};
  double worldY {0.0};
  double worldZ {0.0};
};

constexpr std::uint32_t kDynamicEntityMaxHurtStage = 10;
constexpr std::uint32_t kDynamicEntityMaxCreeperFuseStage = 10;
constexpr std::size_t kDynamicEntityMaxQuadCount = 2048;
constexpr std::size_t kDynamicEntityMaterialVariantCount =
  (static_cast<std::size_t>(kDynamicEntityMaxHurtStage) + 1u)
  * (static_cast<std::size_t>(kDynamicEntityMaxCreeperFuseStage) + 1u)
  * 2u;

enum class DynamicEntityMaterialClass : std::uint8_t {
  Cutout = 0,
  Translucent = 1,
};

struct DynamicEntityBuildState {
  int entityId {-1};
  std::uint32_t hurtStage {0};
  std::uint32_t creeperFuseStage {0};
  std::uint32_t maxBoneCount {0};
  std::uint64_t quadFingerprint {0};
  std::uint32_t currentTextureIndex {0xFFFFFFFFu};
  std::uint64_t currentTextureFingerprint {0};
  std::vector<std::string> texturePaths {};
  std::array<DynamicEntityQuad, kDynamicEntityMaxQuadCount> quads {};
  std::size_t quadCount {0};
  std::vector<DynamicEntityBoneTransform> boneTransforms {};
  bool active {false};
};

struct DynamicEntityMeshData {
  remixapi_MeshHandle meshHandle {nullptr};
  std::uint64_t meshHash {0};
  std::uint64_t geometryFingerprint {0};
  std::size_t quadCount {0};
  std::uint32_t boneCount {0};
};

struct DynamicEntityFrameInstance {
  int entityId {-1};
  remixapi_MeshHandle meshHandle {nullptr};
  std::size_t quadCount {0};
  std::vector<DynamicEntityBoneTransform> boneTransforms {};
};

struct DynamicEntityRenderInstance {
  int entityId {-1};
  remixapi_MeshHandle meshHandle {nullptr};
  std::size_t quadCount {0};
  std::vector<remixapi_Transform> boneTransforms {};
};

} // namespace mcrtx