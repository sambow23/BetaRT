import java.nio.FloatBuffer;
import mcrtx.bridge.CameraPose;
import mcrtx.bridge.HookProfiler;
import mcrtx.bridge.MatrixMath;
import mcrtx.bridge.McrtxRuntimeConfig;
import mcrtx.bridge.McrtxRuntimeSettings;
import mcrtx.bridge.MinecraftRenderHooks;
import mcrtx.lwjglshim.OpenGlCompat;
import org.lwjgl.BufferUtils;

public final class RemixCameraState {
    private static final int GL_MODELVIEW_MATRIX = 0x0BA6;
    private static final int FRUSTUM_PLANE_COUNT = 6;
    private static final float CHUNK_SECTION_FRUSTUM_PADDING = 6.0f;
    private static final double DEFAULT_NO_CULL_DISTANCE_BLOCKS = 200.0;
    private static final FloatBuffer VIEW_BUFFER = BufferUtils.createFloatBuffer(16);
    private static final float[] FRUSTUM_PLANES = new float[FRUSTUM_PLANE_COUNT * 4];
    private static volatile double noCullDistanceBlocks = loadNoCullDistanceBlocks();
    private static volatile double noCullDistanceSq = noCullDistanceBlocks * noCullDistanceBlocks;

    static float cameraPositionX;
    static float cameraPositionY;
    static float cameraPositionZ;
    static float cameraForwardX = 0.0f;
    static float cameraForwardY = 0.0f;
    static float cameraForwardZ = 1.0f;
    static float cameraUpX = 0.0f;
    static float cameraUpY = 1.0f;
    static float cameraUpZ = 0.0f;
    static float cameraRightX = 1.0f;
    static float cameraRightY = 0.0f;
    static float cameraRightZ = 0.0f;

    private static float fovYDegrees = 70.0f;
    private static float aspect = 1.0f;
    private static float nearPlane = 0.05f;
    private static float farPlane = 1024.0f;

    private static boolean frameViewCaptured;
    private static boolean frustumReady;
    private static final float[] frameInverseViewMatrix = new float[16];

    private RemixCameraState() {
    }

    static void onFramePresented() {
        frameViewCaptured = false;
    }

    public static void onCamera(ls entity, float partialTicks, int width, int height, float farPlane, boolean thirdPersonActive) {
        if (entity == null) {
            return;
        }
        bt position = entity.e(partialTicks);
        bt forward = entity.f(partialTicks);
        cameraPositionX = (float) position.a;
        cameraPositionY = (float) (position.b + (double) entity.bf - 1.62);
        cameraPositionZ = (float) position.c;

        float fx = (float) forward.a;
        float fy = (float) forward.b;
        float fz = (float) forward.c;
        float forwardLength = (float) Math.sqrt(fx * fx + fy * fy + fz * fz);
        if (forwardLength > 0.0f) {
            cameraForwardX = fx / forwardLength;
            cameraForwardY = fy / forwardLength;
            cameraForwardZ = fz / forwardLength;
        }

        float upx = 0.0f;
        float upy = 1.0f;
        float upz = 0.0f;
        // Derive the right vector from forward × worldUp. This stays well
        // defined until forward is collinear with worldUp (pitch = ±90°),
        // where we fall back to the previous frame's right to avoid a
        // discontinuous basis snap that visibly distorts the image.
        float rx = cameraForwardY * upz - cameraForwardZ * upy;
        float ry = cameraForwardZ * upx - cameraForwardX * upz;
        float rz = cameraForwardX * upy - cameraForwardY * upx;
        float rightLength = (float) Math.sqrt(rx * rx + ry * ry + rz * rz);
        if (rightLength > 1.0e-4f) {
            cameraRightX = rx / rightLength;
            cameraRightY = ry / rightLength;
            cameraRightZ = rz / rightLength;
        }

        float ux = cameraRightY * cameraForwardZ - cameraRightZ * cameraForwardY;
        float uy = cameraRightZ * cameraForwardX - cameraRightX * cameraForwardZ;
        float uz = cameraRightX * cameraForwardY - cameraRightY * cameraForwardX;
        float upLength = (float) Math.sqrt(ux * ux + uy * uy + uz * uz);
        if (upLength > 0.0f) {
            cameraUpX = ux / upLength;
            cameraUpY = uy / upLength;
            cameraUpZ = uz / upLength;
        }

        float aspect = height <= 0 ? 1.0f : (float) width / (float) height;
        RemixCameraState.fovYDegrees = (float) McrtxRuntimeSettings.getGameplayFovDegrees();
        RemixCameraState.aspect = aspect;
        RemixCameraState.nearPlane = 0.05f;
        RemixCameraState.farPlane = farPlane * 2.0f;
        updateFrustumPlanes();
    }

