import mcrtx.bridge.McrtxGraphicsSettings;

public final class McrtxCloudModeTest {
  public static void main(String[] args) {
    require(!McrtxGraphicsSettings.DEFAULT_REMIX_ATMOSPHERE_CLOUDS_ENABLED, "default should keep game clouds");
    require(
        "MCRTX_REMIX_ATMOSPHERE_CLOUDS_ENABLED".equals(McrtxGraphicsSettings.REMIX_ATMOSPHERE_CLOUDS_ENABLED_KEY),
        "setting key");
    require(McrtxGraphicsSettings.shouldSubmitGameCloudLayer(true, false), "initialized game-cloud mode submits");
    require(!McrtxGraphicsSettings.shouldSubmitGameCloudLayer(true, true), "remix cloud mode skips game clouds");
    require(!McrtxGraphicsSettings.shouldSubmitGameCloudLayer(false, false), "uninitialized renderer skips");
    require(McrtxGraphicsSettings.shouldClearGameCloudLayerAfterToggle(false, true), "off to on clears stale mesh");
    require(!McrtxGraphicsSettings.shouldClearGameCloudLayerAfterToggle(true, true), "staying on does not clear again");
    require(!McrtxGraphicsSettings.shouldClearGameCloudLayerAfterToggle(true, false), "on to off waits for normal submit");
    require("Volumetric Clouds: OFF".equals(McrtxGraphicsSettings.formatCloudButtonLabel(false)), "off label");
    require("Volumetric Clouds: ON".equals(McrtxGraphicsSettings.formatCloudButtonLabel(true)), "on label");
  }

  private static void require(boolean condition, String message) {
    if (!condition) {
      throw new AssertionError(message);
    }
  }
}
