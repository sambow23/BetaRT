/*
 * Decompiled with CFR 0.152.
 */
import java.io.IOException;
import java.util.Properties;

public class nh {
    private static nh a = new nh();
    private Properties b = new Properties();

    private nh() {
        try {
            this.b.load(nh.class.getResourceAsStream("/lang/en_US.lang"));
            this.b.load(nh.class.getResourceAsStream("/lang/stats_US.lang"));
        }
        catch (IOException iOException) {
            iOException.printStackTrace();
        }
    }

    public static nh a() {
        return a;
    }

    public String a(String string) {
        return this.b.getProperty(string, string);
    }

    public String a(String string, Object ... objectArray) {
        String string2 = this.b.getProperty(string, string);
        return String.format(string2, objectArray);
    }

    public String b(String string) {
        return this.b.getProperty(string + ".name", "");
    }
}

