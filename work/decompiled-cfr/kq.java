/*
 * Decompiled with CFR 0.152.
 */
import java.awt.Canvas;
import java.awt.Component;
import java.awt.Frame;
import net.minecraft.client.Minecraft;
import net.minecraft.client.MinecraftApplet;

public final class kq
extends Minecraft {
    final /* synthetic */ Frame a;

    public kq(Component component, Canvas canvas, MinecraftApplet minecraftApplet, int n2, int n3, boolean bl2, Frame frame) {
        this.a = frame;
        super(component, canvas, minecraftApplet, n2, n3, bl2);
    }

    public void a(mh mh2) {
        this.a.removeAll();
        this.a.add((Component)new cb(mh2), "Center");
        this.a.validate();
    }
}

