import java.nio.ByteBuffer;
import java.util.HashSet;
import java.util.Set;
import mcrtx.bridge.RemixBridgeNative;
import mcrtx.bridge.RemixUiBridge;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;

final class RemixUiTextureRegistry {
    private static final int GL_TEXTURE_BINDING_2D = 0x8069;
    private static final Set<Long> UPLOADED_TEXTURES = new HashSet<>();
    private static ByteBuffer textureReadBuffer;

    private RemixUiTextureRegistry() {
    }

    static long currentTextureId() {
        if (!GL11.glIsEnabled(GL11.GL_TEXTURE_2D)) {
            return 0L;
        }
        int glTextureId = GL11.glGetInteger(GL_TEXTURE_BINDING_2D);
        if (glTextureId <= 0 || !ensureUploaded(glTextureId)) {
            return 0L;
        }
        return glTextureId & 0xFFFFFFFFL;
    }

    static boolean ensureUploadedById(int glTextureId) {
        long key = glTextureId & 0xFFFFFFFFL;
        if (UPLOADED_TEXTURES.contains(key)) {
            return true;
        }
        int previous = GL11.glGetInteger(GL_TEXTURE_BINDING_2D);
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, glTextureId);
        boolean ok = ensureUploaded(glTextureId);
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, previous);
        return ok;
    }

    static boolean ensureUploaded(int glTextureId) {
        long key = glTextureId & 0xFFFFFFFFL;
        if (UPLOADED_TEXTURES.contains(key)) {
            return true;
        }
        int width = GL11.glGetTexLevelParameteri(GL11.GL_TEXTURE_2D, 0, GL11.GL_TEXTURE_WIDTH);
        int height = GL11.glGetTexLevelParameteri(GL11.GL_TEXTURE_2D, 0, GL11.GL_TEXTURE_HEIGHT);
        if (width <= 0 || height <= 0) {
            return false;
        }
        int capacity = width * height * 4;
        if (textureReadBuffer == null || textureReadBuffer.capacity() < capacity) {
            textureReadBuffer = BufferUtils.createByteBuffer(capacity);
        } else {
            textureReadBuffer.clear();
        }
        GL11.glGetTexImage(
                GL11.GL_TEXTURE_2D,
                0,
                GL11.GL_RGBA,
                GL11.GL_UNSIGNED_BYTE,
                textureReadBuffer);
        textureReadBuffer.rewind();
        boolean ok = RemixUiBridge.registerUiTexture(
                textureReadBuffer,
                key,
                width,
                height,
                RemixBridgeNative.SCREEN_OVERLAY_FORMAT_RGBA8);
        if (ok) {
            UPLOADED_TEXTURES.add(key);
        }
        return ok;
    }

    static void reset() {
        UPLOADED_TEXTURES.clear();
    }
}
