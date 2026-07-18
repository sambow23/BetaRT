#pragma once

#include <array>
#include <string_view>

namespace mcrtx {

struct RemixConfigValue {
  std::string_view key;
  std::string_view value;
};

using RemixAtmosphereCloudConfigValues = std::array<RemixConfigValue, 4>;

RemixAtmosphereCloudConfigValues remixAtmosphereCloudConfigValues(bool enabled);

}  // namespace mcrtx
