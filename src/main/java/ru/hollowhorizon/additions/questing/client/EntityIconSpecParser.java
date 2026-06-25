package ru.hollowhorizon.additions.questing.client;

import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

final class EntityIconSpecParser {
    private EntityIconSpecParser() {
    }

    static Optional<EntityIconSpec> parse(String value) {
        if (value == null || value.isBlank()) {
            return Optional.empty();
        }

        ParsedBody body = ParsedBody.parse(stripEntityPrefix(value.trim()));
        if (body == null) {
            return Optional.empty();
        }

        Identifier entityId = Identifier.tryParse(body.entityId());
        if (entityId == null) {
            return Optional.empty();
        }

        ParsedOptions options = ParsedOptions.parse(body.options(), EntityIconSpec.PLAYER_ENTITY_ID.equals(entityId));
        if (options == null) {
            return Optional.empty();
        }

        String nbt = body.nbt().isBlank() ? options.nbt() : body.nbt();
        return Optional.of(new EntityIconSpec(
                entityId,
                options.rotationEnabled(),
                options.lookAtCursorEnabled(),
                options.skinName(),
                options.playerName(),
                options.playerNameVisible(),
                options.playerUuid(),
                nbt,
                options.nbtValues()
        ));
    }

    static Optional<UUID> parseUuid(String value) {
        String trimmed = stripOuterQuotes(value.trim());
        if (trimmed.length() == 32 && trimmed.chars().allMatch(EntityIconSpecParser::isHexDigit)) {
            trimmed = trimmed.substring(0, 8) + "-"
                    + trimmed.substring(8, 12) + "-"
                    + trimmed.substring(12, 16) + "-"
                    + trimmed.substring(16, 20) + "-"
                    + trimmed.substring(20);
        }

        try {
            return Optional.of(UUID.fromString(trimmed));
        } catch (IllegalArgumentException ignored) {
            return Optional.empty();
        }
    }

