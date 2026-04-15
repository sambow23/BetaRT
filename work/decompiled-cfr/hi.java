/*
 * Decompiled with CFR 0.152.
 */
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.zip.GZIPInputStream;

public class hi
extends tq {
    public hi(File file) {
        super(file);
    }

    public String a() {
        return "Scaevolus' McRegion";
    }

    public List b() {
        File[] fileArray;
        ArrayList<vb> arrayList = new ArrayList<vb>();
        for (File file : fileArray = this.a.listFiles()) {
            String string;
            ei ei2;
            if (!file.isDirectory() || (ei2 = this.b(string = file.getName())) == null) continue;
            boolean bl2 = ei2.k() != 19132;
            String string2 = ei2.j();
            if (string2 == null || in.a(string2)) {
                string2 = string;
            }
            arrayList.add(new vb(string, string2, ei2.l(), ei2.g(), bl2));
        }
        return arrayList;
    }

    public void c() {
        rj.a();
    }

    public wt a(String string, boolean bl2) {
        return new mx(this.a, string, bl2);
    }

    public boolean a(String string) {
        ei ei2 = this.b(string);
        return ei2 != null && ei2.k() == 0;
    }

    public boolean a(String string, yb yb2) {
        yb2.a(0);
        ArrayList arrayList = new ArrayList();
        ArrayList arrayList2 = new ArrayList();
        ArrayList arrayList3 = new ArrayList();
        ArrayList arrayList4 = new ArrayList();
        File file = new File(this.a, string);
        File file2 = new File(file, "DIM-1");
        System.out.println("Scanning folders...");
        this.a(file, arrayList, arrayList2);
        if (file2.exists()) {
            this.a(file2, arrayList3, arrayList4);
        }
        int n2 = arrayList.size() + arrayList3.size() + arrayList2.size() + arrayList4.size();
        System.out.println("Total conversion count is " + n2);
        this.a(file, arrayList, 0, n2, yb2);
        this.a(file2, arrayList3, arrayList.size(), n2, yb2);
        ei ei2 = this.b(string);
        ei2.d(19132);
        wt wt2 = this.a(string, false);
        wt2.a(ei2);
        this.a(arrayList2, arrayList.size() + arrayList3.size(), n2, yb2);
        if (file2.exists()) {
            this.a(arrayList4, arrayList.size() + arrayList3.size() + arrayList2.size(), n2, yb2);
        }
        return true;
    }

    private void a(File file, ArrayList arrayList, ArrayList arrayList2) {
        File[] fileArray;
        cm cm2 = new cm(null);
        rs rs2 = new rs(null);
        for (File file2 : fileArray = file.listFiles(cm2)) {
            File[] fileArray2;
            arrayList2.add(file2);
            for (File file3 : fileArray2 = file2.listFiles(cm2)) {
                File[] fileArray3;
                for (File file4 : fileArray3 = file3.listFiles(rs2)) {
                    arrayList.add(new dz(file4));
                }
            }
        }
    }

    private void a(File file, ArrayList arrayList, int n2, int n3, yb yb2) {
        Collections.sort(arrayList);
        byte[] byArray = new byte[4096];
        for (dz dz2 : arrayList) {
            int n4;
            int n5 = dz2.b();
            qj qj2 = rj.a(file, n5, n4 = dz2.c());
            if (!qj2.c(n5 & 0x1F, n4 & 0x1F)) {
                try {
                    DataInputStream dataInputStream = new DataInputStream(new GZIPInputStream(new FileInputStream(dz2.a())));
                    DataOutputStream dataOutputStream = qj2.b(n5 & 0x1F, n4 & 0x1F);
                    int n6 = 0;
                    while ((n6 = dataInputStream.read(byArray)) != -1) {
                        dataOutputStream.write(byArray, 0, n6);
                    }
                    dataOutputStream.close();
                    dataInputStream.close();
                }
                catch (IOException iOException) {
                    iOException.printStackTrace();
                }
            }
            int n7 = (int)Math.round(100.0 * (double)(++n2) / (double)n3);
            yb2.a(n7);
        }
        rj.a();
    }

    private void a(ArrayList arrayList, int n2, int n3, yb yb2) {
        for (File file : arrayList) {
            File[] fileArray = file.listFiles();
            hi.a(fileArray);
            file.delete();
            int n4 = (int)Math.round(100.0 * (double)(++n2) / (double)n3);
            yb2.a(n4);
        }
    }
}

