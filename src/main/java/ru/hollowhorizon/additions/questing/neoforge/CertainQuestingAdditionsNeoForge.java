package ru.hollowhorizon.additions.questing.neoforge;

//? if neoforge {

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.client.gl.ShaderProgram;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.util.Identifier;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.client.event.RegisterClientCommandsEvent;
import net.neoforged.neoforge.client.event.RegisterShadersEvent;
import net.neoforged.neoforge.common.NeoForge;
import ru.hollowhorizon.additions.questing.CertainQuestingAdditions;
import ru.hollowhorizon.additions.questing.client.ChapterShaderConfig;
import ru.hollowhorizon.additions.questing.config.QuestAnimationsConfig;
import ru.hollowhorizon.additions.questing.registry.ModShaders;

import java.io.IOException;

@Mod(CertainQuestingAdditions.MOD_ID)
public class CertainQuestingAdditionsNeoForge {
    public CertainQuestingAdditionsNeoForge(IEventBus modBus) {
        // Run our common setup.
        CertainQuestingAdditions.init();

        if (FMLEnvironment.dist == Dist.CLIENT) {
            NeoForge.EVENT_BUS.addListener(CertainQuestingAdditionsNeoForge::onRegisterCommands);
            modBus.addListener((RegisterShadersEvent event) -> {
                for (Identifier shaderId : ChapterShaderConfig.discoverShaderIdsForRegistration()) {
                    try {
                        event.registerShader(new ShaderProgram(event.getResourceProvider(), shaderId, VertexFormats.POSITION_TEXTURE), shader -> ModShaders.register(shaderId, shader));
                    } catch (IOException e) {
                        CertainQuestingAdditions.LOGGER.warn("Failed to register shader {}", shaderId, e);
                    }
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
//?} else {
/*public final class CertainQuestingAdditionsNeoForge {}
 *///?}
