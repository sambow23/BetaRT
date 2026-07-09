import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import mcrtx.bridge.McrtxGraphicsSettings;

public final class McrtxSparseRenderingOptionTest {
  public static void main(String[] args) throws Exception {
    Path tempDir = Files.createTempDirectory("mcrtx-sparse-rendering");
    System.setProperty("user.dir", tempDir.toString());

    require(McrtxGraphicsSettings.isSparseRenderingEnabled(), "sparse rendering should default on");

    McrtxGraphicsSettings.setSparseRenderingEnabled(false);
    require(!McrtxGraphicsSettings.isSparseRenderingEnabled(), "setter should disable sparse rendering");

    String saved = new String(Files.readAllBytes(tempDir.resolve("mcrtx-runtime.env")), StandardCharsets.US_ASCII);
    require(
        saved.indexOf("MCRTX_SPARSE_RENDERING_ENABLED=0") >= 0,
        "saved config should include disabled sparse rendering");

    String graphicsSettings = read("java-src/mcrtx/bridge/McrtxGraphicsSettings.java");
    requireContains(graphicsSettings, "SPARSE_RENDERING_ENABLED_KEY", "sparse config key");
    requireContains(graphicsSettings, "DEFAULT_SPARSE_RENDERING_ENABLED", "sparse default");
    requireContains(graphicsSettings, "isSparseRenderingEnabled()", "sparse getter");
    requireContains(graphicsSettings, "setSparseRenderingEnabled(boolean enabled)", "sparse setter");

    String settingsUi = read("java-src/McrtxGraphicsSettingsUi.java");
    requireContains(settingsUi, "Sparse Rendering: ", "settings label");
    requireContains(settingsUi, "shouldShowDlssOptions()", "settings visibility predicate");
    requireContains(settingsUi, "toggleSparseRendering()", "settings toggle");
    requireContains(settingsUi, "McrtxGraphicsSettings.isSparseRenderingEnabled()", "settings reads sparse preference");
    requireContains(settingsUi, "McrtxGraphicsSettingsNative.setUpscalerConfig(", "settings applies upscaler bridge");

    String nativeBridge = read("java-src/mcrtx/bridge/McrtxGraphicsSettingsNative.java");
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
