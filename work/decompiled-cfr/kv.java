/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.lwjgl.input.Keyboard
 */
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintWriter;
import net.minecraft.client.Minecraft;
import org.lwjgl.input.Keyboard;

public class kv {
    private static final String[] J = new String[]{"options.renderDistance.far", "options.renderDistance.normal", "options.renderDistance.short", "options.renderDistance.tiny"};
    private static final String[] K = new String[]{"options.difficulty.peaceful", "options.difficulty.easy", "options.difficulty.normal", "options.difficulty.hard"};
    private static final String[] L = new String[]{"options.guiScale.auto", "options.guiScale.small", "options.guiScale.normal", "options.guiScale.large"};
    private static final String[] M = new String[]{"performance.max", "performance.balanced", "performance.powersaver"};
    public float a = 1.0f;
    public float b = 1.0f;
    public float c = 0.5f;
    public boolean d = false;
    public int e = 0;
    public boolean f = true;
    public boolean g = false;
    public boolean h = false;
    public int i = 1;
    public boolean j = true;
    public boolean k = true;
    public String l = "Default";
    public qb m = new qb("key.forward", 17);
    public qb n = new qb("key.left", 30);
    public qb o = new qb("key.back", 31);
    public qb p = new qb("key.right", 32);
    public qb q = new qb("key.jump", 57);
    public qb r = new qb("key.inventory", 18);
    public qb s = new qb("key.drop", 16);
    public qb t = new qb("key.chat", 20);
    public qb u = new qb("key.fog", 33);
    public qb v = new qb("key.sneak", 42);
    public qb[] w = new qb[]{this.m, this.n, this.o, this.p, this.q, this.v, this.s, this.r, this.t, this.u};
    protected Minecraft x;
    private File N;
    public int y = 2;
    public boolean z = false;
    public boolean A = false;
    public boolean B = false;
    public String C = "";
    public boolean D = false;
    public boolean E = false;
    public boolean F = false;
    public float G = 1.0f;
    public float H = 1.0f;
    public int I = 0;

    public kv(Minecraft minecraft, File file) {
        this.x = minecraft;
        this.N = new File(file, "options.txt");
        this.a();
    }

    public kv() {
    }

    public String a(int n2) {
        nh nh2 = nh.a();
        return nh2.a(this.w[n2].a);
    }

    public String b(int n2) {
        return Keyboard.getKeyName((int)this.w[n2].b);
    }

    public void a(int n2, int n3) {
        this.w[n2].b = n3;
        this.b();
    }

    public void a(ht ht2, float f2) {
        if (ht2 == ht.a) {
            this.a = f2;
            this.x.B.a();
        }
        if (ht2 == ht.b) {
            this.b = f2;
            this.x.B.a();
        }
        if (ht2 == ht.d) {
            this.c = f2;
        }
    }

    public void a(ht ht2, int n2) {
        if (ht2 == ht.c) {
            boolean bl2 = this.d = !this.d;
        }
        if (ht2 == ht.e) {
            this.e = this.e + n2 & 3;
        }
        if (ht2 == ht.m) {
            this.I = this.I + n2 & 3;
        }
        if (ht2 == ht.f) {
            boolean bl3 = this.f = !this.f;
        }
        if (ht2 == ht.h) {
            this.h = !this.h;
            this.x.g.a();
        }
        if (ht2 == ht.g) {
            this.g = !this.g;
            this.x.p.b();
        }
        if (ht2 == ht.i) {
            this.i = (this.i + n2 + 3) % 3;
        }
        if (ht2 == ht.j) {
            this.y = this.y + n2 & 3;
        }
        if (ht2 == ht.k) {
            this.j = !this.j;
            this.x.g.a();
        }
        if (ht2 == ht.l) {
            this.k = !this.k;
            this.x.g.a();
        }
        this.b();
    }

    public float a(ht ht2) {
        if (ht2 == ht.a) {
            return this.a;
        }
        if (ht2 == ht.b) {
            return this.b;
        }
        if (ht2 == ht.d) {
            return this.c;
        }
        return 0.0f;
    }

    public boolean b(ht ht2) {
        switch (ht2) {
            case c: {
                return this.d;
            }
            case f: {
                return this.f;
            }
            case g: {
                return this.g;
            }
            case h: {
                return this.h;
            }
            case l: {
                return this.k;
            }
        }
        return false;
    }

