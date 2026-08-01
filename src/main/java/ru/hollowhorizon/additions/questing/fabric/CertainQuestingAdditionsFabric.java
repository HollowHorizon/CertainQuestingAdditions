package ru.hollowhorizon.additions.questing.fabric;

//? if fabric {
/*import ru.hollowhorizon.additions.questing.CertainQuestingAdditions;
import net.fabricmc.api.ModInitializer;

public final class CertainQuestingAdditionsFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        // This code runs as soon as Minecraft is in a mod-load-ready state.
        // However, some things (like resources) may still be uninitialized.
        // Proceed with mild caution.

        // Run our common setup.
        CertainQuestingAdditions.init();
    }
}
*///?} else {
public final class CertainQuestingAdditionsFabric {}
//?}