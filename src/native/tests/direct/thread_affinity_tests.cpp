#include "mcrtx/direct/thread_affinity.hpp"
#include <future>
#include <iostream>
#include <stdexcept>
#if defined(__linux__)
#include <sched.h>
#endif

namespace {
void require(bool condition, const char *message) {
  if (!condition) throw std::runtime_error(message);
}
#if defined(__linux__)
cpu_set_t current() {
  cpu_set_t mask;
  require(sched_getaffinity(0, sizeof(mask), &mask) == 0, "Read affinity");
  return mask;
}
#endif
}

int main() {
  using mcrtx::direct::setRenderThreadCpus;
  try {
    require(!setRenderThreadCpus("").applied && !setRenderThreadCpus(" default ").applied, "Default policy");
#if defined(__linux__)
    const auto parent = current();
    auto worker = std::async(std::launch::async, [&] {
      const auto before = current();
      for (const auto value : {"-1", "1-0", "0,", "0,,1", ",0", "0-1-2", "1x", "+1", "42949672960", "1024"}) {
        const auto result = setRenderThreadCpus(value);
        const auto after = current();
        require(!result.applied && !result.message.empty() && CPU_EQUAL(&before, &after), "Invalid list changed affinity");
      }
      unsigned selected = 0;
      while (selected < CPU_SETSIZE && !CPU_ISSET(selected, &before)) ++selected;
      require(selected < CPU_SETSIZE, "No available CPU");
      const auto one = std::to_string(selected);
      require(setRenderThreadCpus(" " + one + "-" + one + ", " + one + " ").applied, "Apply range and duplicate");
      auto limited = current();
      require(CPU_COUNT(&limited) == 1 && CPU_ISSET(selected, &limited), "Wrong worker affinity");
      // A broad request must not expand a previously constrained worker mask.
      require(setRenderThreadCpus("0-1023").applied, "Intersect allowed CPUs");
      auto after = current();
      require(CPU_EQUAL(&limited, &after), "Expanded inherited affinity");
      const auto excluded = std::to_string((selected + 1) % CPU_SETSIZE);
      require(!setRenderThreadCpus(excluded).applied, "Empty intersection accepted");
      after = current();
      require(CPU_EQUAL(&limited, &after), "Empty intersection changed affinity");
      require(!setRenderThreadCpus("default").applied, "Default changed existing restriction");
    });
    worker.get();
    const auto after = current();
    require(CPU_EQUAL(&parent, &after), "Worker changed parent thread affinity");
#else
    require(!setRenderThreadCpus("0").applied, "Unsupported affinity applied");
#endif
    std::cout << "Thread affinity checks passed\n";
  } catch (const std::exception &error) {
    std::cerr << error.what() << '\n';
    return 1;
  }
}
