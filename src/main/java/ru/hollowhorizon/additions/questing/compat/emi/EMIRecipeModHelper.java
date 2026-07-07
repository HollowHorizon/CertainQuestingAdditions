package ru.hollowhorizon.additions.questing.compat.emi;


import dev.emi.emi.api.EmiApi;
import dev.emi.emi.api.stack.EmiStack;
import dev.ftb.mods.ftbquests.FTBQuests;
import dev.ftb.mods.ftbquests.integration.RecipeModHelper;
import dev.ftb.mods.ftbquests.quest.QuestObjectBase;
import net.minecraft.item.ItemStack;
import ru.hollowhorizon.additions.questing.CertainQuestingAdditions;

public final class EMIRecipeModHelper implements RecipeModHelper {
    @Override
    public void refreshAll(Components components) {}

    @Override
    public void refreshRecipes(QuestObjectBase questObjectBase) {}

    @Override
    public void showRecipes(ItemStack itemStack) {
        var questScreen = EmiQuestScreenReturn.captureCurrentQuestScreen();
        EmiApi.displayRecipes(EmiStack.of(itemStack));
        EmiQuestScreenReturn.attachFallback(questScreen);
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
