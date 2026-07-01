package ru.hollowhorizon.additions.questing.mixins;

//? if >= 1.21.11 {
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.state.EntityRenderState;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.hollowhorizon.additions.questing.client.EntityIconRenderContext;

@Mixin(EntityRenderer.class)
public abstract class EntityRendererShadowMixin<T extends Entity, S extends EntityRenderState> {
    @Inject(
            method = {
                    "updateShadow(Lnet/minecraft/entity/Entity;Lnet/minecraft/client/render/entity/state/EntityRenderState;)V",
                    "method_73154(Lnet/minecraft/class_1297;Lnet/minecraft/class_10017;)V",
                    "a(Lcgk;Lidf;)V"
            },
            at = @At("HEAD"),
            cancellable = true,
            remap = false
    )
    private void cqa$skipEntityIconShadow(T entity, S state, CallbackInfo ci) {
        if (EntityIconRenderContext.isRenderingEntityIcon()) {
            state.shadowPieces.clear();
            state.shadowRadius = 0F;
            ci.cancel();
        }
    }
}
//?} else {
/*public abstract class EntityRendererShadowMixin {
}
*///?}
