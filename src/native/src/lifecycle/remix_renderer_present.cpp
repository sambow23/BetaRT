// Frame presentation ordering, screenshots, and performance summaries.

#include "mcrtx/core/remix_renderer.hpp"
#include "mcrtx/chunks/remix_chunk_policy.hpp"
#include "mcrtx/lifecycle/remix_renderer_timing.hpp"
#include "mcrtx/core/remix_render_common.hpp"
#include "mcrtx/core/runtime_config.hpp"
#include "mcrtx/lifecycle/perf_log.hpp"

#include <chrono>
#include <cstdint>
#include <iomanip>
#include <mutex>
#include <sstream>
#include <string>

namespace mcrtx {

using namespace mcrtx::detail;
using namespace mcrtx::chunk;
using namespace mcrtx::renderer_detail;


namespace {

constexpr std::uint64_t kPerfLogIntervalFrames = 60;
std::string formatMilliseconds(std::uint64_t nanoseconds) {
  std::ostringstream stream;
  stream << std::fixed << std::setprecision(2)
         << (static_cast<double>(nanoseconds) / 1000000.0);
  return stream.str();
}

std::string formatAverageMilliseconds(const DurationPerfCounter& counter, std::uint64_t fallbackSamples) {
  const std::uint64_t divisor = counter.sampleCount != 0 ? counter.sampleCount : fallbackSamples;
  if (divisor == 0) {
    return "0.00";
  }
  return formatMilliseconds(counter.totalNanoseconds / divisor);
}

std::string formatAverageCount(const CountPerfCounter& counter, std::uint64_t fallbackSamples) {
  const std::uint64_t divisor = counter.sampleCount != 0 ? counter.sampleCount : fallbackSamples;
  std::ostringstream stream;
  stream << std::fixed << std::setprecision(2)
         << (divisor == 0 ? 0.0 : static_cast<double>(counter.totalCount) / static_cast<double>(divisor));
  return stream.str();
}

void appendDurationSummary(
    std::ostringstream& stream,
    const char* label,
    const DurationPerfCounter& counter,
    std::uint64_t fallbackSamples) {
  stream << ' ' << label << "AvgMs=" << formatAverageMilliseconds(counter, fallbackSamples)
         << ' ' << label << "MaxMs=" << formatMilliseconds(counter.maxNanoseconds);
}

void appendCountSummary(
    std::ostringstream& stream,
    const char* label,
    const CountPerfCounter& counter,
    std::uint64_t fallbackSamples) {
  stream << ' ' << label << "Avg=" << formatAverageCount(counter, fallbackSamples)
         << ' ' << label << "Max=" << counter.maxCount;
}

}  // namespace

bool RemixRenderer::present() {
  MCRTX_PERF_SCOPE(::mcrtx::perf::Side::Native, "RemixRenderer::present");
  MCRTX_TRACY_SCOPE("RemixRenderer::present");
  const auto lockRequestedAt = std::chrono::steady_clock::now();
  TracyUniqueLock lock(mutex_);
  const auto lockAcquiredAt = std::chrono::steady_clock::now();
  MCRTX_TRACY_LOCK_MARK(mutex_);

  if (standaloneOutputWindow_) {
    if (!initialized_) {
      setError("present called before initialize");
      return false;
    }
    standaloneWorkerPresentRequested_ = true;
    standaloneWorkerEvent_.notify_all();
    return true;
  }

  std::string perfSummary;
  const bool ok = presentLocked(
      lock,
      perfSummary,
      toNanoseconds(lockAcquiredAt - lockRequestedAt));
  lock.unlock();
  if (!perfSummary.empty()) {
    log(perfSummary);
  }
  ::mcrtx::perf::onFramePresented();
  return ok;
}

bool RemixRenderer::requestPresentedScreenshot(const std::string& absolutePath) {
  MCRTX_PERF_SCOPE(::mcrtx::perf::Side::Native, "RemixRenderer::requestPresentedScreenshot");
  MCRTX_TRACY_SCOPE("RemixRenderer::requestPresentedScreenshot");
  std::scoped_lock lock(mutex_);

  if (!initialized_) {
    setError("requestPresentedScreenshot called before initialize");
    return false;
  }

  if (absolutePath.empty()) {
    setError("Screenshot path is empty");
    return false;
  }

  if (remix_.RequestPresentedScreenshot == nullptr) {
    setError("Loaded Remix runtime does not support presented screenshot requests");
    return false;
  }

  const remixapi_ErrorCode result = [&]() {
    MCRTX_PERF_SCOPE(::mcrtx::perf::Side::Remix, "RequestPresentedScreenshot");
    return remix_.RequestPresentedScreenshot(absolutePath.c_str());
  }();
  if (result != REMIXAPI_ERROR_CODE_SUCCESS) {
    setError("RequestPresentedScreenshot failed: " + errorCodeToString(result));
    return false;
  }

  return true;
}

bool RemixRenderer::presentLocked(TracyUniqueLock& lock,
                                  std::string& perfSummary,
                                  std::uint64_t lockWaitNanoseconds) {
  MCRTX_PERF_SCOPE(::mcrtx::perf::Side::Native, "RemixRenderer::presentLocked");
  MCRTX_TRACY_SCOPE("RemixRenderer::presentLocked");
  const auto lockAcquiredAt = std::chrono::steady_clock::now();
  MCRTX_TRACY_LOCK_MARK(mutex_);

  if (!initialized_) {
    setError("present called before initialize");
    return false;
  }

  const auto outputWindowStart = std::chrono::steady_clock::now();
  {
    MCRTX_PERF_SCOPE(::mcrtx::perf::Side::Native, "presentLocked.outputWindow");
    MCRTX_TRACY_SCOPE("presentLocked.outputWindow");
    updateOutputWindowSize();
    pumpOutputWindowMessages();
  }
  const auto outputWindowEnd = std::chrono::steady_clock::now();

  FrameRenderSnapshot snapshot;
  bool logNoCapturedScene = false;
  {
    MCRTX_PERF_SCOPE(::mcrtx::perf::Side::Native, "presentLocked.prepareSnapshot");
    MCRTX_TRACY_SCOPE("presentLocked.prepareSnapshot");
    if (!prepareFrameSnapshotLocked(snapshot, logNoCapturedScene)) {
      return false;
    }
  }

  publishWorldRenderOriginLocked(snapshot.renderOrigin);

  if (!chunkBuildActive_) {
    MCRTX_PERF_SCOPE(::mcrtx::perf::Side::Native, "presentLocked.evictDistantChunks");
    MCRTX_TRACY_SCOPE("presentLocked.evictDistantChunks");
    const int cameraChunkX = static_cast<int>(camera_.position[0]) / kChunkDimension;
    const int cameraChunkZ = static_cast<int>(camera_.position[2]) / kChunkDimension;
    evictDistantChunks(cameraChunkX, cameraChunkZ, evictRadiusChunks_);
  }

  std::uint64_t lockHoldNanoseconds = toNanoseconds(std::chrono::steady_clock::now() - lockAcquiredAt);
  lock.unlock();

  const auto cameraSubmitStart = std::chrono::steady_clock::now();
  if (!submitCamera(snapshot.camera)) {
    lock.lock();
    renderSubmissionInFlight_ = false;
    flushDeferredDestroyQueuesLocked();
    return false;
  }
  const auto cameraSubmitEnd = std::chrono::steady_clock::now();

  const auto geometrySubmitStart = std::chrono::steady_clock::now();
  if (!drawCapturedGeometry(snapshot)) {
    lock.lock();
    renderSubmissionInFlight_ = false;
    flushDeferredDestroyQueuesLocked();
    return false;
  }
  const auto geometrySubmitEnd = std::chrono::steady_clock::now();

  submitSyntheticUiTest();

  const auto remixPresentStart = std::chrono::steady_clock::now();
  const remixapi_ErrorCode result = [&]() {
    MCRTX_PERF_SCOPE(::mcrtx::perf::Side::Remix, "Present");
    MCRTX_TRACY_SCOPE("Present");
    return remix_.Present(nullptr);
  }();
  const auto remixPresentEnd = std::chrono::steady_clock::now();
  if (result != REMIXAPI_ERROR_CODE_SUCCESS) {
    lock.lock();
    renderSubmissionInFlight_ = false;
    flushDeferredDestroyQueuesLocked();
    setError("Present failed: " + errorCodeToString(result));
    return false;
  }

  const auto uiSyncStart = std::chrono::steady_clock::now();
  remixapi_UIState uiState {};
  bool hasUiState = false;
  if (remix_.GetUIState != nullptr) {
    MCRTX_PERF_SCOPE(::mcrtx::perf::Side::Remix, "GetUIState.presentSync");
    MCRTX_TRACY_SCOPE("GetUIState.presentSync");
    uiState = remix_.GetUIState();
    hasUiState = true;
  }

  lock.lock();
  MCRTX_TRACY_LOCK_MARK(mutex_);
  const auto secondLockAcquiredAt = std::chrono::steady_clock::now();
  {
    MCRTX_PERF_SCOPE(::mcrtx::perf::Side::Native, "presentLocked.postPresent");
    MCRTX_TRACY_SCOPE("presentLocked.postPresent");
    renderSubmissionInFlight_ = false;
    flushDeferredDestroyQueuesLocked();
    if (hasUiState) {
      syncOutputWindowInteractivity(uiState);
    }

    perfCachedChunkMeshesThisFrame_ = snapshot.cachedChunkMeshes;
    perfSubmittedChunkMeshesThisFrame_ = snapshot.chunkMeshes.size();
    perfSubmittedChunkBlocksThisFrame_ = snapshot.submittedChunkBlocks;

    if (logNoCapturedScene) {
      log("No captured scene meshes available yet");
    }
  }

  const bool submissionCountsChanged = snapshot.chunkMeshes.size() != lastSubmittedChunkCount_
      || snapshot.submittedChunkBlocks != lastSubmittedBlockCount_
      || snapshot.submittedCloudQuads != lastSubmittedCloudQuadCount_
      || snapshot.submittedFireQuads != lastSubmittedFireQuadCount_
      || snapshot.submittedDynamicEntityQuads != lastSubmittedDynamicEntityQuadCount_
      || snapshot.submittedDynamicEntityDrawCalls != lastSubmittedDynamicEntityDrawCallCount_
      || snapshot.submittedDynamicEntityFallbackDrawCalls != lastSubmittedDynamicEntityFallbackDrawCallCount_
      || snapshot.submittedDynamicEntityInstancedDrawCalls != lastSubmittedDynamicEntityInstancedDrawCallCount_
      || snapshot.submittedDynamicEntityInstancedTransforms != lastSubmittedDynamicEntityInstancedTransformCount_
      || snapshot.submittedDynamicEntityRigidCandidates != lastSubmittedDynamicEntityRigidCandidateCount_
      || snapshot.submittedDynamicEntitySingletonRigidFallbacks != lastSubmittedDynamicEntitySingletonRigidFallbackCount_
      || snapshot.submittedDynamicEntitySkinnedFallbacks != lastSubmittedDynamicEntitySkinnedFallbackCount_
      || snapshot.submittedDestroyOverlays != lastSubmittedDestroyOverlayCount_
      || snapshot.submittedBlockOutlines != lastSubmittedBlockOutlineCount_
      || snapshot.submittedParticleQuads != lastSubmittedParticleQuadCount_
      || snapshot.submittedTorchLights != lastSubmittedTorchLightCount_;
  const bool hasMeaningfulSubmissionSummary = snapshot.chunkMeshes.size() != 0
      || snapshot.submittedChunkBlocks != 0
      || snapshot.submittedCloudQuads != 0
      || snapshot.submittedFireQuads != 0
      || snapshot.submittedDynamicEntityQuads != 0
      || snapshot.submittedDynamicEntityDrawCalls != 0
      || snapshot.submittedDestroyOverlays != 0
      || snapshot.submittedBlockOutlines != 0
      || snapshot.submittedParticleQuads != 0
      || snapshot.submittedTorchLights != 0;
  const bool shouldLogPopulatedSubmissionSummary = hasMeaningfulSubmissionSummary
      && loggedPopulatedSubmissionSummaryCount_ < 8;
  const bool shouldLogSubmissionSummary = presentedFrames_ < 8
      || shouldLogPopulatedSubmissionSummary;
  if (submissionCountsChanged && shouldLogSubmissionSummary) {
    std::ostringstream stream;
    stream << "Submitted " << snapshot.chunkMeshes.size()
           << " chunk meshes covering " << snapshot.submittedChunkBlocks
           << " blocks and " << snapshot.submittedCloudQuads
           << " cloud quads and " << snapshot.submittedFireQuads
           << " fire quads and " << snapshot.submittedDynamicEntityQuads
           << " dynamic entity quads across " << snapshot.submittedDynamicEntityDrawCalls
           << " draw calls (fallback=" << snapshot.submittedDynamicEntityFallbackDrawCalls
           << ", instancedDraws=" << snapshot.submittedDynamicEntityInstancedDrawCalls
           << ", instancedTransforms=" << snapshot.submittedDynamicEntityInstancedTransforms
           << ", rigidCandidates=" << snapshot.submittedDynamicEntityRigidCandidates
           << ", singletonRigidFallbacks=" << snapshot.submittedDynamicEntitySingletonRigidFallbacks
           << ", skinnedFallbacks=" << snapshot.submittedDynamicEntitySkinnedFallbacks
           << ") and " << snapshot.submittedDestroyOverlays
           << " destroy overlays and " << snapshot.submittedBlockOutlines
           << " block outlines and "
           << snapshot.submittedParticleQuads << " particle quads and "
           << snapshot.submittedTorchLights
           << " torch lights";
    log(stream.str());
    if (hasMeaningfulSubmissionSummary) {
      ++loggedPopulatedSubmissionSummaryCount_;
    }
  }

  lastSubmittedChunkCount_ = snapshot.chunkMeshes.size();
  lastSubmittedBlockCount_ = snapshot.submittedChunkBlocks;
  lastSubmittedCloudQuadCount_ = snapshot.submittedCloudQuads;
  lastSubmittedFireQuadCount_ = snapshot.submittedFireQuads;
  lastSubmittedDynamicEntityQuadCount_ = snapshot.submittedDynamicEntityQuads;
  lastSubmittedDynamicEntityDrawCallCount_ = snapshot.submittedDynamicEntityDrawCalls;
  lastSubmittedDynamicEntityFallbackDrawCallCount_ = snapshot.submittedDynamicEntityFallbackDrawCalls;
  lastSubmittedDynamicEntityInstancedDrawCallCount_ = snapshot.submittedDynamicEntityInstancedDrawCalls;
  lastSubmittedDynamicEntityInstancedTransformCount_ = snapshot.submittedDynamicEntityInstancedTransforms;
  lastSubmittedDynamicEntityRigidCandidateCount_ = snapshot.submittedDynamicEntityRigidCandidates;
  lastSubmittedDynamicEntitySingletonRigidFallbackCount_ = snapshot.submittedDynamicEntitySingletonRigidFallbacks;
  lastSubmittedDynamicEntitySkinnedFallbackCount_ = snapshot.submittedDynamicEntitySkinnedFallbacks;
  lastSubmittedDestroyOverlayCount_ = snapshot.submittedDestroyOverlays;
  lastSubmittedBlockOutlineCount_ = snapshot.submittedBlockOutlines;
  lastSubmittedParticleQuadCount_ = snapshot.submittedParticleQuads;
  lastSubmittedTorchLightCount_ = snapshot.submittedTorchLights;
  ++presentedFrames_;
  MCRTX_TRACY_FRAME_MARK();

  const auto frameEnd = std::chrono::steady_clock::now();
  const auto cameraSubmitNanoseconds = toNanoseconds(cameraSubmitEnd - cameraSubmitStart);
  const auto geometrySubmitNanoseconds = toNanoseconds(geometrySubmitEnd - geometrySubmitStart);
  const auto uiSyncNanoseconds = toNanoseconds(frameEnd - uiSyncStart);
  perfWindow_.frames += 1;
  perfWindow_.presentLockWait.add(lockWaitNanoseconds);
  lockHoldNanoseconds += toNanoseconds(frameEnd - secondLockAcquiredAt);
  perfWindow_.presentLockHold.add(lockHoldNanoseconds);
  perfWindow_.outputWindowWork.add(toNanoseconds(outputWindowEnd - outputWindowStart));
  perfWindow_.cameraSubmit.add(cameraSubmitNanoseconds);
  perfWindow_.geometrySubmit.add(geometrySubmitNanoseconds);
  perfWindow_.remixPresent.add(toNanoseconds(remixPresentEnd - remixPresentStart));
  perfWindow_.uiStateSync.add(uiSyncNanoseconds);
  perfWindow_.frameCaptureBlockCalls.add(perfCaptureBlockCallsThisFrame_);
  perfWindow_.frameCachedChunkMeshes.add(perfCachedChunkMeshesThisFrame_);
  perfWindow_.frameChunkBuilds.add(perfChunkBuildsThisFrame_);
  perfWindow_.frameChunkMeshRebuilds.add(perfChunkMeshRebuildsThisFrame_);
  perfWindow_.frameSubmittedChunkMeshes.add(perfSubmittedChunkMeshesThisFrame_);
  perfWindow_.frameSubmittedChunkBlocks.add(perfSubmittedChunkBlocksThisFrame_);
  perfWindow_.frameChunkBuildWork.add(perfChunkBuildWorkNanosThisFrame_);
  perfWindow_.frameChunkMeshRebuildWork.add(perfChunkMeshRebuildNanosThisFrame_);
  perfWindow_.frameNeighborRefreshWork.add(perfNeighborRefreshNanosThisFrame_);
    ::mcrtx::perf::recordDuration(
      ::mcrtx::perf::Side::Native, "presentLocked.lockWait", lockWaitNanoseconds);
    ::mcrtx::perf::recordDuration(
      ::mcrtx::perf::Side::Native, "presentLocked.lockHold", lockHoldNanoseconds);
    ::mcrtx::perf::recordDuration(
      ::mcrtx::perf::Side::Native, "presentLocked.cameraSubmit", cameraSubmitNanoseconds);
    ::mcrtx::perf::recordDuration(
      ::mcrtx::perf::Side::Native, "presentLocked.geometrySubmit", geometrySubmitNanoseconds);

  NativePerfWindow summaryWindow {};
  const bool shouldLogSummary = perfWindow_.frames >= kPerfLogIntervalFrames;
  if (shouldLogSummary) {
    summaryWindow = perfWindow_;
    perfWindow_.reset();
  }

  resetPerFramePerfCounters();

  if (shouldLogSummary && isVerboseLoggingEnabled()) {
    std::ostringstream stream;
    stream << "perf native frames=" << summaryWindow.frames;
    appendDurationSummary(stream, "lockWait", summaryWindow.presentLockWait, summaryWindow.frames);
    appendDurationSummary(stream, "lockHold", summaryWindow.presentLockHold, summaryWindow.frames);
    appendDurationSummary(stream, "window", summaryWindow.outputWindowWork, summaryWindow.frames);
    appendDurationSummary(stream, "camera", summaryWindow.cameraSubmit, summaryWindow.frames);
    appendDurationSummary(stream, "draw", summaryWindow.geometrySubmit, summaryWindow.frames);
    appendDurationSummary(stream, "present", summaryWindow.remixPresent, summaryWindow.frames);
    appendDurationSummary(stream, "uiSync", summaryWindow.uiStateSync, summaryWindow.frames);
    appendDurationSummary(stream, "chunkBuild", summaryWindow.frameChunkBuildWork, summaryWindow.frames);
    appendDurationSummary(stream, "chunkRebuild", summaryWindow.frameChunkMeshRebuildWork, summaryWindow.frames);
    appendDurationSummary(stream, "neighborRefresh", summaryWindow.frameNeighborRefreshWork, summaryWindow.frames);
    appendCountSummary(stream, "captureBlocks", summaryWindow.frameCaptureBlockCalls, summaryWindow.frames);
    appendCountSummary(stream, "cachedChunks", summaryWindow.frameCachedChunkMeshes, summaryWindow.frames);
    appendCountSummary(stream, "chunkBuilds", summaryWindow.frameChunkBuilds, summaryWindow.frames);
    appendCountSummary(stream, "chunkRebuilds", summaryWindow.frameChunkMeshRebuilds, summaryWindow.frames);
    appendCountSummary(stream, "submittedChunks", summaryWindow.frameSubmittedChunkMeshes, summaryWindow.frames);
    appendCountSummary(stream, "submittedBlocks", summaryWindow.frameSubmittedChunkBlocks, summaryWindow.frames);
    perfSummary = stream.str();
  }

  return true;
}

}  // namespace mcrtx
