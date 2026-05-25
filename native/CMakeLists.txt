set(MCRTX_DXVK_REMIX_ROOT "${CMAKE_CURRENT_SOURCE_DIR}/../../dxvk-remix-gmod" CACHE PATH "Path to the dxvk-remix-gmod checkout")

option(MCRTX_ENABLE_TRACY "Enable Tracy profiling in the native JNI bridge" ON)
set(MCRTX_TRACY_PORT "8087" CACHE STRING "Default Tracy port for mc-rtx native profiling")

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
  src/perf_log.cpp
)

target_include_directories(mcrtx_jni
  PRIVATE
    ${JNI_INCLUDE_DIRS}
    ${CMAKE_CURRENT_SOURCE_DIR}/include
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
