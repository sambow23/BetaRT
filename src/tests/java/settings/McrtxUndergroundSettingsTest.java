import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import mcrtx.bridge.McrtxGraphicsSettings;

public final class McrtxUndergroundSettingsTest {
    public static void main(String[] args) throws Exception {
        Path directory = Files.createTempDirectory("betart-underground-setting");
        System.setProperty("user.dir",directory.toString());
        if (McrtxGraphicsSettings.isUndergroundCullingEnabled()) throw new AssertionError("Must be opt-in");
        McrtxGraphicsSettings.setUndergroundCullingEnabled(true);
        if (!McrtxGraphicsSettings.isUndergroundCullingEnabled()) throw new AssertionError("Enable failed");
        String saved = new String(Files.readAllBytes(directory.resolve("mcrtx-runtime.env")),StandardCharsets.US_ASCII);
        if (!saved.contains("MCRTX_UNDERGROUND_CULLING_ENABLED=1")) throw new AssertionError("Setting not persisted");
        McrtxGraphicsSettings.setUndergroundCullingEnabled(false);
        if (McrtxGraphicsSettings.isUndergroundCullingEnabled()) throw new AssertionError("Disable failed");
    }
}
