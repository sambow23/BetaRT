/*
 * Decompiled with CFR 0.152.
 */
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;

public final class hx {
    public qe a(Reader reader) {
        lt lt2 = new lt();
        new an().a(reader, (wg)lt2);
        return lt2.a();
    }

    public qe a(String string) {
        qe qe2;
        try {
            qe2 = this.a(new StringReader(string));
        }
        catch (IOException iOException) {
            throw new RuntimeException("Coding failure in Argo:  StringWriter gave an IOException", iOException);
        }
        return qe2;
    }
}

