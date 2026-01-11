package ru.hollowhorizon.additions.questing;

import ru.hollowhorizon.additions.questing.compat.emi.EMIRecipeModHelper;
import ru.hollowhorizon.additions.questing.config.QuestAnimationsConfig;

public final class CertainQuestingAdditions {
    public static final String MOD_ID = "certain_questing_additions";

    public static void init() {
        QuestAnimationsConfig.init();

        //? if forge {
        /*boolean isEmiLoaded = net.minecraftforge.fml.ModList.get().isLoaded("emi");
        *///?} elif fabric {
        /*boolean isEmiLoaded = net.fabricmc.loader.api.FabricLoader.getInstance().isModLoaded("emi");
        *///?} elif neoforge {
        boolean isEmiLoaded = net.neoforged.fml.ModList.get().isLoaded("emi");
        //?}

        if(isEmiLoaded) {
            EMIRecipeModHelper.setRecipeModHelper();
        }
    }
}
