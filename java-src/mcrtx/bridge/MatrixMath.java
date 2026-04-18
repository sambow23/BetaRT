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

    /**
     * Inverts a column-major 4x4 matrix whose upper-left 3x3 is a rotation
     * (possibly with uniform scale) and whose last column is a translation.
     * Used to invert OpenGL's GL_MODELVIEW matrix, which for Minecraft Beta's
     * camera setup is rotation + translation only. Falls back to returning an
     * identity matrix if the rotation basis is degenerate.
     */
    public static float[] invertAffineColumnMajor(float[] matrix) {
        float[] result = new float[16];

        // Transpose of the 3x3 rotation, divided by |col|^2 to handle uniform scale.
        float s0 = matrix[0] * matrix[0] + matrix[1] * matrix[1] + matrix[2] * matrix[2];
        float s1 = matrix[4] * matrix[4] + matrix[5] * matrix[5] + matrix[6] * matrix[6];
        float s2 = matrix[8] * matrix[8] + matrix[9] * matrix[9] + matrix[10] * matrix[10];
        if (s0 <= 1.0e-12f || s1 <= 1.0e-12f || s2 <= 1.0e-12f) {
            result[0] = 1.0f;
            result[5] = 1.0f;
            result[10] = 1.0f;
            result[15] = 1.0f;
            return result;
        }
        float i0 = 1.0f / s0;
        float i1 = 1.0f / s1;
        float i2 = 1.0f / s2;

        result[0] = matrix[0] * i0;
        result[1] = matrix[4] * i1;
        result[2] = matrix[8] * i2;
        result[3] = 0.0f;

        result[4] = matrix[1] * i0;
        result[5] = matrix[5] * i1;
        result[6] = matrix[9] * i2;
        result[7] = 0.0f;

        result[8] = matrix[2] * i0;
        result[9] = matrix[6] * i1;
        result[10] = matrix[10] * i2;
        result[11] = 0.0f;

        float tx = matrix[12];
        float ty = matrix[13];
        float tz = matrix[14];
        result[12] = -(result[0] * tx + result[4] * ty + result[8] * tz);
        result[13] = -(result[1] * tx + result[5] * ty + result[9] * tz);
        result[14] = -(result[2] * tx + result[6] * ty + result[10] * tz);
        result[15] = 1.0f;
        return result;
    }

    public static float[] transformPointColumnMajor(float[] matrix, float x, float y, float z) {
        return new float[] {
                matrix[0] * x + matrix[4] * y + matrix[8] * z + matrix[12],
                matrix[1] * x + matrix[5] * y + matrix[9] * z + matrix[13],
                matrix[2] * x + matrix[6] * y + matrix[10] * z + matrix[14]
        };
    }
}
