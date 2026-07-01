package ru.hollowhorizon.additions.questing.client;

import dev.ftb.mods.ftblibrary.icon.Color4I;
import dev.ftb.mods.ftblibrary.icon.Icon;
import dev.ftb.mods.ftblibrary.icon.Icons;
//? if < 1.21.11 {
/*import com.mojang.blaze3d.systems.RenderSystem;
*///?}
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
//? if < 1.21.11 {
/*import net.minecraft.client.render.DiffuseLighting;
*///?}
//? if >= 1.21.11 {
import net.minecraft.client.render.entity.state.EntityRenderState;
//?} else {
/*import net.minecraft.client.render.entity.EntityRenderDispatcher;
*///?}
//? if < 1.21.11 {
/*import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
*///?}
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
//? if < 1.21.11 {
/*import net.minecraft.util.math.RotationAxis;
*///?}
import net.minecraft.world.World;
//? if >= 1.21.11 {
import org.joml.Quaternionf;
//?}
import org.joml.Vector3f;
//? if < 1.21.11 {
/*import ru.hollowhorizon.additions.questing.mixins.EntityRenderDispatcherAccessor;
*///?}

import java.util.Optional;

//? if >= 1.21.11 {
public final class EntityIcon extends Icon<EntityIcon> {
//?} else {
/*public final class EntityIcon extends Icon {
*///?}
    public static final String PREFIX = "entity:";

    private static final int FULL_BRIGHT_LIGHT = EntityIconLighting.FULL_BRIGHT_LIGHT;
    private static final float MODEL_PADDING = 0.75F;
    private static final float MIN_ENTITY_SIZE = 0.1F;
    private static final long ROTATION_PERIOD_MS = 8000L;
    private static final float FRONT_FACING_YAW = 180F;
    private static final float CURSOR_MAX_YAW = 75F;
    private static final float CURSOR_MAX_PITCH = 35F;
    private static final float ENTITY_UI_OFFSET = 0.0625F;
    //? if >= 1.21.11 {
    private static final double GUI_ENTITY_TARGET_HORIZONTAL_PADDING = 0.5D;
    private static final double GUI_ENTITY_TARGET_VERTICAL_PADDING = 0.15D;
    //?}
    private static final Vector3f ENTITY_ICON_LIGHT = EntityIconLighting.FLAT_LIGHT;

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
                .filter(spec -> EntityIconEntityFactory.isRegistered(spec.entityId()) || spec.isPlayer())
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

    //? if >= 1.21.11 {
    @Override
    public EntityIconRenderer getRenderer() {
        return EntityIconRenderer.INSTANCE;
    }
    //?} else {
    /*@Override
    public void draw(DrawContext graphics, int x, int y, int w, int h) {
        render(graphics, x, y, w, h);
    }
    *///?}

    public void render(DrawContext graphics, int x, int y, int w, int h) {
        if (w <= 0 || h <= 0 || color.alphai() <= 0) {
            return;
        }

        Entity entity = getOrCreateEntity();
        if (entity == null) {
            FtbIconRenderer.draw(Icons.BARRIER, graphics, x, y, w, h);
            return;
        }

        drawEntity(graphics, entity, x, y, w, h);
    }

    @Override
    public EntityIcon copy() {
        return new EntityIcon(spec, color);
    }

    @Override
    public EntityIcon withColor(Color4I color) {
        return new EntityIcon(spec, color);
    }

    //? if < 1.21.11 {
    /*@Override
    public double aspectRatio() {
        return 1D;
    }
    *///?}

    @Override
    public String toString() {
        return PREFIX + spec;
    }

