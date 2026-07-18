#pragma once

#include <chrono>
#include <cstdint>

namespace mcrtx::renderer_detail {

inline std::uint64_t toNanoseconds(std::chrono::steady_clock::duration duration) {
  return static_cast<std::uint64_t>(std::chrono::duration_cast<std::chrono::nanoseconds>(duration).count());
}

}  // namespace mcrtx::renderer_detail
