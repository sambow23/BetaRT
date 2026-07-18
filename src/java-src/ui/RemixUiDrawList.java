final class RemixUiDrawList {
    static final class Checkpoint {
        final int vertexCount;
        final int commandCount;

        Checkpoint(int vertexCount, int commandCount) {
            this.vertexCount = vertexCount;
            this.commandCount = commandCount;
        }
    }

    private float[] xyzuv = new float[5 * 1024];
    private int[] colors = new int[1024];
    private int vertexCount;
    private long[] commandTextureIds = new long[256];
    private int[] commandQuadCounts = new int[256];
    private int[] commandFlags = new int[256];
    private int commandCount;

    int vertexCount() {
        return vertexCount;
    }

    int commandCount() {
        return commandCount;
    }

    boolean hasDraws() {
        return vertexCount > 0 && commandCount > 0;
    }

    void clear() {
        vertexCount = 0;
        commandCount = 0;
    }

    Checkpoint checkpoint() {
        return new Checkpoint(vertexCount, commandCount);
    }

    void rollback(Checkpoint checkpoint) {
        if (checkpoint == null) {
            clear();
            return;
        }
        vertexCount = checkpoint.vertexCount;
        commandCount = checkpoint.commandCount;
    }

    void ensureVertexCapacity(int requiredVertices) {
        if (requiredVertices <= colors.length) {
            return;
        }
        int grown = colors.length;
        while (grown < requiredVertices) {
            grown *= 2;
        }
        float[] newXyzuv = new float[grown * 5];
        int[] newColors = new int[grown];
        System.arraycopy(xyzuv, 0, newXyzuv, 0, vertexCount * 5);
        System.arraycopy(colors, 0, newColors, 0, vertexCount);
        xyzuv = newXyzuv;
        colors = newColors;
    }

    void appendVertex(float x, float y, float depth, float u, float v, int color) {
        ensureVertexCapacity(vertexCount + 1);
        int out = vertexCount * 5;
        xyzuv[out] = x;
        xyzuv[out + 1] = y;
        xyzuv[out + 2] = depth;
        xyzuv[out + 3] = u;
        xyzuv[out + 4] = v;
        colors[vertexCount] = color;
        vertexCount++;
    }

    void appendCommand(long textureId, int quadCount, int flags) {
        if (quadCount <= 0) {
            return;
        }
        if (commandCount > 0
                && commandTextureIds[commandCount - 1] == textureId
                && commandFlags[commandCount - 1] == flags) {
            commandQuadCounts[commandCount - 1] += quadCount;
            return;
        }
        if (commandCount == commandTextureIds.length) {
            int grown = commandTextureIds.length * 2;
            long[] newTextureIds = new long[grown];
            int[] newQuadCounts = new int[grown];
            int[] newFlags = new int[grown];
            System.arraycopy(commandTextureIds, 0, newTextureIds, 0, commandCount);
            System.arraycopy(commandQuadCounts, 0, newQuadCounts, 0, commandCount);
            System.arraycopy(commandFlags, 0, newFlags, 0, commandCount);
            commandTextureIds = newTextureIds;
            commandQuadCounts = newQuadCounts;
            commandFlags = newFlags;
        }
        commandTextureIds[commandCount] = textureId;
        commandQuadCounts[commandCount] = quadCount;
        commandFlags[commandCount] = flags;
        commandCount++;
    }

    float[] vertexXyzuv() {
        return xyzuv;
    }

    int[] vertexColors() {
        return colors;
    }

    long[] commandTextureIds() {
        return commandTextureIds;
    }

    int[] commandQuadCounts() {
        return commandQuadCounts;
    }

    int[] commandFlags() {
        return commandFlags;
    }
}
