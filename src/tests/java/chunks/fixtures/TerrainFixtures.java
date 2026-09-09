class fd {
    boolean loaded = true, hole;
    cl w(){return new cl(this);}
    int reads;
    final int[] blocks = new int[4096];
    boolean i(int x, int y, int z) { return loaded; }
    int a(int x, int y, int z) { ++reads; return blocks[(x & 15) + 16 * (z & 15) + 256 * (y & 15)]; }
    int e(int x, int y, int z) { return 0; }
}
class cl {
    final fd world; cl(fd value){world=value;}
    boolean a(int x,int z){return world.loaded && !(world.hole && x==0 && z==0);}
    zx b(int x,int z){return new zx();}
}
class zx { boolean h(){return false;} }
class uu {
    static final uu[] m = {null, new uu()};
    int b() { return 0; }
}
class DirectGameWorld { static double originX,originY,originZ; }
class RemixChunkBlockCapture {
    static Runnable duringCapture;
    static void captureWorldBlock(fd world, int x, int y, int z, int id, int meta, int type, int[] records, int offset) {
        records[offset] = id;
        if (duringCapture != null) {
            Runnable callback = duringCapture;
            duringCapture = null;
            callback.run();
        }
    }
}
