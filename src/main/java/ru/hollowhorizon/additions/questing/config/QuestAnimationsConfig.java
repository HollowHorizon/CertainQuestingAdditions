package ru.hollowhorizon.additions.questing.config;

import dev.ftb.mods.ftblibrary.config.ConfigGroup;
import dev.ftb.mods.ftblibrary.config.ui.EditConfigScreen;
import dev.ftb.mods.ftblibrary.snbt.config.*;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;

public interface QuestAnimationsConfig {
    SNBTConfig CONFIG = SNBTConfig.create("certain_questing_additions");

    SNBTConfig ANIMATIONS = CONFIG.addGroup("animations");

    BooleanValue QUEST_HOVER = ANIMATIONS.addBoolean("quest_hover", true);
    BooleanValue PANEL_BUTTON_HOVER = ANIMATIONS.addBoolean("panel_button_hover", true);
    BooleanValue PANEL_ANIMATION = ANIMATIONS.addBoolean("panel_animation", true);
    BooleanValue SMOOTH_SCROLLING = ANIMATIONS.addBoolean("smooth_scrolling", true);
    //? if <= 1.20.1 {
    BooleanValue CHAPTER_PANEL = ANIMATIONS.addBoolean("smooth_chapter_panel", true);
    //?}

    SNBTConfig APPEARANCE = CONFIG.addGroup("appearance");

    BooleanValue SMOOTH_ANIMATIONS = APPEARANCE.addBoolean("smooth_animations", true);
    IntValue BUTTON_HOVER_GLOW_ALPHA = APPEARANCE.addInt("button_hover_glow_alpha", 40)
            .range(0, 255);
    IntValue QUEST_HOVER_GLOW_ALPHA = APPEARANCE.addInt("quest_hover_glow_alpha", 100)
            .range(0, 255);

    static void openSettings(final Screen screen) {
        MinecraftClient.getInstance().send(() -> {
            ConfigGroup group = new ConfigGroup("certain_questing_additions", (accepted) -> {
                if (accepted) {
                    saveConfig();
                }

                MinecraftClient.getInstance().setScreen(screen);
            });
            CONFIG.createClientConfig(group);
            EditConfigScreen gui = new EditConfigScreen(group) {
                public boolean doesGuiPauseGame() {
                    if(screen == null) return false;
                    return screen.shouldPause();
                }
            };
            gui.openGui();
        });
    }

    static void init() {
        ConfigUtil.loadDefaulted(CONFIG, ConfigUtil.LOCAL_DIR.resolve("certain_questing_additions"), "certain_questing_additions", "client-config.snbt");
    }

    static void saveConfig() {
        CONFIG.save(ConfigUtil.LOCAL_DIR.resolve("certain_questing_additions").resolve("client-config.snbt"));
    }

}
