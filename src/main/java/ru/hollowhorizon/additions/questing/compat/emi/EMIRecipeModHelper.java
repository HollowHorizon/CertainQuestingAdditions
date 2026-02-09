package ru.hollowhorizon.additions.questing.compat.emi;


import dev.emi.emi.api.EmiApi;
import dev.emi.emi.api.stack.EmiStack;
import dev.ftb.mods.ftbquests.FTBQuests;
import dev.ftb.mods.ftbquests.integration.RecipeModHelper;
import dev.ftb.mods.ftbquests.quest.QuestObjectBase;
import net.minecraft.item.ItemStack;
import ru.hollowhorizon.additions.questing.CertainQuestingAdditions;

public final class EMIRecipeModHelper implements RecipeModHelper {
    public static void setRecipeModHelper() {
        try {
            FTBQuests.setRecipeModHelper(new EMIRecipeModHelper());
        } catch (IllegalStateException e) {
            CertainQuestingAdditions.LOGGER.warn("Failed to set recipe mod helper, there is already helper ({}) set by another mod.", FTBQuests.getRecipeModHelper().getClass().getSimpleName());
        }
    }

    @Override
    public void refreshAll(Components components) {}

    @Override
    public void refreshRecipes(QuestObjectBase questObjectBase) {}

    @Override
    public void showRecipes(ItemStack itemStack) {
        EmiApi.displayRecipes(EmiStack.of(itemStack));
    }

    @Override
    public String getHelperName() {
        return "EMI";
    }

    @Override
    public boolean isRecipeModAvailable() {
        return true;
    }
}