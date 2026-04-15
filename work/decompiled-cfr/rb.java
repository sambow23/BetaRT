/*
 * Decompiled with CFR 0.152.
 */
import java.util.Random;

public class rb
extends kd {
    public rb() {
        this.t.add(new bj(gi.class, 2));
    }

    public pg a(Random random) {
        if (random.nextInt(5) == 0) {
            return new k();
        }
        if (random.nextInt(3) == 0) {
            return new ih();
        }
        return new yh();
    }
}

