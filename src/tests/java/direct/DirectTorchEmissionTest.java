import mcrtx.bridge.DirectBridge;
import mcrtx.bridge.DirectPacket;
import mcrtx.render.DirectEmission;
import mcrtx.render.DirectRenderer;
import mcrtx.render.DirectTextures;
import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Arrays;
import javax.imageio.ImageIO;

/** Real Java/JNI materials: torch tips glow, handles stay dark, and the
 * material does not add cast light already represented by analytic torches. */
public final class DirectTorchEmissionTest {
    private static void require(boolean v,String message) {
        if(!v)throw new AssertionError(message+": "+DirectBridge.error());
    }
    private static void write(File file,String text)throws Exception {
        Files.write(file.toPath(),text.getBytes(StandardCharsets.UTF_8));
    }
    private static void mesh(long id,long material,float halfX,float halfY,float z,float u0,float v0,float u1,float v1) {
        DirectPacket p=DirectRenderer.resources;int at=p.command(3);
        p.id(id);p.id(material);p.integer(4);p.integer(6);
        for(int i=0;i<4;++i) {
            p.number(i==0||i==3?-halfX:halfX);p.number(i<2?-halfY:halfY);p.number(z);
            p.number(i==0||i==3?u0:u1);p.number(i<2?v1:v0);p.integer(0xffffffff);
        }
        for(int i:new int[]{0,1,2,0,2,3})p.integer(i);p.end(at);
    }
    private static DirectPacket frame(long epoch) {
        DirectPacket p=new DirectPacket();int at=p.command(16);p.integer(0);
        for(int i=0;i<16;++i)p.number(i%5==0?1f:0f);
        for(float v:new float[]{2,0,0,0,0,2,0,0,0,0,100f/99.9f,1,0,0,-10f/99.9f,0})p.number(v);
        p.id(1);p.id(epoch);p.integer(0);
        for(float v:new float[]{0,0,0,0,0,1,0,0,0})p.number(v);p.end(at);
        for(long id:new long[]{900001,900002}) {
            at=p.command(17);p.id(id);p.id(id);p.integer(0);p.integer(1);
            for(float v:new float[]{1,0,0,0,0,1,0,0,0,0,1,0})p.number(v);
            p.integer(0);p.integer(1);p.integer(0xffffffff);p.integer(7);p.integer(0);
            p.integer(0);p.integer(6);p.integer(7);p.integer(0);p.end(at);
        }
        return p;
    }
    private static double channel(BufferedImage image,int x0,int y0,int channel) {
        double sum=0;
        for(int y=y0;y<y0+8;++y)for(int x=x0;x<x0+8;++x)
            sum+=(image.getRGB(x,y)>>(16-channel*8))&255;
        return sum/(64*255);
    }
    public static void main(String[] args)throws Exception {
        if(!Boolean.getBoolean("mcrtx.test.directGpu"))return;
        File output=new File(args[0]).getAbsoluteFile();require(output.mkdirs(),"Use a fresh output directory");
        String path=System.getenv("REMIX_CONFIG_DIR");require(path!=null,"Set an isolated config directory");
        File config=new File(path),profiles=new File(config,"profiles");require(profiles.mkdirs(),"Use fresh configuration");
        write(new File(config,"state.conf"),"lastProfile = Torch\n");
        for(String primary:new String[]{"raster","ray"}) {
            write(new File(profiles,"Torch.conf"),"profile.version = 1\nrtx.native.backend = pathtrace\n"
                +"rtx.native.vsync = 0\nrtx.native.pathtrace.upscaler = off\n"
                +"rtx.native.pathtrace.primaryVisibility = "+primary+"\n"
                +"rtx.native.pathtrace.denoiser = off\nrtx.native.pathtrace.taa = 0\n"
                +"rtx.native.pathtrace.sigma = 0\nrtx.native.pathtrace.sharc = 0\n"
                +"rtx.native.pathtrace.sky = 0\nrtx.native.pathtrace.localLights = 1\n"
                +"rtx.native.pathtrace.bounces = 0\nrtx.native.pathtrace.prism = 0\nrtx.native.pathtrace.gamma22 = 0\n");
            try {
                DirectBridge.start(128,128);
                long epoch=0;
                for(int block:new int[]{50,76})for(boolean authored:new boolean[]{false,true}) {
                    String name=primary+"-"+block+(authored?"-authored":"-fallback");
                    DirectRenderer.resources.clear();
                    int tile=block==50?80:99,tx=tile%16*16,ty=tile/16*16;
                    int base=DirectTextures.allocate();int[] color=new int[256*256];Arrays.fill(color,0xffff8000);
                    DirectTextures.image(base,color,256,256,false,false);
                    int mask=DirectTextures.allocate();
                    DirectTextures.image(mask,DirectEmission.mask(DirectTextures.pixels(base),block),256,256,false,false);
                    if(authored) {
                        BufferedImage map=new BufferedImage(256,256,BufferedImage.TYPE_INT_RGB);
                        for(int y=5;y<=9;++y)for(int x=6;x<=9;++x)map.setRGB(tx+x,ty+y,0xff008000);
                        File file=new File(output,name+"-map.png");ImageIO.write(map,"png",file);
                        DirectTextures.pixels(base).pbr[3]=file.getPath();
                    }
                    int level=block==50?14:7;
                    DirectEmission.writeMaterial(block,level,0,base,mask);
                    mesh(900001,0x6000000000000000L+(block*16+level)*8,.75f,1.25f,3,
                        (tx+6)/256f,(ty+5)/256f,(tx+10)/256f,(ty+16)/256f);
                    int white=DirectTextures.allocate();DirectTextures.image(white,new int[]{0xffffffff},1,1,false,false);
                    DirectRenderer.writeMaterial(900003,white);
                    mesh(900002,900003,5,5,5,0,0,1,1);
                    long before=DirectBridge.counts()[1];DirectPacket frame=frame(++epoch);
                    require(DirectBridge.submit(DirectRenderer.resources.buffer(),frame.buffer()),"Submit torch material");
                    long deadline=System.nanoTime()+180_000_000_000L;
                    while(DirectBridge.counts()[1]==before&&System.nanoTime()<deadline) {
                        require(DirectBridge.error().isEmpty(),"Render failed");Thread.sleep(10);
                    }
                    require(DirectBridge.counts()[1]>before,"No torch frame");
                    File shot=new File(output,name+".png");
                    require(DirectBridge.control(6,0,0,0,shot.getPath()),"Queue screenshot");
                    BufferedImage image=null;deadline=System.nanoTime()+10_000_000_000L;
                    while(image==null&&System.nanoTime()<deadline) {
                        Thread.sleep(10);
                        if(shot.isFile())try{image=ImageIO.read(shot);}catch(java.io.IOException pending){}
                    }
                    require(image!=null,"Screenshot missing");
                    double red=channel(image,60,28,0),green=channel(image,60,28,1),body=0,receiver=0;
                    for(int c=0;c<3;++c){body=Math.max(body,channel(image,60,92,c));receiver=Math.max(receiver,channel(image,104,32,c));}
                    System.out.printf("%s tipR=%.3f tipG=%.3f handle=%.3f receiver=%.3f%n",name,red,green,body,receiver);
                    require((authored?green:red)>.9,"Torch emissive tip is dark");
                    if(authored)require(red<.01,"Procedural mask replaced authored emissive map");
                    require(body<.01,"Torch handle is emissive");
                    require(receiver<.01,"Torch material added duplicate cast light");
                }
            } finally { DirectBridge.stop(); }
        }
        System.out.println("Torch emission: authored/fallback maps, dark handles and no duplicate cast light passed");
    }
}
