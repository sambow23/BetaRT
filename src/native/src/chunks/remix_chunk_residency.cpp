#include "mcrtx/core/remix_renderer.hpp"

namespace mcrtx {
void RemixRenderer::destroyChunkMeshHandle(ChunkMeshData& meshData) {
  destroyMeshHandle(meshData.meshHandle);
  meshData.meshHash = 0;
}

void RemixRenderer::destroyChunkMesh(ChunkMeshData& meshData) {
  destroyChunkMeshHandle(meshData);
  destroyChunkTorchLights(meshData);
  destroyChunkPortalLights(meshData);
  destroyChunkGlowstoneLights(meshData);
  meshData.meshFingerprint = 0;
  terrainSubmissionsDirty_ = true;
  lastFireChunkBuildCount_ = ~terrainPublications_;
}
} // namespace mcrtx
