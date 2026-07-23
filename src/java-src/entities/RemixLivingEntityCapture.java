import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import mcrtx.bridge.ColorMath;
import mcrtx.bridge.HookProfiler;

final class RemixLivingEntityCapture {
    private static final int MAX_HURT_STAGE = 10;
    private static final CreeperFuseTracker CREEPER_FUSE_TRACKER = new CreeperFuseTracker();

    private static Class<?> livingEntityBaseClass;
    private static Class<?> creeperEntityClass;
    private static Field livingEntityHurtTimeField;
    private static Field creeperFuseTimeField;
    private static Field creeperPreviousFuseTimeField;
    private static Method creeperFuseStateMethod;
    private static volatile boolean enabled = true;

    private RemixLivingEntityCapture() {
    }

    static void onFrameBegin() {
        if (!canCapture()) {
            return;
        }
        long beginNanos = System.nanoTime();
        RemixDynamicEntitySession.ensureFrame();
        HookProfiler.record(HookProfiler.SIDE_HOOK, "hook.onLivingEntityFrameBegin.ensureFrame",
                System.nanoTime() - beginNanos);
    }

    static void onRenderStart(sn entity, float partialTicks) {
        if (!canCapture() || entity == null) {
            return;
        }
        int hurtStage = isTrackedLivingEntity(entity) ? resolveHurtStage(entity) : 0;
        float fuseProgress = isTrackedCreeper(entity) ? resolveFuseProgress(entity, partialTicks) : 0.0f;
        int fuseStage = ColorMath.creeperFuseStage(fuseProgress);
        RemixDynamicEntitySession.beginEntity(entity.aD, hurtStage, fuseStage, fuseProgress, "");
    }

    static void onRenderEnd() {
        if (!RemixDynamicEntitySession.isEntityActive()) {
            return;
        }
        RemixDynamicEntitySession.endEntity();
        RemixEntityFireCapture.resetActiveCapture();
    }

    static void setEnabled(boolean value) {
        enabled = value;
        if (value) {
            return;
        }
        if (!RemixEntityFireCapture.isActive() && !RemixDynamicEntitySession.isEntityActive()) {
            return;
        }
        RemixEntityFireCapture.resetActiveCapture();
        RemixDynamicEntitySession.clearEntityState();
    }

    static boolean canCapture() {
        return enabled && RemixDynamicEntitySession.canCapture();
    }

    static boolean isTrackedLivingEntity(sn entity) {
        Class<?> baseClass = resolveLivingEntityBaseClass(entity);
        return baseClass != null && baseClass.isInstance(entity);
    }

    private static int clampHurtStage(int hurtStage) {
        return Math.max(0, Math.min(MAX_HURT_STAGE, hurtStage));
    }

    private static Class<?> resolveLivingEntityBaseClass(sn entity) {
        if (entity == null) {
            return null;
        }
        Class<?> cached = livingEntityBaseClass;
        if (cached != null && cached.isInstance(entity)) {
            return cached;
        }
        Class<?> type = entity.getClass();
        while (type != null) {
            if ("ls".equals(type.getName())) {
                livingEntityBaseClass = type;
                return type;
            }
            type = type.getSuperclass();
        }
        return null;
    }

    private static int resolveHurtStage(sn entity) {
        if (entity == null) {
            return 0;
        }
        Class<?> baseClass = resolveLivingEntityBaseClass(entity);
        if (baseClass == null || !baseClass.isInstance(entity)) {
            return 0;
        }

        try {
            Field hurtTimeField = livingEntityHurtTimeField;
            if (hurtTimeField != null && hurtTimeField.getDeclaringClass().isInstance(entity)) {
                return clampHurtStage(hurtTimeField.getInt(entity));
            }

            hurtTimeField = null;
            Class<?> type = entity.getClass();
            while (type != null) {
                try {
                    Field candidateField = type.getDeclaredField("aa");
                    if (candidateField.getType() != Integer.TYPE) {
                        type = type.getSuperclass();
                        continue;
                    }
                    candidateField.setAccessible(true);
                    hurtTimeField = candidateField;
                    livingEntityHurtTimeField = candidateField;
                    break;
                } catch (NoSuchFieldException missingField) {
                    type = type.getSuperclass();
                }
            }
            if (hurtTimeField == null || !hurtTimeField.getDeclaringClass().isInstance(entity)) {
                return 0;
            }
            return clampHurtStage(hurtTimeField.getInt(entity));
        } catch (IllegalAccessException exception) {
            return 0;
        } catch (IllegalArgumentException exception) {
            return 0;
        }
    }

