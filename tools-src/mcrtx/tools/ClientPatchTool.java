package mcrtx.tools;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarOutputStream;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;

public final class ClientPatchTool {
    private static final String MINECRAFT_CLASS = "net/minecraft/client/Minecraft";
    private static final String DISPLAY_CLASS = "org/lwjgl/opengl/Display";
    private static final String MOUSE_CLASS = "org/lwjgl/input/Mouse";
    private static final String MODEL_PART_CLASS = "ps";
    private static final String TESSELLATOR_CLASS = "nw";
    private static final String REMIX_HELPER_CLASS = "MinecraftRemixHooks";
    private static final String CHUNK_RENDERER_CLASS = "dk";
    private static final String LIVING_RENDER_MANAGER_CLASS = "th";
    private static final String BASE_RENDERER_CLASS = "bw";
    private static final String FIRST_PERSON_RENDERER_CLASS = "ra";
    private static final String WORLD_RENDERER_CLASS = "n";

    private ClientPatchTool() {
    }

    public static void main(String[] args) throws Exception {
        if (args.length != 2) {
            throw new IllegalArgumentException("Usage: ClientPatchTool <input-jar> <output-jar>");
        }

        Path inputJar = Paths.get(args[0]);
        Path outputJar = Paths.get(args[1]);
        Files.createDirectories(outputJar.getParent());

        try (JarFile jarFile = new JarFile(inputJar.toFile());
             JarOutputStream jarOutputStream = new JarOutputStream(Files.newOutputStream(outputJar))) {
            Enumeration<JarEntry> entries = jarFile.entries();
            while (entries.hasMoreElements()) {
                JarEntry entry = entries.nextElement();
                byte[] content;
                try (InputStream inputStream = jarFile.getInputStream(entry)) {
                    content = readAllBytes(inputStream);
                }

                String entryName = entry.getName();
                if (entryName.equals(MINECRAFT_CLASS + ".class")) {
                    content = patchMinecraft(content);
                } else if (entryName.equals("px.class")) {
                    content = patchPx(content);
                } else if (entryName.equals(CHUNK_RENDERER_CLASS + ".class")) {
                    content = patchDk(content);
                } else if (entryName.equals(LIVING_RENDER_MANAGER_CLASS + ".class")) {
                    content = patchTh(content);
                } else if (entryName.equals(BASE_RENDERER_CLASS + ".class")) {
                    content = patchBw(content);
                } else if (entryName.equals(FIRST_PERSON_RENDERER_CLASS + ".class")) {
                    content = patchRa(content);
                } else if (entryName.equals(MODEL_PART_CLASS + ".class")) {
                    content = patchPs(content);
                } else if (entryName.equals(TESSELLATOR_CLASS + ".class")) {
                    content = patchNw(content);
                } else if (entryName.equals(WORLD_RENDERER_CLASS + ".class")) {
                    content = patchN(content);
                }

                JarEntry newEntry = new JarEntry(entryName);
                jarOutputStream.putNextEntry(newEntry);
                jarOutputStream.write(content);
                jarOutputStream.closeEntry();
            }
        }
    }

    private static byte[] patchMinecraft(byte[] content) {
        ClassNode classNode = readClass(content);
        for (MethodNode method : classNode.methods) {
            if (method.name.equals("a") && method.desc.equals("()V")) {
                patchMinecraftStartup(method);
            } else if (method.name.equals("d") && method.desc.equals("()V")) {
                patchMinecraftShutdown(method);
            } else if (method.name.equals("j") && method.desc.equals("()V")) {
                patchMinecraftDisplayReset(method);
            } else if (method.name.equals("a") && method.desc.equals("(II)V")) {
                patchMinecraftResize(method);
            }
        }
        return writeClass(classNode);
    }

    private static byte[] patchPx(byte[] content) {
        ClassNode classNode = readClass(content);
        for (MethodNode method : classNode.methods) {
            if (method.name.equals("b") && method.desc.equals("(F)V")) {
                patchPxFrame(method);
            }
        }
        return writeClass(classNode);
    }

