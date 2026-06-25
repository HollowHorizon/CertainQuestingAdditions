package ru.hollowhorizon.additions.questing.mixins;

import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import ru.hollowhorizon.additions.questing.client.EntityIconRenderContext;

@Mixin(EntityRenderer.class)
public abstract class EntityRendererMixin<T extends Entity> {
    @Inject(method = "hasLabel", at = @At("HEAD"), cancellable = true)
    private void cqa$suppressEntityIconLabel(T entity, CallbackInfoReturnable<Boolean> cir) {
        if (EntityIconRenderContext.suppressesVanillaLabels()) {
            cir.setReturnValue(false);
        }
    }

    //? if >= 1.21.1 {
    @Inject(method = "getShadowRadius", at = @At("HEAD"), cancellable = true)
    private void cqa$skipEntityIconShadow(T entity, CallbackInfoReturnable<Float> cir) {
        if (EntityIconRenderContext.isRenderingEntityIcon()) {
            cir.setReturnValue(0F);
        }
    }
    //?}

    @Inject(method = "renderLabelIfPresent", at = @At("HEAD"), cancellable = true)
    private void cqa$skipEntityIconLabelRender(
            T entity,
            Text text,
            MatrixStack matrices,
            VertexConsumerProvider vertexConsumers,
            int light,
            //? if >= 1.21.1 {
            float tickDelta,
            //?}
            CallbackInfo ci
    ) {
        if (EntityIconRenderContext.suppressesVanillaLabels()) {
            ci.cancel();
        }
    }
}
