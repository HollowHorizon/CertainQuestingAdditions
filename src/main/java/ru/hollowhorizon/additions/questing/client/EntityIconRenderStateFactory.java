package ru.hollowhorizon.additions.questing.client;

//? if >= 1.21.11 {
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.entity.EntityRenderManager;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.state.EntityRenderState;
import net.minecraft.client.render.entity.state.LivingEntityRenderState;
import net.minecraft.client.render.entity.state.PlayerEntityRenderState;
import net.minecraft.entity.Entity;
//?}

final class EntityIconRenderStateFactory {
    private EntityIconRenderStateFactory() {
    }

    //? if >= 1.21.11 {
    @SuppressWarnings({"rawtypes", "unchecked"})
    static EntityRenderState create(
            MinecraftClient client,
            Entity entity,
            float yaw,
            float pitch,
            float tickProgress,
            int light
    ) {
        EntityRenderManager manager = client.getEntityRenderDispatcher();
        EntityRenderer renderer = manager.getRenderer(entity);
        EntityRenderState state = renderer.getAndUpdateRenderState(entity, tickProgress);
        clearBaseState(state, light);
        ((EntityIconRenderStateMarker) state).cqa$markEntityIcon();

        if (state instanceof LivingEntityRenderState livingState) {
            orientLivingState(livingState, yaw, pitch);
        }

        if (state instanceof PlayerEntityRenderState playerState) {
            clearPlayerState(playerState);
        }

        return state;
    }

    private static void clearBaseState(EntityRenderState state, int light) {
        state.light = light;
        state.shadowPieces.clear();
        state.shadowRadius = 0F;
        state.outlineColor = EntityRenderState.NO_OUTLINE;
        state.displayName = null;
        state.nameLabelPos = null;
        state.onFire = false;
        if (state.leashDatas != null) {
            state.leashDatas.clear();
        }
    }

    private static void orientLivingState(LivingEntityRenderState state, float yaw, float pitch) {
        state.bodyYaw = yaw;
        state.relativeHeadYaw = 0F;
        state.pitch = pitch;
        if (state.baseScale != 0F) {
            state.width /= state.baseScale;
            state.height /= state.baseScale;
            state.baseScale = 1F;
        }
    }

    private static void clearPlayerState(PlayerEntityRenderState state) {
        state.playerName = null;
        state.leftShoulderParrotVariant = null;
        state.rightShoulderParrotVariant = null;
    }
    //?}
}
