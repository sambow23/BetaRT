package mcrtx.bridge;

import java.io.File;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

/** Private direct-renderer transport. Neither Java buffers nor game objects cross a frame boundary. */
public final class DirectBridge {
    private static volatile boolean running;
    private DirectBridge() { }

    public static synchronized void start(int width, int height) {
        if (running) return;
        if (!RemixBridgeNative.isAvailable()) throw new IllegalStateException(RemixBridgeNative.loadError());
        String path = McrtxRuntimeConfig.getEnvironmentValue("MCRTX_REMIX_PATH");
        if (path.isEmpty()) {
            try {
                File jar = new File(DirectBridge.class.getProtectionDomain().getCodeSource().getLocation().toURI());
                path = new File(jar.isDirectory() ? jar : jar.getParentFile(), System.mapLibraryName("remix")).getAbsolutePath();
            } catch (Exception error) {
                throw new IllegalStateException("Cannot locate the Remix runtime", error);
            }
        }
        if (!nStart(path.getBytes(StandardCharsets.UTF_8), width, height)) {
            String error=nError();nStop();
            throw new IllegalStateException("Remix startup failed: " + error);
        }
        running = true;
    }

    public static synchronized void stop() {
        if (!running) return;
        nStop();
        running = false;
    }
    public static boolean isRunning() { return running; }
    public static boolean submit(ByteBuffer resources, ByteBuffer frame) {
        if(!running)return false;
        if(nSubmit(resources, resources.position(), frame, frame.position()))return true;
        // Closing SDL can race a blocked publication. Let the normal game loop
        // consume the close event instead of reporting a renderer failure.
        int[] state=nPoll();
        return state!=null&&state.length>=9&&state[5]!=0&&nError().isEmpty();
    }
    public static boolean control(int operation, int value, float x, float y, String path) {
        return running && nControl(operation, value, x, y,
                path == null ? null : path.getBytes(StandardCharsets.UTF_8));
    }
    public static int[] poll() { return running ? nPoll() : new int[9]; }
    public static long[] counts() { return running ? nCounts() : new long[4]; }
    public static String error() { return nError(); }

    private static native boolean nStart(byte[] path, int width, int height);
    private static native void nStop();
    private static native boolean nSubmit(ByteBuffer resources, int resourceBytes, ByteBuffer frame, int frameBytes);
    private static native boolean nControl(int operation, int value, float x, float y, byte[] path);
    private static native int[] nPoll();
    private static native long[] nCounts();
    private static native String nError();
}
