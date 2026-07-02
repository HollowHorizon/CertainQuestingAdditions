package ru.hollowhorizon.additions.questing.compat.emi;

//? if >= 1.21.11 {
import dev.ftb.mods.ftblibrary.client.gui.IScreenWrapper;
//?} else {
/*import dev.ftb.mods.ftblibrary.ui.IScreenWrapper;
*///?}
import dev.ftb.mods.ftbquests.client.gui.quests.QuestScreen;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import ru.hollowhorizon.additions.questing.client.EmiRecipeScreenFallback;

final class EmiQuestScreenReturn {
    private EmiQuestScreenReturn() {
    }

    static Screen captureCurrentQuestScreen() {
        Screen screen = MinecraftClient.getInstance().currentScreen;
        if (screen instanceof IScreenWrapper wrapper && wrapper.getGui() instanceof QuestScreen) {
            return screen;
        }

        return null;
    }

    static void attachFallback(Screen fallbackScreen) {
        if (fallbackScreen == null) {
            return;
        }

        Screen currentScreen = MinecraftClient.getInstance().currentScreen;
        if (currentScreen instanceof EmiRecipeScreenFallback recipeScreen) {
            recipeScreen.cqa$setFallbackScreen(fallbackScreen);
        }
    }
}
