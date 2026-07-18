package mcrtx.bridge;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import mcrtx.lwjglshim.OpenGlCompat;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.EXTFramebufferObject;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;
import org.lwjgl.opengl.GL14;

public final class UiOverlayCapture {
    private static final IntBuffer VIEWPORT_BUFFER = BufferUtils.createIntBuffer(16);

    // GL15/GL21 pixel-buffer-object enums, resolved through OpenGlCompat's
    // reflective bindings rather than linked directly so the LWJGL2 compile /
    // LWJGL3 runtime split can't break the build.
    private static final int GL_PIXEL_PACK_BUFFER = 0x88EB;
    private static final int GL_STREAM_READ = 0x88E1;
    private static final int PBO_COUNT = 2;

    private static int framebufferId;
    private static int colorTextureId;
    private static int depthRenderbufferId;
    private static int captureWidth;
    private static int captureHeight;
    private static int previousFramebufferId;
    private static int viewportX;
    private static int viewportY;
    private static int viewportWidth;
    private static int viewportHeight;
    private static boolean captureActive;
    private static ByteBuffer pixelBuffer;
    private static byte[] rowScratch;

    // Double-buffered async readback. glReadPixels into pboIds[pboWriteIndex]
    // is a non-blocking DMA; the PBO filled on a previous frame
    // (pendingPboReadIndex) is transferred to the CPU without stalling on the
    // GPU. pendingPboReadIndex == -1 means no result is ready yet.
    private static final int[] pboIds = new int[PBO_COUNT];
    private static boolean pboReadbackEnabled;
    private static int pboWriteIndex;
    private static int pendingPboReadIndex = -1;

    private UiOverlayCapture() {
    }

    public static void begin(int width, int height) {
        if (!RemixLifecycleBridge.isInitialized()) {
            return;
        }
        if (width <= 0 || height <= 0) {
            return;
        }
        if (captureActive) {
            return;
        }
        if (!ensureResources(width, height)) {
            RemixUiBridge.clearScreenOverlay();
            return;
        }

        previousFramebufferId = GL11.glGetInteger(EXTFramebufferObject.GL_FRAMEBUFFER_BINDING_EXT);
        VIEWPORT_BUFFER.clear();
        if (!OpenGlCompat.getInteger(GL11.GL_VIEWPORT, VIEWPORT_BUFFER)) {
            RemixUiBridge.clearScreenOverlay();
            return;
        }
        viewportX = VIEWPORT_BUFFER.get(0);
        viewportY = VIEWPORT_BUFFER.get(1);
        viewportWidth = VIEWPORT_BUFFER.get(2);
        viewportHeight = VIEWPORT_BUFFER.get(3);

        EXTFramebufferObject.glBindFramebufferEXT(EXTFramebufferObject.GL_FRAMEBUFFER_EXT, framebufferId);
        GL11.glViewport(0, 0, captureWidth, captureHeight);
        GL11.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
        GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
        captureActive = true;
    }

    public static void end() {
        if (!RemixLifecycleBridge.isInitialized()) {
            captureActive = false;
            return;
        }

        if (!captureActive) {
            RemixUiBridge.clearScreenOverlay();
            return;
        }

        if (pboReadbackEnabled && readbackThroughPbo()) {
            return;
        }

        // Synchronous fallback: glReadPixels blocks the CPU until the GPU has
        // finished the UI framebuffer, then copies straight into pixelBuffer.
        pixelBuffer.clear();
        GL11.glReadPixels(0, 0, captureWidth, captureHeight, GL12.GL_BGRA, GL11.GL_UNSIGNED_BYTE, pixelBuffer);
        flipRowsInPlace();
        pixelBuffer.rewind();

        EXTFramebufferObject.glBindFramebufferEXT(EXTFramebufferObject.GL_FRAMEBUFFER_EXT, previousFramebufferId);
        GL11.glViewport(viewportX, viewportY, viewportWidth, viewportHeight);
        captureActive = false;

        RemixUiBridge.drawScreenOverlay(
                pixelBuffer,
                captureWidth,
                captureHeight,
                RemixBridgeNative.SCREEN_OVERLAY_FORMAT_BGRA8,
                1.0f);
    }