    private static byte[] patchDk(byte[] content) {
        ClassNode classNode = readClass(content);
        for (MethodNode method : classNode.methods) {
            if (method.name.equals("a") && method.desc.equals("()V")) {
                patchDkChunkBuild(method);
            }
        }
        return writeClass(classNode);
    }

    private static byte[] patchN(byte[] content) {
        ClassNode classNode = readClass(content);
        for (MethodNode method : classNode.methods) {
            if (method.name.equals("a") && method.desc.equals("(Lfd;)V")) {
                method.instructions.insertBefore(method.instructions.getFirst(), worldChangedCall());
            } else if (method.name.equals("b") && method.desc.equals("(F)V")) {
                patchCloudRender(method, false);
            } else if (method.name.equals("c") && method.desc.equals("(F)V")) {
                patchCloudRender(method, true);
            }
        }
        return writeClass(classNode);
    }

    private static byte[] patchTh(byte[] content) {
        ClassNode classNode = readClass(content);
        for (MethodNode method : classNode.methods) {
            if (method.name.equals("a") && method.desc.equals("(Lfd;Lji;Lsj;Lls;Lkv;F)V")) {
                patchLivingEntityFrameBegin(method);
            } else if (method.name.equals("a") && method.desc.equals("(Lsn;DDDFF)V")) {
                patchLivingEntityRender(method);
            }
        }
        return writeClass(classNode);
    }

    private static byte[] patchBw(byte[] content) {
        ClassNode classNode = readClass(content);
        for (MethodNode method : classNode.methods) {
            if (method.name.equals("a") && method.desc.equals("(Ljava/lang/String;)V")) {
                method.instructions.insertBefore(method.instructions.getFirst(), entityTextureBindCallSingle());
            } else if (method.name.equals("a") && method.desc.equals("(Ljava/lang/String;Ljava/lang/String;)Z")) {
                method.instructions.insertBefore(method.instructions.getFirst(), entityTextureBindCallFallback());
            }
        }
        return writeClass(classNode);
    }

    private static byte[] patchRa(byte[] content) {
        ClassNode classNode = readClass(content);
        for (MethodNode method : classNode.methods) {
            if (method.name.equals("a") && method.desc.equals("(F)V")) {
                patchFirstPersonRender(method);
            } else if (method.name.equals("a") && method.desc.equals("(Lls;Liz;)V")) {
                patchFirstPersonItemRender(method);
            }
        }
        return writeClass(classNode);
    }

    private static byte[] patchNw(byte[] content) {
        ClassNode classNode = readClass(content);
        for (MethodNode method : classNode.methods) {
            if (method.name.equals("a") && method.desc.equals("()V")) {
                patchTessellatorDraw(method);
            }
        }
        return writeClass(classNode);
    }

    private static byte[] patchPs(byte[] content) {
        ClassNode classNode = readClass(content);
        for (MethodNode method : classNode.methods) {
            if (method.name.equals("a") && method.desc.equals("(F)V")) {
                patchModelPartRender(method);
            } else if (method.name.equals("b") && method.desc.equals("(F)V")) {
                patchModelPartRender(method);
            }
        }
        return writeClass(classNode);
    }

    private static void patchMinecraftStartup(MethodNode method) {
        for (AbstractInsnNode node = method.instructions.getFirst(); node != null; node = node.getNext()) {
            if (isStaticCall(node, DISPLAY_CLASS, "create", "()V")) {
                method.instructions.insert(node, onDisplayCreatedCall());
            }
        }
    }

    private static void patchFirstPersonRender(MethodNode method) {
        method.instructions.insertBefore(method.instructions.getFirst(), staticHelperCall("onFirstPersonRenderStart", "()V"));

        for (AbstractInsnNode node = method.instructions.getFirst(); node != null; ) {
            AbstractInsnNode next = node.getNext();
            if (node.getOpcode() == Opcodes.RETURN) {
                method.instructions.insertBefore(node, staticHelperCall("onFirstPersonRenderEnd", "()V"));
            }
            node = next;
        }
    }

    private static void patchFirstPersonItemRender(MethodNode method) {
        method.instructions.insertBefore(method.instructions.getFirst(), firstPersonItemRenderCall());
    }

