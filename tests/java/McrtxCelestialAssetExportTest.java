import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

public final class McrtxCelestialAssetExportTest {
  public static void main(String[] args) throws Exception {
    String script = read("scripts/build-patched-client.ps1");

    requireContains(
        script,
        "Export-ZipEntryFile -ArchivePath $PatchSourceJar -EntryName \"terrain/sun.png\" -DestinationPath (Join-Path $assetsDir \"sun.png\")",
        "sun png export");
    requireContains(
        script,
        "Convert-PngToDds -SourcePngPath (Join-Path $assetsDir \"sun.png\") -DestinationDdsPath (Join-Path $assetsDir \"sun.dds\")",
        "sun dds conversion");
    requireContains(
        script,
        "Export-ZipEntryFile -ArchivePath $PatchSourceJar -EntryName \"terrain/moon.png\" -DestinationPath (Join-Path $assetsDir \"moon.png\")",
        "moon png export");
    requireContains(
        script,
        "Convert-PngToDds -SourcePngPath (Join-Path $assetsDir \"moon.png\") -DestinationDdsPath (Join-Path $assetsDir \"moon.dds\")",
        "moon dds conversion");
  }

  private static String read(String path) throws Exception {
    return new String(Files.readAllBytes(Paths.get(path)), StandardCharsets.UTF_8);
  }

  private static void requireContains(String haystack, String needle, String message) {
    if (haystack.indexOf(needle) < 0) {
      throw new AssertionError(message + " missing: " + needle);
    }
  }
}
