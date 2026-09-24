import mcrtx.render.DirectEmission;
import mcrtx.render.DirectTextures;
import mcrtx.render.DirectRenderer;
import mcrtx.bridge.DirectPacket;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;

/** Pure CPU mask and exact emission-packet checks; no window or graphics API. */
public final class DirectEmissionTest {
    private static void require(boolean v,String m){if(!v)throw new AssertionError(m);}
    public static void main(String[] args) {
        DirectTextures.Texture image=new DirectTextures.Texture();image.width=image.height=256;
        image.rgba=new byte[256*256*4];Arrays.fill(image.rgba,(byte)255);
        int[] torch=DirectEmission.mask(image,50);
        int x=80%16*16+8,y=80/16*16;
        require((torch[(y+7)*256+x]&0xffffff)!=0,"Torch tip is dark");
        require((torch[(y+14)*256+x]&0xffffff)==0,"Torch handle emits");
        require((torch[0]&0xffffff)==0,"Unrelated atlas tile emits");
        int[] stone=DirectEmission.mask(image,1);for(int c:stone)require((c&0xffffff)==0,"Stone inferred as emissive");
        require(DirectEmission.radiance(0)==0&&DirectEmission.radiance(15)>DirectEmission.radiance(7),"Intrinsic level conversion");
        int texture=DirectTextures.allocate();DirectTextures.image(texture,new int[]{0xffff8000},1,1,false,false);
        int mask=DirectTextures.allocate();DirectTextures.image(mask,new int[]{0xffffffff},1,1,false,false);
        DirectRenderer.resources.clear();DirectRenderer.writeMaterial(123,texture,mask,3,2,1);
        ByteBuffer source=DirectRenderer.resources.buffer(),p=source.duplicate().order(ByteOrder.LITTLE_ENDIAN);
        p.limit(source.position());p.position(0);require(p.getInt()==0x54435244&&p.getInt()==6,"Protocol 6");
        int materials=0,textures=0;
        while(p.hasRemaining()) {
            int op=p.getInt(),size=p.getInt(),end=p.position()+size;
            if(op==1){++textures;require(size==20,"Texture command nested in material");}
            else if(op==2) {
                ++materials;require(size==64&&p.getLong()==123,"Emission material framing");
                p.getLong();p.getInt();p.getInt();p.getInt();require(p.getLong()!=0,"Emission texture not transmitted");
                require(p.getFloat()==3&&p.getFloat()==2&&p.getFloat()==1,"Emission RGB not transmitted");
            } else throw new AssertionError("Unexpected resource command");
            p.position(end);
        }
        require(materials==1&&textures==2,"Texture dependencies must precede their material");
        for(int block:new int[]{50,76,89}) {
            int level=block==76?7:14;
            DirectRenderer.resources.clear();DirectEmission.writeMaterial(block,level,0,texture,mask);
            source=DirectRenderer.resources.buffer();p=source.duplicate().order(ByteOrder.LITTLE_ENDIAN);
            p.limit(source.position());p.position(8);
            require(p.getInt()==2,"Placed block material packet missing");p.getInt();
            require(p.getLong()==0x6000000000000000L+(block*16+level)*8,"Placed material identity changed");
            p.getLong();p.getInt();p.getInt();p.getInt();
            require(p.getLong()!=0,"Placed emission mask missing");
            for(int c=0;c<3;++c)require(p.getFloat()==DirectEmission.radiance(level),"Torch visible emission disabled");
        }
        System.out.println("Placed-block masks, intrinsic emission and protocol dependencies passed");
    }
}