    private static void patchTessellatorDraw(MethodNode method) {
        for (AbstractInsnNode node = method.instructions.getFirst(); node != null; node = node.getNext()) {
            if (isStaticCall(node, "org/lwjgl/opengl/GL11", "glDrawArrays", "(III)V")) {
                method.instructions.insertBefore(node, firstPersonTessellatorDrawCall());
            }
        }
    }

    private static void patchMinecraftShutdown(MethodNode method) {
        for (AbstractInsnNode node = method.instructions.getFirst(); node != null; node = node.getNext()) {
            if (isStaticCall(node, MOUSE_CLASS, "destroy", "()V")) {
                method.instructions.insertBefore(node, staticHelperCall("onShutdown", "()V"));
                return;
            }
        }
    }

    private static void patchMinecraftDisplayReset(MethodNode method) {
        for (AbstractInsnNode node = method.instructions.getFirst(); node != null; node = node.getNext()) {
            if (isStaticCall(node, DISPLAY_CLASS, "update", "()V")) {
                method.instructions.insert(node, onDisplayResetCall());
                return;
            }
        }
    }

    private static void patchMinecraftResize(MethodNode method) {
        for (AbstractInsnNode node = method.instructions.getFirst(); node != null; node = node.getNext()) {
            if (node instanceof FieldInsnNode field && node.getOpcode() == Opcodes.PUTFIELD) {
                if (field.owner.equals(MINECRAFT_CLASS) && field.name.equals("e") && field.desc.equals("I")) {
                    method.instructions.insert(node, onResizeCall());
                    return;
                }
            }
        }
    }

    private static void patchPxFrame(MethodNode method) {
        method.instructions.insert(cameraUpdateCall());

        AbstractInsnNode lastReturn = null;
        for (AbstractInsnNode node = method.instructions.getFirst(); node != null; node = node.getNext()) {
            if (node.getOpcode() == Opcodes.RETURN) {
                lastReturn = node;
            }
        }

        if (lastReturn != null) {
            method.instructions.insertBefore(lastReturn, staticHelperCall("onPresent", "()V"));
        }
    }

    private static void patchDkChunkBuild(MethodNode method) {
        final int chunkBuildEnabledLocal = 21;

        for (AbstractInsnNode node = method.instructions.getFirst(); node != null; node = node.getNext()) {
            if (isStore(node, 14)) {
                method.instructions.insert(node, beginChunkBuildCall(chunkBuildEnabledLocal));
                break;
            }
        }

        for (AbstractInsnNode node = method.instructions.getFirst(); node != null; node = node.getNext()) {
            if (node instanceof MethodInsnNode methodInsnNode
                    && methodInsnNode.getOpcode() == Opcodes.INVOKEVIRTUAL
                    && methodInsnNode.owner.equals("cv")
                    && methodInsnNode.name.equals("b")
                    && methodInsnNode.desc.equals("(Luu;III)Z")) {
                AbstractInsnNode insertionPoint = node;
                while (insertionPoint.getPrevious() != null && !(insertionPoint.getPrevious() instanceof VarInsnNode varInsnNode
                        && varInsnNode.getOpcode() == Opcodes.ILOAD
                        && varInsnNode.var == 13)) {
                    insertionPoint = insertionPoint.getPrevious();
                }
                method.instructions.insertBefore(insertionPoint, captureChunkBlockCall(chunkBuildEnabledLocal));
                break;
            }
        }

        for (AbstractInsnNode node = method.instructions.getFirst(); node != null; node = node.getNext()) {
            if (isLoad(node, 12)) {
                method.instructions.insertBefore(node, endChunkBuildCall(chunkBuildEnabledLocal));
                break;
            }
        }
    }

    private static void patchCloudRender(MethodNode method, boolean fancy) {
        method.instructions.insertBefore(method.instructions.getFirst(), cloudRenderCall(fancy));
    }

    private static void patchLivingEntityFrameBegin(MethodNode method) {
        method.instructions.insertBefore(method.instructions.getFirst(), staticHelperCall("onLivingEntityFrameBegin", "()V"));
    }

