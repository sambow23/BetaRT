/*
 * Decompiled with CFR 0.152.
 */
import java.io.File;
import java.util.List;

public class mx
extends fm {
    public mx(File file, String string, boolean bl2) {
        super(file, string, bl2);
    }

    public bf a(xa xa2) {
        File file = this.a();
        if (xa2 instanceof wd) {
            File file2 = new File(file, "DIM-1");
            file2.mkdirs();
            return new ld(file2);
        }
        return new ld(file);
    }

    public void a(ei ei2, List list) {
        ei2.d(19132);
        super.a(ei2, list);
    }
}

