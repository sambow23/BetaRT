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
    private static final String PARTICLE_CLASS = "xw";
    private static final String REMIX_HELPER_CLASS = "MinecraftRemixHooks";
    private static final String CHUNK_RENDERER_CLASS = "dk";
    private static final String LIVING_RENDER_MANAGER_CLASS = "th";
    private static final String BASE_RENDERER_CLASS = "bw";
    private static final String FIRST_PERSON_RENDERER_CLASS = "ra";
    private static final String WORLD_RENDERER_CLASS = "n";
    private static final String MINECART_RENDERER_CLASS = "tb";
    private static final String SIGN_RENDERER_CLASS = "po";
    private static final String FONT_RENDERER_CLASS = "sj";

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
                } else if (entryName.equals(PARTICLE_CLASS + ".class")) {
                    content = patchXw(content);
                } else if (entryName.equals(WORLD_RENDERER_CLASS + ".class")) {
                    content = patchN(content);
                } else if (entryName.equals(MINECART_RENDERER_CLASS + ".class")) {
                    content = patchTb(content);
                } else if (entryName.equals(SIGN_RENDERER_CLASS + ".class")) {
                    content = patchPo(content);
                } else if (entryName.equals(FONT_RENDERER_CLASS + ".class")) {
                    content = patchSj(content);
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
            } else if (method.name.equals("g") && method.desc.equals("()V")) {
                patchDisplayIsActiveChecks(method);
            } else if (method.name.equals("run") && method.desc.equals("()V")) {
                patchDisplayIsActiveChecks(method);
            }
        }
        return writeClass(classNode);
    }

    private static byte[] patchPx(byte[] content) {
        ClassNode classNode = readClass(content);
        for (MethodNode method : classNode.methods) {
            if (method.name.equals("b") && method.desc.equals("(F)V")) {
                patchDisplayIsActiveChecks(method);
                patchPxFrame(method);
            } else if (method.name.equals("a") && method.desc.equals("(FJ)V")) {
                patchPxRender(method);
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
            } else if (method.name.equals("a") && method.desc.equals("(Lgs;Lvf;ILiz;F)V")) {
                patchDestroyOverlayRender(method);
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

    private static byte[] patchXw(byte[] content) {
        ClassNode classNode = readClass(content);
        for (MethodNode method : classNode.methods) {
            if (method.name.equals("a") && method.desc.equals("(Lnw;FFFFFF)V")) {
                method.instructions.insertBefore(method.instructions.getFirst(), particleRenderCall());
            }
        }
        return writeClass(classNode);
    }

    private static byte[] patchTb(byte[] content) {
        ClassNode classNode = readClass(content);
        for (MethodNode method : classNode.methods) {
            if (method.name.equals("a") && method.desc.equals("(Lyl;DDDFF)V")) {
                patchMinecartRender(method);
            }
        }
        return writeClass(classNode);
    }

    private static byte[] patchPo(byte[] content) {
        ClassNode classNode = readClass(content);
        for (MethodNode method : classNode.methods) {
            if (method.name.equals("a") && method.desc.equals("(Lyk;DDDF)V")) {
                patchSignRender(method);
            }
        }
        return writeClass(classNode);
    }

    private static byte[] patchSj(byte[] content) {
        ClassNode classNode = readClass(content);
        for (MethodNode method : classNode.methods) {
            if (method.name.equals("a") && method.desc.equals("(Ljava/lang/String;IIIZ)V")) {
                patchFontRender(method);
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
        InsnList beginInstructions = new InsnList();
        beginInstructions.add(firstPersonShadowPlayerRenderCall());
        beginInstructions.add(staticHelperCall("onFirstPersonRenderStart", "()V"));
        method.instructions.insertBefore(method.instructions.getFirst(), beginInstructions);

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

    private static void patchDisplayIsActiveChecks(MethodNode method) {
        for (AbstractInsnNode node = method.instructions.getFirst(); node != null; ) {
            AbstractInsnNode next = node.getNext();
            if (isStaticCall(node, DISPLAY_CLASS, "isActive", "()Z")) {
                method.instructions.set(
                        node,
                        new MethodInsnNode(
                                Opcodes.INVOKESTATIC,
                                REMIX_HELPER_CLASS,
                                "isWindowInteractionActive",
                                "()Z",
                                false));
            }
            node = next;
        }
    }

    private static void patchPxRender(MethodNode method) {
        for (AbstractInsnNode node = method.instructions.getFirst(); node != null; node = node.getNext()) {
            if (node instanceof MethodInsnNode methodInsnNode
                    && (methodInsnNode.getOpcode() == Opcodes.INVOKESPECIAL
                            || methodInsnNode.getOpcode() == Opcodes.INVOKEVIRTUAL)
                    && methodInsnNode.owner.equals("px")
                    && methodInsnNode.name.equals("a")
                    && methodInsnNode.desc.equals("(FI)V")) {
                method.instructions.insert(node, staticHelperCall("onFrameViewCaptured", "()V"));
                break;
            }
        }
    }

    private static void patchPxFrame(MethodNode method) {
        method.instructions.insert(cameraUpdateCall());

        for (AbstractInsnNode node = method.instructions.getFirst(); node != null; node = node.getNext()) {
            if (node instanceof MethodInsnNode methodInsnNode
                    && methodInsnNode.getOpcode() == Opcodes.INVOKEVIRTUAL
                    && methodInsnNode.name.equals("a")
                    && methodInsnNode.desc.equals("(FZII)V")) {
                method.instructions.insertBefore(node, uiRenderBeginCall());
                break;
            }
        }

        for (AbstractInsnNode node = method.instructions.getFirst(); node != null; node = node.getNext()) {
            if (node instanceof MethodInsnNode methodInsnNode
                    && methodInsnNode.getOpcode() == Opcodes.INVOKEVIRTUAL
                    && methodInsnNode.owner.equals("px")
                    && methodInsnNode.name.equals("b")
                    && methodInsnNode.desc.equals("()V")) {
                method.instructions.insertBefore(node, uiRenderBeginCall());
                break;
            }
        }

        for (AbstractInsnNode node = method.instructions.getFirst(); node != null; node = node.getNext()) {
            if (isStaticCall(node, "org/lwjgl/opengl/GL11", "glClear", "(I)V")) {
                method.instructions.insertBefore(node, uiRenderBeginCall());
            }
        }

        AbstractInsnNode lastReturn = null;
        for (AbstractInsnNode node = method.instructions.getFirst(); node != null; node = node.getNext()) {
            if (node.getOpcode() == Opcodes.RETURN) {
                lastReturn = node;
            }
        }

        if (lastReturn != null) {
            method.instructions.insertBefore(lastReturn, remixUiTickCall());
            method.instructions.insertBefore(lastReturn, staticHelperCall("onUiRenderEnd", "()V"));
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

    private static void patchDestroyOverlayRender(MethodNode method) {
        for (AbstractInsnNode node = method.instructions.getFirst(); node != null; node = node.getNext()) {
            if (node instanceof MethodInsnNode methodInsnNode
                    && methodInsnNode.getOpcode() == Opcodes.INVOKEVIRTUAL
                    && methodInsnNode.owner.equals("cv")
                    && methodInsnNode.name.equals("a")
                    && methodInsnNode.desc.equals("(Luu;IIII)V")) {
                method.instructions.insertBefore(node, destroyOverlayRenderCall());
                return;
            }
        }
    }

    private static void patchMinecartRender(MethodNode method) {
        method.instructions.insertBefore(method.instructions.getFirst(), livingEntityRenderStartCall());

        for (AbstractInsnNode node = method.instructions.getFirst(); node != null; ) {
            AbstractInsnNode next = node.getNext();
            if (node.getOpcode() == Opcodes.RETURN) {
                method.instructions.insertBefore(node, staticHelperCall("onLivingEntityRenderEnd", "()V"));
            }
            node = next;
        }
    }

    private static void patchSignRender(MethodNode method) {
        boolean insertedStart = false;
        for (AbstractInsnNode node = method.instructions.getFirst(); node != null; node = node.getNext()) {
            if (node instanceof MethodInsnNode methodInsnNode
                    && methodInsnNode.getOpcode() == Opcodes.INVOKEVIRTUAL
                    && methodInsnNode.owner.equals("rf")
                    && methodInsnNode.name.equals("a")
                    && methodInsnNode.desc.equals("()V")) {
                method.instructions.insertBefore(node, signRenderStartCall());
                insertedStart = true;
                break;
            }
        }

        if (!insertedStart) {
            return;
        }

        for (AbstractInsnNode node = method.instructions.getFirst(); node != null; ) {
            AbstractInsnNode next = node.getNext();
            if (node.getOpcode() == Opcodes.RETURN) {
                method.instructions.insertBefore(node, staticHelperCall("onSignRenderEnd", "()V"));
            }
            node = next;
        }
    }

    private static void patchFontRender(MethodNode method) {
        method.instructions.insertBefore(method.instructions.getFirst(), signTextRenderCall());
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

    private static InsnList uiRenderBeginCall() {
        InsnList instructions = new InsnList();
        instructions.add(new VarInsnNode(Opcodes.ALOAD, 0));
        instructions.add(new FieldInsnNode(Opcodes.GETFIELD, "px", "j", "Lnet/minecraft/client/Minecraft;"));
        instructions.add(new FieldInsnNode(Opcodes.GETFIELD, MINECRAFT_CLASS, "d", "I"));
        instructions.add(new VarInsnNode(Opcodes.ALOAD, 0));
        instructions.add(new FieldInsnNode(Opcodes.GETFIELD, "px", "j", "Lnet/minecraft/client/Minecraft;"));
        instructions.add(new FieldInsnNode(Opcodes.GETFIELD, MINECRAFT_CLASS, "e", "I"));
        instructions.add(new MethodInsnNode(Opcodes.INVOKESTATIC, REMIX_HELPER_CLASS, "onUiRenderBegin", "(II)V", false));
        return instructions;
    }

    private static InsnList remixUiTickCall() {
        InsnList instructions = new InsnList();
        instructions.add(new VarInsnNode(Opcodes.ALOAD, 0));
        instructions.add(new FieldInsnNode(Opcodes.GETFIELD, "px", "j", "Lnet/minecraft/client/Minecraft;"));
        instructions.add(new MethodInsnNode(
                Opcodes.INVOKESTATIC,
                REMIX_HELPER_CLASS,
                "onRemixUiTick",
                "(Lnet/minecraft/client/Minecraft;)V",
                false));
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

    private static InsnList signRenderStartCall() {
        InsnList instructions = new InsnList();
        instructions.add(new VarInsnNode(Opcodes.ALOAD, 1));
        instructions.add(new MethodInsnNode(Opcodes.INVOKESTATIC, REMIX_HELPER_CLASS, "onSignRenderStart", "(Lyk;)V", false));
        return instructions;
    }

    private static InsnList signTextRenderCall() {
        InsnList instructions = new InsnList();
        instructions.add(new VarInsnNode(Opcodes.ALOAD, 1));
        instructions.add(new VarInsnNode(Opcodes.ILOAD, 2));
        instructions.add(new VarInsnNode(Opcodes.ILOAD, 3));
        instructions.add(new VarInsnNode(Opcodes.ILOAD, 4));
        instructions.add(new VarInsnNode(Opcodes.ILOAD, 5));
        instructions.add(new VarInsnNode(Opcodes.ALOAD, 0));
        instructions.add(new FieldInsnNode(Opcodes.GETFIELD, FONT_RENDERER_CLASS, "b", "[I"));
        instructions.add(new MethodInsnNode(
                Opcodes.INVOKESTATIC,
                REMIX_HELPER_CLASS,
                "onSignTextRender",
                "(Ljava/lang/String;IIIZ[I)V",
                false));
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

    private static InsnList firstPersonShadowPlayerRenderCall() {
        InsnList instructions = new InsnList();
        instructions.add(new VarInsnNode(Opcodes.ALOAD, 0));
        instructions.add(new FieldInsnNode(Opcodes.GETFIELD, FIRST_PERSON_RENDERER_CLASS, "a", "Lnet/minecraft/client/Minecraft;"));
        instructions.add(new VarInsnNode(Opcodes.FLOAD, 1));
        instructions.add(new MethodInsnNode(
                Opcodes.INVOKESTATIC,
                REMIX_HELPER_CLASS,
                "onFirstPersonShadowPlayerRender",
                "(Lnet/minecraft/client/Minecraft;F)V",
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

    private static InsnList particleRenderCall() {
        InsnList instructions = new InsnList();
        instructions.add(new VarInsnNode(Opcodes.ALOAD, 0));
        instructions.add(new VarInsnNode(Opcodes.FLOAD, 2));
        instructions.add(new VarInsnNode(Opcodes.FLOAD, 3));
        instructions.add(new VarInsnNode(Opcodes.FLOAD, 4));
        instructions.add(new VarInsnNode(Opcodes.FLOAD, 5));
        instructions.add(new VarInsnNode(Opcodes.FLOAD, 6));
        instructions.add(new VarInsnNode(Opcodes.FLOAD, 7));
        instructions.add(new MethodInsnNode(
                Opcodes.INVOKESTATIC,
                REMIX_HELPER_CLASS,
                "onParticleRender",
                "(Lxw;FFFFFF)V",
                false));
        return instructions;
    }

    private static InsnList destroyOverlayRenderCall() {
        InsnList instructions = new InsnList();
        instructions.add(new VarInsnNode(Opcodes.ALOAD, 2));
        instructions.add(new FieldInsnNode(Opcodes.GETFIELD, "vf", "b", "I"));
        instructions.add(new VarInsnNode(Opcodes.ALOAD, 2));
        instructions.add(new FieldInsnNode(Opcodes.GETFIELD, "vf", "c", "I"));
        instructions.add(new VarInsnNode(Opcodes.ALOAD, 2));
        instructions.add(new FieldInsnNode(Opcodes.GETFIELD, "vf", "d", "I"));
        instructions.add(new VarInsnNode(Opcodes.ALOAD, 0));
        instructions.add(new FieldInsnNode(Opcodes.GETFIELD, WORLD_RENDERER_CLASS, "i", "F"));
        instructions.add(new MethodInsnNode(
                Opcodes.INVOKESTATIC,
                REMIX_HELPER_CLASS,
                "onDestroyOverlayRender",
                "(IIIF)V",
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
        classReader.accept(classNode, ClassReader.EXPAND_FRAMES);
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