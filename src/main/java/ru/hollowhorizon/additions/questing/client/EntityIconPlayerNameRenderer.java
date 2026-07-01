package ru.hollowhorizon.additions.questing.client;

import dev.ftb.mods.ftblibrary.icon.Color4I;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
//? if >= 1.21.11 {
import org.joml.Matrix3x2f;
import org.joml.Matrix3x2fStack;
//?} else {
/*import net.minecraft.client.util.math.MatrixStack;
*///?}
import net.minecraft.entity.Entity;

final class EntityIconPlayerNameRenderer {
    private static final int PLAYER_NAME_GAP = 2;
    private static final int PLAYER_NAME_COLOR = 0xFFFFFF;

    private EntityIconPlayerNameRenderer() {
    }

    static void draw(
            DrawContext graphics,
            MinecraftClient client,
            EntityIconSpec spec,
            Color4I color,
            Entity entity,
            int x,
            int y,
            int width,
            int height,
            float scale
    ) {
        if (!spec.isPlayer() || !spec.playerNameVisible()) {
            return;
        }

        String name = playerName(client, spec, entity);
        if (name.isBlank()) {
            return;
        }

        //? if >= 1.21.11 {
        Matrix3x2fStack matrices = graphics.getMatrices();
        MatrixScale2D matrixScale = MatrixScale2D.from(new Matrix3x2f(matrices));
        //?} else {
        /*MatrixStack matrices = graphics.getMatrices();
        MatrixScale2D matrixScale = MatrixScale2D.from(matrices.peek().getPositionMatrix());
        *///?}
        if (matrixScale.isInvalid()) {
            return;
        }

        int textWidth = client.textRenderer.getWidth(name);
        float labelX = x + width / 2F;
        float labelY = (float) (y + height * 0.85D - entity.getHeight() * scale)
                - (client.textRenderer.fontHeight + PLAYER_NAME_GAP) / matrixScale.y();
        int labelColor = ((color.alphai() & 0xFF) << 24) | PLAYER_NAME_COLOR;

        //? if >= 1.21.11 {
        matrices.pushMatrix();
        //?} else {
        /*matrices.push();
        *///?}
        try {
            //? if >= 1.21.11 {
            matrices.translate(labelX, labelY);
            matrices.scale(1F / matrixScale.x(), 1F / matrixScale.y());
            //?} else {
            /*matrices.translate(labelX, labelY, 100D);
            matrices.scale(1F / matrixScale.x(), 1F / matrixScale.y(), 1F);
            *///?}
            graphics.drawTextWithShadow(client.textRenderer, name, -textWidth / 2, 0, labelColor);
        } finally {
            //? if >= 1.21.11 {
            matrices.popMatrix();
            //?} else {
            /*matrices.pop();
            *///?}
        }
    }

    private static String playerName(MinecraftClient client, EntityIconSpec spec, Entity entity) {
        if (entity instanceof PreviewPlayerEntity previewPlayer) {
            return previewPlayer.shouldShowPreviewName() ? previewPlayer.previewName() : "";
        }

        return spec.playerName(client.getSession().getUsername());
    }
}
