package ru.hollowhorizon.additions.questing.client;

import net.minecraft.util.math.MathHelper;
import org.joml.Matrix4f;
import org.joml.Vector4f;

final class MatrixScale2D {
    private static final float MIN_VALID_SCALE = 0.0001F;

    private final float x;
    private final float y;

    private MatrixScale2D(float x, float y) {
        this.x = x;
        this.y = y;
    }

    static MatrixScale2D from(Matrix4f matrix) {
        Vector4f origin = transform(matrix, 0, 0);
        Vector4f xAxis = transform(matrix, 1, 0);
        Vector4f yAxis = transform(matrix, 0, 1);
        return new MatrixScale2D(distance(origin, xAxis), distance(origin, yAxis));
    }

    boolean isInvalid() {
        return !Float.isFinite(x)
                || !Float.isFinite(y)
                || x <= MIN_VALID_SCALE
                || y <= MIN_VALID_SCALE;
    }

    float x() {
        return x;
    }

    float y() {
        return y;
    }

    private static Vector4f transform(Matrix4f matrix, int x, int y) {
        Vector4f point = new Vector4f(x, y, 0F, 1F);
        point.mul(matrix);
        return point;
    }

    private static float distance(Vector4f first, Vector4f second) {
        float dx = first.x - second.x;
        float dy = first.y - second.y;
        return MathHelper.sqrt(dx * dx + dy * dy);
    }
}
