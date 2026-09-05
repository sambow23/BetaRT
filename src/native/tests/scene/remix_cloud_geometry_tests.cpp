#include "mcrtx/scene/remix_cloud_geometry.hpp"

#include <algorithm>
#include <cmath>
#include <cstdlib>
#include <iostream>
#include <stdexcept>

namespace {

void require(bool condition, const char* message) {
  if (!condition) {
    std::cerr << message << '\n';
    std::exit(1);
  }
}

void word(std::vector<std::uint8_t>& data, int offset, std::uint32_t value) {
  for (int i = 0; i < 4; ++i) {
    data[offset + i] = static_cast<std::uint8_t>(value >> (8 * i));
  }
}

std::vector<std::uint8_t> dds(int width, int height, int bytes, std::uint32_t compression = 0) {
  std::vector<std::uint8_t> data(128 + bytes);
  word(data, 0, 0x20534444);
  word(data, 4, 124);
  word(data, 12, height);
  word(data, 16, width);
  word(data, 76, 32);
  word(data, 80, compression ? 4 : 0x41);
  word(data, 84, compression);
  word(data, 88, 32);
  word(data, 104, 0xff000000);
  return data;
}

void reject(const std::vector<std::uint8_t>& data) {
  try {
    mcrtx::decodeCloudMask(data);
    require(false, "Invalid texture accepted");
  } catch (const std::runtime_error&) {
  }
}

struct Mesh {
  std::vector<remixapi_HardcodedVertex> vertices;
  std::vector<std::uint32_t> indices;
};

Mesh build(const mcrtx::CloudMask& mask, int phaseX = 16, int phaseZ = 16) {
  Mesh mesh;
  mcrtx::appendFancyCloudGeometry(mask, phaseX, phaseZ, {1, 1, 1}, mesh.vertices, mesh.indices);
  require(mesh.vertices.size() % 4 == 0 && mesh.indices.size() == mesh.vertices.size() / 4 * 6, "Invalid quad indices");
  for (auto index : mesh.indices) {
    require(index < mesh.vertices.size(), "Index out of bounds");
  }
  for (std::size_t i = 0; i < mesh.vertices.size(); i += 4) {
    const auto& a = mesh.vertices[i];
    const auto& b = mesh.vertices[i + 1];
    const auto& c = mesh.vertices[i + 2];
    float e[3], f[3];
    for (int axis = 0; axis < 3; ++axis) {
      e[axis] = b.position[axis] - a.position[axis];
      f[axis] = c.position[axis] - a.position[axis];
    }
    const float dot = (e[1] * f[2] - e[2] * f[1]) * a.normal[0]
        + (e[2] * f[0] - e[0] * f[2]) * a.normal[1] + (e[0] * f[1] - e[1] * f[0]) * a.normal[2];
    require(dot > 0, "Degenerate or reversed cloud face");
    for (std::size_t j = i; j < i + 4; ++j) {
      const auto& p = mesh.vertices[j];
      require(p.position[0] >= -192 && p.position[0] <= 384 && p.position[2] >= -192 && p.position[2] <= 384,
              "Cloud patch bounds changed");
      require(p.position[1] == 0 || p.position[1] == 4, "Cloud thickness changed");
      require((p.color >> 24) == 255, "Fancy cloud vertex is not opaque");
    }
  }
  return mesh;
}

void verifyCoverage(const mcrtx::CloudMask& mask) {
  const auto mesh = build(mask);
  for (int z = 0; z < 48; ++z) {
    for (int x = 0; x < 48; ++x) {
      int tops = 0;
      int bottoms = 0;
      const float wx = (x - 16 + 0.5f) * 12;
      const float wz = (z - 16 + 0.5f) * 12;
      for (std::size_t i = 0; i < mesh.vertices.size(); i += 4) {
        const auto& a = mesh.vertices[i];
        const auto& c = mesh.vertices[i + 2];
        if (wx > std::min(a.position[0], c.position[0]) && wx < std::max(a.position[0], c.position[0])
            && wz > std::min(a.position[2], c.position[2]) && wz < std::max(a.position[2], c.position[2])) {
          tops += a.normal[1] > 0;
          bottoms += a.normal[1] < 0;
        }
      }
      require(tops == int(mask.occupied(x, z)) && bottoms == tops, "Silhouette hole filled or duplicate cap");
    }
  }
  for (std::size_t i = 0; i < mesh.vertices.size(); i += 4) {
    const auto& a = mesh.vertices[i];
    const auto& c = mesh.vertices[i + 2];
    if (a.normal[1] != 0) {
      continue;
    }
    const float x = (a.position[0] + c.position[0]) / 24 + 16;
    const float z = (a.position[2] + c.position[2]) / 24 + 16;
    const int insideX = int(std::floor(x - a.normal[0] * 0.1f));
    const int insideZ = int(std::floor(z - a.normal[2] * 0.1f));
    const int outsideX = int(std::floor(x + a.normal[0] * 0.1f));
    const int outsideZ = int(std::floor(z + a.normal[2] * 0.1f));
    require(mask.occupied(insideX, insideZ), "Face does not border a cloud cell");
    require(outsideX < 0 || outsideX >= 48 || outsideZ < 0 || outsideZ >= 48 || !mask.occupied(outsideX, outsideZ),
            "Interior cloud slice survived");
  }
}

}  // namespace