    static String stripOuterQuotes(String value) {
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

    private static String stripEntityPrefix(String value) {
        return value.startsWith(EntityIcon.PREFIX) ? value.substring(EntityIcon.PREFIX.length()) : value;
    }

    private static boolean isHexDigit(int character) {
        return character >= '0' && character <= '9'
                || character >= 'a' && character <= 'f'
                || character >= 'A' && character <= 'F';
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

    private record ParsedBody(String entityId, List<String> options, String nbt) {
        static ParsedBody parse(String input) {
            int paramsStart = input.indexOf('[');
            int nbtStart = input.indexOf('{');
            int firstExtra = firstPresent(paramsStart, nbtStart);
            String entityId = firstExtra < 0 ? input.trim() : input.substring(0, firstExtra).trim();
            if (entityId.isBlank()) {
                return null;
            }

            List<String> options = List.of();
            String nbt = "";
            int index = entityId.length();
            while (index < input.length()) {
                char current = input.charAt(index);
                if (Character.isWhitespace(current)) {
                    index++;
                    continue;
                }

                if (current == '[') {
                    int end = findClosing(input, index, '[', ']');
                    if (end < 0) {
                        return null;
                    }
                    options = splitTopLevel(input.substring(index + 1, end));
                    index = end + 1;
                } else if (current == '{') {
                    int end = findClosing(input, index, '{', '}');
                    if (end < 0) {
                        return null;
                    }
                    nbt = input.substring(index, end + 1);
                    index = end + 1;
                } else {
                    return null;
                }
            }

            return new ParsedBody(entityId, options, nbt);
        }
    }

    private record ParsedOptions(
            boolean rotationEnabled,
            boolean lookAtCursorEnabled,
            String skinName,
            String playerName,
            boolean playerNameVisible,
            UUID playerUuid,
            String nbt,
            Map<String, String> nbtValues
    ) {
        static ParsedOptions parse(List<String> entries, boolean playerEntity) {
            boolean rotationEnabled = EntityIconSpec.DEFAULT_ROTATION;
            boolean lookAtCursorEnabled = EntityIconSpec.DEFAULT_LOOK_AT_CURSOR;
            boolean playerNameVisible = EntityIconSpec.DEFAULT_PLAYER_NAME_VISIBLE;
            String skinName = "";
            String playerName = "";
            UUID playerUuid = null;
            String nbt = "";
            Map<String, String> nbtValues = new LinkedHashMap<>();

            for (String entry : entries) {
                if (entry.isBlank()) {
                    continue;
                }

                int separator = entry.indexOf('=');
                if (separator <= 0) {
                    return null;
                }

                String key = entry.substring(0, separator).trim();
                String value = entry.substring(separator + 1).trim();
                String normalizedKey = normalizeKey(key);

                if (isRotationKey(normalizedKey)) {
                    Optional<Boolean> parsed = parseBoolean(value);
                    if (parsed.isEmpty()) {
                        return null;
                    }
                    rotationEnabled = parsed.get();
                } else if (isLookAtCursorKey(normalizedKey)) {
                    Optional<Boolean> parsed = parseBoolean(value);
                    if (parsed.isEmpty()) {
                        return null;
                    }
                    lookAtCursorEnabled = parsed.get();
                } else if (playerEntity && isSkinNameKey(normalizedKey)) {
                    skinName = stripOuterQuotes(value);
                } else if (playerEntity && isPlayerNameKey(normalizedKey)) {
                    playerName = stripOuterQuotes(value);
                } else if (playerEntity && isPlayerNameVisibleKey(normalizedKey)) {
                    Optional<Boolean> parsed = parseBoolean(value);
                    if (parsed.isEmpty()) {
                        return null;
                    }
                    playerNameVisible = parsed.get();
                } else if (playerEntity && isPlayerUuidKey(normalizedKey)) {
                    Optional<UUID> parsed = parseUuid(value);
                    if (parsed.isPresent()) {
                        playerUuid = parsed.get();
                    }
                } else if ("nbt".equals(normalizedKey)) {
                    nbt = value;
                } else {
                    nbtValues.put(key, value);
                }
            }

            return new ParsedOptions(rotationEnabled, lookAtCursorEnabled, skinName, playerName, playerNameVisible, playerUuid, nbt, nbtValues);
        }

        private static boolean isRotationKey(String key) {
            return "rotate".equals(key) || "rotation".equals(key) || "auto_rotate".equals(key);
        }

        private static boolean isLookAtCursorKey(String key) {
            return "look".equals(key) || "look_at_cursor".equals(key) || "lookatcursor".equals(key);
        }

        private static boolean isSkinNameKey(String key) {
            return "skin".equals(key) || "nick".equals(key) || "name".equals(key) || "username".equals(key) || "player".equals(key);
        }

        private static boolean isPlayerNameKey(String key) {
            return "display_name".equals(key)
                    || "displayname".equals(key)
                    || "player_name".equals(key)
                    || "nickname".equals(key)
                    || "nick_name".equals(key)
                    || "label".equals(key)
                    || "name_tag".equals(key)
                    || "nametag".equals(key);
        }

        private static boolean isPlayerNameVisibleKey(String key) {
            return "show_name".equals(key)
                    || "show_nickname".equals(key)
                    || "show_nick".equals(key)
                    || "name_visible".equals(key)
                    || "nametag_visible".equals(key)
                    || "show_nametag".equals(key)
                    || "show_name_tag".equals(key);
        }

        private static boolean isPlayerUuidKey(String key) {
            return "uuid".equals(key) || "player_uuid".equals(key) || "profile_uuid".equals(key);
        }

        private static String normalizeKey(String key) {
            return key.trim().replace('-', '_').toLowerCase(Locale.ROOT);
        }

        private static Optional<Boolean> parseBoolean(String value) {
            return switch (stripOuterQuotes(value).toLowerCase(Locale.ROOT)) {
                case "true", "1", "yes", "on" -> Optional.of(true);
                case "false", "0", "no", "off" -> Optional.of(false);
                default -> Optional.empty();
            };
        }
    }

    private static int firstPresent(int first, int second) {
        if (first < 0) {
            return second;
        }
        if (second < 0) {
            return first;
        }
        return Math.min(first, second);
    }

    private static int findClosing(String input, int start, char open, char close) {
        int depth = 0;
        char quote = 0;
        boolean escapedQuote = false;
        boolean escaped = false;

        for (int i = start; i < input.length(); i++) {
            char current = input.charAt(i);
            if (quote != 0) {
                if (escapedQuote && current == '\\' && i + 1 < input.length() && input.charAt(i + 1) == quote) {
                    quote = 0;
                    escapedQuote = false;
                    i++;
                } else if (escaped) {
                    escaped = false;
                } else if (current == '\\') {
                    escaped = true;
                } else if (current == quote) {
                    quote = 0;
                }
                continue;
            }

            if (current == '\\' && i + 1 < input.length() && isQuote(input.charAt(i + 1))) {
                quote = input.charAt(i + 1);
                escapedQuote = true;
                i++;
            } else if (isQuote(current)) {
                quote = current;
                escapedQuote = false;
            } else if (current == open) {
                depth++;
            } else if (current == close) {
                depth--;
                if (depth == 0) {
                    return i;
                }
            }
        }

        return -1;
    }

    private static List<String> splitTopLevel(String input) {
        List<String> result = new ArrayList<>();
        int start = 0;
        int squareDepth = 0;
        int compoundDepth = 0;
        char quote = 0;
        boolean escapedQuote = false;
        boolean escaped = false;

        for (int i = 0; i < input.length(); i++) {
            char current = input.charAt(i);
            if (quote != 0) {
                if (escapedQuote && current == '\\' && i + 1 < input.length() && input.charAt(i + 1) == quote) {
                    quote = 0;
                    escapedQuote = false;
                    i++;
                } else if (escaped) {
                    escaped = false;
                } else if (current == '\\') {
                    escaped = true;
                } else if (current == quote) {
                    quote = 0;
                }
                continue;
            }

            if (current == '\\' && i + 1 < input.length() && isQuote(input.charAt(i + 1))) {
                quote = input.charAt(i + 1);
                escapedQuote = true;
                i++;
            } else if (isQuote(current)) {
                quote = current;
                escapedQuote = false;
            } else if (current == '[') {
                squareDepth++;
            } else if (current == ']') {
                squareDepth--;
            } else if (current == '{') {
                compoundDepth++;
            } else if (current == '}') {
                compoundDepth--;
            } else if (current == ',' && squareDepth == 0 && compoundDepth == 0) {
                result.add(input.substring(start, i).trim());
                start = i + 1;
            }
        }

        result.add(input.substring(start).trim());
        return result;
    }

    private static boolean isQuote(char character) {
        return character == '"' || character == '\'';
    }
}
