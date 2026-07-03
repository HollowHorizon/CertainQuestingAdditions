package ru.hollowhorizon.additions.questing.client;

//? if >= 1.21.11 {
import dev.ftb.mods.ftblibrary.client.config.EditableConfigGroup;
import dev.ftb.mods.ftblibrary.client.config.editable.EditableConfigValue;
//?} else {
/*import dev.ftb.mods.ftblibrary.config.ConfigGroup;
import dev.ftb.mods.ftblibrary.config.ConfigValue;
*///?}

import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

public final class EntityIconConfigEntries {
    private static final String CONFIG_GROUP_KEY = "certain_questing_additions.entity_attachment.config_group";
    private static final String ENTITY_KEY = "certain_questing_additions.entity_attachment.entity";
    private static final String ENTITY_TINT_KEY = "certain_questing_additions.entity_attachment.tint";
    private static final String ENTITY_ORDER_KEY = "certain_questing_additions.entity_attachment.order";
    private static final String ENTITY_ALPHA_KEY = "certain_questing_additions.entity_attachment.alpha";
    private static final String PLAYER_SKIN_KEY = "certain_questing_additions.entity_attachment.player_skin";
    private static final String PLAYER_NAME_KEY = "certain_questing_additions.entity_attachment.player_name";
    private static final String PLAYER_NAME_VISIBLE_KEY = "certain_questing_additions.entity_attachment.player_name_visible";
    private static final String AUTO_ROTATE_KEY = "certain_questing_additions.entity_attachment.auto_rotate";
    private static final String LOOK_AT_CURSOR_KEY = "certain_questing_additions.entity_attachment.look_at_cursor";

    private EntityIconConfigEntries() {
    }

    //? if >= 1.21.11 {
    public static EditableConfigValue<?> add(EditableConfigGroup group, String id, EntityIcon entityIcon, Consumer<EntityIcon> setter) {
    //?} else {
    /*public static ConfigValue add(ConfigGroup group, String id, EntityIcon entityIcon, Consumer<EntityIcon> setter) {
    *///?}
        setEntityGroupName(group);
        AtomicReference<EntityIcon> current = new AtomicReference<>(entityIcon);
        Consumer<EntityIcon> updatingSetter = icon -> {
            current.set(icon);
            setter.accept(icon);
        };

        String entitySpec = entityIcon.getConfigString();
        //? if >= 1.21.11 {
        EditableConfigValue<?> imageConfig = group.addString(id, entitySpec, value -> setEntityImage(value, current, updatingSetter), entitySpec)
        //?} else {
        /*ConfigValue imageConfig = group.addString(id, entitySpec, value -> setEntityImage(value, current, updatingSetter), entitySpec)
        *///?}
                .setNameKey(ENTITY_KEY);
        if (entityIcon.isPlayer()) {
            group.addString("entity_player_skin", entityIcon.getPlayerSkinName(), value -> updatePlayerSkin(current, updatingSetter, value), "")
                    .setNameKey(PLAYER_SKIN_KEY);
            group.addString("entity_player_name", entityIcon.getPlayerName(), value -> updatePlayerName(current, updatingSetter, value), "")
                    .setNameKey(PLAYER_NAME_KEY);
            group.addBool("entity_player_name_visible", entityIcon.isPlayerNameVisible(), enabled -> updatePlayerNameVisible(current, updatingSetter, enabled), true)
                    .setNameKey(PLAYER_NAME_VISIBLE_KEY);
        }
        group.addBool("entity_auto_rotate", entityIcon.isRotationEnabled(), enabled -> updateRotation(current, updatingSetter, enabled), false)
                .setNameKey(AUTO_ROTATE_KEY);
        group.addBool("entity_look_at_cursor", entityIcon.isLookAtCursorEnabled(), enabled -> updateLookAtCursor(current, updatingSetter, enabled), false)
                .setNameKey(LOOK_AT_CURSOR_KEY);
        return imageConfig;
    }

    //? if >= 1.21.11 {
    public static void setEntityGroupName(EditableConfigGroup group) {
    //?} else {
    /*public static void setEntityGroupName(ConfigGroup group) {
    *///?}
        group.setNameKey(CONFIG_GROUP_KEY);
    }

    //? if >= 1.21.11 {
    public static <T extends EditableConfigValue<?>> T setEntityTintName(T value) {
    //?} else {
    /*public static <T extends ConfigValue> T setEntityTintName(T value) {
    *///?}
        value.setNameKey(ENTITY_TINT_KEY);
        return value;
    }

    //? if >= 1.21.11 {
    public static <T extends EditableConfigValue<?>> T setEntityOrderName(T value) {
    //?} else {
    /*public static <T extends ConfigValue> T setEntityOrderName(T value) {
    *///?}
        value.setNameKey(ENTITY_ORDER_KEY);
        return value;
    }

    //? if >= 1.21.11 {
    public static <T extends EditableConfigValue<?>> T setEntityAlphaName(T value) {
    //?} else {
    /*public static <T extends ConfigValue> T setEntityAlphaName(T value) {
    *///?}
        value.setNameKey(ENTITY_ALPHA_KEY);
        return value;
    }

    private static void setEntityImage(String value, AtomicReference<EntityIcon> current, Consumer<EntityIcon> setter) {
        EntityIcon.fromRegisteredEntityId(value).ifPresent(icon -> {
            EntityIcon updated = value.contains("[") ? icon : icon.withDisplayOptionsFrom(current.get());
            setter.accept(updated);
        });
    }

    private static void updatePlayerSkin(AtomicReference<EntityIcon> current, Consumer<EntityIcon> setter, String name) {
        setter.accept(current.get().withPlayerSkinName(name));
    }

    private static void updatePlayerName(AtomicReference<EntityIcon> current, Consumer<EntityIcon> setter, String name) {
        setter.accept(current.get().withPlayerName(name));
    }

    private static void updatePlayerNameVisible(AtomicReference<EntityIcon> current, Consumer<EntityIcon> setter, Boolean visible) {
        setter.accept(current.get().withPlayerNameVisible(visible));
    }

    private static void updateRotation(AtomicReference<EntityIcon> current, Consumer<EntityIcon> setter, Boolean enabled) {
        setter.accept(current.get().withRotationEnabled(enabled));
    }

    private static void updateLookAtCursor(AtomicReference<EntityIcon> current, Consumer<EntityIcon> setter, Boolean enabled) {
        setter.accept(current.get().withLookAtCursorEnabled(enabled));
    }
}
