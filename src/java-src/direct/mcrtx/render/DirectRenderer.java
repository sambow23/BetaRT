package mcrtx.render;

import mcrtx.bridge.DirectBridge;
import mcrtx.bridge.DirectPacket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.LinkedHashMap;

/** Game-owned meshes and drawing state. Resource bytes are sent only when their contents change. */
public final class DirectRenderer {
    public static final DirectPacket resources = new DirectPacket();
    private static final DirectPacket frame = new DirectPacket();
    private static final ArrayList<Long> transientMeshes = new ArrayList<Long>();
    private static final ArrayList<Long> retiredMeshes = new ArrayList<Long>();
    private static final ArrayList<UiVertex> uiVertices = new ArrayList<UiVertex>();
    private static final ArrayList<Integer> uiIndices = new ArrayList<Integer>();
    private static final ArrayList<UiCommand> uiCommands = new ArrayList<UiCommand>();
    private static final Map<Long, Long> materials = new HashMap<Long, Long>();
    // Vanilla sprite/item producers can emit the same local mesh each frame.
    // Content equality keeps those meshes persistent, with bounded residency.
    private static final LinkedHashMap<GeometryKey,Mesh> generated = new LinkedHashMap<GeometryKey,Mesh>(64,.75f,true);
    private static long generatedBytes;
    private static long nextId = 1024;
    private static long epoch;
    public static final double[] floatingOrigin=new double[3];
    private static final double[] cameraOrigin=new double[3];
    private static boolean originValid, worldCamera;
    private static long worldEpoch, historyResetEpoch;
    public static boolean environmentEnabled;
    public static float[] environment=new float[]{0,0,0,0,-1,0,0,0,0};
    public static void cameraCut(){++historyResetEpoch;}
    private static float[] worldView=new float[16], inverseView=new float[16];
    public static boolean objectSpace, rasterVisible=true;
    private static int rasterEffectDepth;
    private static final java.lang.ref.ReferenceQueue<Object> deadEntities=new java.lang.ref.ReferenceQueue<Object>();
    private static final HashMap<EntityIdentity,EntityState> entities=new HashMap<EntityIdentity,EntityState>();
    // Vanilla Entity.equals/hashCode use a mutable, reusable network ID. Render
    // lifetimes must instead follow the Java object without retaining it.
    private static final class EntityIdentity extends java.lang.ref.WeakReference<Object> {
        private final int hash;
        EntityIdentity(Object owner,java.lang.ref.ReferenceQueue<Object> queue){super(owner,queue);hash=System.identityHashCode(owner);}
        public int hashCode(){return hash;}
        public boolean equals(Object other){return this==other||(other instanceof EntityIdentity&&get()!=null&&get()==((EntityIdentity)other).get());}
    }
    private static EntityState entity;
    private static final java.util.WeakHashMap<Object,Long> modelParts=new java.util.WeakHashMap<Object,Long>();
    private static final class EntityState {
        final Map<String,Long> instances=new HashMap<String,Long>();
        final Map<String,Integer> passes=new HashMap<String,Integer>();
    }
    public static void worldChanged(){++worldEpoch;cameraCut();entities.clear();entity=null;originValid=false;worldCamera=false;}
    public static void beginEntity(Object owner,boolean visible){
        java.lang.ref.Reference<?> dead;while((dead=deadEntities.poll())!=null)entities.remove(dead);
        EntityIdentity identity=new EntityIdentity(owner,deadEntities);
        entity=entities.get(identity);if(entity==null){entity=new EntityState();entities.put(identity,entity);}
        entity.passes.clear();objectSpace=true;rasterVisible=visible;
    }
    public static void endEntity(){entity=null;objectSpace=false;rasterVisible=true;rasterEffectDepth=0;}
    public static void beginRasterEffect(){++rasterEffectDepth;}
    public static void endRasterEffect(){if(rasterEffectDepth>0)--rasterEffectDepth;}
    private static long instanceIdentity(Mesh mesh){
        if(entity==null)return 0;
        // Texture/blend distinguish vanilla material passes. Animated tint and
        // alpha-test values change shading, never the model part's lifetime.
        String part=mesh.identity+":"+texture+":"+blendEnabled;
        Integer pass=entity.passes.get(part);int ordinal=pass==null?0:pass;
        entity.passes.put(part,ordinal+1);String key=part+":"+ordinal;
        Long id=entity.instances.get(key);if(id==null){id=allocateId();entity.instances.put(key,id);}return id;
    }
    private static int transientIndex;
    private static boolean inFrame;
    public static boolean ui, textureEnabled = true, depthEnabled = true, cullEnabled, blendEnabled, alphaEnabled = true;
    public static int color = 0xffffffff, texture, layer, width = 1280, height = 720;
    public static int alphaCompare = 4, alphaReference = 25, sourceBlend = 6, destinationBlend = 7;
    public static boolean depthWrite=true, polygonOffset, colorWrites=true;
    public static int depthCompare=3;
    private static int lastDepthState=-1;
    private static final boolean[] cameraSent = new boolean[3];

