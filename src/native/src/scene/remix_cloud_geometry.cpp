#include "mcrtx/scene/remix_cloud_geometry.hpp"
#include "mcrtx/core/remix_geometry_common.hpp"
#include "mcrtx/core/remix_render_common.hpp"

#include <algorithm>
#include <cmath>

namespace mcrtx {

void appendFancyCloudGeometry(
    const CloudMask& mask, int phaseX, int phaseZ, const std::array<float, 3>& color,
    std::vector<remixapi_HardcodedVertex>& vertices, std::vector<std::uint32_t>& indices) {
  if (!mask.valid()) {
    return;
  }
  phaseX = (phaseX % 256 + 256) % 256;
  phaseZ = (phaseZ % 256 + 256) % 256;
  const int startX = static_cast<int>(std::floor(double(phaseX - 16) * mask.width / 256));
  const int startZ = static_cast<int>(std::floor(double(phaseZ - 16) * mask.height / 256));
  const int sizeX = static_cast<int>(std::ceil(double(phaseX + 32) * mask.width / 256)) - startX;
  const int sizeZ = static_cast<int>(std::ceil(double(phaseZ + 32) * mask.height / 256)) - startZ;
  std::vector<bool> occupied(std::size_t(sizeX) * sizeZ);
  for (int z = 0; z < sizeZ; ++z) {
    for (int x = 0; x < sizeX; ++x) {
      occupied[z * sizeX + x] = mask.occupied(startX + x, startZ + z);
    }
  }
  const auto filled = [&](int x, int z) {
    return x >= 0 && x < sizeX && z >= 0 && z < sizeZ && occupied[z * sizeX + x];
  };
  const auto px = [&](int x) {
    return std::clamp((float(startX + x) * 256.0f / mask.width - phaseX) * 12.0f, -192.0f, 384.0f);
  };
  const auto pz = [&](int z) {
    return std::clamp((float(startZ + z) * 256.0f / mask.height - phaseZ) * 12.0f, -192.0f, 384.0f);
  };
  const auto u = [&](int x) { return (phaseX + px(x) / 12.0f) / 256.0f; };
  const auto v = [&](int z) { return (phaseZ + pz(z) / 12.0f) / 256.0f; };
  const auto emit = [&](const std::array<std::array<float, 5>, 4>& q, float nx, float ny, float nz, float shade) {
    geometry::appendCloudQuad(
        q[0][0], q[0][1], q[0][2], q[0][3], q[0][4],
        q[1][0], q[1][1], q[1][2], q[1][3], q[1][4],
        q[2][0], q[2][1], q[2][2], q[2][3], q[2][4],
        q[3][0], q[3][1], q[3][2], q[3][3], q[3][4], nx, ny, nz,
        detail::packVertexColorRgba(color[0] * shade, color[1] * shade, color[2] * shade, 1.0f), vertices, indices);
  };

  std::vector<bool> used(occupied.size());
  for (int z = 0; z < sizeZ; ++z) {
    for (int x = 0; x < sizeX; ++x) {
      if (!filled(x, z) || used[z * sizeX + x]) {
        continue;
      }
      int endX = x + 1;
      while (endX < sizeX && filled(endX, z) && !used[z * sizeX + endX]) {
        ++endX;
      }
      int endZ = z + 1;
      for (; endZ < sizeZ; ++endZ) {
        bool complete = true;
        for (int ix = x; ix < endX; ++ix) {
          complete = complete && filled(ix, endZ) && !used[endZ * sizeX + ix];
        }
        if (!complete) {
          break;
        }
      }
      for (int iz = z; iz < endZ; ++iz) {
        for (int ix = x; ix < endX; ++ix) {
          used[iz * sizeX + ix] = true;
        }
      }
      emit({{{px(x), 0, pz(z), u(x), v(z)}, {px(endX), 0, pz(z), u(endX), v(z)},
             {px(endX), 0, pz(endZ), u(endX), v(endZ)}, {px(x), 0, pz(endZ), u(x), v(endZ)}}}, 0, -1, 0, 0.7f);
      emit({{{px(x), 4, pz(endZ), u(x), v(endZ)}, {px(endX), 4, pz(endZ), u(endX), v(endZ)},
             {px(endX), 4, pz(z), u(endX), v(z)}, {px(x), 4, pz(z), u(x), v(z)}}}, 0, 1, 0, 1.0f);
    }
  }

  for (int direction : {-1, 1}) {
    for (int x = 0; x < sizeX; ++x) {
      for (int z = 0; z < sizeZ;) {
        if (!filled(x, z) || filled(x + direction, z)) {
          ++z;
          continue;
        }
        const int start = z++;
        while (z < sizeZ && filled(x, z) && !filled(x + direction, z)) {
          ++z;
        }
        const float wx = px(x + (direction > 0));
        const float uv = (startX + x + 0.5f) / mask.width;
        const int first = direction < 0 ? start : z;
        const int last = direction < 0 ? z : start;
        emit({{{wx, 0, pz(first), uv, v(first)}, {wx, 0, pz(last), uv, v(last)},
               {wx, 4, pz(last), uv, v(last)}, {wx, 4, pz(first), uv, v(first)}}}, float(direction), 0, 0, 0.9f);
      }
    }
    for (int z = 0; z < sizeZ; ++z) {
      for (int x = 0; x < sizeX;) {
        if (!filled(x, z) || filled(x, z + direction)) {
          ++x;
          continue;
        }
        const int start = x++;
        while (x < sizeX && filled(x, z) && !filled(x, z + direction)) {
          ++x;
        }
        const float wz = pz(z + (direction > 0));
        const float uv = (startZ + z + 0.5f) / mask.height;
        const int first = direction > 0 ? start : x;
        const int last = direction > 0 ? x : start;
        emit({{{px(first), 0, wz, u(first), uv}, {px(last), 0, wz, u(last), uv},
               {px(last), 4, wz, u(last), uv}, {px(first), 4, wz, u(first), uv}}}, 0, 0, float(direction), 0.8f);
      }
    }
  }
}

}  // namespace mcrtx
