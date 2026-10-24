import mcrtx.render.DirectTextures;
import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.file.Files;

/**
 * Water's animated normal map. BetaRT's water_normal is an atlas of frames, each the still and the
 * flowing tile side by side as the water texture lays them out: the frame of the time is uploaded as
 * a linear texture of the water texture's layout, so the water quads' UVs sample it unchanged.
 */
final class DirectWaterNormal {
    private static final int FRAMES_PER_SECOND=20; // main's liquid animation rate
    private static int texture,shown=-1,tile;
    private static int[][] frames;
    private static String source="";
    private DirectWaterNormal() { }
    static void reset(){texture=0;shown=-1;frames=null;source="";}

    /** Points the water texture's normal map at the animated frame (its PBR maps were just loaded). */
    static void attach(int water){
        DirectTextures.Texture image=DirectTextures.pixels(water);
        String path=image.pbr[0];
        if(path.startsWith("0x"))return; // Already this texture; the maps were not reloaded.
        if(!path.equals(source)){source=path;frames=path.isEmpty()?null:load(path);shown=-1;}
        // No atlas (no map, one frame, or a format not read here): the map as it is.
        if(frames==null)return;
        if(texture==0){texture=DirectTextures.allocate();DirectTextures.pixels(texture).linear=true;}
        animate();
        DirectTextures.get(texture); // Uploaded ahead of the material that names it.
        image.pbr[0]="0x"+Long.toHexString(DirectTextures.pixels(texture).id);
    }

    /** Shows the frame of the time; every frame, as vanilla animates the water texture. */
    static void animate(){
        if(frames==null||texture==0)return;
        int frame=(int)((System.nanoTime()/(1000000000L/FRAMES_PER_SECOND))%frames.length);
        if(frame==shown)return;
        DirectTextures.image(texture,frames[frame],tile*2,tile,false,false);
        shown=frame;
    }

    private static int[][] load(String path){
        try {
            int[] size=new int[2];int[] argb=path.toLowerCase().endsWith(".dds")?dds(new File(path),size):png(new File(path),size);
            int width=size[0],height=size[1],count=argb==null?0:width/(height*2);
            if(count<2||width!=count*height*2){
                if(argb!=null&&count!=1)System.err.println("BetaRT water normal: "+width+"x"+height+" is not an atlas of still/flowing frames");
                return null;
            }
            tile=height;int[][] result=new int[count][tile*2*tile];
            for(int f=0;f<count;++f)for(int y=0;y<tile;++y)
                System.arraycopy(argb,y*width+f*tile*2,result[f],y*tile*2,tile*2);
            return result;
        } catch(Exception e) { System.err.println("BetaRT water normal "+path+": "+e.getMessage()); return null; }
    }
    private static int[] png(File file,int[] size)throws java.io.IOException {
        BufferedImage image=javax.imageio.ImageIO.read(file);
        if(image==null)return null;
        size[0]=image.getWidth();size[1]=image.getHeight();
        return image.getRGB(0,0,size[0],size[1],null,0,size[0]);
    }
    /** Uncompressed 32-bit DDS (BetaRT's own maps); compressed ones stay static. */
    private static int[] dds(File file,int[] size)throws java.io.IOException {
        ByteBuffer b=ByteBuffer.wrap(Files.readAllBytes(file.toPath())).order(ByteOrder.LITTLE_ENDIAN);
        if(b.limit()<128||b.getInt(0)!=0x20534444)throw new java.io.IOException("not a DDS file");
        int height=b.getInt(12),width=b.getInt(16),flags=b.getInt(80),bits=b.getInt(88);
        if((flags&0x4)!=0||bits!=32)return null; // FourCC (block compressed) or not 32-bit
        int[] mask={b.getInt(92),b.getInt(96),b.getInt(100),(flags&0x1)!=0?b.getInt(104):0};
        if(width<=0||height<=0||(long)width*height*4>b.limit()-128)throw new java.io.IOException("truncated DDS");
        size[0]=width;size[1]=height;int[] argb=new int[width*height];
        for(int i=0;i<argb.length;++i) {
            int texel=b.getInt(128+i*4);
            argb[i]=(mask[3]==0?255:channel(texel,mask[3]))<<24|channel(texel,mask[0])<<16|channel(texel,mask[1])<<8|channel(texel,mask[2]);
        }
        return argb;
    }
    private static int channel(int texel,int mask){
        if(mask==0)return 0;
        int value=(texel&mask)>>>Integer.numberOfTrailingZeros(mask),max=mask>>>Integer.numberOfTrailingZeros(mask);
        return max==255?value:value*255/max;
    }
}
