import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import mcrtx.bridge.DirectTerrainBridge;

/** Loaded section lifetimes are independent of vanilla chunk renderer/display-list objects. */
final class DirectSectionResidency {
    private static fd world;
    private static long generation,lifetime,nextScan;
    private static final Map<Long,Long> sections=new HashMap<Long,Long>();
    static fd attachedWorld(){return world;}
    static long generation(){return generation;}
    static int residentCount(){return sections.size();}
    static long lifetime(long key){Long value=sections.get(key);return value==null?0:value;}
    static boolean loaded(fd level,int x,int z){
        cl provider=level.w();return provider.a(x,z)&&!provider.b(x,z).h();
    }
    static void attach(fd next){
        world=next;sections.clear();++generation;nextScan=0;
        DirectSectionUpdates.resetForWorldChange();DirectTerrainBridge.resetTerrain(generation);
    }
    static void scan(double px,double pz,int setting){
        scan(px,pz,setting,System.nanoTime());
    }
    static void scan(double px,double pz,int setting,long now){
        if(world==null||now<nextScan)return;nextScan=now+100_000_000;
        int radius=Math.max(2,Math.min(16,(256>>Math.max(0,Math.min(3,setting)))/16));
        int cx=(int)Math.floor(px/16),cz=(int)Math.floor(pz/16);
        Set<Long> keep=new HashSet<Long>();
        for(int z=cz-radius;z<=cz+radius;++z)for(int x=cx-radius;x<=cx+radius;++x){
            if(!loaded(world,x,z))continue;
            for(int y=0;y<128;y+=16){
                long key=RemixChunkSectionKey.encode(x*16,y,z*16);keep.add(key);
                if(!sections.containsKey(key)){
                    sections.put(key,++lifetime);DirectTerrainBridge.allocateSection(x*16,y,z*16,generation,lifetime);
                    DirectSectionUpdates.queueRegion(x*16,y,z*16,x*16+15,y+15,z*16+15);
                }
            }
        }
        Iterator<Map.Entry<Long,Long>> iterator=sections.entrySet().iterator();
        while(iterator.hasNext()){
            Map.Entry<Long,Long> entry=iterator.next();if(keep.contains(entry.getKey()))continue;
            int x=RemixChunkSectionKey.originX(entry.getKey()),y=RemixChunkSectionKey.originY(entry.getKey()),z=RemixChunkSectionKey.originZ(entry.getKey());
            DirectSectionUpdates.queueRegion(x,y,z,x+15,y+15,z+15);
            DirectSectionUpdates.clearSection(x,y,z);DirectTerrainBridge.removeSection(x,y,z,generation,entry.getValue());iterator.remove();
        }
    }
}
