/*
 * Decompiled with CFR 0.152.
 */
import java.io.File;
import java.util.regex.Matcher;

class dz
implements Comparable {
    private final File a;
    private final int b;
    private final int c;

    public dz(File file) {
        this.a = file;
        Matcher matcher = rs.a.matcher(file.getName());
        if (matcher.matches()) {
            this.b = Integer.parseInt(matcher.group(1), 36);
            this.c = Integer.parseInt(matcher.group(2), 36);
        } else {
            this.b = 0;
            this.c = 0;
        }
    }

    public int a(dz dz2) {
        int n2 = this.b >> 5;
        int n3 = dz2.b >> 5;
        if (n2 == n3) {
            int n4 = this.c >> 5;
            int n5 = dz2.c >> 5;
            return n4 - n5;
        }
        return n2 - n3;
    }

    public File a() {
        return this.a;
    }

    public int b() {
        return this.b;
    }

    public int c() {
        return this.c;
    }
}

