/*
 * Decompiled with CFR 0.152.
 */
public class cy
extends ow {
    public int a = 20;
    private String i = "Pig";
    public double b;
    public double c = 0.0;

    public String a() {
        return this.i;
    }

    public void a(String string) {
        this.i = string;
    }

    public boolean b() {
        return this.d.a((double)this.e + 0.5, (double)this.f + 0.5, (double)this.g + 0.5, 16.0) != null;
    }

    public void n_() {
        this.c = this.b;
        if (!this.b()) {
            return;
        }
        double d2 = (float)this.e + this.d.r.nextFloat();
        double d3 = (float)this.f + this.d.r.nextFloat();
        double d4 = (float)this.g + this.d.r.nextFloat();
        this.d.a("smoke", d2, d3, d4, 0.0, 0.0, 0.0);
        this.d.a("flame", d2, d3, d4, 0.0, 0.0, 0.0);
        this.b += (double)(1000.0f / ((float)this.a + 200.0f));
        while (this.b > 360.0) {
            this.b -= 360.0;
            this.c -= 360.0;
        }
        if (!this.d.B) {
            if (this.a == -1) {
                this.d();
            }
            if (this.a > 0) {
                --this.a;
                return;
            }
            int n2 = 4;
            for (int i2 = 0; i2 < n2; ++i2) {
                ls ls2 = (ls)jc.a(this.i, this.d);
                if (ls2 == null) {
                    return;
                }
                int n3 = this.d.a(ls2.getClass(), eq.b(this.e, this.f, this.g, this.e + 1, this.f + 1, this.g + 1).b(8.0, 4.0, 8.0)).size();
                if (n3 >= 6) {
                    this.d();
                    return;
                }
                if (ls2 == null) continue;
                double d5 = (double)this.e + (this.d.r.nextDouble() - this.d.r.nextDouble()) * 4.0;
                double d6 = this.f + this.d.r.nextInt(3) - 1;
                double d7 = (double)this.g + (this.d.r.nextDouble() - this.d.r.nextDouble()) * 4.0;
                ls2.c(d5, d6, d7, this.d.r.nextFloat() * 360.0f, 0.0f);
                if (!ls2.d()) continue;
                this.d.b(ls2);
                for (int i3 = 0; i3 < 20; ++i3) {
                    d2 = (double)this.e + 0.5 + ((double)this.d.r.nextFloat() - 0.5) * 2.0;
                    d3 = (double)this.f + 0.5 + ((double)this.d.r.nextFloat() - 0.5) * 2.0;
                    d4 = (double)this.g + 0.5 + ((double)this.d.r.nextFloat() - 0.5) * 2.0;
                    this.d.a("smoke", d2, d3, d4, 0.0, 0.0, 0.0);
                    this.d.a("flame", d2, d3, d4, 0.0, 0.0, 0.0);
                }
                ls2.V();
                this.d();
            }
        }
        super.n_();
    }

    private void d() {
        this.a = 200 + this.d.r.nextInt(600);
    }

    public void a(nu nu2) {
        super.a(nu2);
        this.i = nu2.i("EntityId");
        this.a = nu2.d("Delay");
    }

    public void b(nu nu2) {
        super.b(nu2);
        nu2.a("EntityId", this.i);
        nu2.a("Delay", (short)this.a);
    }
}

