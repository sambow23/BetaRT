/*
 * Decompiled with CFR 0.152.
 */
import java.io.File;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class hr {
    private Random c = new Random();
    private Map d = new HashMap();
    private List e = new ArrayList();
    public int a = 0;
    public boolean b = true;

    public bh a(String string, File file) {
        try {
            String string2 = string;
            string = string.substring(0, string.indexOf("."));
            if (this.b) {
                while (Character.isDigit(string.charAt(string.length() - 1))) {
                    string = string.substring(0, string.length() - 1);
                }
            }
            if (!this.d.containsKey(string = string.replaceAll("/", "."))) {
                this.d.put(string, new ArrayList());
            }
            bh bh2 = new bh(string2, file.toURI().toURL());
            ((List)this.d.get(string)).add(bh2);
            this.e.add(bh2);
            ++this.a;
            return bh2;
        }
        catch (MalformedURLException malformedURLException) {
            malformedURLException.printStackTrace();
            throw new RuntimeException(malformedURLException);
        }
    }

    public bh a(String string) {
        List list = (List)this.d.get(string);
        if (list == null) {
            return null;
        }
        return (bh)list.get(this.c.nextInt(list.size()));
    }

    public bh a() {
        if (this.e.size() == 0) {
            return null;
        }
        return (bh)this.e.get(this.c.nextInt(this.e.size()));
    }
}

