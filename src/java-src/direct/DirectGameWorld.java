import net.minecraft.client.Minecraft;
import mcrtx.render.DirectRenderer;
import mcrtx.render.DirectTextures;
import mcrtx.render.DirectTransforms;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

/** World submissions use loaded chunks and persistent CPU meshes, with no vanilla render lists. */
public final class DirectGameWorld {
    public static double originX,originY,originZ;
    private static int atlas,water,lava,portal;
    private static long atlasRevision=-1;
    private static final int[] emissionTextures=new int[256];
    private static final int[][] emissionPixels=new int[256][];
    private static DirectRenderer.Mesh dome,stars,sun,moon;
    private DirectGameWorld() { }
    public static void reset(){atlas=water=lava=portal=0;atlasRevision=-1;Arrays.fill(emissionTextures,0);Arrays.fill(emissionPixels,null);DirectWaterNormal.reset();}
    public static void initialize(n renderer,Minecraft game,ji textures){
        renderer.t=game;renderer.l=textures;renderer.a=new ArrayList();renderer.m=new ArrayList();
        renderer.H=-1;renderer.f=renderer.g=renderer.h=-9999;renderer.c=ge.d(0);
    }
    public static void attach(n renderer,fd world){
        if(renderer.k!=null)renderer.k.b(renderer);
        DirectRenderer.worldChanged();
        renderer.k=world;renderer.u=new cv(world);th.a.a(world);renderer.a.clear();
        DirectSectionResidency.attach(world);if(world!=null){world.a(renderer);reload(renderer);}
    }
    public static void reload(n renderer){
        uu.L.a(renderer.t.z.j);renderer.H=renderer.t.z.e;
        DirectSectionResidency.attach(renderer.k);
    }
    public static int terrain(n renderer,ls camera,int pass,double partial){
        if(pass!=0)return 0;
        if(renderer.H!=renderer.t.z.e)reload(renderer);
        materials(renderer.l);
        DirectSectionResidency.scan(originX,originZ,renderer.t.z.e);
        return DirectSectionResidency.residentCount();
    }
    public static boolean pump(n renderer,ls camera,boolean force){DirectSectionUpdates.flush();return true;}
    public static void invalidate(n renderer,int x0,int y0,int z0,int x1,int y1,int z1){
        DirectSectionUpdates.queueRegion(x0,y0,z0,x1,y1,z1);
    }
    public static void entities(n renderer,bt position,yn frustum,float partial){
        Minecraft game=renderer.t;fd world=renderer.k;
        ll.a.a(world,renderer.l,game.q,game.i,partial);th.a.a(world,renderer.l,game.q,game.i,game.z,partial);
        th.b=ll.b=DirectRenderer.floatingOrigin[0];th.c=ll.c=DirectRenderer.floatingOrigin[1];th.d=ll.d=DirectRenderer.floatingOrigin[2];
        int radius=Math.max(2,Math.min(16,(256>>Math.max(0,Math.min(3,game.z.e)))/16));
        java.util.Set<sn> submitted=java.util.Collections.newSetFromMap(new java.util.IdentityHashMap<sn,Boolean>());
        DirectTransforms.select(5888);DirectTransforms.push();DirectTransforms.loadIdentity();
        try {
            ArrayList<Object> visible=new ArrayList<Object>(world.e);visible.addAll(world.o());
            for(Object value:visible){
                sn entity=(sn)value;
                if(!submitted.add(entity)||entity==game.i&&!game.z.A&&!game.i.N())continue;
                if(Math.abs(Math.floor(entity.aM/16)-Math.floor(originX/16))>radius||
                   Math.abs(Math.floor(entity.aO/16)-Math.floor(originZ/16))>radius||
                   !DirectSectionResidency.loaded(world,(int)Math.floor(entity.aM/16),(int)Math.floor(entity.aO/16)))continue;
                DirectRenderer.beginEntity(entity,entity.a(position)&&(entity.bM||frustum.a(entity.aW)));
                try{th.a.a(entity,partial);}finally{DirectRenderer.endEntity();}
            }
            for(Object value:world.c){
                ow tile=(ow)value;
                if(Math.abs(Math.floor(tile.e/16.0)-Math.floor(originX/16))>radius||Math.abs(Math.floor(tile.g/16.0)-Math.floor(originZ/16))>radius)continue;
                DirectRenderer.beginEntity(tile,tile.a(originX,originY,originZ)<4096);try{ll.a.a(tile,tile.e-DirectRenderer.floatingOrigin[0],tile.f-DirectRenderer.floatingOrigin[1],tile.g-DirectRenderer.floatingOrigin[2],partial);}finally{DirectRenderer.endEntity();}
            }
        } finally {
            DirectTransforms.pop();DirectRenderer.endEntity();
            th.b=ll.b=originX;th.c=ll.c=originY;th.d=ll.d=originZ;
        }
    }

