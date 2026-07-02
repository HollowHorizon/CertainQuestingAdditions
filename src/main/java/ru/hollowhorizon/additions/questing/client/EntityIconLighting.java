package ru.hollowhorizon.additions.questing.client;

import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import org.joml.Vector3f;

public final class EntityIconLighting {
    public static final int FULL_BRIGHT_LIGHT = LightmapTextureManager.pack(15, 15);
    static final Vector3f FLAT_LIGHT = new Vector3f(0F, -1F, 0F);

    private EntityIconLighting() {
    }

    public static VertexConsumerProvider flat(VertexConsumerProvider delegate) {
        return FlatEntityLightingVertexConsumerProvider.wrap(delegate, FLAT_LIGHT, FULL_BRIGHT_LIGHT);
    }

    public static VertexConsumer flat(VertexConsumer delegate) {
        return FlatEntityLightingVertexConsumerProvider.wrap(delegate, FLAT_LIGHT, FULL_BRIGHT_LIGHT);
    }

    public static VertexConsumer flatBuffer(VertexConsumerProvider delegate, RenderLayer layer) {
        return flat(delegate.getBuffer(layer));
    }
}
