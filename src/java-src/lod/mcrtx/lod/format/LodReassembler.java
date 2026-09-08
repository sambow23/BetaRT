package mcrtx.lod.format;

/**
 * Rebuilds messages from the fragments {@link LodFragmenter} produced.
 *
 * <p>Fragments arrive in order over a single connection in practice, but nothing
 * in the design requires it and the reassembler does not assume it: a fragment
 * is filed by its index and a message completes when every index has arrived.
 *
 * <p><b>Everything here is bounded.</b> The fragments come off a network socket,
 * so a stream that is never completed -- a server that stops mid-tile, a
 * malformed or hostile sender -- must cost a fixed amount and then stop costing
 * anything. Concurrent streams are capped at {@link #MAX_PENDING_STREAMS} and a
 * new stream evicts the least recently touched one, so memory is capped at a few
 * hundred kilobytes no matter what arrives. Nothing is allocated from a length
 * the sender chose beyond that cap.
 *
 * <p>Not thread-safe. It belongs to whichever thread reads packets.
 */
public final class LodReassembler {
    /**
     * Streams reassembled at once.
     *
     * <p>The client asks for tiles a few at a time and a server answers them in
     * order, so more than a couple of streams in flight is already unusual;
     * sixteen is slack for reordering and for a server that interleaves.
     */
    public static final int MAX_PENDING_STREAMS = 16;

    /** One partially received message. */
    private static final class Stream {
        int streamId = -1;
        int kind;
        int fragmentCount;
        byte[][] payloads;
        int received;
        long touch;
    }

    private final Stream[] streams = new Stream[MAX_PENDING_STREAMS];
    private long clock;

    /** Kind of the message most recently completed by {@link #accept}. */
    private int lastCompletedKind = -1;

    public LodReassembler() {
        for (int i = 0; i < streams.length; i++) {
            streams[i] = new Stream();
        }
    }

    /**
     * Files one fragment.
     *
     * @return the complete message when this fragment finished one, otherwise
     *     null. A malformed fragment is dropped and reported as null rather than
     *     throwing: this runs on the packet path, where one bad packet must not
     *     take the session with it.
     */
    public byte[] accept(byte[] fragment) {
        if (!LodFragmenter.isWellFormed(fragment)) {
            return null;
        }

        int streamId = LodFragmenter.streamId(fragment);
        int kind = LodFragmenter.kind(fragment);
        int index = LodFragmenter.fragmentIndex(fragment);
        int count = LodFragmenter.fragmentCount(fragment);

        Stream stream = slotFor(streamId, kind, count);
        stream.touch = ++clock;

        // A repeat of a fragment already held is ignored rather than counted
        // twice, so a duplicated packet cannot complete a message early.
        if (stream.payloads[index] != null) {
            return null;
        }

        int payloadLength = LodFragmenter.payloadLength(fragment);
        byte[] payload = new byte[payloadLength];
        System.arraycopy(fragment, LodFragmenter.FRAGMENT_HEADER_BYTES, payload, 0, payloadLength);
        stream.payloads[index] = payload;
        stream.received++;

        if (stream.received < stream.fragmentCount) {
            return null;
        }

        int total = 0;
        for (byte[] part : stream.payloads) {
            total += part.length;
        }

        byte[] message = new byte[total];
        int offset = 0;
        for (byte[] part : stream.payloads) {
            System.arraycopy(part, 0, message, offset, part.length);
            offset += part.length;
        }

        lastCompletedKind = stream.kind;
        release(stream);
        return message;
    }

    /** Kind of the message the last successful {@link #accept} completed. */
    public int lastCompletedKind() {
        return lastCompletedKind;
    }

    /** Drops every partial stream. Used when a connection or world changes. */
    public void reset() {
        for (Stream stream : streams) {
            release(stream);
        }
        lastCompletedKind = -1;
    }

    /**
     * Finds the slot for a stream, starting it if new.
     *
     * <p>A stream id that arrives with a different kind or fragment count than
     * the slot holds is a new message reusing a wrapped id, not a continuation:
     * the old partial data is dropped. Without this, a stalled stream would
     * poison its id for as long as the session lasted.
     */
    private Stream slotFor(int streamId, int kind, int count) {
        for (Stream stream : streams) {
            if (stream.streamId == streamId) {
                if (stream.kind == kind && stream.fragmentCount == count) {
                    return stream;
                }
                release(stream);
                return begin(stream, streamId, kind, count);
            }
        }

        Stream victim = null;
        for (Stream stream : streams) {
            if (stream.streamId < 0) {
                victim = stream;
                break;
            }
            if (victim == null || stream.touch < victim.touch) {
                victim = stream;
            }
        }
        release(victim);
        return begin(victim, streamId, kind, count);
    }

    private Stream begin(Stream stream, int streamId, int kind, int count) {
        stream.streamId = streamId;
        stream.kind = kind;
        stream.fragmentCount = count;
        stream.payloads = new byte[count][];
        stream.received = 0;
        return stream;
    }

    private void release(Stream stream) {
        stream.streamId = -1;
        stream.kind = 0;
        stream.fragmentCount = 0;
        stream.payloads = null;
        stream.received = 0;
        stream.touch = 0L;
    }
}
