import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import mcrtx.bridge.McrtxMaterialSettings;

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

    require(McrtxMaterialSettings.DEFAULT_WATER_TRANSMITTANCE_RED_HUNDREDTHS == 74, "red default");
    require(McrtxMaterialSettings.DEFAULT_WATER_TRANSMITTANCE_GREEN_HUNDREDTHS == 90, "green default");
    require(McrtxMaterialSettings.DEFAULT_WATER_TRANSMITTANCE_BLUE_HUNDREDTHS == 100, "blue default");
    require(McrtxMaterialSettings.DEFAULT_WATER_TRANSMITTANCE_DISTANCE_HUNDREDTHS == 150, "distance default");
    require(McrtxMaterialSettings.DEFAULT_WATER_REFRACTIVE_INDEX_THOUSANDTHS == 1333, "ior default");
    require(McrtxMaterialSettings.DEFAULT_WATER_DIFFUSE_LAYER_ENABLED, "diffuse default");

    require(McrtxMaterialSettings.getWaterTransmittanceRedHundredths() == 55, "load red");
    require(McrtxMaterialSettings.getWaterTransmittanceGreenHundredths() == 65, "load green");
    require(McrtxMaterialSettings.getWaterTransmittanceBlueHundredths() == 75, "load blue");
    require(McrtxMaterialSettings.getWaterTransmittanceDistanceHundredths() == 250, "load distance");
    require(McrtxMaterialSettings.getWaterRefractiveIndexThousandths() == 1400, "load ior");
    require(!McrtxMaterialSettings.isWaterDiffuseLayerEnabled(), "load diffuse");

    McrtxMaterialSettings.setWaterTransmittanceRedHundredths(-1);
    McrtxMaterialSettings.setWaterTransmittanceGreenHundredths(101);
    McrtxMaterialSettings.setWaterTransmittanceBlueHundredths(0);
    McrtxMaterialSettings.setWaterTransmittanceDistanceHundredths(2501);
    McrtxMaterialSettings.setWaterRefractiveIndexThousandths(999);
    McrtxMaterialSettings.setWaterDiffuseLayerEnabled(true);

    require(McrtxMaterialSettings.getWaterTransmittanceRedHundredths() == 1, "clamp red min");
    require(McrtxMaterialSettings.getWaterTransmittanceGreenHundredths() == 100, "clamp green max");
    require(McrtxMaterialSettings.getWaterTransmittanceBlueHundredths() == 1, "clamp blue min");
    require(McrtxMaterialSettings.getWaterTransmittanceDistanceHundredths() == 2500, "clamp distance max");
    require(McrtxMaterialSettings.getWaterRefractiveIndexThousandths() == 1000, "clamp ior min");
    require(McrtxMaterialSettings.isWaterDiffuseLayerEnabled(), "set diffuse");

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
