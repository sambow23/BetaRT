package mcrtx.bridge;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Owns topology and portal traversal; callers only exchange immutable snapshots. */
public final class UndergroundVisibilityWorker implements AutoCloseable {
    public static final int REGION_SIZE = 8;
    private static final int CACHE_ENTRIES = 16;
    private static final long CACHE_BYTES = 32L*1024*1024;

    public static final class Topology {
        public final int x, y, z;
        public final long revision;
        public final short[] labels;
        Topology(int x, int y, int z, UndergroundVisibility.Section section) {
            this.x=x; this.y=y; this.z=z;
            revision=section == null ? 0 : section.revision;
            labels=section == null ? null : section.labels;
        }
    }

    public static final class Mask {
        public final int x, y, z;
        public final long revision;
        public final long[] hidden;
        Mask(UndergroundVisibility.Section section) {
            x=section.x; y=section.y; z=section.z; revision=section.revision;
            hidden=section.hidden.clone();
        }
    }

    public static final class Result {
        public final List<Mask> masks;
        public final String status, statistics;
        final Region region;
        final long generation, bytes;
        Result(Region region, long generation, UndergroundVisibility visibility) {
            this.region=region; this.generation=generation;
            status=visibility.status(); statistics=visibility.statistics();
            masks=new ArrayList<Mask>();
            long size=0;
            for (UndergroundVisibility.Section section : visibility.sections()) {
                masks.add(new Mask(section));
                size+=64+8*section.hidden.length;
            }
            bytes=size;
        }
    }

    private static final class Region {
        final int[] eye, player;
        final boolean supported;
        Region(double[] eye, double[] player, boolean supported) {
            this.eye=cell(eye); this.player=cell(player); this.supported=supported;
        }
        Region(Region source, int dx, int dz) {
            eye=source.eye.clone(); player=source.player.clone(); supported=source.supported;
            eye[0]+=dx; eye[2]+=dz; player[0]+=dx; player[2]+=dz;
        }
        private static int[] cell(double[] position) {
            int[] result=new int[3];
            for (int axis=0; axis<3; ++axis) {
                int size=axis==1?1:REGION_SIZE;
                result[axis]=(int)Math.floor(position[axis]/size)*size;
            }
            return result;
        }
        boolean contains(double[] e, double[] p, boolean dimension) {
            if (supported!=dimension) return false;
            for (int axis=0; axis<3; ++axis) {
                int size=axis==1?1:REGION_SIZE;
                if (!(e[axis]>=eye[axis] && e[axis]<eye[axis]+size
                        && p[axis]>=player[axis] && p[axis]<player[axis]+size)) return false;
            }
            return true;
        }
        @Override public int hashCode() { return (Arrays.hashCode(eye)*31+Arrays.hashCode(player))*31+(supported?1:0); }
        @Override public boolean equals(Object value) {
            if (!(value instanceof Region)) return false;
            Region other=(Region)value;
            return supported==other.supported && Arrays.equals(eye,other.eye) && Arrays.equals(player,other.player);
        }
    }

    private static final class Change {
        final int x,y,z;
        final byte[] blocks;
        Change(int x,int y,int z,byte[] blocks) {
            this.x=x; this.y=y; this.z=z; this.blocks=blocks==null?null:blocks.clone();
        }
    }

    private final UndergroundVisibility visibility=new UndergroundVisibility();
    private final Map<Long,Change> changes=new LinkedHashMap<Long,Change>();
    private final Map<Long,Topology> uploads=new LinkedHashMap<Long,Topology>();
    private final LinkedHashMap<Region,Result> cache=new LinkedHashMap<Region,Result>(16,0.75f,true);
    private final Thread thread;
    private Region requested;
    private Result published;
    private long generation, cacheBytes, queries, hits;
    private boolean settled, closed;
    private String failure;

    public UndergroundVisibilityWorker() {
        thread=new Thread(new Runnable() { public void run() { work(); } },"mcrtx-underground-compiler");
        thread.setDaemon(true);
        thread.setPriority(Thread.MIN_PRIORITY);
        thread.start();
    }

    public synchronized void submit(int x,int y,int z,byte[] blocks) {
        if (closed) return;
        changes.put(UndergroundVisibility.key(x,y,z),new Change(x,y,z,blocks));
        ++generation; published=null; cache.clear(); cacheBytes=0;
        notifyAll();
    }

