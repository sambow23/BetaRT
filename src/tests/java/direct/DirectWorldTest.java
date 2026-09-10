import net.minecraft.client.Minecraft;
import org.lwjgl.opengl.Display;
import mcrtx.bridge.DirectBridge;
import java.io.File;
import javax.imageio.ImageIO;

/** Synthetic in-memory world: exercises vanilla gameplay render paths without modifying a save. */
public final class DirectWorldTest {
    private static java.lang.reflect.Field focusTimer;
    private static void worldFrame(Minecraft game)throws Exception {
        // Keep this off-focus fixture in gameplay without grabbing desktop focus.
        // H is the pinned b1.7.3 renderer's pause-on-focus-loss timestamp.
        focusTimer.setLong(game.t,System.currentTimeMillis());game.r=null;game.N=false;
        game.p.a();game.t.a();game.t.b(.5f);Display.update();
    }
    private static final class World extends fd {
        World(){super(new wy(),"DirectTest",xa.a(0),1L);}
        public cl b(){return new uv(this);}
    }
    private static void awaitWorld(Minecraft game)throws Exception {
        // Driver pipeline compilation is excluded from the timed warmup. Require
        // fresh submitted world frames; temporal/menu redraws cannot satisfy this.
        long before=DirectBridge.counts()[1],deadline=System.nanoTime()+180_000_000_000L;
        while(DirectBridge.counts()[1]<before+12&&System.nanoTime()<deadline){
            worldFrame(game);Thread.sleep(8);
            if(!DirectBridge.error().isEmpty())throw new AssertionError(DirectBridge.error());
        }
        if(DirectBridge.counts()[1]<before+12)throw new AssertionError("World pipeline warmup timed out");
    }
    private static void benchmark(Minecraft game,World world,File directory)throws Exception {
        java.util.ArrayList<sn> animated=new java.util.ArrayList<sn>();
        for(int i=0;i<144;++i){sn entity=jc.a(i%2==0?"Pig":"Zombie",world);entity.c(-10+i%12*2,64,7+i/12*2,0,0);world.b(entity);animated.add(entity);}
        long warmup=Long.getLong("mcrtx.test.warmupMs",45000L),duration=Long.getLong("mcrtx.test.measureMs",60000L);
        long start=System.nanoTime(),measureStart=0,measureEnd=0;long[] before=null,after=null;
        while((System.nanoTime()-start)/1000000<warmup+duration){
            double t=(System.nanoTime()-start)*1e-9;
            for(int i=0;i<animated.size();++i){sn entity=animated.get(i);entity.aM=entity.bl=-10+i%12*2+Math.sin(t*2+i)*.4;}
            worldFrame(game);
            if(!DirectBridge.error().isEmpty())throw new AssertionError(DirectBridge.error());
            if(before==null&&(System.nanoTime()-start)/1000000>=warmup){before=DirectBridge.counts();measureStart=System.currentTimeMillis();}
        }
        after=DirectBridge.counts();measureEnd=System.currentTimeMillis();
        String report="{\"start_ms\":"+measureStart+",\"end_ms\":"+measureEnd+",\"warmup_ms\":"+warmup+
            ",\"published\":"+(after[0]-before[0])+",\"fresh\":"+(after[1]-before[1])+",\"presented\":"+(after[2]-before[2])+"}";
        java.nio.file.Files.write(new File(directory,"measurement.json").toPath(),report.getBytes("UTF-8"));
        System.out.println("Benchmark: "+report);
    }
    public static void main(String[] args)throws Exception {
        focusTimer=px.class.getDeclaredField("H");focusTimer.setAccessible(true);
        File directory=new File(args[0]);directory.mkdirs();Minecraft.af=directory;
        Minecraft game=new Minecraft(null,null,null,Integer.getInteger("mcrtx.test.width",640),Integer.getInteger("mcrtx.test.height",480),false){public void a(mh report){throw new AssertionError(report.a,report.b);}};
        game.k=new gr("DirectTest","0");
        try{
            game.a();game.z.e=3;game.z.z=false;game.z.j=false;game.z.i=0;
            World world=new World();world.B=true;
            for(int z=-2;z<=2;++z)for(int x=-2;x<=2;++x)((uv)world.w()).c(x,z);
            for(int z=-16;z<32;++z)for(int x=-16;x<32;++x)world.a(x,63,z,2,0);
            for(int x=3;x<14;++x)for(int y=64;y<67;++y)world.a(x,y,16,1+(x%5),0);
            world.a(5,64,12,20,0);
            if(!Boolean.getBoolean("mcrtx.test.noEmitters")) {
                world.a(6,64,12,50,0);world.a(7,64,12,51,0);
            }
            world.a(9,64,12,8,0);
            if(!Boolean.getBoolean("mcrtx.test.noEmitters"))world.a(10,64,12,10,0);
            world.a(12,64,12,18,0);
            game.f=world;game.h=new dc(game,world,game.k,0);game.h.c(8,64,5,0,8);game.i=game.h;
            game.h.a=new uo();
            game.c=new ob(game);game.g.a(world);game.j.a(world);game.r=null;game.N=false;
            th.a.f=new ra(game);
            String[] entities={"Pig","Sheep","Creeper","Zombie","Skeleton","Cow","Chicken","Wolf","Spider","Slime","Boat","Minecart"};
            for(int i=0;i<entities.length;++i){sn entity=jc.a(entities[i],world);if(entity!=null){entity.c(3+i%6*2,64,8+i/6*3,0,0);world.b(entity);}}
            game.g.a("smoke",8,65,10,0,0,0);game.g.a("flame",9,65,10,0,0,0);
            awaitWorld(game);
            if(Boolean.getBoolean("mcrtx.test.benchmark")) benchmark(game,world,directory);
            for(int i=0;i<180;++i){
                worldFrame(game);Thread.sleep(8);
                if(!DirectBridge.error().isEmpty())throw new AssertionError(DirectBridge.error());
            }
            if(DirectSectionResidency.residentCount()!=200)throw new AssertionError("Incorrect loaded section residency: "+DirectSectionResidency.residentCount());
            File image=new File(directory,"world.png");DirectBridge.control(6,0,0,0,image.getAbsolutePath());
            long deadline=System.nanoTime()+3_000_000_000L;
            while(!image.isFile()&&System.nanoTime()<deadline)Thread.sleep(8);
            if(!image.isFile())throw new AssertionError("Missing screenshot");
            // Exercise first/third person, held items, inventories and block updates.
            game.z.A=true;game.h.c.a[0]=new iz(1,1,0);game.t.c.a();
            for(int i=0;i<4;++i){game.t.b(.5f);Display.update();}
            world.a(8,64,12,0,0);DirectGameWorld.invalidate(game.g,8,64,12,8,64,12);
            game.a(new ue(game.h));game.t.b(.5f);Display.update();
            game.g.a((fd)null);
            if(DirectSectionResidency.residentCount()!=0)throw new AssertionError("World unload retained sections");
            System.out.println("Direct in-memory world: terrain, vanilla entities, effects, held items, inventory and unload passed");
        }finally{
            if(game.U!=null)game.U.b();game.J=false;game.B.b();Display.destroy();
        }
        System.exit(0);
    }
}
