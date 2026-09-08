package mcrtx.lod.format;

import java.util.ArrayList;
import java.util.List;

/**
 * Splits a message into packet-sized fragments, and defines the wire framing.
 *
 * <p>Beta 1.7.3 has no plugin-message channel. The one general byte carrier that
 * a vanilla client tolerates is the map-data packet: it holds a {@code short}
 * item id and a {@code byte[]}, and a client that does not recognise the item id
 * prints a line and returns rather than disconnecting. That makes it safe to
 * stream on, and it is why {@link #ITEM_ID} is stamped on every fragment -- it
 * identifies the stream as ours to clients that know it, and marks it as
 * ignorable to clients that do not.
 *
 * <p>The cost is size. That packet writes its payload length as a single byte,
 * so <b>255 bytes is a hard ceiling per packet</b> and anything larger has to be
 * carried in pieces. A tile is one to two kilobytes encoded, which is five to
 * nine fragments.
 *
 * <p>Fragments carry no coordinates. Everything needed to place a tile is
 * already in the encoded tile's own header, so the framing stays fixed-size and
 * the same fragmenter carries requests, tiles and anything added later.
 */
public final class LodFragmenter {
    /**
     * Item id stamped on every LOD packet.
     *
     * <p>Anything other than the filled-map item is ignored by vanilla clients;
     * this value ('BT') identifies the stream as BetaRT's.
     */
    public static final short ITEM_ID = 0x4254;

    public static final byte VERSION = 1;

    /** Message kinds carried in the fragment header. */
    public static final byte KIND_HELLO = 0;
    public static final byte KIND_TILE = 1;

    /** The map-data packet's length field is one byte. */
    public static final int MAX_PACKET_PAYLOAD = 255;

    /** version, kind, stream id, fragment index, fragment count. */
    public static final int FRAGMENT_HEADER_BYTES = 5;

    /** Useful payload per fragment, kept a round number by the header size above. */
    public static final int FRAGMENT_PAYLOAD_BYTES = MAX_PACKET_PAYLOAD - FRAGMENT_HEADER_BYTES;

    /** A message may not exceed what a one-byte fragment counter can address. */
    public static final int MAX_FRAGMENTS = 255;
    public static final int MAX_MESSAGE_BYTES = MAX_FRAGMENTS * FRAGMENT_PAYLOAD_BYTES;

    private LodFragmenter() {
    }

    /**
     * Splits a message into complete packet payloads.
     *
     * @return one entry per packet, each at most {@link #MAX_PACKET_PAYLOAD}
     *     bytes, or an empty list when the message cannot be framed.
     */
    public static List<byte[]> fragment(byte kind, int streamId, byte[] message) {
        List<byte[]> fragments = new ArrayList<byte[]>();
        if (message == null || message.length > MAX_MESSAGE_BYTES) {
            return fragments;
        }

        int fragmentCount = (message.length + FRAGMENT_PAYLOAD_BYTES - 1) / FRAGMENT_PAYLOAD_BYTES;
        if (fragmentCount == 0) {
            fragmentCount = 1;
        }

        for (int index = 0; index < fragmentCount; index++) {
            int start = index * FRAGMENT_PAYLOAD_BYTES;
            int length = Math.min(FRAGMENT_PAYLOAD_BYTES, message.length - start);
            if (length < 0) {
                length = 0;
            }

            byte[] fragment = new byte[FRAGMENT_HEADER_BYTES + length];
            fragment[0] = VERSION;
            fragment[1] = kind;
            fragment[2] = (byte) (streamId & 0xFF);
            fragment[3] = (byte) index;
            fragment[4] = (byte) fragmentCount;
            System.arraycopy(message, start, fragment, FRAGMENT_HEADER_BYTES, length);
            fragments.add(fragment);
        }
        return fragments;
    }

    /** Splits an encoded tile. Convenience over {@link #fragment}. */
    public static List<byte[]> fragmentTile(int streamId, byte[] encodedTile) {
        return fragment(KIND_TILE, streamId, encodedTile);
    }

    /**
     * Single-fragment announcement, so a client can tell a LOD server from one
     * that has never heard of the protocol.
     */
    public static byte[] helloFragment() {
        return new byte[] {VERSION, KIND_HELLO, 0, 0, 1};
    }

    // ---- fragment header accessors -------------------------------------

    public static boolean isWellFormed(byte[] fragment) {
        return fragment != null
                && fragment.length >= FRAGMENT_HEADER_BYTES
                && fragment.length <= MAX_PACKET_PAYLOAD
                && fragment[0] == VERSION
                && (fragment[4] & 0xFF) > 0
                && (fragment[3] & 0xFF) < (fragment[4] & 0xFF);
    }

    public static int kind(byte[] fragment) {
        return fragment[1] & 0xFF;
    }

    public static int streamId(byte[] fragment) {
        return fragment[2] & 0xFF;
    }

    public static int fragmentIndex(byte[] fragment) {
        return fragment[3] & 0xFF;
    }

    public static int fragmentCount(byte[] fragment) {
        return fragment[4] & 0xFF;
    }

    public static int payloadLength(byte[] fragment) {
        return fragment.length - FRAGMENT_HEADER_BYTES;
    }
}
