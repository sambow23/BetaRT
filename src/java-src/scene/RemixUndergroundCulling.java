import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import mcrtx.bridge.HookProfiler;
import mcrtx.bridge.McrtxGraphicsSettings;
import mcrtx.bridge.RemixChunkBridge;
import mcrtx.bridge.UndergroundVisibility;
import mcrtx.bridge.UndergroundVisibilityWorker;

final class RemixUndergroundCulling {
    private static final Map<Long,Integer> columns=new HashMap<Long,Integer>();
    private static final Set<Long> pending=new LinkedHashSet<Long>();
    private static final Map<Long,byte[]> sampled=new HashMap<Long,byte[]>();
    private static final Map<Long,long[]> sentHidden=new HashMap<Long,long[]>();
    private static UndergroundVisibilityWorker worker;
    private static UndergroundVisibilityWorker.Result applying, applied;
    private static Iterator<UndergroundVisibilityWorker.Mask> masks;
    private static boolean active, nativeEnabled;
    private static long lastLog, readySince, topologyUploads, maskUploads;
    private static String status="Waiting for topology";

    private RemixUndergroundCulling() { }

    static boolean enabled() { return McrtxGraphicsSettings.isUndergroundCullingEnabled(); }
    static String status() { return enabled()?status:"OFF"; }

    private static void setNative(boolean value) {
        if (nativeEnabled!=value) {
            nativeEnabled=value;
            RemixChunkBridge.setUndergroundCullingEnabled(value);
        }
    }

    private static void queueColumn(int x,int z) {
        for (int y=0;y<128;y+=16) pending.add(RemixChunkSectionKey.encode(x,y,z));
    }

    static void remember(int x,int y,int z) {
        long key=RemixChunkSectionKey.encode(x,0,z);
        Integer mask=columns.get(key);
        columns.put(key,(mask==null?0:mask)|(1<<(y>>4)));
        if (mask==null) queueColumn(x,z);
    }

    static void invalidate(int minX,int minY,int minZ,int maxX,int maxY,int maxZ) {
        if (!active || maxY<0 || minY>=128) return;
        fd world=RemixChunkWorldState.attachedWorld();
        long volume=(long)(maxX-minX+1)*(maxY-minY+1)*(maxZ-minZ+1);
        if (world!=null && volume>0 && volume<=64) {
            boolean changed=false;
            for (int z=minZ;z<=maxZ;++z) for (int y=Math.max(0,minY);y<=Math.min(127,maxY);++y) for (int x=minX;x<=maxX;++x) {
                byte[] old=sampled.get(RemixChunkSectionKey.encode(x,y,z));
                if (old==null || !world.i(x,y,z) || old[UndergroundVisibility.index(x&15,y&15,z&15)]
                        !=UndergroundVisibility.blockFlags(world.a(x,y,z))) changed=true;
            }
            // Light/render notifications often leave connectivity unchanged.
            if (!changed) return;
        }
        for (int z=minZ>>4;z<=maxZ>>4;++z) for (int x=minX>>4;x<=maxX>>4;++x) {
            if (!columns.containsKey(RemixChunkSectionKey.encode(x<<4,0,z<<4))) continue;
            for (int y=Math.max(0,minY)>>4;y<=(Math.min(127,maxY)>>4);++y) {
                pending.add(RemixChunkSectionKey.encode(x<<4,y<<4,z<<4));
            }
        }
        if (!pending.isEmpty()) setNative(false);
    }

    static void clear() {
        if (worker!=null) worker.close();
        worker=null; columns.clear(); pending.clear(); sampled.clear(); sentHidden.clear();
        applying=applied=null; masks=null; active=false; nativeEnabled=false;
        status="Waiting for topology"; topologyUploads=maskUploads=0;
    }

    static void unload(int x,int y,int z) {
        long column=RemixChunkSectionKey.encode(x,0,z);
        Integer mask=columns.get(column);
        if (mask==null) return;
        int next=mask&~(1<<(y>>4));
        if (next!=0) { columns.put(column,next); return; }
        columns.remove(column);
        setNative(false);
        for (int by=0;by<128;by+=16) {
            long key=RemixChunkSectionKey.encode(x,by,z);
            pending.remove(key); sampled.remove(key); sentHidden.remove(key);
            if (worker!=null) worker.submit(x,by,z,null);
        }
    }

