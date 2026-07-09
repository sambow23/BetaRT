import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

public final class McrtxCloudModeWiringTest {
  public static void main(String[] args) throws Exception {
    String remixCloudCapture = read("java-src/RemixCloudCapture.java");
    requireContains(remixCloudCapture, "McrtxGraphicsSettings.isRemixAtmosphereCloudsEnabled()", "cloud capture reads setting");
    requireContains(remixCloudCapture, "McrtxGraphicsSettings.shouldSubmitGameCloudLayer", "cloud capture uses mode predicate");
    requireContains(remixCloudCapture, "gameCloudLayerClearedForRemixClouds", "cloud capture remembers clear state");
    requireContains(remixCloudCapture, "MinecraftRenderHooks.clearCloudLayer()", "cloud capture clears stale mesh");

    String graphicsUi = read("java-src/McrtxGraphicsSettingsUi.java");
    requireContains(graphicsUi, "REMIX_ATMOSPHERE_CLOUDS_BUTTON_ID", "button id");
    requireContains(graphicsUi, "McrtxGraphicsSettings.setRemixAtmosphereCloudsEnabled", "button toggles setting");
    requireContains(graphicsUi, "McrtxGraphicsSettings.formatCloudButtonLabel", "button label");
    requireContains(graphicsUi, "McrtxGraphicsSettingsNative.setRemixAtmosphereCloudsEnabled", "settings forwards native state");

    String nativeEntry = read("java-src/mcrtx/bridge/McrtxGraphicsSettingsNative.java");
    requireContains(nativeEntry, "nSetRemixAtmosphereCloudsEnabled(boolean enabled)", "native declaration");

    String nativeJni = read("native/src/jni_settings_graphics.cpp");
    requireContains(nativeJni, "Java_mcrtx_bridge_McrtxGraphicsSettingsNative_nSetRemixAtmosphereCloudsEnabled", "category JNI symbol");
  }

  private static String read(String path) throws Exception {
    return new String(Files.readAllBytes(Paths.get(path)), StandardCharsets.UTF_8);
  }

  private static void requireContains(String haystack, String needle, String message) {
    if (haystack.indexOf(needle) < 0) {
      throw new AssertionError(message + " missing: " + needle);
    }
  }
}
