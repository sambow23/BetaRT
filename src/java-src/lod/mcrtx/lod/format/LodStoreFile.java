package mcrtx.lod.format;

import java.io.UnsupportedEncodingException;

/**
 * Header of a LOD cache file: what world it describes and what it contains.
 *
 * <p>A file is this header followed by a run of tiles, each encoded by
 * {@link LodTileCodec} and each self-delimiting, so a reader can walk the file
 * without an index and skip tiles it does not want without inflating them.
 *
 * <p>The header exists to make a file <b>safe to accept from someone else</b>.
 * A shared cache is the point of the format, which means a file may have been
 * written by another install, another version, or another producer entirely, and
 * the reader has to be able to say so before it trusts a single tile. It carries
 * the world identity to check against, the format version to refuse mismatches,
 * a human label so a person can tell files apart, and the set of levels present
 * so a reader knows whether coarse tiles are baked in or must be derived.
 *
 * <p>The level mask matters more than it looks. A file baked by the offline tool
 * may contain every level; one accumulated by a client while playing may hold
 * only the canonical level, with everything coarser derived at load. Recording
 * which is which turns "are these tiles missing or was this file simply written
 * that way" into a fact rather than a guess.
 */
public final class LodStoreFile {
    /** File magic, 'B' 'T' 'L' 'D'. */
    public static final int MAGIC = 0x42544C44;

    /**
     * Container version, separate from {@link LodTileCodec#VERSION}.
     *
     * <p>Two versions because they change for different reasons: the container
     * changes when the file's framing does, the tile codec when a cell's meaning
     * does. A reader that understands the container can report a tile-version
     * mismatch usefully instead of rejecting the file as unreadable.
     */
    public static final int VERSION = 1;

    /** Longest label stored, in bytes of UTF-8. */
    public static final int MAX_LABEL_BYTES = 255;

    /** magic, container version, tile version, seed, dimension, level mask, tile count, label length. */
    private static final int FIXED_HEADER_BYTES = 4 + 1 + 1 + 8 + 4 + 1 + 4 + 1;

    private final LodWorldId worldId;
    private final String label;
    private final int levelMask;
    private final int tileCount;
    private final int tileCodecVersion;

    public LodStoreFile(
            LodWorldId worldId, String label, int levelMask, int tileCount, int tileCodecVersion) {
        this.worldId = worldId;
        this.label = label == null ? "" : label;
        this.levelMask = levelMask;
        this.tileCount = tileCount;
        this.tileCodecVersion = tileCodecVersion;
    }

    public LodWorldId worldId() {
        return worldId;
    }

    /** Human-readable name for the world; never compared, only shown. */
    public String label() {
        return label;
    }

    /** Bit per level present in the file; bit {@code n} is level {@code n}. */
    public int levelMask() {
        return levelMask;
    }

    public boolean hasLevel(int level) {
        return level >= 0 && level <= LodTileKey.MAX_LEVEL && (levelMask & (1 << level)) != 0;
    }

    public int tileCount() {
        return tileCount;
    }

    public int tileCodecVersion() {
        return tileCodecVersion;
    }

    /** True when this file's tiles can be read by the running build. */
    public boolean isReadable() {
        return tileCodecVersion == LodTileCodec.VERSION;
    }

    /** Bytes this header occupies, so a reader knows where the first tile starts. */
    public int headerBytes() {
        return FIXED_HEADER_BYTES + labelBytes().length;
    }

    public byte[] encode() {
        byte[] labelBytes = labelBytes();
        byte[] header = new byte[FIXED_HEADER_BYTES + labelBytes.length];

        int offset = 0;
        offset = LodTileCodec.writeInt(header, offset, MAGIC);
        header[offset++] = (byte) VERSION;
        header[offset++] = (byte) tileCodecVersion;
        offset = LodTileCodec.writeLong(header, offset, worldId.seed());
        offset = LodTileCodec.writeInt(header, offset, worldId.dimension());
        header[offset++] = (byte) levelMask;
        offset = LodTileCodec.writeInt(header, offset, tileCount);
        header[offset++] = (byte) labelBytes.length;
        System.arraycopy(labelBytes, 0, header, offset, labelBytes.length);
        return header;
    }

    /**
     * Reads a header from the front of a file.
     *
     * @return the header, or null when the bytes are not a LOD cache file or are
     *     truncated. As with tiles, an unreadable file is an expected event --
     *     shared files arrive from anywhere -- so it is reported, not thrown.
     */
    public static LodStoreFile decode(byte[] blob) {
        if (blob == null || blob.length < FIXED_HEADER_BYTES) {
            return null;
        }
        if (LodTileCodec.readInt(blob, 0) != MAGIC) {
            return null;
        }

        int offset = 4;
        int containerVersion = blob[offset++] & 0xFF;
        if (containerVersion != VERSION) {
            return null;
        }

        int tileCodecVersion = blob[offset++] & 0xFF;
        long seed = LodTileCodec.readLong(blob, offset);
        offset += 8;
        int dimension = LodTileCodec.readInt(blob, offset);
        offset += 4;
        int levelMask = blob[offset++] & 0xFF;
        int tileCount = LodTileCodec.readInt(blob, offset);
        offset += 4;
        int labelLength = blob[offset++] & 0xFF;

        if (tileCount < 0 || offset + labelLength > blob.length) {
            return null;
        }

        String label;
        try {
            label = new String(blob, offset, labelLength, "UTF-8");
        } catch (UnsupportedEncodingException impossible) {
            label = "";
        }

        return new LodStoreFile(
                new LodWorldId(seed, dimension), label, levelMask, tileCount, tileCodecVersion);
    }

    /** Encodes the label, truncating on a character boundary if it is too long. */
    private byte[] labelBytes() {
        byte[] encoded;
        try {
            encoded = label.getBytes("UTF-8");
        } catch (UnsupportedEncodingException impossible) {
            return new byte[0];
        }
        if (encoded.length <= MAX_LABEL_BYTES) {
            return encoded;
        }

        // Back off to a boundary rather than splitting a multi-byte character,
        // which would leave the label undecodable.
        int end = MAX_LABEL_BYTES;
        while (end > 0 && (encoded[end] & 0xC0) == 0x80) {
            end--;
        }
        byte[] truncated = new byte[end];
        System.arraycopy(encoded, 0, truncated, 0, end);
        return truncated;
    }
}