    private void drawEntity(DrawContext graphics, Entity entity, int x, int y, int w, int h) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null) {
            return;
        }

        //? if >= 1.21.11 {
        ScreenRect renderRect = EntityIconRenderGeometry.transformedRect(graphics.getMatrices(), x, y, w, h);
        if (!renderRect.isValid()) {
            return;
        }

        EntityPose pose = getEntityPose(client, renderRect);
        EntityStateSnapshot previousState = EntityStateSnapshot.capture(entity);
        float previewTicks = EntityIconRenderGeometry.previewTicks();
        float tickProgress = EntityIconRenderGeometry.tickProgress(previewTicks);

        prepareEntity(entity, pose.yaw(), pose.pitch(), previewTicks);
        try {
            EntityRenderState state = EntityIconRenderContext.renderingEntityIcon(() ->
                    EntityIconRenderStateFactory.create(
                            client,
                            entity,
                            pose.yaw(),
                            pose.pitch(),
                            tickProgress,
                            FULL_BRIGHT_LIGHT
                    )
            );
            if (state == null) {
                return;
            }

            float scale = renderScale(state, renderRect.pixelWidth(), renderRect.pixelHeight());
            ScreenRect targetRect = renderRect.expand(
                    GUI_ENTITY_TARGET_HORIZONTAL_PADDING,
                    GUI_ENTITY_TARGET_VERTICAL_PADDING
            );
            Quaternionf modelRotation = new Quaternionf().rotateZ((float) Math.PI);
            if (pose.modelYaw() != 0F) {
                modelRotation.rotateY(pose.modelYaw() * MathHelper.RADIANS_PER_DEGREE);
            }

            Vector3f translation = new Vector3f(0F, state.height / 2F + ENTITY_UI_OFFSET, 0F);
            graphics.addEntity(
                    state,
                    scale,
                    translation,
                    modelRotation,
                    new Quaternionf(),
                    targetRect.left(),
                    targetRect.top(),
                    targetRect.right(),
                    targetRect.bottom()
            );
        } finally {
            previousState.restore(entity);
        }

        float labelScale = renderScale(entity, w, h);
        EntityIconPlayerNameRenderer.draw(graphics, client, spec, color, entity, x, y, w, h, labelScale);
        //?} else {
        /*MatrixStack matrices = graphics.getMatrices();
        EntityRenderDispatcher dispatcher = client.getEntityRenderDispatcher();
        EntityPose pose = getEntityPose(client, matrices, x, y, w, h);
        EntityStateSnapshot previousState = EntityStateSnapshot.capture(entity);
        boolean previousRenderShadows = ((EntityRenderDispatcherAccessor) dispatcher).cqa$getRenderShadows();
        float scale = renderScale(entity, w, h);
        float previewTicks = EntityIconRenderGeometry.previewTicks();
        float tickProgress = EntityIconRenderGeometry.tickProgress(previewTicks);

        prepareEntity(entity, pose.yaw(), pose.pitch(), previewTicks);
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
                            dispatcher.render(entity, 0D, 0D, 0D, pose.yaw(), tickProgress, matrices, vertexConsumers, FULL_BRIGHT_LIGHT)
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

        EntityIconPlayerNameRenderer.draw(graphics, client, spec, color, entity, x, y, w, h, scale);
        *///?}
    }

    private Entity getOrCreateEntity() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null || client.world == null) {
            return null;
        }

        if (spec.usesCurrentClientPlayer()) {
            return client.player;
        }

        EntityType<?> type = EntityIconEntityFactory.resolveType(spec);
        if (!spec.isPlayer() && type == null) {
            return null;
        }

        if (cachedEntity != null && cachedType == type && cachedWorld == client.world) {
            return cachedEntity;
        }

        cachedType = type;
        cachedWorld = client.world;
        cachedEntity = EntityIconEntityFactory.create(client, spec, type);
        if (cachedEntity != null) {
            EntityIconEntityFactory.applyNbt(spec, cachedEntity);
            cachedEntity.refreshPositionAndAngles(0D, 0D, 0D, 0F, 0F);
            cachedEntity.setNoGravity(true);
        }

        return cachedEntity;
    }

    static String stripPrefix(String value) {
        if (isEntityIconString(value)) {
            return value.substring(PREFIX.length());
        }
        return value;
    }

    private EntityPose getEntityPose(MinecraftClient client, ScreenRect rect) {
        EntityPose defaultPose = defaultPose();
        if (spec.lookAtCursorEnabled()) {
            return cursorPose(client, rect).orElse(defaultPose);
        }

        return defaultPose;
    }

    //? if < 1.21.11 {
    /*private EntityPose getEntityPose(MinecraftClient client, MatrixStack matrices, int x, int y, int width, int height) {
        return getEntityPose(client, EntityIconRenderGeometry.transformedRect(matrices, x, y, width, height));
    }
    *///?}

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

    private static void prepareEntity(Entity entity, float yaw, float pitch, float previewTicks) {
        entity.age = (int) previewTicks;
        entity.setYaw(yaw);
        //? if >= 1.21.11 {
        entity.lastYaw = yaw;
        //?} else {
        /*entity.prevYaw = yaw;
        *///?}
        entity.setPitch(pitch);
        //? if >= 1.21.11 {
        entity.lastPitch = pitch;
        //?} else {
        /*entity.prevPitch = pitch;
        *///?}
        entity.setHeadYaw(yaw);
        entity.setBodyYaw(yaw);

        if (entity instanceof LivingEntity living) {
            living.headYaw = yaw;
            //? if >= 1.21.11 {
            living.lastHeadYaw = yaw;
            //?} else {
            /*living.prevHeadYaw = yaw;
            *///?}
            living.bodyYaw = yaw;
            //? if >= 1.21.11 {
            living.lastBodyYaw = yaw;
            //?} else {
            /*living.prevBodyYaw = yaw;
            *///?}
        }
    }

    //? if < 1.21.11 {
    /*private static void setupEntityLighting() {
        RenderSystem.setShaderLights(ENTITY_ICON_LIGHT, ENTITY_ICON_LIGHT);
    }
    *///?}

    private static float renderScale(Entity entity, int width, int height) {
        return renderScale(entity.getWidth(), entity.getHeight(), width, height);
    }

    //? if >= 1.21.11 {
    private static float renderScale(EntityRenderState state, int width, int height) {
        return renderScale(state.width, state.height, width, height);
    }
    //?}

    private static float renderScale(float width, float height, int targetWidth, int targetHeight) {
        float entityWidth = Math.max(width, MIN_ENTITY_SIZE);
        float entityHeight = Math.max(height, MIN_ENTITY_SIZE);
        return Math.min(targetWidth / entityWidth, targetHeight / entityHeight) * MODEL_PADDING;
    }

    private static float currentYaw() {
        long time = System.currentTimeMillis() % ROTATION_PERIOD_MS;
        return time * 360F / ROTATION_PERIOD_MS;
    }

    private record EntityPose(float yaw, float pitch, float modelYaw) {
    }
}
