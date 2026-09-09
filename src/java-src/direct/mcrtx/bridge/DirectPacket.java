package mcrtx.bridge;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/** Reusable little-endian packet writer; growth never changes published native memory. */
public final class DirectPacket {
    private static final int LIMIT = 256 * 1024 * 1024;
    private ByteBuffer data = ByteBuffer.allocateDirect(4096).order(ByteOrder.LITTLE_ENDIAN);
    public DirectPacket() { clear(); }
    public void clear() { data.clear(); integer(0x54435244); integer(2); }
    public ByteBuffer buffer() { return data; }
    private void reserve(int count) {
        if (count < 0 || count > LIMIT - data.position()) throw new IllegalArgumentException("Submission too large");
        if (count <= data.remaining()) return;
        int capacity = data.capacity();
        while (capacity - data.position() < count) capacity = Math.min(LIMIT, capacity * 2);
        ByteBuffer next = ByteBuffer.allocateDirect(capacity).order(ByteOrder.LITTLE_ENDIAN);
        data.flip(); next.put(data); data = next;
    }
    public int command(int operation) {
        integer(operation);
        int offset = data.position(); integer(0);
        return offset;
    }
    public void end(int offset) {
        if (offset < 12 || offset + 4 > data.position()) throw new IllegalArgumentException("Invalid command offset");
        data.putInt(offset, data.position() - offset - 4);
    }
    public void integer(int value) { reserve(4); data.putInt(value); }
    public void id(long value) { reserve(8); data.putLong(value); }
    public void number(float value) {
        if (Float.isNaN(value) || Float.isInfinite(value)) throw new IllegalArgumentException("Non-finite geometry");
        reserve(4); data.putFloat(value);
    }
    public void number(double value) {
        if (Double.isNaN(value) || Double.isInfinite(value)) throw new IllegalArgumentException("Non-finite position");
        reserve(8); data.putDouble(value);
    }
    public void bytes(byte[] values) { reserve(values.length); data.put(values); }
}
