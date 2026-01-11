package ru.hollowhorizon.additions.questing.mixins;

import com.mojang.blaze3d.systems.RenderSystem;
import dev.ftb.mods.ftblibrary.ui.BaseScreen;
import dev.ftb.mods.ftblibrary.ui.Theme;
import dev.ftb.mods.ftbquests.client.gui.quests.QuestPanel;
import dev.ftb.mods.ftbquests.client.gui.quests.QuestScreen;
import dev.ftb.mods.ftbquests.quest.Chapter;
import dev.ftb.mods.ftbquests.quest.theme.property.ThemeProperties;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import ru.hollowhorizon.additions.questing.client.Animator;
import ru.hollowhorizon.additions.questing.client.CustomBackgroundRenderer;
import ru.hollowhorizon.additions.questing.client.QuestScreenZoom;
import ru.hollowhorizon.additions.questing.config.QuestAnimationsConfig;

@Mixin(value = QuestScreen.class, remap = false)
public abstract class QuestScreenMixin extends BaseScreen implements QuestScreenZoom {
    @Unique
    private final Animator hollow$zoomAnimator = new Animator(16f, 0.075f, f -> f);
    @Shadow
    @Final
    public QuestPanel questPanel;
    @Shadow
    Chapter selectedChapter;
    @Shadow
    int zoom;
    @Shadow private double scrollWidth;
    @Shadow private double scrollHeight;
    @Unique
    private boolean hollow$zoomAnimating = false;

    @Inject(method = "addZoom", at = @At("HEAD"), cancellable = true, remap = false)
    private void onAddZoom(double up, CallbackInfo ci) {
        if (QuestAnimationsConfig.SMOOTH_SCROLLING.get()) {
            float targetZoom = (float) Math.min(Math.max(hollow$zoomAnimator.get() + up * 4, 4), 28);

            hollow$zoomAnimator.set(targetZoom);
            hollow$zoomAnimating = true;
            ci.cancel();
        }
    }

    @Inject(method = "drawBackground", at = @At("HEAD"), remap = false)
    private void onDraw(CallbackInfo ci) {
        if (hollow$zoomAnimating && QuestAnimationsConfig.SMOOTH_SCROLLING.get()) {
            hollow$zoomAnimator.update();

            // Вызываем пересчет виджетов
            questPanel.withPreservedPos(QuestPanel::resetScroll);


            if (!hollow$zoomAnimator.isAnimating()) {
                hollow$zoomAnimating = false;
            }
        }
    }

    @Redirect(method = "drawBackground", at= @At(value = "INVOKE", target = "Ldev/ftb/mods/ftblibrary/ui/BaseScreen;drawBackground(Lnet/minecraft/client/gui/DrawContext;Ldev/ftb/mods/ftblibrary/ui/Theme;IIII)V"), remap = true)
    private void drawCustomBackground(BaseScreen instance, DrawContext graphics, Theme theme, int x, int y, int w, int h) {
        if (QuestAnimationsConfig.SHADER_BACKGROUND.get()) {
            CustomBackgroundRenderer.draw(graphics, x, y, w, h, questPanel.getScrollX(), questPanel.getScrollY(), scrollWidth, scrollHeight, cqa$getZoom());
        } else {
            super.drawBackground(graphics, theme, x, y, w, h);
        }
    }

    @Inject(method = "getQuestButtonSize", at = @At("HEAD"), cancellable = true)
    public void onGetQuestButtonSize(CallbackInfoReturnable<Double> cir) {
        if(!QuestAnimationsConfig.SMOOTH_SCROLLING.get()) return;
        cir.setReturnValue((double) (hollow$zoomAnimator.get() * 3.0F / 2.0F));
    }

    @Inject(method = "getQuestButtonSpacing", at = @At("HEAD"), cancellable = true)
    public void getQuestButtonSpacing(CallbackInfoReturnable<Double> cir) {
        if(!QuestAnimationsConfig.SMOOTH_SCROLLING.get()) return;
        cir.setReturnValue(hollow$zoomAnimator.get() * ThemeProperties.QUEST_SPACING.get(selectedChapter) / 4.0F);
    }

    @Override
    public float cqa$getZoom() {
        return hollow$zoomAnimator.get();
    }
}