    /**
     * Issues a non-blocking readback of the current frame into one PBO and
     * transfers the PBO filled on a previous frame, so the CPU never stalls
     * waiting for the GPU. The overlay trails the scene by one frame, which is
     * imperceptible at interactive frame rates.
     *
     * @return true if this path handled the frame, including restoring the
     *         framebuffer/viewport; false if it failed and the caller should
     *         use the synchronous readback (the framebuffer is left bound).
     */
    private static boolean readbackThroughPbo() {
        try {
            final int writeIndex = pboWriteIndex;
            final int readIndex = pendingPboReadIndex;

            OpenGlCompat.bindBuffer(GL_PIXEL_PACK_BUFFER, pboIds[writeIndex]);
            OpenGlCompat.readPixelsToBoundBuffer(
                    0, 0, captureWidth, captureHeight, GL12.GL_BGRA, GL11.GL_UNSIGNED_BYTE, 0L);

            boolean haveFrame = false;
            if (readIndex >= 0) {
                OpenGlCompat.bindBuffer(GL_PIXEL_PACK_BUFFER, pboIds[readIndex]);
                pixelBuffer.clear();
                OpenGlCompat.getBufferSubData(GL_PIXEL_PACK_BUFFER, 0L, pixelBuffer);
                flipRowsInPlace();
                pixelBuffer.rewind();
                haveFrame = true;
            }
            OpenGlCompat.bindBuffer(GL_PIXEL_PACK_BUFFER, 0);

            EXTFramebufferObject.glBindFramebufferEXT(EXTFramebufferObject.GL_FRAMEBUFFER_EXT, previousFramebufferId);
            GL11.glViewport(viewportX, viewportY, viewportWidth, viewportHeight);
            captureActive = false;

            pendingPboReadIndex = writeIndex;
            pboWriteIndex = (writeIndex + 1) % PBO_COUNT;

            if (haveFrame) {
                RemixUiBridge.drawScreenOverlay(
                        pixelBuffer,
                        captureWidth,
                        captureHeight,
                        RemixBridgeNative.SCREEN_OVERLAY_FORMAT_BGRA8,
                        1.0f);
            }
            return true;
        } catch (RuntimeException exception) {
            // Unbind the pack buffer so the synchronous fallback reads into
            // pixelBuffer rather than an offset, then disable the async path.
            disablePixelBufferReadback();
            return false;
        }
    }

    public static void reset() {
        captureActive = false;
        destroyResources();
    }

