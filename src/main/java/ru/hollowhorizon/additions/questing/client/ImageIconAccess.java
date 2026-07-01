package ru.hollowhorizon.additions.questing.client;

import dev.ftb.mods.ftblibrary.icon.ImageIcon;
import net.minecraft.util.Identifier;

public final class ImageIconAccess {
    private ImageIconAccess() {
    }

    public static Identifier resourceId(ImageIcon icon) {
        //? if >= 1.21.11 {
        return icon.getResourceId();
        //?} else {
        /*return icon.getResourceLocation();
        *///?}
    }
}