int main(int argc, char** argv) {
  auto data = dds(2, 2, 16);
  for (int i = 0; i < 4; ++i) {
    data[128 + i * 4 + 3] = static_cast<std::uint8_t>(i);
  }
  auto mask = mcrtx::decodeCloudMask(data);
  require(mask.alpha == std::vector<std::uint8_t>({0, 1, 2, 3}), "DDS RGBA alpha extraction");
  require(!mask.occupied(0, 1) && mask.occupied(-1, -1) && mask.occupied(3, 3), "Cutoff or negative wrap");
  word(data, 8, 8);
  word(data, 20, 12);
  data.resize(152);
  data[143] = 117;
  require(mcrtx::decodeCloudMask(data).alpha[2] == 117, "DDS row pitch ignored");
  data.resize(140);
  reject(data);
  reject({});
  reject({1, 2, 3, 4});
  data = dds(4097, 1, 4);
  reject(data);
  data = dds(4, 4, 8, 0x31545844);
  word(data, 132, 0xffffffff);
  require(mcrtx::decodeCloudMask(data).alpha[0] == 0, "BC1 transparent selector");
  data[128] = 1;
  require(mcrtx::decodeCloudMask(data).alpha[0] == 255, "BC1 opaque color ordering");
  data = dds(4, 4, 16, 0x33545844);
  data[128] = 0xf3;
  mask = mcrtx::decodeCloudMask(data);
  require(mask.alpha[0] == 51 && mask.alpha[1] == 255, "BC2 explicit alpha");
  data = dds(4, 4, 16, 0x35545844);
  data[128] = 255;
  data[129] = 0;
  data[130] = 2;
  require(mcrtx::decodeCloudMask(data).alpha[0] == 218, "BC3 interpolated alpha");
  data[128] = 0;
  data[129] = 100;
  data[130] = 7;
  require(mcrtx::decodeCloudMask(data).alpha[0] == 255, "BC3 terminal alpha");
  data.pop_back();
  reject(data);
  data = dds(1, 1, 24, 0x30315844);
  word(data, 128, 28);
  word(data, 132, 3);
  word(data, 140, 1);
  data[151] = 71;
  require(mcrtx::decodeCloudMask(data).alpha[0] == 71, "DX10 RGBA alpha");
  word(data, 128, 98);
  reject(data);

  mask = {256, 256, std::vector<std::uint8_t>(256 * 256)};
  require(build(mask).indices.empty(), "Empty mask generated geometry");
  mask.alpha[5 * 256 + 5] = 255;
  require(build(mask).vertices.size() == 24, "One voxel must have six exterior quads");
  mask.alpha[5 * 256 + 6] = 255;
  require(build(mask).vertices.size() == 24, "Adjacent voxels should merge into six quads");
  verifyCoverage(mask);
  for (int z = 3; z < 10; ++z) {
    for (int x = 3; x < 10; ++x) {
      mask.alpha[z * 256 + x] = (x == 3 || x == 9 || z == 3 || z == 9) ? 255 : 0;
    }
  }
  verifyCoverage(mask);
  std::fill(mask.alpha.begin(), mask.alpha.end(), 255);
  require(build(mask).vertices.size() == 24, "Solid patch should have six merged faces");
  verifyCoverage(mask);
  build(mask, -1, -257);
  for (const auto dims : {std::array<int, 2>{7, 13}, {512, 1024}}) {
    mask = {dims[0], dims[1], std::vector<std::uint8_t>(dims[0] * dims[1], 255)};
    require(build(mask, 255, -1).vertices.size() == 24, "Non-256 texture clipping or merge");
  }
  for (int i = 1; i < argc; ++i) {
    mask = mcrtx::loadCloudMask(argv[i]);
    require(mask.valid(), "Installed cloud asset failed to decode");
    const auto mesh = build(mask);
    std::cout << argv[i] << ": " << mask.width << 'x' << mask.height << ", " << mesh.indices.size() / 6 << " quads\n";
  }
  std::cout << "Cloud mask and exterior geometry tests passed\n";
}
