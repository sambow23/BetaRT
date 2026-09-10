import java.lang.reflect.Field;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.HashMap;
import java.util.Map;
import mcrtx.render.DirectRenderer;
import mcrtx.render.DirectTransforms;
import mcrtx.bridge.DirectPacket;

/** CPU-only checks of the exact bytes that the copied JNI mailbox owns. */
public final class DirectIdentityTest {
    private static final class ReusedNetworkId {
        public boolean equals(Object other){return other instanceof ReusedNetworkId;}
        public int hashCode(){return 7;}
    }
    private static long temporalWorld, temporalReset;
    private static void require(boolean value,String message){if(!value)throw new AssertionError(message);}
    private static Map<Long,float[]> capture(Object[] owners,DirectRenderer.Mesh mesh,double camera) throws Exception {
        return capture(owners,mesh,camera,false);
    }
    private static Map<Long,float[]> capture(Object[] owners,DirectRenderer.Mesh mesh,double camera,boolean recolor) throws Exception {
        DirectRenderer.beginFrame(96,96);DirectTransforms.reset();
        if(recolor){DirectRenderer.color=0xff00ff00;DirectRenderer.alphaCompare=6;DirectRenderer.alphaReference=128;}
        DirectRenderer.terrainView(camera,64,10000000);
        for(Object owner:owners){
            DirectRenderer.beginEntity(owner,true);DirectTransforms.loadIdentity();
            DirectTransforms.translate((float)(10000004-DirectRenderer.floatingOrigin[0]),0,0);
            DirectRenderer.draw(mesh,true,false,false);DirectRenderer.endEntity();
        }
        Field field=DirectRenderer.class.getDeclaredField("frame");field.setAccessible(true);
        ByteBuffer source=((DirectPacket)field.get(null)).buffer();ByteBuffer packet=source.duplicate().order(ByteOrder.LITTLE_ENDIAN);
        packet.limit(source.position());packet.position(0);
        require(packet.getInt()==0x54435244&&packet.getInt()==5,"Protocol v5");
        Map<Long,float[]> result=new HashMap<Long,float[]>();
        while(packet.remaining()>0){
            int operation=packet.getInt(),size=packet.getInt(),end=packet.position()+size;
            if(operation==16){
                require(size==188,"Complete v5 camera metadata");
                int kind=packet.getInt();packet.position(packet.position()+128);
                long world=packet.getLong(),reset=packet.getLong();
                if(kind==0){temporalWorld=world;temporalReset=reset;}
            }
            if(operation==17){
                packet.getLong();long id=packet.getLong();require(id!=0,"Stable entity id");
                require(packet.getInt()==1&&packet.getInt()==1,"Dynamic instance/raster visibility");
                float[] model=new float[12];for(int i=0;i<12;++i)model[i]=packet.getFloat();result.put(id,model);
            }
            packet.position(end);
        }
        Field inFrame=DirectRenderer.class.getDeclaredField("inFrame");inFrame.setAccessible(true);inFrame.setBoolean(null,false);
        return result;
    }
    public static void main(String[] args) throws Exception {
        DirectRenderer.reset();Object a=new ReusedNetworkId(),b=new ReusedNetworkId(),part=new Object();
        float[] vertices={0,0,0,0,0,1,0,0,0,0,0,1,0,0,0};int[] colors={-1,-1,-1},indices={0,1,2};
        DirectRenderer.Mesh mesh=new DirectRenderer.Mesh(vertices,colors,indices).identifyPart(part);
        Map<Long,float[]> first=capture(new Object[]{a,b},mesh,10000000);
        long firstWorld=temporalWorld,firstReset=temporalReset;
        require(first.size()==2,"Reused vanilla network IDs collapsed separate entity lifetimes");
        Map<Long,float[]> reordered=capture(new Object[]{b,a},mesh,10000000.5);
        require(temporalWorld==firstWorld&&temporalReset==firstReset,"Camera motion changed reset epochs");
        require(first.keySet().equals(reordered.keySet()),"Draw order changed persistent identities");
        for(Long id:first.keySet())require(java.util.Arrays.equals(first.get(id),reordered.get(id)),"Camera movement leaked into object transform");
        DirectRenderer.Mesh replacement=new DirectRenderer.Mesh(vertices.clone(),colors.clone(),indices.clone()).identifyPart(part);
        require(capture(new Object[]{a,b},replacement,10000001).keySet().equals(first.keySet()),"Mesh replacement changed model-part lifetime");
        require(capture(new Object[]{a,b},replacement,10000001,true).keySet().equals(first.keySet()),"Tint/alpha changes replaced entity identities");
        Map<Long,float[]> rebased=capture(new Object[]{a,b},replacement,10002000);
        require(temporalWorld==firstWorld&&temporalReset>firstReset,"Rebase did not persist reset epoch");
        long rebasedReset=temporalReset;
        DirectRenderer.cameraCut();capture(new Object[]{a,b},replacement,10002000);
        require(temporalReset==rebasedReset+1,"Explicit camera cut was not transported");
        require(rebased.keySet().equals(first.keySet()),"Rebase changed identities");
        for(Long id:first.keySet())require(!java.util.Arrays.equals(first.get(id),rebased.get(id)),"Rebase did not move instance transforms");
        DirectRenderer.worldChanged();
        require(!capture(new Object[]{a,b},replacement,10002000).keySet().equals(first.keySet()),"World change retained identities");
        require(temporalWorld>firstWorld&&temporalReset>rebasedReset,"World change metadata was not transported");
        System.out.println("Stable identities, reorder, replacement, camera separation, cuts and rebase epochs passed");
    }
}
