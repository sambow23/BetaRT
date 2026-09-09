import java.util.Arrays;

public final class SignTextMeshTest {
    private static void require(boolean value, String message) {
        if (!value) throw new AssertionError(message);
    }

    private static void close(float actual, float expected) {
        require(Math.abs(actual - expected) < .00001f, actual + " != " + expected);
    }

    public static void main(String[] args) {
        int glyph = fp.a.indexOf('A') + 32;
        int atlasX = glyph % 16 * 8;
        int atlasY = glyph / 16 * 8;
        int[] widths = new int[256];
        widths[glyph] = 7;
        boolean[] pixels = new boolean[128 * 128];
        pixels[(atlasY + 2) * 128 + atlasX + 1] = true;
        pixels[(atlasY + 4) * 128 + atlasX + 3] = true;
        RemixDynamicModelCapture.TextureAlpha font =
                new RemixDynamicModelCapture.TextureAlpha(pixels, 128, 128);
        RemixSignTextMesh cache = new RemixSignTextMesh();
        float[] a = cache.resolve("A", 10, 20, widths, font);
        require(a.length == 40, "One quad per opaque font pixel");
        float span = 7.99f / 8;
        close(a[0], 10 + span);
        close(a[1], 20 + 2 * span + span);
        close(a[2], -.30f);
        close(a[3], (atlasX + 1.5f) / 128);
        close(a[4], (atlasY + 2.5f) / 128);
        close(a[5], a[0] + span);
        close(a[11], 20 + 2 * span);
        close(a[20], 10 + 3 * span);
        require(cache.resolve("A", 10, 20, widths, font) == a, "Reuse cached immutable geometry");
        float[] aa = cache.resolve("AA", 10, 20, widths, font);
        require(aa.length == 80, "Both glyphs emitted");
        close(aa[40], a[0] + 7);
        require(Arrays.equals(aa, cache.resolve("A\u00a7cA", 10, 20, widths, font)),
                "Preserve sign color-code skipping");
        require(Arrays.equals(a, cache.resolve("A\u00a7c", 10, 20, widths, font)),
                "Trailing color code adds no glyph");
        float[] moved = cache.resolve("A", 12, 24, widths, font);
        close(moved[0], a[0] + 2);
        close(moved[1], a[1] + 4);
        widths[glyph] = 9;
        float[] wider = cache.resolve("AA", 10, 20, widths, font);
        close(wider[40], a[0] + 9);
        require(wider != aa, "Invalidate when font metrics change in place");
        font = new RemixDynamicModelCapture.TextureAlpha(new boolean[128 * 128], 128, 128);
        require(cache.resolve("A", 10, 20, widths, font).length == 0, "Invalidate changed font mask");
        font = new RemixDynamicModelCapture.TextureAlpha(new boolean[0], 0, 0);
        float[] fallback = cache.resolve("A", 10, 20, widths, font);
        require(fallback.length == 20, "Missing pixel data uses one textured glyph quad");
        close(fallback[0], 10);
        close(fallback[1], 27.99f);
        close(fallback[3], atlasX / 128.0f);
        close(fallback[8], (atlasX + 7.99f) / 128);
        for (int i = 0; i < 256; ++i) cache.resolve("A", i + 30, 20, widths, font);
        require(cache.resolve("A", 10, 20, widths, font) != fallback, "Bound cache during world travel");
    }
}
