import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

public final class McrtxNameTagCaptureTest {
  public static void main(String[] args) throws Exception {
    String uiCapture = read("src/java-src/ui/RemixUiCapture.java");
    String session = read("src/java-src/ui/RemixUiCaptureSession.java");
    String projection = read("src/java-src/ui/RemixUiProjection.java");
    String nameTags = read("src/java-src/ui/RemixNameTagCapture.java");
    String hooks = read("src/java-src/ui/MinecraftRemixUiHooks.java");
    String patchTool = read("src/tools-src/patcher/mcrtx/tools/ClientPatchTool.java");

    requireContains(uiCapture, "beginNameTagCapture(", "name-tag capture begin API");
    requireContains(uiCapture, "endNameTagCapture()", "name-tag capture end API");
    requireContains(uiCapture, "isCapturingUiOrNameTags()", "combined UI/name-tag capture predicate");
    requireContains(uiCapture, "hasPendingNameTagDraws()", "pending name-tag predicate");
    requireContains(session, "pendingNameTagCheckpoint", "pending name-tag checkpoint");
    requireContains(session, "DRAW_LIST.rollback(pendingNameTagCheckpoint)", "pending name-tag merge");
    requireContains(session, "nameTagCaptureStartCheckpoint", "projected label rollback start");
    requireContains(session, "discardActiveNameTagCapture()", "invalid name-tag rollback");
    requireContains(projection, "projectToScreenPixels(", "perspective-aware screen projection helper");
    requireContains(nameTags, "adjustProjection(", "name-tag FOV projection helper");
    requireContains(nameTags, "McrtxGameplaySettings.getGameplayFovDegrees()", "gameplay FOV source");
    requireContains(nameTags, "acceptAnchor(", "name-tag anchor clip rejection");
    requireContains(nameTags, "commandFlags()", "name-tag depth-test command flags");
    requireContains(nameTags, "projected.depth", "projected name-tag vertex depth");
    requireContains(nameTags, "NAME_TAG_DEPTH_BIAS", "name-tag stable depth bias");
    requireContains(nameTags, "anchorDepth", "name-tag cached anchor depth");
    requireContains(nameTags, "depthForLayer(", "name-tag text/backing depth split");
    requireContains(nameTags, "alphaScale", "name-tag distance fade scale");
    requireContains(nameTags, "computeNameTagAlphaScale(", "name-tag distance fade helper");
    requireContains(nameTags, "applyFade(", "name-tag alpha fade application");

    requireContains(hooks, "onNameTagRenderBegin()", "name-tag render begin hook");
    requireContains(hooks, "onNameTagRenderEnd()", "name-tag render end hook");
    requireContains(hooks, "RemixUiCapture.beginNameTagCapture", "hook begins Remix UI name-tag capture");
    requireContains(hooks, "RemixUiCapture.endNameTagCapture", "hook ends Remix UI name-tag capture");

    requireContains(patchTool, "BASE_LIVING_RENDERER_CLASS = \"gv\"", "base living renderer patch target");
    requireContains(patchTool, "patchGv", "generic name-tag renderer patch");
    requireContains(patchTool, "patchDsNameTagRender", "player-specific name-tag renderer patch");
    requireContains(patchTool, "onNameTagRenderBegin", "patcher injects name-tag begin hook");
    requireContains(patchTool, "onNameTagRenderEnd", "patcher injects name-tag end hook");
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
