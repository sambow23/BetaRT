package mcrtx.render;

import java.io.*;
import java.nio.file.*;
import java.security.MessageDigest;
import java.util.*;
import mcrtx.bridge.McrtxRuntimeConfig;

/** BetaRT's _normal/_roughness/_metallic/_emissive siblings, DDS before PNG. */
public final class DirectPbr {
    public interface Assets { InputStream open(String name) throws IOException; }
    public static final String[] SUFFIXES={"_normal","_roughness","_metallic","_emissive"};
    private static final boolean LOAD_PBR=loadPbrEnabled();
    private DirectPbr() { }
    private static boolean loadPbrEnabled() {
        String value=McrtxRuntimeConfig.getEnvironmentValue("MCRTX_LOAD_PBR");
        return value.isEmpty()||McrtxRuntimeConfig.isTruthyValue(value);
    }
    public static String[] load(String name,Assets assets) {
        String[] result={"","","",""};
        if(!LOAD_PBR)return result;
        while(name.startsWith("%blur%")||name.startsWith("%clamp%"))name=name.substring(name.indexOf('%',1)+1);
        if(name.startsWith("##"))return result; // Vanilla unwraps these UV layouts.
        while(name.startsWith("/"))name=name.substring(1);
        if(name.contains("..")||name.contains(":"))return result;
        int dot=name.lastIndexOf('.');String stem=dot<0?name:name.substring(0,dot);
        List<Path> roots=new ArrayList<Path>();
        Path cache=Paths.get("mcrtx_texturepack_cache");
        try {
            Path marker=cache.resolve("current.txt");
            if(Files.isRegularFile(marker)) {
                String id=new String(Files.readAllBytes(marker),"UTF-8").trim();
                if(!id.isEmpty()&&!id.contains("..")&&!id.contains("/")&&!id.contains("\\"))roots.add(cache.resolve(id));
            }
        } catch(IOException e) { System.err.println("BetaRT PBR cache: "+e.getMessage()); }
        roots.add(cache);
        String explicit=System.getenv("MCRTX_ASSETS");if(explicit!=null&&!explicit.isEmpty())roots.add(Paths.get(explicit));
        roots.add(Paths.get("mcrtx_assets"));roots.add(Paths.get("..","libraries","mcrtx_assets"));
        try {
            Path jar=Paths.get(DirectPbr.class.getProtectionDomain().getCodeSource().getLocation().toURI());
            if(jar.getParent()!=null)roots.add(jar.getParent().resolve("mcrtx_assets"));
        } catch(Exception e) { /* Relative asset roots still work outside a JAR. */ }
        List<String> stems=new ArrayList<String>();stems.add(stem);
        if(stem.startsWith("mob/"))stems.add("entities/"+stem);
        int underscore=stem.lastIndexOf('_');
        if(underscore>=0&&stem.substring(underscore+1).matches("[0-9]+"))stems.add(stem.substring(0,underscore));
        for(int slot=0;slot<4;++slot) {
            try {
                // The selected pack takes precedence over bundled maps.
                if(assets!=null)for(String base:stems)for(String ext:new String[]{".dds",".png"}) {
                    if(!result[slot].isEmpty())break;
                    try(InputStream in=assets.open("/"+base+SUFFIXES[slot]+ext)) {
                        if(in!=null)result[slot]=cache(in,ext);
                    }
                }
                if(result[slot].isEmpty())outer:for(Path root:roots)for(String base:stems)for(String ext:new String[]{".dds",".png"}) {
                    Path file=root.resolve(base+SUFFIXES[slot]+ext);
                    if(Files.isRegularFile(file)) {
                        try(InputStream in=Files.newInputStream(file)){result[slot]=cache(in,ext);}
                        break outer;
                    }
                }
            } catch(IOException e) { System.err.println("BetaRT PBR "+name+SUFFIXES[slot]+": "+e.getMessage()); }
        }
        return result;
    }
    private static String cache(InputStream in,String extension)throws IOException {
        ByteArrayOutputStream out=new ByteArrayOutputStream();byte[] chunk=new byte[16384];int count;
        while((count=in.read(chunk))!=-1) {
            if(out.size()>64*1024*1024-count)throw new IOException("Map exceeds 64 MiB");
            out.write(chunk,0,count);
        }
        byte[] bytes=out.toByteArray();StringBuilder hash=new StringBuilder();
        try { for(byte b:MessageDigest.getInstance("SHA-256").digest(bytes))hash.append(String.format("%02x",b&255)); }
        catch(java.security.NoSuchAlgorithmException e){throw new IOException(e);}
        Path directory=Paths.get("mcrtx_texturepack_cache","direct");Files.createDirectories(directory);
        Path target=directory.resolve(hash+extension);
        if(!Files.isRegularFile(target))Files.write(target,bytes);
        return target.toAbsolutePath().toString();
    }
}
