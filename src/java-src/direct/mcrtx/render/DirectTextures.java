package mcrtx.render;

import java.awt.image.BufferedImage;
import java.nio.IntBuffer;
import java.util.LinkedHashMap;
import java.util.Map;

/** Authoritative CPU images. Animated regions update these images before submission. */
public final class DirectTextures {
    public static final class Texture {
        public final long id = DirectRenderer.allocateId();
        public int width = 1, height = 1, filter, wrapU = 1, wrapV = 1;
        public byte[] rgba = {(byte)255, 0, (byte)255, (byte)255};
        public long revision;
        private boolean dirty = true, uploaded;
    }
    private static final Map<Integer, Texture> images = new LinkedHashMap<Integer, Texture>();
    private static int next = 1;
    private DirectTextures() { }
    public static int allocate() {
        if (next == Integer.MAX_VALUE) throw new IllegalStateException("Texture identifier overflow");
        int id = next++; images.put(id, new Texture()); return id;
    }
    public static void allocate(IntBuffer out) {
        for (int i=out.position(); i<out.limit(); ++i) out.put(i, allocate());
    }
    public static Texture get(int id) {
        if (id == 0) return null;
        Texture image = images.get(id);
        if (image == null) throw new IllegalArgumentException("Unknown texture " + id);
        upload(image); return image;
    }
    public static Texture pixels(int id) {
        Texture image = images.get(id);
        if (image == null) throw new IllegalArgumentException("Unknown texture " + id);
        return image;
    }
    public static void bind(int target, int id) {
        if (target != 3553) throw new IllegalArgumentException("Only two-dimensional textures are supported");
        DirectRenderer.texture = Math.max(0, id);
    }
    public static void image(int id, BufferedImage image, boolean blur, boolean clamp) {
        int w=image.getWidth(), h=image.getHeight();
        image(id, image.getRGB(0,0,w,h,null,0,w), w,h,blur,clamp);
    }
    public static void image(int id, int[] argb, int w, int h, boolean blur, boolean clamp) {
        if (w<=0 || h<=0 || w>16384 || h>16384 || (long)w*h>16777216 || argb.length<w*h)
            throw new IllegalArgumentException("Invalid texture dimensions");
        Texture image=pixels(id);
        image.width=w; image.height=h; image.rgba=new byte[w*h*4];
        for (int i=0; i<w*h; ++i) {
            int c=argb[i]; image.rgba[i*4]=(byte)(c>>16); image.rgba[i*4+1]=(byte)(c>>8);
            image.rgba[i*4+2]=(byte)c; image.rgba[i*4+3]=(byte)(c>>>24);
        }
        image.filter=blur?1:0; image.wrapU=image.wrapV=clamp?3:1; image.dirty=true;
        ++image.revision;
        DirectRenderer.textureStateChanged(id);
    }
    public static void region(int id, int x, int y, int w, int h, byte[] rgba) {
        Texture image=pixels(id);
        if (x<0 || y<0 || w<0 || h<0 || x>image.width-w || y>image.height-h || (long)w*h*4>rgba.length)
            throw new IllegalArgumentException("Texture update out of bounds");
        for (int row=0; row<h; ++row)
            System.arraycopy(rgba,row*w*4,image.rgba,((y+row)*image.width+x)*4,w*4);
        image.dirty=true;
        ++image.revision;
    }
    public static void parameter(int target, int parameter, int value) {
        if (target!=3553) throw new IllegalArgumentException("Invalid texture target");
        Texture image=pixels(DirectRenderer.texture);
        switch (parameter) {
        case 10240: case 10241: image.filter=(value==9729 || value==9985 || value==9987)?1:0; break;
        case 10242: image.wrapU=value==10497?1:3; break;
        case 10243: image.wrapV=value==10497?1:3; break;
        default: throw new IllegalArgumentException("Unsupported texture parameter " + parameter);
        }
        DirectRenderer.textureStateChanged(DirectRenderer.texture);
    }
    public static void delete(int id) {
        Texture image=images.remove(id);
        if (image==null) return;
        DirectRenderer.textureDeleted(id);
        if(image.uploaded){
            int at=DirectRenderer.resources.command(4); DirectRenderer.resources.id(image.id); DirectRenderer.resources.end(at);
        }
        if (DirectRenderer.texture==id) DirectRenderer.texture=0;
    }
    public static void delete(IntBuffer ids) {
        for (int i=ids.position(); i<ids.limit(); ++i) delete(ids.get(i));
    }
    private static void upload(Texture image) {
        if (!image.dirty) return;
        int at=DirectRenderer.resources.command(1);
        DirectRenderer.resources.id(image.id); DirectRenderer.resources.integer(image.width); DirectRenderer.resources.integer(image.height);
        DirectRenderer.resources.bytes(image.rgba); DirectRenderer.resources.end(at); image.dirty=false;image.uploaded=true;
    }
    public static void flush() { for (Texture image:images.values()) upload(image); }
    public static void reset() { images.clear(); next=1; }
}
