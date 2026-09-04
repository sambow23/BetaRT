import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import mcrtx.bridge.McrtxGraphicsSettings;

public final class McrtxDlssFeatureSettingsTest {
  public static void main(String[] args) throws Exception {
    Path tempDir = Files.createTempDirectory("mcrtx-dlss-features");
    System.setProperty("user.dir", tempDir.toString());

    require(!McrtxGraphicsSettings.isDlssFrameGenerationEnabled(), "frame generation should default off");
    require(McrtxGraphicsSettings.getDlssFrameGenerationMultiplier() == 2, "frame generation should default to 2x");

    McrtxGraphicsSettings.setDlssFrameGenerationEnabled(true);
    McrtxGraphicsSettings.setDlssFrameGenerationMultiplier(99);
    require(McrtxGraphicsSettings.isDlssFrameGenerationEnabled(), "frame generation setter should enable it");
    require(McrtxGraphicsSettings.getDlssFrameGenerationMultiplier() == 6, "frame generation multiplier should clamp to 6x");

    String saved = new String(
        Files.readAllBytes(tempDir.resolve("mcrtx-runtime.env")), StandardCharsets.US_ASCII);
    requireContains(saved, "MCRTX_DLSS_FRAME_GENERATION_ENABLED=1", "saved frame generation state");
    requireContains(saved, "MCRTX_DLSS_FRAME_GENERATION_MULTIPLIER=6", "saved frame generation multiplier");

    String nativeBridge = read("src/java-src/settings/mcrtx/bridge/McrtxGraphicsSettingsNative.java");
    requireContains(nativeBridge, "FEATURE_DLSS_FRAME_GENERATION", "Java frame generation capability");
    requireContains(nativeBridge, "nSetFrameGenerationConfig", "Java frame generation JNI bridge");
    requireContains(nativeBridge, "nGetAvailableFeatureMask", "Java feature availability JNI bridge");

    String settingsUi = read("src/java-src/settings/McrtxGraphicsSettingsUi.java");
    requireContains(settingsUi, "DLSS Frame Generation: Unavailable", "capability-aware frame generation label");
    requireContains(settingsUi, "applyFrameGeneration();", "frame generation apply path");

    String configSubsystem = read("src/native/src/scene/remix_subsystem_config.cpp");
    requireContains(configSubsystem, "REMIXAPI_FEATURE_DLSS_SUPER_RESOLUTION_BIT", "DLSS availability fallback");
    requireContains(configSubsystem, "REMIXAPI_FEATURE_DLSS_RAY_RECONSTRUCTION_BIT", "ray reconstruction availability fallback");
    requireContains(configSubsystem, "REMIXAPI_FEATURE_DLSS_FRAME_GENERATION_BIT", "frame generation availability fallback");
    requireContains(configSubsystem, "rtx.dlfg.maxInterpolatedFrames", "frame generation multiplier config");
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
