#pragma once

#include <cstdint>

namespace mcrtx {

constexpr std::uint32_t kDynamicEntityMaxCreeperFuseStage = 10;
constexpr float kDynamicEntityCreeperFuseMaxEmissiveIntensity = 0.5f;

constexpr std::uint32_t clampDynamicEntityCreeperFuseStage(std::uint32_t creeperFuseStage) {
  return creeperFuseStage > kDynamicEntityMaxCreeperFuseStage
      ? kDynamicEntityMaxCreeperFuseStage
      : creeperFuseStage;
}

constexpr bool isDynamicEntityCreeperFuseFlashStage(std::uint32_t creeperFuseStage) {
  return (clampDynamicEntityCreeperFuseStage(creeperFuseStage) & 1u) != 0u;
}

constexpr float dynamicEntityCreeperFuseEmissiveIntensity(std::uint32_t creeperFuseStage) {
  const std::uint32_t clampedStage = clampDynamicEntityCreeperFuseStage(creeperFuseStage);
  if (!isDynamicEntityCreeperFuseFlashStage(clampedStage)) {
    return 0.0f;
  }

  return kDynamicEntityCreeperFuseMaxEmissiveIntensity
      * (static_cast<float>(clampedStage) / static_cast<float>(kDynamicEntityMaxCreeperFuseStage));
}

}  // namespace mcrtx
