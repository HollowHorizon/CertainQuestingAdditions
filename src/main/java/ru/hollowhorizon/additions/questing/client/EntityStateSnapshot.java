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
            //? if >= 1.21.11 {
            livingPrevHeadYaw = living.lastHeadYaw;
            //?} else {
            /*livingPrevHeadYaw = living.prevHeadYaw;
            *///?}
            livingBodyYaw = living.bodyYaw;
            //? if >= 1.21.11 {
            livingPrevBodyYaw = living.lastBodyYaw;
            //?} else {
            /*livingPrevBodyYaw = living.prevBodyYaw;
            *///?}
        }

        return new EntityStateSnapshot(
                entity.age,
                entity.getYaw(),
                //? if >= 1.21.11 {
                entity.lastYaw,
                //?} else {
                /*entity.prevYaw,
                *///?}
                entity.getPitch(),
                //? if >= 1.21.11 {
                entity.lastPitch,
                //?} else {
                /*entity.prevPitch,
                *///?}
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
        //? if >= 1.21.11 {
        entity.lastYaw = prevYaw;
        //?} else {
        /*entity.prevYaw = prevYaw;
        *///?}
        entity.setPitch(pitch);
        //? if >= 1.21.11 {
        entity.lastPitch = prevPitch;
        //?} else {
        /*entity.prevPitch = prevPitch;
        *///?}
        entity.setHeadYaw(headYaw);
        entity.setBodyYaw(bodyYaw);

        if (entity instanceof LivingEntity living) {
            living.headYaw = livingHeadYaw;
            //? if >= 1.21.11 {
            living.lastHeadYaw = livingPrevHeadYaw;
            //?} else {
            /*living.prevHeadYaw = livingPrevHeadYaw;
            *///?}
            living.bodyYaw = livingBodyYaw;
            //? if >= 1.21.11 {
            living.lastBodyYaw = livingPrevBodyYaw;
            //?} else {
            /*living.prevBodyYaw = livingPrevBodyYaw;
            *///?}
        }
    }
}
