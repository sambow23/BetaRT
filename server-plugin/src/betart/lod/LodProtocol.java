package betart.lod;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.Deflater;

/**
 * Wire format for distant-terrain LOD, carried over Packet131MapData.
 *
 * <p>Beta 1.7.3 has no plugin-message channel, but Packet131MapData is a
 * general byte[] carrier that vanilla clients handle safely: the client checks
 * the item id against the filled-map item and, on anything else, prints
 * "Unknown itemid:" and returns. So a server can stream on this packet without
 * disconnecting or crashing clients that know nothing about it.
 *
 * <p>The catch is size. The packet reads its payload length as a single byte
 * masked to 0xFF, so <b>255 bytes is the hard per-packet ceiling</b>. Regions
 * are therefore compressed and split into fragments that the client reassembles.
 *
 * <p>The payload is deliberately thin: five bytes per column, carrying block id,
 * metadata and biome rather than atlas tiles or resolved colours. The client
 * already knows how to turn a block id and metadata into a texture tile without
 * touching the world, so sending resolved appearance would both waste bandwidth
 * and couple the protocol to one client's texture atlas.
 */
public final class LodProtocol {
    /**
     * Item id stamped on every LOD packet. Anything other than the filled-map
     * item (358) is ignored by vanilla clients; this value identifies the
     * stream as ours.
     */
    public static final short ITEM_ID = 0x4254;  // 'BT'

    public static final byte VERSION = 1;

    /** Message kinds, carried in the fragment header. */
    public static final byte KIND_HELLO = 0;
    public static final byte KIND_REGION = 1;

    /** Packet131MapData length field is one byte. */
    public static final int MAX_PACKET_PAYLOAD = 255;

    /**
     * Fragment header: version, kind, stream id, fragment index, fragment count.
     * Kept to five bytes so the useful payload stays a round 250.
     */
    public static final int FRAGMENT_HEADER_BYTES = 5;
    public static final int FRAGMENT_PAYLOAD_BYTES = MAX_PACKET_PAYLOAD - FRAGMENT_HEADER_BYTES;

    /** Columns along one edge of a region, matching the client. */
    public static final int REGION_COLUMNS = 32;
    public static final int COLUMNS_PER_REGION = REGION_COLUMNS * REGION_COLUMNS;

    /** topY, blockId, meta, waterY, biomeId. */
    public static final int BYTES_PER_COLUMN = 5;
    public static final int REGION_BODY_BYTES = COLUMNS_PER_REGION * BYTES_PER_COLUMN;

    /** Height sentinel meaning "no surface of this kind in the column". */
    public static final int NO_SURFACE = 0xFF;

    private LodProtocol() {
    }

    /**
     * Builds the uncompressed region body that {@link #fragmentRegion} compresses.
     * Column order is row-major in Z then X, matching the client's grid.
     */
    public static byte[] encodeRegionBody(
            int[] topY, int[] blockId, int[] meta, int[] waterY, int[] biomeId) {
        byte[] body = new byte[REGION_BODY_BYTES];
        int offset = 0;
        for (int i = 0; i < COLUMNS_PER_REGION; i++) {
            body[offset++] = (byte) clampHeight(topY[i]);
            body[offset++] = (byte) (blockId[i] & 0xFF);
            body[offset++] = (byte) (meta[i] & 0x0F);
            body[offset++] = (byte) clampHeight(waterY[i]);
            body[offset++] = (byte) (biomeId[i] & 0xFF);
        }
        return body;
    }

    private static int clampHeight(int y) {
        if (y < 0 || y > 127) {
            return NO_SURFACE;
        }
        return y;
    }

    /**
     * Compresses a region body and splits it into wire fragments.
     *
     * <p>Each returned array is a complete packet payload, at most
     * {@link #MAX_PACKET_PAYLOAD} bytes. The region's coordinates ride in the
     * first fragment's body so the header stays fixed-size.
     */
    public static List<byte[]> fragmentRegion(
            int streamId, int originX, int originZ, int step, byte[] regionBody) {
        byte[] header = new byte[10];
        writeInt(header, 0, originX);
        writeInt(header, 4, originZ);
        header[8] = (byte) step;
        header[9] = VERSION;

        byte[] uncompressed = new byte[header.length + regionBody.length];
        System.arraycopy(header, 0, uncompressed, 0, header.length);
        System.arraycopy(regionBody, 0, uncompressed, header.length, regionBody.length);

        byte[] compressed = deflate(uncompressed);

        // The client needs the uncompressed length to size its inflate buffer,
        // so it leads the reassembled stream.
        byte[] framed = new byte[4 + compressed.length];
        writeInt(framed, 0, uncompressed.length);
        System.arraycopy(compressed, 0, framed, 4, compressed.length);

        int fragmentCount = (framed.length + FRAGMENT_PAYLOAD_BYTES - 1) / FRAGMENT_PAYLOAD_BYTES;
        if (fragmentCount > 255) {
            // Would not fit the one-byte fragment counter. A deflated region is
            // far below this, so hitting it means the body grew unexpectedly.
            return new ArrayList<byte[]>();
        }

        List<byte[]> fragments = new ArrayList<byte[]>(fragmentCount);
        for (int index = 0; index < fragmentCount; index++) {
            int start = index * FRAGMENT_PAYLOAD_BYTES;
            int length = Math.min(FRAGMENT_PAYLOAD_BYTES, framed.length - start);

            byte[] fragment = new byte[FRAGMENT_HEADER_BYTES + length];
            fragment[0] = VERSION;
            fragment[1] = KIND_REGION;
            fragment[2] = (byte) (streamId & 0xFF);
            fragment[3] = (byte) index;
            fragment[4] = (byte) fragmentCount;
            System.arraycopy(framed, start, fragment, FRAGMENT_HEADER_BYTES, length);
            fragments.add(fragment);
        }
        return fragments;
    }

    /** Single-fragment announcement so clients can tell a LOD server from a vanilla one. */
    public static byte[] helloFragment() {
        return new byte[] {VERSION, KIND_HELLO, 0, 0, 1};
    }

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

    private static void writeInt(byte[] target, int offset, int value) {
        target[offset] = (byte) (value >>> 24);
        target[offset + 1] = (byte) (value >>> 16);
        target[offset + 2] = (byte) (value >>> 8);
        target[offset + 3] = (byte) value;
    }
}
