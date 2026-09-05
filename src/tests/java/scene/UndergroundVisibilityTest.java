import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import mcrtx.bridge.UndergroundVisibility;

public final class UndergroundVisibilityTest {
    static final class World {
        final UndergroundVisibility visibility = new UndergroundVisibility();
        final Map<Long, byte[]> cells = new HashMap<Long, byte[]>();
        World() {
            for (int z=-64; z<=64; z+=16) for (int x=-96; x<=80; x+=16) for (int y=0; y<128; y+=16) {
                byte[] blocks = new byte[4096];
                for (int cell=0; cell<4096; ++cell) {
                    int by=y+((cell>>4)&15);
                    blocks[cell] = by < (x<32 ? 96 : 24) ? (byte)3 : 0;
                }
                cells.put(UndergroundVisibility.key(x,y,z),blocks);
            }
        }
        void box(int x0,int y0,int z0,int x1,int y1,int z1,byte flags) {
            for (int z=z0;z<=z1;++z) for (int y=y0;y<=y1;++y) for (int x=x0;x<=x1;++x) {
                cells.get(UndergroundVisibility.key(x,y,z))[UndergroundVisibility.index(x&15,y&15,z&15)] = flags;
            }
        }
        void upload() {
            for (Map.Entry<Long,byte[]> entry:cells.entrySet()) {
                long key=entry.getKey();
                int x=(int)(key>>37);
                if ((x&0x2000000)!=0) x|=~0x3FFFFFF;
                x<<=4;
                int z=(int)((key>>11)&0x3FFFFFFL);
                if ((z&0x2000000)!=0) z|=~0x3FFFFFF;
                int y=(int)(key&0x7FF)<<4;
                visibility.put(x,y,z<<4,entry.getValue());
            }
        }
        void update(double x,double y,double z,long time) {
            double[] p={x,y,z}; visibility.update(p,p,true,time);
        }
        boolean hidden(int x,int y,int z) {
            UndergroundVisibility.Section s=visibility.get(x,y,z);
            int cell=UndergroundVisibility.index(x&15,y&15,z&15);
            int label=s.labels[cell];
            return label>=0 && (s.hidden[label>>6]&(1L<<(label&63)))!=0;
        }
    }
    public static void main(String[] args) {
        World world=new World();
        world.box(-70,40,4,-65,44,9,(byte)0);
        world.upload();
        world.update(48,42,7,0); world.update(48,42,7,1_000_000_000L);
        check(world.hidden(-68,42,7),"sealed cave removed");
        check(world.visibility.status().equals("Exterior trimming"),"outside classification");
        long[] same=world.visibility.get(-68,42,7).hidden.clone();
        world.update(48,42,7,2_000_000_000L);
        check(Arrays.equals(same,world.visibility.get(-68,42,7).hidden),"stationary visibility stable");

        world.box(-70,40,4,33,44,9,(byte)0); world.upload();
        world.update(48,42,7,3_000_000_000L);
        check(!world.hidden(-68,42,7),"long straight entrance remains visible");
        world.visibility.remove(-32,32,0);
        world.update(48,42,7,3_500_000_000L);
        check(!world.hidden(-68,42,7),"unknown gap cannot hide a straight entrance");
        world.upload();
        world.box(-70,40,9,-65,44,57,(byte)0); world.upload();
        world.update(48,42,7,4_000_000_000L); world.update(48,42,7,5_000_000_000L);
        check(world.hidden(-68,42,55),"deep bent branch trimmed: "+world.visibility.statistics());
        world.update(-68,42,7,6_000_000_000L);
        check(!world.hidden(-68,42,55),"full connected cave restored immediately inside");
        check(world.visibility.status().equals("Cave protection"),"inside protection status");

        world.box(40,48,0,60,48,15,UndergroundVisibility.SOLID); world.upload();
        check(world.visibility.isExterior(48,42,7),"surface constructed roof is not a cave");
        check(!world.visibility.isExterior(-68,42,7),"natural terrain roof protects caves");
        world.update(48,42,7,7_000_000_000L);
        check(!world.hidden(-68,42,55),"exterior transition has delay");
        world.update(48,42,7,8_000_000_000L);
        check(world.hidden(-68,42,55),"exterior trimming resumes");
        world.visibility.invalidate(-80,32,48);
        world.update(48,42,7,9_000_000_000L);
        check(!world.hidden(-68,42,55),"dirty topology is retained");
        world.visibility.remove(-80,32,48);
        world.update(48,42,7,10_000_000_000L);
        check(!world.hidden(-68,42,20),"missing neighbors are conservative");
        double[] eye={48,42,7},player={-68,42,7};
        world.visibility.update(eye,player,true,11_000_000_000L);
        check(world.visibility.status().equals("Cave protection"),"third person uses player too");
        world.visibility.update(eye,eye,false,12_000_000_000L);
        check(world.visibility.status().equals("Cave protection"),"unsupported dimension");
        world.upload();
        world.update(48,42,7,13_000_000_000L); world.update(48,42,7,14_000_000_000L);
        check(world.hidden(-68,42,55),"reloaded neighbor stops retaining stale unknown pockets");
        long revision=world.visibility.get(-68,42,55).revision;
        world.visibility.invalidate(-80,32,48);
        world.upload();
        check(world.visibility.get(-68,42,55).revision==revision,"unchanged dirty section keeps its revision");
        world.update(48,42,7,15_000_000_000L);
        check(world.hidden(-68,42,55),"unchanged upload clears dirty uncertainty");
        World rebuilt=new World();
        rebuilt.cells.clear(); rebuilt.cells.putAll(world.cells); rebuilt.upload();
        rebuilt.update(48,42,7,14_000_000_000L); rebuilt.update(48,42,7,15_000_000_000L);
        for (UndergroundVisibility.Section section:world.visibility.sections()) {
            check(Arrays.equals(section.hidden,rebuilt.visibility.get(section.x,section.y,section.z).hidden),
                "incremental graph matches a fresh graph at "+section.x+","+section.y+","+section.z);
        }
        for (int id:new int[]{0,8,9,18,20,44,53,64,67,79,85,96,255}) {
            check((UndergroundVisibility.blockFlags(id)&UndergroundVisibility.SOLID)==0,"non-sealing block "+id);
        }
        check((UndergroundVisibility.blockFlags(1)&UndergroundVisibility.TERRAIN)!=0,"stone is terrain");
        world.visibility.clear();
        world.update(0,64,0,16_000_000_000L);
        check(world.visibility.status().equals("Waiting for topology"),"world reset");
        System.out.println("Underground visibility tests passed");
    }
    private static void check(boolean value,String message) {
        if (!value) throw new AssertionError(message);
    }
}
