/*
 * Decompiled with CFR 0.152.
 */
import java.awt.BorderLayout;
import java.awt.Canvas;
import java.awt.Component;
import net.minecraft.client.Minecraft;
import net.minecraft.client.MinecraftApplet;

public class q
extends Minecraft {
    final /* synthetic */ MinecraftApplet a;

    public q(MinecraftApplet minecraftApplet, Component component, Canvas canvas, MinecraftApplet minecraftApplet2, int n2, int n3, boolean bl2) {
        this.a = minecraftApplet;
        super(component, canvas, minecraftApplet2, n2, n3, bl2);
    }

    public void a(mh mh2) {
        this.a.removeAll();
        this.a.setLayout(new BorderLayout());
        this.a.add((Component)new cb(mh2), "Center");
        this.a.validate();
    }
}

