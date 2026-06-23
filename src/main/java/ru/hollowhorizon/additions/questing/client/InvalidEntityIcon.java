package ru.hollowhorizon.additions.questing.client;

import dev.ftb.mods.ftblibrary.icon.Color4I;
import dev.ftb.mods.ftblibrary.icon.Icon;
import dev.ftb.mods.ftblibrary.icon.Icons;
import net.minecraft.client.gui.DrawContext;

public final class InvalidEntityIcon extends Icon {
    private final String value;
    private final Color4I color;

    public InvalidEntityIcon(String value) {
        this(value, Color4I.WHITE);
    }

    private InvalidEntityIcon(String value, Color4I color) {
        this.value = value;
        this.color = color;
    }

    @Override
    public void draw(DrawContext graphics, int x, int y, int w, int h) {
        Icons.BARRIER.withColor(color).draw(graphics, x, y, w, h);
    }

    @Override
    public Icon copy() {
        return new InvalidEntityIcon(value, color);
    }

    @Override
    public Icon withColor(Color4I color) {
        return new InvalidEntityIcon(value, color);
    }

    @Override
    public double aspectRatio() {
        return 1D;
    }

    @Override
    public String toString() {
        return value;
    }
}
