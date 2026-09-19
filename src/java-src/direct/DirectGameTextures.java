import mcrtx.render.DirectTextures;
import mcrtx.render.DirectRenderer;
import java.awt.image.BufferedImage;

/** Texture loading stays in vanilla; storage and animation updates are CPU-owned. */
public final class DirectGameTextures {
    private DirectGameTextures() { }
    public static void upload(ji manager,BufferedImage image,int id) {
        DirectTextures.pixels(id).pbrLoaded=false;
        DirectTextures.image(id,image,manager.k,manager.j);
    }
    public static void uploadPixels(ji manager,int[] pixels,int width,int height,int id) {
        DirectTextures.pixels(id).pbrLoaded=false;
        DirectTextures.image(id,pixels,width,height,manager.k,manager.j);
    }
    public static void loaded(int id,final ji manager,String name) {
        DirectTextures.Texture image=DirectTextures.pixels(id);
        if(image.pbrLoaded&&image.asset.equals(name))return;
        image.asset=name;image.pbrLoaded=true;++image.pbrRevision;
        image.pbr=mcrtx.render.DirectPbr.load(name,new mcrtx.render.DirectPbr.Assets(){
            public java.io.InputStream open(String path)throws java.io.IOException{return manager.l.a.a(path);}
        });
        DirectRenderer.textureStateChanged(id);
    }
    public static void animate(ji manager) {
        int previous=DirectRenderer.texture;
        for(Object entry:manager.g) {
            aw effect=(aw)entry;
            effect.c=false; // Anaglyph lighting/color conversion is not part of the unlit backend.
            effect.a();effect.a(manager);
            for(int x=0;x<effect.e;++x)for(int y=0;y<effect.e;++y)
                DirectTextures.region(DirectRenderer.texture,effect.b%16*16+x*16,effect.b/16*16+y*16,16,16,effect.a);
            if(effect.d>0)DirectTextures.region(effect.d,0,0,16,16,effect.a);
        }
        DirectRenderer.texture=previous;
    }
}
