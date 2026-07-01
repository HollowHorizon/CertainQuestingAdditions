package ru.hollowhorizon.additions.questing.client;

import dev.ftb.mods.ftblibrary.icon.Icon;
//? if >= 1.21.11 {
import dev.ftb.mods.ftblibrary.client.icon.IconRenderer;
//?}
import net.minecraft.client.gui.DrawContext;

public final class FtbIconRenderer {
    private FtbIconRenderer() {
    }

    public static void draw(Icon icon, DrawContext graphics, int x, int y, int width, int height) {
        if (icon == null || icon.isEmpty()) {
            return;
        }

        //? if >= 1.21.11 {
        drawWithRenderer(icon, graphics, x, y, width, height);
        //?} else {
        /*icon.draw(graphics, x, y, width, height);
        *///?}
    }

    //? if >= 1.21.11 {
    @SuppressWarnings({"rawtypes", "unchecked"})
    private static void drawWithRenderer(Icon icon, DrawContext graphics, int x, int y, int width, int height) {
        IconRenderer renderer = icon.getRenderer();
        renderer.render(icon, graphics, x, y, width, height);
    }
    //?}
}
