/*
 * Decompiled with CFR 0.152.
 */
import java.io.BufferedReader;
import java.io.InputStreamReader;

public class fp {
    public static final String a = fp.a();
    public static final char[] b = new char[]{'/', '\n', '\r', '\t', '\u0000', '\f', '`', '?', '*', '\\', '<', '>', '|', '\"', ':'};

    private static String a() {
        String string = "";
        try {
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(fp.class.getResourceAsStream("/font.txt"), "UTF-8"));
            String string2 = "";
            while ((string2 = bufferedReader.readLine()) != null) {
                if (string2.startsWith("#")) continue;
                string = string + string2;
            }
            bufferedReader.close();
        }
        catch (Exception exception) {
            // empty catch block
        }
        return string;
    }
}

