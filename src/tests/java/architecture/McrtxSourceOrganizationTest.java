import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public final class McrtxSourceOrganizationTest {
  public static void main(String[] args) throws Exception {
    requireNoDirectFilesWithSuffix("src/java-src", ".java");
    requireNoFilesWithSuffixRecursively("src/java-src/mcrtx", ".java");
    requireNoFilesWithSuffixRecursively("src/java-src/org", ".java");
    requireNoFilesWithSuffixRecursively("src/java-src/paulscode", ".java");
    requireNoDirectFilesWithSuffix("src/tools-src", ".java");
    requireNoFilesWithSuffixRecursively("src/tools-src/mcrtx", ".java");
    requireNoDirectFilesWithSuffix("src/tests/java", ".java");
    requireNoDirectFilesWithSuffix("src/native/src", ".cpp");
    requireNoDirectFilesWithSuffix("src/native/tests", ".cpp");
    requireNoDirectFilesWithSuffix("src/native/include/mcrtx", ".hpp");
    requireNoDirectFilesWithSuffix("src/native/include/mcrtx", ".in");

    requireFile("src/java-src/lifecycle/MinecraftRemixLifecycleHooks.java");
    requireFile("src/java-src/scene/MinecraftRemixSceneHooks.java");
    requireFile("src/java-src/ui/MinecraftRemixUiHooks.java");
    requireFile("src/java-src/entities/MinecraftRemixEntityHooks.java");
    requireFile("src/java-src/particles/MinecraftRemixParticleHooks.java");
    requireFile("src/java-src/chunks/MinecraftRemixChunkHooks.java");
    requireFile("src/java-src/platform/mcrtx/bridge/MinecraftPlatform.java");
    requireFile("src/java-src/core/mcrtx/bridge/RemixBridgeNative.java");
    requireFile("src/java-src/settings/McrtxSettingsCategories.java");
    requireFile("src/tools-src/patcher/mcrtx/tools/ClientPatchTool.java");
    requireFile("src/tests/java/settings/McrtxCloudModeTest.java");
    requireFile("src/tests/java/scene/McrtxCelestialAssetExportTest.java");
    requireFile("src/tests/java/ui/McrtxNameTagCaptureTest.java");
    requireFile("src/tests/java/entities/SheepWoolColorTest.java");
    requireMissingFile("src/java-src/MinecraftRemixHooks.java");

    String lifecycleHooks = read("src/java-src/lifecycle/MinecraftRemixLifecycleHooks.java");
    requireContains(lifecycleHooks, "public static void onDisplayCreated", "display-created hook ABI");
    requireContains(lifecycleHooks, "public static String onScreenshot", "screenshot hook ABI");
    requireContains(lifecycleHooks, "public static void onRemixUiTick", "remix UI tick hook ABI");
    requireContains(lifecycleHooks, "public static void onFrameRenderStart", "frame-start hook ABI");
    requireContains(lifecycleHooks, "McrtxHookScreenshotHelper.requestPresentedScreenshot", "screenshot helper delegation");
    requireContains(lifecycleHooks, "McrtxHookPerfTracker.", "perf helper delegation");
    requireContains(lifecycleHooks, "McrtxSettingsCategories.applySavedSettings()", "category settings startup application");

    String sceneHooks = read("src/java-src/scene/MinecraftRemixSceneHooks.java");
    String uiHooks = read("src/java-src/ui/MinecraftRemixUiHooks.java");
    String entityHooks = read("src/java-src/entities/MinecraftRemixEntityHooks.java");
    String particleHooks = read("src/java-src/particles/MinecraftRemixParticleHooks.java");
    String chunkHooks = read("src/java-src/chunks/MinecraftRemixChunkHooks.java");
    requireContains(sceneHooks, "public static void onCamera", "camera hook ABI");
    requireContains(sceneHooks, "public static void drawTessellator", "scene draw hook ABI");
    requireContains(uiHooks, "public static void onNameTagRenderBegin", "name-tag hook ABI");
    requireContains(uiHooks, "public static boolean captureFontStringAndMaybeSuppress", "font hook ABI");
    requireContains(entityHooks, "public static void onLivingEntityRenderStart", "entity hook ABI");
    requireContains(entityHooks, "public static void onFirstPersonTessellatorDraw", "first-person hook ABI");
    requireContains(particleHooks, "public static void onWeatherTextureBind", "weather hook ABI");
    requireContains(chunkHooks, "public static boolean onChunkBuildBegin", "chunk hook ABI");

    String patchTool = read("src/tools-src/patcher/mcrtx/tools/ClientPatchTool.java");
    requireContains(patchTool, "LIFECYCLE_HOOKS_CLASS", "lifecycle patch owner");
    requireContains(patchTool, "SCENE_HOOKS_CLASS", "scene patch owner");
    requireContains(patchTool, "UI_HOOKS_CLASS", "UI patch owner");
    requireContains(patchTool, "ENTITY_HOOKS_CLASS", "entity patch owner");
    requireContains(patchTool, "PARTICLE_HOOKS_CLASS", "particle patch owner");
    requireContains(patchTool, "CHUNK_HOOKS_CLASS", "chunk patch owner");
    requireNotContains(patchTool, "REMIX_HELPER_CLASS", "generic hook owner removed");
    requireNotContains(patchTool, "RENDER_HOOKS_CLASS", "mixed renderer owner removed");

    requireFile("src/java-src/lifecycle/McrtxHookScreenshotHelper.java");
    requireFile("src/java-src/lifecycle/McrtxHookPerfTracker.java");
    requireFile("src/java-src/settings/McrtxSettingsCategoryUi.java");
    requireFile("src/java-src/settings/McrtxSettingsCategories.java");
    requireFile("src/java-src/settings/McrtxGameplaySettingsUi.java");
    requireFile("src/java-src/settings/McrtxGraphicsSettingsUi.java");
    requireFile("src/java-src/settings/McrtxDebugSettingsUi.java");
    requireFile("src/java-src/settings/McrtxMaterialSettingsUi.java");

    requireFile("src/java-src/settings/mcrtx/bridge/McrtxSettingsStore.java");
    requireFile("src/java-src/settings/mcrtx/bridge/McrtxGameplaySettings.java");
    requireFile("src/java-src/settings/mcrtx/bridge/McrtxGraphicsSettings.java");
    requireFile("src/java-src/settings/mcrtx/bridge/McrtxDebugSettings.java");
    requireFile("src/java-src/settings/mcrtx/bridge/McrtxMaterialSettings.java");
    requireFile("src/java-src/settings/mcrtx/bridge/McrtxGameplaySettingsNative.java");
    requireFile("src/java-src/settings/mcrtx/bridge/McrtxGraphicsSettingsNative.java");
    requireFile("src/java-src/settings/mcrtx/bridge/McrtxDebugSettingsNative.java");
    requireFile("src/java-src/settings/mcrtx/bridge/McrtxMaterialSettingsNative.java");
    requireFile("src/java-src/settings/mcrtx/bridge/McrtxRuntimeSettingParser.java");
    requireFile("src/java-src/settings/mcrtx/bridge/McrtxRuntimeSettingFormatter.java");
    requireFile("src/java-src/lifecycle/mcrtx/bridge/RemixLifecycleBridge.java");
    requireFile("src/java-src/lifecycle/mcrtx/bridge/McrtxPerfNative.java");
    requireFile("src/native/include/mcrtx/core/jni_helpers.hpp");
    requireFile("src/native/src/lifecycle/jni_lifecycle.cpp");
    requireFile("src/native/src/lifecycle/jni_perf.cpp");
    requireFile("src/java-src/scene/mcrtx/bridge/RemixSceneBridge.java");
    requireFile("src/native/src/scene/jni_scene.cpp");
    requireFile("src/java-src/ui/mcrtx/bridge/RemixUiBridge.java");
    requireFile("src/native/src/ui/jni_ui.cpp");
    requireFile("src/java-src/entities/mcrtx/bridge/RemixDynamicEntityBridge.java");
    requireFile("src/native/src/entities/jni_dynamic_entity.cpp");
    requireFile("src/java-src/particles/mcrtx/bridge/RemixParticleOverlayBridge.java");
    requireFile("src/native/src/particles/jni_particle_overlay.cpp");
    requireFile("src/java-src/chunks/mcrtx/bridge/RemixChunkBridge.java");
    requireFile("src/native/src/chunks/jni_chunk.cpp");

    String lifecycleBridge = read("src/java-src/lifecycle/mcrtx/bridge/RemixLifecycleBridge.java");
    String perfNative = read("src/java-src/lifecycle/mcrtx/bridge/McrtxPerfNative.java");
    requireContains(lifecycleBridge, "private static native boolean nInitialize", "lifecycle owns initialization JNI");
    requireContains(lifecycleBridge, "public static boolean isInitialized()", "shared initialized predicate");
    requireContains(perfNative, "nRecordJavaSampleBatch", "profiler owns batch JNI");

    requireMissingFile("src/java-src/McrtxHookSettingsUi.java");
    requireMissingFile("src/java-src/McrtxFovSlider.java");
    requireMissingFile("src/java-src/McrtxWaterTransmissionSlider.java");
    requireMissingFile("src/java-src/mcrtx/bridge/McrtxRuntimeSettings.java");
    requireMissingFile("src/java-src/mcrtx/bridge/McrtxCloudMode.java");
    requireMissingFile("src/java-src/mcrtx/bridge/MinecraftRenderHooks.java");
    requireMissingFile("src/native/src/jni_bridge.cpp");

    String nativeBridge = read("src/java-src/core/mcrtx/bridge/RemixBridgeNative.java");
    requireNotContains(nativeBridge, "nSetPlayerShadowsEnabled", "settings declaration removed from native loader");
    requireNotContains(nativeBridge, "nSetWaterTransmissionSettings", "material declaration removed from native loader");
    requireNotContains(nativeBridge, " native ", "native loader has no JNI declarations");

    String dynamicCapture = read("src/java-src/entities/RemixDynamicEntityCapture.java");
    requireContains(dynamicCapture, "public final class RemixDynamicEntityCapture", "dynamic capture facade");
    requireContains(dynamicCapture, "public static void onLivingEntityRenderStart", "living entity hook ABI");
    requireContains(dynamicCapture, "public static boolean onModelPartRender", "model-part hook ABI");
    requireContains(dynamicCapture, "RemixLivingEntityCapture.onRenderStart", "living capture facade delegation");
    requireContains(dynamicCapture, "RemixDynamicModelCapture.captureModelPart", "model capture facade delegation");
    requireContains(dynamicCapture, "RemixSignCapture.captureSignModelRender", "sign capture facade delegation");
    requireContains(dynamicCapture, "RemixFirstPersonCapture.onShadowPlayerRender", "first-person facade delegation");
    requireFile("src/java-src/entities/RemixDynamicEntitySession.java");
    requireFile("src/java-src/entities/RemixDynamicModelCapture.java");
    requireFile("src/java-src/entities/RemixLivingEntityCapture.java");
    requireFile("src/java-src/entities/RemixItemEntityCapture.java");
    requireFile("src/java-src/entities/RemixHeldItemCapture.java");
    requireFile("src/java-src/entities/RemixFirstPersonCapture.java");
    requireFile("src/java-src/entities/RemixSignCapture.java");
    requireFile("src/java-src/entities/RemixPaintingCapture.java");
    requireFile("src/java-src/entities/RemixEntityFireCapture.java");
    requireContains(read("src/java-src/entities/RemixDynamicEntitySession.java"),
        "RemixDynamicEntityBridge.beginDynamicEntityFrame", "dynamic session owns frame start");
    requireContains(read("src/java-src/entities/RemixDynamicModelCapture.java"),
        "captureDynamicEntityQuadBatch", "dynamic model owns batched model capture");
    requireContains(read("src/java-src/entities/RemixLivingEntityCapture.java"),
        "livingEntityHurtTimeField", "living capture owns hurt reflection");
    requireContains(read("src/java-src/entities/RemixSignCapture.java"),
        "MODEL_MESH_CACHE", "sign capture owns model cache");
    requireContains(read("src/java-src/entities/RemixEntityFireCapture.java"),
        "FIRE_ANIMATION_FRAME_COUNT", "fire capture owns animation policy");
    requireNotContains(dynamicCapture, "dynamicCaptureFrameActive", "frame state removed from facade");
    requireNotContains(dynamicCapture, "textureAlphaCache", "texture cache removed from facade");
    requireNotContains(dynamicCapture, "livingEntityHurtTimeField", "living reflection removed from facade");
    requireNotContains(dynamicCapture, "signModelMeshCache", "sign cache removed from facade");
    requireNotContains(dynamicCapture, "firstPersonShadowOverlayInverse", "first-person state removed from facade");

    String chunkCapture = read("src/java-src/chunks/RemixChunkCapture.java");
    requireContains(chunkCapture, "public final class RemixChunkCapture", "chunk capture facade");
    requireContains(chunkCapture, "RemixChunkBuildSession.begin", "chunk build facade delegation");
    requireContains(chunkCapture, "RemixChunkWorldState.onWorldChanged", "chunk world facade delegation");
    requireContains(chunkCapture, "RemixChunkRecaptureQueue.flush", "chunk queue facade delegation");
    requireFile("src/java-src/chunks/RemixChunkBuildSession.java");
    requireFile("src/java-src/chunks/RemixChunkBlockCapture.java");
    requireFile("src/java-src/chunks/RemixChunkWorldState.java");
    requireFile("src/java-src/chunks/RemixChunkSectionKey.java");
    requireFile("src/java-src/chunks/RemixChunkRecapturePass.java");
    requireFile("src/java-src/chunks/RemixChunkRecaptureQueue.java");
    requireFile("src/java-src/chunks/RemixChunkNeighborRefresh.java");
    requireContains(read("src/java-src/chunks/RemixChunkWorldState.java"),
        "KNOWN_CHUNK_SECTIONS", "chunk world state owns known sections");
    requireContains(read("src/java-src/chunks/RemixChunkSectionKey.java"),
        "RemixCaveCulling.getChunkKey", "section key owns encoding");
    requireContains(read("src/java-src/chunks/RemixChunkBlockCapture.java"),
        "computeLiquidCornerHeight", "chunk block capture owns liquid policy");
    requireContains(read("src/java-src/chunks/RemixChunkRecapturePass.java"),
        "hook.chunkRecapture.pass.scanBlocks", "recapture pass owns pass profiling");
    requireContains(read("src/java-src/chunks/RemixChunkRecaptureQueue.java"),
        "PENDING_RECAPTURE_SECTIONS", "recapture queue owns pending sections");
    requireContains(read("src/java-src/chunks/RemixChunkNeighborRefresh.java"),
        "CRITICAL_DISTANCE_SQ", "neighbor refresh owns distance policy");
    requireNotContains(chunkCapture, "PENDING_RECAPTURE_SECTIONS", "queue state removed from facade");
    requireNotContains(chunkCapture, "KNOWN_CHUNK_SECTIONS", "world state removed from facade");
    requireNotContains(chunkCapture, "recaptureSectionsBudget", "budget state removed from facade");
    requireNotContains(chunkCapture, "computeLiquidCornerHeight", "liquid policy removed from facade");

    String uiCapture = read("src/java-src/ui/RemixUiCapture.java");
    requireContains(uiCapture, "RemixUiCaptureSession.begin", "UI facade delegates session begin");
    requireContains(uiCapture, "RemixUiModelCapture.onTessellatorDraw", "UI facade delegates model capture");
    requireContains(uiCapture, "RemixUiFontCapture.capture", "UI facade delegates font capture");
    requireFile("src/java-src/ui/RemixUiDrawList.java");
    requireFile("src/java-src/ui/RemixUiCaptureSession.java");
    requireFile("src/java-src/ui/RemixUiTextureRegistry.java");
    requireFile("src/java-src/ui/RemixUiProjection.java");
    requireFile("src/java-src/ui/RemixUiFontCapture.java");
    requireFile("src/java-src/ui/RemixUiModelCapture.java");
    requireFile("src/java-src/ui/RemixNameTagCapture.java");
    requireContains(read("src/java-src/ui/RemixUiDrawList.java"), "static final class Checkpoint", "draw-list checkpoint owner");
    requireContains(read("src/java-src/ui/RemixUiCaptureSession.java"), "RemixUiBridge.submitUiDrawList", "session owns final submission");
    requireContains(read("src/java-src/ui/RemixUiTextureRegistry.java"), "RemixUiBridge.registerUiTexture", "texture registry owns upload");
    requireContains(read("src/java-src/ui/RemixUiFontCapture.java"), "FONT_COLOR_CODES", "font capture owns formatting palette");
    requireContains(read("src/java-src/ui/RemixUiModelCapture.java"), "blockItemFaceLightFactor", "model capture owns item lighting");
    requireNotContains(uiCapture, "float[] xyzuv", "draw-list arrays removed from facade");
    requireNotContains(uiCapture, "UPLOADED_TEXTURES", "texture registry removed from facade");
    requireNotContains(uiCapture, "FONT_COLOR_CODES", "font policy removed from facade");
    requireNotContains(uiCapture, "NAME_TAG_DEPTH_BIAS", "name-tag policy removed from facade");
    requireNotContains(uiCapture, "ITEM_LIGHT_DIFFUSE", "model lighting removed from facade");

    String particleCapture = read("src/java-src/particles/RemixParticleCapture.java");
    requireContains(particleCapture, "RemixParticleOverlayBridge.captureParticleQuad", "particle capture uses particle bridge");

    requireFile("src/native/include/mcrtx/core/remix_renderer.hpp");
    requireFile("src/native/include/mcrtx/lifecycle/remix_renderer_frame.hpp");
    requireFile("src/native/include/mcrtx/scene/remix_renderer_scene.hpp");
    requireFile("src/native/include/mcrtx/entities/remix_renderer_dynamic.hpp");
    requireFile("src/native/include/mcrtx/particles/remix_renderer_overlay.hpp");
    requireFile("src/native/include/mcrtx/materials/remix_material_common.hpp");
    requireFile("src/native/include/mcrtx/platform/remix_window_internals.hpp");
    requireFile("src/native/tests/core/world_origin_tests.cpp");
    requireFile("src/native/tests/scene/remix_cloud_mode_tests.cpp");
    requireFile("src/native/src/settings/remix_settings_graphics.cpp");
    requireFile("src/native/src/materials/remix_materials.cpp");
    requireFile("src/native/src/materials/remix_material_paths.cpp");
    requireFile("src/native/src/materials/remix_material_terrain.cpp");
    requireFile("src/native/src/particles/remix_material_block_outline.cpp");
    requireFile("src/native/src/entities/remix_material_dynamic.cpp");
    requireFile("src/native/src/particles/remix_material_particles.cpp");
    requireFile("src/native/include/mcrtx/chunks/remix_chunk_policy.hpp");
    requireFile("src/native/include/mcrtx/core/remix_geometry_common.hpp");
    requireFile("src/native/include/mcrtx/chunks/remix_block_geometry_natural.hpp");
    requireFile("src/native/include/mcrtx/chunks/remix_block_geometry_structures.hpp");
    requireFile("src/native/include/mcrtx/chunks/remix_block_geometry_fluids.hpp");
    requireFile("src/native/include/mcrtx/chunks/remix_block_geometry_effects.hpp");
    requireFile("src/native/include/mcrtx/chunks/remix_block_geometry_fixtures.hpp");
    requireFile("src/native/include/mcrtx/chunks/remix_block_geometry_redstone.hpp");
    requireFile("src/native/include/mcrtx/chunks/remix_block_geometry_pistons.hpp");
    requireMissingFile("src/native/include/mcrtx/remix_block_geometry.hpp");
    requireFile("src/native/src/chunks/remix_chunk_policy.cpp");
    requireFile("src/native/src/core/remix_geometry_common.cpp");
    requireFile("src/native/src/chunks/remix_block_geometry_natural.cpp");
    requireFile("src/native/src/chunks/remix_block_geometry_structures.cpp");
    requireFile("src/native/src/chunks/remix_block_geometry_fluids.cpp");
    requireFile("src/native/src/chunks/remix_block_geometry_effects.cpp");
    requireFile("src/native/src/chunks/remix_block_geometry_fixtures.cpp");
    requireFile("src/native/src/chunks/remix_block_geometry_redstone.cpp");
    requireFile("src/native/src/chunks/remix_block_geometry_pistons.cpp");
    requireMissingFile("src/native/src/remix_block_geometry_devices.cpp");
    requireFile("src/native/include/mcrtx/chunks/remix_chunk_build.hpp");
    requireFile("src/native/src/chunks/remix_chunk_capture.cpp");
    requireFile("src/native/src/chunks/remix_chunk_occupancy.cpp");
    requireFile("src/native/src/chunks/remix_chunk_geometry.cpp");
    requireFile("src/native/src/chunks/remix_chunk_submission.cpp");
    requireMissingFile("src/native/src/remix_chunk_build.cpp");
    requireFile("src/native/src/chunks/remix_chunk_residency.cpp");
    requireMissingFile("src/native/src/remix_chunk_mesh.cpp");
    requireFile("src/native/include/mcrtx/lifecycle/remix_renderer_timing.hpp");
    requireFile("src/native/src/lifecycle/remix_renderer_frame.cpp");
    requireFile("src/native/src/lifecycle/remix_renderer_present.cpp");
    requireFile("src/native/src/scene/remix_renderer_scene.cpp");
    requireMissingFile("src/native/src/remix_renderer.cpp");
    requireFile("src/native/src/lifecycle/remix_renderer_lifecycle.cpp");
    requireFile("src/native/src/ui/remix_renderer_ui.cpp");
    requireFile("src/native/src/core/remix_renderer_resources.cpp");
    requireFile("src/native/include/mcrtx/platform/remix_window_internals.hpp");
    requireFile("src/native/src/platform/remix_window_internals.cpp");
    requireFile("src/native/src/platform/remix_window_output.cpp");
    requireFile("src/native/src/platform/remix_window_input.cpp");
    requireMissingFile("src/native/src/remix_window.cpp");
    requireFile("src/native/src/particles/remix_overlay_destroy.cpp");
    requireFile("src/native/src/particles/remix_overlay_outline.cpp");
    requireFile("src/native/src/particles/remix_overlay_fire.cpp");
    requireFile("src/native/src/particles/remix_overlay_particles.cpp");
    requireMissingFile("src/native/src/remix_subsystem_overlays.cpp");
    requireFile("src/native/include/mcrtx/core/runtime_config.hpp");
    requireFile("src/native/src/core/runtime_config.cpp");
    requireFile("src/native/include/mcrtx/core/remix_render_common.hpp");
    requireFile("src/native/src/core/remix_render_utils.cpp");
    requireFile("src/native/include/mcrtx/scene/remix_light_common.hpp");
    requireFile("src/native/src/scene/remix_light_common.cpp");
    requireMissingFile("src/native/include/mcrtx/render_internals.hpp");
    requireMissingFile("src/native/src/render_internals.cpp");

    String rendererHeader = read("src/native/include/mcrtx/core/remix_renderer.hpp");
    requireContains(rendererHeader, "#include \"mcrtx/lifecycle/remix_renderer_frame.hpp\"", "frame header include");
    requireContains(rendererHeader, "#include \"mcrtx/scene/remix_renderer_scene.hpp\"", "scene header include");
    requireContains(rendererHeader, "#include \"mcrtx/entities/remix_renderer_dynamic.hpp\"", "dynamic header include");
    requireContains(rendererHeader, "#include \"mcrtx/particles/remix_renderer_overlay.hpp\"", "overlay header include");
    requireContains(rendererHeader, "class RemixRenderer", "renderer facade class");

    String cmake = read("src/native/CMakeLists.txt");
    requireContains(cmake, "src/lifecycle/jni_lifecycle.cpp", "lifecycle JNI source in build");
    requireContains(cmake, "src/lifecycle/jni_perf.cpp", "profiler JNI source in build");
    requireContains(cmake, "src/scene/jni_scene.cpp", "scene JNI source in build");
    requireContains(cmake, "src/ui/jni_ui.cpp", "UI JNI source in build");
    requireContains(cmake, "src/entities/jni_dynamic_entity.cpp", "dynamic JNI source in build");
    requireContains(cmake, "src/particles/jni_particle_overlay.cpp", "particle JNI source in build");
    requireContains(cmake, "src/chunks/jni_chunk.cpp", "chunk JNI source in build");
    requireContains(cmake, "src/settings/jni_settings_gameplay.cpp", "gameplay JNI source in build");
    requireContains(cmake, "src/settings/jni_settings_graphics.cpp", "graphics JNI source in build");
    requireContains(cmake, "src/settings/jni_settings_debug.cpp", "debug JNI source in build");
    requireContains(cmake, "src/settings/jni_settings_material.cpp", "material JNI source in build");
    requireContains(cmake, "src/settings/remix_settings_gameplay.cpp", "gameplay renderer settings source in build");
    requireContains(cmake, "src/settings/remix_settings_graphics.cpp", "graphics renderer settings source in build");
    requireContains(cmake, "src/settings/remix_settings_debug.cpp", "debug renderer settings source in build");
    requireContains(cmake, "src/settings/remix_settings_material.cpp", "material renderer settings source in build");
    requireContains(cmake, "src/materials/remix_materials.cpp", "shared material helpers source in build");
    requireContains(cmake, "src/materials/remix_material_paths.cpp", "material paths source in build");
    requireContains(cmake, "src/materials/remix_material_terrain.cpp", "terrain material source in build");
    requireContains(cmake, "src/particles/remix_material_block_outline.cpp", "block-outline material source in build");
    requireContains(cmake, "src/entities/remix_material_dynamic.cpp", "dynamic material source in build");
    requireContains(cmake, "src/particles/remix_material_particles.cpp", "particle material source in build");
    requireContains(cmake, "src/chunks/remix_chunk_policy.cpp", "chunk policy source in build");
    requireContains(cmake, "src/core/remix_geometry_common.cpp", "common geometry source in build");
    requireContains(cmake, "src/chunks/remix_block_geometry_natural.cpp", "natural block geometry source in build");
    requireContains(cmake, "src/chunks/remix_block_geometry_structures.cpp", "structural block geometry source in build");
    requireContains(cmake, "src/chunks/remix_block_geometry_fluids.cpp", "fluid block geometry source in build");
    requireContains(cmake, "src/chunks/remix_block_geometry_effects.cpp", "effect block geometry source in build");
    requireContains(cmake, "src/chunks/remix_block_geometry_fixtures.cpp", "fixture block geometry source in build");
    requireContains(cmake, "src/chunks/remix_block_geometry_redstone.cpp", "redstone block geometry source in build");
    requireContains(cmake, "src/chunks/remix_block_geometry_pistons.cpp", "piston geometry source in build");
    requireNotContains(cmake, "src/remix_block_geometry_devices.cpp", "mixed device geometry removed from build");
    requireContains(cmake, "src/chunks/remix_chunk_capture.cpp", "chunk capture source in build");
    requireContains(cmake, "src/chunks/remix_chunk_occupancy.cpp", "chunk occupancy source in build");
    requireContains(cmake, "src/chunks/remix_chunk_geometry.cpp", "chunk geometry source in build");
    requireContains(cmake, "src/chunks/remix_chunk_submission.cpp", "chunk submission source in build");
    requireNotContains(cmake, "src/remix_chunk_build.cpp", "mixed chunk build source removed from build");
    requireContains(cmake, "src/chunks/remix_chunk_residency.cpp", "chunk residency source in build");
    requireNotContains(cmake, "src/remix_chunk_mesh.cpp", "mixed chunk source removed from build");
    requireContains(cmake, "src/lifecycle/remix_renderer_frame.cpp", "renderer frame source in build");
    requireContains(cmake, "src/lifecycle/remix_renderer_present.cpp", "renderer presentation source in build");
    requireContains(cmake, "src/scene/remix_renderer_scene.cpp", "renderer scene source in build");
    requireNotContains(cmake, "src/remix_renderer.cpp", "mixed renderer source removed from build");
    requireContains(cmake, "src/lifecycle/remix_renderer_lifecycle.cpp", "renderer lifecycle source in build");
    requireContains(cmake, "src/ui/remix_renderer_ui.cpp", "renderer UI source in build");
    requireContains(cmake, "src/core/remix_renderer_resources.cpp", "renderer resources source in build");
    requireContains(cmake, "src/platform/remix_window_internals.cpp", "window infrastructure source in build");
    requireContains(cmake, "src/platform/remix_window_output.cpp", "output window source in build");
    requireContains(cmake, "src/platform/remix_window_input.cpp", "native input source in build");
    requireNotContains(cmake, "src/remix_window.cpp", "mixed window source removed from build");
    requireContains(cmake, "src/scene/remix_subsystem_config.cpp", "native config source in build");
    requireContains(cmake, "src/entities/remix_subsystem_dynamic.cpp", "native dynamic source in build");
    requireContains(cmake, "src/scene/remix_subsystem_lights.cpp", "native lights source in build");
    requireContains(cmake, "src/particles/remix_overlay_destroy.cpp", "destroy overlay source in build");
    requireContains(cmake, "src/particles/remix_overlay_outline.cpp", "block outline source in build");
    requireContains(cmake, "src/particles/remix_overlay_fire.cpp", "fire overlay source in build");
    requireContains(cmake, "src/particles/remix_overlay_particles.cpp", "particle overlay source in build");
    requireNotContains(cmake, "src/remix_subsystem_overlays.cpp", "mixed overlay source removed from build");
    requireContains(cmake, "src/scene/remix_subsystem_world.cpp", "native world source in build");
    requireContains(cmake, "src/core/runtime_config.cpp", "runtime configuration source in build");
    requireContains(cmake, "src/core/remix_render_utils.cpp", "generic render utilities source in build");
    requireContains(cmake, "src/scene/remix_light_common.cpp", "shared light helpers source in build");
    requireNotContains(cmake, "src/render_internals.cpp", "catch-all render internals removed from build");

    String rendererFrame = read("src/native/src/lifecycle/remix_renderer_frame.cpp");
    String rendererPresent = read("src/native/src/lifecycle/remix_renderer_present.cpp");
    String rendererScene = read("src/native/src/scene/remix_renderer_scene.cpp");
    String rendererCore = rendererFrame + rendererPresent + rendererScene;
    String configSubsystem = read("src/native/src/scene/remix_subsystem_config.cpp");
    requireContains(configSubsystem, "RemixRenderer::setConfigVariableLocked", "config subsystem owns Remix config writes");
    requireContains(configSubsystem, "RemixRenderer::applyRtQualityConfigLocked", "config subsystem owns RT quality");
    requireContains(configSubsystem, "RemixRenderer::applyUpscalerConfigLocked", "config subsystem owns upscaler settings");
    requireContains(configSubsystem, "RemixRenderer::publishWorldRenderOriginLocked", "config subsystem owns origin publication");
    requireContains(configSubsystem, "RemixRenderer::updateAtmosphereConfigLocked", "config subsystem owns atmosphere values");
    requireContains(configSubsystem, "formatConfigFloat", "config subsystem owns float formatting");
    requireContains(configSubsystem, "dlssQualityConfigValue", "config subsystem owns DLSS preset conversion");
    requireContains(configSubsystem, "xessPresetConfigValue", "config subsystem owns XeSS preset conversion");
    requireContains(configSubsystem, "taauPresetConfigValue", "config subsystem owns TAA-U preset conversion");
    requireNotContains(rendererCore, "RemixRenderer::setConfigVariableLocked", "config writes removed from renderer core");
    requireNotContains(rendererCore, "RemixRenderer::applyUpscalerConfigLocked", "upscaler config removed from renderer core");
    requireNotContains(rendererCore, "RemixRenderer::publishWorldRenderOriginLocked", "origin publication removed from renderer core");
    requireNotContains(rendererCore, "formatConfigFloat", "config formatting removed from renderer core");

    String meshSubsystem = read("src/native/src/core/remix_subsystem_meshes.cpp");
    String dynamicSubsystem = read("src/native/src/entities/remix_subsystem_dynamic.cpp");
    String lightSubsystem = read("src/native/src/scene/remix_subsystem_lights.cpp");
    String destroyOverlay = read("src/native/src/particles/remix_overlay_destroy.cpp");
    String blockOutline = read("src/native/src/particles/remix_overlay_outline.cpp");
    String fireOverlay = read("src/native/src/particles/remix_overlay_fire.cpp");
    String particleOverlay = read("src/native/src/particles/remix_overlay_particles.cpp");
    String worldSubsystem = read("src/native/src/scene/remix_subsystem_world.cpp");
    requireContains(meshSubsystem, "RemixRenderer::rebuildMaterialDependentMeshesLocked", "mesh subsystem owns cross-subsystem rebuild orchestration");
    requireContains(dynamicSubsystem, "clearActiveDynamicEntityState", "dynamic subsystem owns build-state reset");
    requireContains(dynamicSubsystem, "RemixRenderer::beginDynamicEntity", "dynamic subsystem owns entity begin");
    requireContains(dynamicSubsystem, "RemixRenderer::findOrCreateDynamicEntityMesh", "dynamic subsystem owns mesh caching");
    requireContains(lightSubsystem, "makeLightLocalOriginInfo", "light subsystem owns local-origin metadata");
    requireContains(lightSubsystem, "RemixRenderer::createTorchLight", "light subsystem owns torch creation");
    requireContains(lightSubsystem, "RemixRenderer::reconcileHeldItemTorchLight", "light subsystem owns held-light reconciliation");
    requireContains(destroyOverlay, "makeDestroyOverlayMeshHash", "destroy overlay owns its mesh hash");
    requireContains(destroyOverlay, "RemixRenderer::beginDestroyOverlayFrame", "destroy overlay owns frame start");
    requireContains(destroyOverlay, "RemixRenderer::captureDestroyOverlay", "destroy overlay owns capture");
    requireContains(destroyOverlay, "RemixRenderer::rebuildDestroyOverlayMesh", "destroy overlay owns mesh rebuilding");
    requireContains(blockOutline, "blockOutlineStyleParametersFor", "block outline owns style policy");
    requireContains(blockOutline, "makeBlockOutlineMeshHash", "block outline owns its mesh hash");
    requireContains(blockOutline, "RemixRenderer::beginBlockOutlineFrame", "block outline owns frame start");
    requireContains(blockOutline, "RemixRenderer::rebuildBlockOutlineMesh", "block outline owns mesh rebuilding");
    requireContains(fireOverlay, "makeFireMeshHash", "fire overlay owns its mesh hash");
    requireContains(fireOverlay, "RemixRenderer::destroyFireMesh", "fire overlay owns mesh destruction");
    requireContains(fireOverlay, "RemixRenderer::rebuildFireMesh", "fire overlay owns mesh rebuilding");
    requireContains(particleOverlay, "makeParticleMeshHash", "particle overlay owns its mesh hash");
    requireContains(particleOverlay, "RemixRenderer::beginParticleFrame", "particle overlay owns frame start");
    requireContains(particleOverlay, "RemixRenderer::captureParticleQuad", "particle overlay owns capture");
    requireContains(particleOverlay, "RemixRenderer::rebuildParticleMesh", "particle overlay owns mesh rebuilding");
    requireContains(worldSubsystem, "RemixRenderer::updateCloudLayer", "world subsystem owns cloud state");
    requireContains(worldSubsystem, "RemixRenderer::clearWorldScene", "world subsystem owns scene clearing");
    requireContains(worldSubsystem, "RemixRenderer::rebuildCloudMesh", "world subsystem owns cloud mesh rebuilding");
    requireNotContains(meshSubsystem, "RemixRenderer::beginDynamicEntity", "dynamic methods removed from mesh orchestrator");
    requireNotContains(meshSubsystem, "RemixRenderer::createTorchLight", "light methods removed from mesh orchestrator");
    requireNotContains(meshSubsystem, "RemixRenderer::rebuildBlockOutlineMesh", "overlay methods removed from mesh orchestrator");
    requireNotContains(meshSubsystem, "RemixRenderer::updateCloudLayer", "world methods removed from mesh orchestrator");

    String materialCommon = read("src/native/src/materials/remix_materials.cpp");
    String materialPaths = read("src/native/src/materials/remix_material_paths.cpp");
    String terrainMaterials = read("src/native/src/materials/remix_material_terrain.cpp");
    String blockOutlineMaterials = read("src/native/src/particles/remix_material_block_outline.cpp");
    String dynamicMaterials = read("src/native/src/entities/remix_material_dynamic.cpp");
    String particleMaterials = read("src/native/src/particles/remix_material_particles.cpp");
    requireContains(materialCommon, "resolveOptionalPbrTextures", "common material source owns optional PBR discovery");
    requireContains(materialCommon, "applyOptionalOpaqueMaterialTextures", "common material source owns optional texture chaining");
    requireNotContains(materialCommon, "RemixRenderer::", "renderer methods removed from common material source");
    requireContains(materialPaths, "RemixRenderer::resolveTerrainAtlasPath", "material paths own terrain resolution");
    requireContains(materialPaths, "RemixRenderer::resolveSunTexturePath", "material paths own celestial resolution");
    requireContains(materialPaths, "RemixRenderer::resolveDynamicEntityTexturePath", "material paths own dynamic resolution");
    requireContains(materialPaths, "RemixRenderer::resolveParticleTexturePath", "material paths own particle resolution");
    requireContains(terrainMaterials, "RemixRenderer::initializeTerrainMaterials", "terrain source owns initialization");
    requireContains(terrainMaterials, "RemixRenderer::destroyTerrainMaterials", "terrain source owns destruction");
    requireContains(blockOutlineMaterials, "RemixRenderer::createBlockOutlineMaterials", "block-outline source owns creation");
    requireContains(blockOutlineMaterials, "RemixRenderer::destroyBlockOutlineMaterials", "block-outline source owns destruction");
    requireContains(dynamicMaterials, "dynamicEntityMaterialVariantIndex", "dynamic source owns variant policy");
    requireContains(dynamicMaterials, "RemixRenderer::acquireDynamicEntityMaterial", "dynamic source owns material acquisition");
    requireContains(particleMaterials, "RemixRenderer::acquireParticleMaterial", "particle source owns material acquisition");

    String runtimeConfig = read("src/native/src/core/runtime_config.cpp");
    String renderCommon = read("src/native/include/mcrtx/core/remix_render_common.hpp");
    String renderUtils = read("src/native/src/core/remix_render_utils.cpp");
    String lightCommon = read("src/native/src/scene/remix_light_common.cpp");
    String dynamicHeader = read("src/native/include/mcrtx/entities/remix_renderer_dynamic.hpp");
    String chunkPolicy = read("src/native/src/chunks/remix_chunk_policy.cpp");
    String geometryCommon = read("src/native/src/core/remix_geometry_common.cpp");
    String naturalBlockGeometry = read("src/native/src/chunks/remix_block_geometry_natural.cpp");
    String structuralBlockGeometry = read("src/native/src/chunks/remix_block_geometry_structures.cpp");
    String fluidBlockGeometry = read("src/native/src/chunks/remix_block_geometry_fluids.cpp");
    String effectBlockGeometry = read("src/native/src/chunks/remix_block_geometry_effects.cpp");
    String fixtureBlockGeometry = read("src/native/src/chunks/remix_block_geometry_fixtures.cpp");
    String redstoneBlockGeometry = read("src/native/src/chunks/remix_block_geometry_redstone.cpp");
    String pistonBlockGeometry = read("src/native/src/chunks/remix_block_geometry_pistons.cpp");
    String chunkBuildContract = read("src/native/include/mcrtx/chunks/remix_chunk_build.hpp");
    String chunkNativeCapture = read("src/native/src/chunks/remix_chunk_capture.cpp");
    String chunkOccupancy = read("src/native/src/chunks/remix_chunk_occupancy.cpp");
    String chunkGeometry = read("src/native/src/chunks/remix_chunk_geometry.cpp");
    String chunkSubmission = read("src/native/src/chunks/remix_chunk_submission.cpp");
    String chunkResidency = read("src/native/src/chunks/remix_chunk_residency.cpp");
    requireContains(chunkPolicy, "materialClassForBlock", "chunk policy owns material classification");
    requireContains(chunkPolicy, "computeChunkFingerprint", "chunk policy owns occupancy fingerprints");
    requireContains(chunkPolicy, "shouldCullFaceAgainstNeighbor", "chunk policy owns neighbor culling");
    requireContains(geometryCommon, "appendFaceGeometry", "common geometry owns terrain faces");
    requireContains(geometryCommon, "appendDoubleSidedTexturedQuad", "common geometry owns textured quads");
    requireContains(naturalBlockGeometry, "appendCrossedQuadGeometry", "natural geometry owns crossed plants");
    requireContains(naturalBlockGeometry, "appendCropGeometry", "natural geometry owns crops");
    requireContains(naturalBlockGeometry, "appendCactusGeometry", "natural geometry owns cactus");
    requireContains(structuralBlockGeometry, "appendStairGeometry", "structural geometry owns stairs");
    requireContains(structuralBlockGeometry, "appendDoorGeometry", "structural geometry owns doors");
    requireContains(structuralBlockGeometry, "appendLadderGeometry", "structural geometry owns ladders");
    requireContains(fluidBlockGeometry, "appendWaterQuad", "fluid geometry owns liquid quads");
    requireContains(fluidBlockGeometry, "appendWaterGeometry", "fluid geometry owns liquid surfaces");
    requireContains(effectBlockGeometry, "appendAnimatedFireSheet", "effect geometry owns animated fire sheets");
    requireContains(effectBlockGeometry, "appendFireGeometry", "effect geometry owns fire");
    requireContains(effectBlockGeometry, "appendPortalGeometry", "effect geometry owns portals");
    requireContains(fixtureBlockGeometry, "appendLeverGeometry", "fixture geometry owns levers");
    requireContains(fixtureBlockGeometry, "appendTorchGeometry", "fixture geometry owns torches");
    requireContains(redstoneBlockGeometry, "appendRepeaterGeometry", "redstone geometry owns repeaters");
    requireContains(redstoneBlockGeometry, "appendRailGeometry", "redstone geometry owns rails");
    requireContains(redstoneBlockGeometry, "appendRedstoneDustGeometry", "redstone geometry owns dust");
    requireContains(pistonBlockGeometry, "appendPistonRodGeometry", "piston geometry owns rods");
    requireContains(pistonBlockGeometry, "appendPistonHeadGeometry", "piston geometry owns heads");
    requireNotContains(naturalBlockGeometry, "appendFireGeometry", "fire removed from natural geometry");
    requireNotContains(naturalBlockGeometry, "appendWaterGeometry", "liquids removed from natural geometry");
    requireNotContains(naturalBlockGeometry, "appendStairGeometry", "structures removed from natural geometry");
    requireNotContains(fixtureBlockGeometry, "appendRedstoneDustGeometry", "redstone removed from fixture geometry");
    requireNotContains(redstoneBlockGeometry, "appendTorchGeometry", "fixtures removed from redstone geometry");
    requireContains(chunkGeometry, "#include \"mcrtx/chunks/remix_block_geometry_effects.hpp\"", "chunk geometry consumes effect contract");
    requireContains(chunkGeometry, "#include \"mcrtx/chunks/remix_block_geometry_fixtures.hpp\"", "chunk geometry consumes fixture contract");
    requireContains(chunkGeometry, "#include \"mcrtx/chunks/remix_block_geometry_fluids.hpp\"", "chunk geometry consumes fluid contract");
    requireContains(chunkGeometry, "#include \"mcrtx/chunks/remix_block_geometry_natural.hpp\"", "chunk geometry consumes natural contract");
    requireContains(chunkGeometry, "#include \"mcrtx/chunks/remix_block_geometry_pistons.hpp\"", "chunk geometry consumes piston contract");
    requireContains(chunkGeometry, "#include \"mcrtx/chunks/remix_block_geometry_redstone.hpp\"", "chunk geometry consumes redstone contract");
    requireContains(chunkGeometry, "#include \"mcrtx/chunks/remix_block_geometry_structures.hpp\"", "chunk geometry consumes structural contract");
    requireContains(destroyOverlay, "#include \"mcrtx/chunks/remix_block_geometry_natural.hpp\"", "destroy overlay consumes natural geometry contract");
    requireContains(destroyOverlay, "#include \"mcrtx/chunks/remix_block_geometry_structures.hpp\"", "destroy overlay consumes structural geometry contract");
    requireContains(destroyOverlay, "#include \"mcrtx/chunks/remix_block_geometry_fixtures.hpp\"", "destroy overlay consumes fixture geometry contract");
    requireContains(destroyOverlay, "#include \"mcrtx/chunks/remix_block_geometry_redstone.hpp\"", "destroy overlay consumes redstone geometry contract");
    requireContains(fireOverlay, "#include \"mcrtx/chunks/remix_block_geometry_effects.hpp\"", "fire overlay consumes effect geometry contract");
    requireContains(chunkBuildContract, "struct ChunkGeometryBuild", "chunk build contract owns transient output");
    requireContains(chunkBuildContract, "surfacesToBuild", "chunk build contract owns transient surfaces");
    requireContains(chunkBuildContract, "desiredTorchLights", "chunk build contract owns desired lights");
    requireNotContains(chunkBuildContract, "occupancy", "persistent occupancy excluded from transient build");
    requireNotContains(chunkBuildContract, "meshHandle", "persistent mesh handle excluded from transient build");
    requireNotContains(chunkBuildContract, "geometryFingerprint", "persistent fingerprints excluded from transient build");
    requireContains(chunkNativeCapture, "RemixRenderer::beginChunkBuild", "chunk capture owns build begin");
    requireContains(chunkNativeCapture, "RemixRenderer::endChunkBuild", "chunk capture owns build end");
    requireContains(chunkNativeCapture, "rebuildChunkMesh(activeChunkBuild_", "chunk capture keeps occupancy entrypoint");
    requireContains(chunkOccupancy, "RemixRenderer::rebuildChunkMesh", "chunk occupancy owns captured-block ingestion");
    requireContains(chunkOccupancy, "rebuildChunkMesh.mergeDirtyRegion", "chunk occupancy owns partial merging");
    requireContains(chunkOccupancy, "rebuildChunkMesh.scanOccupancy", "chunk occupancy owns occupancy scanning");
    requireNotContains(chunkOccupancy, "CreateMesh", "mesh creation excluded from chunk occupancy");
    requireNotContains(chunkOccupancy, "appendWaterGeometry", "geometry dispatch excluded from chunk occupancy");
    requireContains(chunkGeometry, "RemixRenderer::emitChunkGeometry", "chunk geometry owns dispatch");
    requireContains(chunkGeometry, "hasFenceNeighbor", "chunk geometry owns fence neighbor lookup");
    requireContains(chunkGeometry, "findWorldCell", "chunk geometry owns cross-chunk cell lookup");
    requireContains(chunkGeometry, "rebuildChunkMeshFromData.emitGeometry", "chunk geometry preserves dispatch scope");
    requireContains(chunkGeometry, "appendWaterGeometry", "chunk geometry dispatches specialized families");
    requireNotContains(chunkGeometry, "CreateMesh", "mesh creation excluded from chunk geometry");
    requireContains(chunkSubmission, "RemixRenderer::rebuildChunkMeshFromData", "chunk submission owns rebuild-from-data entrypoint");
    requireContains(chunkSubmission, "emitChunkGeometry(chunkKey, meshData, build)", "chunk submission invokes geometry once");
    requireContains(chunkSubmission, "rebuildChunkMeshFromData.finalizeSurfaces", "chunk submission owns finalization");
    requireContains(chunkSubmission, "CreateMesh.chunk", "chunk submission owns Remix mesh creation");
    requireContains(chunkSubmission, "rebuildChunkMeshFromData.reconcileTorchLights", "chunk submission owns light reconciliation order");
    requireContains(chunkSubmission, "destroyChunkMeshHandle(meshData)", "chunk submission owns mesh commit");
    requireNotContains(chunkSubmission, "remix_block_geometry_effects.hpp", "family dispatch dependencies excluded from submission");
    requireContains(chunkResidency, "RemixRenderer::unloadChunkSection", "chunk residency owns unload");
    requireContains(chunkResidency, "rebuildChunkMeshFromData(neighborKey", "chunk residency keeps rebuild-from-data entrypoint");
    requireContains(chunkResidency, "RemixRenderer::flushChunkNeighborRefreshes", "chunk residency owns refresh budgets");
    requireContains(runtimeConfig, "loadRuntimeConfigValues", "runtime source owns config loading");
    requireContains(runtimeConfig, "readEnvironmentVariable", "runtime source owns environment access");
    requireContains(runtimeConfig, "getCurrentModuleDirectory", "runtime source owns module discovery");
    requireContains(renderUtils, "errorCodeToString", "render utilities own error conversion");
    requireContains(renderUtils, "makeTranslationTransform", "render utilities own translation transforms");
    requireContains(renderUtils, "mixHashComponent", "render utilities own generic hash mixing");
    requireContains(renderUtils, "computeQuadNormal", "render utilities own quad normals");
    requireContains(lightCommon, "makeTorchLightHash", "light common owns torch hashes");
    requireContains(lightCommon, "findTorchLightPlacement", "light common owns placement lookup");
    requireContains(lightCommon, "makeTorchLightPlacement", "light common owns placement construction");
    requireContains(dynamicSubsystem, "makeDynamicEntityMeshKey", "dynamic subsystem owns mesh keys");
    requireContains(dynamicSubsystem, "beginDynamicEntityFingerprint", "dynamic subsystem owns fingerprints");
    requireContains(dynamicSubsystem, "hashDynamicEntityQuad", "dynamic subsystem owns quad hashing");
    requireContains(dynamicHeader, "kDynamicEntityMaterialHashSeed", "dynamic contract owns material hash seed");
    requireContains(dynamicHeader, "kFirstPersonDynamicEntityId", "dynamic contract owns first-person entity ID");
    requireContains(worldSubsystem, "kFastCloudTileSize", "world subsystem owns cloud dimensions");
    requireContains(worldSubsystem, "appendFastCloudGeometry", "world subsystem owns fast cloud geometry");
    requireContains(worldSubsystem, "appendFancyCloudGeometry", "world subsystem owns fancy cloud geometry");
    requireContains(rendererScene, "kRtTextureArgTexture", "scene owner owns fixed-function texture constants");
    requireContains(destroyOverlay, "kDestroyOverlayMeshHashSeed", "destroy overlay owns its hash seed");
    requireContains(blockOutline, "kBlockOutlineMeshHashSeed", "block outline owns its hash seed");
    requireContains(fireOverlay, "kFireMeshHashSeed", "fire overlay owns its hash seed");
    requireContains(particleOverlay, "kParticleMeshHashSeed", "particle overlay owns its hash seed");
    requireNotContains(renderCommon, "readEnvironmentVariable", "runtime access excluded from render common");
    requireNotContains(renderCommon, "makeTorchLightHash", "light helpers excluded from render common");
    requireNotContains(renderCommon, "beginDynamicEntityFingerprint", "dynamic helpers excluded from render common");
    requireNotContains(renderCommon, "appendFancyCloudGeometry", "cloud builders excluded from render common");
    requireNotContains(renderCommon, "kTorchLightHashSeed", "light constants excluded from render common");
    requireNotContains(renderCommon, "kFastCloudTileSize", "cloud constants excluded from render common");
    requireNotContains(renderCommon, "kRtTextureArgTexture", "scene constants excluded from render common");
    requireNotContains(renderCommon, "kDestroyOverlayMeshHashSeed", "overlay constants excluded from render common");

    String rendererLifecycle = read("src/native/src/lifecycle/remix_renderer_lifecycle.cpp");
    String rendererUi = read("src/native/src/ui/remix_renderer_ui.cpp");
    String rendererResources = read("src/native/src/core/remix_renderer_resources.cpp");
    requireContains(rendererLifecycle, "RemixRenderer::initialize", "renderer lifecycle owns initialization");
    requireContains(rendererLifecycle, "RemixRenderer::standaloneRenderWorkerMain", "renderer lifecycle owns standalone worker");
    requireContains(rendererLifecycle, "RemixRenderer::shutdownLocked", "renderer lifecycle owns locked shutdown");
    requireContains(rendererLifecycle, "RemixRenderer::loadRemix", "renderer lifecycle owns runtime loading");
    requireContains(rendererUi, "RemixRenderer::drawScreenOverlay", "renderer UI owns overlays");
    requireContains(rendererUi, "RemixRenderer::registerUiTexture", "renderer UI owns texture registration");
    requireContains(rendererUi, "RemixRenderer::submitUiDrawListFromArrays", "renderer UI owns draw-list marshalling");
    requireContains(rendererUi, "RemixRenderer::setUiState", "renderer UI owns state synchronization");
    requireContains(rendererResources, "ChunkKeyHash::operator()", "renderer resources own chunk hash support");
    requireContains(rendererResources, "RemixRenderer::destroyMeshHandle", "renderer resources own mesh destruction");
    requireContains(rendererResources, "RemixRenderer::flushDeferredDestroyQueuesLocked", "renderer resources own deferred cleanup");
    requireContains(rendererResources, "RemixRenderer::createPrimingMesh", "renderer resources own priming mesh");
    requireContains(rendererFrame, "RemixRenderer::resetPerFramePerfCounters", "renderer frame owner resets counters");
    requireContains(rendererFrame, "RemixRenderer::resize", "renderer frame owner owns resize state");
    requireContains(rendererFrame, "RemixRenderer::updateCamera", "renderer frame owner owns camera state");
    requireContains(rendererFrame, "RemixRenderer::currentRenderOriginLocked", "renderer frame owner selects render origin");
    requireContains(rendererFrame, "RemixRenderer::prepareFrameSnapshotLocked", "renderer frame owner prepares snapshots");
    requireContains(rendererPresent, "formatMilliseconds", "renderer presentation owner formats performance summaries");
    requireContains(rendererPresent, "RemixRenderer::present()", "renderer presentation owner owns public presentation");
    requireContains(rendererPresent, "RemixRenderer::requestPresentedScreenshot", "renderer presentation owner owns screenshots");
    requireContains(rendererPresent, "RemixRenderer::presentLocked", "renderer presentation owner owns frame ordering");
    requireContains(rendererScene, "RemixRenderer::updateFogState", "renderer scene owner submits fog state");
    requireContains(rendererScene, "RemixRenderer::drawCapturedGeometry", "renderer scene owner submits captured geometry");
    requireContains(rendererScene, "RemixRenderer::submitCamera", "renderer scene owner submits cameras");
    requireNotContains(rendererFrame, "RemixRenderer::presentLocked", "presentation removed from frame owner");
    requireNotContains(rendererPresent, "RemixRenderer::drawCapturedGeometry", "scene draws removed from presentation owner");
    requireNotContains(rendererScene, "RemixRenderer::prepareFrameSnapshotLocked", "snapshot preparation removed from scene owner");
    requireNotContains(rendererCore, "RemixRenderer::initialize", "lifecycle methods removed from renderer core");
    requireNotContains(rendererCore, "RemixRenderer::drawScreenOverlay", "UI methods removed from renderer core");
    requireNotContains(rendererCore, "RemixRenderer::destroyMeshHandle", "resource methods removed from renderer core");
    requireNotContains(rendererCore, "RemixRenderer::createPrimingMesh", "priming mesh removed from renderer core");

    String windowInternals = read("src/native/src/platform/remix_window_internals.cpp");
    String windowOutput = read("src/native/src/platform/remix_window_output.cpp");
    String windowInput = read("src/native/src/platform/remix_window_input.cpp");
    String lifecycleJni = read("src/native/src/lifecycle/jni_lifecycle.cpp");
    requireContains(windowInternals, "shouldUseOverlayOutputWindow", "window infrastructure owns mode policy");
    requireContains(windowInternals, "remixOutputWindowProc", "window infrastructure owns output procedure");
    requireContains(windowInternals, "ensureRawMouseInputWindow", "window infrastructure owns raw-input window");
    requireContains(windowInternals, "ensureOutputWindowClassRegistered", "window infrastructure owns class registration");
    requireContains(windowInternals, "g_outputWindowCloseRequested", "window infrastructure owns JNI-visible state");
    requireContains(windowOutput, "RemixRenderer::createOutputWindow", "output owner creates the window");
    requireContains(windowOutput, "RemixRenderer::setOutputWindowFullscreen", "output owner owns fullscreen");
    requireContains(windowOutput, "RemixRenderer::syncOutputWindowInteractivity", "output owner owns UI interactivity");
    requireContains(windowInput, "RemixRenderer::pollNativeMouseState", "input owner polls native mouse");
    requireContains(windowInput, "RemixRenderer::applyNativeMouseGrabLocked", "input owner applies mouse grab");
    requireContains(windowInput, "RemixRenderer::setNativeCursorPosition", "input owner positions cursor");
    requireContains(windowInput, "window_detail::syncNativeCursorVisibility", "input owner implements shared cursor visibility");
    requireContains(rendererLifecycle, "#include \"mcrtx/platform/remix_window_internals.hpp\"", "renderer lifecycle consumes window contract");
    requireContains(lifecycleJni, "mcrtx::window_detail::g_outputWindowCloseRequested", "lifecycle JNI consumes window state");
  }

  private static String read(String path) throws Exception {
    return new String(Files.readAllBytes(Paths.get(path)), StandardCharsets.UTF_8);
  }

  private static void requireFile(String path) {
    Path file = Paths.get(path);
    if (!Files.isRegularFile(file)) {
      throw new AssertionError("expected file missing: " + path);
    }
  }

  private static void requireMissingFile(String path) {
    if (Files.exists(Paths.get(path))) {
      throw new AssertionError("superseded file still exists: " + path);
    }
  }

  private static void requireNoDirectFilesWithSuffix(String directory, String suffix) throws Exception {
    Path root = Paths.get(directory);
    if (!Files.isDirectory(root)) {
      throw new AssertionError("expected source root missing: " + directory);
    }

    try (DirectoryStream<Path> entries = Files.newDirectoryStream(root)) {
      for (Path entry : entries) {
        if (Files.isRegularFile(entry) && entry.getFileName().toString().endsWith(suffix)) {
          throw new AssertionError("source file remains in flat root: " + entry);
        }
      }
    }
  }

  private static void requireNoFilesWithSuffixRecursively(String directory, String suffix) throws Exception {
    Path root = Paths.get(directory);
    if (!Files.exists(root)) {
      return;
    }
    requireNoFilesWithSuffixRecursively(root, suffix);
  }

  private static void requireNoFilesWithSuffixRecursively(Path directory, String suffix) throws Exception {
    try (DirectoryStream<Path> entries = Files.newDirectoryStream(directory)) {
      for (Path entry : entries) {
        if (Files.isDirectory(entry)) {
          requireNoFilesWithSuffixRecursively(entry, suffix);
        } else if (Files.isRegularFile(entry) && entry.getFileName().toString().endsWith(suffix)) {
          throw new AssertionError("source file remains in superseded root: " + entry);
        }
      }
    }
  }

  private static void requireContains(String haystack, String needle, String message) {
    if (haystack.indexOf(needle) < 0) {
      throw new AssertionError(message + " missing: " + needle);
    }
  }

  private static void requireNotContains(String haystack, String needle, String message) {
    if (haystack.indexOf(needle) >= 0) {
      throw new AssertionError(message + " still contains: " + needle);
    }
  }
}
