package ru.hollowhorizon.additions.questing.mixins;

import dev.ftb.mods.ftblibrary.icon.Color4I;
import dev.ftb.mods.ftblibrary.icon.Icon;
import dev.ftb.mods.ftblibrary.ui.GuiHelper;
import dev.ftb.mods.ftblibrary.ui.Theme;
import dev.ftb.mods.ftbquests.client.gui.quests.ChapterPanel;
import dev.ftb.mods.ftbquests.quest.Chapter;
import dev.ftb.mods.ftbquests.quest.theme.property.ThemeProperties;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.text.TextColor;
import net.minecraft.util.Formatting;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.hollowhorizon.additions.questing.client.Animator;
import ru.hollowhorizon.additions.questing.config.QuestAnimationsConfig;

@Mixin(value = ChapterPanel.ChapterButton.class, remap = false)
public abstract class ChapterPanelChapterButtonMixin extends ChapterPanel.ListButton {
    @Unique
    private final Animator mou$animator = new Animator(0f, 0.25f, (i) -> 1 - (1 - i) * (1 - i) * (1 - i));
    @Shadow
    @Final
    private Chapter chapter;

    //? if >= 1.21.1 {
    @Shadow @Final private boolean xlateWarningTitle;

    @Shadow @Final private boolean xlateWarningSubtitle;
    //?}

    public ChapterPanelChapterButtonMixin(ChapterPanel panel, Text t, Icon i) {
        super(panel, t, i);
    }

    @Inject(method = "draw", at = @At("HEAD"), cancellable = true)
    private void onDraw(DrawContext graphics, Theme theme, int x, int y, int w, int h, CallbackInfo ci) {
        if(!QuestAnimationsConfig.PANEL_BUTTON_HOVER.get()) return;

        GuiHelper.setupDrawing();
        if (isMouseOver()) mou$animator.set(1f);
        else mou$animator.set(0.0f);
        mou$animator.update();
        var factor = mou$animator.get();

        //? if >=1.21.1 {
        if (this.xlateWarningTitle || this.xlateWarningSubtitle) {
            Color4I.RED.withAlpha(40).draw(graphics, x, y, w, h);
        }
        //?}

        if (factor > 0f) {
            Color4I.WHITE.withAlpha((int) (QuestAnimationsConfig.BUTTON_HOVER_GLOW_ALPHA.get() * factor)).draw(graphics, x + 1, y, w - 2, h);
        }

        QuestScreenAccessor questScreen = (QuestScreenAccessor) ((ChapterPanelAccessor) this.chapterPanel).getQuestScreen();

        Color4I c = this.chapter.getProgressColor(questScreen.getFile().selfTeamData).addBrightness(-0.35f * (1f - factor));

        int xOff = this.chapter.getGroup().isDefaultGroup() ? 0 : 7;
        this.icon.draw(graphics, x + 2 + xOff, y + 1, 12, 12);
        MutableText text = Text.literal("").append(this.title).setStyle(Style.EMPTY.withColor(TextColor.fromRgb(c.rgb())));
        if (questScreen.getSelectedChapter() != null && this.chapter.id == questScreen.getSelectedChapter().id) {
            text.append(Text.literal(" ◀").formatted(Formatting.GRAY));
        }

        theme.drawString(graphics, text, x + 16 + xOff, y + 3);
        GuiHelper.setupDrawing();
        if (!this.chapter.hasAnyVisibleChildren()) {
            ThemeProperties.CLOSE_ICON.get().draw(graphics, x + w - 12, y + 3, 8, 8);
        } else if (questScreen.getFile().selfTeamData.hasUnclaimedRewards(MinecraftClient.getInstance().player.getUuid(), this.chapter)) {
            ThemeProperties.ALERT_ICON.get().draw(graphics, x + w - 12, y + 3, 8, 8);
        }

        ci.cancel();
    }
}