    private static void patchLivingEntityRender(MethodNode method) {
        for (AbstractInsnNode node = method.instructions.getFirst(); node != null; node = node.getNext()) {
            if (node instanceof MethodInsnNode methodInsnNode
                    && methodInsnNode.getOpcode() == Opcodes.INVOKEVIRTUAL
                    && methodInsnNode.owner.equals(BASE_RENDERER_CLASS)
                    && methodInsnNode.name.equals("a")
                    && methodInsnNode.desc.equals("(Lsn;DDDFF)V")) {
                method.instructions.insertBefore(node, livingEntityRenderStartCall());
                method.instructions.insert(node, staticHelperCall("onLivingEntityRenderEnd", "()V"));
                return;
            }
        }
    }

    private static void patchModelPartRender(MethodNode method) {
        for (AbstractInsnNode node = method.instructions.getFirst(); node != null; node = node.getNext()) {
            if (isStaticCall(node, "org/lwjgl/opengl/GL11", "glCallList", "(I)V")) {
                method.instructions.insertBefore(node, modelPartRenderCall());
            }
        }
    }

    private static boolean isStaticCall(AbstractInsnNode node, String owner, String name, String desc) {
        if (!(node instanceof MethodInsnNode methodInsnNode)) {
            return false;
        }
        return methodInsnNode.getOpcode() == Opcodes.INVOKESTATIC
                && methodInsnNode.owner.equals(owner)
                && methodInsnNode.name.equals(name)
                && methodInsnNode.desc.equals(desc);
    }

    private static boolean isStore(AbstractInsnNode node, int localIndex) {
        return node instanceof VarInsnNode varInsnNode
                && varInsnNode.getOpcode() == Opcodes.ISTORE
                && varInsnNode.var == localIndex;
    }

    private static boolean isLoad(AbstractInsnNode node, int localIndex) {
        return node instanceof VarInsnNode varInsnNode
                && varInsnNode.getOpcode() == Opcodes.ILOAD
                && varInsnNode.var == localIndex;
    }

    private static InsnList onDisplayCreatedCall() {
        InsnList instructions = new InsnList();
        instructions.add(new VarInsnNode(Opcodes.ALOAD, 0));
        instructions.add(new FieldInsnNode(Opcodes.GETFIELD, MINECRAFT_CLASS, "d", "I"));
        instructions.add(new VarInsnNode(Opcodes.ALOAD, 0));
        instructions.add(new FieldInsnNode(Opcodes.GETFIELD, MINECRAFT_CLASS, "e", "I"));
        instructions.add(new MethodInsnNode(Opcodes.INVOKESTATIC, REMIX_HELPER_CLASS, "onDisplayCreated", "(II)V", false));
        return instructions;
    }

    private static InsnList onDisplayResetCall() {
        InsnList instructions = new InsnList();
        instructions.add(new VarInsnNode(Opcodes.ALOAD, 0));
        instructions.add(new FieldInsnNode(Opcodes.GETFIELD, MINECRAFT_CLASS, "d", "I"));
        instructions.add(new VarInsnNode(Opcodes.ALOAD, 0));
        instructions.add(new FieldInsnNode(Opcodes.GETFIELD, MINECRAFT_CLASS, "e", "I"));
        instructions.add(new MethodInsnNode(Opcodes.INVOKESTATIC, REMIX_HELPER_CLASS, "onDisplayReset", "(II)V", false));
        return instructions;
    }

    private static InsnList onResizeCall() {
        InsnList instructions = new InsnList();
        instructions.add(new VarInsnNode(Opcodes.ALOAD, 0));
        instructions.add(new FieldInsnNode(Opcodes.GETFIELD, MINECRAFT_CLASS, "d", "I"));
        instructions.add(new VarInsnNode(Opcodes.ALOAD, 0));
        instructions.add(new FieldInsnNode(Opcodes.GETFIELD, MINECRAFT_CLASS, "e", "I"));
        instructions.add(new MethodInsnNode(Opcodes.INVOKESTATIC, REMIX_HELPER_CLASS, "onResize", "(II)V", false));
        return instructions;
    }

