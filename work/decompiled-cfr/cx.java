/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.lwjgl.opengl.GLContext
 */
import org.lwjgl.opengl.GLContext;

public class cx {
    private static boolean a = true;

    public boolean a() {
        return a && GLContext.getCapabilities().GL_ARB_occlusion_query;
    }
}

