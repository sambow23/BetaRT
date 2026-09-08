package mcrtx.lod.format;

import java.io.ByteArrayOutputStream;
import java.util.zip.DataFormatException;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

/**
 * Serialises one tile to bytes, and back.
 *
 * <p>The same encoding serves memory, disk and the wire. That is deliberate:
 * a shareable cache file, a server's reply and a client's own store are the same
 * artifact, so giving each its own representation would mean three encoders that
 * have to agree and three places for them to stop agreeing.
 *
 * <p>The header is left uncompressed so a reader can walk a file of tiles --
 * identifying, indexing or skipping each one -- without inflating any of them.
 * Only the cell array is deflated, and it deflates hard: heights and climate
 * vary smoothly across a tile and most fields repeat, so a 9,216 byte tile
 * typically lands between one and two kilobytes.
 *
 * <p>The decoder never trusts a length from the stream. A tile's inflated size
 * is a constant both sides already know, so decoding inflates into a
 * fixed-size buffer and rejects any stream that would produce a different
 * amount. A corrupt or hostile blob cannot make the decoder allocate.
 */
public final class LodTileCodec {
    /**
     * On-disk and on-wire format version.
     *
     * <p>Bumping this invalidates existing cache files. It should change when
     * the cell layout, the header layout or the reducer's rules change --
     * the reducer included, because a file's coarse tiles were produced by
     * whichever reducer wrote them.
     */
    public static final int VERSION = 1;

    /** version, level, tileX, tileZ, source, stamp, compressed length. */
    public static final int HEADER_BYTES = 1 + 1 + 4 + 4 + 1 + 8 + 4;

    private LodTileCodec() {
    }

    /** A tile recovered from bytes. */
    public static final class Decoded {
        public final LodTileKey key;
        public final int source;
        public final long stampMillis;
        public final byte[] cells;
        /** Bytes consumed, so a caller walking a file knows where the next tile starts. */
        public final int consumed;

        Decoded(LodTileKey key, int source, long stampMillis, byte[] cells, int consumed) {
            this.key = key;
            this.source = source;
            this.stampMillis = stampMillis;
            this.cells = cells;
            this.consumed = consumed;
        }
    }

    /** Encodes a tile, header and all, into a self-delimiting blob. */
    public static byte[] encode(LodTileKey key, int source, long stampMillis, byte[] cells) {
        if (key == null) {
            throw new IllegalArgumentException("key required");
        }
        if (cells == null || cells.length != LodTileKey.TILE_BYTES) {
            throw new IllegalArgumentException("cells must be a whole tile");
        }
        if (!LodProvenance.isValid(source)) {
            throw new IllegalArgumentException("unknown provenance: " + source);
        }

        byte[] compressed = deflate(cells);
        byte[] blob = new byte[HEADER_BYTES + compressed.length];

        int offset = 0;
        blob[offset++] = (byte) VERSION;
        blob[offset++] = (byte) key.level();
        offset = writeInt(blob, offset, key.tileX());
        offset = writeInt(blob, offset, key.tileZ());
        blob[offset++] = (byte) source;
        offset = writeLong(blob, offset, stampMillis);
        offset = writeInt(blob, offset, compressed.length);

        System.arraycopy(compressed, 0, blob, offset, compressed.length);
        return blob;
    }

    /**
     * Decodes a tile beginning at {@code offset}.
     *
     * @return the tile, or null when the blob is truncated, of an unknown
     *     version, or otherwise not a tile. Returning null rather than throwing
     *     is deliberate: cache files are shared between installs and arrive over
     *     a network, so a bad tile is an expected event to be skipped, not an
     *     exceptional one to abort a session over.
     */
    public static Decoded decode(byte[] blob, int offset) {
        if (blob == null || offset < 0 || offset + HEADER_BYTES > blob.length) {
            return null;
        }

        int cursor = offset;
        int version = blob[cursor++] & 0xFF;
        if (version != VERSION) {
            return null;
        }

        int level = blob[cursor++] & 0xFF;
        if (level < LodTileKey.MIN_LEVEL || level > LodTileKey.MAX_LEVEL) {
            return null;
        }

        int tileX = readInt(blob, cursor);
        cursor += 4;
        int tileZ = readInt(blob, cursor);
        cursor += 4;

        int source = blob[cursor++] & 0xFF;
        if (!LodProvenance.isValid(source)) {
            return null;
        }

        long stampMillis = readLong(blob, cursor);
        cursor += 8;

        int compressedLength = readInt(blob, cursor);
        cursor += 4;
        if (compressedLength < 0 || cursor + compressedLength > blob.length) {
            return null;
        }

        byte[] cells = inflate(blob, cursor, compressedLength);
        if (cells == null) {
            return null;
        }

        int consumed = (cursor + compressedLength) - offset;
        return new Decoded(new LodTileKey(level, tileX, tileZ), source, stampMillis, cells, consumed);
    }

