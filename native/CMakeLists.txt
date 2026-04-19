set(MCRTX_DXVK_REMIX_ROOT "${CMAKE_CURRENT_SOURCE_DIR}/../../dxvk-remix-gmod" CACHE PATH "Path to the dxvk-remix-gmod checkout")

find_package(JNI REQUIRED)

add_library(mcrtx_jni SHARED
  src/jni_bridge.cpp
  src/remix_renderer.cpp
  src/remix_chunk_mesh.cpp
  src/remix_materials.cpp
  src/remix_subsystem_meshes.cpp
  src/remix_window.cpp
  src/render_internals.cpp
  src/perf_log.cpp
)

target_include_directories(mcrtx_jni
  PRIVATE
    ${JNI_INCLUDE_DIRS}
    ${CMAKE_CURRENT_SOURCE_DIR}/include
    ${MCRTX_DXVK_REMIX_ROOT}/public/include
)

target_compile_definitions(mcrtx_jni
  PRIVATE
    WIN32_LEAN_AND_MEAN
    NOMINMAX
)

target_compile_features(mcrtx_jni PRIVATE cxx_std_20)

if(MSVC)
  set_property(TARGET mcrtx_jni PROPERTY MSVC_RUNTIME_LIBRARY "MultiThreaded$<$<CONFIG:Debug>:Debug>")
endif()

if(MSVC)
  target_compile_options(mcrtx_jni PRIVATE /W4 /EHsc)
else()
  target_compile_options(mcrtx_jni PRIVATE -Wall -Wextra -Wpedantic)
endif()

set_target_properties(mcrtx_jni PROPERTIES OUTPUT_NAME mcrtx_jni)
