#include "mcrtx/scene/remix_underground_visibility.hpp"

#include <algorithm>
#include <cmath>
#include <limits>
#include <tuple>

namespace mcrtx {

std::uint64_t UndergroundFrame::topologyFingerprint(const ChunkKey& key) const {
  std::uint64_t hash = 14695981039346656037ull;
  for (int z = -16; z <= 16; z += 16) {
    for (int y = -16; y <= 16; y += 16) {
      for (int x = -16; x <= 16; x += 16) {
        const auto it = sections->find({key.originX + x, key.originY + y, key.originZ + z, 0});
        hash ^= it == sections->end() ? 0 : it->second->revision;
        hash *= 1099511628211ull;
      }
    }
  }
  return hash;
}

bool UndergroundPocket::operator<(const UndergroundPocket& other) const {
  return std::tie(section.originX, section.originY, section.originZ, revision, label)
      < std::tie(other.section.originX, other.section.originY, other.section.originZ, other.revision, other.label);
}

std::vector<UndergroundPocket> UndergroundFrame::findPockets(
    const std::array<double, 3>& min, const std::array<double, 3>& max) const {
  std::array<int, 3> lo, hi;
  for (int axis = 0; axis < 3; ++axis) {
    if (!std::isfinite(min[axis]) || !std::isfinite(max[axis]) || min[axis] > max[axis]
        || min[axis] < -30000000 || max[axis] > 30000000 || max[axis] - min[axis] > 64) {
      return {};
    }
    lo[axis] = static_cast<int>(std::floor(min[axis]));
    hi[axis] = static_cast<int>(std::floor(max[axis]));
  }
  std::vector<UndergroundPocket> result;
  for (int z = lo[2]; z <= hi[2]; ++z) {
    for (int y = lo[1]; y <= hi[1]; ++y) {
      for (int x = lo[0]; x <= hi[0]; ++x) {
        const ChunkKey key {x & ~15, y & ~15, z & ~15, 0};
        const auto it = sections->find(key);
        if (it == sections->end()) {
          return {};
        }
        const auto& section = *it->second;
        const int index = (x & 15) | ((y & 15) << 4) | ((z & 15) << 8);
        const auto label = (*section.labels)[index];
        if (label >= 0) {
          UndergroundPocket pocket {key, section.revision, label};
          if (std::find(result.begin(), result.end(), pocket) == result.end()) {
            result.push_back(pocket);
          }
        }
      }
    }
  }
  std::sort(result.begin(), result.end());
  return result;
}

bool UndergroundFrame::matches(const std::vector<UndergroundPocket>& pockets) const {
  for (const auto& pocket : pockets) {
    const auto it = sections->find(pocket.section);
    if (it == sections->end() || it->second->revision != pocket.revision) {
      return false;
    }
  }
  return true;
}

bool UndergroundFrame::isHidden(const std::vector<UndergroundPocket>& pockets) const {
  if (!enabled || pockets.empty() || !matches(pockets)) {
    return false;
  }
  for (const auto& pocket : pockets) {
    const auto& section = *sections->at(pocket.section);
    if (pocket.label < 0 || pocket.label >= 4096) {
      return false;
    }
    if ((section.hiddenPockets[pocket.label / 64] & (std::uint64_t {1} << (pocket.label % 64))) == 0) {
      return false;
    }
  }
  return true;
}

bool UndergroundFrame::isHidden(const std::array<double, 3>& min, const std::array<double, 3>& max) const {
  return enabled && isHidden(findPockets(min, max));
}

} // namespace mcrtx
