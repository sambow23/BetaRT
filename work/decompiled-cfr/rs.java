/*
 * Decompiled with CFR 0.152.
 */
import java.io.File;
import java.io.FilenameFilter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class rs
implements FilenameFilter {
    public static final Pattern a = Pattern.compile("c\\.(-?[0-9a-z]+)\\.(-?[0-9a-z]+)\\.dat");

    private rs() {
    }

    public boolean accept(File file, String string) {
        Matcher matcher = a.matcher(string);
        return matcher.matches();
    }

    /* synthetic */ rs(ic ic2) {
        this();
    }
}

