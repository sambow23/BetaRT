import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

/** Local sign-text geometry; transforms and color are supplied separately each frame. */
final class RemixSignTextMesh {
    private static final float GLYPH_SIZE = 7.99f;
    private static final float TEXT_DEPTH = -0.30f;
    private static final int MAX_ENTRIES = 256;
    private static final int MAX_FLOATS = 2 * 1024 * 1024;
    private final LinkedHashMap<Key, float[]> meshes = new LinkedHashMap<Key, float[]>(16, .75f, true);
    private RemixDynamicModelCapture.TextureAlpha font;
    private int[] widths;
    private int cachedFloats;

    float[] resolve(String text, int x, int y, int[] characterWidths,
            RemixDynamicModelCapture.TextureAlpha alpha) {
        if (font != alpha || !Arrays.equals(widths, characterWidths)) {
            meshes.clear();
            cachedFloats = 0;
            font = alpha;
            widths = characterWidths.clone();
        }
        Key key = new Key(text, x, y);
        float[] cached = meshes.get(key);
        if (cached != null) {
            return cached;
        }
        float[] built = build(text, x, y, characterWidths, alpha);
        if (built.length <= MAX_FLOATS) {
            Iterator<Map.Entry<Key, float[]>> entries = meshes.entrySet().iterator();
            while (entries.hasNext()
                    && (meshes.size() >= MAX_ENTRIES || cachedFloats + built.length > MAX_FLOATS)) {
                cachedFloats -= entries.next().getValue().length;
                entries.remove();
            }
            meshes.put(key, built);
            cachedFloats += built.length;
        }
        return built;
    }

    private static float[] build(String text, int x, int y, int[] widths,
            RemixDynamicModelCapture.TextureAlpha alpha) {
        Quads out = new Quads();
        boolean pixels = alpha.width == 128 && alpha.height == 128 && alpha.hasPixels();
        float cursor = x;
        for (int i = 0; i < text.length(); ++i) {
            while (i + 1 < text.length() && text.charAt(i) == '\u00a7') {
                i += 2;
            }
            if (i >= text.length()) {
                break;
            }
            int index = fp.a.indexOf(text.charAt(i));
            int glyph = index + 32;
            if (index < 0 || glyph >= widths.length) {
                continue;
            }
            int atlasX = glyph % 16 * 8;
            int atlasY = glyph / 16 * 8;
            if (pixels) {
                float span = GLYPH_SIZE / 8;
                for (int py = 0; py < 8; ++py) {
                    for (int px = 0; px < 8; ++px) {
                        if (alpha.isTexturePixelOpaque(atlasX + px, atlasY + py)) {
                            float left = cursor + px * span;
                            float top = y + py * span;
                            float u = (atlasX + px + .5f) / 128;
                            float v = (atlasY + py + .5f) / 128;
                            out.append(left, top, left + span, top + span, u, v, u, v);
                        }
                    }
                }
            } else {
                out.append(cursor, y, cursor + GLYPH_SIZE, y + GLYPH_SIZE,
                        atlasX / 128.0f, atlasY / 128.0f,
                        (atlasX + GLYPH_SIZE) / 128, (atlasY + GLYPH_SIZE) / 128);
            }
            cursor += widths[glyph];
        }
        return Arrays.copyOf(out.data, out.used);
    }

    private static final class Quads {
        float[] data = new float[320];
        int used;

        void append(float x0, float y0, float x1, float y1, float u0, float v0, float u1, float v1) {
            if (used + 20 > data.length) {
                data = Arrays.copyOf(data, data.length * 2);
            }
            vertex(x0, y1, u0, v1);
            vertex(x1, y1, u1, v1);
            vertex(x1, y0, u1, v0);
            vertex(x0, y0, u0, v0);
        }

        void vertex(float x, float y, float u, float v) {
            data[used++] = x;
            data[used++] = y;
            data[used++] = TEXT_DEPTH;
            data[used++] = u;
            data[used++] = v;
        }
    }

    private static final class Key {
        final String text;
        final int x, y;

        Key(String text, int x, int y) {
            this.text = text;
            this.x = x;
            this.y = y;
        }

        @Override public int hashCode() {
            return (text.hashCode() * 31 + x) * 31 + y;
        }

        @Override public boolean equals(Object value) {
            if (!(value instanceof Key)) {
                return false;
            }
            Key other = (Key) value;
            return x == other.x && y == other.y && text.equals(other.text);
        }
    }
}
