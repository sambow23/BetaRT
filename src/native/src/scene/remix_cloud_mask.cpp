#include "mcrtx/scene/remix_cloud_geometry.hpp"

#include <algorithm>
#include <bit>
#include <fstream>
#include <memory>
#include <stdexcept>

#define STB_IMAGE_STATIC
#define STB_IMAGE_IMPLEMENTATION
#define STBI_ONLY_PNG
#define STBI_NO_STDIO
#define STBI_MAX_DIMENSIONS 4096
#include <stb_image.h>

namespace mcrtx {
namespace {

constexpr int kMaxCloudDimension = 4096;
constexpr std::size_t kMaxCloudFileBytes = 128 * 1024 * 1024;

CloudMask makeMask(int width, int height) {
  if (width <= 0 || height <= 0 || width > kMaxCloudDimension || height > kMaxCloudDimension) {
    throw std::runtime_error("Cloud mask dimensions must be between 1 and 4096");
  }
  return {width, height, std::vector<std::uint8_t>(std::size_t(width) * height)};
}

std::uint32_t readWord(std::span<const std::uint8_t> bytes, std::size_t offset) {
  if (offset > bytes.size() || bytes.size() - offset < 4) {
    throw std::runtime_error("Truncated cloud DDS header");
  }
  return std::uint32_t(bytes[offset]) | (std::uint32_t(bytes[offset + 1]) << 8)
      | (std::uint32_t(bytes[offset + 2]) << 16) | (std::uint32_t(bytes[offset + 3]) << 24);
}

CloudMask decodeDds(std::span<const std::uint8_t> bytes) {
  if (readWord(bytes, 4) != 124 || readWord(bytes, 76) != 32 || readWord(bytes, 112) != 0) {
    throw std::runtime_error("Cloud DDS must be a single 2D texture");
  }
  auto mask = makeMask(static_cast<int>(readWord(bytes, 16)), static_cast<int>(readWord(bytes, 12)));
  std::size_t offset = 128;
  std::uint32_t bits = readWord(bytes, 88);
  std::uint32_t alphaMask = (readWord(bytes, 80) & 1) ? readWord(bytes, 104) : 0;
  int compression = 0;
  if (readWord(bytes, 80) & 4) {
    switch (readWord(bytes, 84)) {
    case 0x31545844: compression = 1; break;
    case 0x33545844: compression = 2; break;
    case 0x35545844: compression = 3; break;
    case 0x30315844:
      if (readWord(bytes, 132) != 3 || readWord(bytes, 136) != 0 || readWord(bytes, 140) != 1) {
        throw std::runtime_error("Cloud DX10 DDS must be a single 2D texture");
      }
      offset = 148;
      switch (readWord(bytes, 128)) {
      case 28: case 29: case 87: case 91: bits = 32; alphaMask = 0xff000000u; break;
      case 88: case 93: bits = 32; alphaMask = 0; break;
      case 71: case 72: compression = 1; break;
      case 74: case 75: compression = 2; break;
      case 77: case 78: compression = 3; break;
      default: throw std::runtime_error("Unsupported cloud DDS format; use RGBA8, BC1/2/3 or PNG");
      }
      break;
    default: throw std::runtime_error("Unsupported cloud DDS compression; use RGBA8, BC1/2/3 or PNG");
    }
  } else if (!(readWord(bytes, 80) & 0x40)) {
    throw std::runtime_error("Unsupported cloud DDS pixel layout");
  }

  if (compression == 0) {
    if (bits != 16 && bits != 24 && bits != 32) {
      throw std::runtime_error("Cloud DDS must have 16, 24 or 32-bit RGB pixels");
    }
    const std::size_t pixelBytes = bits / 8;
    const std::size_t rowBytes = (readWord(bytes, 8) & 8) ? readWord(bytes, 20) : mask.width * pixelBytes;
    if (rowBytes < mask.width * pixelBytes || offset > bytes.size()
        || rowBytes > (bytes.size() - offset) / mask.height) {
      throw std::runtime_error("Truncated cloud DDS pixels");
    }
    if (bits < 32 && (alphaMask >> bits) != 0) {
      throw std::runtime_error("Invalid cloud DDS alpha mask");
    }
    const unsigned shift = alphaMask ? std::countr_zero(alphaMask) : 0;
    const std::uint32_t maximum = alphaMask >> shift;
    if (maximum && (std::uint64_t(maximum) & (std::uint64_t(maximum) + 1)) != 0) {
      throw std::runtime_error("Noncontiguous cloud DDS alpha mask");
    }
    for (int z = 0; z < mask.height; ++z) {
      for (int x = 0; x < mask.width; ++x) {
        const std::size_t pixelOffset = offset + z * rowBytes + x * pixelBytes;
        std::uint32_t pixel = 0;
        for (std::size_t byte = 0; byte < pixelBytes; ++byte) {
          pixel |= std::uint32_t(bytes[pixelOffset + byte]) << (byte * 8);
        }
        mask.alpha[z * mask.width + x] = maximum
            ? static_cast<std::uint8_t>(std::uint64_t((pixel & alphaMask) >> shift) * 255 / maximum) : 255;
      }
    }
    return mask;
  }

  const std::size_t blockBytes = compression == 1 ? 8 : 16;
  const int blocksX = (mask.width + 3) / 4;
  const int blocksZ = (mask.height + 3) / 4;
  if (offset > bytes.size() || std::size_t(blocksX) * blocksZ > (bytes.size() - offset) / blockBytes) {
    throw std::runtime_error("Truncated cloud DDS blocks");
  }
  for (int bz = 0; bz < blocksZ; ++bz) {
    for (int bx = 0; bx < blocksX; ++bx) {
      const auto block = bytes.subspan(offset + (bz * blocksX + bx) * blockBytes, blockBytes);
      std::array<std::uint8_t, 8> table {block[0], block[1]};
      if (compression == 3) {
        const int divisor = table[0] > table[1] ? 7 : 5;
        for (int i = 2; i <= divisor; ++i) {
          table[i] = ((divisor + 1 - i) * table[0] + (i - 1) * table[1]) / divisor;
        }
        if (divisor == 5) {
          table[6] = 0;
          table[7] = 255;
        }
      }
      std::uint64_t selectors = 0;
      for (int byte = 0; byte < 6; ++byte) {
        selectors |= std::uint64_t(block[2 + byte]) << (8 * byte);
      }
      for (int z = 0; z < 4 && bz * 4 + z < mask.height; ++z) {
        for (int x = 0; x < 4 && bx * 4 + x < mask.width; ++x) {
          const int pixel = z * 4 + x;
          std::uint8_t alpha = 255;
          if (compression == 1) {
            const unsigned color0 = block[0] | (unsigned(block[1]) << 8);
            const unsigned color1 = block[2] | (unsigned(block[3]) << 8);
            const unsigned index = (readWord(block, 4) >> (2 * pixel)) & 3;
            alpha = color0 <= color1 && index == 3 ? 0 : 255;
          } else if (compression == 2) {
            alpha = ((block[pixel / 2] >> (4 * (pixel % 2))) & 15) * 17;
          } else {
            alpha = table[(selectors >> (3 * pixel)) & 7];
          }
          mask.alpha[(bz * 4 + z) * mask.width + bx * 4 + x] = alpha;
        }
      }
    }
  }
  return mask;
}

}  // namespace

bool CloudMask::valid() const {
  return width > 0 && height > 0 && width <= kMaxCloudDimension && height <= kMaxCloudDimension
      && alpha.size() == std::size_t(width) * height;
}

bool CloudMask::occupied(int x, int z) const {
  if (!valid()) {
    return false;
  }
  x = (x % width + width) % width;
  z = (z % height + height) % height;
  // Match the old 2/255 cutoff after its 0.8 vertex-alpha multiplier.
  return alpha[z * width + x] >= 3;
}

CloudMask decodeCloudMask(std::span<const std::uint8_t> bytes) {
  if (bytes.size() > kMaxCloudFileBytes) {
    throw std::runtime_error("Cloud texture is too large");
  }
  if (bytes.size() >= 4 && readWord(bytes, 0) == 0x20534444) {
    return decodeDds(bytes);
  }
  int width = 0, height = 0, channels = 0;
  if (!stbi_info_from_memory(bytes.data(), static_cast<int>(bytes.size()), &width, &height, &channels)) {
    throw std::runtime_error("Cloud mask requires a valid PNG or DDS texture");
  }
  auto mask = makeMask(width, height);
  const std::unique_ptr<stbi_uc, decltype(&stbi_image_free)> pixels(
      stbi_load_from_memory(bytes.data(), static_cast<int>(bytes.size()), &width, &height, &channels, 4), stbi_image_free);
  if (!pixels || width != mask.width || height != mask.height) {
    throw std::runtime_error("Failed to decode cloud PNG");
  }
  for (std::size_t pixel = 0; pixel < mask.alpha.size(); ++pixel) {
    mask.alpha[pixel] = pixels.get()[pixel * 4 + 3];
  }
  return mask;
}

CloudMask loadCloudMask(const std::filesystem::path& path) {
  std::ifstream stream(path, std::ios::binary | std::ios::ate);
  const auto size = stream.tellg();
  if (!stream || size <= 0 || size > std::streamoff(kMaxCloudFileBytes)) {
    throw std::runtime_error("Cannot read cloud texture, or file exceeds 128 MiB");
  }
  std::vector<std::uint8_t> bytes(static_cast<std::size_t>(size));
  stream.seekg(0);
  if (!stream.read(reinterpret_cast<char*>(bytes.data()), size)) {
    throw std::runtime_error("Cannot read complete cloud texture");
  }
  return decodeCloudMask(bytes);
}

}  // namespace mcrtx
