package mcrtx.render;

/** Texture-pack-relative masks for intrinsically luminous placed blocks. */
public final class DirectEmission {
    private DirectEmission() { }
    public static float radiance(int level) {
        float normalized=Math.max(0,Math.min(15,level))/15f;
        return (float)(16*Math.PI)*normalized*normalized;
    }
    public static void writeMaterial(int block,int level,int materialClass,int texture,int mask) {
        float power=radiance(level);
        // The native material policy suppresses duplicate area lights for
        // analytic torches without disabling their visible emission maps.
        DirectRenderer.writeMaterial(0x6000000000000000L+(block*16+level)*8+materialClass,
            texture,mask,power,power,power);
    }
    public static int[] mask(DirectTextures.Texture image,int block) {
        int[] pixels=new int[image.width*image.height];
        int tileWidth=Math.max(1,image.width/16),tileHeight=Math.max(1,image.height/16);
        for(int y=0;y<image.height;++y)for(int x=0;x<image.width;++x) {
            int tile=(y/tileHeight)*16+x/tileWidth;
            int tx=(x%tileWidth)*16/tileWidth,ty=(y%tileHeight)*16/tileHeight;
            int at=(y*image.width+x)*4,r=image.rgba[at]&255,g=image.rgba[at+1]&255,b=image.rgba[at+2]&255,a=image.rgba[at+3]&255;
            boolean emit=false;
            switch(block) {
            case 39: emit=tile==29;break;
            case 50: emit=tile==80&&tx>=6&&tx<=9&&ty>=5&&ty<=9;break;
            case 51: emit=tile==31||tile==47;break;
            case 62: emit=tile==61&&tx>=3&&tx<=12&&ty>=7&&ty<=14&&r>g*1.15;break;
            case 74: emit=tile==51&&r>g*1.5&&r>b*1.5;break;
            case 76: case 94: emit=(tile==99||tile==164)&&tx>=6&&tx<=9&&ty>=5&&ty<=9&&r>g*1.3;break;
            case 89: emit=tile==105;break;
            case 91: emit=tile==120&&((ty>=4&&ty<=6&&((tx>=3&&tx<=6)||(tx>=9&&tx<=12)))||(ty>=9&&ty<=12&&tx>=3&&tx<=12));break;
            case 95: emit=tile==25||tile==26||tile==27;break;
            // Lava/portal use the compact texture whose UVs the terrain builder emits.
            case 10: case 11: case 90: emit=true;break;
            default: break;
            }
            if(emit)pixels[y*image.width+x]=0xff000000|((r*a/255)<<16)|((g*a/255)<<8)|(b*a/255);
            else pixels[y*image.width+x]=0xff000000;
        }
        return pixels;
    }
}
