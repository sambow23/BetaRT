#pragma once

#include <array>
#include <cstdint>
#include <filesystem>
#include <span>
#include <vector>

#include <remix/remix_c.h>

namespace mcrtx {

struct CloudMask {
  int width {0};
  int height {0};
  std::vector<std::uint8_t> alpha;

  bool valid() const;
  bool occupied(int x, int z) const;
};

CloudMask decodeCloudMask(std::span<const std::uint8_t> bytes);
CloudMask loadCloudMask(const std::filesystem::path& path);

void appendFancyCloudGeometry(
    const CloudMask& mask, int phaseX, int phaseZ, const std::array<float, 3>& color,
    std::vector<remixapi_HardcodedVertex>& vertices, std::vector<std::uint32_t>& indices);

}  // namespace mcrtx
