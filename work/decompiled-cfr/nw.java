/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.lwjgl.opengl.ARBVertexBufferObject
 *  org.lwjgl.opengl.GL11
 *  org.lwjgl.opengl.GLContext
 */
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import org.lwjgl.opengl.ARBVertexBufferObject;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GLContext;

public class nw {
    private static boolean b = true;
    private static boolean c = false;
    private ByteBuffer d;
    private IntBuffer e;
    private FloatBuffer f;
    private int[] g;
    private int h = 0;
    private double i;
    private double j;
    private int k;
    private boolean l = false;
    private boolean m = false;
    private boolean n = false;
    private int o = 0;
    private int p = 0;
    private boolean q = false;
    private int r;
    private double s;
    private double t;
    private double u;
    private int v;
    public static final nw a = new nw(0x200000);
    private boolean w = false;
    private boolean x = false;
    private IntBuffer y;
    private int z = 0;
    private int A = 10;
    private int B;

    private nw(int n2) {
        this.B = n2;
        this.d = ge.c(n2 * 4);
        this.e = this.d.asIntBuffer();
        this.f = this.d.asFloatBuffer();
        this.g = new int[n2];
        boolean bl2 = this.x = c && GLContext.getCapabilities().GL_ARB_vertex_buffer_object;
        if (this.x) {
            this.y = ge.d(this.A);
            ARBVertexBufferObject.glGenBuffersARB((IntBuffer)this.y);
        }
    }

    public void a() {
        if (!this.w) {
            throw new IllegalStateException("Not tesselating!");
        }
        this.w = false;
        if (this.h > 0) {
            this.e.clear();
            this.e.put(this.g, 0, this.o);
            this.d.position(0);
            this.d.limit(this.o * 4);
            if (this.x) {
                this.z = (this.z + 1) % this.A;
                ARBVertexBufferObject.glBindBufferARB((int)34962, (int)this.y.get(this.z));
                ARBVertexBufferObject.glBufferDataARB((int)34962, (ByteBuffer)this.d, (int)35040);
            }
            if (this.m) {
                if (this.x) {
                    GL11.glTexCoordPointer((int)2, (int)5126, (int)32, (long)12L);
                } else {
                    this.f.position(3);
                    GL11.glTexCoordPointer((int)2, (int)32, (FloatBuffer)this.f);
                }
                GL11.glEnableClientState((int)32888);
            }
            if (this.l) {
                if (this.x) {
                    GL11.glColorPointer((int)4, (int)5121, (int)32, (long)20L);
                } else {
                    this.d.position(20);
                    GL11.glColorPointer((int)4, (boolean)true, (int)32, (ByteBuffer)this.d);
                }
                GL11.glEnableClientState((int)32886);
            }
            if (this.n) {
                if (this.x) {
                    GL11.glNormalPointer((int)5120, (int)32, (long)24L);
                } else {
                    this.d.position(24);
                    GL11.glNormalPointer((int)32, (ByteBuffer)this.d);
                }
                GL11.glEnableClientState((int)32885);
            }
            if (this.x) {
                GL11.glVertexPointer((int)3, (int)5126, (int)32, (long)0L);
            } else {
                this.f.position(0);
                GL11.glVertexPointer((int)3, (int)32, (FloatBuffer)this.f);
            }
            GL11.glEnableClientState((int)32884);
            if (this.r == 7 && b) {
                GL11.glDrawArrays((int)4, (int)0, (int)this.h);
            } else {
                GL11.glDrawArrays((int)this.r, (int)0, (int)this.h);
            }
            GL11.glDisableClientState((int)32884);
            if (this.m) {
                GL11.glDisableClientState((int)32888);
            }
            if (this.l) {
                GL11.glDisableClientState((int)32886);
            }
            if (this.n) {
                GL11.glDisableClientState((int)32885);
            }
        }
        this.d();
    }

    private void d() {
        this.h = 0;
        this.d.clear();
        this.o = 0;
        this.p = 0;
    }

    public void b() {
        this.a(7);
    }

    public void a(int n2) {
        if (this.w) {
            throw new IllegalStateException("Already tesselating!");
        }
        this.w = true;
        this.d();
        this.r = n2;
        this.n = false;
        this.l = false;
        this.m = false;
        this.q = false;
    }