    /**
     * Reads GL_MODELVIEW at the moment dynamic entities are about to be
     * rendered, stores its inverse as the authoritative camera-to-world, and
     * submits the decomposed basis to Remix so the game camera and dynamic
     * entity transforms share a single source of truth -- otherwise view bob
     * and damage tilt leak into entity world positions and cause rubber-band
     * lag behind the camera.
     *
     * Beta's view matrix at this point contains only camera rotation; the
     * world-space translation is applied per-draw (chunks via the tessellator
     * offset, entities via glTranslated). So we take the rotation basis from
     * GL (bob/tilt included) and compose it with the entity-derived camera
     * position previously stashed by {@link #onCamera} to build a full
     * camera-to-world matrix.
     */
    public static void captureFrameView() {
        if (!MinecraftRenderHooks.isInitialized()) {
            return;
        }
        long captureStartNanos = System.nanoTime();
        VIEW_BUFFER.clear();
        if (!OpenGlCompat.getFloat(GL_MODELVIEW_MATRIX, VIEW_BUFFER)) {
            return;
        }
        long readModelViewEndNanos = System.nanoTime();
        float[] view = new float[16];
        VIEW_BUFFER.get(view);

        float[] inverse = MatrixMath.invertAffineColumnMajor(view);
        long invertViewEndNanos = System.nanoTime();
        inverse[12] += cameraPositionX;
        inverse[13] += cameraPositionY;
        inverse[14] += cameraPositionZ;
        inverse[15] = 1.0f;
        System.arraycopy(inverse, 0, frameInverseViewMatrix, 0, 16);
        frameViewCaptured = true;

        cameraRightX = inverse[0];
        cameraRightY = inverse[1];
        cameraRightZ = inverse[2];
        cameraUpX = inverse[4];
        cameraUpY = inverse[5];
        cameraUpZ = inverse[6];
        cameraForwardX = -inverse[8];
        cameraForwardY = -inverse[9];
        cameraForwardZ = -inverse[10];

        // Submit the basis decomposed from the GL view matrix directly. The
        // 10-arg helper would re-derive right/up from forward × (0,1,0) and
        // snap at pitch = ±90°, discarding the authoritative basis we just
        // extracted from GL_MODELVIEW.
        CameraPose cameraPose = new CameraPose();
        cameraPose.px = inverse[12];
        cameraPose.py = inverse[13];
        cameraPose.pz = inverse[14];
        cameraPose.fx = cameraForwardX;
        cameraPose.fy = cameraForwardY;
        cameraPose.fz = cameraForwardZ;
        cameraPose.ux = cameraUpX;
        cameraPose.uy = cameraUpY;
        cameraPose.uz = cameraUpZ;
        cameraPose.rx = cameraRightX;
        cameraPose.ry = cameraRightY;
        cameraPose.rz = cameraRightZ;
        cameraPose.fovYDegrees = fovYDegrees;
        cameraPose.aspect = aspect;
        cameraPose.nearPlane = nearPlane;
        cameraPose.farPlane = farPlane;
        updateFrustumPlanes();
        MinecraftRenderHooks.updateCamera(cameraPose);
        long submitCameraEndNanos = System.nanoTime();

        HookProfiler.record(HookProfiler.SIDE_HOOK, "hook.onFrameViewCaptured.readModelView",
            readModelViewEndNanos - captureStartNanos);
        HookProfiler.record(HookProfiler.SIDE_HOOK, "hook.onFrameViewCaptured.invertView",
            invertViewEndNanos - readModelViewEndNanos);
        HookProfiler.record(HookProfiler.SIDE_HOOK, "hook.onFrameViewCaptured.submitCamera",
            submitCameraEndNanos - invertViewEndNanos);
    }

