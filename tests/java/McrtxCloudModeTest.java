import mcrtx.bridge.McrtxCloudMode;

public final class McrtxCloudModeTest {
  public static void main(String[] args) {
    require(!McrtxCloudMode.DEFAULT_REMIX_ATMOSPHERE_CLOUDS_ENABLED, "default should keep game clouds");
    require(
        "MCRTX_REMIX_ATMOSPHERE_CLOUDS_ENABLED".equals(McrtxCloudMode.REMIX_ATMOSPHERE_CLOUDS_ENABLED_KEY),
        "setting key");
    require(McrtxCloudMode.shouldSubmitGameCloudLayer(true, false), "initialized game-cloud mode submits");
    require(!McrtxCloudMode.shouldSubmitGameCloudLayer(true, true), "remix cloud mode skips game clouds");
    require(!McrtxCloudMode.shouldSubmitGameCloudLayer(false, false), "uninitialized renderer skips");
    require(McrtxCloudMode.shouldClearGameCloudLayerAfterToggle(false, true), "off to on clears stale mesh");
    require(!McrtxCloudMode.shouldClearGameCloudLayerAfterToggle(true, true), "staying on does not clear again");
    require(!McrtxCloudMode.shouldClearGameCloudLayerAfterToggle(true, false), "on to off waits for normal submit");
    require("Remix Clouds: OFF".equals(McrtxCloudMode.formatButtonLabel(false)), "off label");
    require("Remix Clouds: ON".equals(McrtxCloudMode.formatButtonLabel(true)), "on label");
  }

  private static void require(boolean condition, String message) {
    if (!condition) {
      throw new AssertionError(message);
    }
  }
}
