/*
 * Decompiled with CFR 0.152.
 */
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;

public class ld
implements bf {
    private final File a;

    public ld(File file) {
        this.a = file;
    }

    public lm a(fd fd2, int n2, int n3) {
        DataInputStream dataInputStream = rj.c(this.a, n2, n3);
        if (dataInputStream == null) {
            return null;
        }
        nu nu2 = as.a(dataInputStream);
        if (!nu2.b("Level")) {
            System.out.println("Chunk file at " + n2 + "," + n3 + " is missing level data, skipping");
            return null;
        }
        if (!nu2.k("Level").b("Blocks")) {
            System.out.println("Chunk file at " + n2 + "," + n3 + " is missing block data, skipping");
            return null;
        }
        lm lm2 = to.a(fd2, nu2.k("Level"));
        if (!lm2.a(n2, n3)) {
            System.out.println("Chunk file at " + n2 + "," + n3 + " is in the wrong location; relocating. (Expected " + n2 + ", " + n3 + ", got " + lm2.j + ", " + lm2.k + ")");
            nu2.a("xPos", n2);
            nu2.a("zPos", n3);
            lm2 = to.a(fd2, nu2.k("Level"));
        }
        lm2.i();
        return lm2;
    }

    public void a(fd fd2, lm lm2) {
        fd2.r();
        try {
            DataOutputStream dataOutputStream = rj.d(this.a, lm2.j, lm2.k);
            nu nu2 = new nu();
            nu nu3 = new nu();
            nu2.a("Level", (ij)nu3);
            to.a(lm2, fd2, nu3);
            as.a(nu2, dataOutputStream);
            dataOutputStream.close();
            ei ei2 = fd2.x();
            ei2.b(ei2.g() + (long)rj.b(this.a, lm2.j, lm2.k));
        }
        catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    public void b(fd fd2, lm lm2) {
    }

    public void a() {
    }

    public void b() {
    }
}