    private static boolean ensureResources(int width, int height) {
        if (framebufferId != 0 && captureWidth == width && captureHeight == height && pixelBuffer != null) {
            return true;
        }

        destroyResources();

        final int previousFramebuffer = GL11.glGetInteger(EXTFramebufferObject.GL_FRAMEBUFFER_BINDING_EXT);
        final int previousTexture = GL11.glGetInteger(GL11.GL_TEXTURE_BINDING_2D);
        final int previousRenderbuffer = GL11.glGetInteger(EXTFramebufferObject.GL_RENDERBUFFER_BINDING_EXT);

        framebufferId = EXTFramebufferObject.glGenFramebuffersEXT();
        colorTextureId = GL11.glGenTextures();
        depthRenderbufferId = EXTFramebufferObject.glGenRenderbuffersEXT();

        GL11.glBindTexture(GL11.GL_TEXTURE_2D, colorTextureId);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL12.GL_CLAMP_TO_EDGE);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL12.GL_CLAMP_TO_EDGE);
        GL11.glTexImage2D(
                GL11.GL_TEXTURE_2D,
                0,
                GL11.GL_RGBA8,
                width,
                height,
                0,
                GL11.GL_RGBA,
                GL11.GL_UNSIGNED_BYTE,
                (ByteBuffer) null);

        EXTFramebufferObject.glBindRenderbufferEXT(EXTFramebufferObject.GL_RENDERBUFFER_EXT, depthRenderbufferId);
        EXTFramebufferObject.glRenderbufferStorageEXT(
                EXTFramebufferObject.GL_RENDERBUFFER_EXT,
                GL14.GL_DEPTH_COMPONENT24,
                width,
                height);

        EXTFramebufferObject.glBindFramebufferEXT(EXTFramebufferObject.GL_FRAMEBUFFER_EXT, framebufferId);
        EXTFramebufferObject.glFramebufferTexture2DEXT(
                EXTFramebufferObject.GL_FRAMEBUFFER_EXT,
                EXTFramebufferObject.GL_COLOR_ATTACHMENT0_EXT,
                GL11.GL_TEXTURE_2D,
                colorTextureId,
                0);
        EXTFramebufferObject.glFramebufferRenderbufferEXT(
                EXTFramebufferObject.GL_FRAMEBUFFER_EXT,
                EXTFramebufferObject.GL_DEPTH_ATTACHMENT_EXT,
                EXTFramebufferObject.GL_RENDERBUFFER_EXT,
                depthRenderbufferId);

        final int framebufferStatus = EXTFramebufferObject.glCheckFramebufferStatusEXT(EXTFramebufferObject.GL_FRAMEBUFFER_EXT);

        EXTFramebufferObject.glBindFramebufferEXT(EXTFramebufferObject.GL_FRAMEBUFFER_EXT, previousFramebuffer);
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, previousTexture);
        EXTFramebufferObject.glBindRenderbufferEXT(EXTFramebufferObject.GL_RENDERBUFFER_EXT, previousRenderbuffer);

        if (framebufferStatus != EXTFramebufferObject.GL_FRAMEBUFFER_COMPLETE_EXT) {
            destroyResources();
            return false;
        }

        pixelBuffer = BufferUtils.createByteBuffer(width * height * 4);
        rowScratch = new byte[width * 4];
        captureWidth = width;
        captureHeight = height;
        allocatePixelReadbackBuffers(width, height);
        return true;
    }

    private static void flipRowsInPlace() {
        if (pixelBuffer == null || captureWidth <= 0 || captureHeight <= 1) {
            return;
        }

        final int rowStride = captureWidth * 4;
        if (rowScratch == null || rowScratch.length != rowStride) {
            rowScratch = new byte[rowStride];
        }
        final byte[] bottomScratch = new byte[rowStride];

        // Bulk ByteBuffer transfers; per-byte get/put on a direct buffer is
        // roughly 10x slower than a single bulk copy.
        for (int topRow = 0, bottomRow = captureHeight - 1; topRow < bottomRow; topRow += 1, bottomRow -= 1) {
            final int topOffset = topRow * rowStride;
            final int bottomOffset = bottomRow * rowStride;

            pixelBuffer.position(topOffset);
            pixelBuffer.get(rowScratch, 0, rowStride);
            pixelBuffer.position(bottomOffset);
            pixelBuffer.get(bottomScratch, 0, rowStride);
            pixelBuffer.position(topOffset);
            pixelBuffer.put(bottomScratch, 0, rowStride);
            pixelBuffer.position(bottomOffset);
            pixelBuffer.put(rowScratch, 0, rowStride);
        }
        pixelBuffer.rewind();
    }

    private static void allocatePixelReadbackBuffers(int width, int height) {
        releasePixelReadbackBuffers();
        if (!OpenGlCompat.isPixelBufferReadbackSupported()) {
            return;
        }
        try {
            final long byteCount = (long) width * height * 4;
            for (int i = 0; i < PBO_COUNT; i += 1) {
                pboIds[i] = OpenGlCompat.genBuffer();
                OpenGlCompat.bindBuffer(GL_PIXEL_PACK_BUFFER, pboIds[i]);
                OpenGlCompat.bufferData(GL_PIXEL_PACK_BUFFER, byteCount, GL_STREAM_READ);
            }
            OpenGlCompat.bindBuffer(GL_PIXEL_PACK_BUFFER, 0);
            pboReadbackEnabled = true;
        } catch (RuntimeException exception) {
            disablePixelBufferReadback();
        }
    }

    private static void disablePixelBufferReadback() {
        try {
            OpenGlCompat.bindBuffer(GL_PIXEL_PACK_BUFFER, 0);
        } catch (RuntimeException ignored) {
            // The pack buffer binding is best-effort during teardown.
        }
        releasePixelReadbackBuffers();
    }

    private static void releasePixelReadbackBuffers() {
        for (int i = 0; i < PBO_COUNT; i += 1) {
            if (pboIds[i] != 0) {
                try {
                    OpenGlCompat.deleteBuffer(pboIds[i]);
                } catch (RuntimeException ignored) {
                    // Deleting an already-lost buffer is harmless.
                }
                pboIds[i] = 0;
            }
        }
        pboReadbackEnabled = false;
        pboWriteIndex = 0;
        pendingPboReadIndex = -1;
    }

    private static void destroyResources() {
        releasePixelReadbackBuffers();
        if (framebufferId != 0) {
            EXTFramebufferObject.glDeleteFramebuffersEXT(framebufferId);
            framebufferId = 0;
        }
        if (colorTextureId != 0) {
            GL11.glDeleteTextures(colorTextureId);
            colorTextureId = 0;
        }
        if (depthRenderbufferId != 0) {
            EXTFramebufferObject.glDeleteRenderbuffersEXT(depthRenderbufferId);
            depthRenderbufferId = 0;
        }
        pixelBuffer = null;
        rowScratch = null;
        captureWidth = 0;
        captureHeight = 0;
    }
}
