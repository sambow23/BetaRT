#include "mcrtx/direct/submission.hpp"
#include <utility>

namespace mcrtx::direct {
bool Mailbox::publish(Bytes resources, Bytes frame) {
  if (resources.size() > m_limit || frame.empty() || frame.size() > m_limit) return false;
  // Allocate before modifying queue state, preserving the pending submission on failure.
  auto ownedFrame = std::make_shared<const Bytes>(std::move(frame));
  std::unique_lock lock(m_mutex);
  m_event.wait(lock, [&] { return m_stopped || (resources.size() <= m_limit - m_bytes && !m_pendingReplaced); });
  if (m_stopped) return false;
  const auto resourceBytes = resources.size();
  if (resourceBytes) m_pending.resources.push_back(std::move(resources));
  m_bytes += resourceBytes;
  m_pendingReplaced = bool(m_pending.frame);
  m_replaced += m_pendingReplaced;
  m_pending.frame = std::move(ownedFrame);
  m_pending.sequence = ++m_sequence;
  lock.unlock();
  m_event.notify_all();
  return true;
}

Submission Mailbox::take(std::chrono::milliseconds timeout) {
  std::unique_lock lock(m_mutex);
  m_event.wait_for(lock, timeout, [&] { return m_stopped || m_pending.frame; });
  Submission result = std::move(m_pending);
  m_pending = {};
  m_pendingReplaced = false;
  m_bytes = 0;
  lock.unlock();
  m_event.notify_all();
  return result;
}

void Mailbox::stop() {
  std::lock_guard lock(m_mutex);
  m_stopped = true;
  m_pending = {};
  m_pendingReplaced = false;
  m_bytes = 0;
  m_event.notify_all();
}

void Mailbox::reset() {
  std::lock_guard lock(m_mutex);
  m_stopped = false;
  m_pending = {};
  m_pendingReplaced = false;
  m_bytes = 0;
  m_sequence = m_replaced = 0;
}

std::uint64_t Mailbox::published() const { std::lock_guard lock(m_mutex); return m_sequence; }
std::uint64_t Mailbox::replaced() const { std::lock_guard lock(m_mutex); return m_replaced; }
}
