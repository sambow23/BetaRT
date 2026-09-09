package mcrtx.tools;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.jar.*;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;

/** Patches the original client at its geometry producers, preserving gameplay bytecode. */
public final class DirectClientPatchTool implements Opcodes {
    private static final String MC="net/minecraft/client/Minecraft", R="mcrtx/render/DirectRenderer", T="mcrtx/render/DirectTransforms", X="mcrtx/render/DirectTextures";
    private static final Set<String> ACCESS=new HashSet<String>(Arrays.asList(MC,"nw","ps","sj","ji","n","px","w","fd","lm","th","ll","cv"));
    private static final SortedSet<String> unhandled=new TreeSet<String>();
    private static final Map<String,String> parents=new HashMap<String,String>();
    private DirectClientPatchTool() { }
    public static void main(String[] args)throws Exception {
        if(args.length==2&&args[0].equals("--audit")){audit(Paths.get(args[1]));return;}
        if(args.length!=2)throw new IllegalArgumentException("Usage: DirectClientPatchTool original.jar patched.jar");
        try(JarFile source=new JarFile(args[0]);JarOutputStream out=new JarOutputStream(Files.newOutputStream(Paths.get(args[1])))) {
            Enumeration<JarEntry> classes=source.entries();
            while(classes.hasMoreElements()){
                JarEntry entry=classes.nextElement();if(!entry.getName().endsWith(".class"))continue;
                try(InputStream in=source.getInputStream(entry)){ClassReader reader=new ClassReader(read(in));parents.put(reader.getClassName(),reader.getSuperName());}
            }
            Enumeration<JarEntry> entries=source.entries();
            while(entries.hasMoreElements()) {
                JarEntry entry=entries.nextElement();String name=entry.getName();
                if(name.startsWith("META-INF/"))continue;
                byte[] bytes;try(InputStream in=source.getInputStream(entry)){bytes=read(in);}
                if(name.endsWith(".class"))bytes=patch(bytes);
                out.putNextEntry(new JarEntry(name));out.write(bytes);out.closeEntry();
            }
        }
        if(!unhandled.isEmpty())throw new IllegalStateException("Unconverted graphics calls:\n"+String.join("\n",unhandled));
    }
    private static void audit(Path jar)throws IOException {
        java.util.regex.Pattern graphics=java.util.regex.Pattern.compile("org/lwjgl/(?:opengl|glfw)/[A-Za-z0-9_$]+");
        int count=0;
        try(JarFile source=new JarFile(jar.toFile())){
            Enumeration<JarEntry> entries=source.entries();
            while(entries.hasMoreElements()){
                JarEntry entry=entries.nextElement();if(!entry.getName().endsWith(".class"))continue;
                ++count;
                try(InputStream in=source.getInputStream(entry)){
                    java.util.regex.Matcher names=graphics.matcher(new String(read(in),java.nio.charset.StandardCharsets.ISO_8859_1));
                    while(names.find())if(!names.group().equals("org/lwjgl/opengl/Display")&&!names.group().equals("org/lwjgl/opengl/DisplayMode"))
                        throw new IOException("Forbidden graphics dependency in "+entry.getName()+": "+names.group());
                }
            }
        }
        System.out.println("Audited "+count+" client classes: no OpenGL or GLFW dependencies");
    }
    private static byte[] read(InputStream in)throws IOException {
        ByteArrayOutputStream out=new ByteArrayOutputStream();byte[] buffer=new byte[8192];int count;
        while((count=in.read(buffer))!=-1)out.write(buffer,0,count);return out.toByteArray();
    }
    private static byte[] patch(byte[] bytes) {
        ClassNode node=new ClassNode();new ClassReader(bytes).accept(node,0);
        boolean graphicsClass=node.name.equals("cv")||node.name.equals("th")||node.name.equals("ll")||inherits(node.name,"xw");
        for(MethodNode method:node.methods)for(AbstractInsnNode instruction:method.instructions.toArray())
            if(instruction instanceof MethodInsnNode&&((MethodInsnNode)instruction).owner.equals("org/lwjgl/opengl/GL11"))graphicsClass=true;
        if(ACCESS.contains(node.name))for(FieldNode field:node.fields)field.access=(field.access&~(ACC_PRIVATE|ACC_PROTECTED))|ACC_PUBLIC;
        if(ACCESS.contains(node.name))for(MethodNode method:node.methods)
            if(!method.name.equals("<clinit>"))method.access=(method.access&~(ACC_PRIVATE|ACC_PROTECTED))|ACC_PUBLIC;
        for(MethodNode method:node.methods) {
            String signature=method.name+method.desc;
            if(node.name.equals("nw")) {
                if(signature.equals("<init>(I)V"))delegate(node,method,"DirectGameGeometry","initialize");
                if(signature.equals("a()V"))delegate(node,method,"DirectGameGeometry","finish");
            } else if(node.name.equals("ps")) {
                if(signature.equals("d(F)V"))delegate(node,method,"DirectGameGeometry","compile");
                if(signature.equals("a(FFFIIIF)V"))method.instructions.insert(callThis("DirectGameGeometry","invalidate","(Lps;)V"));
            } else if(node.name.equals("sj")) {
                if(method.name.equals("<init>"))delegate(node,method,"DirectGameFont","initialize");
                if(signature.equals("a(Ljava/lang/String;IIIZ)V"))delegate(node,method,"DirectGameFont","draw");
                if(signature.equals("a(Ljava/lang/String;)I"))method.instructions.insert(callThis("DirectGameFont","refresh","(Lsj;)V"));
            } else if(node.name.equals("ji")) {
                if(signature.equals("a(Ljava/awt/image/BufferedImage;I)V"))delegate(node,method,"DirectGameTextures","upload");
                if(signature.equals("a([IIII)V"))delegate(node,method,"DirectGameTextures","uploadPixels");
                if(signature.equals("a()V"))delegate(node,method,"DirectGameTextures","animate");
            } else if(node.name.equals("w")&&signature.equals("b()V"))delegate(node,method,"DirectGameGeometry","frustum");
            else if(node.name.equals("vy")) {
                if(method.name.equals("<init>"))emptyConstructor(node,method);
                if(signature.equals("b()V"))delegate(node,method,"DirectGameClient","releaseMouse");
            }
            else if(node.name.equals("cx")&&signature.equals("a()Z"))constant(method,0);
            else if(node.name.equals("u")) {if(!method.name.equals("<init>"))constant(method,null);}
            else if(node.name.equals("bw")&&signature.equals("c(Lsn;DDDFF)V"))constant(method,null);
            else if(node.name.equals("hj")&&method.name.equals("a"))delegate(node,method,"DirectGameClient","screenshot");
            else if(node.name.equals("ge")) {
                if(signature.equals("a(I)I"))constant(method,0);
                if(signature.equals("b(I)V")||signature.equals("a()V"))constant(method,null);
            } else if(node.name.equals("dk")||node.name.equals("tg")) {
                // No instances remain: terrain residency and submission live in DirectGameWorld.
                if(!method.name.equals("<init>")&&!method.name.equals("<clinit>"))constant(method,Type.getReturnType(method.desc).getSort()==Type.VOID?null:0);
                else if(method.name.equals("<init>"))emptyConstructor(node,method);
            } else if(node.name.equals("n"))patchWorld(node,method,signature);
            else if(node.name.equals("px")) {
                if(signature.equals("d(F)F"))for(AbstractInsnNode instruction:method.instructions.toArray())
                    if(instruction.getOpcode()==FRETURN)method.instructions.insertBefore(instruction,new MethodInsnNode(INVOKESTATIC,"DirectGameClient","adjustFov","(F)F",false));
                if(signature.equals("a(IF)V"))constant(method,null);
                if(signature.equals("a(FJ)V"))delegate(node,method,"DirectGameClient","world");
                if(signature.equals("b(F)V"))method.instructions.insert(callThis("DirectGameClient","begin","(Lpx;)V"));
            } else if(node.name.equals(MC)) {
                if(signature.equals("b(Lmh;)V"))beforeReturns(method,callThis("DirectGameClient","failed","(L"+MC+";)V"));
                if(signature.equals("c(Ljava/lang/String;)V"))constant(method,null);
                if(signature.equals("z()V"))method.instructions.insert(callThis("DirectGameClient","end","(L"+MC+";)V"));
                if(signature.equals("a(Lda;)V"))beforeReturns(method,callThis("DirectGameClient","screen","(L"+MC+";)V"));
                if(signature.equals("a()V")){
                    method.instructions.insert(callThis("DirectGameClient","starting","(L"+MC+";)V"));
                    beforeReturns(method,callThis("DirectGameClient","started","(L"+MC+";)V"));
                }
            }
            if((inherits(node.name,"gv")&&(method.desc.equals("(Lls;Ljava/lang/String;DDDI)V") || method.name.equals("b")&&method.desc.endsWith(";DDD)V"))) ||
               (node.name.equals("bw")&&signature.equals("a(Lsn;DDDF)V"))) {
                method.instructions.insert(new MethodInsnNode(INVOKESTATIC,R,"beginRasterEffect","()V",false));
                InsnList end=new InsnList();end.add(new MethodInsnNode(INVOKESTATIC,R,"endRasterEffect","()V",false));beforeReturns(method,end);
            }
            for(AbstractInsnNode instruction:method.instructions.toArray()) {
                if(!(instruction instanceof MethodInsnNode))continue;
                MethodInsnNode call=(MethodInsnNode)instruction;
                if(graphicsClass&&((inherits(call.owner,"sn")&&call.name.equals("a")&&call.desc.equals("(F)F"))
                    ||((call.owner.equals("fd")||call.owner.equals("xp"))&&call.name.equals("c")&&call.desc.equals("(III)F"))
                    ||(call.owner.equals("xp")&&call.name.equals("a")&&call.desc.equals("(IIII)F"))
                    ||(call.owner.equals("uu")&&call.name.equals("d")&&call.desc.equals("(Lxp;III)F")))){
                    InsnList light=new InsnList();Type[] args=Type.getArgumentTypes(call.desc);
                    for(int i=args.length-1;i>=0;--i)light.add(new InsnNode(args[i].getSize()==2?POP2:POP));
                    light.add(new InsnNode(POP));light.add(new InsnNode(FCONST_1));
                    method.instructions.insertBefore(call,light);method.instructions.remove(call);
                }else if(call.owner.equals("org/lwjgl/opengl/GL11"))graphics(node,method,call);
                else if(call.owner.equals("org/lwjgl/util/glu/GLU")&&call.name.equals("gluPerspective"))retarget(call,T,"perspective");
                else if(call.owner.equals("org/lwjgl/openal/AL10"))call.owner="mcrtx/lwjglshim/LegacyAL10";
                else if(call.owner.startsWith("org/lwjgl/opengl/")&&!call.owner.equals("org/lwjgl/opengl/Display")&&!call.owner.equals("org/lwjgl/opengl/DisplayMode"))
                    unhandled.add(node.name+"."+signature+" -> "+call.owner+"."+call.name+call.desc);
            }
        }
        // Original classes use Java 5 verification; retain their version and recompute stack limits.
        ClassWriter writer=new ClassWriter(ClassWriter.COMPUTE_MAXS);node.accept(writer);return writer.toByteArray();
    }
    private static boolean inherits(String name,String base){
        while(name!=null){if(name.equals(base))return true;name=parents.get(name);}return false;
    }
    private static void patchWorld(ClassNode node,MethodNode method,String signature) {
        if(method.name.equals("<init>"))delegate(node,method,"DirectGameWorld","initialize");
        else if(signature.equals("a()V"))delegate(node,method,"DirectGameWorld","reload");
        else if(signature.equals("a(Lfd;)V"))delegate(node,method,"DirectGameWorld","attach");
        else if(signature.equals("a(Lls;ID)I"))delegate(node,method,"DirectGameWorld","terrain");
        else if(signature.equals("a(Lls;Z)Z"))delegate(node,method,"DirectGameWorld","pump");
        else if(signature.equals("a(IIIIII)V"))delegate(node,method,"DirectGameWorld","invalidate");
        else if(signature.equals("a(Lbt;Lyn;F)V"))delegate(node,method,"DirectGameWorld","entities");
        else if(signature.equals("a(F)V"))delegate(node,method,"DirectGameWorld","sky");
        else if(signature.equals("a(Lyn;F)V")||signature.equals("b(III)V")||signature.equals("a(II)V")||signature.equals("a(ID)V")||signature.equals("e()V")||signature.equals("f()V"))constant(method,null);
        else if(signature.equals("a(IIID)I"))constant(method,0);
        else if(signature.equals("b()Ljava/lang/String;"))constant(method,"Direct Remix terrain");
        else if(signature.equals("c()Ljava/lang/String;"))constant(method,"Direct Remix entities");
    }
    private static void graphics(ClassNode node,MethodNode method,MethodInsnNode call) {
        String name=call.name;
        switch(name) {
        case "glMatrixMode":retarget(call,T,"select");return;
        case "glLoadIdentity":retarget(call,T,"loadIdentity");return;
        case "glPushMatrix":retarget(call,T,"push");return;
        case "glPopMatrix":retarget(call,T,"pop");return;
        case "glTranslatef":case "glTranslated":retarget(call,T,"translate");return;
        case "glRotatef":retarget(call,T,"rotate");return;
        case "glScalef":case "glScaled":retarget(call,T,"scale");return;
        case "glOrtho":retarget(call,T,"ortho");return;
        case "glColor3f":case "glColor4f":retarget(call,R,"color");return;
        case "glEnable":retarget(call,R,"enable");return;
        case "glDisable":retarget(call,R,"disable");return;
        case "glAlphaFunc":retarget(call,R,"alpha");return;
        case "glBlendFunc":retarget(call,R,"blend");return;
        case "glDepthMask":retarget(call,R,"depthMask");return;
        case "glDepthFunc":retarget(call,R,"depthFunction");return;
        case "glBindTexture":retarget(call,X,"bind");return;
        case "glTexParameteri":retarget(call,X,"parameter");return;
        case "glGenTextures":retarget(call,X,"allocate");return;
        case "glDeleteTextures":retarget(call,X,"delete");return;
        case "glCallList":if(node.name.equals("ps")){retarget(call,"DirectGameGeometry","drawModel");return;}break;
        case "glClear":retarget(call,"DirectGameClient","clear");return;
        case "glGetString":retarget(call,"DirectGameClient","deviceString");return;
        case "glColorMask":retarget(call,R,"colorMask");return;
        // The unlit renderer has no fixed-function lighting, fog or driver error state.
        case "glLight":case "glLightModel":case "glColorMaterial":case "glFog":case "glFogf":case "glFogi":case "glNormal3f":
        case "glShadeModel":case "glClearDepth":case "glCullFace":case "glViewport":case "glLineWidth":
        case "glClearColor":case "glPolygonOffset":
            discard(method,call);return;
        }
        unhandled.add(node.name+"."+method.name+method.desc+" -> "+name+call.desc);
    }
    private static void retarget(MethodInsnNode call,String owner,String name){call.owner=owner;call.name=name;call.itf=false;call.setOpcode(INVOKESTATIC);}
    private static void discard(MethodNode method,MethodInsnNode call){
        InsnList replacement=new InsnList();Type[] args=Type.getArgumentTypes(call.desc);
        for(int i=args.length-1;i>=0;--i)replacement.add(new InsnNode(args[i].getSize()==2?POP2:POP));
        method.instructions.insertBefore(call,replacement);method.instructions.remove(call);
    }
    private static void clear(MethodNode method){method.instructions.clear();method.tryCatchBlocks.clear();if(method.localVariables!=null)method.localVariables.clear();}
    private static void constant(MethodNode method,Object value){
        clear(method);Type type=Type.getReturnType(method.desc);
        if(type.getSort()==Type.VOID){method.instructions.add(new InsnNode(RETURN));return;}
        if(type.getSort()==Type.OBJECT||type.getSort()==Type.ARRAY) {
            method.instructions.add(value instanceof String?new LdcInsnNode(value):new InsnNode(ACONST_NULL));
        }else {
            Number number=value instanceof Number?(Number)value:Integer.valueOf(0);
            if(type.getSort()==Type.FLOAT)value=Float.valueOf(number.floatValue());
            else if(type.getSort()==Type.DOUBLE)value=Double.valueOf(number.doubleValue());
            else if(type.getSort()==Type.LONG)value=Long.valueOf(number.longValue());
            else value=Integer.valueOf(number.intValue());
            method.instructions.add(new LdcInsnNode(value));
        }
        method.instructions.add(new InsnNode(type.getOpcode(IRETURN)));
    }
    private static void emptyConstructor(ClassNode node,MethodNode method){
        clear(method);method.instructions.add(new VarInsnNode(ALOAD,0));method.instructions.add(new MethodInsnNode(INVOKESPECIAL,node.superName,"<init>","()V",false));method.instructions.add(new InsnNode(RETURN));
    }
    private static void delegate(ClassNode node,MethodNode method,String owner,String name){
        clear(method);
        if(method.name.equals("<init>")){
            method.instructions.add(new VarInsnNode(ALOAD,0));method.instructions.add(new MethodInsnNode(INVOKESPECIAL,node.superName,"<init>","()V",false));
        }
        boolean instance=(method.access&ACC_STATIC)==0;int local=instance?1:0;
        if(instance)method.instructions.add(new VarInsnNode(ALOAD,0));
        for(Type arg:Type.getArgumentTypes(method.desc)){method.instructions.add(new VarInsnNode(arg.getOpcode(ILOAD),local));local+=arg.getSize();}
        String desc=instance?"(L"+node.name+";"+method.desc.substring(1):method.desc;
        method.instructions.add(new MethodInsnNode(INVOKESTATIC,owner,name,desc,false));
        method.instructions.add(new InsnNode(Type.getReturnType(method.desc).getOpcode(IRETURN)));
    }
    private static InsnList callThis(String owner,String name,String desc){InsnList list=new InsnList();list.add(new VarInsnNode(ALOAD,0));list.add(new MethodInsnNode(INVOKESTATIC,owner,name,desc,false));return list;}
    private static void beforeReturns(MethodNode method,InsnList call){
        for(AbstractInsnNode instruction:method.instructions.toArray())if(instruction.getOpcode()==RETURN){
            InsnList copy=new InsnList();for(AbstractInsnNode item:call.toArray())copy.add(item.clone(new HashMap<LabelNode,LabelNode>()));method.instructions.insertBefore(instruction,copy);
        }
    }
}