    private DirectRenderer() { }
    public static long allocateId() {
        if (nextId == Long.MAX_VALUE) throw new IllegalStateException("Resource identifier overflow");
        return nextId++;
    }
    public static void reset() {
        ++epoch;
        worldChanged();modelParts.clear();
        resources.clear(); frame.clear();
        transientMeshes.clear(); retiredMeshes.clear(); materials.clear();
        generated.clear();generatedBytes=0;
        uiVertices.clear(); uiIndices.clear(); uiCommands.clear();
        DirectTransforms.reset();
        DirectTextures.reset();
        nextId = 1024; inFrame = false; texture = 0;
    }
    public static void beginFrame(int drawableWidth, int drawableHeight) {
        if (inFrame) return;
        inFrame = true;
        width = Math.max(1, drawableWidth); height = Math.max(1, drawableHeight);
        frame.clear(); uiVertices.clear(); uiIndices.clear(); uiCommands.clear();
        transientIndex = 0; Arrays.fill(cameraSent, false);
        worldCamera=false;objectSpace=false;entity=null;rasterEffectDepth=0;rasterVisible=true;
        environmentEnabled=false;environment=new float[]{0,0,0,0,-1,0,0,0,0};
        for (long id : retiredMeshes) {
            int command = resources.command(6); resources.id(id); resources.end(command);
        }
        retiredMeshes.clear();
        ui = false; layer = 0; color = 0xffffffff;
        textureEnabled=true;depthEnabled=true;cullEnabled=false;blendEnabled=false;alphaEnabled=true;
        alphaCompare=4;alphaReference=25;sourceBlend=6;destinationBlend=7;
        depthWrite=true;depthCompare=3;lastDepthState=-1;polygonOffset=false;colorWrites=true;
    }
    public static void present() {
        if (!inFrame || !DirectBridge.isRunning()) return;
        writeUi();
        DirectTextures.flush();
        if (!DirectBridge.submit(resources.buffer(), frame.buffer()))
            throw new IllegalStateException("Direct frame submission failed: " + DirectBridge.error());
        resources.clear(); inFrame = false;
    }
    public static void retire(long mesh) { retiredMeshes.add(mesh); }
    public static long material(int textureId) {
        long key = textureId & 0xffffffffL;
        Long found = materials.get(key);
        if (found != null) return found;
        long id = allocateId(); materials.put(key, id);
        writeMaterial(id, textureId); return id;
    }
    public static void writeMaterial(long id, int textureId) {
        DirectTextures.Texture image=textureId==0?null:DirectTextures.pixels(textureId);
        float emission=image!=null&&!image.pbr[3].isEmpty()?1:0;
        writeMaterial(id,textureId,0,emission,emission,emission);
    }
    public static void writeMaterial(long id,int textureId,int emissionTexture,float r,float g,float b) {
        DirectTextures.Texture image = DirectTextures.get(textureId);
        DirectTextures.Texture emissive=DirectTextures.get(emissionTexture);
        int command = resources.command(2);
        resources.id(id); resources.id(textureId == 0 ? 0 : image.id);
        resources.integer(image == null ? 0 : image.filter);
        resources.integer(image == null ? 1 : image.wrapU); resources.integer(image == null ? 1 : image.wrapV);
        resources.id(emissive==null?0:emissive.id);
        resources.number(r);resources.number(g);resources.number(b);
        for(int i=0;i<4;++i)resources.text(image==null?"":image.pbr[i]);
        resources.end(command);
    }
    public static void textureStateChanged(int textureId) {
        Long id = materials.get(textureId & 0xffffffffL);
        if (id != null) writeMaterial(id, textureId);
    }
    public static void textureDeleted(int textureId) {
        Long id=materials.remove(textureId & 0xffffffffL);
        if (id!=null) {
            int at=resources.command(5); resources.id(id); resources.end(at);
        }
    }
    public static final class Mesh {
        private long identity=allocateId();
        public Mesh identifyPart(Object part){
            Long id=modelParts.get(part);if(id==null){id=allocateId();modelParts.put(part,id);}identity=id;return this;
        }
        public final float[] vertices; // xyzuv
        public final int[] colors, indices;
        private final Map<Long, Long> variants = new HashMap<Long, Long>();
        private long generation=epoch;
        public Mesh(float[] vertices, int[] colors, int[] indices) {
            if (vertices.length != colors.length * 5 || indices.length % 3 != 0)
                throw new IllegalArgumentException("Invalid mesh arrays");
            this.vertices = vertices; this.colors = colors; this.indices = indices;
        }
        public void dispose() { if(generation==epoch)for (long id : variants.values()) retire(id); variants.clear(); }
    }
    private static final class GeometryKey {
        final Mesh mesh; final int hash;
        GeometryKey(Mesh mesh){this.mesh=mesh;hash=(Arrays.hashCode(mesh.vertices)*31+Arrays.hashCode(mesh.colors))*31+Arrays.hashCode(mesh.indices);}
        public int hashCode(){return hash;}
        public boolean equals(Object value){
            if(!(value instanceof GeometryKey))return false;GeometryKey other=(GeometryKey)value;
            return Arrays.equals(mesh.vertices,other.mesh.vertices)&&Arrays.equals(mesh.colors,other.mesh.colors)&&Arrays.equals(mesh.indices,other.mesh.indices);
        }
    }
    private static long size(Mesh mesh){return 4L*(mesh.vertices.length+mesh.colors.length+mesh.indices.length);}
    public static void drawGenerated(Mesh mesh,boolean vertexColor,boolean modelDepth){
        if(ui||size(mesh)>1024*1024){draw(mesh,false,vertexColor,modelDepth);return;}
        GeometryKey key=new GeometryKey(mesh);Mesh previous=generated.get(key);
        if(previous==null){
            generated.put(key,mesh);generatedBytes+=size(mesh);
            while(generated.size()>512||generatedBytes>8*1024*1024){
                Map.Entry<GeometryKey,Mesh> oldest=generated.entrySet().iterator().next();
                generatedBytes-=size(oldest.getValue());oldest.getValue().dispose();generated.remove(oldest.getKey());
            }
        }else mesh=previous;
        draw(mesh,true,vertexColor,modelDepth);
    }
    private static long upload(Mesh mesh, long id, long material) {
        int command = resources.command(3);
        resources.id(id); resources.id(material); resources.integer(mesh.colors.length); resources.integer(mesh.indices.length);
        for (int i = 0; i < mesh.colors.length; ++i) {
            for (int j = 0; j < 5; ++j) resources.number(mesh.vertices[i * 5 + j]);
            resources.integer(mesh.colors[i]);
        }
        // The retained Remix raster API uses clockwise fronts. Vanilla local
        // geometry uses OpenGL's opposite winding; UI retains its painter order.
        for (int i=0;i<mesh.indices.length;i+=3){
            resources.integer(mesh.indices[i]);resources.integer(mesh.indices[i+2]);resources.integer(mesh.indices[i+1]);
        }
        resources.end(command); return id;
    }
    public static void draw(Mesh mesh, boolean persistent, boolean vertexColor, boolean modelDepth) {
        if (mesh.indices.length == 0) return;
        if(mesh.generation!=epoch){mesh.variants.clear();mesh.generation=epoch;}
        float[] uv=DirectTransforms.texture();
        if(uv[0]!=1||uv[1]!=0||uv[4]!=0||uv[5]!=1||uv[12]!=0||uv[13]!=0){
            float[] vertices=mesh.vertices.clone();
            for(int i=0;i<mesh.colors.length;++i){
                float u=vertices[i*5+3],v=vertices[i*5+4];
                vertices[i*5+3]=uv[0]*u+uv[4]*v+uv[12];vertices[i*5+4]=uv[1]*u+uv[5]*v+uv[13];
            }
            mesh=new Mesh(vertices,mesh.colors,mesh.indices);persistent=false;
        }
        if (ui) { appendUi(mesh, vertexColor, modelDepth); return; }
        if (!inFrame) return;
        if (!cameraSent[layer]) camera(layer);
        int textureId = textureEnabled ? texture : 0;
        long material = material(textureId);
        long meshId;
        if (persistent) {
            Long found = mesh.variants.get(material);
            if (found == null) {
                found = upload(mesh, allocateId(), material); mesh.variants.put(material, found);
            }
            meshId = found;
        } else {
            if (transientIndex == transientMeshes.size()) transientMeshes.add(allocateId());
            meshId = upload(mesh, transientMeshes.get(transientIndex++), material);
        }
        int depthState=(depthEnabled?1:0)|(depthWrite?2:0)|(depthCompare<<2)|(colorWrites?0:32);
        if(depthState!=lastDepthState){
            int at=frame.command(20);frame.integer(depthEnabled?1:0);frame.integer(depthWrite?1:0);frame.integer(depthCompare);frame.integer(colorWrites?0:1);frame.end(at);lastDepthState=depthState;
        }
        int command = frame.command(17); frame.id(meshId);
        boolean traced=entity!=null&&layer==0&&rasterEffectDepth==0&&depthCompare!=2&&!polygonOffset&&colorWrites;
        frame.id(traced?instanceIdentity(mesh):0);frame.integer(traced?1:2);frame.integer(rasterVisible?1:0);
        float[] model = DirectTransforms.model();
        if(layer==0&&worldCamera&&!objectSpace){
            float[] object=new float[16];DirectTransforms.multiply(inverseView,model,object);
            for(int i=0;i<3;++i)object[12+i]+=(float)(cameraOrigin[i]-floatingOrigin[i]);model=object;
        }
        for (int row = 0; row < 3; ++row)
            for (int column = 0; column < 4; ++column) frame.number(model[column * 4 + row]);
        // Category masks retain the public Remix 0.1003 values.
        frame.integer((layer == 1 ? DirectConstants.SKY : layer == 2 ? DirectConstants.VIEW_MODEL : 0)|(polygonOffset?1<<12:0));
        frame.integer(cullEnabled ? 0 : 1); frame.integer(color);
        frame.integer(alphaEnabled ? alphaCompare : 7); frame.integer(alphaReference);
        frame.integer(blendEnabled ? 1 : 0); frame.integer(sourceBlend); frame.integer(destinationBlend);
        frame.integer(vertexColor ? 1 : 0); frame.end(command);
    }
    /** Whether the eye is under water; before the world camera, which carries it. */
    public static void medium(boolean inside) {
        int command = frame.command(21); frame.integer(inside ? 1 : 0); frame.end(command);
    }
    public static void camera(int kind) {
        int command = frame.command(16); frame.integer(kind);
        for (int i = 0; i < 16; ++i) frame.number(kind==0&&worldCamera?worldView[i]:(i % 5 == 0 ? 1f : 0f));
        float[] projection = DirectTransforms.projection();
        for (int i = 0; i < 16; ++i)
            frame.number(i % 4 == 2 ? (projection[i] + projection[i + 1]) * .5f : projection[i]);
        frame.id(worldEpoch);frame.id(historyResetEpoch);
        frame.integer(kind==0&&environmentEnabled?1:0);
        for(float value:environment)frame.number(value);
        frame.end(command); cameraSent[kind] = true;
    }
    public static void terrainView(double x, double y, double z) {
        cameraOrigin[0]=x;cameraOrigin[1]=y;cameraOrigin[2]=z;
        if(!originValid||Math.abs(x-floatingOrigin[0])>1024||Math.abs(y-floatingOrigin[1])>1024||Math.abs(z-floatingOrigin[2])>1024){
            cameraCut();for(int i=0;i<3;++i)floatingOrigin[i]=Math.floor(cameraOrigin[i]/256)*256;originValid=true;
        }
        float[] view=DirectTransforms.model();worldView=view.clone();inverseView=DirectTransforms.inverse(view);
        for(int r=0;r<3;++r){
            double value=view[12+r];for(int c=0;c<3;++c)value+=view[c*4+r]*(floatingOrigin[c]-cameraOrigin[c]);worldView[12+r]=(float)value;
        }
        worldCamera=true;camera(0);
        int command = frame.command(19);
        for(double v:cameraOrigin)frame.number(v);
        for(double v:floatingOrigin)frame.number(v);
        for(float value:view)frame.number(value);
        frame.end(command);
    }
    public static void color(float r, float g, float b, float a) {
        color = channel(a)<<24 | channel(r)<<16 | channel(g)<<8 | channel(b);
    }
    public static void color(float r, float g, float b) { color(r,g,b,1); }
    private static int channel(float value) { return Math.round(Math.max(0, Math.min(1, value)) * 255); }
    public static void enable(int capability) { setEnabled(capability, true); }
    public static void disable(int capability) { setEnabled(capability, false); }
    private static void setEnabled(int capability, boolean enabled) {
        switch (capability) {
        case 3553: textureEnabled = enabled; break;
        case 2929: depthEnabled = enabled; break;
        case 2884: cullEnabled = enabled; break;
        case 3042: blendEnabled = enabled; break;
        case 3008: alphaEnabled = enabled; break;
        case 32823: polygonOffset = enabled; break;
        // Lighting, fog and normal normalization have no work in this unlit renderer.
        case 2896: case 2903: case 2912: case 2977: case 32826:
        case 16384: case 16385: break;
        default: throw new IllegalArgumentException("Unhandled draw capability " + capability);
        }
    }
    public static void alpha(int compare, float reference) {
        alphaCompare = compare - 512; alphaReference = channel(reference);
    }
    public static void colorMask(boolean r,boolean g,boolean b,boolean a){colorWrites=r||g||b||a;}
    public static void depthMask(boolean write){depthWrite=write;}
    public static void depthFunction(int comparison){depthCompare=comparison-512;}
    public static void blend(int source, int destination) { sourceBlend = blendFactor(source); destinationBlend = blendFactor(destination); }
    private static int blendFactor(int factor) {
        switch(factor) {
        case 0: return 0; case 1: return 1;
        case 768: return 2; case 769: return 3; case 774: return 4; case 775: return 5;
        case 770: return 6; case 771: return 7; case 772: return 8; case 773: return 9; case 776: return 14;
        default: throw new IllegalArgumentException("Unsupported blend factor " + factor);
        }
    }
    private static final class UiVertex {
        float x,y,z,w,u,v,r,g,b,a;
        UiVertex interpolate(UiVertex other, float t) {
            UiVertex out = new UiVertex();
            out.x=x+(other.x-x)*t; out.y=y+(other.y-y)*t; out.z=z+(other.z-z)*t; out.w=w+(other.w-w)*t;
            out.u=u+(other.u-u)*t; out.v=v+(other.v-v)*t;
            out.r=r+(other.r-r)*t; out.g=g+(other.g-g)*t; out.b=b+(other.b-b)*t; out.a=a+(other.a-a)*t;
            return out;
        }
    }
    private static final class UiCommand {
        long texture;
        int offset,count,flags;
    }
    private static float distance(UiVertex v, int plane) {
        switch (plane) {
        case 0: return v.w+v.x; case 1: return v.w-v.x;
        case 2: return v.w+v.y; case 3: return v.w-v.y;
        case 4: return v.w+v.z; default: return v.w-v.z;
        }
    }
    private static void appendUi(Mesh mesh, boolean vertexColor, boolean modelDepth) {
        float[] matrix = DirectTransforms.clipMatrix();
        UiVertex[] projected = new UiVertex[mesh.colors.length];
        for (int i=0; i<projected.length; ++i) {
            UiVertex v = projected[i] = new UiVertex();
            float x=mesh.vertices[i*5], y=mesh.vertices[i*5+1], z=mesh.vertices[i*5+2];
            v.x=matrix[0]*x+matrix[4]*y+matrix[8]*z+matrix[12];
            v.y=matrix[1]*x+matrix[5]*y+matrix[9]*z+matrix[13];
            v.z=matrix[2]*x+matrix[6]*y+matrix[10]*z+matrix[14];
            v.w=matrix[3]*x+matrix[7]*y+matrix[11]*z+matrix[15];
            v.u=mesh.vertices[i*5+3]; v.v=mesh.vertices[i*5+4];
            int c=vertexColor ? mesh.colors[i] : color;
            v.r=(c>>16&255)/255f; v.g=(c>>8&255)/255f; v.b=(c&255)/255f; v.a=(c>>>24)/255f;
        }
        UiCommand command = new UiCommand();
        DirectTextures.Texture image=textureEnabled?DirectTextures.get(texture):null;
        command.texture = image==null?0:image.id;
        command.flags = modelDepth && depthEnabled ? 1 : 0; command.offset = uiIndices.size();
        if(image!=null)command.flags|=(image.wrapU==1?2:0)|(image.wrapV==1?4:0)|(image.filter!=0?8:0);
        if(blendEnabled)command.flags|=16|(sourceBlend<<8)|(destinationBlend<<12);
        for (int i=0; i<mesh.indices.length; i+=3) {
            ArrayList<UiVertex> polygon = new ArrayList<UiVertex>(3);
            for (int j=0;j<3;++j) polygon.add(projected[mesh.indices[i+j]]);
            for (int plane=0; plane<6 && !polygon.isEmpty(); ++plane) {
                ArrayList<UiVertex> clipped = new ArrayList<UiVertex>();
                UiVertex previous = polygon.get(polygon.size()-1); float previousDistance = distance(previous,plane);
                for (UiVertex vertex : polygon) {
                    float d = distance(vertex,plane);
                    if ((previousDistance>=0)!=(d>=0)) clipped.add(previous.interpolate(vertex,previousDistance/(previousDistance-d)));
                    if (d>=0) clipped.add(vertex);
                    previous=vertex; previousDistance=d;
                }
                polygon=clipped;
            }
            if (polygon.size()<3) continue;
            boolean finite=true;
            for (UiVertex v:polygon) finite &= v.w>1e-7f;
            if (!finite) continue;
            int base=uiVertices.size(); uiVertices.addAll(polygon);
            for (int j=1;j+1<polygon.size();++j) {
                uiIndices.add(base); uiIndices.add(base+j); uiIndices.add(base+j+1);
            }
        }
        command.count=uiIndices.size()-command.offset;
        if (command.count>0) uiCommands.add(command);
    }
    private static void writeUi() {
        int at=frame.command(18); frame.integer(width); frame.integer(height);
        frame.integer(uiVertices.size()); frame.integer(uiIndices.size()); frame.integer(uiCommands.size());
        for (UiVertex v:uiVertices) {
            frame.number((v.x/v.w*.5f+.5f)*width); frame.number((.5f-v.y/v.w*.5f)*height);
            frame.number(Math.max(0,Math.min(1,v.z/v.w*.5f+.5f))); frame.number(v.u); frame.number(v.v);
            frame.integer(channel(v.a)<<24 | channel(v.b)<<16 | channel(v.g)<<8 | channel(v.r));
        }
        for (int index:uiIndices) frame.integer(index);
        for (UiCommand cmd:uiCommands) {
            frame.id(cmd.texture); frame.integer(cmd.count); frame.integer(cmd.offset); frame.integer(0); frame.integer(cmd.flags);
        }
        frame.end(at);
    }
}
