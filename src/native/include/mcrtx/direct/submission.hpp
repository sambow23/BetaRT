#pragma once

#include <chrono>
#include <condition_variable>
#include <cstddef>
#include <cstdint>
#include <deque>
#include <memory>
#include <mutex>
#include <vector>

namespace mcrtx::direct {
using Bytes = std::vector<std::byte>;

// Each resource packet precedes its frame. Replacing a pending frame never
// discards resource operations; it only replaces transient draw/camera/UI data.
struct Submission {
  std::uint64_t sequence = 0;
  std::deque<Bytes> resources;
  std::shared_ptr<const Bytes> frame;
};

class Mailbox {
public:
  explicit Mailbox(std::size_t limit = 256 * 1024 * 1024) : m_limit(limit) { }
  bool publish(Bytes resources, Bytes frame);
  Submission take(std::chrono::milliseconds timeout);
  void stop();
  void reset();
  std::uint64_t published() const;
  std::uint64_t replaced() const;
private:
  mutable std::mutex m_mutex;
  std::condition_variable m_event;
  Submission m_pending;
  std::size_t m_bytes = 0, m_limit;
  std::uint64_t m_sequence = 0, m_replaced = 0;
  bool m_pendingReplaced = false;
  bool m_stopped = false;
};
}
