// Auto-extracted from remix_renderer.cpp during the monolith split.
// Keep edits here; do not re-merge into remix_renderer.cpp.

#include "mcrtx/core/remix_renderer.hpp"
#include "mcrtx/core/remix_render_common.hpp"
#include "mcrtx/lifecycle/perf_log.hpp"

#include <algorithm>
#include <atomic>
#include <bit>
#include <cctype>
#include <cmath>
#include <cstdlib>
#include <cstddef>
#include <cstdint>
#include <iomanip>
#include <iostream>
#include <sstream>
#include <string_view>
#include <vector>

namespace mcrtx {

using namespace mcrtx::detail;

void RemixRenderer::rebuildMaterialDependentMeshesLocked() {
  destroyBlockOutlineMesh();

  destroyTerrainMaterials();
  initializeTerrainMaterials();

  for (auto& [chunkKey, meshData] : chunkMeshes_) {
    if (meshData.hasOccupancy) {
      rebuildChunkMeshFromData(chunkKey, meshData, true);
    }
  }

  const WorldRenderOrigin renderOrigin = currentRenderOriginLocked();
  if (!destroyOverlayInstances_.empty()) {
    rebuildDestroyOverlayMesh(renderOrigin);
  }

  if (!blockOutlineInstances_.empty()) {
    rebuildBlockOutlineMesh(renderOrigin);
  }

  if (!particleQuads_.empty()) {
    rebuildParticleMesh(renderOrigin);
  }

  rebuildFireMesh(renderOrigin);
}

}  // namespace mcrtx
