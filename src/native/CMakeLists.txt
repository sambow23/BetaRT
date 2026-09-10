set(MCRTX_DXVK_REMIX_ROOT "${PROJECT_SOURCE_DIR}/../dxvk-remix-gmod" CACHE PATH "Remix raster SDK checkout")
option(MCRTX_TERRAIN_SYNCHRONOUS "Build terrain on the submitting worker for comparisons" OFF)
option(MCRTX_ENABLE_TRACY "Enable optional native Tracy profiling" OFF)
set(MCRTX_TRACY_PORT "8087" CACHE STRING "Tracy TCP port")
find_package(Threads REQUIRED)

add_library(mcrtx_terrain STATIC
  src/chunks/terrain_pipeline.cpp
  src/chunks/remix_chunk_geometry.cpp
  src/chunks/remix_chunk_policy.cpp
  src/chunks/remix_block_geometry_effects.cpp
  src/chunks/remix_block_geometry_fixtures.cpp
  src/chunks/remix_block_geometry_fluids.cpp
  src/chunks/remix_block_geometry_natural.cpp
  src/chunks/remix_block_geometry_pistons.cpp
  src/chunks/remix_block_geometry_redstone.cpp
  src/chunks/remix_block_geometry_structures.cpp
  src/core/remix_geometry_common.cpp
  src/core/remix_render_utils.cpp
  src/core/runtime_config.cpp
  src/lifecycle/perf_log.cpp
)
set_target_properties(mcrtx_terrain PROPERTIES POSITION_INDEPENDENT_CODE ON)
target_include_directories(mcrtx_terrain PUBLIC
  ${CMAKE_CURRENT_SOURCE_DIR}/include ${MCRTX_DXVK_REMIX_ROOT}/public/include)
target_link_libraries(mcrtx_terrain PUBLIC Threads::Threads ${CMAKE_DL_LIBS})
if(MCRTX_TERRAIN_SYNCHRONOUS)
  target_compile_definitions(mcrtx_terrain PUBLIC MCRTX_TERRAIN_SYNCHRONOUS)
endif()
if(MCRTX_ENABLE_TRACY)
  if(NOT MCRTX_TRACY_PORT MATCHES "^[0-9]+$")
    message(FATAL_ERROR "MCRTX_TRACY_PORT must be numeric")
  endif()
  target_sources(mcrtx_terrain PRIVATE third_party/tracy/public/TracyClient.cpp)
  target_include_directories(mcrtx_terrain PRIVATE ${CMAKE_CURRENT_SOURCE_DIR}/third_party/tracy/public)
  target_compile_definitions(mcrtx_terrain PRIVATE MCRTX_ENABLE_TRACY TRACY_ENABLE TRACY_PORT=${MCRTX_TRACY_PORT})
  if(WIN32)
    target_link_libraries(mcrtx_terrain PUBLIC ws2_32 dbghelp)
  endif()
endif()

if(MCRTX_BUILD_JNI)
  find_package(JNI REQUIRED)
  # Companion SDKs need not implement advanced lighting/sampling statistics.
  include(CheckCXXSourceCompiles)
  set(CMAKE_REQUIRED_INCLUDES "${MCRTX_DXVK_REMIX_ROOT}/public/include")
  foreach(feature Lighting Sampling Sharc Shadow LocalLight)
    string(TOUPPER "${feature}" feature_upper)
    check_cxx_source_compiles("#include <remix/remix_native_ext.h>
      int main() { return sizeof(remixapi_${feature}StatisticsEXT) + sizeof(PFN_remixapi_Get${feature}StatisticsEXT); }"
      MCRTX_HAS_${feature_upper}_STATISTICS)
  endforeach()
  unset(CMAKE_REQUIRED_INCLUDES)
  add_library(mcrtx_jni SHARED
    src/direct/submission.cpp src/direct/protocol.cpp src/direct/runtime.cpp
    src/direct/jni.cpp src/direct/jni_terrain.cpp src/lifecycle/jni_perf.cpp)
  target_include_directories(mcrtx_jni PRIVATE ${JNI_INCLUDE_DIRS})
  target_compile_definitions(mcrtx_jni PRIVATE
    MCRTX_HAS_LOCALLIGHT_STATISTICS=$<BOOL:${MCRTX_HAS_LOCALLIGHT_STATISTICS}>
    MCRTX_HAS_SHADOW_STATISTICS=$<BOOL:${MCRTX_HAS_SHADOW_STATISTICS}>
    MCRTX_HAS_SHARC_STATISTICS=$<BOOL:${MCRTX_HAS_SHARC_STATISTICS}>
    MCRTX_HAS_LIGHTING_STATISTICS=$<BOOL:${MCRTX_HAS_LIGHTING_STATISTICS}>
    MCRTX_HAS_SAMPLING_STATISTICS=$<BOOL:${MCRTX_HAS_SAMPLING_STATISTICS}>)
  target_link_libraries(mcrtx_jni PRIVATE mcrtx_terrain)
  set_target_properties(mcrtx_jni PROPERTIES OUTPUT_NAME mcrtx_jni)
  install(TARGETS mcrtx_jni RUNTIME DESTINATION . LIBRARY DESTINATION .)
endif()

foreach(target mcrtx_terrain mcrtx_jni)
  if(TARGET ${target})
    target_compile_features(${target} PRIVATE cxx_std_20)
    if(WIN32)
      target_compile_definitions(${target} PRIVATE WIN32_LEAN_AND_MEAN NOMINMAX)
    endif()
    if(MSVC)
      target_compile_options(${target} PRIVATE /W4 /EHsc)
      set_property(TARGET ${target} PROPERTY MSVC_RUNTIME_LIBRARY "MultiThreaded$<$<CONFIG:Debug>:Debug>")
    else()
      target_compile_options(${target} PRIVATE -Wall -Wextra -Wpedantic)
    endif()
  endif()
endforeach()

if(MCRTX_BUILD_TESTS)
  add_executable(mcrtx_direct_submission_tests src/direct/submission.cpp src/direct/protocol.cpp tests/direct/submission_tests.cpp)
  target_include_directories(mcrtx_direct_submission_tests PRIVATE ${CMAKE_CURRENT_SOURCE_DIR}/include)
  target_link_libraries(mcrtx_direct_submission_tests PRIVATE Threads::Threads)
  add_test(NAME mcrtx_direct_submission_tests COMMAND mcrtx_direct_submission_tests)
  add_executable(mcrtx_terrain_tests tests/chunks/terrain_pipeline_tests.cpp tests/chunks/terrain_reference.cpp)
  target_link_libraries(mcrtx_terrain_tests PRIVATE mcrtx_terrain)
  add_test(NAME mcrtx_terrain_tests COMMAND mcrtx_terrain_tests)
endif()