    private static InsnList worldChangedCall() {
        InsnList instructions = new InsnList();
        instructions.add(new VarInsnNode(Opcodes.ALOAD, 1));
        instructions.add(new MethodInsnNode(Opcodes.INVOKESTATIC, REMIX_HELPER_CLASS, "onWorldChanged", "(Lfd;)V", false));
        return instructions;
    }

    private static InsnList cameraUpdateCall() {
        InsnList instructions = new InsnList();
        instructions.add(new VarInsnNode(Opcodes.ALOAD, 0));
        instructions.add(new FieldInsnNode(Opcodes.GETFIELD, "px", "j", "Lnet/minecraft/client/Minecraft;"));
        instructions.add(new FieldInsnNode(Opcodes.GETFIELD, MINECRAFT_CLASS, "i", "Lls;"));
        instructions.add(new VarInsnNode(Opcodes.FLOAD, 1));
        instructions.add(new VarInsnNode(Opcodes.ALOAD, 0));
        instructions.add(new FieldInsnNode(Opcodes.GETFIELD, "px", "j", "Lnet/minecraft/client/Minecraft;"));
        instructions.add(new FieldInsnNode(Opcodes.GETFIELD, MINECRAFT_CLASS, "d", "I"));
        instructions.add(new VarInsnNode(Opcodes.ALOAD, 0));
        instructions.add(new FieldInsnNode(Opcodes.GETFIELD, "px", "j", "Lnet/minecraft/client/Minecraft;"));
        instructions.add(new FieldInsnNode(Opcodes.GETFIELD, MINECRAFT_CLASS, "e", "I"));
        instructions.add(new VarInsnNode(Opcodes.ALOAD, 0));
        instructions.add(new FieldInsnNode(Opcodes.GETFIELD, "px", "k", "F"));
        instructions.add(new MethodInsnNode(Opcodes.INVOKESTATIC, REMIX_HELPER_CLASS, "onCamera", "(Lls;FIIF)V", false));
        return instructions;
    }

    private static InsnList staticHelperCall(String name, String descriptor) {
        InsnList instructions = new InsnList();
        instructions.add(new MethodInsnNode(Opcodes.INVOKESTATIC, REMIX_HELPER_CLASS, name, descriptor, false));
        return instructions;
    }

    private static InsnList livingEntityRenderStartCall() {
        InsnList instructions = new InsnList();
        instructions.add(new VarInsnNode(Opcodes.ALOAD, 1));
        instructions.add(new MethodInsnNode(Opcodes.INVOKESTATIC, REMIX_HELPER_CLASS, "onLivingEntityRenderStart", "(Lsn;)V", false));
        return instructions;
    }

    private static InsnList entityTextureBindCallSingle() {
        InsnList instructions = new InsnList();
        instructions.add(new VarInsnNode(Opcodes.ALOAD, 1));
        instructions.add(new InsnNode(Opcodes.ACONST_NULL));
        instructions.add(new MethodInsnNode(
                Opcodes.INVOKESTATIC,
                REMIX_HELPER_CLASS,
                "onEntityTextureBind",
                "(Ljava/lang/String;Ljava/lang/String;)V",
                false));
        return instructions;
    }

    private static InsnList entityTextureBindCallFallback() {
        InsnList instructions = new InsnList();
        instructions.add(new VarInsnNode(Opcodes.ALOAD, 1));
        instructions.add(new VarInsnNode(Opcodes.ALOAD, 2));
        instructions.add(new MethodInsnNode(
                Opcodes.INVOKESTATIC,
                REMIX_HELPER_CLASS,
                "onEntityTextureBind",
                "(Ljava/lang/String;Ljava/lang/String;)V",
                false));
        return instructions;
    }

    private static InsnList modelPartRenderCall() {
        InsnList instructions = new InsnList();
        instructions.add(new VarInsnNode(Opcodes.ALOAD, 0));
        instructions.add(new FieldInsnNode(Opcodes.GETFIELD, MODEL_PART_CLASS, "k", "[Ltz;"));
        instructions.add(new VarInsnNode(Opcodes.FLOAD, 1));
        instructions.add(new MethodInsnNode(
                Opcodes.INVOKESTATIC,
                REMIX_HELPER_CLASS,
                "onModelPartRender",
                "([Ltz;F)V",
                false));
        return instructions;
    }

