/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.lwjgl.opengl.GL11
 */
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Random;
import org.lwjgl.opengl.GL11;

public class fu
extends da {
    private static final Random a = new Random();
    private float i = 0.0f;
    private String j = "missingno";
    private ke l;

    public fu() {
        try {
            ArrayList<String> arrayList = new ArrayList<String>();
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(fu.class.getResourceAsStream("/title/splashes.txt"), Charset.forName("UTF-8")));
            String string = "";
            while ((string = bufferedReader.readLine()) != null) {
                if ((string = string.trim()).length() <= 0) continue;
                arrayList.add(string);
            }
            this.j = (String)arrayList.get(a.nextInt(arrayList.size()));
        }
        catch (Exception exception) {
            // empty catch block
        }
    }

    public void a() {
        this.i += 1.0f;
    }

    protected void a(char c2, int n2) {
    }

    public void b() {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        if (calendar.get(2) + 1 == 11 && calendar.get(5) == 9) {
            this.j = "Happy birthday, ez!";
        } else if (calendar.get(2) + 1 == 6 && calendar.get(5) == 1) {
            this.j = "Happy birthday, Notch!";
        } else if (calendar.get(2) + 1 == 12 && calendar.get(5) == 24) {
            this.j = "Merry X-mas!";
        } else if (calendar.get(2) + 1 == 1 && calendar.get(5) == 1) {
            this.j = "Happy new year!";
        }
        nh nh2 = nh.a();
        int n2 = this.d / 4 + 48;
        this.e.add(new ke(1, this.c / 2 - 100, n2, nh2.a("menu.singleplayer")));
        this.l = new ke(2, this.c / 2 - 100, n2 + 24, nh2.a("menu.multiplayer"));
        this.e.add(this.l);
        this.e.add(new ke(3, this.c / 2 - 100, n2 + 48, nh2.a("menu.mods")));
        if (this.b.n) {
            this.e.add(new ke(0, this.c / 2 - 100, n2 + 72, nh2.a("menu.options")));
        } else {
            this.e.add(new ke(0, this.c / 2 - 100, n2 + 72 + 12, 98, 20, nh2.a("menu.options")));
            this.e.add(new ke(4, this.c / 2 + 2, n2 + 72 + 12, 98, 20, nh2.a("menu.quit")));
        }
        if (this.b.k == null) {
            this.l.g = false;
        }
    }

    protected void a(ke ke2) {
        if (ke2.f == 0) {
            this.b.a(new co(this, this.b.z));
        }
        if (ke2.f == 1) {
            this.b.a(new rq(this));
        }
        if (ke2.f == 2) {
            this.b.a(new lq(this));
        }
        if (ke2.f == 3) {
            this.b.a(new ft(this));
        }
        if (ke2.f == 4) {
            this.b.f();
        }
    }

    public void a(int n2, int n3, float f2) {
        this.i();
        nw nw2 = nw.a;
        int n4 = 274;
        int n5 = this.c / 2 - n4 / 2;
        int n6 = 30;
        GL11.glBindTexture((int)3553, (int)this.b.p.b("/title/mclogo.png"));
        GL11.glColor4f((float)1.0f, (float)1.0f, (float)1.0f, (float)1.0f);
        this.b(n5 + 0, n6 + 0, 0, 0, 155, 44);
        this.b(n5 + 155, n6 + 0, 0, 45, 155, 44);
        nw2.b(0xFFFFFF);
        GL11.glPushMatrix();
        GL11.glTranslatef((float)(this.c / 2 + 90), (float)70.0f, (float)0.0f);
        GL11.glRotatef((float)-20.0f, (float)0.0f, (float)0.0f, (float)1.0f);
        float f3 = 1.8f - in.e(in.a((float)(System.currentTimeMillis() % 1000L) / 1000.0f * (float)Math.PI * 2.0f) * 0.1f);
        f3 = f3 * 100.0f / (float)(this.g.a(this.j) + 32);
        GL11.glScalef((float)f3, (float)f3, (float)f3);
        this.a(this.g, this.j, 0, -8, 0xFFFF00);
        GL11.glPopMatrix();
        this.b(this.g, "Minecraft Beta 1.7.3", 2, 2, 0x505050);
        String string = "Copyright Mojang AB. Do not distribute.";
        this.b(this.g, string, this.c - this.g.a(string) - 2, this.d - 10, 0xFFFFFF);
        super.a(n2, n3, f2);
    }
}