    /**
     * True when two encoded tiles say the same thing about the same ground.
     *
     * <p>Compares everything but the timestamp: two encodings of identical cells
     * made a minute apart differ only there, and treating them as different
     * would make every re-observation of unchanged ground look like a change --
     * dirtying a file and dropping derived tiles for nothing. Deflate is
     * deterministic over identical input, so comparing the compressed payload is
     * as good as comparing the cells and far cheaper than inflating both.
     */
    public static boolean sameContent(byte[] left, byte[] right) {
        if (left == null || right == null || left.length != right.length) {
            return false;
        }
        for (int index = 0; index < left.length; index++) {
            if (index >= STAMP_OFFSET && index < STAMP_OFFSET + 8) {
                continue;
            }
            if (left[index] != right[index]) {
                return false;
            }
        }
        return true;
    }

    /** Offset of the millisecond stamp within the header. */
    private static final int STAMP_OFFSET = 1 + 1 + 4 + 4 + 1;

    /** Reads just the key of a tile without inflating it, or null if unreadable. */
    public static LodTileKey peekKey(byte[] blob, int offset) {
        if (blob == null || offset < 0 || offset + HEADER_BYTES > blob.length) {
            return null;
        }
        if ((blob[offset] & 0xFF) != VERSION) {
            return null;
        }
        int level = blob[offset + 1] & 0xFF;
        if (level < LodTileKey.MIN_LEVEL || level > LodTileKey.MAX_LEVEL) {
            return null;
        }
        return new LodTileKey(level, readInt(blob, offset + 2), readInt(blob, offset + 6));
    }

    // ---- compression ---------------------------------------------------

    private static byte[] deflate(byte[] input) {
        Deflater deflater = new Deflater(Deflater.BEST_COMPRESSION);
        try {
            deflater.setInput(input);
            deflater.finish();

            ByteArrayOutputStream out = new ByteArrayOutputStream(input.length / 4);
            byte[] chunk = new byte[4096];
            while (!deflater.finished()) {
                int produced = deflater.deflate(chunk);
                if (produced <= 0) {
                    break;
                }
                out.write(chunk, 0, produced);
            }
            return out.toByteArray();
        } finally {
            deflater.end();
        }
    }

    /**
     * Inflates exactly one tile's worth of cells, or null.
     *
     * <p>The output buffer is sized from the format constant rather than from
     * anything in the stream, and a stream that wants to produce more is
     * rejected -- so an oversized or hostile blob costs one fixed allocation
     * and nothing else.
     */
    private static byte[] inflate(byte[] blob, int offset, int length) {
        Inflater inflater = new Inflater();
        try {
            inflater.setInput(blob, offset, length);

            byte[] cells = new byte[LodTileKey.TILE_BYTES];
            int written = 0;
            while (written < cells.length) {
                int produced = inflater.inflate(cells, written, cells.length - written);
                if (produced <= 0) {
                    // Needing more input or a dictionary means the stream ended
                    // early; either way this is not a whole tile.
                    break;
                }
                written += produced;
            }

            if (written != cells.length || !inflater.finished()) {
                return null;
            }
            return cells;
        } catch (DataFormatException corrupt) {
            return null;
        } finally {
            inflater.end();
        }
    }

    // ---- primitives ----------------------------------------------------

    static int writeInt(byte[] target, int offset, int value) {
        target[offset] = (byte) (value >>> 24);
        target[offset + 1] = (byte) (value >>> 16);
        target[offset + 2] = (byte) (value >>> 8);
        target[offset + 3] = (byte) value;
        return offset + 4;
    }

    static int readInt(byte[] source, int offset) {
        return ((source[offset] & 0xFF) << 24)
                | ((source[offset + 1] & 0xFF) << 16)
                | ((source[offset + 2] & 0xFF) << 8)
                | (source[offset + 3] & 0xFF);
    }

    static int writeLong(byte[] target, int offset, long value) {
        for (int i = 0; i < 8; i++) {
            target[offset + i] = (byte) (value >>> (56 - 8 * i));
        }
        return offset + 8;
    }

    static long readLong(byte[] source, int offset) {
        long value = 0L;
        for (int i = 0; i < 8; i++) {
            value = (value << 8) | (source[offset + i] & 0xFF);
        }
        return value;
    }
}
