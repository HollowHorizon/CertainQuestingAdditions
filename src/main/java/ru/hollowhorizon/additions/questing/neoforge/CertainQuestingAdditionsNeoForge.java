package ru.hollowhorizon.additions.questing.neoforge;

//? if neoforge {
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.server.command.ServerCommandSource;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.client.event.RegisterClientCommandsEvent;
import net.neoforged.neoforge.common.NeoForge;
import ru.hollowhorizon.additions.questing.CertainQuestingAdditions;
import ru.hollowhorizon.additions.questing.config.QuestAnimationsConfig;

@Mod(CertainQuestingAdditions.MOD_ID)
public class CertainQuestingAdditionsNeoForge {
    public CertainQuestingAdditionsNeoForge() {
        // Run our common setup.
        CertainQuestingAdditions.init();

        if (FMLEnvironment.dist == Dist.CLIENT) {
            NeoForge.EVENT_BUS.addListener(CertainQuestingAdditionsNeoForge::onRegisterCommands);
        }
    }

    private static void onRegisterCommands(RegisterClientCommandsEvent event) {
        event.getDispatcher().register(LiteralArgumentBuilder.<ServerCommandSource>literal("certain-questing-additions").then(LiteralArgumentBuilder.<ServerCommandSource>literal("config").executes((stack) -> {
            QuestAnimationsConfig.openSettings(null);
            return 1;
        })));
    }
}
//?} else {
/*public final class CertainQuestingAdditionsNeoForge {}
*///?}