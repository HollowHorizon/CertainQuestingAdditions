package ru.hollowhorizon.additions.questing.mixins;

//? if >= 1.21.11 {
import dev.ftb.mods.ftblibrary.client.icon.ImageIconRenderer;
import net.minecraft.client.texture.AbstractTexture;
import net.minecraft.client.texture.TextureManager;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import ru.hollowhorizon.additions.questing.client.ApngTextureManager;

@Mixin(value = ImageIconRenderer.class, remap = false)
public abstract class ImageIconRendererMixin {
    @ModifyArg(
            method = "render(Ldev/ftb/mods/ftblibrary/icon/ImageIcon;Lnet/minecraft/client/gui/DrawContext;IIII)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/texture/TextureManager;getTexture(Lnet/minecraft/util/Identifier;)Lnet/minecraft/client/texture/AbstractTexture;",
                    remap = true
            ),
            index = 0
    )
    private Identifier cqa$resolveAnimatedTexture(Identifier textureId) {
        return ApngTextureManager.resolveAnimatedTexture(textureId);
    }
}
//?}
