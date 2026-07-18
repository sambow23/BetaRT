public final class SignModelVisibilityTest {
  public static void main(String[] args) {
    ps visiblePart = new ps(0, 0);
    assertCaptureVisibility("visible model part", visiblePart, true);

    ps hiddenPart = new ps(0, 0);
    hiddenPart.h = false;
    assertCaptureVisibility("hidden model part", hiddenPart, false);

    ps skippedPart = new ps(0, 0);
    skippedPart.i = true;
    assertCaptureVisibility("skipped model part", skippedPart, false);

    assertCaptureVisibility("null model part", null, false);
  }

  private static void assertCaptureVisibility(String name, ps modelPart, boolean expected) {
    boolean actual = RemixDynamicEntityCapture.shouldCaptureModelPart(modelPart);
    if (actual != expected) {
      throw new AssertionError(name + " expected " + expected + " got " + actual);
    }
  }
}
