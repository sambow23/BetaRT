final class RemixWorldListener implements pm {
    @Override
    public void a(int x, int y, int z) {
        RemixUndergroundCulling.invalidate(x,y,z,x,y,z);
        RemixChunkRecaptureQueue.queueRegion(x, y, z, x, y, z);
    }

    @Override
    public void b(int minX, int minY, int minZ, int maxX, int maxY, int maxZ) {
        RemixUndergroundCulling.invalidate(minX,minY,minZ,maxX,maxY,maxZ);
        RemixChunkRecaptureQueue.queueRegion(minX, minY, minZ, maxX, maxY, maxZ);
    }

    @Override
    public void a(String name, double x, double y, double z, float volume, float pitch) {
    }

    @Override
    public void a(String name, double x, double y, double z, double velocityX, double velocityY, double velocityZ) {
    }

    @Override
    public void a(sn entity) {
    }

    @Override
    public void b(sn entity) {
    }

    @Override
    public void e() {
    }

    @Override
    public void a(String name, int x, int y, int z) {
    }

    @Override
    public void a(int x, int y, int z, ow tileEntity) {
    }

    @Override
    public void a(gs player, int x, int y, int z, int direction, int itemId) {
    }
}
