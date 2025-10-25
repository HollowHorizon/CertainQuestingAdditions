package ru.hollowhorizon.additions.questing.forge;

//? if forge {
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterClientCommandsEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.loading.FMLEnvironment;
import ru.hollowhorizon.additions.questing.CertainQuestingAdditions;
import dev.architectury.platform.forge.EventBuses;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import ru.hollowhorizon.additions.questing.config.QuestAnimationsConfig;

@Mod(CertainQuestingAdditions.MOD_ID)
public final class CertainQuestingAdditionsForge {
    public CertainQuestingAdditionsForge() {
        // Submit our event bus to let Architectury API register our content on the right time.
        EventBuses.registerModEventBus(CertainQuestingAdditions.MOD_ID, FMLJavaModLoadingContext.get().getModEventBus());

        // Run our common setup.
        CertainQuestingAdditions.init();

        if (FMLEnvironment.dist == Dist.CLIENT) {
            MinecraftForge.EVENT_BUS.addListener(CertainQuestingAdditionsForge::onRegisterCommands);
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
/*public final class CertainQuestingAdditionsForge {}
*///?}