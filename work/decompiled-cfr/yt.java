/*
 * Decompiled with CFR 0.152.
 */
final class yt {
    private final String a;

    yt(String string) {
        this.a = string.replace("\\", "\\\\").replace("\"", "\\\"").replace("\b", "\\b").replace("\f", "\\f").replace("\n", "\\n").replace("\r", "\\r").replace("\t", "\\t");
    }

    public String toString() {
        return this.a;
    }
}

