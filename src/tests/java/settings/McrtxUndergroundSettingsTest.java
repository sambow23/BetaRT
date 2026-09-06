import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import mcrtx.bridge.McrtxGraphicsSettings;

public final class McrtxUndergroundSettingsTest {
    public static void main(String[] args) throws Exception {
        Path directory = Files.createTempDirectory("betart-underground-setting");
        System.setProperty("user.dir",directory.toString());
        Files.write(directory.resolve("mcrtx-runtime.env"),
                "MCRTX_UNDERGROUND_CULLING_ENABLED=1\nMCRTX_NO_CULL_DISTANCE=123\n".getBytes(StandardCharsets.US_ASCII));
        if (McrtxGraphicsSettings.getNoCullDistanceBlocks() != 123) throw new AssertionError("Unrelated setting changed");
        McrtxGraphicsSettings.setNoCullDistanceBlocks(124);
        String saved = new String(Files.readAllBytes(directory.resolve("mcrtx-runtime.env")), StandardCharsets.US_ASCII);
        if (!saved.contains("MCRTX_NO_CULL_DISTANCE=124")) throw new AssertionError("Unrelated setting not saved");
    }
}
