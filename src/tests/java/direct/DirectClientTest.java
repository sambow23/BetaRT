import mcrtx.bridge.DirectBridge;
import mcrtx.render.DirectRenderer;
import mcrtx.render.DirectTextures;
import mcrtx.render.DirectTransforms;
import net.minecraft.client.Minecraft;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;
import org.lwjgl.input.Keyboard;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;

/** Executes real patched vanilla geometry and GUI methods without a GL/GLFW classpath. */
public final class DirectClientTest {
    private static void require(boolean condition,String message){if(!condition)throw new AssertionError(message);}
    private static void near(float value,float expected){require(Math.abs(value-expected)<.001,"Transform: "+value+" expected "+expected);}
    private static BufferedImage screenshot(File file)throws Exception {
        DirectBridge.control(6,0,0,0,file.getAbsolutePath());
        long deadline=System.nanoTime()+5_000_000_000L;
        while(System.nanoTime()<deadline){
            try{BufferedImage image=ImageIO.read(file);if(image!=null)return image;}catch(Exception retry){}
            Thread.sleep(8);
        }
        throw new AssertionError("Screenshot timed out: "+DirectBridge.error());
    }
    private static void awaitFrame(long before)throws Exception {
        long deadline=System.nanoTime()+5_000_000_000L;
        while(DirectBridge.counts()[1]<=before&&System.nanoTime()<deadline)Thread.sleep(8);
        require(DirectBridge.counts()[1]>before,"Frame failed: "+DirectBridge.error());
    }
    public static void main(String[] args)throws Exception {
        try{Class.forName("org.lwjgl.opengl.GL11");throw new AssertionError("OpenGL module present");}catch(ClassNotFoundException expected){}
        DirectTransforms.reset();DirectTransforms.translate(1,2,3);DirectTransforms.push();DirectTransforms.rotate(90,0,0,1);
        near(DirectTransforms.model()[1],1);near(DirectTransforms.model()[12],1);DirectTransforms.pop();near(DirectTransforms.model()[0],1);
        Keyboard.create();Keyboard.key(1,true,false);Keyboard.text(0x1f600);Keyboard.key(1,false,false);
        require(Keyboard.next()&&Keyboard.getEventKey()==30&&Keyboard.getEventKeyState(),"Key press");
        require(Keyboard.next()&&Character.isHighSurrogate(Keyboard.getEventCharacter()),"Unicode high surrogate");
        require(Keyboard.next()&&Character.isLowSurrogate(Keyboard.getEventCharacter()),"Unicode low surrogate");
        require(Keyboard.next()&&!Keyboard.getEventKeyState(),"Key release");Keyboard.destroy();
        org.lwjgl.input.Mouse.create();
        org.lwjgl.input.Mouse.event(4,1,true,17,23,0,0);
        require(org.lwjgl.input.Mouse.next()&&org.lwjgl.input.Mouse.getEventX()==17&&
                org.lwjgl.input.Mouse.getEventY()==Display.getHeight()-24,"Button position without prior motion");
        org.lwjgl.input.Mouse.reset();
        require(org.lwjgl.input.Mouse.next()&&!org.lwjgl.input.Mouse.getEventButtonState(),"Reset releases held button");
        org.lwjgl.input.Mouse.event(6,0,false,0,1,0,0);
        require(org.lwjgl.input.Mouse.next()&&org.lwjgl.input.Mouse.getEventDWheel()==120,"Wheel displacement");
        org.lwjgl.input.Mouse.destroy();
        File directory=new File(args[0]);directory.mkdirs();
        Display.setDisplayMode(new DisplayMode(640,480));Display.create();
        int unused=DirectTextures.allocate();DirectTextures.delete(unused);
        DirectRenderer.beginFrame(640,480);DirectTransforms.reset();
        ps part=new ps(0,0);part.a(-.5f,-.5f,-.5f,1,1,1);
        DirectRenderer.color=0xffff0000;DirectRenderer.textureEnabled=false;
        DirectTransforms.translate(0,0,-2);DirectTransforms.select(5889);DirectTransforms.perspective(70,640f/480,.05f,100);DirectTransforms.select(5888);
        part.a(1);require(part.n,"Persistent model was not built");int id=part.o;
        long before=DirectBridge.counts()[1];Display.swapBuffers();awaitFrame(before);
        BufferedImage model=screenshot(new File(directory,"model.png"));
        require((model.getRGB(320,240)&0xffffff)==0xff0000,"Model transform/material pixel");
        DirectRenderer.beginFrame(640,480);part.a(1);require(part.o==id,"Model rebuilt without a shape change");Display.swapBuffers();
        Minecraft game=new Minecraft(null,null,null,640,480,false){public void a(mh report){throw new AssertionError("Game crash");}};
        game.z=new kv(game,directory);game.D=new ik(game,directory);game.p=new ji(game.D,game.z);game.q=new sj(game.z,"/font/default.png",game.p);
        DirectRenderer.beginFrame(640,480);DirectTransforms.reset();
        DirectTransforms.select(5889);DirectTransforms.perspective(70,640f/480,.05f,100);DirectTransforms.select(5888);
        DirectRenderer.color=0xffff0000;DirectRenderer.textureEnabled=false;DirectTransforms.translate(0,0,-2);part.a(1);
        DirectTransforms.loadIdentity();DirectTransforms.translate(0,0,-1.4f);DirectTransforms.scale(.04f,-.04f,.04f);
        DirectRenderer.cullEnabled=true;DirectRenderer.depthMask(false);game.q.b("A",0,0,0);DirectRenderer.depthMask(true);
        before=DirectBridge.counts()[1];Display.swapBuffers();awaitFrame(before);
        BufferedImage text=screenshot(new File(directory,"world-text.png"));int black=0;
        for(int y=240;y<267;++y)for(int x=320;x<347;++x)if((text.getRGB(x,y)&0xffffff)==0)++black;
        require(black>10,"World glyphs did not render over model geometry");
        game.t=new px(game);game.N=false;game.r=new fu();game.r.a(game,320,240);
        game.t.b(0);
        before=DirectBridge.counts()[1];Display.swapBuffers();awaitFrame(before);
        BufferedImage menu=screenshot(new File(directory,"menu.png"));
        int distinct=0,color=menu.getRGB(0,0);
        for(int y=0;y<480;y+=8)for(int x=0;x<640;x+=8)if(menu.getRGB(x,y)!=color)++distinct;
        require(distinct>500,"Vanilla menu did not draw");
        int originalWidth=game.q.a("ABC");
        DirectTextures.Texture atlas=DirectTextures.pixels(game.q.a);
        int[] saved=new int[atlas.width*atlas.height];
        for(int i=0;i<saved.length;++i)saved[i]=(atlas.rgba[i*4+3]&255)<<24|(atlas.rgba[i*4]&255)<<16|(atlas.rgba[i*4+1]&255)<<8|(atlas.rgba[i*4+2]&255);
        DirectTextures.image(game.q.a,new int[saved.length],atlas.width,atlas.height,false,false);
        require(game.q.a("ABC")==3,"Font widths did not refresh with atlas contents");
        DirectTextures.image(game.q.a,saved,atlas.width,atlas.height,false,false);
        require(game.q.a("ABC")==originalWidth,"Font widths did not restore after reload");
        game.t.b(0);before=DirectBridge.counts()[1];Display.swapBuffers();awaitFrame(before);
        Display.destroy();game.J=false;
        System.out.println("Patched vanilla models, menus, textures, text and direct input passed without OpenGL");
        System.exit(0);
    }
}
