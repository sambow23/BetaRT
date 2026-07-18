import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import mcrtx.bridge.McrtxGraphicsSettings;

public final class McrtxRuntimeCloudSettingsConfigTest {
  public static void main(String[] args) throws Exception {
    Path tempDir = Files.createTempDirectory("mcrtx-cloud-config");
    Files.write(
        tempDir.resolve("mcrtx-runtime.env"),
        (McrtxGraphicsSettings.REMIX_ATMOSPHERE_CLOUDS_ENABLED_KEY + "=yes\n").getBytes(StandardCharsets.US_ASCII));
    System.setProperty("user.dir", tempDir.toString());

    require(McrtxGraphicsSettings.isRemixAtmosphereCloudsEnabled(), "truthy file config should enable mode");
  }

  private static void require(boolean condition, String message) {
    if (!condition) {
      throw new AssertionError(message);
    }
  }
}
