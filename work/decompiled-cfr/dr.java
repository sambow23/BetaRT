/*
 * Decompiled with CFR 0.152.
 */
public class dr {
    public float[][] a = new float[16][16];
    public float[] b = new float[16];
    public float[] c = new float[16];
    public float[] d = new float[16];

    public boolean a(double d2, double d3, double d4, double d5, double d6, double d7) {
        for (int i2 = 0; i2 < 6; ++i2) {
            if ((double)this.a[i2][0] * d2 + (double)this.a[i2][1] * d3 + (double)this.a[i2][2] * d4 + (double)this.a[i2][3] > 0.0 || (double)this.a[i2][0] * d5 + (double)this.a[i2][1] * d3 + (double)this.a[i2][2] * d4 + (double)this.a[i2][3] > 0.0 || (double)this.a[i2][0] * d2 + (double)this.a[i2][1] * d6 + (double)this.a[i2][2] * d4 + (double)this.a[i2][3] > 0.0 || (double)this.a[i2][0] * d5 + (double)this.a[i2][1] * d6 + (double)this.a[i2][2] * d4 + (double)this.a[i2][3] > 0.0 || (double)this.a[i2][0] * d2 + (double)this.a[i2][1] * d3 + (double)this.a[i2][2] * d7 + (double)this.a[i2][3] > 0.0 || (double)this.a[i2][0] * d5 + (double)this.a[i2][1] * d3 + (double)this.a[i2][2] * d7 + (double)this.a[i2][3] > 0.0 || (double)this.a[i2][0] * d2 + (double)this.a[i2][1] * d6 + (double)this.a[i2][2] * d7 + (double)this.a[i2][3] > 0.0 || (double)this.a[i2][0] * d5 + (double)this.a[i2][1] * d6 + (double)this.a[i2][2] * d7 + (double)this.a[i2][3] > 0.0) continue;
            return false;
        }
        return true;
    }
}

