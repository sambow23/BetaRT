#include "mcrtx/core/remix_renderer.hpp"

#include <atomic>
#include <chrono>
#include <cstdlib>
#include <iostream>
#include <thread>

namespace mcrtx {
class StandaloneRenderTest {
  inline static std::atomic<bool> minimized {false};
  inline static std::atomic<bool> emptyExtent {false};
  inline static std::atomic<bool> failPresent {false};
  inline static std::atomic<int> pumps {0};
  inline static std::atomic<int> calls {0};
  inline static std::atomic<bool> probePublisher {false};
  inline static std::atomic<bool> publisherFinished {false};
  inline static std::atomic<int> presentDelayMs {0};
  inline static std::atomic<int> delayedPresents {0};
  inline static std::mutex apiMutex;
  inline static int fogCalls {0};
  inline static int uiCalls {0};
  inline static float fogEnd {0};
  inline static float uiVertex {0};
  inline static bool failUi {false};
  inline static RemixRenderer* renderer = nullptr;

  static void require(bool condition, const char* message) {
    if (!condition) {
      std::cerr << message << '\n';
      std::exit(1);
    }
  }
  static remixapi_ErrorCode REMIXAPI_CALL pump() {
    ++pumps;
    return REMIXAPI_ERROR_CODE_SUCCESS;
  }
  static remixapi_ErrorCode REMIXAPI_CALL window(remixapi_WindowState* state) {
    *state = {};
    state->minimized = minimized;
    state->drawableWidth = emptyExtent ? 0 : 640;
    state->drawableHeight = emptyExtent ? 0 : 360;
    return REMIXAPI_ERROR_CODE_SUCCESS;
  }
  static remixapi_ErrorCode REMIXAPI_CALL mouse(remixapi_MouseState* state) {
    *state = {};
    return REMIXAPI_ERROR_CODE_SUCCESS;
  }
  static remixapi_ErrorCode REMIXAPI_CALL key(remixapi_Key, remixapi_Bool* down) {
    *down = 0;
    return REMIXAPI_ERROR_CODE_SUCCESS;
  }
  static remixapi_ErrorCode REMIXAPI_CALL camera(const remixapi_CameraInfo*) {
    return REMIXAPI_ERROR_CODE_SUCCESS;
  }
  static remixapi_ErrorCode REMIXAPI_CALL fog(const remixapi_FogInfo* info) {
    std::scoped_lock lock(apiMutex);
    ++fogCalls;
    fogEnd = info->end;
    return REMIXAPI_ERROR_CODE_SUCCESS;
  }
  static remixapi_ErrorCode REMIXAPI_CALL tint(float, float, float, float) {
    std::scoped_lock lock(apiMutex);
    return REMIXAPI_ERROR_CODE_SUCCESS;
  }
  static remixapi_ErrorCode REMIXAPI_CALL gameValue(const char*, const char*) {
    std::scoped_lock lock(apiMutex);
    return REMIXAPI_ERROR_CODE_SUCCESS;
  }
  static remixapi_UIState REMIXAPI_CALL uiState() {
    std::scoped_lock lock(apiMutex);
    return REMIXAPI_UI_STATE_BASIC;
  }
  static remixapi_ErrorCode REMIXAPI_CALL ui(const remixapi_UIDrawList* list) {
    std::scoped_lock lock(apiMutex);
    ++uiCalls;
    uiVertex = list != nullptr && list->vertexCount != 0 ? list->pVertices[0].x : -1.0f;
    return failUi ? REMIXAPI_ERROR_CODE_GENERAL_FAILURE : REMIXAPI_ERROR_CODE_SUCCESS;
  }
  static void capture(RemixRenderer& state, float value) {
    for (int i = 0; i < 7; ++i) {
      state.updateFogState(0, 1, 1, 1, 0, value, 0);
      state.setScreenTint(1, 1, 1, 0);
    }
    state.updateAtmosphereState(value, false);
    remixapi_UIVertex vertex {};
    vertex.x = value;
    std::uint32_t index = 0;
    remixapi_UIDrawCommand command {};
    command.indexCount = 1;
    remixapi_UIDrawList list {};
    list.sType = REMIXAPI_STRUCT_TYPE_UI_DRAW_LIST;
    list.displayWidth = 640;
    list.displayHeight = 360;
    list.pVertices = &vertex;
    list.vertexCount = 1;
    list.pIndices = &index;
    list.indexCount = 1;
    list.pCommands = &command;
    list.commandCount = 1;
    require(state.submitUiDrawList(&list), "UI capture failed");
    vertex.x = -100;
    index = 100;
    command.indexCount = 100;
  }
  static remixapi_ErrorCode REMIXAPI_CALL present(const remixapi_PresentInfo*) {
    if (probePublisher.exchange(false)) {
      // Simulate Present holding Remix's API lock while it waits for the GPU.
      std::scoped_lock apiLock(apiMutex);
      std::thread publisher([] {
        capture(*renderer, 4);
        require(renderer->getUiState() == REMIXAPI_UI_STATE_BASIC, "UI state was not cached");
        renderer->present();
        publisherFinished = true;
      });
      const auto deadline = std::chrono::steady_clock::now() + std::chrono::milliseconds(250);
      while (!publisherFinished && std::chrono::steady_clock::now() < deadline) {
        std::this_thread::sleep_for(std::chrono::milliseconds(1));
      }
      require(publisherFinished, "Game frame capture waited for GPU presentation");
      publisher.join();
    }
    // The game thread must be able to publish while native rendering is underway.
    std::thread publisher([] {
      std::scoped_lock lock(renderer->mutex_);
    });
    publisher.join();
    ++calls;
    const auto delay = presentDelayMs.load();
    if (delay != 0) {
      std::this_thread::sleep_for(std::chrono::milliseconds(delay));
      ++delayedPresents;
    }
    return failPresent ? REMIXAPI_ERROR_CODE_GENERAL_FAILURE : REMIXAPI_ERROR_CODE_SUCCESS;
  }
  template<typename Predicate>
  static void waitFor(Predicate predicate) {
    const auto deadline = std::chrono::steady_clock::now() + std::chrono::seconds(2);
    while (!predicate() && std::chrono::steady_clock::now() < deadline) {
      std::this_thread::sleep_for(std::chrono::milliseconds(1));
    }
    require(predicate(), "Renderer made no progress");
  }
public:
  static void run() {
    RemixRenderer state;
    renderer = &state;
    state.initialized_ = true;
    state.standaloneOutputWindow_ = true;
    state.remix_.PumpEvents = pump;
    state.remix_.GetWindowState = window;
    state.remix_.PollMouseState = mouse;
    state.remix_.IsKeyDown = key;
    state.remix_.SetupCamera = camera;
    state.remix_.Present = present;
    state.remix_.SetFogState = fog;
    state.remix_.SetScreenTint = tint;
    state.remix_.GetUIState = uiState;
    state.remix_.SubmitUIDrawList = ui;
    state.remix_.SetGameValue = gameValue;
    capture(state, 1);
    require(fogCalls == 0 && uiCalls == 0, "Capture called Remix before publication");
    state.present();
    capture(state, 2);
    require(state.submitGameFrameStateLocked(), "Published frame submission failed");
    require(fogEnd == 1 && uiVertex == 1, "Published frame did not own a consistent copy");
    require(fogCalls == 1 && uiCalls == 1, "Fog calls were not coalesced");
    require(state.submitGameFrameStateLocked() && fogCalls == 1 && uiCalls == 1,
      "A repeated native frame resubmitted unchanged game state");
    state.present();
    capture(state, 3);
    state.present();
    failUi = true;
    require(!state.submitGameFrameStateLocked(), "Failed UI submission was accepted");
    require(state.submittedGameFrameRevision_ != state.publishedGameFrameRevision_,
      "Failed game frame was discarded");
    failUi = false;
    require(state.submitGameFrameStateLocked() && fogEnd == 3 && uiVertex == 3,
      "Latest frame was not retained for retry");
    require(state.submitUiDrawList(nullptr), "Empty UI capture failed");
    state.present();
    require(state.submitGameFrameStateLocked() && uiVertex == -1, "Explicit empty UI was lost");
    const auto start = std::chrono::steady_clock::now();
    std::thread worker([&] { state.renderStandaloneFrames(); });
    waitFor([&] { return state.submittedFrameCount() >= 60; });
    require(std::chrono::steady_clock::now() - start < std::chrono::milliseconds(800),
      "Active output still has an artificial per-frame delay");
    probePublisher = true;
    waitFor([] { return publisherFinished.load(); });
    const auto publicationStart = std::chrono::steady_clock::now();
    for (int i = 0; i < 60; ++i) {
      capture(state, float(i));
      state.present();
    }
    require(std::chrono::steady_clock::now() - publicationStart < std::chrono::milliseconds(800),
      "A ready native renderer did not wake the game publisher");
    presentDelayMs = 10;
    waitFor([] { return delayedPresents > 0; });
    const auto nativeStart = state.submittedFrameCount();
    const auto overlapStart = std::chrono::steady_clock::now();
    int javaFrames = 0;
    do {
      // A Java frame just slower than native rendering must not be paced down to half rate.
      std::this_thread::sleep_for(std::chrono::milliseconds(11));
      capture(state, float(javaFrames));
      state.present();
      ++javaFrames;
    } while (std::chrono::steady_clock::now() - overlapStart < std::chrono::seconds(1));
    const auto nativeFrames = state.submittedFrameCount() - nativeStart;
    require(nativeFrames >= 50 && javaFrames * 100 >= nativeFrames * 80,
      "Java capture did not overlap CPU-bound native frames");
    presentDelayMs = 0;
    minimized = true;
    waitFor([&] { std::scoped_lock lock(state.mutex_); return state.outputSuspended_; });
    const auto count = state.submittedFrameCount();
    const auto beforePumps = pumps.load();
    const auto idleStart = std::chrono::steady_clock::now();
    for (int i = 0; i < 100; ++i) {
      state.present();
      std::this_thread::sleep_for(std::chrono::milliseconds(1));
    }
    require(state.submittedFrameCount() == count, "Minimized output counted skipped frames");
    const auto idleMs = std::chrono::duration_cast<std::chrono::milliseconds>(
        std::chrono::steady_clock::now() - idleStart).count();
    require(pumps - beforePumps <= idleMs / 16 + 4, "Java notifications spin the minimized worker");
    emptyExtent = true;
    minimized = false;
    std::this_thread::sleep_for(std::chrono::milliseconds(50));
    require(state.submittedFrameCount() == count, "Zero drawable extent submitted frames");
    failPresent = true;
    emptyExtent = false;
    const auto beforeCalls = calls.load();
    waitFor([&] { return calls > beforeCalls; });
    require(state.submittedFrameCount() == count, "Failed present counted a frame");
    failPresent = false;
    waitFor([&] { return state.submittedFrameCount() > count + 3; });
    minimized = true;
    waitFor([&] { std::scoped_lock lock(state.mutex_); return state.outputSuspended_; });
    {
      std::scoped_lock lock(state.mutex_);
      state.standaloneWorkerStopRequested_ = true;
      state.standaloneWorkerEvent_.notify_all();
    }
    worker.join();
    const auto stopped = state.submittedFrameCount();
    require(stopped > count && !state.renderSubmissionInFlight_, "Shutdown left a submission in flight");
    state.standaloneOutputWindow_ = false;
    const auto synchronousFogCalls = fogCalls;
    capture(state, 9);
    require(fogCalls == synchronousFogCalls + 7 && fogEnd == 9 && uiVertex == 9,
      "Synchronous frame calls were incorrectly deferred");
    require(state.getUiState() == REMIXAPI_UI_STATE_BASIC, "Synchronous UI query failed");
    state.standaloneOutputWindow_ = true;
    state.shutdownLocked();
    require(!state.gameFrameState_.ui && !state.publishedGameFrameState_.ui
        && state.publishedGameFrameRevision_ == 0 && state.submittedGameFrameRevision_ == 0
        && state.remixUiState_ == REMIXAPI_UI_STATE_NONE, "Shutdown retained game frame state");
    renderer = nullptr;
  }
};
}
int main() {
  mcrtx::StandaloneRenderTest::run();
}
