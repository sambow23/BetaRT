import mcrtx.bridge.DirectBridge;
import mcrtx.bridge.DirectPacket;
import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import javax.imageio.ImageIO;

/** Streamed meshes have no authored normals. A vertical face must not acquire
 * an upward normal on one side of the camera horizon. Requires path tracing. */
public final class DirectNormalsTest {
    private static void require(boolean value, String message) {
        if (!value) throw new AssertionError(message + ": " + DirectBridge.error());
    }
    private static void write(File file, String value) throws Exception {
        Files.write(file.toPath(), value.getBytes(StandardCharsets.UTF_8));
    }
    private static double linear(int pixel) {
        double value = (pixel & 255) / 255.0;
        return value <= .04045 ? value / 12.92 : Math.pow((value + .055) / 1.055, 2.4);
    }
    private static DirectPacket mesh() {
        DirectPacket resources = new DirectPacket();
        int at = resources.command(2);
        resources.id(100); resources.id(0);
        resources.integer(0); resources.integer(1); resources.integer(1);
        resources.id(0);
        for (int i = 0; i < 3; ++i) resources.number(0f);
        for (int i = 0; i < 4; ++i) resources.text("");
        resources.end(at);
        at = resources.command(3);
        resources.id(101); resources.id(100); resources.integer(4); resources.integer(6);
        for (float[] vertex : new float[][]{{-3,-3,0}, {3,-3,0}, {3,3,0}, {-3,3,0}}) {
            for (float value : vertex) resources.number(value);
            resources.number(0f); resources.number(0f); resources.integer(0xffffffff);
        }
        for (int index : new int[]{0,1,2,0,2,3}) resources.integer(index);
        resources.end(at);
        return resources;
    }
    private static DirectPacket frame(boolean transformed, long epoch) {
        DirectPacket frame = new DirectPacket();
        int at = frame.command(16); frame.integer(0);
        for (int i = 0; i < 16; ++i) frame.number(i % 5 == 0 ? 1f : 0f);
        // Perspective, forward +Z, Vulkan depth 0..1. Unlike an orthographic
        // camera, wo.y changes sign across the horizontal center of the image.
        float[] projection = {1,0,0,0, 0,1,0,0, 0,0,100f/99.9f,1, 0,0,-10f/99.9f,0};
        for (float value : projection) frame.number(value);
        frame.id(1); frame.id(epoch); frame.integer(1);
        for (float value : new float[]{0,0,0, 0,0,1, (float)Math.PI,(float)Math.PI,(float)Math.PI})
            frame.number(value);
        frame.end(at);
        at = frame.command(17);
        frame.id(101); frame.id(201); frame.integer(1); frame.integer(1);
        float c = (float)Math.cos(.3), s = (float)Math.sin(.3);
        // Include a mirrored, nonuniformly scaled, tilted object as well as
        // the ordinary vertical face. Neither may change normal per pixel.
        float[] model = transformed
            ? new float[]{-1.2f,0,0,.15f, 0,.9f*c,-s,0, 0,.9f*s,c,4}
            : new float[]{1,0,0,0, 0,1,0,0, 0,0,1,4};
        for (float value : model) frame.number(value);
        frame.integer(0); frame.integer(1); frame.integer(0xffffffff);
        frame.integer(7); frame.integer(0); frame.integer(0);
        frame.integer(6); frame.integer(7); frame.integer(0);
        frame.end(at);
        return frame;
    }
    public static void main(String[] args) throws Exception {
        if (!Boolean.getBoolean("mcrtx.test.directGpu")) return;
        File output = new File(args[0]).getAbsoluteFile();
        require(output.mkdirs(), "Use a fresh normals output directory");
        String configuration = System.getenv("REMIX_CONFIG_DIR");
        require(configuration != null && !configuration.isEmpty(), "Set an isolated REMIX_CONFIG_DIR");
        File config = new File(configuration);
        require(!config.exists(), "Use a fresh test configuration directory");
        File profiles = new File(config, "profiles");
        require(profiles.mkdirs(), "Create test profiles");
        write(new File(config, "state.conf"), "lastProfile = Normals\n");
        for (String primary : new String[]{"raster", "ray"}) {
            for (int mode = 0; mode < 3; ++mode) {
                String name = primary + (mode == 0 ? "-flat" : mode == 1 ? "-transformed" : "-normals");
                write(new File(profiles, "Normals.conf"), "profile.version = 1\nrtx.native.backend = pathtrace\n"
                    + "rtx.native.vsync = 0\nrtx.native.pathtrace.upscaler = off\n"
                    + "rtx.native.pathtrace.primaryVisibility = " + primary + "\n"
                    + "rtx.native.pathtrace.debugView = " + (mode == 2 ? 2 : 0) + "\n"
                    + "rtx.native.pathtrace.denoiser = off\nrtx.native.pathtrace.taa = 0\n"
                    + "rtx.native.pathtrace.sigma = 0\nrtx.native.pathtrace.sharc = 0\n"
                    + "rtx.native.pathtrace.sky = 0\nrtx.native.pathtrace.localLights = 0\n"
                    + "rtx.native.pathtrace.prism = 0\nrtx.native.pathtrace.gamma22 = 0\n"
                    + "rtx.native.pathtrace.sunAngularDiameter = 0\n");
                try {
                    DirectBridge.start(256, 256);
                    DirectPacket resources = mesh();
                    DirectPacket frame = frame(mode == 1, mode + 1);
                    require(DirectBridge.submit(resources.buffer(), frame.buffer()), "Submit vertical face");
                    long deadline = System.nanoTime() + 60_000_000_000L;
                    while (DirectBridge.counts()[1] == 0 && System.nanoTime() < deadline) {
                        require(DirectBridge.error().isEmpty(), "Render failed");
                        Thread.sleep(10);
                    }
                    require(DirectBridge.counts()[1] > 0, "Frame not presented");
                    File screenshot = new File(output, name + ".png");
                    require(DirectBridge.control(6, 0, 0, 0, screenshot.getPath()), "Queue screenshot");
                    deadline = System.nanoTime() + 10_000_000_000L;
                    BufferedImage image = null;
                    while (image == null && System.nanoTime() < deadline) {
                        Thread.sleep(10);
                        if (screenshot.isFile()) {
                            try { image = ImageIO.read(screenshot); } catch (java.io.IOException pending) { }
                        }
                    }
                    require(image != null, "Screenshot missing");
                    double top = 0, bottom = 0;
                    int samples = 0;
                    for (int y = 64; y < 112; ++y) for (int x = 96; x < 160; ++x) {
                        top += linear(image.getRGB(x, y));
                        bottom += linear(image.getRGB(x, 255-y));
                        ++samples;
                    }
                    top /= samples; bottom /= samples;
                    System.out.printf("%s top=%.5f bottom=%.5f%n", name, top, bottom);
                    // The tilted rough diffuse face can vary smoothly with
                    // viewing angle, but cannot jump to black at the horizon.
                    require(Math.abs(top-bottom) < .03, "Lighting/normal changes at camera horizon: " + name);
                    if (mode != 2) require(Math.min(top,bottom) > .9, "Vertical face lost direct illumination");
                    else require(Math.max(top,bottom) < .01, "Vertical normal does not point toward camera");
                } finally { DirectBridge.stop(); }
            }
        }
        System.out.println("Streamed dynamic normals: raster/ray, horizon continuity and transforms passed");
    }
}
