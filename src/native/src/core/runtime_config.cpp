// Runtime config-file and process-environment access.

#include "mcrtx/core/runtime_config.hpp"

#include <cctype>
#include <cstddef>
#include <cstdlib>
#include <fstream>
#include <string>
#include <unordered_map>
#include <vector>

#include <windows.h>

namespace mcrtx::detail {

namespace {

constexpr wchar_t kRuntimeConfigFileName[] = L"mcrtx-runtime.env";

std::string trimAsciiWhitespace(std::string value) {
  const std::size_t first = value.find_first_not_of(" \t\r\n");
  if (first == std::string::npos) {
    return {};
  }

  const std::size_t last = value.find_last_not_of(" \t\r\n");
  return value.substr(first, last - first + 1);
}

std::unordered_map<std::string, std::string> loadRuntimeConfigValues() {
  std::unordered_map<std::string, std::string> values;
  const std::filesystem::path configPath = getRuntimeConfigPath();
  if (configPath.empty() || !std::filesystem::is_regular_file(configPath)) {
    return values;
  }

  std::ifstream stream(configPath);
  if (!stream.is_open()) {
    return values;
  }

  std::string line;
  while (std::getline(stream, line)) {
    const std::string trimmedLine = trimAsciiWhitespace(line);
    if (trimmedLine.empty() || trimmedLine[0] == '#') {
      continue;

    }

    const std::size_t separatorIndex = trimmedLine.find('=');
    if (separatorIndex == std::string::npos || separatorIndex == 0) {
      continue;
    }

    const std::string key = trimAsciiWhitespace(trimmedLine.substr(0, separatorIndex));
    const std::string value = trimAsciiWhitespace(trimmedLine.substr(separatorIndex + 1));
    if (!key.empty()) {
      values[key] = value;
    }
  }

  return values;
}

const std::unordered_map<std::string, std::string>& runtimeConfigValues() {
  static const std::unordered_map<std::string, std::string> values = loadRuntimeConfigValues();
  return values;
}

}  // namespace

bool isTruthyEnvValue(const char* envValue) {
  if (envValue == nullptr || envValue[0] == '\0') {
    return false;
  }

  const char firstCharacter = envValue[0];
  return firstCharacter == '1'
      || firstCharacter == 't'
      || firstCharacter == 'T'
      || firstCharacter == 'y'
      || firstCharacter == 'Y';
}

std::string readEnvironmentVariable(const char* name) {
  const auto& configuredValues = runtimeConfigValues();
  const auto configuredValue = configuredValues.find(name);
  if (configuredValue != configuredValues.end() && !configuredValue->second.empty()) {
    return configuredValue->second;
  }

  char* envValue = nullptr;
  std::size_t envValueLength = 0;
  if (_dupenv_s(&envValue, &envValueLength, name) != 0 || envValue == nullptr || envValueLength == 0) {
    return {};
  }


  std::string value(envValue);
  std::free(envValue);
  return value;
}

std::filesystem::path getRuntimeConfigPath() {
  std::vector<wchar_t> buffer(MAX_PATH);
  DWORD length = GetCurrentDirectoryW(static_cast<DWORD>(buffer.size()), buffer.data());
  if (length == 0) {
    return {};
  }

  if (length >= buffer.size()) {
    buffer.resize(length + 1);
    length = GetCurrentDirectoryW(static_cast<DWORD>(buffer.size()), buffer.data());
    if (length == 0 || length >= buffer.size()) {
      return {};
    }
  }

  return std::filesystem::path(std::wstring(buffer.data(), length)) / kRuntimeConfigFileName;
}

bool isVerboseLoggingEnabled() {
  static const bool enabled = []() {
    const std::string value = readEnvironmentVariable("MCRTX_VERBOSE_LOG");
    return isTruthyEnvValue(value.c_str());
  }();
  return enabled;
}

bool equalsIgnoreCase(std::string_view left, std::string_view right) {
  if (left.size() != right.size()) {
    return false;
  }

  for (std::size_t index = 0; index < left.size(); ++index) {
    const unsigned char leftChar = static_cast<unsigned char>(left[index]);
    const unsigned char rightChar = static_cast<unsigned char>(right[index]);
    if (std::tolower(leftChar) != std::tolower(rightChar)) {
      return false;
    }
  }

  return true;
}

std::filesystem::path getCurrentModuleDirectory() {
  HMODULE moduleHandle = nullptr;
  if (!GetModuleHandleExW(
          GET_MODULE_HANDLE_EX_FLAG_FROM_ADDRESS | GET_MODULE_HANDLE_EX_FLAG_UNCHANGED_REFCOUNT,
          reinterpret_cast<LPCWSTR>(&getCurrentModuleDirectory),
          &moduleHandle)) {

    return {};
  }

  std::wstring buffer(MAX_PATH, L'\0');
  DWORD length = 0;
  while (true) {
    length = GetModuleFileNameW(moduleHandle, buffer.data(), static_cast<DWORD>(buffer.size()));
    if (length == 0) {
      return {};
    }
    if (length < buffer.size() - 1) {
      break;
    }
    buffer.resize(buffer.size() * 2);
  }

  buffer.resize(length);
  return std::filesystem::path(buffer).parent_path();
}

}  // namespace mcrtx::detail