    static float[] buildInverseViewMatrix() {
        if (frameViewCaptured) {
            return frameInverseViewMatrix.clone();
        }
        float[] matrix = new float[16];
        matrix[0] = cameraRightX;
        matrix[1] = cameraRightY;
        matrix[2] = cameraRightZ;
        matrix[3] = 0.0f;
        matrix[4] = cameraUpX;
        matrix[5] = cameraUpY;
        matrix[6] = cameraUpZ;
        matrix[7] = 0.0f;
        matrix[8] = -cameraForwardX;
        matrix[9] = -cameraForwardY;
        matrix[10] = -cameraForwardZ;
        matrix[11] = 0.0f;
        matrix[12] = cameraPositionX;
        matrix[13] = cameraPositionY;
        matrix[14] = cameraPositionZ;
        matrix[15] = 1.0f;
        return matrix;
    }

    static boolean shouldCaptureChunkSection(int originX, int originY, int originZ) {
        return shouldCaptureBounds(
                originX - CHUNK_SECTION_FRUSTUM_PADDING,
                originY - CHUNK_SECTION_FRUSTUM_PADDING,
                originZ - CHUNK_SECTION_FRUSTUM_PADDING,
                originX + 16.0f + CHUNK_SECTION_FRUSTUM_PADDING,
                originY + 16.0f + CHUNK_SECTION_FRUSTUM_PADDING,
                originZ + 16.0f + CHUNK_SECTION_FRUSTUM_PADDING);
    }

    public static boolean isWithinNoCullDistance(double minX, double minY, double minZ, double maxX, double maxY, double maxZ) {
        double dx = axisDistance(cameraPositionX, minX, maxX);
        double dy = axisDistance(cameraPositionY, minY, maxY);
        double dz = axisDistance(cameraPositionZ, minZ, maxZ);
        return dx * dx + dy * dy + dz * dz <= noCullDistanceSq;
    }

    public static void setNoCullDistanceBlocks(int blockDistance) {
        double clampedDistance = blockDistance;
        if (!Double.isFinite(clampedDistance) || clampedDistance < 0.0) {
            clampedDistance = 0.0;
        }
        noCullDistanceBlocks = clampedDistance;
        noCullDistanceSq = clampedDistance * clampedDistance;
    }

    static boolean shouldCaptureBounds(double minX, double minY, double minZ, double maxX, double maxY, double maxZ) {
        return isWithinNoCullDistance(minX, minY, minZ, maxX, maxY, maxZ)
                || isBoundsInFrustum(minX, minY, minZ, maxX, maxY, maxZ);
    }

    private static boolean isBoundsInFrustum(double minX, double minY, double minZ, double maxX, double maxY, double maxZ) {
        if (!frustumReady) {
            return true;
        }

        float centerX = (float) ((minX + maxX) * 0.5);
        float centerY = (float) ((minY + maxY) * 0.5);
        float centerZ = (float) ((minZ + maxZ) * 0.5);
        float extentX = (float) ((maxX - minX) * 0.5);
        float extentY = (float) ((maxY - minY) * 0.5);
        float extentZ = (float) ((maxZ - minZ) * 0.5);

        for (int planeIndex = 0; planeIndex < FRUSTUM_PLANE_COUNT; planeIndex += 1) {
            int offset = planeIndex * 4;
            float nx = FRUSTUM_PLANES[offset];
            float ny = FRUSTUM_PLANES[offset + 1];
            float nz = FRUSTUM_PLANES[offset + 2];
            float planeD = FRUSTUM_PLANES[offset + 3];
            float distance = nx * centerX + ny * centerY + nz * centerZ + planeD;
            float radius = Math.abs(nx) * extentX + Math.abs(ny) * extentY + Math.abs(nz) * extentZ;
            if (distance + radius < 0.0f) {
                return false;
            }
        }

        return true;
    }

    private static double loadNoCullDistanceBlocks() {
        String configuredValue = McrtxRuntimeConfig.getEnvironmentValue("MCRTX_NO_CULL_DISTANCE");
        if (configuredValue == null || configuredValue.isEmpty()) {
            return DEFAULT_NO_CULL_DISTANCE_BLOCKS;
        }

        try {
            double configuredDistance = Double.parseDouble(configuredValue);
            if (!Double.isFinite(configuredDistance) || configuredDistance < 0.0) {
                return DEFAULT_NO_CULL_DISTANCE_BLOCKS;
            }
            return configuredDistance;
        } catch (NumberFormatException exception) {
            return DEFAULT_NO_CULL_DISTANCE_BLOCKS;
        }
    }

