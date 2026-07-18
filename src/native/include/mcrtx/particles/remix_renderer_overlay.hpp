#pragma once

#include <array>
#include <cstddef>
#include <cstdint>

#include <remix/remix_c.h>

#include "mcrtx/scene/remix_renderer_scene.hpp"

namespace mcrtx {

struct DestroyOverlayInstance {
  int blockX {0};
  int blockY {0};
  int blockZ {0};
  int blockId {0};
  int blockMetadata {0};
  int renderType {0};
  int destroyStage {0};
};

struct BlockOutlineInstance {
  int blockX {0};
  int blockY {0};
  int blockZ {0};
};

struct ParticleQuad {
  std::array<float, 12> positions {};
  std::array<float, 8> texcoords {};
  std::uint32_t color {0xFFFFFFFFu};
  std::uint32_t textureKind {0};
};

struct ChunkRenderInstance {
  ChunkKey chunkKey {};
  remixapi_MeshHandle meshHandle {nullptr};
  std::size_t blockCount {0};
};

} // namespace mcrtx