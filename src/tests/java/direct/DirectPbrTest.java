import mcrtx.render.*;
import java.io.*;
import java.nio.*;
import java.nio.file.*;
import java.util.*;

public final class DirectPbrTest {
    private static void require(boolean value,String why){if(!value)throw new AssertionError(why);}
    public static void main(String[] args)throws Exception {
        final Map<String,byte[]> pack=new HashMap<String,byte[]>();
        pack.put("/terrain_normal.dds",new byte[]{1,2,3});
        pack.put("/terrain_roughness.png",new byte[]{4,5,6});
        pack.put("/terrain_metallic.dds",new byte[]{7,8,9});
        pack.put("/terrain_emissive.png",new byte[]{10,11,12});
        DirectPbr.Assets assets=new DirectPbr.Assets(){public InputStream open(String name){
            byte[] data=pack.get(name);return data==null?null:new ByteArrayInputStream(data);
        }};
        String[] maps=DirectPbr.load("/terrain.png",assets);
        for(String path:maps)require(Files.isRegularFile(Paths.get(path)),"Missing material map");
        require(maps[0].endsWith(".dds")&&maps[1].endsWith(".png"),"DDS/PNG discovery");
        require(Arrays.equals(maps,DirectPbr.load("%blur%/terrain.png",assets)),"Unstable cache identity");
        pack.put("/terrain_normal.dds",new byte[]{13,14,15});
        String[] replacement=DirectPbr.load("/terrain.png",assets);
        require(!maps[0].equals(replacement[0])&&Files.readAllBytes(Paths.get(maps[0]))[0]==1,"Map replacement changed queued resource");
        for(String path:DirectPbr.load("/missing.png",assets))require(path.isEmpty(),"Missing map did not use constant");
        int texture=DirectTextures.allocate();DirectTextures.image(texture,new int[]{0xffffffff},1,1,false,false);
        DirectTextures.pixels(texture).pbr=maps;
        DirectRenderer.resources.clear();DirectRenderer.writeMaterial(123,texture);
        ByteBuffer source=DirectRenderer.resources.buffer(),data=source.duplicate().order(ByteOrder.LITTLE_ENDIAN);
        data.limit(source.position());data.position(8);
        while(data.hasRemaining()) {
            int op=data.getInt(),size=data.getInt(),end=data.position()+size;
            if(op==2) {
                data.position(data.position()+36);
                require(data.getFloat()==1&&data.getFloat()==1&&data.getFloat()==1,"Authored emission not enabled");
                for(String map:maps) {
                    byte[] bytes=new byte[data.getInt()];data.get(bytes);
                    require(map.equals(new String(bytes,"UTF-8")),"Material map order or path encoding");
                }
                require(data.position()==end,"Material packet length");
            }
            data.position(end);
        }
        System.out.println("PBR sibling discovery, immutable reloads and material transport passed");
    }
}