    private static double axisDistance(double point, double min, double max) {
        if (point < min) {
            return min - point;
        }
        if (point > max) {
            return point - max;
        }
        return 0.0;
    }

    private static void updateFrustumPlanes() {
        float safeAspect = aspect > 0.0f ? aspect : 1.0f;
        float safeNearPlane = nearPlane > 0.0f ? nearPlane : 0.05f;
        float safeFarPlane = farPlane > safeNearPlane ? farPlane : safeNearPlane + 1.0f;
        float clampedFovY = Math.max(1.0f, Math.min(179.0f, fovYDegrees));
        float tanHalfY = (float) Math.tan(Math.toRadians(clampedFovY) * 0.5);
        if (!Float.isFinite(tanHalfY) || tanHalfY <= 0.0f) {
            tanHalfY = (float) Math.tan(Math.toRadians(70.0f) * 0.5);
        }
        float tanHalfX = tanHalfY * safeAspect;

        float[] forward = normalizeVector(cameraForwardX, cameraForwardY, cameraForwardZ, 0.0f, 0.0f, 1.0f);
        float[] right = normalizeVector(cameraRightX, cameraRightY, cameraRightZ, 1.0f, 0.0f, 0.0f);
        float[] up = normalizeVector(cameraUpX, cameraUpY, cameraUpZ, 0.0f, 1.0f, 0.0f);

        float positionDotForward = dot(forward[0], forward[1], forward[2], cameraPositionX, cameraPositionY, cameraPositionZ);
        setPlane(0, forward[0], forward[1], forward[2], -(positionDotForward + safeNearPlane));
        setPlane(1, -forward[0], -forward[1], -forward[2], positionDotForward + safeFarPlane);
        setPlaneThroughCamera(2,
                forward[0] * tanHalfX + right[0],
                forward[1] * tanHalfX + right[1],
                forward[2] * tanHalfX + right[2]);
        setPlaneThroughCamera(3,
                forward[0] * tanHalfX - right[0],
                forward[1] * tanHalfX - right[1],
                forward[2] * tanHalfX - right[2]);
        setPlaneThroughCamera(4,
                forward[0] * tanHalfY + up[0],
                forward[1] * tanHalfY + up[1],
                forward[2] * tanHalfY + up[2]);
        setPlaneThroughCamera(5,
                forward[0] * tanHalfY - up[0],
                forward[1] * tanHalfY - up[1],
                forward[2] * tanHalfY - up[2]);
        frustumReady = true;
    }

    private static void setPlaneThroughCamera(int planeIndex, float nx, float ny, float nz) {
        setPlane(planeIndex, nx, ny, nz, -dot(nx, ny, nz, cameraPositionX, cameraPositionY, cameraPositionZ));
    }

    private static void setPlane(int planeIndex, float nx, float ny, float nz, float planeD) {
        float length = (float) Math.sqrt(nx * nx + ny * ny + nz * nz);
        int offset = planeIndex * 4;
        if (!Float.isFinite(length) || length <= 1.0e-6f) {
            FRUSTUM_PLANES[offset] = 0.0f;
            FRUSTUM_PLANES[offset + 1] = 0.0f;
            FRUSTUM_PLANES[offset + 2] = 1.0f;
            FRUSTUM_PLANES[offset + 3] = 0.0f;
            return;
        }

        float inverseLength = 1.0f / length;
        FRUSTUM_PLANES[offset] = nx * inverseLength;
        FRUSTUM_PLANES[offset + 1] = ny * inverseLength;
        FRUSTUM_PLANES[offset + 2] = nz * inverseLength;
        FRUSTUM_PLANES[offset + 3] = planeD * inverseLength;
    }

    private static float[] normalizeVector(float x, float y, float z, float fallbackX, float fallbackY, float fallbackZ) {
        float length = (float) Math.sqrt(x * x + y * y + z * z);
        if (!Float.isFinite(length) || length <= 1.0e-6f) {
            return new float[] {fallbackX, fallbackY, fallbackZ};
        }

        float inverseLength = 1.0f / length;
        return new float[] {x * inverseLength, y * inverseLength, z * inverseLength};
    }

    private static float dot(float x0, float y0, float z0, float x1, float y1, float z1) {
        return x0 * x1 + y0 * y1 + z0 * z1;
    }
}
