#pragma once

#include <filesystem>
#include <string>
#include <string_view>

namespace mcrtx::detail {

bool isTruthyEnvValue(const char* envValue);
std::string readEnvironmentVariable(const char* name);
std::filesystem::path getRuntimeConfigPath();
bool isVerboseLoggingEnabled();
bool equalsIgnoreCase(std::string_view left, std::string_view right);
std::filesystem::path getCurrentModuleDirectory();

}  // namespace mcrtx::detail
