package ru.hollowhorizon.additions.questing.client;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.resource.Resource;
import net.minecraft.util.Identifier;
import ru.hollowhorizon.additions.questing.CertainQuestingAdditions;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public final class ApngTextureManager {
    private static final Map<Identifier, AnimatedTexture> ANIMATED = new ConcurrentHashMap<>();
    private static final Set<Identifier> UNSUPPORTED = ConcurrentHashMap.newKeySet();

    private ApngTextureManager() {
    }

    public static boolean bindIfAnimated(Identifier textureId) {
        if (!isAPngPath(textureId) || UNSUPPORTED.contains(textureId)) {
            return false;
        }

        AnimatedTexture animated = ANIMATED.get(textureId);
        if (animated == null) {
            animated = load(textureId);
            if (animated == null) {
                UNSUPPORTED.add(textureId);
                return false;
            }
            ANIMATED.put(textureId, animated);
        }

        RenderSystem.setShaderTexture(0, animated.currentFrame());
        return true;
    }

    private static AnimatedTexture load(Identifier textureId) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null) {
            return null;
        }

        try {
            Resource resource = client.getResourceManager().getResource(textureId).orElse(null);
            if (resource == null) {
                return null;
            }

            ApngParser.ParsedApng parsed;
            try (InputStream stream = resource.getInputStream()) {
                parsed = ApngParser.parse(stream);
            }

            if (parsed == null || parsed.frames().isEmpty()) {
                return null;
            }

            List<Identifier> frameTextures = new ArrayList<>(parsed.frames().size());
            long[] cumulativeDurations = new long[parsed.frames().size()];
            long sum = 0L;

            for (int i = 0; i < parsed.frames().size(); i++) {
                ApngParser.RenderedFrame frame = parsed.frames().get(i);
                Identifier dynamicId = Identifier.of(
                        CertainQuestingAdditions.MOD_ID,
                        "apng/" + sanitize(textureId.toString()) + "/" + i
                );
                NativeImage nativeImage = toNativeImage(frame.image());
                client.getTextureManager().registerTexture(dynamicId, new NativeImageBackedTexture(nativeImage));
                frameTextures.add(dynamicId);

                sum += Math.max(1, frame.delayMillis());
                cumulativeDurations[i] = sum;
            }

            return new AnimatedTexture(frameTextures, cumulativeDurations, Math.max(1L, sum));
        } catch (Throwable t) {
            CertainQuestingAdditions.LOGGER.warn("Failed to load APNG texture {}", textureId, t);
            return null;
        }
    }

    private static NativeImage toNativeImage(BufferedImage image) throws IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        ImageIO.write(image, "png", output);
        byte[] bytes = output.toByteArray();
        return NativeImage.read(new ByteArrayInputStream(bytes));
    }

    private static boolean isAPngPath(Identifier textureId) {
        String path = textureId.getPath().toLowerCase(Locale.ROOT);
        return path.endsWith(".apng.png");
    }

    private static String sanitize(String value) {
        StringBuilder out = new StringBuilder(value.length());
        for (int i = 0; i < value.length(); i++) {
            char c = Character.toLowerCase(value.charAt(i));
            if ((c >= 'a' && c <= 'z') || (c >= '0' && c <= '9') || c == '/' || c == '_' || c == '-' || c == '.') {
                out.append(c);
            } else if (c == ':') {
                out.append('/');
            } else {
                out.append('_');
            }
        }
        return out.toString();
    }

    private record AnimatedTexture(List<Identifier> frameTextures, long[] cumulativeDurations, long totalDurationMs) {
        private Identifier currentFrame() {
            if (frameTextures.isEmpty()) {
                throw new IllegalStateException("APNG frame list is empty");
            }

            long now = System.currentTimeMillis();
            long t = Math.floorMod(now, totalDurationMs);
            for (int i = 0; i < cumulativeDurations.length; i++) {
                if (t < cumulativeDurations[i]) {
                    return frameTextures.get(i);
                }
            }

            return frameTextures.get(frameTextures.size() - 1);
        }
    }
}
