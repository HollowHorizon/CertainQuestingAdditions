package ru.hollowhorizon.additions.questing.client;

import dev.ftb.mods.ftblibrary.icon.Color4I;
import dev.ftb.mods.ftblibrary.icon.Icon;
import dev.ftb.mods.ftblibrary.icon.Icons;
import net.minecraft.client.gui.DrawContext;

//? if >= 1.21.11 {
public final class InvalidEntityIcon extends Icon<InvalidEntityIcon> {
//?} else {
/*public final class InvalidEntityIcon extends Icon {
*///?}
    private final String value;
    private final Color4I color;

    public InvalidEntityIcon(String value) {
        this(value, Color4I.WHITE);
    }

    private InvalidEntityIcon(String value, Color4I color) {
        this.value = value;
        this.color = color;
    }

    //? if >= 1.21.11 {
    @Override
    public InvalidEntityIconRenderer getRenderer() {
        return InvalidEntityIconRenderer.INSTANCE;
    }

    public void render(DrawContext graphics, int x, int y, int w, int h) {
        FtbIconRenderer.draw(Icons.BARRIER.withColor(color), graphics, x, y, w, h);
    }
    //?} else {
    /*@Override
    public void draw(DrawContext graphics, int x, int y, int w, int h) {
        FtbIconRenderer.draw(Icons.BARRIER.withColor(color), graphics, x, y, w, h);
    }
    *///?}

    @Override
    public InvalidEntityIcon copy() {
        return new InvalidEntityIcon(value, color);
    }

    @Override
    public InvalidEntityIcon withColor(Color4I color) {
        return new InvalidEntityIcon(value, color);
    }

    //? if < 1.21.11 {
    /*@Override
    public double aspectRatio() {
        return 1D;
    }
    *///?}

    @Override
    public String toString() {
        return value;
    }
}
