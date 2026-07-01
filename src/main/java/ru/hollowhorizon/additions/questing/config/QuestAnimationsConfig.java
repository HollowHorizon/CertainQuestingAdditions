package ru.hollowhorizon.additions.questing.config;

//? if >= 1.21.11 {
import dev.ftb.mods.ftblibrary.client.config.EditableConfigGroup;
import dev.ftb.mods.ftblibrary.client.config.gui.EditConfigScreen;
import dev.ftb.mods.ftblibrary.config.ConfigUtil;
import dev.ftb.mods.ftblibrary.config.serializer.SNBTConfigSerializer;
import dev.ftb.mods.ftblibrary.config.value.BooleanValue;
import dev.ftb.mods.ftblibrary.config.value.Config;
import dev.ftb.mods.ftblibrary.config.value.IntValue;
//?} else {
/*import dev.ftb.mods.ftblibrary.config.ConfigGroup;
import dev.ftb.mods.ftblibrary.config.ui.EditConfigScreen;
import dev.ftb.mods.ftblibrary.snbt.config.*;
*///?}
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import ru.hollowhorizon.additions.questing.CertainQuestingAdditions;

import java.io.IOException;
import java.nio.file.Path;

public interface QuestAnimationsConfig {
    //? if >= 1.21.11 {
    Config CONFIG = Config.create("certain_questing_additions");

    Config ANIMATIONS = CONFIG.addGroup("animations");
    //?} else {
    /*SNBTConfig CONFIG = SNBTConfig.create("certain_questing_additions");

    SNBTConfig ANIMATIONS = CONFIG.addGroup("animations");
    *///?}

    BooleanValue QUEST_HOVER = ANIMATIONS.addBoolean("quest_hover", true);
    BooleanValue PANEL_BUTTON_HOVER = ANIMATIONS.addBoolean("panel_button_hover", true);
    BooleanValue PANEL_ANIMATION = ANIMATIONS.addBoolean("panel_animation", true);
    BooleanValue SMOOTH_SCROLLING = ANIMATIONS.addBoolean("smooth_scrolling", true);

    //? if <= 1.20.1 {
    /*BooleanValue CHAPTER_PANEL = ANIMATIONS.addBoolean("smooth_chapter_panel", true);
    *///?}

    //? if >= 1.21.11 {
    Config APPEARANCE = CONFIG.addGroup("appearance");
    //?} else {
    /*SNBTConfig APPEARANCE = CONFIG.addGroup("appearance");
    *///?}

    BooleanValue SMOOTH_ANIMATIONS = APPEARANCE.addBoolean("smooth_animations", true);
    BooleanValue SHADER_BACKGROUND = APPEARANCE.addBoolean("shader_background", false);
    IntValue BUTTON_HOVER_GLOW_ALPHA = APPEARANCE.addInt("button_hover_glow_alpha", 40)
            .range(0, 255);
    IntValue QUEST_HOVER_GLOW_ALPHA = APPEARANCE.addInt("quest_hover_glow_alpha", 100)
            .range(0, 255);

    static void openSettings(final Screen screen) {
        MinecraftClient.getInstance().send(() -> {
            //? if >= 1.21.11 {
            EditableConfigGroup group = ConfigUtil.makeConfigEditGroup(CONFIG, "certain_questing_additions", false);
            //?} else {
            /*ConfigGroup group = new ConfigGroup("certain_questing_additions", (accepted) -> {
                if (accepted) {
                    saveConfig();
                }

                MinecraftClient.getInstance().setScreen(screen);
            });
            CONFIG.createClientConfig(group);
            *///?}
            EditConfigScreen gui = new EditConfigScreen(group) {
                //? if >= 1.21.11 {
                @Override
                protected void doAccept() {
                    super.doAccept();
                    saveConfig();
                    MinecraftClient.getInstance().setScreen(screen);
                }

                @Override
                protected void doCancel() {
                    super.doCancel();
                    MinecraftClient.getInstance().setScreen(screen);
                }

                @Override
                //?}
                public boolean doesGuiPauseGame() {
                    if(screen == null) return false;
                    return screen.shouldPause();
                }
            };
            gui.openGui();
        });
    }

    static void init() {
        //? if >= 1.21.11 {
        try {
            SNBTConfigSerializer.readFromFile(CONFIG, configPath());
        } catch (IOException e) {
            CertainQuestingAdditions.LOGGER.warn("Failed to read config {}, saving defaults.", configPath(), e);
            saveConfig();
        }
        //?} else {
        /*ConfigUtil.loadDefaulted(CONFIG, ConfigUtil.LOCAL_DIR.resolve("certain_questing_additions"), "certain_questing_additions", "client-config.snbt");
        *///?}
    }

    static void saveConfig() {
        //? if >= 1.21.11 {
        try {
            SNBTConfigSerializer.writeToFile(CONFIG, configPath());
        } catch (IOException e) {
            CertainQuestingAdditions.LOGGER.warn("Failed to save config {}", configPath(), e);
        }
        //?} else {
        /*CONFIG.save(ConfigUtil.LOCAL_DIR.resolve("certain_questing_additions").resolve("client-config.snbt"));
        *///?}
    }

    static Path configPath() {
        return ConfigUtil.LOCAL_DIR.resolve("certain_questing_additions").resolve("client-config.snbt");
    }

}
