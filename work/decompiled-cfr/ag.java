/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.lwjgl.opengl.GL11
 */
import java.util.HashMap;
import java.util.Map;
import org.lwjgl.opengl.GL11;

public class ag
extends je {
    private Map b = new HashMap();

    public void a(cy cy2, double d2, double d3, double d4, float f2) {
        GL11.glPushMatrix();
        GL11.glTranslatef((float)((float)d2 + 0.5f), (float)((float)d3), (float)((float)d4 + 0.5f));
        sn sn2 = (sn)this.b.get(cy2.a());
        if (sn2 == null) {
            sn2 = jc.a(cy2.a(), null);
            this.b.put(cy2.a(), sn2);
        }
        if (sn2 != null) {
            sn2.a(cy2.d);
            float f3 = 0.4375f;
            GL11.glTranslatef((float)0.0f, (float)0.4f, (float)0.0f);
            GL11.glRotatef((float)((float)(cy2.c + (cy2.b - cy2.c) * (double)f2) * 10.0f), (float)0.0f, (float)1.0f, (float)0.0f);
            GL11.glRotatef((float)-30.0f, (float)1.0f, (float)0.0f, (float)0.0f);
            GL11.glTranslatef((float)0.0f, (float)-0.4f, (float)0.0f);
            GL11.glScalef((float)f3, (float)f3, (float)f3);
            sn2.c(d2, d3, d4, 0.0f, 0.0f);
            th.a.a(sn2, 0.0, 0.0, 0.0, 0.0f, f2);
        }
        GL11.glPopMatrix();
    }
}

