package ru.hollowhorizon.additions.questing.client;

import com.mojang.blaze3d.systems.RenderSystem;
import dev.ftb.mods.ftblibrary.icon.Color4I;
import dev.ftb.mods.ftblibrary.icon.Icon;
import dev.ftb.mods.ftblibrary.icon.Icons;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.DiffuseLighting;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.world.World;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import ru.hollowhorizon.additions.questing.mixins.EntityRenderDispatcherAccessor;
//? if forge {
/*import net.minecraftforge.registries.ForgeRegistries;
*///?}

import java.util.Optional;

public final class EntityIcon extends Icon {
    public static final String PREFIX = "entity:";

    private static final int FULL_BRIGHT_LIGHT = LightmapTextureManager.pack(15, 15);
    private static final float MODEL_PADDING = 0.75F;
    private static final float MIN_ENTITY_SIZE = 0.1F;
    private static final long ROTATION_PERIOD_MS = 8000L;
    private static final float FRONT_FACING_YAW = 180F;
    private static final float CURSOR_MAX_YAW = 75F;
    private static final float CURSOR_MAX_PITCH = 35F;
    private static final int PLAYER_NAME_GAP = 2;
    private static final int PLAYER_NAME_COLOR = 0xFFFFFF;
    private static final Vector3f ENTITY_ICON_LIGHT = new Vector3f(0F, -1F, 0F);

    private final EntityIconSpec spec;
    private final Color4I color;

    private Entity cachedEntity;
    private EntityType<?> cachedType;
    private World cachedWorld;

    public EntityIcon(Identifier entityId) {
        this(new EntityIconSpec(entityId), Color4I.WHITE);
    }

    public EntityIcon(EntityIconSpec spec) {
        this(spec, Color4I.WHITE);
    }

    private EntityIcon(EntityIconSpec spec, Color4I color) {
        this.spec = spec;
        this.color = color;
    }

    public static boolean isEntityIconString(String value) {
        return value != null && value.startsWith(PREFIX);
    }

    public static Optional<EntityIcon> fromIconString(String value) {
        if (!isEntityIconString(value)) {
            return Optional.empty();
        }

        return EntityIconSpec.parse(value.substring(PREFIX.length())).map(EntityIcon::new);
    }

    public static Optional<EntityIcon> fromRegisteredEntityId(String value) {
        return EntityIconSpec.parse(stripPrefix(value))
                .filter(spec -> isRegistered(spec.entityId()) || spec.isPlayer())
                .filter(EntityIconSpec::hasValidNbt)
                .map(EntityIcon::new);
    }

    public Identifier getEntityId() {
        return spec.entityId();
    }

    public String getConfigString() {
        return spec.toString();
    }

    public boolean isRotationEnabled() {
        return spec.rotationEnabled();
    }

    public boolean isLookAtCursorEnabled() {
        return spec.lookAtCursorEnabled();
    }

    public boolean isPlayer() {
        return spec.isPlayer();
    }

    public String getPlayerSkinName() {
        return spec.skinName();
    }

    public String getPlayerName() {
        return spec.playerName();
    }

    public boolean isPlayerNameVisible() {
        return spec.playerNameVisible();
    }

    public EntityIcon withRotationEnabled(boolean enabled) {
        return new EntityIcon(spec.withRotationEnabled(enabled), color);
    }

    public EntityIcon withLookAtCursorEnabled(boolean enabled) {
        return new EntityIcon(spec.withLookAtCursorEnabled(enabled), color);
    }

    public EntityIcon withPlayerSkinName(String name) {
        return new EntityIcon(spec.withPlayerSkinName(name), color);
    }

    public EntityIcon withPlayerName(String name) {
        return new EntityIcon(spec.withPlayerName(name), color);
    }

    public EntityIcon withPlayerNameVisible(boolean visible) {
        return new EntityIcon(spec.withPlayerNameVisible(visible), color);
    }

