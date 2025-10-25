package ru.hollowhorizon.additions.questing.fabric.client;

//? if fabric {
/*import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import ru.hollowhorizon.additions.questing.config.QuestAnimationsConfig;

public final class CertainQuestingAdditionsFabricClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, dedicated) -> {
            dispatcher.register(LiteralArgumentBuilder.<FabricClientCommandSource>literal("certain-questing-additions").then(LiteralArgumentBuilder.<FabricClientCommandSource>literal("config").executes((stack) -> {
                QuestAnimationsConfig.openSettings(null);
                return 1;
            })));
        });
    }
}
*///?} else {
public final class CertainQuestingAdditionsFabricClient {}
//?}