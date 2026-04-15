/*
 * Decompiled with CFR 0.152.
 */
import java.net.ConnectException;
import java.net.UnknownHostException;
import net.minecraft.client.Minecraft;

class wz
extends Thread {
    final /* synthetic */ Minecraft a;
    final /* synthetic */ String b;
    final /* synthetic */ int c;
    final /* synthetic */ vx d;

    wz(vx vx2, Minecraft minecraft, String string, int n2) {
        this.d = vx2;
        this.a = minecraft;
        this.b = string;
        this.c = n2;
    }

    public void run() {
        try {
            vx.a(this.d, new nb(this.a, this.b, this.c));
            if (vx.a(this.d)) {
                return;
            }
            vx.b(this.d).b(new mp(this.a.k.b));
        }
        catch (UnknownHostException unknownHostException) {
            if (vx.a(this.d)) {
                return;
            }
            this.a.a(new ex("connect.failed", "disconnect.genericReason", "Unknown host '" + this.b + "'"));
        }
        catch (ConnectException connectException) {
            if (vx.a(this.d)) {
                return;
            }
            this.a.a(new ex("connect.failed", "disconnect.genericReason", connectException.getMessage()));
        }
        catch (Exception exception) {
            if (vx.a(this.d)) {
                return;
            }
            exception.printStackTrace();
            this.a.a(new ex("connect.failed", "disconnect.genericReason", exception.toString()));
        }
    }
}

