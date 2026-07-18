import mcrtx.bridge.RemixLifecycleBridge;
import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

final class McrtxHookScreenshotHelper {
    private static final DateFormat SCREENSHOT_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd_HH.mm.ss");

    private McrtxHookScreenshotHelper() {
    }

    static String requestPresentedScreenshot(File minecraftDir, int width, int height) {
        File destination = nextVanillaScreenshotFile(minecraftDir);
        if (RemixLifecycleBridge.requestPresentedScreenshot(destination.getAbsolutePath())) {
            return "Saved screenshot as " + destination.getName();
        }
        return hj.a(minecraftDir, width, height);
    }

    private static File nextVanillaScreenshotFile(File minecraftDir) {
        File screenshotDir = new File(minecraftDir, "screenshots");
        screenshotDir.mkdir();
        String baseName = SCREENSHOT_DATE_FORMAT.format(new Date());
        int index = 1;
        File candidate;
        do {
            candidate = new File(screenshotDir, baseName + (index == 1 ? "" : "_" + index) + ".png");
            ++index;
        } while (candidate.exists());
        return candidate;
    }
}
