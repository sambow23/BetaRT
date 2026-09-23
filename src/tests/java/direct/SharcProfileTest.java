import net.minecraft.client.Minecraft;
import org.lwjgl.opengl.Display;
import mcrtx.bridge.DirectBridge;
import java.io.*;
import java.nio.file.Files;

/** Fixed server terrain snapshot, held entirely in RAM. Never connects or opens a save. */
public final class SharcProfileTest {
    static { System.load(System.getProperty("mcrtx.test.profileHelper")); }
    private static native long[] statistics(String library);
    private static native int configure(String library, String key, String value);
    private static void config(String key, String value) {
        int code = configure(System.getenv("MCRTX_REMIX_PATH"), "rtx.native.pathtrace." + key, value);
        if (code != 0) throw new AssertionError("Configuration failed: " + key + ": " + code);
    }
    private static java.lang.reflect.Field focus;
    private static double x, y, z;
    private static float yaw, pitch;
    private static int serial;
    private static final class World extends fd {
        World(long seed) { super(new wy(), "SharcProfile", xa.a(0), seed); }
        public cl b() { return new uv(this); }
    }
    private static byte[] bytes(DataInputStream in) throws Exception {
        int count = in.readInt();
        if (count < 0 || count > 32768) throw new AssertionError("Invalid chunk array");
        byte[] data = new byte[count]; in.readFully(data); return data;
    }
    private static void frame(Minecraft game) throws Exception {
        // The renderer's opt-in REMIX_PROFILE_EVERY_FRAME bypasses final-image
        // reuse while preserving this exact camera and temporal/cache history.
        float motion = Boolean.getBoolean("mcrtx.test.motion") ? (float)Math.sin(serial * .008) * 8 : 0;
        ++serial;
        // Snapshot Y is already at eye height. The spawn setter c(...) adds
        // bf again and only sets current rotation. Set both interpolation
        // endpoints explicitly; e(...) only updates position/bounds.
        game.h.bf = 1.62f; game.h.aa = 0; game.h.e(x, y, z);
        game.h.aJ = game.h.aM = game.h.bl = x;
        game.h.aK = game.h.aN = game.h.bm = y;
        game.h.aL = game.h.aO = game.h.bn = z;
        game.h.aS = game.h.aU = yaw + motion;
        game.h.aT = game.h.aV = pitch;
        focus.setLong(game.t, System.currentTimeMillis());
        game.r = null; game.N = false;
        game.t.b(.5f); Display.update();
        if (Math.abs(DirectGameWorld.originX - x) > 1e-9 || Math.abs(DirectGameWorld.originY - y) > 1e-9 ||
                Math.abs(DirectGameWorld.originZ - z) > 1e-9 || game.h.aS != yaw + motion || game.h.aT != pitch)
            throw new AssertionError("Logged-out camera was not submitted");
        if (!DirectBridge.error().isEmpty()) throw new AssertionError(DirectBridge.error());
    }
    private static void renderFor(Minecraft game, long ms) throws Exception {
        long end = System.nanoTime() + ms * 1000000L;
        while (System.nanoTime() < end) frame(game);
    }
    private static long warmup(Minecraft game, long minimumMs) throws Exception {
        long start = System.nanoTime(), changed = start, sampleAt = start;
        long stableFrame = 0;
        long[] previous = null;
        while (true) {
            frame(game);
            long now = System.nanoTime();
            if (now >= sampleAt) {
                long[] state = statistics(System.getenv("MCRTX_REMIX_PATH"));
                if (state == null) throw new AssertionError("Missing warmup diagnostics");
                if (previous == null || state[8] != previous[8] || state[9] != previous[9] || state[10] != previous[10]) {
                    changed = now;
                    stableFrame = state[0];
                }
                previous = state; sampleAt = now + 1000000000L;
            }
            // A cold shader compile can take longer than the entire warmup
            // while rendering only one frame. Require actual GPU progress too.
            if (previous != null && previous[0] - stableFrame >= 128 &&
                    now - start >= minimumMs * 1000000L && now - changed >= 10000000000L)
                return (now - start) / 1000000L;
            if (now - start > 180000000000L) throw new AssertionError("Terrain/cache warmup did not settle");
        }
    }
    public static void main(String[] args) throws Exception {
        File out = new File(args[0]); out.mkdirs(); Minecraft.af = out;
        focus = px.class.getDeclaredField("H"); focus.setAccessible(true);
        Minecraft game = new Minecraft(null, null, null,
                Integer.getInteger("mcrtx.test.width", 1920), Integer.getInteger("mcrtx.test.height", 1080), false) {
            public void a(mh report) { throw new AssertionError(report.a, report.b); }
        };
        game.k = new gr("SharcProfile", "0");
        try {
            game.a(); game.z.e = 2; game.z.z = false; game.z.j = false; game.z.i = 0;
            DataInputStream in = new DataInputStream(new BufferedInputStream(new FileInputStream(args[1])));
            if (in.readInt() != 1) throw new AssertionError("Snapshot version");
            int chunks = in.readInt();
            x = in.readDouble(); y = in.readDouble(); z = in.readDouble();
            yaw = in.readFloat(); pitch = in.readFloat();
            World world = new World(in.readLong()); world.B = true; world.a(in.readLong());
            for (int i = 0; i < chunks; ++i) {
                int cx = in.readInt(), cz = in.readInt();
                lm chunk = ((uv)world.w()).c(cx, cz);
                chunk.b = bytes(in); chunk.e = new wi(bytes(in)); chunk.f = new wi(bytes(in));
                chunk.g = new wi(bytes(in)); chunk.h = bytes(in);
            }
            in.close();
            game.f = world; game.h = new dc(game, world, game.k, 0);
            game.h.c(x, y, z, yaw, pitch); game.i = game.h; game.h.a = new uo();
            game.c = new ob(game); game.g.a(world); game.j.a(world);
            game.r = null; game.N = false; th.a.f = new ra(game);
            long fresh = DirectBridge.counts()[1], deadline = System.nanoTime() + 240000000000L;
            while (DirectBridge.counts()[1] < fresh + 24 && System.nanoTime() < deadline) {
                frame(game); Thread.sleep(8);
            }
            if (DirectBridge.counts()[1] < fresh + 24) throw new AssertionError("No fresh world frames");
            long warmup = Long.getLong("mcrtx.test.warmupMs", 10000L);
            long duration = Long.getLong("mcrtx.test.measureMs", 20000L);
            String phaseFile = System.getProperty("mcrtx.test.phases", "");
            java.util.List<String> phases = phaseFile.isEmpty() ? java.util.Collections.singletonList("") :
                    Files.readAllLines(new File(phaseFile).toPath(), java.nio.charset.StandardCharsets.UTF_8);
            for (String phase : phases) {
                File destination = out;
                if (!phase.isEmpty()) {
                    String[] values = phase.split(",");
                    if ((values.length < 5 || values.length > 7) || !values[0].matches("[a-z0-9-]+"))
                        throw new AssertionError("Invalid phase");
                    destination = new File(out, values[0]);
                    if (!destination.mkdir()) throw new AssertionError("Existing phase: " + destination);
                    config("sharc", values[1]); config("bounces", values[2]);
                    config("sharcCapacityLog2", values[3]); config("sharcDownscale", values[4]);
                    if (values.length >= 6) {
                        if (!values[5].matches("[01a]")) throw new AssertionError("Invalid dispatch mode");
                        if (values.length == 7 && !values[6].matches("[01]"))
                            throw new AssertionError("Invalid placement mode");
                        java.nio.file.Path mode = new File(System.getenv("REMIX_INDIRECT_MODE_FILE")).toPath();
                        java.nio.file.Path next = mode.resolveSibling(mode.getFileName() + ".next");
                        String controls = values[5] + (values.length == 7 ? "," + values[6] : "");
                        Files.write(next, controls.getBytes("UTF-8"));
                        Files.move(next, mode, java.nio.file.StandardCopyOption.ATOMIC_MOVE,
                                java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                    }
                    config("sharcReset", "1");
                    System.out.println("PHASE " + phase);
                }
                System.out.println("WARMUP " + x + " " + y + " " + z + " " + yaw + " " + pitch);
                long actualWarmup = warmup(game, warmup);
                long[] gpuBefore = statistics(System.getenv("MCRTX_REMIX_PATH"));
                if (gpuBefore == null) throw new AssertionError("Missing renderer diagnostics");
                boolean dlss = "dlss".equals(System.getenv("MCRTX_PT_UPSCALER"));
                if (gpuBefore[2] != (dlss ? 2 : 0) || gpuBefore[7] != 0)
                    throw new AssertionError("Requested reconstruction backend is unavailable");
                long[] before = DirectBridge.counts(); long start = System.currentTimeMillis();
                System.out.println("MEASURE_START " + start);
                renderFor(game, duration);
                long end = System.currentTimeMillis(); long[] after = DirectBridge.counts();
                long[] gpuAfter = statistics(System.getenv("MCRTX_REMIX_PATH"));
                String result = "{\"start_ms\":" + start + ",\"end_ms\":" + end + ",\"warmup_ms\":" + actualWarmup +
                        ",\"fresh\":" + (after[1] - before[1]) + ",\"published\":" + (after[0] - before[0]) +
                        ",\"presented\":" + (after[2] - before[2]) + ",\"chunks\":" + chunks +
                        ",\"gpu_before\":" + java.util.Arrays.toString(gpuBefore) +
                        ",\"gpu_after\":" + java.util.Arrays.toString(gpuAfter) + "}";
                Files.write(new File(destination, "measurement.json").toPath(), result.getBytes("UTF-8"));
                System.out.println("MEASURE_END " + result);
                if (Boolean.getBoolean("mcrtx.test.capture")) {
                    File image = new File(destination, "world.png");
                    DirectBridge.control(6, 0, 0, 0, image.getAbsolutePath());
                    deadline = System.nanoTime() + 10000000000L;
                    while (!image.isFile() && System.nanoTime() < deadline) frame(game);
                    if (!image.isFile()) throw new AssertionError("Missing capture");
                }
            }
            game.g.a((fd)null);
        } finally {
            if (game.U != null) game.U.b(); game.J = false; game.B.b(); Display.destroy();
        }
        System.exit(0);
    }
}
