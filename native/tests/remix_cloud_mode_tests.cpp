#include "mcrtx/remix_cloud_mode.hpp"

#include <cstdlib>
#include <iostream>
#include <string_view>

namespace {

void require(bool condition, const char* message) {
  if (!condition) {
    std::cerr << message << '\n';
    std::exit(1);
  }
}

void requireValue(std::string_view actual, std::string_view expected, const char* message) {
  if (actual != expected) {
    std::cerr << message << ": expected " << expected << ", got " << actual << '\n';
    std::exit(1);
  }
}

}  // namespace

int main() {
  const auto disabled = mcrtx::remixAtmosphereCloudConfigValues(false);
  require(disabled.size() == 4, "disabled config count");
  requireValue(disabled[0].key, "rtx.skyMode", "disabled sky mode key");
  requireValue(disabled[0].value, "1", "disabled sky mode value");
  requireValue(disabled[1].key, "rtx.atmosphere.cloudEnabled", "disabled cloud enabled key");
  requireValue(disabled[1].value, "False", "disabled cloud enabled value");
  requireValue(disabled[2].key, "rtx.atmosphere.cloudRenderRTEnable", "disabled cloud rt key");
  requireValue(disabled[2].value, "False", "disabled cloud rt value");
  requireValue(disabled[3].key, "rtx.atmosphere.cloudSecondaryLutEnable", "disabled cloud secondary key");
  requireValue(disabled[3].value, "False", "disabled cloud secondary value");

  const auto enabled = mcrtx::remixAtmosphereCloudConfigValues(true);
  require(enabled.size() == 4, "enabled config count");
  requireValue(enabled[0].key, "rtx.skyMode", "enabled sky mode key");
  requireValue(enabled[0].value, "1", "enabled sky mode value");
  requireValue(enabled[1].key, "rtx.atmosphere.cloudEnabled", "enabled cloud enabled key");
  requireValue(enabled[1].value, "True", "enabled cloud enabled value");
  requireValue(enabled[2].key, "rtx.atmosphere.cloudRenderRTEnable", "enabled cloud rt key");
  requireValue(enabled[2].value, "True", "enabled cloud rt value");
  requireValue(enabled[3].key, "rtx.atmosphere.cloudSecondaryLutEnable", "enabled cloud secondary key");
  requireValue(enabled[3].value, "True", "enabled cloud secondary value");
  return 0;
}
