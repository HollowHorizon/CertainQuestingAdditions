package ru.hollowhorizon.additions.questing.client;

//? if >= 1.21.11 {
import org.joml.Matrix3x2f;
import org.joml.Matrix3x2fStack;
import org.joml.Vector2f;
//?} else {
/*import net.minecraft.client.util.math.MatrixStack;
import org.joml.Matrix4f;
import org.joml.Vector4f;
*///?}

final class EntityIconRenderGeometry {
    private EntityIconRenderGeometry() {
    }

    static float previewTicks() {
        return System.currentTimeMillis() / 50F;
    }

    static float tickProgress(float previewTicks) {
        return previewTicks - (float) Math.floor(previewTicks);
    }

    //? if >= 1.21.11 {
    static ScreenRect transformedRect(Matrix3x2fStack matrices, int x, int y, int width, int height) {
        Matrix3x2f matrix = new Matrix3x2f(matrices);
        Vector2f topLeft = transform(matrix, x, y);
        Vector2f topRight = transform(matrix, x + width, y);
        Vector2f bottomLeft = transform(matrix, x, y + height);
        Vector2f bottomRight = transform(matrix, x + width, y + height);

        double minX = Math.min(Math.min(topLeft.x, topRight.x), Math.min(bottomLeft.x, bottomRight.x));
        double maxX = Math.max(Math.max(topLeft.x, topRight.x), Math.max(bottomLeft.x, bottomRight.x));
        double minY = Math.min(Math.min(topLeft.y, topRight.y), Math.min(bottomLeft.y, bottomRight.y));
        double maxY = Math.max(Math.max(topLeft.y, topRight.y), Math.max(bottomLeft.y, bottomRight.y));
        return new ScreenRect((minX + maxX) / 2D, (minY + maxY) / 2D, maxX - minX, maxY - minY);
    }

    private static Vector2f transform(Matrix3x2f matrix, int x, int y) {
        return matrix.transformPosition(x, y, new Vector2f());
    }
    //?} else {
    /*static ScreenRect transformedRect(MatrixStack matrices, int x, int y, int width, int height) {
        Matrix4f matrix = matrices.peek().getPositionMatrix();
        Vector4f topLeft = transform(matrix, x, y);
        Vector4f topRight = transform(matrix, x + width, y);
        Vector4f bottomLeft = transform(matrix, x, y + height);
        Vector4f bottomRight = transform(matrix, x + width, y + height);

        double minX = Math.min(Math.min(topLeft.x, topRight.x), Math.min(bottomLeft.x, bottomRight.x));
        double maxX = Math.max(Math.max(topLeft.x, topRight.x), Math.max(bottomLeft.x, bottomRight.x));
        double minY = Math.min(Math.min(topLeft.y, topRight.y), Math.min(bottomLeft.y, bottomRight.y));
        double maxY = Math.max(Math.max(topLeft.y, topRight.y), Math.max(bottomLeft.y, bottomRight.y));
        return new ScreenRect((minX + maxX) / 2D, (minY + maxY) / 2D, maxX - minX, maxY - minY);
    }

    private static Vector4f transform(Matrix4f matrix, int x, int y) {
        Vector4f point = new Vector4f(x, y, 0F, 1F);
        point.mul(matrix);
        return point;
    }
    *///?}
}
