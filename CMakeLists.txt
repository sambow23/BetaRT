cmake_minimum_required(VERSION 3.24)

project(mc_rtx LANGUAGES CXX)

set(CMAKE_CXX_STANDARD 20)
set(CMAKE_CXX_STANDARD_REQUIRED ON)
set(CMAKE_CXX_EXTENSIONS OFF)

option(MCRTX_BUILD_JNI "Build the JNI Remix bridge library" ON)
option(MCRTX_BUILD_TESTS "Build native tests" OFF)

if(MCRTX_BUILD_TESTS)
  enable_testing()
endif()

if(MCRTX_BUILD_JNI OR MCRTX_BUILD_TESTS)
  add_subdirectory(src/native native)
endif()
