/*
 * Decompiled with CFR 0.152.
 */
import java.net.HttpURLConnection;
import java.net.URL;
import net.minecraft.client.Minecraft;

public class kp
extends Thread {
    final /* synthetic */ Minecraft a;

    public kp(Minecraft minecraft) {
        this.a = minecraft;
    }

    public void run() {
        try {
            HttpURLConnection httpURLConnection = (HttpURLConnection)new URL("https://login.minecraft.net/session?name=" + this.a.k.b + "&session=" + this.a.k.c).openConnection();
            httpURLConnection.connect();
            if (httpURLConnection.getResponseCode() == 400) {
                Minecraft.H = System.currentTimeMillis();
            }
            httpURLConnection.disconnect();
        }
        catch (Exception exception) {
            exception.printStackTrace();
        }
    }
}

