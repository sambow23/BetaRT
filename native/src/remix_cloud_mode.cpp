#include "mcrtx/remix_cloud_mode.hpp"

namespace mcrtx {

RemixAtmosphereCloudConfigValues remixAtmosphereCloudConfigValues(bool enabled) {
  const std::string_view cloudValue = enabled ? "True" : "False";
  return {{
      {"rtx.skyMode", "1"},
      {"rtx.atmosphere.cloudEnabled", cloudValue},
      {"rtx.atmosphere.cloudRenderRTEnable", cloudValue},
      {"rtx.atmosphere.cloudSecondaryLutEnable", cloudValue},
  }};
}

}  // namespace mcrtx
