#pragma once

#include <array>
#include <cstdint>
#include <memory>
#include <unordered_map>
#include <vector>

#include "mcrtx/scene/remix_renderer_scene.hpp"

namespace mcrtx {

struct UndergroundSection {
  using Labels = std::array<std::int16_t, 4096>;
  std::uint64_t revision {0};
  std::shared_ptr<const Labels> labels = std::make_shared<Labels>();
  std::array<std::uint64_t, 64> hiddenPockets {};
};

struct UndergroundFrame {
  using Sections = std::unordered_map<ChunkKey, std::shared_ptr<const UndergroundSection>, ChunkKeyHash>;
  bool enabled {false};
  std::uint64_t revision {0};
  std::uint64_t topologyRevision {0};
  std::shared_ptr<Sections> sections = std::make_shared<Sections>();

  Sections& editSections() {
    if (!sections.unique()) {
      sections = std::make_shared<Sections>(*sections);
    }
    return *sections;
  }

  bool isHidden(const std::vector<UndergroundPocket>& pockets) const;
  bool isHidden(const std::array<double, 3>& min, const std::array<double, 3>& max) const;
  std::vector<UndergroundPocket> findPockets(const std::array<double, 3>& min, const std::array<double, 3>& max) const;
  bool matches(const std::vector<UndergroundPocket>& pockets) const;
  std::uint64_t topologyFingerprint(const ChunkKey& key) const;
};

} // namespace mcrtx
