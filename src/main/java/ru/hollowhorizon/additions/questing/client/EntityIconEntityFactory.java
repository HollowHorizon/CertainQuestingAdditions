package ru.hollowhorizon.additions.questing.client;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
//? if >= 1.21.11 {
import net.minecraft.entity.SpawnReason;
import net.minecraft.storage.NbtReadView;
import net.minecraft.util.ErrorReporter;
//?}
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;
//? if forge {
/*import net.minecraftforge.registries.ForgeRegistries;
*///?}

final class EntityIconEntityFactory {
    private EntityIconEntityFactory() {
    }

    @SuppressWarnings("deprecation")
    static Entity create(MinecraftClient client, EntityIconSpec spec, EntityType<?> type) {
        if (spec.isPlayer()) {
            ClientWorld world = client.world;
            if (world == null) {
                return null;
            }

            return new PreviewPlayerEntity(world, spec, client.getSession().getUsername());
        }

        return createEntity(type, client.world);
    }

    static void applyNbt(EntityIconSpec spec, Entity entity) {
        spec.createNbt().ifPresent(nbt -> {
            //? if >= 1.21.11 {
            entity.readData(NbtReadView.create(ErrorReporter.EMPTY, entity.getRegistryManager(), nbt));
            //?} else {
            /*entity.readNbt(nbt);
            *///?}
        });
    }

    static EntityType<?> resolveType(EntityIconSpec spec) {
        if (spec.isPlayer()) {
            return null;
        }

        //? if forge {
        /*return ForgeRegistries.ENTITY_TYPES.getValue(spec.entityId());
        *///?} else {
        //? if >= 1.21.11 {
        return Registries.ENTITY_TYPE.getOptionalValue(spec.entityId()).orElse(null);
        //?} else {
        /*return Registries.ENTITY_TYPE.getOrEmpty(spec.entityId()).orElse(null);
        *///?}
        //?}
    }

    static boolean isRegistered(Identifier entityId) {
        //? if forge {
        /*return ForgeRegistries.ENTITY_TYPES.containsKey(entityId);
        *///?} else {
        return Registries.ENTITY_TYPE.containsId(entityId);
        //?}
    }

    @SuppressWarnings("deprecation")
    private static Entity createEntity(EntityType<?> type, World world) {
        //? if >= 1.21.11 {
        return type.create(world, SpawnReason.COMMAND);
        //?} else {
        /*return type.create(world);
        *///?}
    }
}
