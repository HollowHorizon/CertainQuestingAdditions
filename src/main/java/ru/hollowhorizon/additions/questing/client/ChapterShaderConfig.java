package ru.hollowhorizon.additions.questing.client;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import dev.ftb.mods.ftbquests.quest.Chapter;
import net.minecraft.client.MinecraftClient;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import ru.hollowhorizon.additions.questing.CertainQuestingAdditions;
import ru.hollowhorizon.additions.questing.registry.ModShaders;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

public final class ChapterShaderConfig {
    private static final Identifier CONFIG_ID = Identifier.of(CertainQuestingAdditions.MOD_ID, "chapter_shaders.json");
    private static final String CONFIG_CLASSPATH = "assets/" + CertainQuestingAdditions.MOD_ID + "/chapter_shaders.json";

    private static final Object LOCK = new Object();
    private static ResourceManager cachedResourceManager;
    private static Identifier defaultShaderId = ModShaders.DEFAULT_BACKGROUND_ID;
    private static Map<String, Identifier> chapterShaderMap = Collections.emptyMap();

    private ChapterShaderConfig() {
    }

    public static Identifier resolveShaderId(Chapter chapter) {
        ensureLoaded();
        if (chapter == null) {
            return defaultShaderId;
        }

        String codeKey = normalizeChapterKey(chapter.getCodeString());
        return chapterShaderMap.getOrDefault(codeKey, defaultShaderId);
    }

    public static Set<Identifier> discoverShaderIdsForRegistration() {
        LinkedHashSet<Identifier> result = new LinkedHashSet<>();
        result.add(ModShaders.DEFAULT_BACKGROUND_ID);

        MinecraftClient client = MinecraftClient.getInstance();
        if (client != null && client.getResourceManager() != null) {
            try {
                Resource resource = client.getResourceManager().getResource(CONFIG_ID).orElse(null);
                if (resource != null) {
                    try (Reader reader = new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8)) {
                        ParsedConfig parsed = parse(reader, CONFIG_ID.toString());
                        result.add(parsed.defaultShaderId());
                        result.addAll(parsed.chapterShaderMap().values());
                        return result;
                    }
                }
            } catch (Exception e) {
                CertainQuestingAdditions.LOGGER.warn("Failed to read shader registration config from resources {}", CONFIG_ID, e);
            }
        }

        try (InputStream stream = ChapterShaderConfig.class.getClassLoader().getResourceAsStream(CONFIG_CLASSPATH)) {
            if (stream == null) {
                return result;
            }

            try (Reader reader = new InputStreamReader(stream, StandardCharsets.UTF_8)) {
                ParsedConfig parsed = parse(reader, "classpath:" + CONFIG_CLASSPATH);
                result.add(parsed.defaultShaderId());
                result.addAll(parsed.chapterShaderMap().values());
            }
        } catch (Exception e) {
            CertainQuestingAdditions.LOGGER.warn("Failed to read shader registration config from {}", CONFIG_CLASSPATH, e);
        }

        return result;
    }

    private static void ensureLoaded() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null) {
            return;
        }

        ResourceManager manager = client.getResourceManager();
        if (manager == null) {
            return;
        }

        if (manager == cachedResourceManager) {
            return;
        }

        synchronized (LOCK) {
            if (manager == cachedResourceManager) {
                return;
            }

            reload(manager);
            cachedResourceManager = manager;
        }
    }

    private static void reload(ResourceManager manager) {
        try {
            Resource resource = manager.getResource(CONFIG_ID).orElse(null);
            if (resource == null) {
                defaultShaderId = ModShaders.DEFAULT_BACKGROUND_ID;
                chapterShaderMap = Collections.emptyMap();
                return;
            }

            try (Reader reader = new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8)) {
                ParsedConfig parsed = parse(reader, CONFIG_ID.toString());
                defaultShaderId = parsed.defaultShaderId();
                chapterShaderMap = parsed.chapterShaderMap();
            }
        } catch (Exception e) {
            CertainQuestingAdditions.LOGGER.warn("Failed to load chapter shader config {}", CONFIG_ID, e);
            defaultShaderId = ModShaders.DEFAULT_BACKGROUND_ID;
            chapterShaderMap = Collections.emptyMap();
        }
    }

    private static ParsedConfig parse(Reader reader, String source) {
        JsonObject root = JsonParser.parseReader(reader).getAsJsonObject();

        Identifier parsedDefault = parseIdentifier(root.get("default"), source, "default", ModShaders.DEFAULT_BACKGROUND_ID);
        LinkedHashMap<String, Identifier> map = new LinkedHashMap<>();

        JsonElement chaptersElement = root.get("chapters");
        if (chaptersElement != null && chaptersElement.isJsonObject()) {
            JsonObject chapters = chaptersElement.getAsJsonObject();
            for (Map.Entry<String, JsonElement> entry : chapters.entrySet()) {
                Identifier shaderId = parseIdentifier(entry.getValue(), source, "chapters." + entry.getKey(), null);
                if (shaderId != null) {
                    map.put(normalizeChapterKey(entry.getKey()), shaderId);
                }
            }
        }

        map.put("default", parsedDefault);
        return new ParsedConfig(parsedDefault, Collections.unmodifiableMap(map));
    }

    private static Identifier parseIdentifier(JsonElement element, String source, String key, Identifier fallback) {
        if (element == null || !element.isJsonPrimitive()) {
            return fallback;
        }

        String value = element.getAsString();
        Identifier id = Identifier.tryParse(value);
        if (id == null) {
            CertainQuestingAdditions.LOGGER.warn("Invalid shader id '{}' at {} ({})", value, source, key);
            return fallback;
        }

        return id;
    }

    private static String normalizeChapterKey(String key) {
        if (key == null) {
            return "";
        }

        String normalized = key.trim().toLowerCase();
        if (normalized.startsWith("0x")) {
            return normalized.substring(2);
        }
        return normalized;
    }

    private record ParsedConfig(Identifier defaultShaderId, Map<String, Identifier> chapterShaderMap) {
    }
}
