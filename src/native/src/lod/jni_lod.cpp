#include "mcrtx/core/jni_helpers.hpp"
#include "mcrtx/lifecycle/perf_log.hpp"
#include "mcrtx/core/remix_renderer.hpp"
#include "mcrtx/lod/lod_types.hpp"

#include <jni.h>

#include <atomic>
#include <string>
#include <vector>

namespace {

using mcrtx::RemixRenderer;
using mcrtx::lod::kPaddedColumnsPerRegion;
using mcrtx::lod::kWordsPerColumn;
using mcrtx::lod::LodColumn;
using mcrtx::lod::RegionKey;

// Java hands over a whole region in one call: kWordsPerColumn ints per column,
// row-major in Z then X. One JNI crossing per region rather than per column
// keeps region rebuilds off the profile entirely.
//
// The grid is kPaddedRegionColumns square rather than kRegionColumns: the
// mesher needs a one-cell apron of the neighbouring regions' cells to average
// corner heights that agree across a region boundary. Apron cells for ground no
// tile covers arrive with every height unset, which every consumer already
// reads as "nothing here".
LodColumn unpackColumn(const jint* words) {
  LodColumn column;
  column.topY = static_cast<std::int16_t>(words[0] & 0xFFFF);
  column.waterY = static_cast<std::int16_t>((words[0] >> 16) & 0xFFFF);
  column.topTile = static_cast<std::int16_t>(words[1] & 0xFFFF);
  column.sideTile = static_cast<std::int16_t>((words[1] >> 16) & 0xFFFF);
  column.color = static_cast<std::uint32_t>(words[2]);
  column.canopyTopY = static_cast<std::int16_t>(words[3] & 0xFFFF);
  column.canopyBottomY = static_cast<std::int16_t>((words[3] >> 16) & 0xFFFF);
  column.canopyTopTile = static_cast<std::int16_t>(words[4] & 0xFFFF);
  column.canopySideTile = static_cast<std::int16_t>((words[4] >> 16) & 0xFFFF);
  column.canopyColor = static_cast<std::uint32_t>(words[5]);
  column.trunkTile = static_cast<std::int16_t>(words[6] & 0xFFFF);
  column.hasTrunk = ((words[6] >> 16) & 0x1) != 0;
  return column;
}

// Says why a region was turned away at the boundary.
//
// Java gets a bare false back and can only report "native refused", so a
// refusal here used to be a dead end -- and the width check below is exactly
// the kind that fires for every region in the field the moment the two sides
// of the payload disagree by a single word. Naming both numbers turns a
// session of bisecting into one line of log.
//
// One-shot per reason. The first refusal has already said everything the
// thousandth would, and this runs on the store worker for every region it
// builds.
void refuse(std::atomic<bool>& logged, const std::string& reason) {
  if (logged.exchange(true, std::memory_order_relaxed)) {
    return;
  }
  RemixRenderer::instance().logPublic("LOD region refused at the JNI boundary: " + reason);
}

std::atomic<bool> g_loggedNullPayload {false};
std::atomic<bool> g_loggedBadRegionKey {false};
std::atomic<bool> g_loggedWidthMismatch {false};

}  // namespace

