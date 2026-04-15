/*
 * Decompiled with CFR 0.152.
 */
import java.io.File;
import java.io.FileFilter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class cm
implements FileFilter {
    public static final Pattern a = Pattern.compile("[0-9a-z]|([0-9a-z][0-9a-z])");

    private cm() {
    }

    public boolean accept(File file) {
        if (file.isDirectory()) {
            Matcher matcher = a.matcher(file.getName());
            return matcher.matches();
        }
        return false;
    }

    /* synthetic */ cm(ic ic2) {
        this();
    }
}