    static void update(fd world) {
        boolean next=enabled();
        if (next!=active) {
            setNative(false); active=next;
            RemixChunkBridge.resetUndergroundCulling();
            if (worker!=null) worker.close();
            worker=next?new UndergroundVisibilityWorker():null;
            applying=applied=null; masks=null; sampled.clear(); sentHidden.clear();
            pending.clear();
            for (long key:columns.keySet()) queueColumn(RemixChunkSectionKey.originX(key),RemixChunkSectionKey.originZ(key));
            RemixCaveCulling.clear();
            RemixChunkWorldState.recaptureKnownSections();
        }
        if (!next || world==null) return;
        long start=HookProfiler.begin();
        try {
            if (!pending.isEmpty()) setNative(false);
            long deadline=System.nanoTime()+2_000_000L;
            for (int count=0;count<8 && !pending.isEmpty();++count) {
                Iterator<Long> iterator=pending.iterator();
                long key=iterator.next(); iterator.remove();
                int x=RemixChunkSectionKey.originX(key), y=RemixChunkSectionKey.originY(key), z=RemixChunkSectionKey.originZ(key);
                byte[] blocks=null;
                if (world.i(x,0,z)) {
                    blocks=new byte[4096];
                    for (int by=0;by<16;++by) for (int bz=0;bz<16;++bz) for (int bx=0;bx<16;++bx) {
                        blocks[UndergroundVisibility.index(bx,by,bz)]=UndergroundVisibility.blockFlags(world.a(x+bx,y+by,z+bz));
                    }
                }
                if (!Arrays.equals(sampled.get(key),blocks)) {
                    if (blocks==null) sampled.remove(key); else sampled.put(key,blocks);
                    worker.submit(x,y,z,blocks);
                }
                if (System.nanoTime()>=deadline) break;
            }

            double[] eye=RemixCameraState.cullingEye();
            double[] player={RemixCameraState.cameraPositionX,RemixCameraState.cameraPositionY,RemixCameraState.cameraPositionZ};
            worker.request(eye,player,!(world.t instanceof wd),pending.isEmpty());
            for (UndergroundVisibilityWorker.Topology section:worker.drainTopology(8)) {
                RemixChunkBridge.updateUndergroundTopology(section.x,section.y,section.z,section.revision,section.labels);
                ++topologyUploads;
                sentHidden.remove(RemixChunkSectionKey.encode(section.x,section.y,section.z));
                if (section.revision!=0) RemixChunkWorldState.recaptureTopologySection(section.x,section.y,section.z);
            }

            UndergroundVisibilityWorker.Result result=worker.result();
            if (result==null || worker.pending()!=0) {
                setNative(false); applying=null; masks=null; status=worker.status();
            } else if (result!=applied) {
                setNative(false);
                if (result!=applying) { applied=null; applying=result; masks=result.masks.iterator(); readySince=System.nanoTime(); }
                deadline=System.nanoTime()+1_000_000L;
                for (int count=0;count<128 && masks.hasNext();++count) {
                    UndergroundVisibilityWorker.Mask section=masks.next();
                    long key=RemixChunkSectionKey.encode(section.x,section.y,section.z);
                    if (!Arrays.equals(sentHidden.get(key),section.hidden)) {
                        RemixChunkBridge.updateUndergroundVisibility(section.x,section.y,section.z,section.revision,section.hidden);
                        sentHidden.put(key,section.hidden); ++maskUploads;
                    }
                    if (System.nanoTime()>=deadline) break;
                }
                status="Preparing region (retaining geometry)";
                if (!masks.hasNext() && System.nanoTime()-readySince>=500_000_000L && worker.current(result)) {
                    applied=result; applying=null; masks=null; setNative(true); status=result.status;
                }
            } else {
                setNative(true); status=result.status;
            }
            if (System.nanoTime()-lastLog>2_000_000_000L) {
                lastLog=System.nanoTime();
                System.out.println("[mcrtx] Underground: "+(result==null?status:result.statistics)
                    +" pendingColumns="+((pending.size()+7)/8)+" "+worker.statistics()
                    +" topologyUploads="+topologyUploads+" maskUploads="+maskUploads+" active="+nativeEnabled);
            }
        } finally { HookProfiler.endHook("hook.undergroundVisibility",start); }
    }
}
