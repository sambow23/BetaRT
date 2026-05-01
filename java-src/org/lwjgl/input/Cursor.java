package org.lwjgl.input;

import java.nio.IntBuffer;
import org.lwjgl.LWJGLException;

public final class Cursor {
    private final int width;
    private final int height;
    private final int xHotspot;
    private final int yHotspot;
    private final int numImages;

    public Cursor(int width, int height, int xHotspot, int yHotspot, int numImages, IntBuffer images, IntBuffer delays)
            throws LWJGLException {
        if (width <= 0 || height <= 0) {
            throw new LWJGLException("Cursor dimensions must be positive");
        }
        if (xHotspot < 0 || xHotspot >= width || yHotspot < 0 || yHotspot >= height) {
            throw new LWJGLException("Cursor hotspot is out of bounds");
        }
        if (numImages <= 0) {
            throw new LWJGLException("Cursor must contain at least one image");
        }
        if (images == null) {
            throw new LWJGLException("Cursor image buffer is required");
        }

        this.width = width;
        this.height = height;
        this.xHotspot = xHotspot;
        this.yHotspot = yHotspot;
        this.numImages = numImages;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public int getXHotspot() {
        return xHotspot;
    }

    public int getYHotspot() {
        return yHotspot;
    }

    public int getNumImages() {
        return numImages;
    }

    public void destroy() {
    }
}