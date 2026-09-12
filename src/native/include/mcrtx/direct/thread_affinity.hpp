#pragma once
#include <string>
#include <string_view>

namespace mcrtx::direct {
struct ThreadAffinityResult {
  bool applied = false;
  std::string message;
};

// Optional Linux render-worker policy. Never expands the calling thread's
// inherited CPU mask. Empty/default leaves scheduling to the OS.
ThreadAffinityResult setRenderThreadCpus(std::string_view specification);
}
