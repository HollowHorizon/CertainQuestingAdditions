package ru.hollowhorizon.additions.questing.client;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;

record EntityStateSnapshot(
        int age,
        float yaw,
        float prevYaw,
        float pitch,
        float prevPitch,
        float headYaw,
        float bodyYaw,
        float livingHeadYaw,
        float livingPrevHeadYaw,
        float livingBodyYaw,
        float livingPrevBodyYaw
) {
    static EntityStateSnapshot capture(Entity entity) {
        float livingHeadYaw = 0F;
        float livingPrevHeadYaw = 0F;
        float livingBodyYaw = 0F;
        float livingPrevBodyYaw = 0F;
        if (entity instanceof LivingEntity living) {
            livingHeadYaw = living.headYaw;
            livingPrevHeadYaw = living.prevHeadYaw;
            livingBodyYaw = living.bodyYaw;
            livingPrevBodyYaw = living.prevBodyYaw;
        }

        return new EntityStateSnapshot(
                entity.age,
                entity.getYaw(),
                entity.prevYaw,
                entity.getPitch(),
                entity.prevPitch,
                entity.getHeadYaw(),
                entity.getBodyYaw(),
                livingHeadYaw,
                livingPrevHeadYaw,
                livingBodyYaw,
                livingPrevBodyYaw
        );
    }

    void restore(Entity entity) {
        entity.age = age;
        entity.setYaw(yaw);
        entity.prevYaw = prevYaw;
        entity.setPitch(pitch);
        entity.prevPitch = prevPitch;
        entity.setHeadYaw(headYaw);
        entity.setBodyYaw(bodyYaw);

        if (entity instanceof LivingEntity living) {
            living.headYaw = livingHeadYaw;
            living.prevHeadYaw = livingPrevHeadYaw;
            living.bodyYaw = livingBodyYaw;
            living.prevBodyYaw = livingPrevBodyYaw;
        }
    }
}
