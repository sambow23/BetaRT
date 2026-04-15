/*
 * Decompiled with CFR 0.152.
 */
package paulscode.sound;

import java.net.URL;
import paulscode.sound.SoundSystemConfig;
import paulscode.sound.SoundSystemLogger;

public class FilenameURL {
    private SoundSystemLogger logger = SoundSystemConfig.getLogger();
    private String filename = null;
    private URL url = null;

    public FilenameURL(URL uRL, String string) {
        this.filename = string;
        this.url = uRL;
    }

    public FilenameURL(String string) {
        this.filename = string;
        this.url = null;
    }

    public String getFilename() {
        return this.filename;
    }

    public URL getURL() {
        if (this.url == null) {
            if (this.filename.matches("^[hH][tT][tT][pP]://.*")) {
                try {
                    this.url = new URL(this.filename);
                }
                catch (Exception exception) {
                    this.errorMessage("Unable to access online URL in method 'getURL'");
                    this.printStackTrace(exception);
                    return null;
                }
            } else {
                this.url = this.getClass().getClassLoader().getResource(SoundSystemConfig.getSoundFilesPackage() + this.filename);
            }
        }
        return this.url;
    }

    private void errorMessage(String string) {
        this.logger.errorMessage("MidiChannel", string, 0);
    }

    private void printStackTrace(Exception exception) {
        this.logger.printStackTrace(exception, 1);
    }
}