    public String c(ht ht2) {
        nh nh2 = nh.a();
        String string = nh2.a(ht2.d()) + ": ";
        if (ht2.a()) {
            float f2 = this.a(ht2);
            if (ht2 == ht.d) {
                if (f2 == 0.0f) {
                    return string + nh2.a("options.sensitivity.min");
                }
                if (f2 == 1.0f) {
                    return string + nh2.a("options.sensitivity.max");
                }
                return string + (int)(f2 * 200.0f) + "%";
            }
            if (f2 == 0.0f) {
                return string + nh2.a("options.off");
            }
            return string + (int)(f2 * 100.0f) + "%";
        }
        if (ht2.b()) {
            boolean bl2 = this.b(ht2);
            if (bl2) {
                return string + nh2.a("options.on");
            }
            return string + nh2.a("options.off");
        }
        if (ht2 == ht.e) {
            return string + nh2.a(J[this.e]);
        }
        if (ht2 == ht.j) {
            return string + nh2.a(K[this.y]);
        }
        if (ht2 == ht.m) {
            return string + nh2.a(L[this.I]);
        }
        if (ht2 == ht.i) {
            return string + do.a(M[this.i]);
        }
        if (ht2 == ht.k) {
            if (this.j) {
                return string + nh2.a("options.graphics.fancy");
            }
            return string + nh2.a("options.graphics.fast");
        }
        return string;
    }

    public void a() {
        try {
            if (!this.N.exists()) {
                return;
            }
            BufferedReader bufferedReader = new BufferedReader(new FileReader(this.N));
            String string = "";
            while ((string = bufferedReader.readLine()) != null) {
                try {
                    String[] stringArray = string.split(":");
                    if (stringArray[0].equals("music")) {
                        this.a = this.a(stringArray[1]);
                    }
                    if (stringArray[0].equals("sound")) {
                        this.b = this.a(stringArray[1]);
                    }
                    if (stringArray[0].equals("mouseSensitivity")) {
                        this.c = this.a(stringArray[1]);
                    }
                    if (stringArray[0].equals("invertYMouse")) {
                        this.d = stringArray[1].equals("true");
                    }
                    if (stringArray[0].equals("viewDistance")) {
                        this.e = Integer.parseInt(stringArray[1]);
                    }
                    if (stringArray[0].equals("guiScale")) {
                        this.I = Integer.parseInt(stringArray[1]);
                    }
                    if (stringArray[0].equals("bobView")) {
                        this.f = stringArray[1].equals("true");
                    }
                    if (stringArray[0].equals("anaglyph3d")) {
                        this.g = stringArray[1].equals("true");
                    }
                    if (stringArray[0].equals("advancedOpengl")) {
                        this.h = stringArray[1].equals("true");
                    }
                    if (stringArray[0].equals("fpsLimit")) {
                        this.i = Integer.parseInt(stringArray[1]);
                    }
                    if (stringArray[0].equals("difficulty")) {
                        this.y = Integer.parseInt(stringArray[1]);
                    }
                    if (stringArray[0].equals("fancyGraphics")) {
                        this.j = stringArray[1].equals("true");
                    }
                    if (stringArray[0].equals("ao")) {
                        this.k = stringArray[1].equals("true");
                    }
                    if (stringArray[0].equals("skin")) {
                        this.l = stringArray[1];
                    }
                    if (stringArray[0].equals("lastServer") && stringArray.length >= 2) {
                        this.C = stringArray[1];
                    }
                    for (int i2 = 0; i2 < this.w.length; ++i2) {
                        if (!stringArray[0].equals("key_" + this.w[i2].a)) continue;
                        this.w[i2].b = Integer.parseInt(stringArray[1]);
                    }
                }
                catch (Exception exception) {
                    System.out.println("Skipping bad option: " + string);
                }
            }
            bufferedReader.close();
        }
        catch (Exception exception) {
            System.out.println("Failed to load options");
            exception.printStackTrace();
        }
    }

    private float a(String string) {
        if (string.equals("true")) {
            return 1.0f;
        }
        if (string.equals("false")) {
            return 0.0f;
        }
        return Float.parseFloat(string);
    }

    public void b() {
        try {
            PrintWriter printWriter = new PrintWriter(new FileWriter(this.N));
            printWriter.println("music:" + this.a);
            printWriter.println("sound:" + this.b);
            printWriter.println("invertYMouse:" + this.d);
            printWriter.println("mouseSensitivity:" + this.c);
            printWriter.println("viewDistance:" + this.e);
            printWriter.println("guiScale:" + this.I);
            printWriter.println("bobView:" + this.f);
            printWriter.println("anaglyph3d:" + this.g);
            printWriter.println("advancedOpengl:" + this.h);
            printWriter.println("fpsLimit:" + this.i);
            printWriter.println("difficulty:" + this.y);
            printWriter.println("fancyGraphics:" + this.j);
            printWriter.println("ao:" + this.k);
            printWriter.println("skin:" + this.l);
            printWriter.println("lastServer:" + this.C);
            for (int i2 = 0; i2 < this.w.length; ++i2) {
                printWriter.println("key_" + this.w[i2].a + ":" + this.w[i2].b);
            }
            printWriter.close();
        }
        catch (Exception exception) {
            System.out.println("Failed to save options");
            exception.printStackTrace();
        }
    }
}

