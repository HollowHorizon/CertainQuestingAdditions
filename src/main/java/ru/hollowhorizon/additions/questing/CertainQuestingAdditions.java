package ru.hollowhorizon.additions.questing;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.hollowhorizon.additions.questing.compat.emi.EMIRecipeModHelper;
import ru.hollowhorizon.additions.questing.config.QuestAnimationsConfig;

public final class CertainQuestingAdditions {
    public static final String MOD_ID = "certain_questing_additions";
    public static final Logger LOGGER = LogManager.getLogger("CertainQuestingAdditions");
    private static EMIRecipeModHelper helper = null;

    public static void init() {
        QuestAnimationsConfig.init();

        if (isLoaded("emi")) {
            helper = new EMIRecipeModHelper();
        }
    }

    public static EMIRecipeModHelper getRecipeModHelperOrNull() {
        if (isLoaded("emi")) {
            return helper;
        } else {
            return null;
        }
    }

    public static boolean isLoaded(String mod) {
        //? if forge {
        /*return net.minecraftforge.fml.ModList.get().isLoaded(mod);
         *///?} elif fabric {
        /*return net.fabricmc.loader.api.FabricLoader.getInstance().isModLoaded(mod);
         *///?} elif neoforge {
        return net.neoforged.fml.ModList.get().isLoaded(mod);
        //?}
    }
}
