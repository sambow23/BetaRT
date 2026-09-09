import mcrtx.render.DirectRenderer;
import mcrtx.render.DirectTransforms;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

/** Direct mesh construction at the two vanilla geometry producers. */
public final class DirectGameGeometry {
    private static final HashMap<Integer, DirectRenderer.Mesh> models = new HashMap<Integer, DirectRenderer.Mesh>();
    private static int nextModel = 1;
    private DirectGameGeometry() { }
    public static void initialize(nw builder, int capacity) {
        builder.B=capacity; builder.g=new int[capacity];
        // The remaining CPU builder resets this buffer; it is never sent to a graphics driver.
        builder.d=ge.c(0);
    }
    public static void finish(nw builder) {
        if (!builder.w) throw new IllegalStateException("Not tessellating");
        builder.w=false;
        int count=builder.h;
        if (count>0) {
            float[] vertices=new float[count*5]; int[] colors=new int[count];
            for (int i=0;i<count;++i) {
                for (int j=0;j<5;++j) vertices[i*5+j]=Float.intBitsToFloat(builder.g[i*8+j]);
                int color=builder.l?builder.g[i*8+5]:0xffffffff;
                if (builder.l && ByteOrder.nativeOrder()==ByteOrder.LITTLE_ENDIAN)
                    color=(color&0xff00ff00) | ((color&255)<<16) | ((color>>>16)&255);
                colors[i]=color;
            }
            int mode=builder.r==7 && nw.b?4:builder.r;
            int[] indices=triangles(mode,count);
            if (indices!=null) DirectRenderer.drawGenerated(new DirectRenderer.Mesh(vertices,colors,indices),builder.l,builder.n);
            else lines(vertices, colors,mode,builder.l);
        }
        builder.h=builder.o=builder.p=0; builder.d.clear();
    }
    public static int[] triangles(int mode, int count) {
        int[] indices;
        switch(mode) {
        case 4:
            indices=new int[count-count%3]; for(int i=0;i<indices.length;++i) indices[i]=i; return indices;
        case 7:
            indices=new int[count/4*6];
            for(int i=0;i<count/4;++i) {
                int p=i*6,b=i*4; indices[p]=b; indices[p+1]=b+1; indices[p+2]=b+2;
                indices[p+3]=b; indices[p+4]=b+2; indices[p+5]=b+3;
            }
            return indices;
        case 5: case 6: case 9:
            indices=new int[Math.max(0,count-2)*3];
            for(int i=0;i<count-2;++i) {
                indices[i*3]=mode==5?i:0;
                indices[i*3+1]=i+1; indices[i*3+2]=i+2;
                if(mode==5 && (i&1)!=0) {indices[i*3]=i+1; indices[i*3+1]=i;}
            }
            return indices;
        case 1: case 2: case 3: return null;
        default: throw new IllegalArgumentException("Unsupported primitive " + mode);
        }
    }
    private static void lines(float[] vertices,int[] colors,int mode,boolean vertexColor) {
        // Selection/debug line segments become thin camera-facing quads in view space.
        int count=colors.length, segments=mode==1?count/2:Math.max(0,count-(mode==2?0:1));
        float[] out=new float[segments*20]; int[] tint=new int[segments*4];
        float[] model=DirectTransforms.model();
        for(int i=0;i<segments;++i) {
            int a=mode==1?i*2:i, b=mode==1?a+1:(i+1)%count;
            float[] start=point(model,vertices,a), end=point(model,vertices,b);
            float dx=end[0]-start[0],dy=end[1]-start[1],length=(float)Math.sqrt(dx*dx+dy*dy);
            float thickness=Math.max(.0005f,Math.abs((start[2]+end[2])*.5f)/Math.max(1,DirectRenderer.height));
            float ox=length>1e-8f?-dy/length*thickness:thickness,oy=length>1e-8f?dx/length*thickness:0;
            for(int j=0;j<4;++j) {
                float[] p=j<2?start:end; int at=(i*4+j)*5; float sign=(j==0||j==3)?-1:1;
                out[at]=p[0]+sign*ox;out[at+1]=p[1]+sign*oy;out[at+2]=p[2];tint[i*4+j]=colors[j<2?a:b];
            }
        }
        DirectTransforms.select(5888);DirectTransforms.push();DirectTransforms.loadIdentity();
        boolean cull=DirectRenderer.cullEnabled;DirectRenderer.cullEnabled=false;
        DirectRenderer.draw(new DirectRenderer.Mesh(out,tint,triangles(7,tint.length)),false,vertexColor,false);
        DirectRenderer.cullEnabled=cull;
        DirectTransforms.pop();
    }
    private static float[] point(float[] m,float[] vertices,int i) {
        float x=vertices[i*5],y=vertices[i*5+1],z=vertices[i*5+2];
        return new float[]{m[0]*x+m[4]*y+m[8]*z+m[12],m[1]*x+m[5]*y+m[9]*z+m[13],m[2]*x+m[6]*y+m[10]*z+m[14]};
    }
    public static void compile(ps part,float scale) {
        int count=0;
        for(tz face:part.k) count+=face.a.length;
        float[] vertices=new float[count*5];int[] colors=new int[count];Arrays.fill(colors,0xffffffff);
        ArrayList<Integer> indices=new ArrayList<Integer>();int base=0;
        for(tz face:part.k) {
            for(ib vertex:face.a) {
                int at=base++*5; vertices[at]=(float)vertex.a.a*scale;vertices[at+1]=(float)vertex.a.b*scale;
                vertices[at+2]=(float)vertex.a.c*scale;vertices[at+3]=vertex.b;vertices[at+4]=vertex.c;
            }
            int begin=base-face.a.length;
            for(int i=1;i+1<face.a.length;++i) {indices.add(begin);indices.add(begin+i);indices.add(begin+i+1);}
        }
        int[] triangle=new int[indices.size()];for(int i=0;i<triangle.length;++i)triangle[i]=indices.get(i);
        part.o=nextModel++; models.put(part.o,new DirectRenderer.Mesh(vertices,colors,triangle).identifyPart(part)); part.n=true;
    }
    public static void drawModel(int id) {
        DirectRenderer.Mesh mesh=models.get(id);
        if(mesh==null)throw new IllegalStateException("Unknown model " + id);
        DirectRenderer.draw(mesh,true,false,true);
    }
    public static void invalidate(ps part) {
        DirectRenderer.Mesh mesh=models.remove(part.o);if(mesh!=null)mesh.dispose();part.n=false;part.o=0;
    }
    public static void reset() {models.clear();nextModel=1;}
    public static void frustum(w view) {
        DirectTransforms.multiply(DirectTransforms.projection(),DirectTransforms.model(),view.d);
        for(int plane=0;plane<6;++plane) {
            int axis=plane/2;float sign=(plane==0||plane==3||plane==4)?-1:1;
            for(int i=0;i<4;++i)view.a[plane][i]=view.d[i*4+3]+sign*view.d[i*4+axis];
            float length=(float)Math.sqrt(view.a[plane][0]*view.a[plane][0]+view.a[plane][1]*view.a[plane][1]+view.a[plane][2]*view.a[plane][2]);
            if(length>0)for(int i=0;i<4;++i)view.a[plane][i]/=length;
        }
    }
}
