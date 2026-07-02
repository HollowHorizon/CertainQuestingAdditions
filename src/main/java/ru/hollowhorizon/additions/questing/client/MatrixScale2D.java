package ru.hollowhorizon.additions.questing.client;

import net.minecraft.util.math.MathHelper;
//? if >= 1.21.11 {
import org.joml.Matrix3x2f;
import org.joml.Vector2f;
//?}
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

    //? if >= 1.21.11 {
    static MatrixScale2D from(Matrix3x2f matrix) {
        Vector2f origin = transform(matrix, 0, 0);
        Vector2f xAxis = transform(matrix, 1, 0);
        Vector2f yAxis = transform(matrix, 0, 1);
        return new MatrixScale2D(distance(origin, xAxis), distance(origin, yAxis));
    }
    //?}

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

    //? if >= 1.21.11 {
    private static Vector2f transform(Matrix3x2f matrix, int x, int y) {
        return matrix.transformPosition(x, y, new Vector2f());
    }

    private static float distance(Vector2f first, Vector2f second) {
        float dx = first.x - second.x;
        float dy = first.y - second.y;
        return MathHelper.sqrt(dx * dx + dy * dy);
    }
    //?}
}
