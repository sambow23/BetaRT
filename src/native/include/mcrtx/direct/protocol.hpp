#pragma once
#include "mcrtx/direct/submission.hpp"
#include <array>
#include <cstring>
#include <cmath>
#include <stdexcept>
#include <type_traits>

namespace mcrtx::direct {
// Private JNI protocol: little-endian, version 1, independent of public ABI.
// Header: magic/version. Commands: opcode, payload bytes, then tightly packed payload.
inline constexpr std::uint32_t kMagic = 0x54435244; // DRCT
inline constexpr std::uint32_t kVersion = 1;
enum class Op : std::uint32_t {
  Texture = 1, Material = 2, Mesh = 3,
  DestroyTexture = 4, DestroyMaterial = 5, DestroyMesh = 6,
  Camera = 16, Draw = 17, Ui = 18, TerrainView = 19, RasterState = 20,
};

class Reader {
public:
  Reader(const std::byte *data, std::size_t size) : m_data(data), m_size(size) { }
  explicit Reader(const Bytes &bytes) : Reader(bytes.data(), bytes.size()) { }
  template<class T> T read() {
    static_assert(std::is_arithmetic_v<T>);
    require(sizeof(T));
    T result;
    std::memcpy(&result, m_data, sizeof(T));
    m_data += sizeof(T);
    m_size -= sizeof(T);
    if constexpr (std::is_floating_point_v<T>) {
      if (!std::isfinite(result)) throw std::invalid_argument("Non-finite submission value");
    }
    return result;
  }
  void header() {
    if (read<std::uint32_t>() != kMagic || read<std::uint32_t>() != kVersion)
      throw std::invalid_argument("Unsupported direct submission protocol");
  }
  Reader slice(std::size_t bytes) {
    require(bytes);
    Reader result(m_data, bytes);
    m_data += bytes;
    m_size -= bytes;
    return result;
  }
  const void *data() const { return m_data; }
  std::size_t remaining() const { return m_size; }
  void end() const {
    if (m_size) throw std::invalid_argument("Trailing submission bytes");
  }
  void require(std::size_t bytes) const {
    if (bytes > m_size) throw std::invalid_argument("Truncated direct submission");
  }
private:
  const std::byte *m_data;
  std::size_t m_size;
};

// Validates framing before a Java submission can replace an accepted frame.
void validatePacket(const Bytes &packet, bool resources);
}
