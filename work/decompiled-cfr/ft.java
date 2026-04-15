/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.lwjgl.Sys
 */
import java.io.File;
import net.minecraft.client.Minecraft;
import org.lwjgl.Sys;

public class ft
extends da {
    protected da a;
    private int i = -1;
    private String j = "";
    private de l;

    public ft(da da2) {
        this.a = da2;
    }

    public void b() {
        nh nh2 = nh.a();
        this.e.add(new ab(5, this.c / 2 - 154, this.d - 48, nh2.a("texturePack.openFolder")));
        this.e.add(new ab(6, this.c / 2 + 4, this.d - 48, nh2.a("gui.done")));
        this.b.D.a();
        this.j = new File(Minecraft.b(), "texturepacks").getAbsolutePath();
        this.l = new de(this);
        this.l.a(this.e, 7, 8);
    }

    protected void a(ke ke2) {
        if (!ke2.g) {
            return;
        }
        if (ke2.f == 5) {
            Sys.openURL((String)("file://" + this.j));
        } else if (ke2.f == 6) {
            this.b.p.b();
            this.b.a(this.a);
        } else {
            this.l.a(ke2);
        }
    }

    protected void a(int n2, int n3, int n4) {
        super.a(n2, n3, n4);
    }

    protected void b(int n2, int n3, int n4) {
        super.b(n2, n3, n4);
    }

    public void a(int n2, int n3, float f2) {
        this.l.a(n2, n3, f2);
        if (this.i <= 0) {
            this.b.D.a();
            this.i += 20;
        }
        nh nh2 = nh.a();
        this.a(this.g, nh2.a("texturePack.title"), this.c / 2, 16, 0xFFFFFF);
        this.a(this.g, nh2.a("texturePack.folderInfo"), this.c / 2 - 77, this.d - 26, 0x808080);
        super.a(n2, n3, f2);
    }

    public void a() {
        super.a();
        --this.i;
    }

    static /* synthetic */ Minecraft a(ft ft2) {
        return ft2.b;
    }

    static /* synthetic */ Minecraft b(ft ft2) {
        return ft2.b;
    }

    static /* synthetic */ Minecraft c(ft ft2) {
        return ft2.b;
    }

    static /* synthetic */ Minecraft d(ft ft2) {
        return ft2.b;
    }

    static /* synthetic */ Minecraft e(ft ft2) {
        return ft2.b;
    }

    static /* synthetic */ Minecraft f(ft ft2) {
        return ft2.b;
    }

    static /* synthetic */ Minecraft g(ft ft2) {
        return ft2.b;
    }

    static /* synthetic */ Minecraft h(ft ft2) {
        return ft2.b;
    }

    static /* synthetic */ Minecraft i(ft ft2) {
        return ft2.b;
    }

    static /* synthetic */ sj j(ft ft2) {
        return ft2.g;
    }

    static /* synthetic */ sj k(ft ft2) {
        return ft2.g;
    }

    static /* synthetic */ sj l(ft ft2) {
        return ft2.g;
    }
}

