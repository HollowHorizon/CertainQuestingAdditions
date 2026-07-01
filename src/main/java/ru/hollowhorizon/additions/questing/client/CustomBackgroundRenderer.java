package ru.hollowhorizon.additions.questing.client;

import dev.ftb.mods.ftbquests.quest.Chapter;
//? if < 1.21.11 {
/*import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.ShaderProgram;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.BufferRenderer;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;
import org.joml.Matrix4f;
import ru.hollowhorizon.additions.questing.registry.ModShaders;
*///?}
import net.minecraft.client.gui.DrawContext;

public class CustomBackgroundRenderer {
    public static void draw(DrawContext graphics, Chapter selectedChapter, int x, int y, int w, int h, double centerX, double centerY, double scrollWidth, double scrollHeight, float zoom) {
        //? if >= 1.21.11 {
        return;
        //?} else if >= 1.21.1 {
        /*var shaderId = ChapterShaderConfig.resolveShaderId(selectedChapter);
        var shader = ModShaders.get(shaderId);
        if (shader == null) {
            return;
        }

        RenderSystem.setShader(() -> shader);
        setupUniforms(shader, w, h, centerX, centerY, scrollWidth, scrollHeight, zoom);
        Matrix4f matrix = graphics.getMatrices().peek().getPositionMatrix();
        BufferBuilder buffer = Tessellator.getInstance().begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE);
        buffer.vertex(matrix, x, y, 0F).texture(0F, 0F);
        buffer.vertex(matrix, x, y + h, 0F).texture(0F, 1F);
        buffer.vertex(matrix, x + w, y + h, 0F).texture(1F, 1F);
        buffer.vertex(matrix, x + w, y, 0F).texture(1F, 0F);
        BufferRenderer.drawWithGlobalProgram(buffer.end());
        *///?} else {
        /*var shaderId = ChapterShaderConfig.resolveShaderId(selectedChapter);
        var shader = ModShaders.get(shaderId);
        if (shader == null) {
            return;
        }

        RenderSystem.setShader(() -> shader);
        setupUniforms(shader, w, h, centerX, centerY, scrollWidth, scrollHeight, zoom);
        Matrix4f matrix = graphics.getMatrices().peek().getPositionMatrix();
        BufferBuilder buffer = Tessellator.getInstance().getBuffer();
        buffer.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE);
        buffer.vertex(matrix, x, y, 0F).texture(0F, 0F).next();
        buffer.vertex(matrix, x, y + h, 0F).texture(0F, 1F).next();
        buffer.vertex(matrix, x + w, y + h, 0F).texture(1F, 1F).next();
        buffer.vertex(matrix, x + w, y, 0F).texture(1F, 0F).next();
        BufferRenderer.drawWithGlobalProgram(buffer.end());
        *///?}
    }

    //? if < 1.21.11 {
    /*private static void setupUniforms(ShaderProgram shader, int w, int h, double centerX, double centerY, double scrollWidth, double scrollHeight, float zoom) {
        var mc = MinecraftClient.getInstance();

        //? if >= 1.21.1 {
        float time = mc.world == null ? 0F : (mc.world.getTime() % 100000 + mc.getRenderTickCounter().getTickDelta(true)) / 20F;
        //?} else {
        /^float time = mc.world == null ? 0F : (mc.world.getTime() % 100000 + mc.getTickDelta()) / 20F;
        ^///?}

        shader.getUniformOrDefault("size").set((float) w, (float) h);
        shader.getUniformOrDefault("scrollOffset").set((float) centerX, (float) centerY);
        shader.getUniformOrDefault("scrollSize").set((float) scrollWidth, (float) scrollHeight);
        shader.getUniformOrDefault("time").set(time);
        shader.getUniformOrDefault("zoom").set(zoom);
    }
    *///?}
}
