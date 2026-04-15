/*
 * Decompiled with CFR 0.152.
 */
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.TreeSet;

public final class el
implements ml {
    public String a(qe qe2) {
        StringWriter stringWriter = new StringWriter();
        try {
            this.a(qe2, (Writer)stringWriter);
        }
        catch (IOException iOException) {
            throw new RuntimeException("Coding failure in Argo:  StringWriter gave an IOException", iOException);
        }
        return stringWriter.toString();
    }

    public void a(qe qe2, Writer writer) {
        this.a((gu)qe2, writer);
    }

    private void a(gu gu2, Writer writer) {
        boolean bl2 = true;
        switch (gu2.a()) {
            case b: {
                writer.append('[');
                for (gu gu3 : gu2.d()) {
                    if (!bl2) {
                        writer.append(',');
                    }
                    bl2 = false;
                    this.a(gu3, writer);
                }
                writer.append(']');
                break;
            }
            case a: {
                writer.append('{');
                for (qa qa2 : new TreeSet(gu2.c().keySet())) {
                    if (!bl2) {
                        writer.append(',');
                    }
                    bl2 = false;
                    this.a(qa2, writer);
                    writer.append(':');
                    this.a((gu)gu2.c().get(qa2), writer);
                }
                writer.append('}');
                break;
            }
            case c: {
                writer.append('\"').append(new yt(gu2.b()).toString()).append('\"');
                break;
            }
            case d: {
                writer.append(gu2.b());
                break;
            }
            case f: {
                writer.append("false");
                break;
            }
            case e: {
                writer.append("true");
                break;
            }
            case g: {
                writer.append("null");
                break;
            }
            default: {
                throw new RuntimeException("Coding failure in Argo:  Attempt to format a JsonNode of unknown type [" + (Object)((Object)gu2.a()) + "];");
            }
        }
    }
}

