import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

public final class McrtxCloudModeWiringTest {
  public static void main(String[] args) throws Exception {
    String remixCloudCapture = read("java-src/RemixCloudCapture.java");
    String hookSettingsUi = read("java-src/McrtxHookSettingsUi.java");
    requireContains(remixCloudCapture, "McrtxRuntimeSettings.isRemixAtmosphereCloudsEnabled()", "cloud capture reads setting");
    requireContains(remixCloudCapture, "McrtxCloudMode.shouldSubmitGameCloudLayer", "cloud capture uses mode predicate");
    requireContains(remixCloudCapture, "gameCloudLayerClearedForRemixClouds", "cloud capture remembers clear state");
    requireContains(remixCloudCapture, "MinecraftRenderHooks.clearCloudLayer()", "cloud capture clears stale mesh");

    String minecraftRemixHooks = read("java-src/MinecraftRemixHooks.java");
    requireContains(minecraftRemixHooks, "isRemixAtmosphereCloudsEnabled()", "hook getter");
    requireContains(minecraftRemixHooks, "setRemixAtmosphereCloudsEnabled(boolean enabled)", "hook setter");
    requireContains(minecraftRemixHooks, "getRemixAtmosphereCloudsButtonLabel()", "hook label");
    requireContains(minecraftRemixHooks, "McrtxHookSettingsUi.setRemixAtmosphereCloudsEnabled(enabled)", "hook delegates cloud setter");
    requireContains(hookSettingsUi, "MinecraftRenderHooks.setRemixAtmosphereCloudsEnabled(enabled)", "settings helper forwards native state");

    String quickSettings = read("java-src/McrtxQuickSettingsScreen.java");
    requireContains(quickSettings, "REMIX_ATMOSPHERE_CLOUDS_BUTTON_ID", "button id");
    requireContains(quickSettings, "MinecraftRemixHooks.setRemixAtmosphereCloudsEnabled", "button toggles setting");
    requireContains(quickSettings, "MinecraftRemixHooks.getRemixAtmosphereCloudsButtonLabel()", "button label");

    String renderHooks = read("java-src/mcrtx/bridge/MinecraftRenderHooks.java");
    requireContains(renderHooks, "setRemixAtmosphereCloudsEnabled(boolean enabled)", "render hook wrapper");
    requireContains(renderHooks, "RemixBridgeNative.nSetRemixAtmosphereCloudsEnabled(enabled)", "render hook calls native");

    String nativeBridge = read("java-src/mcrtx/bridge/RemixBridgeNative.java");
    requireContains(nativeBridge, "nSetRemixAtmosphereCloudsEnabled(boolean enabled)", "native declaration");
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
