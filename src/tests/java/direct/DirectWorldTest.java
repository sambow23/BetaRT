import net.minecraft.client.Minecraft;
import org.lwjgl.opengl.Display;
import mcrtx.bridge.DirectBridge;
import java.io.File;
import javax.imageio.ImageIO;

/** Synthetic in-memory world: exercises vanilla gameplay render paths without modifying a save. */
public final class DirectWorldTest {
    private static final class World extends fd {
        World(){super(new wy(),"DirectTest",xa.a(0),1L);}
        public cl b(){return new uv(this);}
    }
    public static void main(String[] args)throws Exception {
        File directory=new File(args[0]);directory.mkdirs();Minecraft.af=directory;
        Minecraft game=new Minecraft(null,null,null,640,480,false){public void a(mh report){throw new AssertionError(report.a,report.b);}};
        game.k=new gr("DirectTest","0");
        try{
            game.a();game.z.e=3;game.z.z=false;game.z.j=false;game.z.i=0;
            World world=new World();world.B=true;
            for(int z=-2;z<=2;++z)for(int x=-2;x<=2;++x)((uv)world.w()).c(x,z);
            for(int z=-16;z<32;++z)for(int x=-16;x<32;++x)world.a(x,63,z,2,0);
            for(int x=3;x<14;++x)for(int y=64;y<67;++y)world.a(x,y,16,1+(x%5),0);
            world.a(5,64,12,20,0);world.a(6,64,12,50,0);world.a(7,64,12,51,0);
            world.a(9,64,12,8,0);world.a(10,64,12,10,0);world.a(12,64,12,18,0);
            game.f=world;game.h=new dc(game,world,game.k,0);game.h.c(8,64,5,0,8);game.i=game.h;
            game.h.a=new uo();
            game.c=new ob(game);game.g.a(world);game.j.a(world);game.r=null;game.N=false;
            th.a.f=new ra(game);
            String[] entities={"Pig","Sheep","Creeper","Zombie","Skeleton","Cow","Chicken","Wolf","Spider","Slime","Boat","Minecart"};
            for(int i=0;i<entities.length;++i){sn entity=jc.a(entities[i],world);if(entity!=null){entity.c(3+i%6*2,64,8+i/6*3,0,0);world.b(entity);}}
            game.g.a("smoke",8,65,10,0,0,0);game.g.a("flame",9,65,10,0,0,0);
            for(int i=0;i<180;++i){
                game.p.a();game.t.a();game.t.b(.5f);Display.update();Thread.sleep(8);
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
