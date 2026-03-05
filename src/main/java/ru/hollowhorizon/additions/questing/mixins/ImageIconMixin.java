package ru.hollowhorizon.additions.questing.mixins;

import dev.ftb.mods.ftblibrary.icon.ImageIcon;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.hollowhorizon.additions.questing.client.ApngTextureManager;

@Mixin(value = ImageIcon.class, remap = false)
public abstract class ImageIconMixin {
    @Shadow
    @Final
    public Identifier texture;

    @Inject(method = "bindTexture", at = @At("HEAD"), cancellable = true)
    private void cqa$bindAnimatedTexture(CallbackInfo ci) {
        if (ApngTextureManager.bindIfAnimated(texture)) {
            ci.cancel();
        }
    }
}