    private static Class<?> resolveCreeperEntityClass(sn entity) {
        if (entity == null) {
            return null;
        }
        Class<?> cached = creeperEntityClass;
        if (cached != null && cached.isInstance(entity)) {
            return cached;
        }
        Class<?> type = entity.getClass();
        while (type != null) {
            if ("gb".equals(type.getName())) {
                creeperEntityClass = type;
                return type;
            }
            type = type.getSuperclass();
        }
        return null;
    }

    private static boolean isTrackedCreeper(sn entity) {
        Class<?> type = resolveCreeperEntityClass(entity);
        return type != null && type.isInstance(entity);
    }

    private static Field resolveIntField(Class<?> startClass, String fieldName) {
        Class<?> type = startClass;
        while (type != null) {
            try {
                Field field = type.getDeclaredField(fieldName);
                if (field.getType() == Integer.TYPE) {
                    field.setAccessible(true);
                    return field;
                }
            } catch (NoSuchFieldException missingField) {
                // Continue up the hierarchy.
            }
            type = type.getSuperclass();
        }
        return null;
    }

    private static float resolveFuseProgress(sn entity, float partialTicks) {
        if (entity == null) {
            return 0.0f;
        }
        Class<?> trackedClass = resolveCreeperEntityClass(entity);
        if (trackedClass == null || !trackedClass.isInstance(entity)) {
            return 0.0f;
        }

        try {
            Field currentFuseField = creeperFuseTimeField;
            if (currentFuseField == null || !currentFuseField.getDeclaringClass().isInstance(entity)) {
                currentFuseField = resolveIntField(entity.getClass(), "a");
                creeperFuseTimeField = currentFuseField;
            }
            Field previousFuseField = creeperPreviousFuseTimeField;
            if (previousFuseField == null || !previousFuseField.getDeclaringClass().isInstance(entity)) {
                previousFuseField = resolveIntField(entity.getClass(), "b");
                creeperPreviousFuseTimeField = previousFuseField;
            }
            if (currentFuseField == null || previousFuseField == null) {
                return 0.0f;
            }

            int previousFuse = previousFuseField.getInt(entity);
            int currentFuse = currentFuseField.getInt(entity);
            int fuseState = resolveFuseState(entity);
            return CREEPER_FUSE_TRACKER.resolveProgress(
                    entity.aD,
                    entity.bt,
                    fuseState,
                    previousFuse,
                    currentFuse,
                    partialTicks);
        } catch (IllegalAccessException exception) {
            return 0.0f;
        } catch (IllegalArgumentException exception) {
            return 0.0f;
        }
    }

    private static int resolveFuseState(sn entity) {
        try {
            Method method = creeperFuseStateMethod;
            if (method == null || !method.getDeclaringClass().isInstance(entity)) {
                method = entity.getClass().getDeclaredMethod("v");
                method.setAccessible(true);
                creeperFuseStateMethod = method;
            }
            Object result = method.invoke(entity);
            return result instanceof Integer ? ((Integer) result).intValue() : 0;
        } catch (NoSuchMethodException exception) {
            return 0;
        } catch (IllegalAccessException exception) {
            return 0;
        } catch (InvocationTargetException exception) {
            return 0;
        }
    }

}
