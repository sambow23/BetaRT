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
  auto accepts = [](const Bytes &bytes) {
    try { validatePacket(bytes, false); return true; }
    catch (const std::invalid_argument &) { return false; }
  };
  auto draw = [](std::uint64_t identity, std::uint32_t classification, std::uint32_t visible) {
    auto bytes = packet();
    append(bytes, std::uint32_t(Op::Draw)); append(bytes, std::uint32_t(108));
    append(bytes, std::uint64_t(1)); append(bytes, identity); append(bytes, classification); append(bytes, visible);
    for (int i = 0; i < 12; ++i) append(bytes, i % 5 == 0 ? 1.f : 0.f);
    for (int i = 0; i < 9; ++i) append(bytes, std::uint32_t(0));
    return bytes;
  };
  require(accepts(draw(17, 0, 0)) && accepts(draw(18, 1, 1)) && accepts(draw(0, 2, 1)), "Version 5 instance framing");
  require(!accepts(draw(0, 0, 1)) && !accepts(draw(0, 1, 1)) && !accepts(draw(18, 3, 1)) &&
          !accepts(draw(18, 1, 2)), "Malformed identities replaced an accepted frame");
  auto old = draw(18, 1, 1); old[4] = std::byte{1};
  require(!accepts(old), "Version 1 client accepted by version 5 JNI");
  old[4]=std::byte{2};require(!accepts(old), "Version 2 client accepted by version 5 JNI");
  old[4]=std::byte{3};require(!accepts(old),"Version 3 material protocol accepted by version 5 JNI");
  auto material=packet();append(material,std::uint32_t(Op::Material));append(material,std::uint32_t(48));
  append(material,std::uint64_t(9));append(material,std::uint64_t(10));
  append(material,std::uint32_t(0));append(material,std::uint32_t(1));append(material,std::uint32_t(1));
  append(material,std::uint64_t(11));append(material,3.f);append(material,2.f);append(material,1.f);
  validatePacket(material,true);
  auto broken=material;broken.pop_back();rejected=false;
  try{validatePacket(broken,true);}catch(const std::invalid_argument &){rejected=true;}
  require(rejected,"Truncated emission accepted");
  float negative=-1;std::memcpy(material.data()+material.size()-4,&negative,4);rejected=false;
  try{validatePacket(material,true);}catch(const std::invalid_argument &){rejected=true;}
  require(rejected,"Negative emission accepted");
  auto camera = [](std::uint64_t world, std::uint64_t reset) {
    auto bytes=packet();append(bytes,std::uint32_t(Op::Camera));append(bytes,std::uint32_t(188));append(bytes,std::uint32_t(0));
    for(int i=0;i<32;++i)append(bytes,i%5==0?1.f:0.f);
    append(bytes,world);append(bytes,reset);
    append(bytes,std::uint32_t(1));
    for(float value:{.1f,.2f,.3f,0.f,-1.f,0.f,1.f,1.f,1.f})append(bytes,value);
    return bytes;
  };
  old[4]=std::byte{4};require(!accepts(old),"Protocol 4 accepted without complete lighting state");
  auto epochFrame=camera(8,11);require(accepts(epochFrame),"Camera temporal metadata rejected");
  epochFrame.pop_back();require(!accepts(epochFrame),"Truncated temporal epoch accepted");
  Mailbox temporalQueue(1024);
  require(temporalQueue.publish({},camera(8,10))&&temporalQueue.publish({},camera(8,11)),"Publish temporal frames");auto latest=temporalQueue.take(0ms);
  require(bool(latest.frame),"Missing replaced temporal frame");
  Reader cameraReader(*latest.frame);cameraReader.header();cameraReader.read<std::uint32_t>();auto payload=cameraReader.slice(cameraReader.read<std::uint32_t>());
  payload.read<std::uint32_t>();for(int i=0;i<32;++i)payload.read<float>();
  require(payload.read<std::uint64_t>()==8&&payload.read<std::uint64_t>()==11,"Replaced frame lost pending history reset");
  require(payload.read<std::uint32_t>()==1&&payload.read<float>()==.1f,"Replaced camera lost environment");
  auto invalidEnvironment=camera(8,11);
  float badRadiance=-1;std::memcpy(invalidEnvironment.data()+168,&badRadiance,4);
  require(!accepts(invalidEnvironment),"Negative sky radiance accepted");
  invalidEnvironment=camera(8,11);invalidEnvironment[164]=std::byte{2};
  require(!accepts(invalidEnvironment),"Invalid environment enable value accepted");
  auto view = packet(); append(view, std::uint32_t(Op::TerrainView)); append(view, std::uint32_t(112));
  for (int i = 0; i < 6; ++i) append(view, i < 3 ? 10000000.125 : 9999872.0);
  for (int i = 0; i < 16; ++i) append(view, i % 5 == 0 ? 1.f : 0.f);
  require(accepts(view), "Separate double camera and floating origin");
  view.pop_back(); require(!accepts(view), "Truncated floating origin accepted");
  std::cout << "Direct mailbox and protocol tests passed\n";
}
