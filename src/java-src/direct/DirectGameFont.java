import mcrtx.render.DirectRenderer;
import mcrtx.render.DirectTransforms;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

/** Cached glyph geometry shared by GUI text, name tags and signs. */
public final class DirectGameFont {
    private static final Map<sj,LinkedHashMap<String,DirectRenderer.Mesh>> fonts=new java.util.WeakHashMap<sj,LinkedHashMap<String,DirectRenderer.Mesh>>();
    private static final Map<sj,Long> revisions=new java.util.WeakHashMap<sj,Long>();
    private DirectGameFont() { }
    public static void initialize(sj font,kv options,String path,ji textures) {
        font.b=new int[256];font.a=textures.b(path);
        refresh(font);
    }
    public static void refresh(sj font){
        mcrtx.render.DirectTextures.Texture image=mcrtx.render.DirectTextures.pixels(font.a);
        Long revision=revisions.get(font);if(revision!=null&&revision==image.revision)return;
        revisions.put(font,image.revision);
        for(int glyph=0;glyph<256;++glyph) {
            int right=7;
            for(;right>=0;--right) {
                boolean empty=true;
                for(int y=0;y<8;++y) {
                    int x=(glyph%16*8+right)*image.width/128;
                    int py=(glyph/16*8+y)*image.height/128;
                    if((image.rgba[(py*image.width+x)*4+2]&255)>0) {empty=false;break;}
                }
                if(!empty)break;
            }
            font.b[glyph]=glyph==32?4:right+2;
        }
        Map<String,DirectRenderer.Mesh> cache=fonts.remove(font);
        if(cache!=null)for(DirectRenderer.Mesh mesh:cache.values())mesh.dispose();
    }
    public static void draw(sj font,String text,int x,int y,int color,boolean shadow) {
        if(text==null||text.isEmpty())return;
        refresh(font);
        if((color&0xff000000)==0)color|=0xff000000;
        if(shadow)color=(color&0xff000000)|((color&0xfcfcfc)>>2);
        LinkedHashMap<String,DirectRenderer.Mesh> cache=fonts.get(font);
        if(cache==null){cache=new LinkedHashMap<String,DirectRenderer.Mesh>(64,.75f,true);fonts.put(font,cache);}
        String key=color+":"+shadow+":"+text;
        DirectRenderer.Mesh mesh=cache.get(key);
        if(mesh==null) {
            mesh=build(font,text,color,shadow);cache.put(key,mesh);
            if(cache.size()>2048){Map.Entry<String,DirectRenderer.Mesh> oldest=cache.entrySet().iterator().next();oldest.getValue().dispose();cache.remove(oldest.getKey());}
        }
        DirectRenderer.texture=font.a;DirectRenderer.textureEnabled=true;
        DirectTransforms.select(5888);DirectTransforms.push();DirectTransforms.translate(x,y,0);
        DirectRenderer.draw(mesh,true,true,false);DirectTransforms.pop();
    }
    private static DirectRenderer.Mesh build(sj font,String text,int color,boolean shadow) {
        ArrayList<Float> vertices=new ArrayList<Float>();ArrayList<Integer> colors=new ArrayList<Integer>();int x=0;
        for(int i=0;i<text.length();++i) {
            char c=text.charAt(i);
            if(c=='\u00a7' && i+1<text.length()) {
                int value="0123456789abcdef".indexOf(Character.toLowerCase(text.charAt(++i)));if(value<0)value=15;
                int bright=(value>>3&1)*85,r=(value>>2&1)*170+bright,g=(value>>1&1)*170+bright,b=(value&1)*170+bright;
                if(value==6)r+=85;if(shadow){r/=4;g/=4;b/=4;}
                color=0xff000000|r<<16|g<<8|b;continue;
            }
            int glyph=fp.a.indexOf(c);if(glyph<0)continue;glyph+=32;
            float u=(glyph%16*8)/128f,v=(glyph/16*8)/128f,s=7.99f;
            float[] quad={x,s,0,u,v+s/128,x+s,s,0,u+s/128,v+s/128,x+s,0,0,u+s/128,v,x,0,0,u,v};
            for(float f:quad)vertices.add(f);for(int j=0;j<4;++j)colors.add(color);x+=font.b[glyph];
        }
        float[] v=new float[vertices.size()];int[] c=new int[colors.size()];
        for(int i=0;i<v.length;++i)v[i]=vertices.get(i);for(int i=0;i<c.length;++i)c[i]=colors.get(i);
        return new DirectRenderer.Mesh(v,c,DirectGameGeometry.triangles(7,c.length));
    }
}
