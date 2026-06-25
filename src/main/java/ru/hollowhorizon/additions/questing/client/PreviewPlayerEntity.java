package ru.hollowhorizon.additions.questing.client;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.OtherClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;
//? if >= 1.21.1 {
import net.minecraft.client.util.SkinTextures;
//?} else {
/*import net.minecraft.util.Identifier;
*///?}
import net.minecraft.text.Text;

public final class PreviewPlayerEntity extends OtherClientPlayerEntity {
    private final EntityIconSpec spec;
    private final String fallbackName;

    public PreviewPlayerEntity(ClientWorld world, EntityIconSpec spec, String fallbackName) {
        super(world, PlayerPreviewProfiles.baseProfile(spec, fallbackName));
        this.spec = spec;
        this.fallbackName = fallbackName;
    }

    @Override
    public boolean shouldRenderName() {
        return false;
    }

    @Override
    public Text getName() {
        return Text.literal(previewName());
    }

    @Override
    public Text getDisplayName() {
        return getName();
    }

    //? if >= 1.21.1 {
    @Override
    public String getNameForScoreboard() {
        return previewName();
    }
    //?} else {
    /*public String getEntityName() {
        return previewName();
    }
    *///?}

    public boolean shouldShowPreviewName() {
        return spec.playerNameVisible();
    }

    public String previewName() {
        return spec.playerName(fallbackName);
    }

    //? if >= 1.21.1 {
    @Override
    public SkinTextures getSkinTextures() {
        if (spec.usesCurrentClientPlayerSkin()) {
            MinecraftClient client = MinecraftClient.getInstance();
            if (client != null && client.player != null) {
                return client.player.getSkinTextures();
            }
        }

        return PlayerPreviewProfiles.skinTextures(spec, fallbackName);
    }
    //?} else {
    /*@Override
    public Identifier getSkinTexture() {
        if (spec.usesCurrentClientPlayerSkin()) {
            MinecraftClient client = MinecraftClient.getInstance();
            if (client != null && client.player != null) {
                return client.player.getSkinTexture();
            }
        }

        return PlayerPreviewProfiles.skinTexture(spec, fallbackName);
    }

    @Override
    public String getModel() {
        if (spec.usesCurrentClientPlayerSkin()) {
            MinecraftClient client = MinecraftClient.getInstance();
            if (client != null && client.player != null) {
                return client.player.getModel();
            }
        }

        return PlayerPreviewProfiles.skinModel(spec, fallbackName);
    }
    *///?}
}
