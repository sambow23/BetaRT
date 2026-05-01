import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.Panel;
import java.awt.TextArea;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import mcrtx.lwjglshim.OpenGlCompat;
import org.lwjgl.Sys;

public class cb extends Panel {
    public cb(mh crashReport) {
        this.setBackground(new Color(3028036));
        this.setLayout(new BorderLayout());
        StringWriter stackTraceWriter = new StringWriter();
        crashReport.b.printStackTrace(new PrintWriter(stackTraceWriter));
        String stackTrace = stackTraceWriter.toString();
        String glVendor = "";
        String reportBody = "";

        try {
            reportBody = reportBody + "Generated " + new SimpleDateFormat().format(new Date()) + "\n";
            reportBody = reportBody + "\n";
            reportBody = reportBody + "Minecraft: Minecraft Beta 1.7.3\n";
            reportBody = reportBody + "OS: " + System.getProperty("os.name") + " (" + System.getProperty("os.arch") + ") version " + System.getProperty("os.version") + "\n";
            reportBody = reportBody + "Java: " + System.getProperty("java.version") + ", " + System.getProperty("java.vendor") + "\n";
            reportBody = reportBody + "VM: " + System.getProperty("java.vm.name") + " (" + System.getProperty("java.vm.info") + "), " + System.getProperty("java.vm.vendor") + "\n";
            reportBody = reportBody + "LWJGL: " + Sys.getVersion() + "\n";

            String vendor = OpenGlCompat.tryGetString(7936);
            String renderer = OpenGlCompat.tryGetString(7937);
            String version = OpenGlCompat.tryGetString(7938);
            glVendor = vendor == null ? "" : vendor;
            if (vendor != null && renderer != null && version != null) {
                reportBody = reportBody + "OpenGL: " + renderer + " version " + version + ", " + vendor + "\n";
            } else {
                reportBody = reportBody + "OpenGL: [unavailable]\n";
            }
        } catch (Throwable throwable) {
            reportBody = reportBody + "[failed to get system properties (" + throwable + ")]\n";
        }

        reportBody = reportBody + "\n";
        reportBody = reportBody + stackTrace;
        String header = "";
        header = header + "\n";
        header = header + "\n";
        if (stackTrace.contains("Pixel format not accelerated")) {
            header = header + "      Bad video card drivers!      \n";
            header = header + "      -----------------------      \n";
            header = header + "\n";
            header = header + "Minecraft was unable to start because it failed to find an accelerated OpenGL mode.\n";
            header = header + "This can usually be fixed by updating the video card drivers.\n";
            if (glVendor.toLowerCase().contains("nvidia")) {
                header = header + "\n";
                header = header + "You might be able to find drivers for your video card here:\n";
                header = header + "  http://www.nvidia.com/\n";
            } else if (glVendor.toLowerCase().contains("ati")) {
                header = header + "\n";
                header = header + "You might be able to find drivers for your video card here:\n";
                header = header + "  http://www.amd.com/\n";
            }
        } else {
            header = header + "      Minecraft has crashed!      \n";
            header = header + "      ----------------------      \n";
            header = header + "\n";
            header = header + "Minecraft has stopped running because it encountered a problem.\n";
            header = header + "\n";
            header = header + "If you wish to report this, please copy this entire text and email it to support@mojang.com.\n";
            header = header + "Please include a description of what you did when the error occured.\n";
        }

        header = header + "\n";
        header = header + "\n";
        header = header + "\n";
        header = header + "--- BEGIN ERROR REPORT " + Integer.toHexString(header.hashCode()) + " --------\n";
        header = header + reportBody;
        header = header + "--- END ERROR REPORT " + Integer.toHexString(header.hashCode()) + " ----------\n";
        header = header + "\n";
        header = header + "\n";
        TextArea reportArea = new TextArea(header, 0, 0, 1);
        reportArea.setFont(new Font("Monospaced", 0, 12));
        this.add(new fy(), "North");
        this.add(new wv(80), "East");
        this.add(new wv(80), "West");
        this.add(new wv(100), "South");
        this.add(reportArea, "Center");
    }
}