package org.lwjgl;

import java.awt.Desktop;
import java.net.URI;

public final class Sys {
    private Sys() {
    }

    public static String getVersion() {
        return "mcrtx-lwjgl3-compat";
    }

    public static boolean openURL(String url) {
        if (url == null || url.isEmpty() || !Desktop.isDesktopSupported()) {
            return false;
        }

        try {
            Desktop.getDesktop().browse(URI.create(url));
            return true;
        } catch (Exception exception) {
            return false;
        }
    }
}