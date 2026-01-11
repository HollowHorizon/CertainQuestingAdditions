package ru.hollowhorizon.additions.questing.forge;

//? if forge {
/*import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.client.gl.ShaderProgram;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.util.Identifier;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.client.event.RegisterClientCommandsEvent;
import net.minecraftforge.client.event.RegisterShadersEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.loading.FMLEnvironment;
import ru.hollowhorizon.additions.questing.CertainQuestingAdditions;
import dev.architectury.platform.forge.EventBuses;
import net.minecraftforge.fml.common.Mod;
import ru.hollowhorizon.additions.questing.config.QuestAnimationsConfig;
import ru.hollowhorizon.additions.questing.registry.ModShaders;

import java.io.IOException;

@Mod(CertainQuestingAdditions.MOD_ID)
public final class CertainQuestingAdditionsForge {
    public CertainQuestingAdditionsForge() {
        var modBus = FMLJavaModLoadingContext.get().getModEventBus();
        // Submit our event bus to let Architectury API register our content on the right time.
        EventBuses.registerModEventBus(CertainQuestingAdditions.MOD_ID, modBus);

        // Run our common setup.
        CertainQuestingAdditions.init();

        if (FMLEnvironment.dist == Dist.CLIENT) {
            MinecraftForge.EVENT_BUS.addListener(CertainQuestingAdditionsForge::onRegisterCommands);
            modBus.addListener((RegisterShadersEvent event) -> {
                try {
                    event.registerShader(new ShaderProgram(event.getResourceProvider(), Identifier.parse("certain_questing_additions:custom_background"), VertexFormats.POSITION_TEXTURE), shader -> ModShaders.background = shader);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
        }
    }

    private static void onRegisterCommands(RegisterClientCommandsEvent event) {
        event.getDispatcher().register(LiteralArgumentBuilder.<ServerCommandSource>literal("certain-questing-additions").then(LiteralArgumentBuilder.<ServerCommandSource>literal("config").executes((stack) -> {
            QuestAnimationsConfig.openSettings(null);
            return 1;
        })));
    }
}
*///?} else {
public final class CertainQuestingAdditionsForge {}
//?}