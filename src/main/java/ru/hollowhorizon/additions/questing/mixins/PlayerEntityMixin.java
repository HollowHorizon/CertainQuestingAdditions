package ru.hollowhorizon.additions.questing.mixins;

import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import ru.hollowhorizon.additions.questing.client.EntityIconRenderContext;

@Mixin(PlayerEntity.class)
public abstract class PlayerEntityMixin {
    @Inject(method = "shouldRenderName", at = @At("HEAD"), cancellable = true)
    private void cqa$suppressEntityIconPlayerName(CallbackInfoReturnable<Boolean> cir) {
        if (EntityIconRenderContext.suppressesVanillaLabels()) {
            cir.setReturnValue(false);
        }
    }
}
