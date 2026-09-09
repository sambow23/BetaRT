#include "mcrtx/direct/protocol.hpp"
#include <future>
#include <iostream>

using namespace mcrtx::direct;
using namespace std::chrono_literals;
namespace {
void require(bool condition, const char *message) {
  if (!condition) throw std::runtime_error(message);
}
template<class T> void append(Bytes &bytes, T value) {
  const auto *start = reinterpret_cast<const std::byte *>(&value);
  bytes.insert(bytes.end(), start, start + sizeof(value));
}
Bytes packet() {
  Bytes result;
  append(result, kMagic); append(result, kVersion);
  return result;
}
}
int main() {
  Mailbox queue(32);
  require(queue.publish(Bytes(8, std::byte{1}), Bytes(8, std::byte{10})), "Publish first");
  require(queue.publish(Bytes(8, std::byte{2}), Bytes(8, std::byte{20})), "Replace frame");
  auto frame = queue.take(0ms);
  require(frame.sequence == 2 && frame.resources.size() == 2 && frame.frame->front() == std::byte{20},
          "Latest frame or reliable resource ordering");
  require(frame.resources[0][0] == std::byte{1} && frame.resources[1][0] == std::byte{2}, "Resources reordered");
  require(queue.replaced() == 1 && queue.published() == 2, "Counters");
  require(queue.publish(Bytes(32), Bytes(8)), "Fill resource queue");
  std::promise<void> started;
  auto producer = std::async(std::launch::async, [&] {
    started.set_value(); return queue.publish(Bytes(1), Bytes(8));
  });
  started.get_future().wait();
  require(producer.wait_for(20ms) == std::future_status::timeout, "Queue did not apply backpressure");
  queue.stop();
  require(!producer.get(), "Shutdown failed to release producer");
  require(!queue.take(0ms).frame, "Shutdown retained a pending frame");
  queue.reset();
  require(queue.publish({}, Bytes(1)) && queue.take(0ms).sequence == 1, "Restart sequence");
  queue.publish({}, Bytes(1));queue.publish({}, Bytes(1));
  auto paced = std::async(std::launch::async, [&] { return queue.publish({}, Bytes(1)); });
  require(paced.wait_for(20ms) == std::future_status::timeout, "Unbounded producer outran pending frame");
  require(queue.take(0ms).sequence == 3, "Latest replaceable pending frame lost");
  require(paced.get(), "Consumer did not release paced producer");
  queue.take(0ms);
  validatePacket(packet(), false);
  auto resource = packet();
  append(resource, std::uint32_t(Op::Texture)); append(resource, std::uint32_t(20));
  append(resource, std::uint64_t(1)); append(resource, std::uint32_t(1)); append(resource, std::uint32_t(1));
  append(resource, std::uint32_t(0xff00ff00));
  validatePacket(resource, true);
  for (std::size_t size = 0; size < resource.size(); ++size) {
    if (size == 0 || size == 8) continue; // Empty resource packet/header are valid.
    bool rejected = false;
    try { validatePacket(Bytes(resource.begin(), resource.begin() + size), true); }
    catch (const std::invalid_argument &) { rejected = true; }
    require(rejected, "Truncated packet accepted");
  }
  bool rejected = false;
  try { validatePacket(resource, false); } catch (const std::invalid_argument &) { rejected = true; }
  require(rejected, "Resource command accepted as a frame");
  std::cout << "Direct mailbox and protocol tests passed\n";
}
