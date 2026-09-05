import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import mcrtx.bridge.UndergroundVisibility;
import mcrtx.bridge.UndergroundVisibilityWorker;

public final class UndergroundVisibilityWorkerTest {
    private static void check(boolean condition,String message) {
        if (!condition) throw new AssertionError(message);
    }
    private static UndergroundVisibilityWorker.Result ready(UndergroundVisibilityWorker worker) throws Exception {
        long deadline=System.nanoTime()+20_000_000_000L;
        while (worker.result()==null && System.nanoTime()<deadline) Thread.sleep(5);
        check(worker.result()!=null,"compiler timeout: "+worker.statistics());
        return worker.result();
    }
    private static boolean hidden(UndergroundVisibilityWorker.Result result, UndergroundVisibility visibility,int x,int y,int z) {
        UndergroundVisibility.Section section=visibility.get(x,y,z);
        int label=section.labels[UndergroundVisibility.index(x&15,y&15,z&15)];
        for (UndergroundVisibilityWorker.Mask mask:result.masks) {
            if (mask.x==section.x && mask.y==section.y && mask.z==section.z) {
                return label>=0 && (mask.hidden[label>>6]&(1L<<(label&63)))!=0;
            }
        }
        return false;
    }
    public static void main(String[] args) throws Exception {
        UndergroundVisibilityTest.World world=new UndergroundVisibilityTest.World();
        world.box(-70,40,4,33,44,9,(byte)0);
        world.box(-70,40,9,-65,44,57,(byte)0);
        world.box(-70,16,-40,-65,20,-35,(byte)0);
        world.upload();
        UndergroundVisibilityWorker worker=new UndergroundVisibilityWorker();
        try {
            for (UndergroundVisibility.Section section:world.visibility.sections()) {
                worker.submit(section.x,section.y,section.z,section.blocks);
            }
            double[] eye={48.5,42,7};
            worker.request(eye,eye,true,true);
            UndergroundVisibilityWorker.Result result=ready(worker);
            check(hidden(result,world.visibility,-68,18,-38),"sealed cave hidden by region query");
            check(!hidden(result,world.visibility,-68,42,7),"region retains straight entrance");
            check(hidden(result,world.visibility,-68,42,55),"region still trims distant bent branch: "+result.statistics);

            world.visibility.updateRegion(new int[]{48,42,0},new int[]{48,42,0},8,true);
            Map<Long,long[]> region=new HashMap<Long,long[]>();
            for (UndergroundVisibility.Section section:world.visibility.sections()) region.put(UndergroundVisibility.key(section.x,section.y,section.z),section.hidden.clone());
            // Interior samples are essential: eight corner rays alone do not establish a region PVS.
            for (double dx:new double[]{0.01,2.3,5.7,7.99}) for (double dz:new double[]{0.01,3.1,7.99}) {
                world.update(48+dx,42,dz,0); world.update(48+dx,42,dz,2_000_000_000L);
                for (UndergroundVisibility.Section section:world.visibility.sections()) {
                    long[] mask=region.get(UndergroundVisibility.key(section.x,section.y,section.z));
                    for (int word=0;word<mask.length;++word) check((mask[word]&~section.hidden[word])==0,"region hid point-visible pocket");
                }
            }

            long deadline=System.nanoTime()+20_000_000_000L;
            while (worker.queries()<5 && System.nanoTime()<deadline) Thread.sleep(10);
            check(worker.queries()==5,"bounded adjacent-region prefetch: "+worker.statistics());
            worker.drainTopology(Integer.MAX_VALUE);
            long queries=worker.queries();
            long start=System.nanoTime();
            for (int i=0;i<100000;++i) {
                worker.request(eye,eye,true,true);
                check(worker.result()==result,"stationary cache identity");
                check(worker.drainTopology(8).isEmpty(),"stationary topology upload");
            }
            System.out.println("100000 settled requests: "+(System.nanoTime()-start)/1000+" us");
            check(worker.queries()==queries,"stationary traversal repeated");
            double[] shifted={55.9,42.9,0.1};
            worker.request(shifted,shifted,true,true);
            check(worker.result()==result,"movement within the compiled volume missed the cache");
            double[] adjacent={56.5,42,7};
            worker.request(adjacent,adjacent,true,true);
            check(worker.result()!=null,"neighbor prefetch cache hit");

            double[] inside={-68,42,7};
            worker.request(inside,inside,true,true);
            check(!worker.current(result),"old exterior mask accepted inside");
            UndergroundVisibilityWorker.Result cave=ready(worker);
            check(!hidden(cave,world.visibility,-68,42,55),"connected cave restored");
            worker.request(eye,eye,true,true);
            result=ready(worker);
            byte[] changed=world.visibility.get(-68,42,55).blocks.clone();
            Arrays.fill(changed,(byte)0);
            worker.submit(-80,32,48,changed);
            Arrays.fill(changed,(byte)3);
            check(worker.result()==null && !worker.current(result),"edit failed to invalidate dependent cache");
            UndergroundVisibilityWorker.Result edited=ready(worker);
            check(edited!=result,"edit reused stale result");
            boolean copied=false;
            for (UndergroundVisibilityWorker.Topology topology:worker.drainTopology(Integer.MAX_VALUE)) {
                if (topology.x==-80 && topology.y==32 && topology.z==48) {
                    copied=true;
                    for (short label:topology.labels) check(label==0,"caller mutated worker snapshot");
                }
            }
            check(copied,"changed snapshot not published");
            worker.submit(-80,32,48,null);
            check(worker.result()==null,"unload kept stale result");
            ready(worker);
            worker.request(eye,eye,false,true);
            check(ready(worker).status.equals("Cave protection"),"dimension protection");
            double[] negative={-87.5,98.25,-39.5};
            worker.request(negative,negative,true,true);
            UndergroundVisibilityWorker.Result negativeResult=ready(worker);
            worker.request(new double[]{-81.01,98.99,-32.01},new double[]{-81.01,98.99,-32.01},true,true);
            check(worker.result()==negativeResult,"negative region rounding");
            worker.request(new double[]{-79.99,98.99,-32.01},negative,true,true);
            check(!worker.current(negativeResult),"third-person eye region change kept an old mask");
            worker.request(new double[]{Double.NaN,0,0},eye,true,true);
            check(worker.result()==null,"invalid camera failed open");
        } finally { worker.close(); worker.awaitStopped(2000); }
        check(!worker.isAlive(),"world reset left compiler running");
        System.out.println("Underground worker, whole-region visibility and stationary-cache tests passed");
    }
}
