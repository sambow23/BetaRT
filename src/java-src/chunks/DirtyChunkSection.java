final class DirtyChunkSection {
    final int originX;
    final int originY;
    final int originZ;
    int dirtyMinX;
    int dirtyMinY;
    int dirtyMinZ;
    int dirtyMaxX;
    int dirtyMaxY;
    int dirtyMaxZ;

    DirtyChunkSection(int originX, int originY, int originZ) {
        this(originX, originY, originZ,
                originX,
                originY,
                originZ,
                originX + 15,
                originY + 15,
                originZ + 15);
    }

    DirtyChunkSection(
            int originX,
            int originY,
            int originZ,
            int dirtyMinX,
            int dirtyMinY,
            int dirtyMinZ,
            int dirtyMaxX,
            int dirtyMaxY,
            int dirtyMaxZ) {
        this.originX = originX;
        this.originY = originY;
        this.originZ = originZ;
        setDirtyRegion(dirtyMinX, dirtyMinY, dirtyMinZ, dirtyMaxX, dirtyMaxY, dirtyMaxZ);
    }

    void mergeDirtyRegion(int minX, int minY, int minZ, int maxX, int maxY, int maxZ) {
        dirtyMinX = Math.min(dirtyMinX, clampX(minX));
        dirtyMinY = Math.min(dirtyMinY, clampY(minY));
        dirtyMinZ = Math.min(dirtyMinZ, clampZ(minZ));
        dirtyMaxX = Math.max(dirtyMaxX, clampX(maxX));
        dirtyMaxY = Math.max(dirtyMaxY, clampY(maxY));
        dirtyMaxZ = Math.max(dirtyMaxZ, clampZ(maxZ));
    }

    boolean coversWholeSection() {
        return dirtyMinX == originX
                && dirtyMinY == originY
                && dirtyMinZ == originZ
                && dirtyMaxX == originX + 15
                && dirtyMaxY == originY + 15
                && dirtyMaxZ == originZ + 15;
    }

    int dirtyBlockVolume() {
        return (dirtyMaxX - dirtyMinX + 1)
                * (dirtyMaxY - dirtyMinY + 1)
                * (dirtyMaxZ - dirtyMinZ + 1);
    }

    private void setDirtyRegion(int minX, int minY, int minZ, int maxX, int maxY, int maxZ) {
        dirtyMinX = clampX(Math.min(minX, maxX));
        dirtyMinY = clampY(Math.min(minY, maxY));
        dirtyMinZ = clampZ(Math.min(minZ, maxZ));
        dirtyMaxX = clampX(Math.max(minX, maxX));
        dirtyMaxY = clampY(Math.max(minY, maxY));
        dirtyMaxZ = clampZ(Math.max(minZ, maxZ));
    }

    private int clampX(int x) {
        return Math.max(originX, Math.min(originX + 15, x));
    }

    private int clampY(int y) {
        return Math.max(originY, Math.min(originY + 15, y));
    }

    private int clampZ(int z) {
        return Math.max(originZ, Math.min(originZ + 15, z));
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (!(object instanceof DirtyChunkSection)) {
            return false;
        }

        DirtyChunkSection other = (DirtyChunkSection) object;
        return originX == other.originX && originY == other.originY && originZ == other.originZ;
    }

    @Override
    public int hashCode() {
        int result = originX;
        result = 31 * result + originY;
        result = 31 * result + originZ;
        return result;
    }
}