    public synchronized void request(double[] eye,double[] player,boolean supported,boolean ready) {
        for (int axis=0; axis<3; ++axis) {
            if (!Double.isFinite(eye[axis]) || !Double.isFinite(player[axis])
                    || Math.abs(eye[axis])>30000000 || Math.abs(player[axis])>30000000) {
                requested=null; published=null; settled=false; return;
            }
        }
        if (requested==null || !requested.contains(eye,player,supported)) {
            requested=new Region(eye,player,supported);
            published=cache.get(requested);
            if (published!=null) ++hits;
            notifyAll();
        }
        if (settled!=ready) { settled=ready; notifyAll(); }
    }

    public synchronized Result result() { return settled ? published : null; }
    public synchronized boolean current(Result result) {
        return result!=null && settled && result.generation==generation && result.region.equals(requested);
    }
    public synchronized List<Topology> drainTopology(int limit) {
        if (uploads.isEmpty()) return Collections.emptyList();
        List<Topology> result=new ArrayList<Topology>();
        Iterator<Topology> iterator=uploads.values().iterator();
        while (iterator.hasNext() && result.size()<limit) { result.add(iterator.next()); iterator.remove(); }
        return result;
    }
    public synchronized int pending() { return changes.size()+uploads.size(); }
    public synchronized long queries() { return queries; }
    public synchronized String statistics() {
        return "queries="+queries+" cacheHits="+hits+" cachedRegions="+cache.size()+" pendingWorker="+pending()
            +(failure==null?"":" error="+failure);
    }
    public synchronized String status() { return failure==null ? "Compiling region (retaining geometry)" : "Compiler failed (retaining geometry)"; }

    private void work() {
        try {
            while (true) {
                List<Change> batch;
                Region region;
                long version;
                synchronized (this) {
                    while (!closed && changes.isEmpty() && nextRegion()==null) wait();
                    if (closed) return;
                    batch=new ArrayList<Change>(changes.values()); changes.clear();
                    region=batch.isEmpty()?nextRegion():null;
                    version=generation;
                }
                for (Change change : batch) {
                    if (Thread.currentThread().isInterrupted()) return;
                    UndergroundVisibility.Section old=visibility.get(change.x,change.y,change.z);
                    UndergroundVisibility.Section section=null;
                    if (change.blocks==null) visibility.remove(change.x,change.y,change.z);
                    else section=visibility.put(change.x,change.y,change.z,change.blocks);
                    if (section!=old) synchronized (this) {
                        uploads.put(UndergroundVisibility.key(change.x,change.y,change.z),new Topology(change.x,change.y,change.z,section));
                    }
                }
                if (region==null) continue;
                visibility.updateRegion(region.eye,region.player,REGION_SIZE,region.supported);
                Result result=new Result(region,version,visibility);
                synchronized (this) {
                    ++queries;
                    if (closed) return;
                    if (version!=generation) continue;
                    cache.put(region,result); cacheBytes+=result.bytes;
                    while (cache.size()>1 && (cache.size()>CACHE_ENTRIES || cacheBytes>CACHE_BYTES)) {
                        Iterator<Result> iterator=cache.values().iterator();
                        cacheBytes-=iterator.next().bytes; iterator.remove();
                    }
                    if (region.equals(requested)) published=result;
                }
            }
        } catch (InterruptedException stopped) {
            Thread.currentThread().interrupt();
        } catch (RuntimeException error) {
            synchronized (this) { if (closed) return; failure=error.getClass().getSimpleName(); published=null; }
            System.err.println("[mcrtx] Underground compiler stopped: "+error);
        }
    }

    private Region nextRegion() {
        if (!settled || requested==null) return null;
        if (!cache.containsKey(requested)) return requested;
        // Limit speculative work to four adjacent horizontal cells after the requested cell is ready.
        if (cacheBytes*2>CACHE_BYTES) return null;
        for (int[] offset : new int[][]{{-REGION_SIZE,0},{REGION_SIZE,0},{0,-REGION_SIZE},{0,REGION_SIZE}}) {
            Region neighbor=new Region(requested,offset[0],offset[1]);
            if (!cache.containsKey(neighbor)) return neighbor;
        }
        return null;
    }

    @Override public synchronized void close() {
        closed=true; published=null; changes.clear(); uploads.clear(); cache.clear();
        notifyAll(); thread.interrupt();
    }

    public void awaitStopped(long milliseconds) throws InterruptedException { thread.join(milliseconds); }
    public boolean isAlive() { return thread.isAlive(); }
}
