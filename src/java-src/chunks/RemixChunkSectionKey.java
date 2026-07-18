final class RemixChunkSectionKey {
    private RemixChunkSectionKey() {
    }

    static long encode(int originX, int originY, int originZ) {
        return RemixCaveCulling.getChunkKey(originX, originY, originZ);
    }

    static int originX(long key) {
        return extractSignedAxis(key >>> 37, 0x3FFFFFFL, 0x2000000L) << 4;
    }

    static int originZ(long key) {
        return extractSignedAxis(key >>> 11, 0x3FFFFFFL, 0x2000000L) << 4;
    }

    static int originY(long key) {
        return ((int) (key & 0x7FFL)) << 4;
    }

    private static int extractSignedAxis(long encodedValue, long mask, long signBit) {
        long decoded = encodedValue & mask;
        if ((decoded & signBit) != 0L) {
            decoded |= ~mask;
        }
        return (int) decoded;
    }
}
