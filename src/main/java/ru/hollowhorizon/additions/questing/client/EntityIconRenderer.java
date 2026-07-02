package ru.hollowhorizon.additions.questing.client;

//? if >= 1.21.11 {
import dev.ftb.mods.ftblibrary.client.icon.IconRenderer;
import net.minecraft.client.gui.DrawContext;

public enum EntityIconRenderer implements IconRenderer<EntityIcon> {
    INSTANCE;

    @Override
    public void render(EntityIcon icon, DrawContext graphics, int x, int y, int width, int height) {
        icon.render(graphics, x, y, width, height);
    }

    @Override
    public double aspectRatio(EntityIcon icon) {
        return 1D;
    }
}
//?}
