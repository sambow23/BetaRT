import mcrtx.bridge.DirectTerrainBridge;

public final class TerrainQueueTest {
    private static long now;
    private static void check(boolean value,String message){if(!value)throw new AssertionError(message);}
    private static void scan(){DirectSectionResidency.scan(0,0,3,now+=200_000_000L);}
    private static void drain() throws Exception {
        for(int i=0;i<2000;++i){
            DirectSectionUpdates.flush();
            if(DirectSectionUpdates.lastPendingQueueDepthAfterFlush()==0)return;
            if(i%100==99)Thread.sleep(55);
        }
        throw new AssertionError("queue did not drain");
    }
    public static void main(String[] args)throws Exception {
        final fd world=new fd();DirectSectionResidency.attach(world);scan();
        check(DirectSectionResidency.residentCount()==200,"loaded chunks did not allocate all sections");
        world.loaded=false;DirectSectionUpdates.flush();
        check(world.reads==0,"unavailable chunk was read/generated");
        check(DirectSectionUpdates.lastPendingQueueDepthAfterFlush()==200,"unavailable section lost work");
        world.loaded=true;Thread.sleep(60);DirectTerrainBridge.accept=false;
        for(int i=0;i<30;++i)DirectSectionUpdates.flush();
        check(DirectTerrainBridge.updates>0,"terrain never attempted admission");
        int reads=world.reads;DirectSectionUpdates.flush();
        check(world.reads==reads,"backpressure repeated collection");
        check(DirectSectionUpdates.lastPendingQueueDepthAfterFlush()==200,"backpressure lost work");
        DirectTerrainBridge.accept=true;drain();
        check(DirectTerrainBridge.lastRecords.length==4096*DirectTerrainBridge.RECORD_WORDS,"initial section was partial");
        int updates=DirectTerrainBridge.updates;scan();drain();
        check(updates==DirectTerrainBridge.updates,"unchanged residency dirtied terrain");
        world.blocks[273]=1;DirectSectionUpdates.queueRegion(1,1,1,1,1,1);
        RemixChunkBlockCapture.duringCapture=()->{world.blocks[273]=0;DirectSectionUpdates.queueRegion(1,1,1,1,1,1);};
        drain();
        check(DirectTerrainBridge.lastRecords.length==27*DirectTerrainBridge.RECORD_WORDS,"partial bounds were lost");
        for(int i=0;i<27;++i)check(DirectTerrainBridge.lastRecords[i*DirectTerrainBridge.RECORD_WORDS]==0,"invalidated collection published stale data");
        long before=DirectSectionResidency.lifetime(RemixChunkSectionKey.encode(0,0,0));
        world.hole=true;scan();
        check(DirectSectionResidency.residentCount()==192&&DirectTerrainBridge.removals==8,"unloaded chunk retained sections");
        check(DirectSectionResidency.lifetime(RemixChunkSectionKey.encode(0,0,0))==0,"unloaded lifetime retained");
        world.hole=false;scan();
        check(DirectSectionResidency.lifetime(RemixChunkSectionKey.encode(0,0,0))>before,"coordinate reuse retained lifetime");
        drain();
        world.blocks[0]=1;DirectSectionUpdates.queueSection(0,0,0);
        final fd next=new fd();
        RemixChunkBlockCapture.duringCapture=()->{DirectSectionResidency.attach(next);scan();};
        drain();
        check(DirectSectionResidency.attachedWorld()==next,"world change lost");
        for(int value:DirectTerrainBridge.lastRecords)check(value==0,"collection crossed worlds");
        DirectSectionResidency.attach(null);drain();
        check(DirectSectionResidency.residentCount()==0,"world unload retained residency");
        System.out.println("Direct terrain residency, partial updates, backpressure, invalidation and world changes passed");
    }
}
