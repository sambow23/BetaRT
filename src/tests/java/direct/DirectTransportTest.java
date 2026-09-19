import mcrtx.bridge.DirectBridge;
import mcrtx.bridge.DirectPacket;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;

/** Real JNI/API/Vulkan smoke test. Run separately from CPU-only Java tests. */
public final class DirectTransportTest {
    private static void require(boolean condition, String message) {
        if (!condition) throw new AssertionError(message + ": " + DirectBridge.error());
    }
    public static void main(String[] args) throws Exception {
        if (!Boolean.getBoolean("mcrtx.test.directGpu")) return;
        File screenshot = new File(args[0]).getAbsoluteFile();
        if (screenshot.exists() && !screenshot.delete()) throw new Exception("Cannot remove test screenshot");
        for (int cycle = 0; cycle < 2; ++cycle) {
            DirectBridge.start(320, 240);
            DirectPacket resources = new DirectPacket();
            int at = resources.command(2);
            resources.id(100); resources.id(0);
            resources.integer(0); resources.integer(1); resources.integer(1); resources.id(0);resources.number(0f);resources.number(0f);resources.number(0f);
            for(int i=0;i<4;++i)resources.text("");resources.end(at);
            at = resources.command(3);
            resources.id(101); resources.id(100); resources.integer(3); resources.integer(3);
            float[][] vertices = {{-1, -1, 0.5f}, {1, -1, 0.5f}, {0, 1, 0.5f}};
            for (float[] vertex : vertices) {
                for (float number : vertex) resources.number(number);
                resources.number(0f); resources.number(0f); resources.integer(0xffffffff);
            }
            resources.integer(0); resources.integer(1); resources.integer(2); resources.end(at);
            DirectPacket frame = new DirectPacket();
            at = frame.command(16); frame.integer(0);
            for (int matrix = 0; matrix < 2; ++matrix)
                for (int i = 0; i < 16; ++i) frame.number(i % 5 == 0 ? 1f : 0f);
            frame.id(1);frame.id(1);frame.integer(0);
            for(float v:new float[]{0,0,0,0,-1,0,0,0,0})frame.number(v);frame.end(at);
            at = frame.command(17); frame.id(101); frame.id(201); frame.integer(1); frame.integer(1);
            for (int row = 0; row < 3; ++row)
                for (int column = 0; column < 4; ++column) frame.number(row == column ? 1f : 0f);
            frame.integer(0); frame.integer(1); frame.integer(0xffff0000);
            frame.integer(7); frame.integer(0); frame.integer(0);
            frame.integer(6); frame.integer(7); frame.integer(0); frame.end(at);
            require(DirectBridge.submit(resources.buffer(), frame.buffer()), "Submit triangle");
            // Native must own copies; corrupting reused Java storage cannot change the frame.
            resources.clear(); frame.clear();
            long deadline = System.nanoTime() + 3_000_000_000L;
            while (DirectBridge.counts()[1] == 0 && System.nanoTime() < deadline) Thread.sleep(8);
            require(DirectBridge.counts()[1] == 1, "Fresh frame not presented");
            long before = DirectBridge.counts()[2];
            Thread.sleep(80);
            require(DirectBridge.counts()[2] == before, "Idle worker redrew an unchanged scene");
            require(DirectBridge.control(6, 0, 0, 0, "relative.png"), "Queue optional invalid request");
            Thread.sleep(80);
            require(DirectBridge.error().isEmpty(), "Optional control poisoned the renderer error state");
            if (cycle == 0) {
                require(DirectBridge.control(6, 0, 0, 0, screenshot.getPath()), "Queue screenshot");
                deadline = System.nanoTime() + 3_000_000_000L;
                while (!screenshot.isFile() && System.nanoTime() < deadline) Thread.sleep(8);
                BufferedImage image = ImageIO.read(screenshot);
                require((image.getRGB(160, 120) & 0xffffff) == 0xff0000, "Direct mesh/tint pixel");
                require(DirectBridge.counts()[1] == 1, "Redraw counted as a fresh frame");
            }
            DirectBridge.stop();
        }
        System.out.println("Direct Java/JNI/Vulkan transport passed");
    }
}