extern "C" {

JNIEXPORT jboolean JNICALL Java_mcrtx_bridge_RemixLodBridge_nSubmitLodRegion(
    JNIEnv* env,
    jclass,
    jint originX,
    jint originZ,
    jint step,
    jint renderPass,
    jintArray packedColumns) {
  MCRTX_PERF_SCOPE(::mcrtx::perf::Side::Jni, "nSubmitLodRegion");

  if (packedColumns == nullptr) {
    refuse(g_loggedNullPayload, "no payload");
    return JNI_FALSE;
  }
  if (step <= 0 || (renderPass != 0 && renderPass != 1)) {
    refuse(g_loggedBadRegionKey, "step " + std::to_string(step)
        + " render pass " + std::to_string(renderPass) + " is not a region");
    return JNI_FALSE;
  }

  const jsize wordCount = env->GetArrayLength(packedColumns);
  if (wordCount != static_cast<jsize>(kPaddedColumnsPerRegion * kWordsPerColumn)) {
    refuse(g_loggedWidthMismatch, "payload is " + std::to_string(wordCount)
        + " ints, this build expects " + std::to_string(kPaddedColumnsPerRegion * kWordsPerColumn)
        + " (" + std::to_string(kPaddedColumnsPerRegion) + " columns x "
        + std::to_string(kWordsPerColumn) + " words) -- the jar and mcrtx_jni.dll"
        " disagree about the column format, so one of them is stale");
    return JNI_FALSE;
  }

  std::vector<LodColumn> columns;
  columns.reserve(kPaddedColumnsPerRegion);
  {
    jint* words = env->GetIntArrayElements(packedColumns, nullptr);
    if (words == nullptr) {
      return JNI_FALSE;
    }
    for (int columnIndex = 0; columnIndex < kPaddedColumnsPerRegion; ++columnIndex) {
      columns.push_back(unpackColumn(words + columnIndex * kWordsPerColumn));
    }
    env->ReleaseIntArrayElements(packedColumns, words, JNI_ABORT);
  }

  const RegionKey key {
      static_cast<int>(originX),
      static_cast<int>(originZ),
      static_cast<int>(step),
      static_cast<int>(renderPass)};

  const bool ok = RemixRenderer::instance().submitLodRegion(key, columns);
  return ok ? JNI_TRUE : JNI_FALSE;
}

JNIEXPORT void JNICALL Java_mcrtx_bridge_RemixLodBridge_nUnloadLodRegion(
    JNIEnv*, jclass, jint originX, jint originZ, jint step) {
  MCRTX_PERF_SCOPE(::mcrtx::perf::Side::Jni, "nUnloadLodRegion");
  RemixRenderer::instance().unloadLodRegion(
      static_cast<int>(originX), static_cast<int>(originZ), static_cast<int>(step));
}

JNIEXPORT void JNICALL Java_mcrtx_bridge_RemixLodBridge_nSetLodRegionHidden(
    JNIEnv*, jclass, jint originX, jint originZ, jint step, jboolean hidden) {
  RemixRenderer::instance().setLodRegionHidden(
      static_cast<int>(originX),
      static_cast<int>(originZ),
      static_cast<int>(step),
      hidden != JNI_FALSE);
}

JNIEXPORT void JNICALL Java_mcrtx_bridge_RemixLodBridge_nClearLodRegions(JNIEnv*, jclass) {
  RemixRenderer::instance().clearLodRegions();
}

JNIEXPORT jint JNICALL Java_mcrtx_bridge_RemixLodBridge_nLodRegionCount(JNIEnv*, jclass) {
  return static_cast<jint>(RemixRenderer::instance().lodRegionCount());
}

JNIEXPORT jint JNICALL Java_mcrtx_bridge_RemixLodBridge_nLodTriangleCount(
    JNIEnv*, jclass, jboolean visibleOnly) {
  return static_cast<jint>(
      RemixRenderer::instance().lodTriangleCount(visibleOnly != JNI_FALSE));
}

JNIEXPORT jint JNICALL Java_mcrtx_bridge_RemixLodBridge_nLodRebuildToken(JNIEnv*, jclass) {
  return static_cast<jint>(RemixRenderer::instance().lodRebuildToken());
}

JNIEXPORT void JNICALL Java_mcrtx_bridge_RemixLodBridge_nLog(
    JNIEnv* env, jclass, jstring message) {
  if (message == nullptr) {
    return;
  }
  const char* text = env->GetStringUTFChars(message, nullptr);
  if (text == nullptr) {
    return;
  }
  RemixRenderer::instance().logPublic(std::string(text));
  env->ReleaseStringUTFChars(message, text);
}

}  // extern "C"
