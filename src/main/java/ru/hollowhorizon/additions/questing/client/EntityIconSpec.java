package ru.hollowhorizon.additions.questing.client;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.StringNbtReader;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.StringJoiner;
import java.util.UUID;

public final class EntityIconSpec {
    public static final Identifier PLAYER_ENTITY_ID = Identifier.tryParse("minecraft:player");

    static final boolean DEFAULT_ROTATION = false;
    static final boolean DEFAULT_LOOK_AT_CURSOR = false;
    static final boolean DEFAULT_PLAYER_NAME_VISIBLE = true;
    private static final String DEFAULT_PLAYER_NAME = "Player";

    private final Identifier entityId;
    private final boolean rotationEnabled;
    private final boolean lookAtCursorEnabled;
    private final String skinName;
    private final String playerName;
    private final boolean playerNameVisible;
    private final UUID playerUuid;
    private final String nbt;
    private final Map<String, String> nbtValues;

    public EntityIconSpec(Identifier entityId) {
        this(entityId, DEFAULT_ROTATION, DEFAULT_LOOK_AT_CURSOR, "", "", DEFAULT_PLAYER_NAME_VISIBLE, null, "", Map.of());
    }

    EntityIconSpec(
            Identifier entityId,
            boolean rotationEnabled,
            boolean lookAtCursorEnabled,
            String skinName,
            String playerName,
            boolean playerNameVisible,
            UUID playerUuid,
            String nbt,
            Map<String, String> nbtValues
    ) {
        this.entityId = entityId;
        this.rotationEnabled = rotationEnabled;
        this.lookAtCursorEnabled = lookAtCursorEnabled;
        this.skinName = skinName == null ? "" : skinName;
        this.playerName = playerName == null ? "" : playerName;
        this.playerNameVisible = playerNameVisible;
        this.playerUuid = playerUuid;
        this.nbt = nbt == null ? "" : nbt;
        this.nbtValues = Collections.unmodifiableMap(new LinkedHashMap<>(nbtValues));
    }

    public static Optional<EntityIconSpec> parse(String value) {
        return EntityIconSpecParser.parse(value);
    }

    public Identifier entityId() {
        return entityId;
    }

    public boolean rotationEnabled() {
        return rotationEnabled;
    }

    public boolean lookAtCursorEnabled() {
        return lookAtCursorEnabled;
    }

    public boolean isPlayer() {
        return PLAYER_ENTITY_ID.equals(entityId);
    }

    public boolean usesCurrentClientPlayer() {
        return isPlayer()
                && skinName.isBlank()
                && playerName.isBlank()
                && playerNameVisible == DEFAULT_PLAYER_NAME_VISIBLE
                && playerUuid == null
                && nbt.isBlank()
                && nbtValues.isEmpty();
    }

    public boolean usesCurrentClientPlayerSkin() {
        return isPlayer()
                && skinName.isBlank()
                && playerUuid == null
                && nbt.isBlank()
                && nbtValues.isEmpty();
    }

    public String skinName() {
        return skinName;
    }

    public String skinName(String fallback) {
        if (!skinName.isBlank()) {
            return skinName;
        }

        return fallback == null || fallback.isBlank() ? DEFAULT_PLAYER_NAME : fallback;
    }

    public String playerName() {
        return playerName;
    }

    public String playerName(String fallback) {
        if (!playerName.isBlank()) {
            return playerName;
        }

        return skinName(fallback);
    }

    public boolean playerNameVisible() {
        return playerNameVisible;
    }

    public Optional<UUID> playerUuid() {
        return Optional.ofNullable(playerUuid);
    }

    public EntityIconSpec withRotationEnabled(boolean enabled) {
        return new EntityIconSpec(entityId, enabled, lookAtCursorEnabled, skinName, playerName, playerNameVisible, playerUuid, nbt, nbtValues);
    }

    public EntityIconSpec withLookAtCursorEnabled(boolean enabled) {
        return new EntityIconSpec(entityId, rotationEnabled, enabled, skinName, playerName, playerNameVisible, playerUuid, nbt, nbtValues);
    }

    public EntityIconSpec withPlayerSkinName(String name) {
        return new EntityIconSpec(entityId, rotationEnabled, lookAtCursorEnabled, name, playerName, playerNameVisible, playerUuid, nbt, nbtValues);
    }

    public EntityIconSpec withPlayerName(String name) {
        return new EntityIconSpec(entityId, rotationEnabled, lookAtCursorEnabled, skinName, name, playerNameVisible, playerUuid, nbt, nbtValues);
    }

