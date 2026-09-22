import mcrtx.bridge.DirectBridge;

/** Run with MCRTX_REMIX_PATH pointing to a nonexistent library; needs no GPU. */
public final class DirectLibraryFailureTest {
    public static void main(String[] args) {
        try {
            DirectBridge.start(640, 480);
            throw new AssertionError("Expected the library load to fail");
        } catch (IllegalStateException error) {
            String message = error.getMessage();
            if (!message.contains("Cannot load Remix library '") || !message.contains(args[0])
                    || message.endsWith(": dynamic loader returned no details")) {
                throw new AssertionError("Missing library path or loader explanation: " + message);
            }
            System.out.println(message);
        } finally {
            DirectBridge.stop();
        }
    }
}
