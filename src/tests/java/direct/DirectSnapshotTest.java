import net.minecraft.client.Minecraft;
import org.lwjgl.opengl.Display;
import mcrtx.bridge.DirectBridge;
import java.io.*;
/** Imports terrain bytes into a RAM-only world; never opens a saved world or connects to a game server. */
public final class DirectSnapshotTest {
  private static java.lang.reflect.Field focusTimer;
  private static double cameraX,cameraY,cameraZ;private static float cameraYaw,cameraPitch;private static int frameSequence;
  private static final boolean quality = Boolean.getBoolean("mcrtx.test.quality");
  private static final String scene = System.getProperty("mcrtx.test.scene", "localhost");
  private static long motionStart;
  private static final class World extends fd {
    World(long seed){super(new wy(),"SnapshotTest",xa.a(0),seed);}
    public cl b(){return new uv(this);}
  }
  private static void frame(Minecraft game)throws Exception {
    double phase = quality ? frameSequence / 64.0 : (System.nanoTime()-motionStart)/1e9/4;
    double motion = scene.equals("moving") ? Math.sin(phase) : 0;
    game.h.c(cameraX,cameraY,cameraZ,cameraYaw+(float)(motion*8)+(frameSequence++%2)*.00001f,cameraPitch+(float)(motion*2));
    focusTimer.setLong(game.t,System.currentTimeMillis());game.r=null;game.N=false;
    game.p.a();game.t.a();game.t.b(.5f);Display.update();
    if(!DirectBridge.error().isEmpty())throw new AssertionError(DirectBridge.error());
  }
  private static byte[] bytes(DataInputStream in)throws Exception {byte[] b=new byte[in.readInt()];in.readFully(b);return b;}
  public static void main(String[] args)throws Exception {
    focusTimer=px.class.getDeclaredField("H");focusTimer.setAccessible(true);
    File directory=new File(args[0]);directory.mkdirs();Minecraft.af=directory;
    Minecraft game=new Minecraft(null,null,null,1920,1080,false){public void a(mh r){throw new AssertionError(r.a,r.b);}};
    game.k=new gr("SnapshotTest","0");
    try {
      game.a();game.z.e=2;game.z.z=false;game.z.j=true;game.z.i=0;
      DataInputStream in=new DataInputStream(new BufferedInputStream(new FileInputStream(args[1])));
      if(in.readInt()!=1)throw new AssertionError("Snapshot version");int count=in.readInt();
      double x=in.readDouble(),y=in.readDouble(),z=in.readDouble();float yaw=in.readFloat(),pitch=in.readFloat();long seed=in.readLong(),time=in.readLong();
      cameraX=x;cameraY=y;cameraZ=z;cameraYaw=yaw;cameraPitch=pitch;
      World world=new World(seed);world.B=true;world.a(scene.equals("localhost") ? time : 6000L);
      for(int i=0;i<count;++i){int cx=in.readInt(),cz=in.readInt();lm chunk=((uv)world.w()).c(cx,cz);chunk.b=bytes(in);chunk.e=new wi(bytes(in));chunk.f=new wi(bytes(in));chunk.g=new wi(bytes(in));chunk.h=bytes(in);}
      in.close();game.f=world;game.h=new dc(game,world,game.k,0);game.h.c(x,y,z,yaw,pitch);game.i=game.h;game.h.a=new uo();
      game.c=new ob(game);game.g.a(world);game.j.a(world);game.r=null;game.N=false;th.a.f=new ra(game);
      long start=System.nanoTime(),warmup=Long.getLong("mcrtx.test.warmupMs",45000L),duration=Long.getLong("mcrtx.test.measureMs",60000L);long[] before=null,after=null;long measureStart=0,measureEnd;
      // Wait for fresh world frames before timed shader/terrain warmup.
      long fresh=DirectBridge.counts()[1];while(DirectBridge.counts()[1]<fresh+12&&(System.nanoTime()-start)<180_000_000_000L){frame(game);Thread.sleep(8);}
      if(DirectBridge.counts()[1]<fresh+12)throw new AssertionError("No fresh world frames");
      motionStart = System.nanoTime();
      if (quality) {
        long terrainWarmup=System.nanoTime();
        while(System.nanoTime()-terrainWarmup<45_000_000_000L) frame(game);
        mcrtx.render.DirectRenderer.cameraCut();
        frameSequence = 0;
        for (int i=0;i<288;++i) {
          long previous=DirectBridge.counts()[1]; frame(game);
          long deadline=System.nanoTime()+10_000_000_000L;
          while(DirectBridge.counts()[1]<=previous && System.nanoTime()<deadline)Thread.sleep(1);
          if(DirectBridge.counts()[1]<=previous)throw new AssertionError("Stalled quality frame");
        }
        before=DirectBridge.counts(); measureStart=System.currentTimeMillis();
        for(int i=0;i<32;++i) {
          File image=new File(directory,String.format("quality-%02d.png",i));
          DirectBridge.control(6,0,0,0,image.getAbsolutePath()); frame(game);
          long deadline=System.nanoTime()+10_000_000_000L;
          while(!image.exists() && System.nanoTime()<deadline)Thread.sleep(1);
          if(!image.exists())throw new AssertionError("No quality capture");
        }
        after=DirectBridge.counts(); measureEnd=System.currentTimeMillis();
        String window="{\"start_ms\":"+measureStart+",\"end_ms\":"+measureEnd+",\"fresh\":"+(after[1]-before[1])+"}";
        java.nio.file.Files.write(new File(directory,"measurement.json").toPath(),window.getBytes("UTF-8"));
      } else {
      start=System.nanoTime();while((System.nanoTime()-start)/1000000<warmup+duration){frame(game);if(before==null&&(System.nanoTime()-start)/1000000>=warmup){before=DirectBridge.counts();measureStart=System.currentTimeMillis();}}
      after=DirectBridge.counts();measureEnd=System.currentTimeMillis();
      String result="{\"start_ms\":"+measureStart+",\"end_ms\":"+measureEnd+",\"fresh\":"+(after[1]-before[1])+",\"presented\":"+(after[2]-before[2])+",\"chunks\":"+count+"}";
      java.nio.file.Files.write(new File(directory,"measurement.json").toPath(),result.getBytes("UTF-8"));System.out.println(result);
      File image=new File(directory,"world.png");DirectBridge.control(6,0,0,0,image.getAbsolutePath());long deadline=System.nanoTime()+5_000_000_000L;while(!image.exists()&&System.nanoTime()<deadline){frame(game);Thread.sleep(8);}if(!image.exists())throw new AssertionError("No screenshot");
      }
      game.g.a((fd)null);
    }finally{if(game.U!=null)game.U.b();game.J=false;game.B.b();Display.destroy();}
    System.exit(0);
  }
}
