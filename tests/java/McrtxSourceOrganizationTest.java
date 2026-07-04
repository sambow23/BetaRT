import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public final class McrtxSourceOrganizationTest {
  public static void main(String[] args) throws Exception {
    String hooks = read("java-src/MinecraftRemixHooks.java");
    requireContains(hooks, "public final class MinecraftRemixHooks", "hook facade class");
    requireContains(hooks, "public static void onDisplayCreated", "display-created hook ABI");
    requireContains(hooks, "public static String onScreenshot", "screenshot hook ABI");
    requireContains(hooks, "public static void onRemixUiTick", "remix UI tick hook ABI");
    requireContains(hooks, "public static void onFrameRenderStart", "frame-start hook ABI");
    requireContains(hooks, "McrtxHookScreenshotHelper.requestPresentedScreenshot", "screenshot helper delegation");
    requireContains(hooks, "McrtxHookPerfTracker.", "perf helper delegation");
    requireContains(hooks, "McrtxHookSettingsUi.", "settings UI helper delegation");

    requireFile("java-src/McrtxHookScreenshotHelper.java");
    requireFile("java-src/McrtxHookPerfTracker.java");
    requireFile("java-src/McrtxHookSettingsUi.java");

    String settings = read("java-src/mcrtx/bridge/McrtxRuntimeSettings.java");
    requireContains(settings, "public final class McrtxRuntimeSettings", "runtime settings facade");
    requireContains(settings, "McrtxRuntimeSettingParser.", "runtime parser helper delegation");
    requireContains(settings, "McrtxRuntimeSettingFormatter.", "runtime formatter helper delegation");
    requireFile("java-src/mcrtx/bridge/McrtxRuntimeSettingParser.java");
    requireFile("java-src/mcrtx/bridge/McrtxRuntimeSettingFormatter.java");

    String dynamicCapture = read("java-src/RemixDynamicEntityCapture.java");
    requireContains(dynamicCapture, "public final class RemixDynamicEntityCapture", "dynamic capture facade");
    requireContains(dynamicCapture, "public static void onLivingEntityRenderStart", "living entity hook ABI");
    requireContains(dynamicCapture, "public static boolean onModelPartRender", "model-part hook ABI");

    requireFile("native/include/mcrtx/remix_renderer_frame.hpp");
    requireFile("native/include/mcrtx/remix_renderer_scene.hpp");
    requireFile("native/include/mcrtx/remix_renderer_dynamic.hpp");
    requireFile("native/include/mcrtx/remix_renderer_overlay.hpp");

    String rendererHeader = read("native/include/mcrtx/remix_renderer.hpp");
    requireContains(rendererHeader, "#include \"mcrtx/remix_renderer_frame.hpp\"", "frame header include");
    requireContains(rendererHeader, "#include \"mcrtx/remix_renderer_scene.hpp\"", "scene header include");
    requireContains(rendererHeader, "#include \"mcrtx/remix_renderer_dynamic.hpp\"", "dynamic header include");
    requireContains(rendererHeader, "#include \"mcrtx/remix_renderer_overlay.hpp\"", "overlay header include");
    requireContains(rendererHeader, "class RemixRenderer", "renderer facade class");

    String cmake = read("native/CMakeLists.txt");
    requireContains(cmake, "src/remix_subsystem_config.cpp", "native config source in build");
    requireContains(cmake, "src/remix_subsystem_dynamic.cpp", "native dynamic source in build");
    requireContains(cmake, "src/remix_subsystem_lights.cpp", "native lights source in build");
    requireContains(cmake, "src/remix_subsystem_overlays.cpp", "native overlays source in build");
    requireContains(cmake, "src/remix_subsystem_world.cpp", "native world source in build");
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

  private static void requireContains(String haystack, String needle, String message) {
    if (haystack.indexOf(needle) < 0) {
      throw new AssertionError(message + " missing: " + needle);
    }
  }
}
