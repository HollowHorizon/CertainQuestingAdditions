package ru.hollowhorizon.additions.questing;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.hollowhorizon.additions.questing.compat.emi.EMIRecipeModHelper;
import ru.hollowhorizon.additions.questing.config.QuestAnimationsConfig;

public final class CertainQuestingAdditions {
    public static final String MOD_ID = "certain_questing_additions";
    public static final Logger LOGGER = LogManager.getLogger("CertainQuestingAdditions");

    public static void init() {
        QuestAnimationsConfig.init();

        //? if forge {
        /*boolean isEmiLoaded = net.minecraftforge.fml.ModList.get().isLoaded("emi");
        *///?} elif fabric {
        /*boolean isEmiLoaded = net.fabricmc.loader.api.FabricLoader.getInstance().isModLoaded("emi");
        *///?} elif neoforge {
        boolean isEmiLoaded = net.neoforged.fml.ModList.get().isLoaded("emi");
        //?}

        //? if forge {
        /*boolean isJeiLoaded = net.minecraftforge.fml.ModList.get().isLoaded("jei");
        *///?} elif fabric {
        /*boolean isJeiLoaded = net.fabricmc.loader.api.FabricLoader.getInstance().isModLoaded("jei");
        *///?} elif neoforge {
        boolean isJeiLoaded = net.neoforged.fml.ModList.get().isLoaded("jei");
        //?}



        if(isEmiLoaded && !isJeiLoaded) {
            EMIRecipeModHelper.setRecipeModHelper();
        }
    }
}
