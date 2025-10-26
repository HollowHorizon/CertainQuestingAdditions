package ru.hollowhorizon.additions.questing.client;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.ShaderProgram;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.*;
import net.minecraft.client.texture.TextureManager;
import net.minecraft.util.Identifier;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterShadersEvent;
import org.joml.Matrix4f;

import java.io.IOException;

@EventBusSubscriber
public class CustomBackgroundRenderer {
    public static ShaderProgram shader;

    @SubscribeEvent
    public static void onShaderRegister(RegisterShadersEvent event) {
        try {
            event.registerShader(new ShaderProgram(event.getResourceProvider(), Identifier.of("certain_questing_additions:custom_background"), VertexFormats.POSITION_TEXTURE), shader -> {
                CustomBackgroundRenderer.shader = shader;
            });
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void draw(DrawContext graphics, int x, int y, int w, int h, double centerX, double centerY) {
        var mc = MinecraftClient.getInstance();
        RenderSystem.setShader(() -> shader);
        shader.getUniformOrDefault("iResolution").set((float) w, (float) h, 0.0f);
        assert mc.world != null;
        shader.getUniformOrDefault("iTime").set((mc.world.getTime() % 100000 + mc.getRenderTickCounter().getTickDelta(true)) / 20f);
        shader.getUniformOrDefault("iMouse").set(
                (float) centerX * 64f, (float) centerY * 32f, 0f, 0f
        );
        Matrix4f matrix = graphics.getMatrices().peek().getPositionMatrix();

        BufferBuilder buffer = Tessellator.getInstance().begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE);
        buffer.vertex(matrix, x,     y,     0f).texture(0f, 0f);
        buffer.vertex(matrix, x,     y + h, 0f).texture(0f, 1f);
        buffer.vertex(matrix, x + w, y + h, 0f).texture(1f, 1f);
        buffer.vertex(matrix, x + w, y,     0f).texture(1f, 0f);
        BufferRenderer.drawWithGlobalProgram(buffer.end());
    }
}
