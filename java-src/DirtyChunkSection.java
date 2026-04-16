final class DirtyChunkSection {
    final int originX;
    final int originY;
    final int originZ;

    DirtyChunkSection(int originX, int originY, int originZ) {
        this.originX = originX;
        this.originY = originY;
        this.originZ = originZ;
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
