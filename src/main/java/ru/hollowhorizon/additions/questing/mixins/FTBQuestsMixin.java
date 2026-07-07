package ru.hollowhorizon.additions.questing.mixins;

import dev.ftb.mods.ftbquests.FTBQuests;
import dev.ftb.mods.ftbquests.integration.RecipeModHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import ru.hollowhorizon.additions.questing.CertainQuestingAdditions;

@Mixin(value = FTBQuests.class, remap = false)
public class FTBQuestsMixin {
    @Inject(method = "getRecipeModHelper", at = @At("HEAD"), cancellable = true)
    private static void getRecipeHelper(CallbackInfoReturnable<RecipeModHelper> cir) {
        var helper = CertainQuestingAdditions.getRecipeModHelperOrNull();
        if (helper != null) cir.setReturnValue(helper);
    }
}