    public EntityIconSpec withPlayerNameVisible(boolean visible) {
        return new EntityIconSpec(entityId, rotationEnabled, lookAtCursorEnabled, skinName, playerName, visible, playerUuid, nbt, nbtValues);
    }

    public EntityIconSpec withDisplayOptionsFrom(EntityIconSpec source) {
        return new EntityIconSpec(entityId, source.rotationEnabled, source.lookAtCursorEnabled, skinName, playerName, playerNameVisible, playerUuid, nbt, nbtValues);
    }

    public boolean hasValidNbt() {
        return createNbt().isPresent() || nbtBody().isBlank();
    }

    public Optional<NbtCompound> createNbt() {
        String body = nbtBody();
        if (body.isBlank()) {
            return Optional.empty();
        }

        try {
            return Optional.of(StringNbtReader.parse("{" + body + "}"));
        } catch (CommandSyntaxException ignored) {
            return Optional.empty();
        }
    }

    @Override
    public String toString() {
        List<String> parameters = new ArrayList<>();
        if (!skinName.isBlank()) {
            parameters.add("skin=" + formatValue(skinName));
        }
        if (!playerName.isBlank()) {
            parameters.add("display_name=" + formatValue(playerName));
        }
        if (playerNameVisible != DEFAULT_PLAYER_NAME_VISIBLE) {
            parameters.add("show_name=" + playerNameVisible);
        }
        if (playerUuid != null) {
            parameters.add("uuid=" + playerUuid);
        }
        if (rotationEnabled) {
            parameters.add("rotate=true");
        }
        if (lookAtCursorEnabled) {
            parameters.add("look_at_cursor=true");
        }

        nbtValues.forEach((key, value) -> parameters.add(key + "=" + value));
        if (!nbt.isBlank()) {
            parameters.add("nbt=" + normalizedNbt());
        }

        if (parameters.isEmpty()) {
            return entityId.toString();
        }

        return entityId + "[" + String.join(",", parameters) + "]";
    }

    private String nbtBody() {
        StringJoiner joiner = new StringJoiner(",");
        String normalized = normalizedNbt();
        if (!normalized.isBlank()) {
            joiner.add(stripCompoundBraces(normalized));
        }
        nbtValues.forEach((key, value) -> joiner.add(key + ":" + value));
        return joiner.toString();
    }

    private String normalizedNbt() {
        return EntityIconSpecParser.stripOuterQuotes(nbt.trim());
    }

    private static String stripCompoundBraces(String value) {
        String trimmed = value.trim();
        if (trimmed.startsWith("{") && trimmed.endsWith("}")) {
            return trimmed.substring(1, trimmed.length() - 1).trim();
        }

        return trimmed;
    }

    private static String formatValue(String value) {
        if (value.chars().allMatch(EntityIconSpec::isSimpleValueCharacter)) {
            return value;
        }

        return "\"" + escape(value) + "\"";
    }

    private static boolean isSimpleValueCharacter(int character) {
        return character >= 'a' && character <= 'z'
                || character >= 'A' && character <= 'Z'
                || character >= '0' && character <= '9'
                || character == '_' || character == '-' || character == '.';
    }

    private static String escape(String value) {
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    private static String stripOuterQuotes(String value) {
        if (value.length() >= 4 && startsWithEscapedQuote(value) && endsWithEscapedQuote(value)) {
            return unescape(value.substring(2, value.length() - 2));
        }

        if (value.length() < 2) {
            return value;
        }

        char first = value.charAt(0);
        char last = value.charAt(value.length() - 1);
        if ((first == '"' && last == '"') || (first == '\'' && last == '\'')) {
            return unescape(value.substring(1, value.length() - 1));
        }

        return unescape(value);
    }

    private static boolean startsWithEscapedQuote(String value) {
        return value.startsWith("\\\"") || value.startsWith("\\'");
    }

    private static boolean endsWithEscapedQuote(String value) {
        return value.endsWith("\\\"") || value.endsWith("\\'");
    }

    private static String unescape(String value) {
        StringBuilder result = new StringBuilder(value.length());
        boolean escaped = false;
        for (int i = 0; i < value.length(); i++) {
            char current = value.charAt(i);
            if (escaped) {
                result.append(current);
                escaped = false;
            } else if (current == '\\') {
                escaped = true;
            } else {
                result.append(current);
            }
        }

        if (escaped) {
            result.append('\\');
        }
        return result.toString();
    }

    public static Optional<UUID> parseUuid(String value) {
        return EntityIconSpecParser.parseUuid(value);
    }
}
