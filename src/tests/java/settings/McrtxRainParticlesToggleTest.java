import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import mcrtx.bridge.McrtxGraphicsSettings;

public final class McrtxRainParticlesToggleTest {
  public static void main(String[] args) throws Exception {
    Path tempDir = Files.createTempDirectory("mcrtx-rain-toggle");
    System.setProperty("user.dir", tempDir.toString());
    require(McrtxGraphicsSettings.isGameRainParticlesEnabled(), "game rain should default on");
    McrtxGraphicsSettings.setGameRainParticlesEnabled(false);
    require(!McrtxGraphicsSettings.isGameRainParticlesEnabled(), "setter should disable game rain");

    String saved = new String(Files.readAllBytes(tempDir.resolve("mcrtx-runtime.env")), StandardCharsets.US_ASCII);
    require(
        saved.indexOf("MCRTX_GAME_RAIN_PARTICLES_ENABLED=0") >= 0,
        "saved config should include disabled game rain");

    String graphicsSettings = read("src/java-src/settings/mcrtx/bridge/McrtxGraphicsSettings.java");
    requireContains(graphicsSettings, "GAME_RAIN_PARTICLES_ENABLED_KEY", "rain config key");
    requireContains(graphicsSettings, "DEFAULT_GAME_RAIN_PARTICLES_ENABLED", "rain default");
    requireContains(graphicsSettings, "isGameRainParticlesEnabled()", "rain getter");
    requireContains(graphicsSettings, "setGameRainParticlesEnabled(boolean enabled)", "rain setter");

    String graphicsUi = read("src/java-src/settings/McrtxGraphicsSettingsUi.java");
    requireContains(graphicsUi, "GAME_RAIN_PARTICLES_BUTTON_ID", "button id");
    requireContains(graphicsUi, "McrtxGraphicsSettings.setGameRainParticlesEnabled", "button toggles setting");
    requireContains(graphicsUi, "Game Rain: ", "button label");

    String particleCapture = read("src/java-src/particles/RemixParticleCapture.java");
    requireContains(particleCapture, "McrtxGraphicsSettings.isGameRainParticlesEnabled()", "particle capture reads setting");
    requireContains(particleCapture, "shouldSubmitWeatherRainQuad()", "weather rain submission gate");
    requireContains(particleCapture, "if (!shouldSubmitWeatherRainQuad())", "rain quads skipped when disabled");
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
