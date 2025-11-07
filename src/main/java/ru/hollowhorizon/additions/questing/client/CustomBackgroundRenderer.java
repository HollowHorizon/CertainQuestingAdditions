package ru.hollowhorizon.additions.questing.client;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.ShaderProgram;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.*;
import org.joml.Matrix4f;
import ru.hollowhorizon.additions.questing.registry.ModShaders;


public class CustomBackgroundRenderer {
    public static void draw(DrawContext graphics, int x, int y, int w, int h, double centerX, double centerY, double scrollWidth, double scrollHeight, float zoom) {
        var shader = ModShaders.background;

        RenderSystem.setShader(() -> shader);
        setupUniforms(shader, w, h, centerX, centerY, scrollWidth, scrollHeight, zoom);
        Matrix4f matrix = graphics.getMatrices().peek().getPositionMatrix();
        //? if >= 1.21.1 {
        BufferBuilder buffer = Tessellator.getInstance().begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE);
        buffer.vertex(matrix, x, y, 0f).texture(0f, 0f);
        buffer.vertex(matrix, x, y + h, 0f).texture(0f, 1f);
        buffer.vertex(matrix, x + w, y + h, 0f).texture(1f, 1f);
        buffer.vertex(matrix, x + w, y, 0f).texture(1f, 0f);
        BufferRenderer.drawWithGlobalProgram(buffer.end());
        //?} else {
        /*
        BufferBuilder buffer = Tessellator.getInstance().getBuffer();
        buffer.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE);
        buffer.vertex(matrix, x,     y,     0f).texture(0f, 0f);
        buffer.vertex(matrix, x,     y + h, 0f).texture(0f, 1f);
        buffer.vertex(matrix, x + w, y + h, 0f).texture(1f, 1f);
        buffer.vertex(matrix, x + w, y,     0f).texture(1f, 0f);
        BufferRenderer.drawWithGlobalProgram(buffer.end());
        *///?}
    }

    private static void setupUniforms(ShaderProgram shader, int w, int h, double centerX, double centerY, double scrollWidth, double scrollHeight, float zoom) {
        var mc = MinecraftClient.getInstance();

        //? if >= 1.21.1 {
        float time = mc.world == null ? 0f : (mc.world.getTime() % 100000 + mc.getRenderTickCounter().getTickDelta(true)) / 20f;
        //?} else {
        /*float time = mc.world == null ? 0f : (mc.world.getTime() % 100000 + mc.getTickDelta()) / 20f;
        *///?}

        shader.getUniformOrDefault("size").set((float) w, (float) h);
        shader.getUniformOrDefault("scrollOffset").set((float) centerX, (float) centerY);
        shader.getUniformOrDefault("scrollSize").set((float) scrollWidth, (float) scrollHeight);
        shader.getUniformOrDefault("time").set(time);
        shader.getUniformOrDefault("zoom").set(zoom);
    }
}
