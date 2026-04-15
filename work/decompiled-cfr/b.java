/*
 * Decompiled with CFR 0.152.
 */
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

public class b {
    public static b a = new b();
    private Map b = new HashMap();

    private b() {
        try {
            String string;
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(b.class.getResourceAsStream("/achievement/map.txt")));
            while ((string = bufferedReader.readLine()) != null) {
                String[] stringArray = string.split(",");
                int n2 = Integer.parseInt(stringArray[0]);
                this.b.put(n2, stringArray[1]);
            }
            bufferedReader.close();
        }
        catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    public static String a(int n2) {
        return (String)b.a.b.get(n2);
    }
}

