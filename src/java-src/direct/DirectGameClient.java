import net.minecraft.client.Minecraft;
import mcrtx.bridge.DirectBridge;
import mcrtx.render.DirectRenderer;
import mcrtx.render.DirectTransforms;
import org.lwjgl.opengl.Display;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

/** Explicit direct-rendering frame lifecycle; gameplay and GUI calculations stay in Java. */
public final class DirectGameClient {
    private DirectGameClient() { }
    private static float setting(String key){
        try{float value=Float.parseFloat(mcrtx.bridge.McrtxRuntimeConfig.getEnvironmentValue(key));return Float.isFinite(value)?Math.max(30,Math.min(120,value)):70;}
        catch(NumberFormatException absent){return 70;}
    }
    private static final float WORLD_FOV=setting("MCRTX_GAMEPLAY_FOV"),VIEW_FOV=setting("MCRTX_VIEWMODEL_FOV");
    public static float adjustFov(float vanilla){return vanilla*WORLD_FOV/70;}

    private static long metricsAt;
    private static long[] metrics=new long[4];
    private static String frameRates="Direct Remix";
    private static void frameRates(Minecraft game){
        long now=System.nanoTime();
        if(now-metricsAt>=1_000_000_000L){
            long[] current=DirectBridge.counts();double seconds=(now-metricsAt)/1e9;
            if(metricsAt!=0){
                long fresh=current[1]-metrics[1],total=current[2]-metrics[2];
                frameRates="Java "+Math.round((current[0]-metrics[0])/seconds)+" fps / Remix fresh "+Math.round(fresh/seconds)+" fps ("+Math.round((total-fresh)/seconds)+" redraws/s)";
            }
            metrics=current;metricsAt=now;
        }
        game.K=frameRates;
    }
    public static void starting(Minecraft game){
        if(Minecraft.af==null)Minecraft.af=new File(System.getProperty("user.dir"));
    }
    public static void failed(Minecraft game){
        game.J=false;
        try{game.d();}catch(Throwable cleanup){cleanup.printStackTrace();Display.destroy();}
    }
    public static void started(Minecraft game){metricsAt=0;frameRates="Direct Remix";DirectGameWorld.reset();game.m=null;game.z.g=false;game.z.k=false;screen(game);}
    public static void screen(Minecraft game){DirectBridge.control(3,game.r!=null?1:0,0,0,null);}
    public static void begin(px renderer){
        Minecraft game=renderer.j;Display.poll();frameRates(game);
        int w=Display.getWidth(),h=Display.getHeight();
        if(game.d!=w||game.e!=h)game.a(w,h);
        game.z.g=false;game.z.k=false;
        DirectRenderer.beginFrame(w,h);
    }
    public static void end(Minecraft game){DirectRenderer.present();}
    public static void releaseMouse(vy mouse){
        org.lwjgl.input.Mouse.setGrabbed(false);
        org.lwjgl.input.Mouse.setCursorPosition(Display.getWidth()/2,Display.getHeight()/2);
    }
    public static void clear(int mask){
        if((mask&16384)!=0)DirectRenderer.beginFrame(Display.getWidth(),Display.getHeight());
    }
    public static String deviceString(int field){return "Remix Vulkan (direct unlit renderer)";}
    public static String screenshot(File directory,int width,int height){
        try{
            File folder=new File(directory,"screenshots");if(!folder.isDirectory()&&!folder.mkdirs())return "Cannot create screenshots directory";
            String name=new SimpleDateFormat("yyyy-MM-dd_HH.mm.ss.SSS").format(new Date());
            File file=new File(folder,name+".png");
            return DirectBridge.control(6,0,0,0,file.getAbsolutePath())?"Screenshot queued: "+file.getName():"Screenshot request failed";
        }catch(Exception error){return "Screenshot failed: "+error.getMessage();}
    }
    public static void world(px renderer,float partial,long deadline){
        Minecraft game=renderer.j;if(game.i==null)game.i=game.h;
        renderer.a(partial); // Ray picking remains gameplay-owned.
        ls camera=game.i;
        DirectGameWorld.originX=camera.bl+(camera.aM-camera.bl)*partial;
        DirectGameWorld.originY=camera.bm+(camera.aN-camera.bm)*partial;
        DirectGameWorld.originZ=camera.bn+(camera.aO-camera.bn)*partial;
        cl provider=game.f.w();if(provider instanceof kx)((kx)provider).d((int)Math.floor(DirectGameWorld.originX/16),(int)Math.floor(DirectGameWorld.originZ/16));
        DirectRenderer.ui=false;DirectRenderer.layer=0;DirectRenderer.depthEnabled=true;
        DirectRenderer.cullEnabled=true;DirectRenderer.blendEnabled=false;DirectRenderer.alphaEnabled=true;
        renderer.a(partial,0); // Vanilla camera collision, bobbing, hurt, sleeping and third person.
        DirectGameWorld.environment(game.g,partial);
        DirectRenderer.terrainView(DirectGameWorld.originX,DirectGameWorld.originY,DirectGameWorld.originZ);
        sr frustum=new sr();frustum.a(DirectGameWorld.originX,DirectGameWorld.originY,DirectGameWorld.originZ);
        DirectGameWorld.sky(game.g,partial);
        DirectGameWorld.terrain(game.g,camera,0,partial);
        DirectGameWorld.pump(game.g,camera,false);
        DirectGameWorld.entities(game.g,camera.e(partial),frustum,partial);
        game.j.b(camera,partial);game.j.a(camera,partial);
        if(camera instanceof gs&&game.y!=null&&!game.z.z){
            gs player=(gs)camera;game.g.a(player,game.y,0,player.c.b(),partial);game.g.b(player,game.y,0,player.c.b(),partial);
        }
        renderer.c(partial); // Weather geometry, including rain/snow and splash particles.
        game.g.b(partial); // Clouds use the same CPU tessellator as other transient effects.
        DirectRenderer.layer=2;DirectRenderer.ui=false;DirectRenderer.alphaEnabled=true;
        DirectRenderer.blendEnabled=false;DirectRenderer.cullEnabled=true;DirectRenderer.color=0xffffffff;
        DirectTransforms.select(5889);DirectTransforms.loadIdentity();
        DirectTransforms.perspective(VIEW_FOV,(float)game.d/game.e,.05f,128);
        DirectTransforms.select(5888);
        renderer.b(partial,0);DirectRenderer.layer=0;
    }
}
