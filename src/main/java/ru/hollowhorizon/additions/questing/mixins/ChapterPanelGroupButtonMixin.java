package ru.hollowhorizon.additions.questing.mixins;

import dev.ftb.mods.ftblibrary.icon.Color4I;
import dev.ftb.mods.ftblibrary.icon.Icon;
import dev.ftb.mods.ftblibrary.ui.GuiHelper;
import dev.ftb.mods.ftblibrary.ui.Theme;
import dev.ftb.mods.ftbquests.client.gui.quests.ChapterPanel;
import dev.ftb.mods.ftbquests.quest.ChapterGroup;
import dev.ftb.mods.ftbquests.quest.theme.property.ThemeProperties;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.hollowhorizon.additions.questing.client.Animator;
import ru.hollowhorizon.additions.questing.config.QuestAnimationsConfig;

@Mixin(value = ChapterPanel.ChapterGroupButton.class, remap = false)
public abstract class ChapterPanelGroupButtonMixin extends ChapterPanel.ListButton {

    @Unique
    private final Animator mou$animator = new Animator(0f, 0.25f, (i) -> 1 - (1 - i) * (1 - i) * (1 - i));
    @Shadow
    @Final
    public ChapterGroup group;

    //? if >= 1.21.1 {
    /*@Shadow @Final private boolean xlateWarning;
    *///?}

    public ChapterPanelGroupButtonMixin(ChapterPanel panel, Text t, Icon i) {
        super(panel, t, i);
    }

    @Inject(method = "draw", at = @At("HEAD"), cancellable = true)
    private void onDraw(DrawContext graphics, Theme theme, int x, int y, int w, int h, CallbackInfo ci) {
        if (!QuestAnimationsConfig.PANEL_BUTTON_HOVER.get()) return;

        GuiHelper.setupDrawing();
        if (isMouseOver()) mou$animator.set(1f);
        else mou$animator.set(0.0f);
        mou$animator.update();
        var factor = mou$animator.get();

        //? if >= 1.21.1 {
        /*if (this.xlateWarning) {
            Color4I.RED.withAlpha(40).draw(graphics, x, y, w, h);
        }
        *///?}

        if (factor > 0) {
            Color4I.WHITE.withAlpha((int) (QuestAnimationsConfig.BUTTON_HOVER_GLOW_ALPHA.get() * factor)).draw(graphics, x + 1, y, w - 2, h);
        }

        var c1 = 16777215;
        var c2 = 11184810;

        int r = (int) (((c1 >> 16) & 0xFF) * factor + ((c2 >> 16) & 0xFF) * (1 - factor));
        int g = (int) (((c1 >> 8) & 0xFF) * factor + ((c2 >> 8) & 0xFF) * (1 - factor));
        int b = (int) ((c1 & 0xFF) * factor + (c2 & 0xFF) * (1 - factor));

        int mixed = (r << 16) | (g << 8) | b;


        (this.group.isGuiCollapsed() ? ChapterPanel.ARROW_COLLAPSED : ChapterPanel.ARROW_EXPANDED).withColor(Color4I.rgb(r, g, b)).draw(graphics, x + 3, y + 5, 8, 8);
        theme.drawString(graphics, Text.literal("").append(this.title).setStyle(Style.EMPTY.withColor(mixed)), x + 15, y + 5);
        boolean canEdit = ((QuestScreenAccessor) ((ChapterPanelAccessor) this.chapterPanel).getQuestScreen()).getFile().canEdit();
        if (canEdit) {
            ThemeProperties.ADD_ICON.get().draw(graphics, x + w - 14, y + 3, 12, 12);
        }

        ci.cancel();
    }
}
