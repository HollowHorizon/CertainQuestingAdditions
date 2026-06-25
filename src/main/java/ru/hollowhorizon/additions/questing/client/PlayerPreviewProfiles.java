package ru.hollowhorizon.additions.questing.client;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.minecraft.MinecraftSessionService;
//? if >= 1.21.1 {
import com.mojang.authlib.yggdrasil.ProfileResult;
import net.minecraft.client.util.DefaultSkinHelper;
import net.minecraft.client.util.SkinTextures;
//?} else {
/*import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.util.DefaultSkinHelper;
import net.minecraft.util.Identifier;
*///?}
import net.minecraft.client.MinecraftClient;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public final class PlayerPreviewProfiles {
    private static final String PROFILE_BY_NAME_URL = "https://api.mojang.com/users/profiles/minecraft/";
    private static final Duration REQUEST_TIMEOUT = Duration.ofSeconds(8);
    private static final HttpClient HTTP_CLIENT = HttpClient.newBuilder()
            .connectTimeout(REQUEST_TIMEOUT)
            .build();
    private static final ExecutorService RESOLVER_EXECUTOR = Executors.newFixedThreadPool(2, task -> {
        Thread thread = new Thread(task, "CQA Player Profile Resolver");
        thread.setDaemon(true);
        return thread;
    });
    private static final ConcurrentMap<String, CompletableFuture<Optional<GameProfile>>> PROFILE_CACHE = new ConcurrentHashMap<>();

    private PlayerPreviewProfiles() {
    }

    public static GameProfile baseProfile(EntityIconSpec spec, String fallbackName) {
        if (spec.usesCurrentClientPlayerSkin()) {
            MinecraftClient client = MinecraftClient.getInstance();
            if (client != null && client.player != null) {
                return client.player.getGameProfile();
            }
        }

        return skinProfile(spec, fallbackName);
    }

    //? if >= 1.21.1 {
    public static SkinTextures skinTextures(EntityIconSpec spec, String fallbackName) {
        MinecraftClient client = MinecraftClient.getInstance();
        GameProfile profile = skinProfile(spec, fallbackName);
        if (client == null) {
            return DefaultSkinHelper.getSkinTextures(profile);
        }

        return client.getSkinProvider().getSkinTextures(profile);
    }
    //?} else {
    /*public static Identifier skinTexture(EntityIconSpec spec, String fallbackName) {
        MinecraftClient client = MinecraftClient.getInstance();
        GameProfile profile = skinProfile(spec, fallbackName);
        if (client == null) {
            return DefaultSkinHelper.getTexture(profile.getId());
        }

        if (profile.getProperties().containsKey("textures")) {
            return client.getSkinProvider().loadSkin(profile);
        }

        Identifier skinId = AbstractClientPlayerEntity.getSkinId(profile.getName());
        AbstractClientPlayerEntity.loadSkin(skinId, profile.getName());
        return skinId;
    }

    public static String skinModel(EntityIconSpec spec, String fallbackName) {
        return DefaultSkinHelper.getModel(skinProfile(spec, fallbackName).getId());
    }
    *///?}

    private static GameProfile skinProfile(EntityIconSpec spec, String fallbackName) {
        String name = spec.skinName(fallbackName);
        Optional<UUID> uuid = spec.playerUuid();
        Optional<GameProfile> resolved = uuid.isPresent()
                ? cachedProfile("uuid:" + uuid.get(), sessionService -> fetchProfile(uuid.get(), name, sessionService))
                : cachedProfile("name:" + name.toLowerCase(Locale.ROOT), sessionService -> fetchProfile(name, sessionService));

        return resolved.orElseGet(() -> new GameProfile(uuid.orElseGet(() -> offlineUuid(name)), name));
    }

    private static Optional<GameProfile> cachedProfile(String key, ProfileSupplier supplier) {
        MinecraftSessionService sessionService = sessionService();
        if (sessionService == null) {
            return Optional.empty();
        }

        CompletableFuture<Optional<GameProfile>> future = PROFILE_CACHE.computeIfAbsent(key, ignored ->
                CompletableFuture.supplyAsync(() -> supplier.get(sessionService), RESOLVER_EXECUTOR)
                        .exceptionally(error -> Optional.empty())
        );
        return future.getNow(Optional.empty());
    }

    private static Optional<GameProfile> fetchProfile(String name, MinecraftSessionService sessionService) {
        return fetchNameProfile(name).flatMap(profile -> fillProfile(sessionService, new GameProfile(profile.uuid(), profile.name())));
    }

    private static Optional<GameProfile> fetchProfile(UUID uuid, String name, MinecraftSessionService sessionService) {
        return fillProfile(sessionService, new GameProfile(uuid, name));
    }

    private static Optional<NameProfile> fetchNameProfile(String name) {
        HttpRequest request = HttpRequest.newBuilder(profileByNameUri(name))
                .timeout(REQUEST_TIMEOUT)
                .GET()
                .build();

        try {
            HttpResponse<String> response = HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            if (response.statusCode() != 200) {
                return Optional.empty();
            }

            return parseNameProfile(name, response.body());
        } catch (IOException ignored) {
            return Optional.empty();
        } catch (InterruptedException ignored) {
            Thread.currentThread().interrupt();
            return Optional.empty();
        }
    }

    private static Optional<NameProfile> parseNameProfile(String requestedName, String body) {
        JsonElement root = JsonParser.parseString(body);
        if (!root.isJsonObject()) {
            return Optional.empty();
        }

        JsonObject object = root.getAsJsonObject();
        String id = stringValue(object, "id");
        String name = stringValue(object, "name");
        if (name.isBlank()) {
            name = requestedName;
        }

        String resolvedName = name;
        return EntityIconSpec.parseUuid(id).map(uuid -> new NameProfile(uuid, resolvedName));
    }

    private static Optional<GameProfile> fillProfile(MinecraftSessionService sessionService, GameProfile profile) {
        //? if >= 1.21.1 {
        ProfileResult result = sessionService.fetchProfile(profile.getId(), true);
        if (result == null || result.profile() == null) {
            return Optional.empty();
        }

        return Optional.of(result.profile());
        //?} else {
        /*GameProfile filledProfile = sessionService.fillProfileProperties(profile, true);
        return Optional.ofNullable(filledProfile);
        *///?}
    }

    private static MinecraftSessionService sessionService() {
        MinecraftClient client = MinecraftClient.getInstance();
        return client == null ? null : client.getSessionService();
    }

    private static URI profileByNameUri(String name) {
        return URI.create(PROFILE_BY_NAME_URL + URLEncoder.encode(name, StandardCharsets.UTF_8));
    }

    private static String stringValue(JsonObject object, String key) {
        JsonElement element = object.get(key);
        return element == null || element.isJsonNull() ? "" : element.getAsString();
    }

    private static UUID offlineUuid(String name) {
        return UUID.nameUUIDFromBytes(("OfflinePlayer:" + name).getBytes(StandardCharsets.UTF_8));
    }

    @FunctionalInterface
    private interface ProfileSupplier {
        Optional<GameProfile> get(MinecraftSessionService sessionService);
    }

    private record NameProfile(UUID uuid, String name) {
    }
}
