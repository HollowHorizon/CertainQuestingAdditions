package ru.hollowhorizon.additions.questing.mixins;

import dev.ftb.mods.ftbquests.client.gui.quests.QuestPanel;
import dev.ftb.mods.ftbquests.client.gui.quests.QuestScreen;
import dev.ftb.mods.ftbquests.quest.Chapter;
import dev.ftb.mods.ftbquests.quest.theme.property.ThemeProperties;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import ru.hollowhorizon.additions.questing.client.Animator;
import ru.hollowhorizon.additions.questing.client.QuestScreenZoom;
import ru.hollowhorizon.additions.questing.config.QuestAnimationsConfig;

@Mixin(value = QuestScreen.class, remap = false)
public class QuestScreenMixin implements QuestScreenZoom {
    @Unique
    private final Animator hollow$zoomAnimator = new Animator(16f, 0.075f, f -> f);
    @Shadow
    @Final
    public QuestPanel questPanel;
    @Shadow
    Chapter selectedChapter;
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
