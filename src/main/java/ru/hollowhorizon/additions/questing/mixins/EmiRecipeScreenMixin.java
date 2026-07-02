package ru.hollowhorizon.additions.questing.mixins;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.hollowhorizon.additions.questing.client.EmiRecipeScreenFallback;

import java.util.Map;

@Mixin(targets = "dev.emi.emi.screen.RecipeScreen", remap = false)
public abstract class EmiRecipeScreenMixin implements EmiRecipeScreenFallback {
    @Unique
    private Screen cqa$fallbackScreen;

    @Inject(method = "<init>", at = @At("TAIL"), remap = false)
    private void cqa$copyFallbackScreen(HandledScreen<?> old, Map<?, ?> recipes, CallbackInfo ci) {
        Screen currentScreen = MinecraftClient.getInstance().currentScreen;
        if (currentScreen instanceof EmiRecipeScreenFallback recipeScreen) {
            cqa$fallbackScreen = recipeScreen.cqa$getFallbackScreen();
        }
    }

    @Override
    public Screen cqa$getFallbackScreen() {
        return cqa$fallbackScreen;
    }

    @Override
    public void cqa$setFallbackScreen(Screen fallbackScreen) {
        cqa$fallbackScreen = fallbackScreen;
    }

    @Inject(method = "close", at = @At("HEAD"), cancellable = true, remap = false)
    private void cqa$restoreFallbackScreen(CallbackInfo ci) {
        if (cqa$fallbackScreen == null) {
            return;
        }

        Screen fallbackScreen = cqa$fallbackScreen;
        cqa$fallbackScreen = null;
        MinecraftClient.getInstance().setScreen(fallbackScreen);
        ci.cancel();
    }
}
