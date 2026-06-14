if(MCRTX_BUILD_JNI)
set(MCRTX_DXVK_REMIX_ROOT "${CMAKE_CURRENT_SOURCE_DIR}/../../dxvk-remix-gmod" CACHE PATH "Path to the dxvk-remix-gmod checkout")

option(MCRTX_ENABLE_TRACY "Enable Tracy profiling in the native JNI bridge" ON)
set(MCRTX_TRACY_PORT "8087" CACHE STRING "Default Tracy port for mc-rtx native profiling")

find_package(Git QUIET)

set(MCRTX_BUILD_ID "unknown")
if(GIT_FOUND)
  execute_process(
    COMMAND "${GIT_EXECUTABLE}" -C "${PROJECT_SOURCE_DIR}" describe --tags --exact-match
    RESULT_VARIABLE MCRTX_GIT_TAG_RESULT
    OUTPUT_VARIABLE MCRTX_BUILD_ID
    OUTPUT_STRIP_TRAILING_WHITESPACE
    ERROR_QUIET
  )

  if(NOT MCRTX_GIT_TAG_RESULT EQUAL 0 OR MCRTX_BUILD_ID STREQUAL "")
    execute_process(
      COMMAND "${GIT_EXECUTABLE}" -C "${PROJECT_SOURCE_DIR}" rev-parse --short=12 HEAD
      RESULT_VARIABLE MCRTX_GIT_COMMIT_RESULT
      OUTPUT_VARIABLE MCRTX_BUILD_ID
      OUTPUT_STRIP_TRAILING_WHITESPACE
      ERROR_QUIET
    )

    if(NOT MCRTX_GIT_COMMIT_RESULT EQUAL 0 OR MCRTX_BUILD_ID STREQUAL "")
      set(MCRTX_BUILD_ID "unknown")
    endif()
  endif()
endif()

configure_file(
  "${CMAKE_CURRENT_SOURCE_DIR}/include/mcrtx/build_version.hpp.in"
  "${CMAKE_CURRENT_BINARY_DIR}/generated/mcrtx/build_version.hpp"
  @ONLY
)

if(MCRTX_ENABLE_TRACY AND NOT MCRTX_TRACY_PORT MATCHES "^[0-9]+$")
  message(FATAL_ERROR "MCRTX_TRACY_PORT must be a numeric TCP port")
endif()

find_package(JNI REQUIRED)

add_library(mcrtx_jni SHARED
  src/jni_bridge.cpp
  src/remix_renderer.cpp
  src/remix_chunk_mesh.cpp
  src/remix_materials.cpp
  src/remix_subsystem_meshes.cpp
  src/remix_window.cpp
  src/render_internals.cpp
  src/world_origin.cpp
  src/perf_log.cpp
)

target_include_directories(mcrtx_jni
  PRIVATE
    ${JNI_INCLUDE_DIRS}
    ${CMAKE_CURRENT_SOURCE_DIR}/include
    ${CMAKE_CURRENT_BINARY_DIR}/generated
    ${MCRTX_DXVK_REMIX_ROOT}/public/include
)

if(MCRTX_ENABLE_TRACY)
  target_sources(mcrtx_jni PRIVATE
    third_party/tracy/public/TracyClient.cpp
  )
  target_include_directories(mcrtx_jni PRIVATE
    ${CMAKE_CURRENT_SOURCE_DIR}/third_party/tracy/public
  )
endif()

target_compile_definitions(mcrtx_jni
  PRIVATE
    WIN32_LEAN_AND_MEAN
    NOMINMAX
)

if(MCRTX_ENABLE_TRACY)
  target_compile_definitions(mcrtx_jni
    PRIVATE
      MCRTX_ENABLE_TRACY
      TRACY_ENABLE
      TRACY_PORT=${MCRTX_TRACY_PORT}
  )
endif()

target_compile_features(mcrtx_jni PRIVATE cxx_std_20)

if(JAVA_AWT_LIBRARY)
  target_link_libraries(mcrtx_jni PRIVATE ${JAVA_AWT_LIBRARY})
endif()

if(WIN32)
  target_link_libraries(mcrtx_jni PRIVATE ws2_32)
endif()

if(MSVC)
  set_property(TARGET mcrtx_jni PROPERTY MSVC_RUNTIME_LIBRARY "MultiThreaded$<$<CONFIG:Debug>:Debug>")
endif()

if(MSVC)
  target_compile_options(mcrtx_jni PRIVATE /W4 /EHsc)
else()
  target_compile_options(mcrtx_jni PRIVATE -Wall -Wextra -Wpedantic)
endif()

set_target_properties(mcrtx_jni PROPERTIES OUTPUT_NAME mcrtx_jni)
endif()

if(MCRTX_BUILD_TESTS)
  add_executable(mcrtx_world_origin_tests
    src/world_origin.cpp
    tests/world_origin_tests.cpp
  )
  target_include_directories(mcrtx_world_origin_tests
    PRIVATE
      ${CMAKE_CURRENT_SOURCE_DIR}/include
  )
  target_compile_features(mcrtx_world_origin_tests PRIVATE cxx_std_20)
  if(MSVC)
    target_compile_options(mcrtx_world_origin_tests PRIVATE /W4 /EHsc)
  else()
    target_compile_options(mcrtx_world_origin_tests PRIVATE -Wall -Wextra -Wpedantic)
  endif()
  add_test(NAME mcrtx_world_origin_tests COMMAND mcrtx_world_origin_tests)
endif()
