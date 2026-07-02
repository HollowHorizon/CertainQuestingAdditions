package ru.hollowhorizon.additions.questing.mixins;

import dev.ftb.mods.ftblibrary.icon.Icon;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import ru.hollowhorizon.additions.questing.client.EntityIcon;
import ru.hollowhorizon.additions.questing.client.InvalidEntityIcon;

import java.util.Optional;

@Mixin(value = Icon.class, remap = false)
public abstract class IconMixin {
    @Inject(method = "getIcon(Ljava/lang/String;)Ldev/ftb/mods/ftblibrary/icon/Icon;", at = @At("HEAD"), cancellable = true)
    private static void cqa$getEntityIcon(String id, CallbackInfoReturnable<Icon> cir) {
        cqa$getEntityIconOrFallback(id).ifPresent(cir::setReturnValue);
    }

    //? if < 1.21.11 {
    /*@Inject(method = "getIcon0(Ljava/lang/String;)Ldev/ftb/mods/ftblibrary/icon/Icon;", at = @At("HEAD"), cancellable = true)
    private static void cqa$getEntityIcon0(String id, CallbackInfoReturnable<Icon> cir) {
        cqa$getEntityIconOrFallback(id).ifPresent(cir::setReturnValue);
    }
    *///?}

    @Unique
    private static Optional<Icon> cqa$getEntityIconOrFallback(String id) {
        if (!EntityIcon.isEntityIconString(id)) {
            return Optional.empty();
        }

        return Optional.of(EntityIcon.fromIconString(id).<Icon>map(icon -> icon).orElseGet(() -> new InvalidEntityIcon(id)));
    }
}
