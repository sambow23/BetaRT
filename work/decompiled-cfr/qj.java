/*
 * Decompiled with CFR 0.152.
 */
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.GZIPInputStream;
import java.util.zip.InflaterInputStream;

public class qj {
    private static final byte[] a = new byte[4096];
    private final File b;
    private RandomAccessFile c;
    private final int[] d = new int[1024];
    private final int[] e = new int[1024];
    private ArrayList f;
    private int g;
    private long h = 0L;

    public qj(File file) {
        this.b = file;
        this.b("REGION LOAD " + this.b);
        this.g = 0;
        try {
            int n2;
            int n3;
            int n4;
            if (file.exists()) {
                this.h = file.lastModified();
            }
            this.c = new RandomAccessFile(file, "rw");
            if (this.c.length() < 4096L) {
                for (n4 = 0; n4 < 1024; ++n4) {
                    this.c.writeInt(0);
                }
                for (n4 = 0; n4 < 1024; ++n4) {
                    this.c.writeInt(0);
                }
                this.g += 8192;
            }
            if ((this.c.length() & 0xFFFL) != 0L) {
                n4 = 0;
                while ((long)n4 < (this.c.length() & 0xFFFL)) {
                    this.c.write(0);
                    ++n4;
                }
            }
            n4 = (int)this.c.length() / 4096;
            this.f = new ArrayList(n4);
            for (n3 = 0; n3 < n4; ++n3) {
                this.f.add(true);
            }
            this.f.set(0, false);
            this.f.set(1, false);
            this.c.seek(0L);
            for (n3 = 0; n3 < 1024; ++n3) {
                this.d[n3] = n2 = this.c.readInt();
                if (n2 == 0 || (n2 >> 8) + (n2 & 0xFF) > this.f.size()) continue;
                for (int i2 = 0; i2 < (n2 & 0xFF); ++i2) {
                    this.f.set((n2 >> 8) + i2, false);
                }
            }
            for (n3 = 0; n3 < 1024; ++n3) {
                this.e[n3] = n2 = this.c.readInt();
            }
        }
        catch (IOException iOException) {
            iOException.printStackTrace();
        }
    }

    public synchronized int a() {
        int n2 = this.g;
        this.g = 0;
        return n2;
    }

    private void a(String string) {
    }

    private void b(String string) {
        this.a(string + "\n");
    }

    private void a(String string, int n2, int n3, String string2) {
        this.a("REGION " + string + " " + this.b.getName() + "[" + n2 + "," + n3 + "] = " + string2);
    }

    private void a(String string, int n2, int n3, int n4, String string2) {
        this.a("REGION " + string + " " + this.b.getName() + "[" + n2 + "," + n3 + "] " + n4 + "B = " + string2);
    }

    private void b(String string, int n2, int n3, String string2) {
        this.a(string, n2, n3, string2 + "\n");
    }

    public synchronized DataInputStream a(int n2, int n3) {
        if (this.d(n2, n3)) {
            this.b("READ", n2, n3, "out of bounds");
            return null;
        }
        try {
            int n4 = this.e(n2, n3);
            if (n4 == 0) {
                return null;
            }
            int n5 = n4 >> 8;
            int n6 = n4 & 0xFF;
            if (n5 + n6 > this.f.size()) {
                this.b("READ", n2, n3, "invalid sector");
                return null;
            }
            this.c.seek(n5 * 4096);
            int n7 = this.c.readInt();
            if (n7 > 4096 * n6) {
                this.b("READ", n2, n3, "invalid length: " + n7 + " > 4096 * " + n6);
                return null;
            }
            byte by2 = this.c.readByte();
            if (by2 == 1) {
                byte[] byArray = new byte[n7 - 1];
                this.c.read(byArray);
                DataInputStream dataInputStream = new DataInputStream(new GZIPInputStream(new ByteArrayInputStream(byArray)));
                return dataInputStream;
            }
            if (by2 == 2) {
                byte[] byArray = new byte[n7 - 1];
                this.c.read(byArray);
                DataInputStream dataInputStream = new DataInputStream(new InflaterInputStream(new ByteArrayInputStream(byArray)));
                return dataInputStream;
            }
            this.b("READ", n2, n3, "unknown version " + by2);
            return null;
        }
        catch (IOException iOException) {
            this.b("READ", n2, n3, "exception");
            return null;
        }
    }

    public DataOutputStream b(int n2, int n3) {
        if (this.d(n2, n3)) {
            return null;
        }
        return new DataOutputStream(new DeflaterOutputStream(new hz(this, n2, n3)));
    }

    protected synchronized void a(int n2, int n3, byte[] byArray, int n4) {
        try {
            int n5 = this.e(n2, n3);
            int n6 = n5 >> 8;
            int n7 = n5 & 0xFF;
            int n8 = (n4 + 5) / 4096 + 1;
            if (n8 >= 256) {
                return;
            }
            if (n6 != 0 && n7 == n8) {
                this.a("SAVE", n2, n3, n4, "rewrite");
                this.a(n6, byArray, n4);
            } else {
                int n9;
                int n10;
                for (n10 = 0; n10 < n7; ++n10) {
                    this.f.set(n6 + n10, true);
                }
                n10 = this.f.indexOf(true);
                int n11 = 0;
                if (n10 != -1) {
                    for (n9 = n10; n9 < this.f.size(); ++n9) {
                        if (n11 != 0) {
                            n11 = ((Boolean)this.f.get(n9)).booleanValue() ? ++n11 : 0;
                        } else if (((Boolean)this.f.get(n9)).booleanValue()) {
                            n10 = n9;
                            n11 = 1;
                        }
                        if (n11 >= n8) break;
                    }
                }
                if (n11 >= n8) {
                    this.a("SAVE", n2, n3, n4, "reuse");
                    n6 = n10;
                    this.a(n2, n3, n6 << 8 | n8);
                    for (n9 = 0; n9 < n8; ++n9) {
                        this.f.set(n6 + n9, false);
                    }
                    this.a(n6, byArray, n4);
                } else {
                    this.a("SAVE", n2, n3, n4, "grow");
                    this.c.seek(this.c.length());
                    n6 = this.f.size();
                    for (n9 = 0; n9 < n8; ++n9) {
                        this.c.write(a);
                        this.f.add(false);
                    }
                    this.g += 4096 * n8;
                    this.a(n6, byArray, n4);
                    this.a(n2, n3, n6 << 8 | n8);
                }
            }
            this.b(n2, n3, (int)(System.currentTimeMillis() / 1000L));
        }
        catch (IOException iOException) {
            iOException.printStackTrace();
        }
    }

    private void a(int n2, byte[] byArray, int n3) {
        this.b(" " + n2);
        this.c.seek(n2 * 4096);
        this.c.writeInt(n3 + 1);
        this.c.writeByte(2);
        this.c.write(byArray, 0, n3);
    }

    private boolean d(int n2, int n3) {
        return n2 < 0 || n2 >= 32 || n3 < 0 || n3 >= 32;
    }

    private int e(int n2, int n3) {
        return this.d[n2 + n3 * 32];
    }

    public boolean c(int n2, int n3) {
        return this.e(n2, n3) != 0;
    }

    private void a(int n2, int n3, int n4) {
        this.d[n2 + n3 * 32] = n4;
        this.c.seek((n2 + n3 * 32) * 4);
        this.c.writeInt(n4);
    }

    private void b(int n2, int n3, int n4) {
        this.e[n2 + n3 * 32] = n4;
        this.c.seek(4096 + (n2 + n3 * 32) * 4);
        this.c.writeInt(n4);
    }

    public void b() {
        this.c.close();
    }
}

