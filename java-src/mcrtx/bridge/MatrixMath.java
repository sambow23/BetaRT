package mcrtx.bridge;

public final class MatrixMath {
    private MatrixMath() {
    }

    public static float[] multiplyColumnMajor(float[] left, float[] right) {
        float[] result = new float[16];
        for (int column = 0; column < 4; column++) {
            for (int row = 0; row < 4; row++) {
                result[column * 4 + row] =
                        left[0 * 4 + row] * right[column * 4 + 0]
                                + left[1 * 4 + row] * right[column * 4 + 1]
                                + left[2 * 4 + row] * right[column * 4 + 2]
                                + left[3 * 4 + row] * right[column * 4 + 3];
            }
        }
        return result;
    }
}
