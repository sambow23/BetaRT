/*
 * Decompiled with CFR 0.152.
 */
import java.io.DataInput;
import java.io.DataInputStream;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class as {
    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static nu a(InputStream inputStream) {
        DataInputStream dataInputStream = new DataInputStream(new GZIPInputStream(inputStream));
        try {
            nu nu2 = as.a(dataInputStream);
            return nu2;
        }
        finally {
            dataInputStream.close();
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static void a(nu nu2, OutputStream outputStream) {
        DataOutputStream dataOutputStream = new DataOutputStream(new GZIPOutputStream(outputStream));
        try {
            as.a(nu2, dataOutputStream);
        }
        finally {
            dataOutputStream.close();
        }
    }

    public static nu a(DataInput dataInput) {
        ij ij2 = ij.b(dataInput);
        if (ij2 instanceof nu) {
            return (nu)ij2;
        }
        throw new IOException("Root tag must be a named compound tag");
    }

    public static void a(nu nu2, DataOutput dataOutput) {
        ij.a(nu2, dataOutput);
    }
}

