import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

public final class McrtxNameTagCaptureTest {
  public static void main(String[] args) throws Exception {
    String uiCapture = read("java-src/RemixUiCapture.java");
    String hooks = read("java-src/MinecraftRemixHooks.java");
    String patchTool = read("tools-src/mcrtx/tools/ClientPatchTool.java");

    requireContains(uiCapture, "beginNameTagCapture(", "name-tag capture begin API");
    requireContains(uiCapture, "endNameTagCapture()", "name-tag capture end API");
    requireContains(uiCapture, "isCapturingUiOrNameTags()", "combined UI/name-tag capture predicate");
    requireContains(uiCapture, "hasPendingNameTagDraws()", "pending name-tag predicate");
    requireContains(uiCapture, "seedActiveDrawListFromPendingNameTags()", "pending name-tag merge helper");
    requireContains(uiCapture, "projectToScreenPixels(", "perspective-aware screen projection helper");
    requireContains(uiCapture, "pendingNameTagVertexCount", "pending name-tag vertex count");
    requireContains(uiCapture, "pendingNameTagCmdCount", "pending name-tag command count");
    requireContains(uiCapture, "projectionForCurrentCapture(", "name-tag FOV projection helper");
    requireContains(uiCapture, "McrtxRuntimeSettings.getGameplayFovDegrees()", "gameplay FOV source");
    requireContains(uiCapture, "beginProjectedLabelCapture(", "projected label rollback start");
    requireContains(uiCapture, "discardActiveNameTagCapture()", "invalid name-tag rollback");
    requireContains(uiCapture, "isNameTagAnchorVisible(", "name-tag anchor clip rejection");
    requireContains(uiCapture, "nameTagCommandFlags()", "name-tag depth-test command flags");
    requireContains(uiCapture, "projected.depth", "projected name-tag vertex depth");
    requireContains(uiCapture, "NAME_TAG_DEPTH_BIAS", "name-tag stable depth bias");
    requireContains(uiCapture, "nameTagAnchorDepth", "name-tag cached anchor depth");
    requireContains(uiCapture, "nameTagDepthForLayer(", "name-tag text/backing depth split");
    requireContains(uiCapture, "nameTagAlphaScale", "name-tag distance fade scale");
    requireContains(uiCapture, "computeNameTagAlphaScale(", "name-tag distance fade helper");
    requireContains(uiCapture, "applyNameTagFade(", "name-tag alpha fade application");

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
