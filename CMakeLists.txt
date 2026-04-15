cmake_minimum_required(VERSION 3.24)

project(mc_rtx LANGUAGES CXX)

set(CMAKE_CXX_STANDARD 20)
set(CMAKE_CXX_STANDARD_REQUIRED ON)
set(CMAKE_CXX_EXTENSIONS OFF)

option(MCRTX_BUILD_JNI "Build the JNI Remix bridge library" ON)

if(MCRTX_BUILD_JNI)
  add_subdirectory(native)
endif()