    private static long atlasPbrRevision=-1;
    private static void materials(ji textures){
        int current=textures.b("/terrain.png");
        DirectTextures.Texture source=DirectTextures.pixels(current);
        if(atlas!=current){
            atlas=current;atlasRevision=-1;atlasPbrRevision=-1;
            if(water==0){water=DirectTextures.allocate();lava=DirectTextures.allocate();portal=DirectTextures.allocate();}
        }
        DirectWaterNormal.animate();
        if(atlasRevision==source.revision)return;atlasRevision=source.revision;
        extract(source,water,205,206);extract(source,lava,237,238);extract(source,portal,14);
        if(atlasPbrRevision!=source.pbrRevision) {
            atlasPbrRevision=source.pbrRevision;
            DirectTextures.pixels(water).pbrLoaded=false;
            DirectTextures.pixels(lava).pbrLoaded=false;
            DirectTextures.pixels(portal).pbrLoaded=false;
        }
        DirectGameTextures.loaded(water,textures,"/water.png");
        DirectWaterNormal.attach(water);
        DirectGameTextures.loaded(lava,textures,"/lava.png");
        DirectGameTextures.loaded(portal,textures,"/portal.png");
        for(int i=0;i<8;++i)DirectRenderer.writeMaterial(i+1,i==2?water:i==3?lava:i==5?portal:atlas);
        for(int block=0;block<uu.m.length&&block<256;++block) {
            int level=uu.s[block];if(uu.m[block]==null||level<=0)continue;
            int color=(block==10||block==11)?lava:block==90?portal:atlas;
            DirectTextures.Texture image=DirectTextures.pixels(color);
            int[] mask=mcrtx.render.DirectEmission.mask(image,block);
            if(emissionTextures[block]==0)emissionTextures[block]=DirectTextures.allocate();
            if(!Arrays.equals(mask,emissionPixels[block])) {
                DirectTextures.image(emissionTextures[block],mask,image.width,image.height,false,false);
                emissionPixels[block]=mask;
            }
            for(int material=0;material<8;++material)
                mcrtx.render.DirectEmission.writeMaterial(block,level,material,
                    material==2?water:material==3?lava:material==5?portal:atlas,emissionTextures[block]);
        }
    }
    private static void extract(DirectTextures.Texture source,int id,int... tiles){
        int size=source.width/16,height=source.height/16;int[] pixels=new int[size*tiles.length*height];
        for(int tile=0;tile<tiles.length;++tile)for(int y=0;y<height;++y)for(int x=0;x<size;++x){
            int p=(((tiles[tile]/16*height+y)*source.width)+(tiles[tile]%16*size+x))*4;
            pixels[(y*tiles.length+tile)*size+x]=(source.rgba[p+3]&255)<<24|(source.rgba[p]&255)<<16|(source.rgba[p+1]&255)<<8|(source.rgba[p+2]&255);
        }
        DirectTextures.image(id,pixels,size*tiles.length,height,false,false);
    }
    private static DirectRenderer.Mesh mesh(float[] vertices){
        int[] colors=new int[vertices.length/5];Arrays.fill(colors,0xffffffff);
        return new DirectRenderer.Mesh(vertices,colors,DirectGameGeometry.triangles(7,colors.length));
    }
    private static void initializeSky(){
        if(dome!=null)return;
        float[] cube=new float[24*5];int at=0;
        for(int axis=0;axis<3;++axis)for(int side=-1;side<=1;side+=2)for(int corner=0;corner<4;++corner){
            cube[at+axis]=side*60;cube[at+(axis+1)%3]=(corner<2?-1:1)*60;
            cube[at+(axis+2)%3]=(corner==0||corner==3?-1:1)*60;at+=5;
        }
        dome=mesh(cube);
        sun=mesh(new float[]{-12,40,-12,0,0,12,40,-12,1,0,12,40,12,1,1,-12,40,12,0,1});
        moon=mesh(new float[]{-8,-40,8,1,1,8,-40,8,0,1,8,-40,-8,0,0,-8,-40,-8,1,0});
        Random random=new Random(10842);ArrayList<Float> points=new ArrayList<Float>();
        for(int i=0;i<1500;++i){
            float x=random.nextFloat()*2-1,y=random.nextFloat()*2-1,z=random.nextFloat()*2-1;
            float length=(float)Math.sqrt(x*x+y*y+z*z);if(length<=.1||length>=1)continue;
            x/=length;y/=length;z/=length;float size=.1f+random.nextFloat()*.1f;
            float tx=z,tz=-x,t=(float)Math.sqrt(tx*tx+tz*tz);if(t<.001)continue;tx/=t;tz/=t;
            float bx=y*tz,by=z*tx-x*tz,bz=-y*tx;
            for(int corner=0;corner<4;++corner){float a=corner<2?-size:size,b=corner==0||corner==3?-size:size;
                points.add(x*40+tx*a+bx*b);points.add(y*40+by*b);points.add(z*40+tz*a+bz*b);points.add(0f);points.add(0f);}
        }
        float[] data=new float[points.size()];for(int i=0;i<data.length;++i)data[i]=points.get(i);stars=mesh(data);
    }
    public static void environment(n renderer,float partial){
        boolean enabled=!renderer.k.t.c;
        bt color=enabled?renderer.k.a(renderer.t.i,partial):null;
        DirectRenderer.environmentEnabled=enabled;
        DirectRenderer.environment=mcrtx.render.DirectEnvironment.lighting(enabled,
            enabled?(float)color.a:0,enabled?(float)color.b:0,enabled?(float)color.c:0,
            renderer.k.b(partial),renderer.k.g(partial),renderer.k.f(partial));
    }
    public static void sky(n renderer,float partial){
        if(renderer.k.t.c)return;initializeSky();
        DirectRenderer.layer=1;DirectRenderer.textureEnabled=false;DirectRenderer.cullEnabled=false;DirectRenderer.blendEnabled=false;
        DirectTransforms.select(5888);DirectTransforms.push();float[] view=DirectTransforms.model();view[12]=view[13]=view[14]=0;
        bt color=renderer.k.a(renderer.t.i,partial);DirectRenderer.color((float)color.a,(float)color.b,(float)color.c);
        DirectRenderer.draw(dome,true,false,false);
        DirectTransforms.rotate(renderer.k.b(partial)*360,1,0,0);
        DirectRenderer.blendEnabled=true;DirectRenderer.blend(770,1);
        float opacity=1-renderer.k.g(partial);DirectRenderer.color(1,1,1,opacity);DirectRenderer.textureEnabled=true;
        DirectRenderer.texture=renderer.l.b("/terrain/sun.png");DirectRenderer.draw(sun,true,false,false);
        DirectRenderer.texture=renderer.l.b("/terrain/moon.png");DirectRenderer.draw(moon,true,false,false);
        float star=renderer.k.e(partial)*opacity;
        if(star>0){DirectRenderer.color(star,star,star,star);DirectRenderer.textureEnabled=false;DirectRenderer.draw(stars,true,false,false);}
        DirectTransforms.pop();DirectRenderer.layer=0;DirectRenderer.textureEnabled=true;DirectRenderer.cullEnabled=true;
        DirectRenderer.blendEnabled=false;DirectRenderer.color=0xffffffff;DirectRenderer.blend(770,771);
    }
}