    private static InsnList firstPersonItemRenderCall() {
        InsnList instructions = new InsnList();
        instructions.add(new VarInsnNode(Opcodes.ALOAD, 2));
        instructions.add(new MethodInsnNode(
                Opcodes.INVOKESTATIC,
                REMIX_HELPER_CLASS,
                "onFirstPersonItemRender",
                "(Liz;)V",
                false));
        return instructions;
    }

    private static InsnList firstPersonTessellatorDrawCall() {
        InsnList instructions = new InsnList();
        instructions.add(new VarInsnNode(Opcodes.ALOAD, 0));
        instructions.add(new FieldInsnNode(Opcodes.GETFIELD, TESSELLATOR_CLASS, "g", "[I"));
        instructions.add(new VarInsnNode(Opcodes.ALOAD, 0));
        instructions.add(new FieldInsnNode(Opcodes.GETFIELD, TESSELLATOR_CLASS, "h", "I"));
        instructions.add(new VarInsnNode(Opcodes.ALOAD, 0));
        instructions.add(new FieldInsnNode(Opcodes.GETFIELD, TESSELLATOR_CLASS, "r", "I"));
        instructions.add(new VarInsnNode(Opcodes.ALOAD, 0));
        instructions.add(new FieldInsnNode(Opcodes.GETFIELD, TESSELLATOR_CLASS, "m", "Z"));
        instructions.add(new VarInsnNode(Opcodes.ALOAD, 0));
        instructions.add(new FieldInsnNode(Opcodes.GETFIELD, TESSELLATOR_CLASS, "l", "Z"));
        instructions.add(new MethodInsnNode(
                Opcodes.INVOKESTATIC,
                REMIX_HELPER_CLASS,
                "onFirstPersonTessellatorDraw",
                "([IIIZZ)V",
                false));
        return instructions;
    }

    private static InsnList cloudRenderCall(boolean fancy) {
        InsnList instructions = new InsnList();
        instructions.add(new VarInsnNode(Opcodes.ALOAD, 0));
        instructions.add(new FieldInsnNode(Opcodes.GETFIELD, WORLD_RENDERER_CLASS, "t", "Lnet/minecraft/client/Minecraft;"));
        instructions.add(new VarInsnNode(Opcodes.ALOAD, 0));
        instructions.add(new FieldInsnNode(Opcodes.GETFIELD, WORLD_RENDERER_CLASS, "k", "Lfd;"));
        instructions.add(new VarInsnNode(Opcodes.ALOAD, 0));
        instructions.add(new FieldInsnNode(Opcodes.GETFIELD, WORLD_RENDERER_CLASS, "x", "I"));
        instructions.add(new VarInsnNode(Opcodes.FLOAD, 1));
        instructions.add(new InsnNode(fancy ? Opcodes.ICONST_1 : Opcodes.ICONST_0));
        instructions.add(new MethodInsnNode(
                Opcodes.INVOKESTATIC,
                REMIX_HELPER_CLASS,
                "onCloudRender",
            "(Lnet/minecraft/client/Minecraft;Lfd;IFZ)V",
                false));
        return instructions;
    }

    private static InsnList beginChunkBuildCall(int resultLocalIndex) {
        InsnList instructions = new InsnList();
        instructions.add(new VarInsnNode(Opcodes.ALOAD, 0));
        instructions.add(new FieldInsnNode(Opcodes.GETFIELD, CHUNK_RENDERER_CLASS, "c", "I"));
        instructions.add(new VarInsnNode(Opcodes.ALOAD, 0));
        instructions.add(new FieldInsnNode(Opcodes.GETFIELD, CHUNK_RENDERER_CLASS, "d", "I"));
        instructions.add(new VarInsnNode(Opcodes.ALOAD, 0));
        instructions.add(new FieldInsnNode(Opcodes.GETFIELD, CHUNK_RENDERER_CLASS, "e", "I"));
        instructions.add(new VarInsnNode(Opcodes.ALOAD, 0));
        instructions.add(new FieldInsnNode(Opcodes.GETFIELD, CHUNK_RENDERER_CLASS, "f", "I"));
        instructions.add(new VarInsnNode(Opcodes.ALOAD, 0));
        instructions.add(new FieldInsnNode(Opcodes.GETFIELD, CHUNK_RENDERER_CLASS, "g", "I"));
        instructions.add(new VarInsnNode(Opcodes.ALOAD, 0));
        instructions.add(new FieldInsnNode(Opcodes.GETFIELD, CHUNK_RENDERER_CLASS, "h", "I"));
        instructions.add(new VarInsnNode(Opcodes.ILOAD, 11));
        instructions.add(new MethodInsnNode(
                Opcodes.INVOKESTATIC,
                REMIX_HELPER_CLASS,
                "onChunkBuildBegin",
                "(IIIIIII)Z",
                false));
        instructions.add(new VarInsnNode(Opcodes.ISTORE, resultLocalIndex));
        return instructions;
    }

