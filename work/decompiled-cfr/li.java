/*
 * Decompiled with CFR 0.152.
 */
import java.util.List;
import java.util.Random;

public class li
extends lm {
    public li(fd fd2, int n2, int n3) {
        super(fd2, n2, n3);
        this.p = true;
    }

    public li(fd fd2, byte[] byArray, int n2, int n3) {
        super(fd2, byArray, n2, n3);
        this.p = true;
    }

    public boolean a(int n2, int n3) {
        return n2 == this.j && n3 == this.k;
    }

    public int b(int n2, int n3) {
        return 0;
    }

    public void a() {
    }

    public void b() {
    }

    public void c() {
    }

    public void d() {
    }

    public int a(int n2, int n3, int n4) {
        return 0;
    }

    public boolean a(int n2, int n3, int n4, int n5, int n6) {
        return true;
    }

    public boolean a(int n2, int n3, int n4, int n5) {
        return true;
    }

    public int b(int n2, int n3, int n4) {
        return 0;
    }

    public void b(int n2, int n3, int n4, int n5) {
    }

    public int a(eb eb2, int n2, int n3, int n4) {
        return 0;
    }

    public void a(eb eb2, int n2, int n3, int n4, int n5) {
    }

    public int c(int n2, int n3, int n4, int n5) {
        return 0;
    }

    public void a(sn sn2) {
    }

    public void b(sn sn2) {
    }

    public void a(sn sn2, int n2) {
    }

    public boolean c(int n2, int n3, int n4) {
        return false;
    }

    public ow d(int n2, int n3, int n4) {
        return null;
    }

    public void a(ow ow2) {
    }

    public void a(int n2, int n3, int n4, ow ow2) {
    }

    public void e(int n2, int n3, int n4) {
    }

    public void e() {
    }

    public void f() {
    }

    public void g() {
    }

    public void a(sn sn2, eq eq2, List list) {
    }

    public void a(Class clazz, eq eq2, List list) {
    }

    public boolean a(boolean bl2) {
        return false;
    }

    public int a(byte[] byArray, int n2, int n3, int n4, int n5, int n6, int n7, int n8) {
        int n9 = n5 - n2;
        int n10 = n6 - n3;
        int n11 = n7 - n4;
        int n12 = n9 * n10 * n11;
        return n12 + n12 / 2 * 3;
    }

    public Random a(long l2) {
        return new Random(this.d.s() + (long)(this.j * this.j * 4987142) + (long)(this.j * 5947611) + (long)(this.k * this.k) * 4392871L + (long)(this.k * 389711) ^ l2);
    }

    public boolean h() {
        return true;
    }
}

