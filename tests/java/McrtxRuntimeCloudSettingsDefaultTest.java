import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import mcrtx.bridge.McrtxRuntimeSettings;

public final class McrtxRuntimeCloudSettingsDefaultTest {
  public static void main(String[] args) throws Exception {
    Path tempDir = Files.createTempDirectory("mcrtx-cloud-default");
    System.setProperty("user.dir", tempDir.toString());

    require(!McrtxRuntimeSettings.isRemixAtmosphereCloudsEnabled(), "default should be false");

    McrtxRuntimeSettings.setRemixAtmosphereCloudsEnabled(true);
    require(McrtxRuntimeSettings.isRemixAtmosphereCloudsEnabled(), "setter should enable mode");

    String saved = new String(Files.readAllBytes(tempDir.resolve("mcrtx-runtime.env")), StandardCharsets.US_ASCII);
    require(
        saved.indexOf("MCRTX_REMIX_ATMOSPHERE_CLOUDS_ENABLED=1") >= 0,
        "saved config should include enabled cloud mode");
  }

  private static void require(boolean condition, String message) {
    if (!condition) {
      throw new AssertionError(message);
    }
  }
}
