import net.minecraft.client.Minecraft;
import org.lwjgl.opengl.Display;
import mcrtx.bridge.DirectBridge;
import java.io.File;

/** Isolated real vanilla startup and menu/animated-texture lifecycle (no user world). */
public final class DirectStartupTest {
    public static void main(String[] args)throws Exception {
        Minecraft.af=new File(args[0]);Minecraft.af.mkdirs();
        Minecraft game=new Minecraft(null,null,null,640,480,false){
            public void a(mh report){throw new AssertionError(report.a,report.b);}
        };
        game.k=new gr("DirectTest","0");
        try{
            game.a();
            for(int i=0;i<5;++i){game.p.a();game.t.b(.5f);Display.update();Thread.sleep(20);}
            if(DirectBridge.counts()[1]<2)throw new AssertionError(DirectBridge.error());
            System.out.println("Vanilla startup, animated textures and menu frames passed without OpenGL");
        }finally{
            if(game.U!=null)game.U.b();game.J=false;game.B.b();Display.destroy();
        }
        System.exit(0);
    }
}
