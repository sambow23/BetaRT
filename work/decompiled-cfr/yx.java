/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.lwjgl.opengl.GL11
 */
import org.lwjgl.opengl.GL11;

public class yx
extends gv {
    public yx() {
        super(new sc(), 1.0f);
        this.a(new sc());
    }

    protected float a(cn cn2) {
        return 180.0f;
    }

    protected boolean a(cn cn2, int n2, float f2) {
        if (n2 != 0) {
            return false;
        }
        if (n2 != 0) {
            return false;
        }
        this.a("/mob/spider_eyes.png");
        float f3 = (1.0f - cn2.a(1.0f)) * 0.5f;
        GL11.glEnable((int)3042);
        GL11.glDisable((int)3008);
        GL11.glBlendFunc((int)770, (int)771);
        GL11.glColor4f((float)1.0f, (float)1.0f, (float)1.0f, (float)f3);
        return true;
    }
}

