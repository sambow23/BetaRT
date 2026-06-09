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
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;

public final class ClientPatchTool {
    private static final String MINECRAFT_CLASS = "net/minecraft/client/Minecraft";
    private static final String DISPLAY_CLASS = "org/lwjgl/opengl/Display";
    private static final String MOUSE_CLASS = "org/lwjgl/input/Mouse";
    private static final String AL10_CLASS = "org/lwjgl/openal/AL10";
    private static final String LEGACY_AL10_CLASS = "mcrtx/lwjglshim/LegacyAL10";
    private static final String ARB_OCCLUSION_QUERY_CLASS = "org/lwjgl/opengl/ARBOcclusionQuery";
    private static final String LEGACY_ARB_OCCLUSION_QUERY_CLASS = "mcrtx/lwjglshim/LegacyARBOcclusionQuery";
    private static final String GL11_CLASS = "org/lwjgl/opengl/GL11";
    private static final String LEGACY_GL11_CLASS = "mcrtx/lwjglshim/LegacyGL11";
    private static final String RENDER_HOOKS_CLASS = "mcrtx/bridge/MinecraftRenderHooks";
    private static final String MODEL_PART_CLASS = "ps";
    private static final String TESSELLATOR_CLASS = "nw";
    private static final String PARTICLE_CLASS = "xw";
    private static final String BLOCK_PARTICLE_CLASS = "qm";
    private static final String ITEM_PARTICLE_CLASS = "pb";
    private static final String PICKUP_PARTICLE_CLASS = "em";
    private static final String REMIX_HELPER_CLASS = "MinecraftRemixHooks";
    private static final String CHUNK_RENDERER_CLASS = "dk";
    private static final String LIVING_RENDER_MANAGER_CLASS = "th";
    private static final String BASE_RENDERER_CLASS = "bw";
    private static final String ITEM_ENTITY_RENDERER_CLASS = "bb";
    private static final String PLAYER_RENDERER_CLASS = "ds";
    private static final String HUMANOID_MOB_RENDERER_CLASS = "v";
    private static final String FIRST_PERSON_RENDERER_CLASS = "ra";
    private static final String WORLD_RENDERER_CLASS = "n";
    private static final String OPTIONS_SCREEN_CLASS = "co";
    private static final String MINECART_RENDERER_CLASS = "tb";
    private static final String PAINTING_RENDERER_CLASS = "dy";
    private static final String SIGN_RENDERER_CLASS = "po";
    private static final String MOVING_PISTON_RENDERER_CLASS = "hy";
    private static final String FONT_RENDERER_CLASS = "sj";
    private static final String GUI_INGAME_CLASS = "uq";
    private static final String GUI_SCREEN_CLASS = "da";
    private static final int MCRTX_OPTIONS_BUTTON_ID = 102;

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
                } else if (entryName.equals(ITEM_ENTITY_RENDERER_CLASS + ".class")) {
                    content = patchBb(content);
                } else if (entryName.equals(PLAYER_RENDERER_CLASS + ".class")) {
                    content = patchDs(content);
                } else if (entryName.equals(HUMANOID_MOB_RENDERER_CLASS + ".class")) {
                    content = patchV(content);
                } else if (entryName.equals(FIRST_PERSON_RENDERER_CLASS + ".class")) {
                    content = patchRa(content);
                } else if (entryName.equals(MODEL_PART_CLASS + ".class")) {
                    content = patchPs(content);
                } else if (entryName.equals(TESSELLATOR_CLASS + ".class")) {
                    content = patchNw(content);
                } else if (entryName.equals(PARTICLE_CLASS + ".class")) {
                    content = patchParticle(content, "captureParticleRender");
                } else if (entryName.equals(BLOCK_PARTICLE_CLASS + ".class")
                        || entryName.equals(ITEM_PARTICLE_CLASS + ".class")) {
                    content = patchParticle(content, "captureAnimatedParticleRender");
                } else if (entryName.equals(PICKUP_PARTICLE_CLASS + ".class")) {
                    content = patchEm(content);
                } else if (entryName.equals(OPTIONS_SCREEN_CLASS + ".class")) {
                    content = patchCo(content);
                } else if (entryName.equals(WORLD_RENDERER_CLASS + ".class")) {
                    content = patchN(content);
                } else if (entryName.equals(MINECART_RENDERER_CLASS + ".class")) {
                    content = patchTb(content);
                } else if (entryName.equals(PAINTING_RENDERER_CLASS + ".class")) {
                    content = patchDy(content);
                } else if (entryName.equals(SIGN_RENDERER_CLASS + ".class")) {
                    content = patchPo(content);
                } else if (entryName.equals(MOVING_PISTON_RENDERER_CLASS + ".class")) {
                    content = patchHy(content);
                } else if (entryName.equals(FONT_RENDERER_CLASS + ".class")) {
                    content = patchSj(content);
                } else if (entryName.equals(GUI_INGAME_CLASS + ".class")) {
                    content = patchUq(content);
                }

                if (entryName.endsWith(".class")) {
                    content = remapLegacyBindings(content);
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
            } else if (method.name.equals("a") && method.desc.equals("(IF)V")) {
                patchPxFogSetup(method);
            } else if (method.name.equals("c") && method.desc.equals("(F)V")) {
                patchPxWeatherRender(method);
            }
        }
        return writeClass(classNode);
    }

    private static byte[] patchDk(byte[] content) {
        ClassNode classNode = readClass(content);
        boolean patchedChunkBuild = false;
        boolean patchedChunkDisplayListLookup = false;
        boolean patchedChunkUnload = false;
        for (MethodNode method : classNode.methods) {
            if (method.name.equals("a") && method.desc.equals("()V")) {
                patchDkChunkBuild(method);
                patchedChunkBuild = true;
            } else if (method.name.equals("a") && method.desc.equals("(I)I")) {
                patchDkChunkDisplayListLookup(method);
                patchedChunkDisplayListLookup = true;
            } else if (method.name.equals("c") && method.desc.equals("()V")) {
                patchDkChunkUnload(method);
                patchedChunkUnload = true;
            }
        }
        if (!patchedChunkDisplayListLookup) {
            throw new IllegalStateException(
                "ClientPatchTool: could not find dk.a(I)I - display-list suppression hook not injected");
        }
        if (!patchedChunkUnload) {
            throw new IllegalStateException(
                "ClientPatchTool: could not find dk.c()V — chunk unload hook not injected");
        }
        return writeClass(classNode);
    }

    private static void patchDkChunkDisplayListLookup(MethodNode method) {
        if (hasHelperCall(method, "shouldSuppressWorldRasterDisplayLists", "()Z")) {
            return;
        }

        LabelNode skip = new LabelNode();
        InsnList hook = new InsnList();
        hook.add(new MethodInsnNode(
            Opcodes.INVOKESTATIC,
            REMIX_HELPER_CLASS,
            "shouldSuppressWorldRasterDisplayLists",
            "()Z",
            false));
        hook.add(new JumpInsnNode(Opcodes.IFEQ, skip));
        hook.add(new InsnNode(Opcodes.ICONST_M1));
        hook.add(new InsnNode(Opcodes.IRETURN));
        hook.add(skip);
        method.instructions.insertBefore(method.instructions.getFirst(), hook);
    }

    private static void patchDkChunkUnload(MethodNode method) {
        if (hasHelperCall(method, "onChunkSectionUnload", "(III)V")) {
            return;
        }

        InsnList hook = new InsnList();
        hook.add(new VarInsnNode(Opcodes.ALOAD, 0));
        hook.add(new FieldInsnNode(Opcodes.GETFIELD, CHUNK_RENDERER_CLASS, "c", "I")); // originX
        hook.add(new VarInsnNode(Opcodes.ALOAD, 0));
        hook.add(new FieldInsnNode(Opcodes.GETFIELD, CHUNK_RENDERER_CLASS, "d", "I")); // originY
        hook.add(new VarInsnNode(Opcodes.ALOAD, 0));
        hook.add(new FieldInsnNode(Opcodes.GETFIELD, CHUNK_RENDERER_CLASS, "e", "I")); // originZ
        hook.add(new MethodInsnNode(
            Opcodes.INVOKESTATIC,
            REMIX_HELPER_CLASS,
            "onChunkSectionUnload",
            "(III)V",
            false));
        method.instructions.insertBefore(method.instructions.getFirst(), hook);
    }

    private static byte[] patchN(byte[] content) {
        ClassNode classNode = readClass(content);
        for (MethodNode method : classNode.methods) {
            if (method.name.equals("a") && method.desc.equals("(Lfd;)V")) {
                if (!hasHelperCall(method, "onWorldChanged", "(Lfd;)V")) {
                    method.instructions.insertBefore(method.instructions.getFirst(), worldChangedCall());
                }
            } else if (method.name.equals("a") && method.desc.equals("()V")) {
                if (!hasHelperCall(method, "clearWorldScene", "()V")) {
                    method.instructions.insertBefore(method.instructions.getFirst(), clearWorldSceneCall());
                }
            } else if (method.name.equals("a") && method.desc.equals("(Lls;ID)I")) {
                patchWorldRasterRender(method);
            } else if (method.name.equals("a") && method.desc.equals("(Lbt;Lyn;F)V")) {
                patchWorldEntityVisibility(method);
            } else if (method.name.equals("b") && method.desc.equals("(F)V")) {
                patchCloudRender(method);
            } else if (method.name.equals("a") && method.desc.equals("(Lgs;Lvf;ILiz;F)V")) {
                patchDestroyOverlayRender(method);
            } else if (method.name.equals("b") && method.desc.equals("(Lgs;Lvf;ILiz;F)V")) {
                patchBlockOutlineRender(method);
            }
        }
        return writeClass(classNode);
    }

    private static byte[] patchCo(byte[] content) {
        ClassNode classNode = readClass(content);
        for (MethodNode method : classNode.methods) {
            if (method.name.equals("b") && method.desc.equals("()V")) {
                patchCoBuild(method);
            } else if (method.name.equals("a") && method.desc.equals("(Lke;)V")) {
                patchCoAction(method);
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
                if (!hasHelperCall(method, "onEntityTextureBind", "(Ljava/lang/String;Ljava/lang/String;)V")) {
                    method.instructions.insertBefore(method.instructions.getFirst(), entityTextureBindCallSingle());
                }
            } else if (method.name.equals("a") && method.desc.equals("(Ljava/lang/String;Ljava/lang/String;)Z")) {
                if (!hasHelperCall(method, "onEntityTextureBind", "(Ljava/lang/String;Ljava/lang/String;)V")) {
                    method.instructions.insertBefore(method.instructions.getFirst(), entityTextureBindCallFallback());
                }
            } else if (method.name.equals("b") && method.desc.equals("(Lsn;DDDFF)V")) {
                patchEntityFireOverlay(method);
            }
        }
        return writeClass(classNode);
    }

    private static byte[] patchBb(byte[] content) {
        ClassNode classNode = readClass(content);
        for (MethodNode method : classNode.methods) {
            if (method.name.equals("a") && method.desc.equals("(Lhl;DDDFF)V")) {
                patchItemEntityRender(method);
            }
        }
        return writeClass(classNode);
    }

    private static byte[] patchDs(byte[] content) {
        ClassNode classNode = readClass(content);
        for (MethodNode method : classNode.methods) {
            if (method.name.equals("a") && method.desc.equals("(Lgs;F)V")) {
                patchPlayerEquippedItemRender(method);
            }
        }
        return writeClass(classNode);
    }

    private static byte[] patchV(byte[] content) {
        ClassNode classNode = readClass(content);
        for (MethodNode method : classNode.methods) {
            if (method.name.equals("b") && method.desc.equals("(Lls;F)V")) {
                patchLivingEquippedItemRender(method);
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

    private static byte[] patchUq(byte[] content) {
        ClassNode classNode = readClass(content);
        for (MethodNode method : classNode.methods) {
            if (method.name.equals("a") && method.desc.equals("(FZII)V")) {
                patchGuiIngameOverlay(method);
            }
        }
        return writeClass(classNode);
    }

    private static byte[] patchParticle(byte[] content, String helperMethodName) {
        ClassNode classNode = readClass(content);
        for (MethodNode method : classNode.methods) {
            if (method.name.equals("a") && method.desc.equals("(Lnw;FFFFFF)V")) {
                if (!hasHelperCall(method, helperMethodName, "(Lxw;FFFFFF)Z")) {
                    LabelNode continueLabel = new LabelNode();
                    method.instructions.insertBefore(method.instructions.getFirst(), particleRenderReplacementCall(helperMethodName, continueLabel));
                }
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

    private static byte[] patchDy(byte[] content) {
        ClassNode classNode = readClass(content);
        for (MethodNode method : classNode.methods) {
            if (method.name.equals("a") && method.desc.equals("(Lqv;DDDFF)V")) {
                patchPaintingRender(method);
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

    private static byte[] patchHy(byte[] content) {
        ClassNode classNode = readClass(content);
        for (MethodNode method : classNode.methods) {
            if (method.name.equals("a") && method.desc.equals("(Luk;DDDF)V")) {
                patchMovingPistonRender(method);
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

    private static byte[] patchEm(byte[] content) {
        ClassNode classNode = readClass(content);
        for (MethodNode method : classNode.methods) {
            if (method.name.equals("a") && method.desc.equals("(Lnw;FFFFFF)V")) {
                patchPickupParticleRender(method);
            }
        }
        return writeClass(classNode);
    }

    private static void patchMinecraftStartup(MethodNode method) {
        if (hasHelperCall(method, "onDisplayCreated", "(II)V")
                && hasMethodCall(method, RENDER_HOOKS_CLASS, "rememberMinecraftInstance", "(Lnet/minecraft/client/Minecraft;)V")) {
            return;
        }

        for (AbstractInsnNode node = method.instructions.getFirst(); node != null; node = node.getNext()) {
            if (isStaticCall(node, DISPLAY_CLASS, "create", "()V")) {
                InsnList startupInstructions = new InsnList();
                startupInstructions.add(onDisplayCreatedCall());
                startupInstructions.add(rememberMinecraftInstanceCall());
                method.instructions.insert(node, startupInstructions);
            }
        }
    }

    private static void patchFirstPersonRender(MethodNode method) {
        if (hasHelperCall(method, "onFirstPersonRenderStart", "()V")) {
            return;
        }

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
        if (hasHelperCall(method, "onFirstPersonItemRender", "(Liz;)V")) {
            return;
        }

        method.instructions.insertBefore(method.instructions.getFirst(), firstPersonItemRenderCall());
    }

    private static void patchPlayerEquippedItemRender(MethodNode method) {
        if (hasHelperCall(method, "onPlayerEquippedItemRenderStart", "(Lgs;Liz;F)V")) {
            return;
        }

        int renderItemCallCount = 0;
        for (AbstractInsnNode node = method.instructions.getFirst(); node != null; node = node.getNext()) {
            if (node instanceof MethodInsnNode methodInsnNode
                    && methodInsnNode.getOpcode() == Opcodes.INVOKEVIRTUAL
                    && methodInsnNode.owner.equals(FIRST_PERSON_RENDERER_CLASS)
                    && methodInsnNode.name.equals("a")
                    && methodInsnNode.desc.equals("(Lls;Liz;)V")) {
                renderItemCallCount += 1;
                if (renderItemCallCount == 2) {
                    method.instructions.insertBefore(node, playerEquippedItemRenderCall());
                    return;
                }
            }
        }
    }

    private static void patchLivingEquippedItemRender(MethodNode method) {
        if (hasHelperCall(method, "onLivingEquippedItemRenderStart", "(Lls;Liz;)V")) {
            return;
        }

        for (AbstractInsnNode node = method.instructions.getFirst(); node != null; node = node.getNext()) {
            if (node instanceof MethodInsnNode methodInsnNode
                    && methodInsnNode.getOpcode() == Opcodes.INVOKEVIRTUAL
                    && methodInsnNode.owner.equals(FIRST_PERSON_RENDERER_CLASS)
                    && methodInsnNode.name.equals("a")
                    && methodInsnNode.desc.equals("(Lls;Liz;)V")) {
                method.instructions.insertBefore(node, livingEquippedItemRenderCall());
                return;
            }
        }
    }

    private static void patchTessellatorDraw(MethodNode method) {
        if (hasHelperCall(method, "onFirstPersonTessellatorDraw", "([IIIZZ)V")) {
            return;
        }

        // nw.a() emits glDrawArrays from two branches (quad-to-triangle and the
        // direct-mode path). InsnList.set() clears the replaced node's next pointer, so
        // the successor must be cached before the replacement or the second branch would
        // be left uncaptured.
        AbstractInsnNode node = method.instructions.getFirst();
        while (node != null) {
            AbstractInsnNode next = node.getNext();
            if (isStaticCall(node, GL11_CLASS, "glDrawArrays", "(III)V")) {
                method.instructions.insertBefore(node, firstPersonTessellatorDrawCall());
                method.instructions.set(
                        node,
                        new MethodInsnNode(
                                Opcodes.INVOKESTATIC,
                                REMIX_HELPER_CLASS,
                                "drawTessellator",
                                "(III)V",
                                false));
            }
            node = next;
        }
    }

    private static void patchMinecraftShutdown(MethodNode method) {
        if (hasHelperCall(method, "onShutdown", "()V")) {
            return;
        }

        for (AbstractInsnNode node = method.instructions.getFirst(); node != null; node = node.getNext()) {
            if (isStaticCall(node, MOUSE_CLASS, "destroy", "()V")) {
                method.instructions.insertBefore(node, staticHelperCall("onShutdown", "()V"));
                return;
            }
        }
    }

    private static void patchMinecraftDisplayReset(MethodNode method) {
        if (hasHelperCall(method, "onDisplayReset", "(II)V")) {
            return;
        }

        for (AbstractInsnNode node = method.instructions.getFirst(); node != null; node = node.getNext()) {
            if (isStaticCall(node, DISPLAY_CLASS, "update", "()V")) {
                method.instructions.insert(node, onDisplayResetCall());
                return;
            }
        }
    }

    private static void patchMinecraftResize(MethodNode method) {
        if (hasHelperCall(method, "onResize", "(II)V")) {
            return;
        }

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
        if (hasHelperCall(method, "onFrameViewCaptured", "()V")) {
            return;
        }

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
        if (hasHelperCall(method, "onFrameRenderStart", "()V")) {
            return;
        }

        method.instructions.insertBefore(method.instructions.getFirst(), staticHelperCall("onFrameRenderStart", "()V"));
        method.instructions.insert(cameraUpdateCall());

        for (AbstractInsnNode node = method.instructions.getFirst(); node != null; node = node.getNext()) {
            if (node instanceof MethodInsnNode methodInsnNode
                    && methodInsnNode.getOpcode() == Opcodes.INVOKEVIRTUAL
                    && methodInsnNode.owner.equals(GUI_INGAME_CLASS)
                    && methodInsnNode.name.equals("a")
                    && methodInsnNode.desc.equals("(FZII)V")) {
                method.instructions.insertBefore(node, uiRenderBeginCall());
                break;
            }
        }

        for (AbstractInsnNode node = method.instructions.getFirst(); node != null; node = node.getNext()) {
            if (node instanceof MethodInsnNode methodInsnNode
                    && methodInsnNode.getOpcode() == Opcodes.INVOKEVIRTUAL
                    && methodInsnNode.owner.equals(GUI_SCREEN_CLASS)
                    && methodInsnNode.name.equals("a")
                    && methodInsnNode.desc.equals("(IIF)V")) {
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

    private static void patchPxWeatherRender(MethodNode method) {
        if (hasHelperCall(method, "onWeatherTextureBind", "(Ljava/lang/String;)V")) {
            return;
        }

        boolean insertedWeatherBind = false;
        for (AbstractInsnNode node = method.instructions.getFirst(); node != null; node = node.getNext()) {
            if (node instanceof LdcInsnNode ldcInsnNode && "/environment/rain.png".equals(ldcInsnNode.cst)) {
                AbstractInsnNode insertionPoint = node;
                while (insertionPoint != null && !isStaticCall(insertionPoint, GL11_CLASS, "glBindTexture", "(II)V")) {
                    insertionPoint = insertionPoint.getNext();
                }
                if (insertionPoint != null) {
                    method.instructions.insert(insertionPoint, weatherTextureBindCall("/environment/rain.png"));
                    insertedWeatherBind = true;
                }
                break;
            }
        }

        if (!insertedWeatherBind) {
            return;
        }

        for (AbstractInsnNode node = method.instructions.getFirst(); node != null; ) {
            AbstractInsnNode next = node.getNext();
            if (node.getOpcode() == Opcodes.RETURN) {
                method.instructions.insertBefore(node, staticHelperCall("onWeatherRenderEnd", "()V"));
            }
            node = next;
        }
    }

    private static void patchPxFogSetup(MethodNode method) {
        if (hasHelperCall(method, "onFogState", "(Lls;ZIZFFFF)V")) {
            return;
        }

        for (AbstractInsnNode node = method.instructions.getFirst(); node != null; ) {
            AbstractInsnNode next = node.getNext();
            if (node.getOpcode() == Opcodes.RETURN) {
                method.instructions.insertBefore(node, fogStateCaptureCall());
            }
            node = next;
        }
    }

    private static void patchDkChunkBuild(MethodNode method) {
        if (hasHelperCall(method, "onChunkBuildBegin", "(IIIIIII)Z")) {
            return;
        }

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

    private static void patchCloudRender(MethodNode method) {
        if (hasHelperCall(method, "onCloudRender", "(Lnet/minecraft/client/Minecraft;Lfd;IFZ)V")) {
            return;
        }

        method.instructions.insertBefore(method.instructions.getFirst(), cloudRenderCall());
    }

    private static void patchWorldRasterRender(MethodNode method) {
        if (hasHelperCall(method, "onWorldRasterRenderStart", "()V")) {
            return;
        }

        method.instructions.insertBefore(method.instructions.getFirst(), staticHelperCall("onWorldRasterRenderStart", "()V"));

        for (AbstractInsnNode node = method.instructions.getFirst(); node != null; ) {
            AbstractInsnNode next = node.getNext();
            if (node.getOpcode() == Opcodes.IRETURN) {
                method.instructions.insertBefore(node, staticHelperCall("onWorldRasterRenderEnd", "()V"));
            }
            node = next;
        }
    }

    private static void patchWorldEntityVisibility(MethodNode method) {
        for (AbstractInsnNode node = method.instructions.getFirst(); node != null; node = node.getNext()) {
            if (node instanceof MethodInsnNode methodInsnNode
                    && methodInsnNode.getOpcode() == Opcodes.INVOKEINTERFACE
                    && methodInsnNode.owner.equals("yn")
                    && methodInsnNode.name.equals("a")
                    && methodInsnNode.desc.equals("(Leq;)Z")) {
                method.instructions.set(
                        node,
                        new MethodInsnNode(
                                Opcodes.INVOKESTATIC,
                                REMIX_HELPER_CLASS,
                                "shouldRenderBoundingBox",
                                "(Lyn;Leq;)Z",
                                false));
                return;
            }
        }
    }

    private static void patchLivingEntityFrameBegin(MethodNode method) {
        if (hasHelperCall(method, "onLivingEntityFrameBegin", "()V")) {
            return;
        }

        method.instructions.insertBefore(method.instructions.getFirst(), staticHelperCall("onLivingEntityFrameBegin", "()V"));
    }

    private static void patchLivingEntityRender(MethodNode method) {
        if (hasHelperCall(method, "onLivingEntityRenderStart", "(Lsn;F)V")) {
            return;
        }

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
        if (hasHelperCall(method, "onModelPartRender", "([Ltz;F)V")) {
            return;
        }

        // ps.a(F) contains three glCallList sites (rotation, translation, and bare
        // branches). InsnList.set() clears the replaced node's next pointer, so the
        // successor must be cached before the replacement or the loop would stop after
        // the first site and leave the no-transform branches uncaptured.
        AbstractInsnNode node = method.instructions.getFirst();
        while (node != null) {
            AbstractInsnNode next = node.getNext();
            if (isStaticCall(node, GL11_CLASS, "glCallList", "(I)V")) {
                method.instructions.insertBefore(node, modelPartRenderCall());
                method.instructions.set(
                        node,
                        new MethodInsnNode(
                                Opcodes.INVOKESTATIC,
                                REMIX_HELPER_CLASS,
                                "drawModelPartCallList",
                                "(I)V",
                                false));
            }
            node = next;
        }
    }

    private static void patchGuiIngameOverlay(MethodNode method) {
        for (AbstractInsnNode node = method.instructions.getFirst(); node != null; node = node.getNext()) {
            if (node instanceof MethodInsnNode methodInsnNode
                    && methodInsnNode.getOpcode() == Opcodes.INVOKESTATIC
                    && methodInsnNode.owner.equals(MINECRAFT_CLASS)
                    && methodInsnNode.name.equals("u")
                    && methodInsnNode.desc.equals("()Z")) {
                method.instructions.set(node, new InsnNode(Opcodes.ICONST_0));
                return;
            }
        }
    }

    private static byte[] remapLegacyBindings(byte[] content) {
        ClassNode classNode = readClass(content);
        boolean modified = false;
        for (MethodNode method : classNode.methods) {
            for (AbstractInsnNode node = method.instructions.getFirst(); node != null; node = node.getNext()) {
                if (node instanceof MethodInsnNode methodInsnNode) {
                    if (GL11_CLASS.equals(methodInsnNode.owner)) {
                        methodInsnNode.owner = LEGACY_GL11_CLASS;
                        modified = true;
                    } else if (AL10_CLASS.equals(methodInsnNode.owner)) {
                        methodInsnNode.owner = LEGACY_AL10_CLASS;
                        modified = true;
                    } else if (ARB_OCCLUSION_QUERY_CLASS.equals(methodInsnNode.owner)) {
                        methodInsnNode.owner = LEGACY_ARB_OCCLUSION_QUERY_CLASS;
                        modified = true;
                    }
                }
            }
        }
        return modified ? writeClass(classNode) : content;
    }

    private static void patchDestroyOverlayRender(MethodNode method) {
        if (hasHelperCall(method, "onDestroyOverlayRender", "(IIIF)V")) {
            return;
        }

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

    private static void patchBlockOutlineRender(MethodNode method) {
        removeBlockOutlineRenderCalls(method);
        method.instructions.insertBefore(method.instructions.getFirst(), blockOutlineRenderCall());
    }

    private static void removeBlockOutlineRenderCalls(MethodNode method) {
        for (AbstractInsnNode node = method.instructions.getFirst(); node != null; ) {
            AbstractInsnNode next = node.getNext();
            if (node instanceof MethodInsnNode methodInsnNode
                    && methodInsnNode.getOpcode() == Opcodes.INVOKESTATIC
                    && methodInsnNode.owner.equals(REMIX_HELPER_CLASS)
                    && methodInsnNode.name.equals("onBlockOutlineRender")
                    && (methodInsnNode.desc.equals("(Lvf;I)V")
                        || methodInsnNode.desc.equals("(Lgs;Lvf;IF)V"))) {
                int argumentLoadCount = methodInsnNode.desc.equals("(Lvf;I)V") ? 2 : 4;
                AbstractInsnNode previous = node.getPrevious();
                for (int removed = 0; removed < argumentLoadCount && previous instanceof VarInsnNode; removed += 1) {
                    AbstractInsnNode loadNode = previous;
                    previous = previous.getPrevious();
                    method.instructions.remove(loadNode);
                }
                method.instructions.remove(node);
            }
            node = next;
        }
    }

    private static void patchMinecartRender(MethodNode method) {
        if (hasHelperCall(method, "onLivingEntityRenderStart", "(Lsn;F)V")) {
            return;
        }

        method.instructions.insertBefore(method.instructions.getFirst(), livingEntityRenderStartCall());

        for (AbstractInsnNode node = method.instructions.getFirst(); node != null; ) {
            AbstractInsnNode next = node.getNext();
            if (node.getOpcode() == Opcodes.RETURN) {
                method.instructions.insertBefore(node, staticHelperCall("onLivingEntityRenderEnd", "()V"));
            }
            node = next;
        }
    }

    private static void patchItemEntityRender(MethodNode method) {
        if (hasHelperCall(method, "onItemEntityRenderStart", "(Lsn;)V")) {
            return;
        }

        method.instructions.insertBefore(method.instructions.getFirst(), itemEntityRenderStartCall());
        for (AbstractInsnNode node = method.instructions.getFirst(); node != null; ) {
            AbstractInsnNode next = node.getNext();
            if (node.getOpcode() == Opcodes.RETURN) {
                method.instructions.insertBefore(node, staticHelperCall("onItemEntityRenderEnd", "()V"));
            }
            node = next;
        }
    }

    private static void patchEntityFireOverlay(MethodNode method) {
        if (hasHelperCall(method, "onEntityFireOverlayStart", "(Lsn;)V")) {
            return;
        }

        for (AbstractInsnNode node = method.instructions.getFirst(); node != null; node = node.getNext()) {
            if (node instanceof MethodInsnNode methodInsnNode
                    && methodInsnNode.getOpcode() == Opcodes.INVOKESPECIAL
                    && methodInsnNode.owner.equals(BASE_RENDERER_CLASS)
                    && methodInsnNode.name.equals("a")
                    && methodInsnNode.desc.equals("(Lsn;DDDF)V")) {
                method.instructions.insertBefore(node, entityFireOverlayStartCall());
                method.instructions.insert(node, staticHelperCall("onEntityFireOverlayEnd", "()V"));
                return;
            }
        }
    }

    private static void patchPickupParticleRender(MethodNode method) {
        if (hasHelperCall(method, "onPickupParticleEntityRenderStart", "(Lsn;)V")) {
            return;
        }

        for (AbstractInsnNode node = method.instructions.getFirst(); node != null; node = node.getNext()) {
            if (node instanceof MethodInsnNode methodInsnNode
                    && methodInsnNode.getOpcode() == Opcodes.INVOKEVIRTUAL
                    && methodInsnNode.owner.equals(LIVING_RENDER_MANAGER_CLASS)
                    && methodInsnNode.name.equals("a")
                    && methodInsnNode.desc.equals("(Lsn;DDDFF)V")) {
                method.instructions.insertBefore(node, pickupParticleEntityRenderStartCall());
                method.instructions.insert(node, staticHelperCall("onPickupParticleEntityRenderEnd", "()V"));
                return;
            }
        }
    }

    private static void patchPaintingRender(MethodNode method) {
        if (hasHelperCall(method, "tryReplacePaintingRender", "(Lqv;)Z")
                || hasHelperCall(method, "onPaintingRender", "(Lqv;)V")) {
            return;
        }

        MethodInsnNode scaleCall = null;
        MethodInsnNode drawCall = null;
        for (AbstractInsnNode node = method.instructions.getFirst(); node != null; node = node.getNext()) {
            if (node instanceof MethodInsnNode methodInsnNode) {
                if (methodInsnNode.owner.equals(GL11_CLASS)
                        && methodInsnNode.name.equals("glScalef")
                        && methodInsnNode.desc.equals("(FFF)V")) {
                    scaleCall = methodInsnNode;
                } else if (methodInsnNode.owner.equals(PAINTING_RENDERER_CLASS)
                        && methodInsnNode.name.equals("a")
                        && methodInsnNode.desc.equals("(Lqv;IIII)V")) {
                    drawCall = methodInsnNode;
                    break;
                }
            }
        }

        if (scaleCall == null || drawCall == null) {
            return;
        }

        AbstractInsnNode cleanupStart = drawCall.getNext();
        if (cleanupStart == null) {
            return;
        }

        LabelNode cleanupLabel = new LabelNode();
        method.instructions.insert(scaleCall, paintingRenderReplacementCall(cleanupLabel));
        method.instructions.insertBefore(cleanupStart, cleanupLabel);
    }

    private static void patchSignRender(MethodNode method) {
        if (hasHelperCall(method, "onSignRenderStart", "(Lyk;)V")) {
            return;
        }

        MethodInsnNode signModelRenderCall = null;
        AbstractInsnNode signModelRenderStart = null;
        for (AbstractInsnNode node = method.instructions.getFirst(); node != null; node = node.getNext()) {
            if (node instanceof MethodInsnNode methodInsnNode
                    && methodInsnNode.getOpcode() == Opcodes.INVOKEVIRTUAL
                    && methodInsnNode.owner.equals("rf")
                    && methodInsnNode.name.equals("a")
                    && methodInsnNode.desc.equals("()V")) {
                signModelRenderCall = methodInsnNode;
                AbstractInsnNode getFieldNode = node.getPrevious();
                if (getFieldNode instanceof FieldInsnNode fieldInsnNode
                        && fieldInsnNode.getOpcode() == Opcodes.GETFIELD
                        && fieldInsnNode.owner.equals(SIGN_RENDERER_CLASS)
                        && fieldInsnNode.name.equals("b")
                        && fieldInsnNode.desc.equals("Lrf;")) {
                    AbstractInsnNode receiverLoad = getFieldNode.getPrevious();
                    if (receiverLoad instanceof VarInsnNode varInsnNode
                            && varInsnNode.getOpcode() == Opcodes.ALOAD
                            && varInsnNode.var == 0) {
                        signModelRenderStart = receiverLoad;
                    }
                }
                break;
            }
        }

        if (signModelRenderCall == null || signModelRenderStart == null) {
            return;
        }

        method.instructions.insertBefore(signModelRenderStart, signRenderStartCall());

        AbstractInsnNode cleanupStart = signModelRenderCall.getNext();
        if (cleanupStart == null) {
            return;
        }

        LabelNode cleanupLabel = new LabelNode();
        method.instructions.insertBefore(signModelRenderStart, signModelReplacementCall(cleanupLabel));
        method.instructions.insertBefore(cleanupStart, cleanupLabel);

        for (AbstractInsnNode node = method.instructions.getFirst(); node != null; ) {
            AbstractInsnNode next = node.getNext();
            if (node instanceof MethodInsnNode methodInsnNode
                    && methodInsnNode.getOpcode() == Opcodes.INVOKEVIRTUAL
                    && methodInsnNode.owner.equals(FONT_RENDERER_CLASS)
                    && methodInsnNode.name.equals("b")
                    && methodInsnNode.desc.equals("(Ljava/lang/String;III)V")) {
                method.instructions.set(
                        node,
                        new MethodInsnNode(
                                Opcodes.INVOKESTATIC,
                                REMIX_HELPER_CLASS,
                                "renderSignText",
                                "(Lsj;Ljava/lang/String;III)V",
                                false));
            }
            node = next;
        }

        for (AbstractInsnNode node = method.instructions.getFirst(); node != null; ) {
            AbstractInsnNode next = node.getNext();
            if (node.getOpcode() == Opcodes.RETURN) {
                method.instructions.insertBefore(node, staticHelperCall("onSignRenderEnd", "()V"));
            }
            node = next;
        }
    }

    private static void patchMovingPistonRender(MethodNode method) {
        if (hasHelperCall(method, "onMovingPistonRenderStart", "(Luk;)V")) {
            return;
        }

        boolean insertedStart = false;
        for (AbstractInsnNode node = method.instructions.getFirst(); node != null; node = node.getNext()) {
            if (node instanceof MethodInsnNode methodInsnNode
                    && methodInsnNode.getOpcode() == Opcodes.INVOKEVIRTUAL
                    && methodInsnNode.owner.equals(TESSELLATOR_CLASS)
                    && methodInsnNode.name.equals("b")
                    && methodInsnNode.desc.equals("()V")) {
                method.instructions.insertBefore(node, movingPistonRenderStartCall());
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
                method.instructions.insertBefore(node, staticHelperCall("onMovingPistonRenderEnd", "()V"));
            }
            node = next;
        }
    }

    private static void patchFontRender(MethodNode method) {
        if (hasHelperCall(method, "captureFontStringAndMaybeSuppress", "(Ljava/lang/String;IIIZ[II)Z")) {
            return;
        }

        LabelNode continueLabel = new LabelNode();
        method.instructions.insertBefore(method.instructions.getFirst(), fontRenderReplacementCall(continueLabel));
    }

    private static void patchCoBuild(MethodNode method) {
        if (hasHelperCall(method, "configureMcrtxOptionsScreen", "(Lco;)V")) {
            return;
        }

        for (AbstractInsnNode node = method.instructions.getFirst(); node != null; ) {
            AbstractInsnNode next = node.getNext();
            if (node.getOpcode() == Opcodes.RETURN) {
                method.instructions.insertBefore(node, configureMcrtxOptionsScreenCall());
            }
            node = next;
        }
    }

    private static void patchCoAction(MethodNode method) {
        if (hasHelperCall(method, "handleMcrtxOptionsButton", "(Lco;Lke;)Z")) {
            return;
        }

        LabelNode continueLabel = new LabelNode();
        InsnList instructions = new InsnList();
        instructions.add(new VarInsnNode(Opcodes.ALOAD, 0));
        instructions.add(new VarInsnNode(Opcodes.ALOAD, 1));
        instructions.add(new MethodInsnNode(
                Opcodes.INVOKESTATIC,
                REMIX_HELPER_CLASS,
                "handleMcrtxOptionsButton",
                "(Lco;Lke;)Z",
                false));
        instructions.add(new JumpInsnNode(Opcodes.IFEQ, continueLabel));
        instructions.add(new InsnNode(Opcodes.RETURN));
        instructions.add(continueLabel);
        method.instructions.insertBefore(method.instructions.getFirst(), instructions);
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

    private static boolean hasMethodCall(MethodNode method, String owner, String name, String desc) {
        for (AbstractInsnNode node = method.instructions.getFirst(); node != null; node = node.getNext()) {
            if (node instanceof MethodInsnNode methodInsnNode
                    && methodInsnNode.owner.equals(owner)
                    && methodInsnNode.name.equals(name)
                    && methodInsnNode.desc.equals(desc)) {
                return true;
            }
        }
        return false;
    }

    private static boolean hasHelperCall(MethodNode method, String name, String desc) {
        return hasMethodCall(method, REMIX_HELPER_CLASS, name, desc);
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

    private static InsnList rememberMinecraftInstanceCall() {
        InsnList instructions = new InsnList();
        instructions.add(new VarInsnNode(Opcodes.ALOAD, 0));
        instructions.add(new MethodInsnNode(
                Opcodes.INVOKESTATIC,
                RENDER_HOOKS_CLASS,
                "rememberMinecraftInstance",
                "(Lnet/minecraft/client/Minecraft;)V",
                false));
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
        instructions.add(new VarInsnNode(Opcodes.ALOAD, 0));
        instructions.add(new FieldInsnNode(Opcodes.GETFIELD, "px", "j", "Lnet/minecraft/client/Minecraft;"));
        instructions.add(new FieldInsnNode(Opcodes.GETFIELD, MINECRAFT_CLASS, "z", "Lkv;"));
        instructions.add(new FieldInsnNode(Opcodes.GETFIELD, "kv", "A", "Z"));
        instructions.add(new MethodInsnNode(Opcodes.INVOKESTATIC, REMIX_HELPER_CLASS, "onCamera", "(Lls;FIIFZ)V", false));
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

    private static InsnList fogStateCaptureCall() {
        InsnList instructions = new InsnList();
        instructions.add(new VarInsnNode(Opcodes.ALOAD, 0));
        instructions.add(new FieldInsnNode(Opcodes.GETFIELD, "px", "j", "Lnet/minecraft/client/Minecraft;"));
        instructions.add(new FieldInsnNode(Opcodes.GETFIELD, MINECRAFT_CLASS, "i", "Lls;"));
        instructions.add(new VarInsnNode(Opcodes.ALOAD, 0));
        instructions.add(new FieldInsnNode(Opcodes.GETFIELD, "px", "D", "Z"));
        instructions.add(new VarInsnNode(Opcodes.ILOAD, 1));
        instructions.add(new VarInsnNode(Opcodes.ALOAD, 0));
        instructions.add(new FieldInsnNode(Opcodes.GETFIELD, "px", "j", "Lnet/minecraft/client/Minecraft;"));
        instructions.add(new FieldInsnNode(Opcodes.GETFIELD, MINECRAFT_CLASS, "f", "Lfd;"));
        instructions.add(new FieldInsnNode(Opcodes.GETFIELD, "fd", "t", "Lxa;"));
        instructions.add(new FieldInsnNode(Opcodes.GETFIELD, "xa", "c", "Z"));
        instructions.add(new VarInsnNode(Opcodes.ALOAD, 0));
        instructions.add(new FieldInsnNode(Opcodes.GETFIELD, "px", "k", "F"));
        instructions.add(new VarInsnNode(Opcodes.ALOAD, 0));
        instructions.add(new FieldInsnNode(Opcodes.GETFIELD, "px", "g", "F"));
        instructions.add(new VarInsnNode(Opcodes.ALOAD, 0));
        instructions.add(new FieldInsnNode(Opcodes.GETFIELD, "px", "h", "F"));
        instructions.add(new VarInsnNode(Opcodes.ALOAD, 0));
        instructions.add(new FieldInsnNode(Opcodes.GETFIELD, "px", "i", "F"));
        instructions.add(new MethodInsnNode(
                Opcodes.INVOKESTATIC,
                REMIX_HELPER_CLASS,
                "onFogState",
                "(Lls;ZIZFFFF)V",
                false));
        return instructions;
    }

    private static InsnList staticHelperCall(String name, String descriptor) {
        InsnList instructions = new InsnList();
        instructions.add(new MethodInsnNode(Opcodes.INVOKESTATIC, REMIX_HELPER_CLASS, name, descriptor, false));
        return instructions;
    }

    private static InsnList weatherTextureBindCall(String texturePath) {
        InsnList instructions = new InsnList();
        instructions.add(new LdcInsnNode(texturePath));
        instructions.add(new MethodInsnNode(
                Opcodes.INVOKESTATIC,
                REMIX_HELPER_CLASS,
                "onWeatherTextureBind",
                "(Ljava/lang/String;)V",
                false));
        return instructions;
    }

    private static InsnList livingEntityRenderStartCall() {
        InsnList instructions = new InsnList();
        instructions.add(new VarInsnNode(Opcodes.ALOAD, 1));
        instructions.add(new VarInsnNode(Opcodes.FLOAD, 9));
        instructions.add(new MethodInsnNode(Opcodes.INVOKESTATIC, REMIX_HELPER_CLASS, "onLivingEntityRenderStart", "(Lsn;F)V", false));
        return instructions;
    }

    private static InsnList signRenderStartCall() {
        InsnList instructions = new InsnList();
        instructions.add(new VarInsnNode(Opcodes.ALOAD, 1));
        instructions.add(new MethodInsnNode(Opcodes.INVOKESTATIC, REMIX_HELPER_CLASS, "onSignRenderStart", "(Lyk;)V", false));
        return instructions;
    }

    private static InsnList paintingRenderReplacementCall(LabelNode cleanupLabel) {
        InsnList instructions = new InsnList();
        instructions.add(new VarInsnNode(Opcodes.ALOAD, 1));
        instructions.add(new MethodInsnNode(Opcodes.INVOKESTATIC, REMIX_HELPER_CLASS, "tryReplacePaintingRender", "(Lqv;)Z", false));
        instructions.add(new JumpInsnNode(Opcodes.IFNE, cleanupLabel));
        return instructions;
    }

    private static InsnList movingPistonRenderStartCall() {
        InsnList instructions = new InsnList();
        instructions.add(new VarInsnNode(Opcodes.ALOAD, 1));
        instructions.add(new MethodInsnNode(Opcodes.INVOKESTATIC, REMIX_HELPER_CLASS, "onMovingPistonRenderStart", "(Luk;)V", false));
        return instructions;
    }

    private static InsnList signModelReplacementCall(LabelNode cleanupLabel) {
        InsnList instructions = new InsnList();
        instructions.add(new VarInsnNode(Opcodes.ALOAD, 0));
        instructions.add(new FieldInsnNode(Opcodes.GETFIELD, SIGN_RENDERER_CLASS, "b", "Lrf;"));
        instructions.add(new MethodInsnNode(
                Opcodes.INVOKESTATIC,
                REMIX_HELPER_CLASS,
                "tryReplaceSignModelRender",
                "(Lrf;)Z",
                false));
        instructions.add(new JumpInsnNode(Opcodes.IFNE, cleanupLabel));
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
        instructions.add(new VarInsnNode(Opcodes.ALOAD, 0));
        instructions.add(new FieldInsnNode(Opcodes.GETFIELD, FONT_RENDERER_CLASS, "a", "I"));
        instructions.add(new MethodInsnNode(
                Opcodes.INVOKESTATIC,
                REMIX_HELPER_CLASS,
                "onSignTextRender",
                "(Ljava/lang/String;IIIZ[II)V",
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

    private static InsnList configureMcrtxOptionsScreenCall() {
        InsnList instructions = new InsnList();
        instructions.add(new VarInsnNode(Opcodes.ALOAD, 0));
        instructions.add(new MethodInsnNode(
                Opcodes.INVOKESTATIC,
                REMIX_HELPER_CLASS,
                "configureMcrtxOptionsScreen",
                "(Lco;)V",
                false));
        return instructions;
    }

    private static InsnList clearWorldSceneCall() {
        InsnList instructions = new InsnList();
        instructions.add(new MethodInsnNode(
                Opcodes.INVOKESTATIC,
                REMIX_HELPER_CLASS,
                "clearWorldScene",
                "()V",
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

    private static InsnList playerEquippedItemRenderCall() {
        InsnList instructions = new InsnList();
        instructions.add(new VarInsnNode(Opcodes.ALOAD, 1));
        instructions.add(new VarInsnNode(Opcodes.ALOAD, 4));
        instructions.add(new VarInsnNode(Opcodes.FLOAD, 2));
        instructions.add(new MethodInsnNode(
                Opcodes.INVOKESTATIC,
                REMIX_HELPER_CLASS,
                "onPlayerEquippedItemRenderStart",
            "(Lgs;Liz;F)V",
                false));
        return instructions;
    }

    private static InsnList livingEquippedItemRenderCall() {
        InsnList instructions = new InsnList();
        instructions.add(new VarInsnNode(Opcodes.ALOAD, 1));
        instructions.add(new VarInsnNode(Opcodes.ALOAD, 3));
        instructions.add(new MethodInsnNode(
                Opcodes.INVOKESTATIC,
                REMIX_HELPER_CLASS,
                "onLivingEquippedItemRenderStart",
                "(Lls;Liz;)V",
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

    private static InsnList particleRenderReplacementCall(String helperMethodName, LabelNode continueLabel) {
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
            helperMethodName,
                "(Lxw;FFFFFF)Z",
                false));
        instructions.add(new JumpInsnNode(Opcodes.IFEQ, continueLabel));
        instructions.add(new InsnNode(Opcodes.RETURN));
        instructions.add(continueLabel);
        return instructions;
    }

    private static InsnList fontRenderReplacementCall(LabelNode continueLabel) {
        InsnList instructions = new InsnList();
        instructions.add(new VarInsnNode(Opcodes.ALOAD, 1));
        instructions.add(new VarInsnNode(Opcodes.ILOAD, 2));
        instructions.add(new VarInsnNode(Opcodes.ILOAD, 3));
        instructions.add(new VarInsnNode(Opcodes.ILOAD, 4));
        instructions.add(new VarInsnNode(Opcodes.ILOAD, 5));
        instructions.add(new VarInsnNode(Opcodes.ALOAD, 0));
        instructions.add(new FieldInsnNode(Opcodes.GETFIELD, FONT_RENDERER_CLASS, "b", "[I"));
        instructions.add(new VarInsnNode(Opcodes.ALOAD, 0));
        instructions.add(new FieldInsnNode(Opcodes.GETFIELD, FONT_RENDERER_CLASS, "a", "I"));
        instructions.add(new MethodInsnNode(
                Opcodes.INVOKESTATIC,
                REMIX_HELPER_CLASS,
                "captureFontStringAndMaybeSuppress",
                "(Ljava/lang/String;IIIZ[II)Z",
                false));
        instructions.add(new JumpInsnNode(Opcodes.IFEQ, continueLabel));
        instructions.add(new InsnNode(Opcodes.RETURN));
        instructions.add(continueLabel);
        return instructions;
    }

    private static InsnList pickupParticleEntityRenderStartCall() {
        InsnList instructions = new InsnList();
        instructions.add(new VarInsnNode(Opcodes.ALOAD, 0));
        instructions.add(new FieldInsnNode(Opcodes.GETFIELD, PICKUP_PARTICLE_CLASS, "a", "Lsn;"));
        instructions.add(new MethodInsnNode(
                Opcodes.INVOKESTATIC,
                REMIX_HELPER_CLASS,
                "onPickupParticleEntityRenderStart",
                "(Lsn;)V",
                false));
        return instructions;
    }

    private static InsnList itemEntityRenderStartCall() {
        InsnList instructions = new InsnList();
        instructions.add(new VarInsnNode(Opcodes.ALOAD, 1));
        instructions.add(new MethodInsnNode(
                Opcodes.INVOKESTATIC,
                REMIX_HELPER_CLASS,
                "onItemEntityRenderStart",
                "(Lsn;)V",
                false));
        return instructions;
    }

    private static InsnList entityFireOverlayStartCall() {
        InsnList instructions = new InsnList();
        instructions.add(new VarInsnNode(Opcodes.ALOAD, 1));
        instructions.add(new MethodInsnNode(
                Opcodes.INVOKESTATIC,
                REMIX_HELPER_CLASS,
                "onEntityFireOverlayStart",
                "(Lsn;)V",
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

    private static InsnList blockOutlineRenderCall() {
        InsnList instructions = new InsnList();
        instructions.add(new VarInsnNode(Opcodes.ALOAD, 1));
        instructions.add(new VarInsnNode(Opcodes.ALOAD, 2));
        instructions.add(new VarInsnNode(Opcodes.ILOAD, 3));
        instructions.add(new VarInsnNode(Opcodes.FLOAD, 5));
        instructions.add(new MethodInsnNode(
                Opcodes.INVOKESTATIC,
                REMIX_HELPER_CLASS,
                "onBlockOutlineRender",
            "(Lgs;Lvf;IF)V",
                false));
        return instructions;
    }

    private static InsnList cloudRenderCall() {
        InsnList instructions = new InsnList();
        instructions.add(new VarInsnNode(Opcodes.ALOAD, 0));
        instructions.add(new FieldInsnNode(Opcodes.GETFIELD, WORLD_RENDERER_CLASS, "t", "Lnet/minecraft/client/Minecraft;"));
        instructions.add(new VarInsnNode(Opcodes.ALOAD, 0));
        instructions.add(new FieldInsnNode(Opcodes.GETFIELD, WORLD_RENDERER_CLASS, "k", "Lfd;"));
        instructions.add(new VarInsnNode(Opcodes.ALOAD, 0));
        instructions.add(new FieldInsnNode(Opcodes.GETFIELD, WORLD_RENDERER_CLASS, "x", "I"));
        instructions.add(new VarInsnNode(Opcodes.FLOAD, 1));
        instructions.add(new VarInsnNode(Opcodes.ALOAD, 0));
        instructions.add(new FieldInsnNode(Opcodes.GETFIELD, WORLD_RENDERER_CLASS, "t", "Lnet/minecraft/client/Minecraft;"));
        instructions.add(new FieldInsnNode(Opcodes.GETFIELD, MINECRAFT_CLASS, "z", "Lkv;"));
        instructions.add(new FieldInsnNode(Opcodes.GETFIELD, "kv", "j", "Z"));
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