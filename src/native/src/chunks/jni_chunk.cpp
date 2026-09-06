#include "mcrtx/core/remix_renderer.hpp"
#include "mcrtx/chunks/remix_chunk_policy.hpp"
#include "mcrtx/lifecycle/perf_log.hpp"

#include <bit>
#include <cmath>
#include <jni.h>

extern "C" {

JNIEXPORT void JNICALL Java_mcrtx_bridge_RemixChunkBridge_nResetTerrain(JNIEnv*, jclass, jlong world) {
  mcrtx::RemixRenderer::instance().terrain().reset(world);
}

JNIEXPORT void JNICALL Java_mcrtx_bridge_RemixChunkBridge_nAllocateSection(
    JNIEnv*, jclass, jint x, jint y, jint z, jlong world, jlong lifetime) {
  mcrtx::RemixRenderer::instance().terrain().allocate({x, y, z, 0}, world, lifetime);
}

JNIEXPORT void JNICALL Java_mcrtx_bridge_RemixChunkBridge_nRemoveSection(
    JNIEnv*, jclass, jint x, jint y, jint z, jlong world, jlong lifetime) {
  mcrtx::RemixRenderer::instance().terrain().remove({x, y, z, 0}, world, lifetime);
}

JNIEXPORT jboolean JNICALL Java_mcrtx_bridge_RemixChunkBridge_nUpdateSection(
    JNIEnv* env, jclass, jint x, jint y, jint z, jlong world, jlong lifetime, jlong revision,
    jint minX, jint minY, jint minZ, jint maxX, jint maxY, jint maxZ, jintArray records) {
  MCRTX_PERF_SCOPE(::mcrtx::perf::Side::Jni, "terrain.update");
  if (!records || minX < 0 || minY < 0 || minZ < 0 || maxX > 15 || maxY > 15 || maxZ > 15
      || minX > maxX || minY > maxY || minZ > maxZ || world <= 0 || lifetime <= 0 || revision <= 0
      || (x & 15) || (y & 15) || (z & 15) || y < 0 || y >= 128) {
    return JNI_FALSE;
  }
  constexpr int kRecordWords = 23;
  const int count = (maxX - minX + 1) * (maxY - minY + 1) * (maxZ - minZ + 1);
  if (env->GetArrayLength(records) != count * kRecordWords) {
    return JNI_FALSE;
  }
  try {
    std::vector<jint> owned(count * kRecordWords);
    env->GetIntArrayRegion(records, 0, static_cast<jsize>(owned.size()), owned.data());
    if (env->ExceptionCheck()) {
      return JNI_FALSE;
    }
    std::vector<mcrtx::ChunkBlockCell> cells(count);
    for (int i = 0; i < count; ++i) {
      const auto* pRecord = owned.data() + i * kRecordWords;
      if (!mcrtx::chunk::shouldCaptureBlock(pRecord[0], pRecord[2], pRecord[3])) {
        continue;
      }
      auto& cell = cells[i];
      cell.blockId = pRecord[0];
      cell.blockMetadata = pRecord[1];
      cell.renderType = pRecord[2];
      cell.renderPass = pRecord[3];
      cell.materialClass = mcrtx::chunk::materialClassForBlock(pRecord[0], pRecord[1], pRecord[2]);
      for (int j = 0; j < 6; ++j) {
        cell.terrainTiles[j] = pRecord[4 + j];
        cell.bounds[j] = std::bit_cast<float>(pRecord[10 + j]);
        if (!std::isfinite(cell.bounds[j])) {
          return JNI_FALSE;
        }
      }
      cell.blockColor = pRecord[16] & 0x00FFFFFFu;
      cell.liquidVisibilityMask = pRecord[17] & 0x3F;
      for (int j = 0; j < 4; ++j) {
        cell.liquidHeights[j] = std::bit_cast<float>(pRecord[18 + j]);
        if (!std::isfinite(cell.liquidHeights[j])) {
          return JNI_FALSE;
        }
      }
      cell.liquidFlowAngle = std::bit_cast<float>(pRecord[22]);
      if (!std::isfinite(cell.liquidFlowAngle)) {
        return JNI_FALSE;
      }
    }
    return mcrtx::RemixRenderer::instance().terrain().update({x, y, z, 0}, world, lifetime, revision,
        {minX, minY, minZ, maxX, maxY, maxZ}, cells) ? JNI_TRUE : JNI_FALSE;
  } catch (const std::bad_alloc&) {
    return JNI_FALSE;
  }
}

} // extern "C"
