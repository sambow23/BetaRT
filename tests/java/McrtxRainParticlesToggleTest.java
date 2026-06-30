import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import mcrtx.bridge.McrtxRuntimeSettings;

public final class McrtxRainParticlesToggleTest {
  public static void main(String[] args) throws Exception {
    require(McrtxRuntimeSettings.isGameRainParticlesEnabled(), "game rain should default on");

    Path tempDir = Files.createTempDirectory("mcrtx-rain-toggle");
    System.setProperty("user.dir", tempDir.toString());
    McrtxRuntimeSettings.setGameRainParticlesEnabled(false);
    require(!McrtxRuntimeSettings.isGameRainParticlesEnabled(), "setter should disable game rain");

    String saved = new String(Files.readAllBytes(tempDir.resolve("mcrtx-runtime.env")), StandardCharsets.US_ASCII);
    require(
        saved.indexOf("MCRTX_GAME_RAIN_PARTICLES_ENABLED=0") >= 0,
        "saved config should include disabled game rain");

    String runtimeSettings = read("java-src/mcrtx/bridge/McrtxRuntimeSettings.java");
    requireContains(runtimeSettings, "GAME_RAIN_PARTICLES_ENABLED_KEY", "rain config key");
    requireContains(runtimeSettings, "DEFAULT_GAME_RAIN_PARTICLES_ENABLED", "rain default");
    requireContains(runtimeSettings, "isGameRainParticlesEnabled()", "rain getter");
    requireContains(runtimeSettings, "setGameRainParticlesEnabled(boolean enabled)", "rain setter");

    String remixHooks = read("java-src/MinecraftRemixHooks.java");
    requireContains(remixHooks, "isGameRainParticlesEnabled()", "hook getter");
    requireContains(remixHooks, "setGameRainParticlesEnabled(boolean enabled)", "hook setter");
    requireContains(remixHooks, "getGameRainParticlesButtonLabel()", "hook label");

    String quickSettings = read("java-src/McrtxQuickSettingsScreen.java");
    requireContains(quickSettings, "GAME_RAIN_PARTICLES_BUTTON_ID", "button id");
    requireContains(quickSettings, "MinecraftRemixHooks.setGameRainParticlesEnabled", "button toggles setting");
    requireContains(quickSettings, "MinecraftRemixHooks.getGameRainParticlesButtonLabel()", "button label");

    String particleCapture = read("java-src/RemixParticleCapture.java");
    requireContains(particleCapture, "McrtxRuntimeSettings.isGameRainParticlesEnabled()", "particle capture reads setting");
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
