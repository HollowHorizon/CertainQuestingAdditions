package ru.hollowhorizon.additions.questing.client;

import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import org.joml.Vector3f;

final class FlatEntityLightingVertexConsumerProvider implements VertexConsumerProvider {
    private final VertexConsumerProvider delegate;
    private final float normalX;
    private final float normalY;
    private final float normalZ;
    private final int lightU;
    private final int lightV;

    private FlatEntityLightingVertexConsumerProvider(VertexConsumerProvider delegate, Vector3f normal, int packedLight) {
        this.delegate = delegate;
        this.normalX = normal.x;
        this.normalY = normal.y;
        this.normalZ = normal.z;
        this.lightU = packedLight & 0xFFFF;
        this.lightV = packedLight >> 16 & 0xFFFF;
    }

    static VertexConsumerProvider wrap(VertexConsumerProvider delegate, Vector3f normal, int packedLight) {
        return new FlatEntityLightingVertexConsumerProvider(delegate, normal, packedLight);
    }

    @Override
    public VertexConsumer getBuffer(RenderLayer layer) {
        return new FlatEntityLightingVertexConsumer(delegate.getBuffer(layer));
    }

    private final class FlatEntityLightingVertexConsumer implements VertexConsumer {
        private final VertexConsumer delegate;

        private FlatEntityLightingVertexConsumer(VertexConsumer delegate) {
            this.delegate = delegate;
        }

        //? if >= 1.21.1 {
        @Override
        public VertexConsumer vertex(float x, float y, float z) {
            delegate.vertex(x, y, z);
            return this;
        }
        //?} else {
        /*@Override
        public VertexConsumer vertex(double x, double y, double z) {
            delegate.vertex(x, y, z);
            return this;
        }
        *///?}

        @Override
        public VertexConsumer color(int red, int green, int blue, int alpha) {
            delegate.color(red, green, blue, alpha);
            return this;
        }

        @Override
        public VertexConsumer texture(float u, float v) {
            delegate.texture(u, v);
            return this;
        }

        @Override
        public VertexConsumer overlay(int u, int v) {
            delegate.overlay(u, v);
            return this;
        }

        @Override
        public VertexConsumer light(int u, int v) {
            delegate.light(lightU, lightV);
            return this;
        }

        @Override
        public VertexConsumer normal(float x, float y, float z) {
            delegate.normal(normalX, normalY, normalZ);
            return this;
        }

        //? if < 1.21.1 {
        /*@Override
        public void next() {
            delegate.next();
        }

        @Override
        public void fixedColor(int red, int green, int blue, int alpha) {
            delegate.fixedColor(red, green, blue, alpha);
        }

        @Override
        public void unfixColor() {
            delegate.unfixColor();
        }
        *///?}
    }
}
