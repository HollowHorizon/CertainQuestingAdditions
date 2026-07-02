package ru.hollowhorizon.additions.questing.fabric.client;

//? if fabric {

/*import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
//? if < 1.21.11 {
import net.fabricmc.fabric.api.client.rendering.v1.CoreShaderRegistrationCallback;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.util.Identifier;
import ru.hollowhorizon.additions.questing.client.ChapterShaderConfig;
//?}
import ru.hollowhorizon.additions.questing.config.QuestAnimationsConfig;
//? if < 1.21.11 {
import ru.hollowhorizon.additions.questing.registry.ModShaders;
//?}

public final class CertainQuestingAdditionsFabricClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, dedicated) -> {
            dispatcher.register(LiteralArgumentBuilder.<FabricClientCommandSource>literal("certain-questing-additions").then(LiteralArgumentBuilder.<FabricClientCommandSource>literal("config").executes((stack) -> {
                QuestAnimationsConfig.openSettings(null);
                return 1;
            })));
        });
        //? if < 1.21.11 {
        CoreShaderRegistrationCallback.EVENT.register((CoreShaderRegistrationCallback.RegistrationContext context) -> {
            for (Identifier shaderId : ChapterShaderConfig.discoverShaderIdsForRegistration()) {
                context.register(shaderId, VertexFormats.POSITION_TEXTURE, shader -> ModShaders.register(shaderId, shader));
            }
        });
        //?}
    }
}
*///?} else {
public final class CertainQuestingAdditionsFabricClient {}
 //?}
