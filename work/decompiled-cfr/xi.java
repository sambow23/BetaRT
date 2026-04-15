/*
 * Decompiled with CFR 0.152.
 */
import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class xi {
    private Map a = new HashMap();
    private Map b = new HashMap();
    private boolean c = false;
    private cj d;

    public xi(gr gr2, File file) {
        File file2 = new File(file, "stats");
        if (!file2.exists()) {
            file2.mkdir();
        }
        for (File file3 : file.listFiles()) {
            File file4;
            if (!file3.getName().startsWith("stats_") || !file3.getName().endsWith(".dat") || (file4 = new File(file2, file3.getName())).exists()) continue;
            System.out.println("Relocating " + file3.getName());
            file3.renameTo(file4);
        }
        this.d = new cj(gr2, this, file2);
    }

    public void a(vr vr2, int n2) {
        this.a(this.b, vr2, n2);
        this.a(this.a, vr2, n2);
        this.c = true;
    }

    private void a(Map map, vr vr2, int n2) {
        Integer n3 = (Integer)map.get(vr2);
        int n4 = n3 == null ? 0 : n3;
        map.put(vr2, n4 + n2);
    }

    public Map a() {
        return new HashMap(this.b);
    }

    public void a(Map map) {
        if (map == null) {
            return;
        }
        this.c = true;
        for (vr vr2 : map.keySet()) {
            this.a(this.b, vr2, (Integer)map.get(vr2));
            this.a(this.a, vr2, (Integer)map.get(vr2));
        }
    }

    public void b(Map map) {
        if (map == null) {
            return;
        }
        for (vr vr2 : map.keySet()) {
            Integer n2 = (Integer)this.b.get(vr2);
            int n3 = n2 == null ? 0 : n2;
            this.a.put(vr2, (Integer)map.get(vr2) + n3);
        }
    }

    public void c(Map map) {
        if (map == null) {
            return;
        }
        this.c = true;
        for (vr vr2 : map.keySet()) {
            this.a(this.b, vr2, (Integer)map.get(vr2));
        }
    }

    public static Map a(String string) {
        HashMap<vr, Integer> hashMap = new HashMap<vr, Integer>();
        try {
            Object object2;
            String string2 = "local";
            StringBuilder stringBuilder = new StringBuilder();
            qe qe2 = new hx().a(string);
            List list = qe2.b("stats-change");
            for (Object object2 : list) {
                Map map = ((gu)object2).c();
                Map.Entry entry = map.entrySet().iterator().next();
                int n2 = Integer.parseInt(((qa)entry.getKey()).b());
                int n3 = Integer.parseInt(((gu)entry.getValue()).b());
                vr vr2 = jl.a(n2);
                if (vr2 == null) {
                    System.out.println(n2 + " is not a valid stat");
                    continue;
                }
                stringBuilder.append(jl.a((int)n2).h).append(",");
                stringBuilder.append(n3).append(",");
                hashMap.put(vr2, n3);
            }
            hf hf2 = new hf(string2);
            object2 = hf2.a(stringBuilder.toString());
            if (!((String)object2).equals(qe2.a("checksum"))) {
                System.out.println("CHECKSUM MISMATCH");
                return null;
            }
        }
        catch (xe xe2) {
            xe2.printStackTrace();
        }
        return hashMap;
    }

    public static String a(String string, String string2, Map map) {
        StringBuilder stringBuilder = new StringBuilder();
        StringBuilder stringBuilder2 = new StringBuilder();
        boolean bl2 = true;
        stringBuilder.append("{\r\n");
        if (string != null && string2 != null) {
            stringBuilder.append("  \"user\":{\r\n");
            stringBuilder.append("    \"name\":\"").append(string).append("\",\r\n");
            stringBuilder.append("    \"sessionid\":\"").append(string2).append("\"\r\n");
            stringBuilder.append("  },\r\n");
        }
        stringBuilder.append("  \"stats-change\":[");
        for (vr vr2 : map.keySet()) {
            if (!bl2) {
                stringBuilder.append("},");
            } else {
                bl2 = false;
            }
            stringBuilder.append("\r\n    {\"").append(vr2.e).append("\":").append(map.get(vr2));
            stringBuilder2.append(vr2.h).append(",");
            stringBuilder2.append(map.get(vr2)).append(",");
        }
        if (!bl2) {
            stringBuilder.append("}");
        }
        hf hf2 = new hf(string2);
        stringBuilder.append("\r\n  ],\r\n");
        stringBuilder.append("  \"checksum\":\"").append(hf2.a(stringBuilder2.toString())).append("\"\r\n");
        stringBuilder.append("}");
        return stringBuilder.toString();
    }

    public boolean a(ny ny2) {
        return this.a.containsKey(ny2);
    }

    public boolean b(ny ny2) {
        return ny2.c == null || this.a(ny2.c);
    }

    public int a(vr vr2) {
        Integer n2 = (Integer)this.a.get(vr2);
        return n2 == null ? 0 : n2;
    }

    public void b() {
    }

    public void c() {
        this.d.b(this.a());
    }

    public void d() {
        if (this.c && this.d.b()) {
            this.d.a(this.a());
        }
        this.d.c();
    }
}

