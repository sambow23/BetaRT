import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import mcrtx.bridge.McrtxRuntimeSettings;

public final class McrtxWaterTransmissionSettingsTest {
  public static void main(String[] args) throws Exception {
    Path tempDir = Files.createTempDirectory("mcrtx-water-transmission");
    Files.write(
        tempDir.resolve("mcrtx-runtime.env"),
        ("MCRTX_WATER_TRANSMITTANCE_COLOR_R=0.55\n"
            + "MCRTX_WATER_TRANSMITTANCE_COLOR_G=0.65\n"
            + "MCRTX_WATER_TRANSMITTANCE_COLOR_B=0.75\n"
            + "MCRTX_WATER_TRANSMITTANCE_MEASUREMENT_DISTANCE=2.50\n"
            + "MCRTX_WATER_REFRACTIVE_INDEX=1.400\n"
            + "MCRTX_WATER_USE_DIFFUSE_LAYER=0\n")
            .getBytes(StandardCharsets.US_ASCII));
    System.setProperty("user.dir", tempDir.toString());

    require(McrtxRuntimeSettings.DEFAULT_WATER_TRANSMITTANCE_RED_HUNDREDTHS == 74, "red default");
    require(McrtxRuntimeSettings.DEFAULT_WATER_TRANSMITTANCE_GREEN_HUNDREDTHS == 90, "green default");
    require(McrtxRuntimeSettings.DEFAULT_WATER_TRANSMITTANCE_BLUE_HUNDREDTHS == 100, "blue default");
    require(McrtxRuntimeSettings.DEFAULT_WATER_TRANSMITTANCE_DISTANCE_HUNDREDTHS == 150, "distance default");
    require(McrtxRuntimeSettings.DEFAULT_WATER_REFRACTIVE_INDEX_THOUSANDTHS == 1333, "ior default");
    require(McrtxRuntimeSettings.DEFAULT_WATER_DIFFUSE_LAYER_ENABLED, "diffuse default");

    require(McrtxRuntimeSettings.getWaterTransmittanceRedHundredths() == 55, "load red");
    require(McrtxRuntimeSettings.getWaterTransmittanceGreenHundredths() == 65, "load green");
    require(McrtxRuntimeSettings.getWaterTransmittanceBlueHundredths() == 75, "load blue");
    require(McrtxRuntimeSettings.getWaterTransmittanceDistanceHundredths() == 250, "load distance");
    require(McrtxRuntimeSettings.getWaterRefractiveIndexThousandths() == 1400, "load ior");
    require(!McrtxRuntimeSettings.isWaterDiffuseLayerEnabled(), "load diffuse");

    McrtxRuntimeSettings.setWaterTransmittanceRedHundredths(-1);
    McrtxRuntimeSettings.setWaterTransmittanceGreenHundredths(101);
    McrtxRuntimeSettings.setWaterTransmittanceBlueHundredths(0);
    McrtxRuntimeSettings.setWaterTransmittanceDistanceHundredths(2501);
    McrtxRuntimeSettings.setWaterRefractiveIndexThousandths(999);
    McrtxRuntimeSettings.setWaterDiffuseLayerEnabled(true);

    require(McrtxRuntimeSettings.getWaterTransmittanceRedHundredths() == 1, "clamp red min");
    require(McrtxRuntimeSettings.getWaterTransmittanceGreenHundredths() == 100, "clamp green max");
    require(McrtxRuntimeSettings.getWaterTransmittanceBlueHundredths() == 1, "clamp blue min");
    require(McrtxRuntimeSettings.getWaterTransmittanceDistanceHundredths() == 2500, "clamp distance max");
    require(McrtxRuntimeSettings.getWaterRefractiveIndexThousandths() == 1000, "clamp ior min");
    require(McrtxRuntimeSettings.isWaterDiffuseLayerEnabled(), "set diffuse");

    String saved = new String(
        Files.readAllBytes(tempDir.resolve("mcrtx-runtime.env")),
        StandardCharsets.US_ASCII);
    requireContains(saved, "MCRTX_WATER_TRANSMITTANCE_COLOR_R=0.01", "save red");
    requireContains(saved, "MCRTX_WATER_TRANSMITTANCE_COLOR_G=1.00", "save green");
    requireContains(saved, "MCRTX_WATER_TRANSMITTANCE_COLOR_B=0.01", "save blue");
    requireContains(saved, "MCRTX_WATER_TRANSMITTANCE_MEASUREMENT_DISTANCE=25.00", "save distance");
    requireContains(saved, "MCRTX_WATER_REFRACTIVE_INDEX=1.000", "save ior");
    requireContains(saved, "MCRTX_WATER_USE_DIFFUSE_LAYER=1", "save diffuse");
  }

  private static void requireContains(String haystack, String needle, String message) {
    require(haystack.indexOf(needle) >= 0, message + " missing: " + needle);
  }

  private static void require(boolean condition, String message) {
    if (!condition) {
      throw new AssertionError(message);
    }
  }
}
