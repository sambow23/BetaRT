import java.util.Locale;
import mcrtx.bridge.RemixLifecycleBridge;
import net.minecraft.client.Minecraft;

public final class McrtxFrameRate {
    private static final McrtxFrameRate DISPLAY = new McrtxFrameRate();
    private long lastSample;
    private long lastFrames = -1;
    private long generation = -1;
    private String text = "Remix -- fps, Java ";

    public static String formatDebug(String javaFps) {
        long now = System.nanoTime();
        long epoch = RemixLifecycleBridge.getRendererGeneration();
        if (epoch != DISPLAY.generation || now - DISPLAY.lastSample >= 500000000L) {
            DISPLAY.sample(now, RemixLifecycleBridge.getSubmittedFrameCount(), epoch, javaFps);
        }
        return DISPLAY.text;
    }

    public static String drawDebug(Minecraft minecraft) {
        String display = formatDebug(minecraft.K);
        minecraft.q.a(display.substring(0, display.indexOf(", Java ")), 2, 96, 0xffffff);
        minecraft.q.a("Java " + minecraft.K, 2, 104, 0xffffff);
        return minecraft.K;
    }

    String sample(long now, long frames, long epoch, String javaFps) {
        long elapsed = now - lastSample;
        long count = frames - lastFrames;
        if (epoch != generation || frames < 0 || lastFrames < 0 || count < 0 || elapsed <= 0) {
            text = "Remix -- fps, Java " + javaFps;
        } else if (count == 0) {
            text = "Remix 0 fps (-- ms), Java " + javaFps;
        } else {
            text = String.format(Locale.ROOT, "Remix %.0f fps (%.1f ms), Java %s",
                    count * 1.0e9 / elapsed, elapsed / (count * 1.0e6), javaFps);
        }
        lastSample = now;
        lastFrames = frames;
        generation = epoch;
        return text;
    }
}
