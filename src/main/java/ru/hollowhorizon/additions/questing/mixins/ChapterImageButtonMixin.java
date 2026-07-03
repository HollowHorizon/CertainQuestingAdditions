package ru.hollowhorizon.additions.questing.mixins;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
//? if <= 1.20.1 {
/*import dev.ftb.mods.ftblibrary.config.ConfigGroup;
*///?}
//? if >= 1.21.11 {
import dev.ftb.mods.ftblibrary.client.gui.theme.Theme;
//?} else {
/*import dev.ftb.mods.ftblibrary.ui.Theme;
*///?}
import dev.ftb.mods.ftbquests.client.gui.quests.ChapterImageButton;
import dev.ftb.mods.ftbquests.quest.ChapterImage;
import net.minecraft.client.gui.DrawContext;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import ru.hollowhorizon.additions.questing.client.ApngTextureManager;
import ru.hollowhorizon.additions.questing.client.EntityIcon;
import ru.hollowhorizon.additions.questing.client.EntityIconConfigEntries;

@Mixin(value = ChapterImageButton.class, remap = false)
public abstract class ChapterImageButtonMixin {
    @WrapMethod(method = "draw")
    private void cqa$withCanvasApngScope(DrawContext graphics, Theme theme, int x, int y, int w, int h, Operation<Void> original) {
        ApngTextureManager.pushScope(ApngTextureManager.ApngScope.QUEST_SCREEN_CANVAS);
        try {
            original.call(graphics, theme, x, y, w, h);
        } finally {
            ApngTextureManager.popScope(ApngTextureManager.ApngScope.QUEST_SCREEN_CANVAS);
        }
    }
}
