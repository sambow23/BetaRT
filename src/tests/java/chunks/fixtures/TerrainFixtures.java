class fd {
    boolean loaded = true;
    int reads;
    final int[] blocks = new int[4096];
    boolean i(int x, int y, int z) { return loaded; }
    int a(int x, int y, int z) { ++reads; return blocks[(x & 15) + 16 * (z & 15) + 256 * (y & 15)]; }
    int e(int x, int y, int z) { return 0; }
    void a(RemixWorldListener listener) { }
    void b(RemixWorldListener listener) { }
}
class dk {
    fd a;
    int c, d, e;
    dk(fd world, int x, int y, int z) { a = world; c = x; d = y; e = z; }
}
class uu {
    static final uu[] m = {null, new uu()};
    int b() { return 0; }
}
class RemixWorldListener { }
class RemixCameraState { static double cameraPositionX, cameraPositionY, cameraPositionZ; }
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