    public EntityIcon withDisplayOptionsFrom(EntityIcon source) {
        return new EntityIcon(spec.withDisplayOptionsFrom(source.spec), color);
    }

    @Override
    public void draw(DrawContext graphics, int x, int y, int w, int h) {
        if (w <= 0 || h <= 0 || color.alphai() <= 0) {
            return;
        }

        Entity entity = getOrCreateEntity();
        if (entity == null) {
            Icons.BARRIER.draw(graphics, x, y, w, h);
            return;
        }

        drawEntity(graphics, entity, x, y, w, h);
    }

    @Override
    public Icon copy() {
        return new EntityIcon(spec, color);
    }

    @Override
    public Icon withColor(Color4I color) {
        return new EntityIcon(spec, color);
    }

    @Override
    public double aspectRatio() {
        return 1D;
    }

    @Override
    public String toString() {
        return PREFIX + spec;
    }

    private void drawEntity(DrawContext graphics, Entity entity, int x, int y, int w, int h) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null) {
            return;
        }

        MatrixStack matrices = graphics.getMatrices();
        EntityRenderDispatcher dispatcher = client.getEntityRenderDispatcher();
        EntityPose pose = getEntityPose(client, matrices, x, y, w, h);
        EntityStateSnapshot previousState = EntityStateSnapshot.capture(entity);
        boolean previousRenderShadows = ((EntityRenderDispatcherAccessor) dispatcher).cqa$getRenderShadows();
        float scale = renderScale(entity, w, h);

        prepareEntity(entity, pose.yaw(), pose.pitch());
        matrices.push();
        try {
            matrices.translate(x + w / 2D, y + h * 0.85D, 50D);
            matrices.scale(scale, scale, -scale);
            matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(180F));
            if (pose.modelYaw() != 0F) {
                matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(pose.modelYaw()));
            }

            setupEntityLighting();
            dispatcher.setRenderShadows(false);
            RenderSystem.setShaderColor(color.redf(), color.greenf(), color.bluef(), color.alphaf());
            VertexConsumerProvider vertexConsumers = FlatEntityLightingVertexConsumerProvider.wrap(
                    graphics.getVertexConsumers(),
                    ENTITY_ICON_LIGHT,
                    FULL_BRIGHT_LIGHT
            );
            EntityIconRenderContext.renderingEntityIcon(() ->
                    RenderSystem.runAsFancy(() ->
                            dispatcher.render(entity, 0D, 0D, 0D, pose.yaw(), 1F, matrices, vertexConsumers, FULL_BRIGHT_LIGHT)
                    )
            );
            graphics.draw();
        } finally {
            previousState.restore(entity);
            dispatcher.setRenderShadows(previousRenderShadows);
            RenderSystem.setShaderColor(1F, 1F, 1F, 1F);
            matrices.pop();
            DiffuseLighting.enableGuiDepthLighting();
        }

        drawPlayerName(graphics, client, entity, x, y, w, h, scale);
    }

    private Entity getOrCreateEntity() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null || client.world == null) {
            return null;
        }

        if (spec.usesCurrentClientPlayer()) {
            return client.player;
        }

        EntityType<?> type = resolveType();
        if (!spec.isPlayer() && type == null) {
            return null;
        }

        if (cachedEntity != null && cachedType == type && cachedWorld == client.world) {
            return cachedEntity;
        }

        cachedType = type;
        cachedWorld = client.world;
        cachedEntity = createPreviewEntity(client, type);
        if (cachedEntity != null) {
            spec.createNbt().ifPresent(cachedEntity::readNbt);
            cachedEntity.refreshPositionAndAngles(0D, 0D, 0D, 0F, 0F);
            cachedEntity.setNoGravity(true);
        }

        return cachedEntity;
    }

    @SuppressWarnings("deprecation")
    private Entity createPreviewEntity(MinecraftClient client, EntityType<?> type) {
        if (spec.isPlayer()) {
            ClientWorld world = client.world;
            if (world == null) {
                return null;
            }

            return new PreviewPlayerEntity(world, spec, client.getSession().getUsername());
        }

        return createEntity(type, client.world);
    }

    @SuppressWarnings("deprecation")
    private static Entity createEntity(EntityType<?> type, World world) {
        return type.create(world);
    }

    private EntityType<?> resolveType() {
        if (spec.isPlayer()) {
            return null;
        }

        //? if forge {
        /*return ForgeRegistries.ENTITY_TYPES.getValue(spec.entityId());
        *///?} else {
        return Registries.ENTITY_TYPE.getOrEmpty(spec.entityId()).orElse(null);
        //?}
    }

    private static boolean isRegistered(Identifier entityId) {
        //? if forge {
        /*return ForgeRegistries.ENTITY_TYPES.containsKey(entityId);
        *///?} else {
        return Registries.ENTITY_TYPE.containsId(entityId);
        //?}
    }

    static String stripPrefix(String value) {
        if (isEntityIconString(value)) {
            return value.substring(PREFIX.length());
        }
        return value;
    }

    private EntityPose getEntityPose(MinecraftClient client, MatrixStack matrices, int x, int y, int width, int height) {
        EntityPose defaultPose = defaultPose();
        if (spec.lookAtCursorEnabled()) {
            return cursorPose(client, transformedRect(matrices, x, y, width, height)).orElse(defaultPose);
        }

        return defaultPose;
    }

    private EntityPose defaultPose() {
        return new EntityPose(FRONT_FACING_YAW, 0F, spec.rotationEnabled() ? currentYaw() : 0F);
    }

    private static Optional<EntityPose> cursorPose(MinecraftClient client, ScreenRect rect) {
        int windowWidth = client.getWindow().getWidth();
        int windowHeight = client.getWindow().getHeight();
        int scaledWidth = client.getWindow().getScaledWidth();
        int scaledHeight = client.getWindow().getScaledHeight();
        if (windowWidth <= 0 || windowHeight <= 0 || scaledWidth <= 0 || scaledHeight <= 0 || !rect.isValid()) {
            return Optional.empty();
        }

        double mouseX = client.mouse.getX();
        double mouseY = client.mouse.getY();
        if (!Double.isFinite(mouseX) || !Double.isFinite(mouseY)) {
            return Optional.empty();
        }

        double scaledMouseX = mouseX * scaledWidth / windowWidth;
        double scaledMouseY = mouseY * scaledHeight / windowHeight;
        if (!Double.isFinite(scaledMouseX) || !Double.isFinite(scaledMouseY)) {
            return Optional.empty();
        }

        float yawOffset = (float) MathHelper.clamp((rect.centerX() - scaledMouseX) / Math.max(rect.width(), 1D) * CURSOR_MAX_YAW, -CURSOR_MAX_YAW, CURSOR_MAX_YAW);
        float pitch = (float) MathHelper.clamp((scaledMouseY - rect.centerY()) / Math.max(rect.height(), 1D) * CURSOR_MAX_PITCH, -CURSOR_MAX_PITCH, CURSOR_MAX_PITCH);
        float yaw = FRONT_FACING_YAW + yawOffset;
        if (!Float.isFinite(yaw) || !Float.isFinite(pitch)) {
            return Optional.empty();
        }

        return Optional.of(new EntityPose(yaw, pitch, 0F));
    }

    private static ScreenRect transformedRect(MatrixStack matrices, int x, int y, int width, int height) {
        Matrix4f matrix = matrices.peek().getPositionMatrix();
        Vector4f topLeft = transform(matrix, x, y);
        Vector4f topRight = transform(matrix, x + width, y);
        Vector4f bottomLeft = transform(matrix, x, y + height);
        Vector4f bottomRight = transform(matrix, x + width, y + height);

        double minX = Math.min(Math.min(topLeft.x, topRight.x), Math.min(bottomLeft.x, bottomRight.x));
        double maxX = Math.max(Math.max(topLeft.x, topRight.x), Math.max(bottomLeft.x, bottomRight.x));
        double minY = Math.min(Math.min(topLeft.y, topRight.y), Math.min(bottomLeft.y, bottomRight.y));
        double maxY = Math.max(Math.max(topLeft.y, topRight.y), Math.max(bottomLeft.y, bottomRight.y));
        return new ScreenRect((minX + maxX) / 2D, (minY + maxY) / 2D, maxX - minX, maxY - minY);
    }

    private static Vector4f transform(Matrix4f matrix, int x, int y) {
        Vector4f point = new Vector4f(x, y, 0F, 1F);
        point.mul(matrix);
        return point;
    }

    private static void prepareEntity(Entity entity, float yaw, float pitch) {
        entity.age = (int) (System.currentTimeMillis() / 50L);
        entity.setYaw(yaw);
        entity.prevYaw = yaw;
        entity.setPitch(pitch);
        entity.prevPitch = pitch;
        entity.setHeadYaw(yaw);
        entity.setBodyYaw(yaw);

        if (entity instanceof LivingEntity living) {
            living.headYaw = yaw;
            living.prevHeadYaw = yaw;
            living.bodyYaw = yaw;
            living.prevBodyYaw = yaw;
        }
    }

    private static void setupEntityLighting() {
        RenderSystem.setShaderLights(ENTITY_ICON_LIGHT, ENTITY_ICON_LIGHT);
    }

    private static float renderScale(Entity entity, int width, int height) {
        float entityWidth = Math.max(entity.getWidth(), MIN_ENTITY_SIZE);
        float entityHeight = Math.max(entity.getHeight(), MIN_ENTITY_SIZE);
        return Math.min(width / entityWidth, height / entityHeight) * MODEL_PADDING;
    }

    private void drawPlayerName(DrawContext graphics, MinecraftClient client, Entity entity, int x, int y, int width, int height, float scale) {
        if (!spec.isPlayer() || !spec.playerNameVisible()) {
            return;
        }

        String name = playerName(client, entity);
        if (name.isBlank()) {
            return;
        }

        MatrixStack matrices = graphics.getMatrices();
        MatrixScale2D matrixScale = MatrixScale2D.from(matrices.peek().getPositionMatrix());
        if (matrixScale.isInvalid()) {
            return;
        }

        int textWidth = client.textRenderer.getWidth(name);
        float labelX = x + width / 2F;
        float labelY = (float) (y + height * 0.85D - entity.getHeight() * scale)
                - (client.textRenderer.fontHeight + PLAYER_NAME_GAP) / matrixScale.y();
        int labelColor = ((color.alphai() & 0xFF) << 24) | PLAYER_NAME_COLOR;

        matrices.push();
        try {
            matrices.translate(labelX, labelY, 100D);
            matrices.scale(1F / matrixScale.x(), 1F / matrixScale.y(), 1F);
            graphics.drawTextWithShadow(client.textRenderer, name, -textWidth / 2, 0, labelColor);
        } finally {
            matrices.pop();
        }
    }

    private String playerName(MinecraftClient client, Entity entity) {
        if (entity instanceof PreviewPlayerEntity previewPlayer) {
            return previewPlayer.shouldShowPreviewName() ? previewPlayer.previewName() : "";
        }

        return spec.playerName(client.getSession().getUsername());
    }

    private static float currentYaw() {
        long time = System.currentTimeMillis() % ROTATION_PERIOD_MS;
        return time * 360F / ROTATION_PERIOD_MS;
    }

    private record EntityPose(float yaw, float pitch, float modelYaw) {
    }

    private record ScreenRect(double centerX, double centerY, double width, double height) {
        boolean isValid() {
            return Double.isFinite(centerX)
                    && Double.isFinite(centerY)
                    && Double.isFinite(width)
                    && Double.isFinite(height)
                    && width > 0D
                    && height > 0D;
        }
    }
}
