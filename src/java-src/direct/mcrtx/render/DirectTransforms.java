package mcrtx.render;

import java.util.Arrays;

/** CPU transforms used by the game's rendering calculations; no driver state is queried. */
public final class DirectTransforms {
    private static final float[][][] STACKS = new float[3][64][16];
    private static final int[] DEPTH = new int[3];
    private static int selected;
    static { reset(); }
    private DirectTransforms() { }
    public static void reset() {
        Arrays.fill(DEPTH, 0); selected = 0;
        for (int i = 0; i < 3; ++i) identity(STACKS[i][0]);
    }
    public static void identity(float[] matrix) {
        Arrays.fill(matrix, 0); matrix[0] = matrix[5] = matrix[10] = matrix[15] = 1;
    }
    public static float[] model() { return STACKS[0][DEPTH[0]]; }
    public static float[] projection() { return STACKS[1][DEPTH[1]]; }
    public static float[] texture() { return STACKS[2][DEPTH[2]]; }
    private static float[] current() { return STACKS[selected][DEPTH[selected]]; }
    public static void select(int kind) {
        selected = kind - 5888;
        if (selected < 0 || selected > 2) throw new IllegalArgumentException("Unsupported transform kind");
    }
    public static void loadIdentity() { identity(current()); }
    public static void push() {
        int depth = DEPTH[selected];
        if (depth == 63) throw new IllegalStateException("Transform stack overflow");
        System.arraycopy(current(), 0, STACKS[selected][depth + 1], 0, 16);
        DEPTH[selected]++;
    }
    public static void pop() {
        if (DEPTH[selected] == 0) throw new IllegalStateException("Transform stack underflow");
        DEPTH[selected]--;
    }
    public static void multiply(float[] left, float[] right, float[] output) {
        float[] result = new float[16];
        for (int column = 0; column < 4; ++column)
            for (int row = 0; row < 4; ++row)
                for (int k = 0; k < 4; ++k)
                    result[column * 4 + row] += left[k * 4 + row] * right[column * 4 + k];
        System.arraycopy(result, 0, output, 0, 16);
    }
    public static void translate(float x, float y, float z) {
        float[] matrix = current();
        for (int row = 0; row < 4; ++row)
            matrix[12 + row] += matrix[row] * x + matrix[4 + row] * y + matrix[8 + row] * z;
    }
    public static void translate(double x, double y, double z) { translate((float)x, (float)y, (float)z); }
    public static void scale(float x, float y, float z) {
        float[] matrix = current();
        for (int row = 0; row < 4; ++row) { matrix[row] *= x; matrix[4 + row] *= y; matrix[8 + row] *= z; }
    }
    public static void scale(double x, double y, double z) { scale((float)x, (float)y, (float)z); }
    public static void rotate(float angle, float x, float y, float z) {
        double length = Math.sqrt(x*x + y*y + z*z);
        if (length == 0) return;
        x /= length; y /= length; z /= length;
        float c = (float)Math.cos(Math.toRadians(angle)), s = (float)Math.sin(Math.toRadians(angle)), t = 1-c;
        float[] rotation = {t*x*x+c,t*x*y+s*z,t*x*z-s*y,0,
                t*x*y-s*z,t*y*y+c,t*y*z+s*x,0,
                t*x*z+s*y,t*y*z-s*x,t*z*z+c,0,0,0,0,1};
        multiply(current(), rotation, current());
    }
    public static void perspective(float fov, float aspect, float near, float far) {
        if (!(fov > 0 && fov < 180 && aspect > 0 && near > 0 && far > near))
            throw new IllegalArgumentException("Invalid perspective");
        float y = (float)(1 / Math.tan(Math.toRadians(fov) / 2));
        float[] matrix = {y/aspect,0,0,0,0,y,0,0,0,0,-(far+near)/(far-near),-1,
                0,0,-2*far*near/(far-near),0};
        multiply(current(), matrix, current());
    }
    public static void ortho(double left, double right, double bottom, double top, double near, double far) {
        if (left == right || bottom == top || near == far) throw new IllegalArgumentException("Invalid orthographic view");
        float[] matrix = {(float)(2/(right-left)),0,0,0,0,(float)(2/(top-bottom)),0,0,
                0,0,(float)(-2/(far-near)),0,(float)(-(right+left)/(right-left)),
                (float)(-(top+bottom)/(top-bottom)),(float)(-(far+near)/(far-near)),1};
        multiply(current(), matrix, current());
        DirectRenderer.ui = true;
    }
    public static float[] clipMatrix() {
        float[] result = new float[16]; multiply(projection(), model(), result); return result;
    }
}
