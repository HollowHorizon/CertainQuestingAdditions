package ru.hollowhorizon.additions.questing.client;

import net.minecraft.client.gui.DrawContext;

public final class GuiMatrices {
    private GuiMatrices() {}

    public static void push(DrawContext graphics) {
        //? if >= 1.21.11 {
        graphics.getMatrices().pushMatrix();
        //?} else {
        /*graphics.getMatrices().push();
        *///?}
    }

    public static void pop(DrawContext graphics) {
        //? if >= 1.21.11 {
        graphics.getMatrices().popMatrix();
        //?} else {
        /*graphics.getMatrices().pop();
        *///?}
    }

    public static void translate(DrawContext graphics, float x, float y, float z) {
        //? if >= 1.21.11 {
        graphics.getMatrices().translate(x, y);
        //?} else {
        /*graphics.getMatrices().translate(x, y, z);
        *///?}
    }

    public static void scale(DrawContext graphics, float x, float y, float z) {
        //? if >= 1.21.11 {
        graphics.getMatrices().scale(x, y);
        //?} else {
        /*graphics.getMatrices().scale(x, y, z);
        *///?}
    }
}