    public void a(double d2, double d3) {
        this.m = true;
        this.i = d2;
        this.j = d3;
    }

    public void a(float f2, float f3, float f4) {
        this.a((int)(f2 * 255.0f), (int)(f3 * 255.0f), (int)(f4 * 255.0f));
    }

    public void a(float f2, float f3, float f4, float f5) {
        this.a((int)(f2 * 255.0f), (int)(f3 * 255.0f), (int)(f4 * 255.0f), (int)(f5 * 255.0f));
    }

    public void a(int n2, int n3, int n4) {
        this.a(n2, n3, n4, 255);
    }

    public void a(int n2, int n3, int n4, int n5) {
        if (this.q) {
            return;
        }
        if (n2 > 255) {
            n2 = 255;
        }
        if (n3 > 255) {
            n3 = 255;
        }
        if (n4 > 255) {
            n4 = 255;
        }
        if (n5 > 255) {
            n5 = 255;
        }
        if (n2 < 0) {
            n2 = 0;
        }
        if (n3 < 0) {
            n3 = 0;
        }
        if (n4 < 0) {
            n4 = 0;
        }
        if (n5 < 0) {
            n5 = 0;
        }
        this.l = true;
        this.k = ByteOrder.nativeOrder() == ByteOrder.LITTLE_ENDIAN ? n5 << 24 | n4 << 16 | n3 << 8 | n2 : n2 << 24 | n3 << 16 | n4 << 8 | n5;
    }

    public void a(double d2, double d3, double d4, double d5, double d6) {
        this.a(d5, d6);
        this.a(d2, d3, d4);
    }

    public void a(double d2, double d3, double d4) {
        ++this.p;
        if (this.r == 7 && b && this.p % 4 == 0) {
            for (int i2 = 0; i2 < 2; ++i2) {
                int n2 = 8 * (3 - i2);
                if (this.m) {
                    this.g[this.o + 3] = this.g[this.o - n2 + 3];
                    this.g[this.o + 4] = this.g[this.o - n2 + 4];
                }
                if (this.l) {
                    this.g[this.o + 5] = this.g[this.o - n2 + 5];
                }
                this.g[this.o + 0] = this.g[this.o - n2 + 0];
                this.g[this.o + 1] = this.g[this.o - n2 + 1];
                this.g[this.o + 2] = this.g[this.o - n2 + 2];
                ++this.h;
                this.o += 8;
            }
        }
        if (this.m) {
            this.g[this.o + 3] = Float.floatToRawIntBits((float)this.i);
            this.g[this.o + 4] = Float.floatToRawIntBits((float)this.j);
        }
        if (this.l) {
            this.g[this.o + 5] = this.k;
        }
        if (this.n) {
            this.g[this.o + 6] = this.v;
        }
        this.g[this.o + 0] = Float.floatToRawIntBits((float)(d2 + this.s));
        this.g[this.o + 1] = Float.floatToRawIntBits((float)(d3 + this.t));
        this.g[this.o + 2] = Float.floatToRawIntBits((float)(d4 + this.u));
        this.o += 8;
        ++this.h;
        if (this.h % 4 == 0 && this.o >= this.B - 32) {
            this.a();
            this.w = true;
        }
    }

    public void b(int n2) {
        int n3 = n2 >> 16 & 0xFF;
        int n4 = n2 >> 8 & 0xFF;
        int n5 = n2 & 0xFF;
        this.a(n3, n4, n5);
    }

    public void a(int n2, int n3) {
        int n4 = n2 >> 16 & 0xFF;
        int n5 = n2 >> 8 & 0xFF;
        int n6 = n2 & 0xFF;
        this.a(n4, n5, n6, n3);
    }

    public void c() {
        this.q = true;
    }

    public void b(float f2, float f3, float f4) {
        if (!this.w) {
            System.out.println("But..");
        }
        this.n = true;
        byte by2 = (byte)(f2 * 128.0f);
        byte by3 = (byte)(f3 * 127.0f);
        byte by4 = (byte)(f4 * 127.0f);
        this.v = by2 | by3 << 8 | by4 << 16;
    }

    public void b(double d2, double d3, double d4) {
        this.s = d2;
        this.t = d3;
        this.u = d4;
    }

    public void c(float f2, float f3, float f4) {
        this.s += (double)f2;
        this.t += (double)f3;
        this.u += (double)f4;
    }
}

