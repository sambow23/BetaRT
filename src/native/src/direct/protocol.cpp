#include "mcrtx/direct/protocol.hpp"

namespace mcrtx::direct {
void validatePacket(const Bytes &packet, bool resources) {
  if (resources && packet.empty()) return;
  Reader stream(packet);
  stream.header();
  std::uint32_t commands = 0;
  while (stream.remaining()) {
    const auto op = static_cast<Op>(stream.read<std::uint32_t>());
    const auto size = stream.read<std::uint32_t>();
    auto payload = stream.slice(size);
    if (++commands > 65536) throw std::invalid_argument("Too many submission commands");
    if (resources) {
      if (op < Op::Texture || op > Op::DestroyMesh || size < 8)
        throw std::invalid_argument("Invalid resource command");
      if (!payload.read<std::uint64_t>()) throw std::invalid_argument("Zero resource identifier");
      switch (op) {
      case Op::Texture: {
        const auto width = payload.read<std::uint32_t>(), height = payload.read<std::uint32_t>();
        if (!width || !height || width > 16384 || height > 16384 ||
            std::uint64_t(width) * height * 4 != payload.remaining())
          throw std::invalid_argument("Invalid texture payload");
        payload.slice(payload.remaining());
        break;
      }
      case Op::Material:
        // texture id, nearest/linear, U/V addressing; tint and alpha are per draw.
        payload.read<std::uint64_t>();
        if (payload.read<std::uint32_t>() > 1 || payload.read<std::uint32_t>() > 4 ||
            payload.read<std::uint32_t>() > 4) throw std::invalid_argument("Invalid material state");
        break;
      case Op::Mesh: {
        payload.read<std::uint64_t>(); // material
        const auto vertices = payload.read<std::uint32_t>(), indices = payload.read<std::uint32_t>();
        if (!vertices || vertices > 8 * 1024 * 1024 || !indices || indices > 24 * 1024 * 1024 || indices % 3 ||
            std::uint64_t(vertices) * 24 + std::uint64_t(indices) * 4 != payload.remaining())
          throw std::invalid_argument("Invalid mesh payload");
        for (std::uint32_t i = 0; i < vertices; ++i) {
          for (int j = 0; j < 5; ++j) payload.read<float>();
          payload.read<std::uint32_t>();
        }
        for (std::uint32_t i = 0; i < indices; ++i) {
          if (payload.read<std::uint32_t>() >= vertices) throw std::invalid_argument("Mesh index out of bounds");
        }
        break;
      }
      default: break;
      }
    } else {
      switch (op) {
      case Op::Camera:
        if (payload.read<std::uint32_t>() > 2) throw std::invalid_argument("Invalid camera kind");
        for (int i = 0; i < 32; ++i) payload.read<float>();
        break;
      case Op::Draw: {
        if (!payload.read<std::uint64_t>()) throw std::invalid_argument("Zero mesh identifier");
        const auto identity = payload.read<std::uint64_t>();
        const auto classification = payload.read<std::uint32_t>();
        if (classification > 2 || (!identity && classification != 2) || payload.read<std::uint32_t>() > 1)
          throw std::invalid_argument("Invalid instance classification or identity");
        for (int i = 0; i < 12; ++i) payload.read<float>();
        // category, double-sided, RGBA tint, alpha compare/reference,
        // blending enabled, source/destination Vulkan factors, vertex color enabled.
        for (int i = 0; i < 9; ++i) payload.read<std::uint32_t>();
        break;
      }
      case Op::Ui: {
        const auto width = payload.read<std::uint32_t>(), height = payload.read<std::uint32_t>();
        const auto vertices = payload.read<std::uint32_t>(), indices = payload.read<std::uint32_t>();
        const auto draws = payload.read<std::uint32_t>();
        if (!width || !height || vertices > 8 * 1024 * 1024 || indices > 24 * 1024 * 1024 || draws > 65536 ||
            std::uint64_t(vertices) * 24 + std::uint64_t(indices) * 4 + std::uint64_t(draws) * 24 != payload.remaining())
          throw std::invalid_argument("Invalid UI payload");
        for (std::uint32_t i = 0; i < vertices; ++i) {
          for (int j = 0; j < 5; ++j) payload.read<float>();
          payload.read<std::uint32_t>();
        }
        // Individual offset-adjusted indices are checked by the retained UI API.
        payload.slice(payload.remaining());
        break;
      }
      case Op::TerrainView:
        for (int i = 0; i < 6; ++i) payload.read<double>();
        for (int i = 0; i < 16; ++i) payload.read<float>();
        break;
      case Op::RasterState:
        if (payload.read<std::uint32_t>() > 1 || payload.read<std::uint32_t>() > 1 || payload.read<std::uint32_t>() > 7 || payload.read<std::uint32_t>() > 1)
          throw std::invalid_argument("Invalid depth state");
        break;
      default: throw std::invalid_argument("Invalid frame command");
      }
    }
    payload.end();
  }
}
}
