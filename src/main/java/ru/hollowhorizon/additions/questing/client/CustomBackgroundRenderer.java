package ru.hollowhorizon.additions.questing.client;

import dev.ftb.mods.ftbquests.quest.Chapter;
//? if >= 1.21.11 {
import com.mojang.blaze3d.pipeline.RenderPipeline;
import net.minecraft.client.gui.render.state.TexturedQuadGuiElementRenderState;
import net.minecraft.client.texture.TextureSetup;
import net.minecraft.util.math.MathHelper;
import org.joml.Matrix3x2f;
import org.joml.Matrix3x2fc;
import ru.hollowhorizon.additions.questing.mixins.DrawContextAccessor;
import ru.hollowhorizon.additions.questing.registry.ModShaders;
//?} else {
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
        var shaderId = ChapterShaderConfig.resolveShaderId(selectedChapter);
        RenderPipeline pipeline = ModShaders.get(shaderId);
        if (pipeline == null) {
            return;
        }

        Matrix3x2f pose = new Matrix3x2f((Matrix3x2fc) graphics.getMatrices());
        ((DrawContextAccessor) graphics).cqa$getState().addSimpleElement(new TexturedQuadGuiElementRenderState(
                pipeline,
                TextureSetup.empty(),
                pose,
                x,
                y,
                x + w,
                y + h,
                0F,
                1F,
                0F,
                1F,
                encodeShaderParameters(centerX, centerY, scrollWidth, scrollHeight, zoom),
                null
        ));
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

    //? if >= 1.21.11 {
    private static int encodeShaderParameters(double centerX, double centerY, double scrollWidth, double scrollHeight, float zoom) {
        int scrollX = normalizedByte(scrollWidth <= 0D ? 0D : centerX / scrollWidth);
        int scrollY = normalizedByte(scrollHeight <= 0D ? 0D : centerY / scrollHeight);
        int zoomValue = normalizedByte((zoom - 4F) / 24F);
        return 0xFF000000 | scrollX << 16 | scrollY << 8 | zoomValue;
    }

    private static int normalizedByte(double value) {
        return MathHelper.clamp((int) Math.round(value * 255D), 0, 255);
    }
    //?}

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
