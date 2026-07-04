import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import mcrtx.bridge.McrtxRuntimeSettings;

public final class McrtxSparseRenderingOptionTest {
  public static void main(String[] args) throws Exception {
    Path tempDir = Files.createTempDirectory("mcrtx-sparse-rendering");
    System.setProperty("user.dir", tempDir.toString());

    require(McrtxRuntimeSettings.isSparseRenderingEnabled(), "sparse rendering should default on");

    McrtxRuntimeSettings.setSparseRenderingEnabled(false);
    require(!McrtxRuntimeSettings.isSparseRenderingEnabled(), "setter should disable sparse rendering");

    String saved = new String(Files.readAllBytes(tempDir.resolve("mcrtx-runtime.env")), StandardCharsets.US_ASCII);
    require(
        saved.indexOf("MCRTX_SPARSE_RENDERING_ENABLED=0") >= 0,
        "saved config should include disabled sparse rendering");

    String runtimeSettings = read("java-src/mcrtx/bridge/McrtxRuntimeSettings.java");
    requireContains(runtimeSettings, "SPARSE_RENDERING_ENABLED_KEY", "sparse config key");
    requireContains(runtimeSettings, "DEFAULT_SPARSE_RENDERING_ENABLED", "sparse default");
    requireContains(runtimeSettings, "isSparseRenderingEnabled()", "sparse getter");
    requireContains(runtimeSettings, "setSparseRenderingEnabled(boolean enabled)", "sparse setter");

    String hooks = read("java-src/MinecraftRemixHooks.java");
    requireContains(hooks, "isSparseRenderingEnabled()", "hook getter");
    requireContains(hooks, "toggleSparseRenderingEnabled()", "hook toggle");
    requireContains(hooks, "shouldShowSparseRenderingOption()", "hook visibility predicate");
    requireContains(hooks, "getSparseRenderingButtonLabel()", "hook label");

    String settingsUi = read("java-src/McrtxHookSettingsUi.java");
    requireContains(settingsUi, "Sparse Rendering: ", "settings label");
    requireContains(settingsUi, "shouldShowSparseRenderingOption()", "settings visibility predicate");
    requireContains(settingsUi, "toggleSparseRenderingEnabled()", "settings toggle");
    requireContains(settingsUi, "McrtxRuntimeSettings.isSparseRenderingEnabled()", "settings reads sparse preference");
    requireContains(settingsUi, "MinecraftRenderHooks.setUpscalerConfig(", "settings applies upscaler bridge");

    String quickSettings = read("java-src/McrtxQuickSettingsScreen.java");
    requireContains(quickSettings, "SPARSE_RENDERING_BUTTON_ID", "button id");
    requireContains(quickSettings, "MinecraftRemixHooks.shouldShowSparseRenderingOption()", "DLSS-only add path");
    requireContains(quickSettings, "MinecraftRemixHooks.toggleSparseRenderingEnabled()", "button toggles setting");
    requireContains(quickSettings, "MinecraftRemixHooks.getSparseRenderingButtonLabel()", "button label");

    String renderHooks = read("java-src/mcrtx/bridge/MinecraftRenderHooks.java");
    requireContains(renderHooks, "boolean sparseRenderingEnabled", "render hook sparse parameter");
    requireContains(renderHooks, "RemixBridgeNative.nSetUpscalerConfig", "render hook native call");

    String nativeBridge = read("java-src/mcrtx/bridge/RemixBridgeNative.java");
    requireContains(nativeBridge, "boolean sparseRenderingEnabled", "native declaration sparse parameter");

    String rendererHeader = read("native/include/mcrtx/remix_renderer.hpp");
    requireContains(rendererHeader, "bool sparseRenderingEnabled", "renderer sparse parameter");
    requireContains(rendererHeader, "bool sparseRenderingEnabled_ {true}", "renderer sparse default");

    String rendererImpl = read("native/src/remix_renderer.cpp");
    requireContains(rendererImpl, "rtx.sparseRendering.enableSparseRendering", "native Remix config key");
    requireContains(rendererImpl, "sparseRenderingEnabled_ ? \"True\" : \"False\"", "DLSS applies saved sparse setting");
    requireContains(rendererImpl, "\"rtx.sparseRendering.enableSparseRendering\", \"False\"", "non-DLSS forces sparse off");
  }

  private static String read(String path) throws Exception {
    return new String(Files.readAllBytes(Paths.get(path)), StandardCharsets.UTF_8);
  }

  private static void requireContains(String haystack, String needle, String message) {
    if (haystack.indexOf(needle) < 0) {
      throw new AssertionError(message + " missing: " + needle);
    }
  }

  private static void require(boolean condition, String message) {
    if (!condition) {
      throw new AssertionError(message);
    }
  }
}
