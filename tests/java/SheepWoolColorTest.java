public final class SheepWoolColorTest {
  public static void main(String[] args) {
    assertColor(
        "sheep wool preserves grayscale dye",
        RemixDynamicEntityCapture.sanitizeDynamicModelPartColor("/mob/sheep_fur.png", 0.1f, 0.1f, 0.1f, 1.0f),
        0.1f, 0.1f, 0.1f, 1.0f);

    assertColor(
        "ordinary entity grayscale is treated as lighting",
        RemixDynamicEntityCapture.sanitizeDynamicModelPartColor("/mob/sheep.png", 0.1f, 0.1f, 0.1f, 1.0f),
        1.0f, 1.0f, 1.0f, 1.0f);

    assertColor(
        "ordinary entity chroma tint is preserved",
        RemixDynamicEntityCapture.sanitizeDynamicModelPartColor("/mob/sheep.png", 0.9f, 0.5f, 0.85f, 1.0f),
        0.9f, 0.5f, 0.85f, 1.0f);
  }

  private static void assertColor(String name, float[] actual, float red, float green, float blue, float alpha) {
    if (actual == null || actual.length != 4) {
      throw new AssertionError(name + " returned invalid color array");
    }
    assertClose(name + " red", actual[0], red);
    assertClose(name + " green", actual[1], green);
    assertClose(name + " blue", actual[2], blue);
    assertClose(name + " alpha", actual[3], alpha);
  }

  private static void assertClose(String name, float actual, float expected) {
    if (Math.abs(actual - expected) > 0.0001f) {
      throw new AssertionError(name + " expected " + expected + " got " + actual);
    }
  }
}
