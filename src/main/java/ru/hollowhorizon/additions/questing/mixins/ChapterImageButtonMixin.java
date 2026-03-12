package ru.hollowhorizon.additions.questing.mixins;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import dev.ftb.mods.ftblibrary.ui.Theme;
import dev.ftb.mods.ftbquests.client.gui.quests.ChapterImageButton;
import net.minecraft.client.gui.DrawContext;
import org.spongepowered.asm.mixin.Mixin;
import ru.hollowhorizon.additions.questing.client.ApngTextureManager;

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
