if(MCRTX_BUILD_JNI)
set(MCRTX_DXVK_REMIX_ROOT "${PROJECT_SOURCE_DIR}/../dxvk-remix-gmod" CACHE PATH "Path to the dxvk-remix-gmod checkout")

option(MCRTX_ENABLE_TRACY "Enable Tracy profiling in the native JNI bridge" OFF)
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
  "${CMAKE_CURRENT_SOURCE_DIR}/include/mcrtx/core/build_version.hpp.in"
  "${CMAKE_CURRENT_BINARY_DIR}/generated/mcrtx/core/build_version.hpp"
  @ONLY
)

if(MCRTX_ENABLE_TRACY AND NOT MCRTX_TRACY_PORT MATCHES "^[0-9]+$")
  message(FATAL_ERROR "MCRTX_TRACY_PORT must be a numeric TCP port")
endif()

find_package(JNI REQUIRED)

add_library(mcrtx_jni SHARED
  src/lifecycle/jni_lifecycle.cpp
  src/lifecycle/jni_perf.cpp
  src/scene/jni_scene.cpp
  src/ui/jni_ui.cpp
  src/entities/jni_dynamic_entity.cpp
  src/particles/jni_particle_overlay.cpp
  src/chunks/jni_chunk.cpp
  src/settings/jni_settings_gameplay.cpp
  src/settings/jni_settings_graphics.cpp
  src/settings/jni_settings_debug.cpp
  src/settings/jni_settings_material.cpp
  src/scene/celestial_textures.cpp
  src/scene/remix_cloud_mode.cpp
  src/lifecycle/remix_renderer_frame.cpp
  src/lifecycle/remix_renderer_present.cpp
  src/scene/remix_renderer_scene.cpp
  src/lifecycle/remix_renderer_lifecycle.cpp
  src/ui/remix_renderer_ui.cpp
  src/core/remix_renderer_resources.cpp
  src/settings/remix_settings_gameplay.cpp
  src/settings/remix_settings_graphics.cpp
  src/settings/remix_settings_debug.cpp
  src/settings/remix_settings_material.cpp
  src/chunks/remix_chunk_policy.cpp
  src/core/remix_geometry_common.cpp
  src/chunks/remix_block_geometry_natural.cpp
  src/chunks/remix_block_geometry_structures.cpp
  src/chunks/remix_block_geometry_fluids.cpp
  src/chunks/remix_block_geometry_effects.cpp
  src/chunks/remix_block_geometry_fixtures.cpp
  src/chunks/remix_block_geometry_redstone.cpp
  src/chunks/remix_block_geometry_pistons.cpp
  src/chunks/remix_chunk_capture.cpp
  src/chunks/remix_chunk_occupancy.cpp
  src/chunks/remix_chunk_geometry.cpp
  src/chunks/remix_chunk_submission.cpp
  src/chunks/remix_chunk_residency.cpp
  src/materials/remix_materials.cpp
  src/materials/remix_material_paths.cpp
  src/materials/remix_material_terrain.cpp
  src/particles/remix_material_block_outline.cpp
  src/entities/remix_material_dynamic.cpp
  src/particles/remix_material_particles.cpp
  src/core/remix_subsystem_meshes.cpp
  src/scene/remix_subsystem_config.cpp
  src/entities/remix_subsystem_dynamic.cpp
  src/scene/remix_subsystem_lights.cpp
  src/particles/remix_overlay_destroy.cpp
  src/particles/remix_overlay_outline.cpp
  src/particles/remix_overlay_fire.cpp
  src/particles/remix_overlay_particles.cpp
  src/scene/remix_subsystem_world.cpp
  src/platform/remix_window_internals.cpp
  src/platform/remix_window_output.cpp
  src/platform/remix_window_input.cpp
  src/core/runtime_config.cpp
  src/core/remix_render_utils.cpp
  src/scene/remix_light_common.cpp
  src/core/world_origin.cpp
  src/lifecycle/perf_log.cpp
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
    src/core/world_origin.cpp
    tests/core/world_origin_tests.cpp
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

  add_executable(mcrtx_remix_cloud_mode_tests
    src/scene/remix_cloud_mode.cpp
    tests/scene/remix_cloud_mode_tests.cpp
  )
  target_include_directories(mcrtx_remix_cloud_mode_tests
    PRIVATE
      ${CMAKE_CURRENT_SOURCE_DIR}/include
  )
  target_compile_features(mcrtx_remix_cloud_mode_tests PRIVATE cxx_std_20)
  if(MSVC)
    target_compile_options(mcrtx_remix_cloud_mode_tests PRIVATE /W4 /EHsc)
  else()
    target_compile_options(mcrtx_remix_cloud_mode_tests PRIVATE -Wall -Wextra -Wpedantic)
  endif()
  add_test(NAME mcrtx_remix_cloud_mode_tests COMMAND mcrtx_remix_cloud_mode_tests)

  add_executable(mcrtx_celestial_texture_tests
    src/scene/celestial_textures.cpp
    tests/scene/celestial_textures_tests.cpp
  )
  target_include_directories(mcrtx_celestial_texture_tests
    PRIVATE
      ${CMAKE_CURRENT_SOURCE_DIR}/include
  )
  target_compile_features(mcrtx_celestial_texture_tests PRIVATE cxx_std_20)
  if(MSVC)
    target_compile_options(mcrtx_celestial_texture_tests PRIVATE /W4 /EHsc)
  else()
    target_compile_options(mcrtx_celestial_texture_tests PRIVATE -Wall -Wextra -Wpedantic)
  endif()
  add_test(NAME mcrtx_celestial_texture_tests COMMAND mcrtx_celestial_texture_tests)
endif()