    private static InsnList captureChunkBlockCall(int chunkBuildEnabledLocal) {
        LabelNode skip = new LabelNode();
        InsnList instructions = new InsnList();
        instructions.add(new VarInsnNode(Opcodes.ILOAD, chunkBuildEnabledLocal));
        instructions.add(new JumpInsnNode(Opcodes.IFEQ, skip));
        instructions.add(new VarInsnNode(Opcodes.ALOAD, 9));
        instructions.add(new VarInsnNode(Opcodes.ILOAD, 17));
        instructions.add(new VarInsnNode(Opcodes.ILOAD, 15));
        instructions.add(new VarInsnNode(Opcodes.ILOAD, 16));
        instructions.add(new VarInsnNode(Opcodes.ILOAD, 18));
        instructions.add(new VarInsnNode(Opcodes.ALOAD, 9));
        instructions.add(new VarInsnNode(Opcodes.ILOAD, 17));
        instructions.add(new VarInsnNode(Opcodes.ILOAD, 15));
        instructions.add(new VarInsnNode(Opcodes.ILOAD, 16));
        instructions.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "ew", "e", "(III)I", false));
        instructions.add(new VarInsnNode(Opcodes.ALOAD, 19));
        instructions.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "uu", "b", "()I", false));
        instructions.add(new MethodInsnNode(
                Opcodes.INVOKESTATIC,
                REMIX_HELPER_CLASS,
                "onChunkBlock",
            "(Lew;IIIIII)V",
                false));
        instructions.add(skip);
        return instructions;
    }

    private static InsnList endChunkBuildCall(int chunkBuildEnabledLocal) {
        LabelNode skip = new LabelNode();
        InsnList instructions = new InsnList();
        instructions.add(new VarInsnNode(Opcodes.ILOAD, chunkBuildEnabledLocal));
        instructions.add(new JumpInsnNode(Opcodes.IFEQ, skip));
        instructions.add(new VarInsnNode(Opcodes.ILOAD, 13));
        instructions.add(new MethodInsnNode(
                Opcodes.INVOKESTATIC,
                REMIX_HELPER_CLASS,
                "onChunkBuildEnd",
                "(Z)V",
                false));
        instructions.add(skip);
        return instructions;
    }

    private static ClassNode readClass(byte[] content) {
        ClassNode classNode = new ClassNode();
        ClassReader classReader = new ClassReader(content);
        classReader.accept(classNode, 0);
        return classNode;
    }

    private static byte[] writeClass(ClassNode classNode) {
        ClassWriter classWriter = new SafeClassWriter(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);
        classNode.accept(classWriter);
        return classWriter.toByteArray();
    }

    private static byte[] readAllBytes(InputStream inputStream) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        byte[] buffer = new byte[8192];
        int read;
        while ((read = inputStream.read(buffer)) != -1) {
            outputStream.write(buffer, 0, read);
        }
        return outputStream.toByteArray();
    }

    private static final class SafeClassWriter extends ClassWriter {
        private SafeClassWriter(int flags) {
            super(flags);
        }

        @Override
        protected String getCommonSuperClass(String type1, String type2) {
            if (type1.equals(type2)) {
                return type1;
            }
            return "java/lang/Object";
        }
    }
}