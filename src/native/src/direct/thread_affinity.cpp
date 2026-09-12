#include "mcrtx/direct/thread_affinity.hpp"
#include <charconv>
#include <cstring>
#if defined(__linux__)
#include <cerrno>
#include <sched.h>
#endif

namespace mcrtx::direct {
namespace {
std::string_view trim(std::string_view value) {
  const auto first = value.find_first_not_of(" \t\r\n");
  if (first == std::string_view::npos) return {};
  return value.substr(first, value.find_last_not_of(" \t\r\n") - first + 1);
}
}

ThreadAffinityResult setRenderThreadCpus(std::string_view specification) {
  specification = trim(specification);
  if (specification.empty() || specification == "default") return {};
#if defined(__linux__)
  cpu_set_t requested, allowed, selected;
  CPU_ZERO(&requested);
  CPU_ZERO(&selected);
  auto number = [](std::string_view value, unsigned &result) {
    value = trim(value);
    if (value.empty()) return false;
    const auto parsed = std::from_chars(value.data(), value.data() + value.size(), result);
    return parsed.ec == std::errc{} && parsed.ptr == value.data() + value.size() && result < CPU_SETSIZE;
  };
  auto invalid = [] {
    return ThreadAffinityResult{false, "MCRTX_RENDER_CPUS ignored: expected CPU numbers/ranges such as 0-3,6 within 0-"
      + std::to_string(CPU_SETSIZE - 1)};
  };
  while (!specification.empty()) {
    const auto comma = specification.find(',');
    const auto item = trim(specification.substr(0, comma));
    const auto dash = item.find('-');
    unsigned first = 0, last = 0;
    if (dash == std::string_view::npos) {
      if (!number(item, first)) return invalid();
      last = first;
    } else if (!number(item.substr(0, dash), first) || !number(item.substr(dash + 1), last) || first > last) {
      return invalid();
    }
    for (unsigned cpu = first; cpu <= last; ++cpu) CPU_SET(cpu, &requested);
    if (comma == std::string_view::npos) break;
    specification.remove_prefix(comma + 1);
    if (trim(specification).empty()) return invalid();
  }
  if (sched_getaffinity(0, sizeof(allowed), &allowed))
    return {false, std::string("MCRTX_RENDER_CPUS ignored: cannot read thread affinity: ") + std::strerror(errno)};
  CPU_AND(&selected, &requested, &allowed);
  if (!CPU_COUNT(&selected))
    return {false, "MCRTX_RENDER_CPUS ignored: no requested CPUs are in the render thread's allowed mask"};
  // pid 0 changes only this worker, not the Java producer or existing workers.
  if (sched_setaffinity(0, sizeof(selected), &selected))
    return {false, std::string("MCRTX_RENDER_CPUS ignored: cannot set thread affinity: ") + std::strerror(errno)};
  std::string cpus;
  for (unsigned cpu = 0; cpu < CPU_SETSIZE; ++cpu) if (CPU_ISSET(cpu, &selected)) {
    if (!cpus.empty()) cpus += ',';
    cpus += std::to_string(cpu);
  }
  return {true, "Render thread CPUs: " + cpus};
#else
  return {false, "MCRTX_RENDER_CPUS ignored: thread affinity is supported on Linux only"};
#endif
}
}
