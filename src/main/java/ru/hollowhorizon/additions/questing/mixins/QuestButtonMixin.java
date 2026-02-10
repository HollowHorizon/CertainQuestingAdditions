package ru.hollowhorizon.additions.questing.mixins;

import dev.ftb.mods.ftblibrary.icon.Color4I;
import dev.ftb.mods.ftblibrary.icon.Icon;
import dev.ftb.mods.ftblibrary.ui.Button;
import dev.ftb.mods.ftblibrary.ui.GuiHelper;
import dev.ftb.mods.ftblibrary.ui.Panel;
import dev.ftb.mods.ftblibrary.ui.Theme;
import dev.ftb.mods.ftblibrary.ui.input.MouseButton;
import dev.ftb.mods.ftbquests.client.FTBQuestsClientConfig;
import dev.ftb.mods.ftbquests.client.gui.quests.QuestButton;
import dev.ftb.mods.ftbquests.client.gui.quests.QuestScreen;
import dev.ftb.mods.ftbquests.quest.*;
import dev.ftb.mods.ftbquests.quest.theme.property.ThemeProperties;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.hollowhorizon.additions.questing.client.Animator;
import ru.hollowhorizon.additions.questing.client.QuestButtonAnimator;
import ru.hollowhorizon.additions.questing.client.QuestPanelAnimator;
import ru.hollowhorizon.additions.questing.config.QuestAnimationsConfig;

@Mixin(value = QuestButton.class, remap = false)
public abstract class QuestButtonMixin extends Button implements QuestButtonAnimator {
    @Shadow
    @Final
    protected QuestScreen questScreen;
    @Shadow
    @Final
    Quest quest;

    @Unique
    private final Animator cqa$animator = new Animator(0f, 0.2f, (i) -> 1 - (1 - i) * (1 - i) * (1 - i));

    @Override
    public Animator cqa$getAnimator() {
        return cqa$animator;
    }

    public QuestButtonMixin(Panel panel, Text t, Icon i) {
        super(panel, t, i);
    }

    @Shadow
    protected abstract String getShape();

    @Shadow
    public abstract Movable moveAndDeleteFocus();

    @Inject(method = "draw", at = @At("HEAD"), cancellable = true)
    private void onDraw(DrawContext graphics, Theme theme, int x, int y, int w, int h, CallbackInfo ci) {
        if(!QuestAnimationsConfig.QUEST_HOVER.get()) return;

        if (isMouseOver()) cqa$animator.set(1f);
        else cqa$animator.set(0.0f);
        cqa$animator.update();
        var factor = cqa$animator.get();

        Color4I outlineColor = ThemeProperties.QUEST_NOT_STARTED_COLOR.get(this.quest);
        Icon questIcon = Color4I.empty();
        Icon hiddenIcon = Color4I.empty();
        Icon lockIcon = Color4I.empty();

        var file = ((QuestScreenAccessor) this.questScreen).getFile();
        var selectedObjects = ((QuestScreenAccessor) this.questScreen).getSelectedObjects();
        TeamData teamData = file.selfTeamData;
        boolean isCompleted = teamData.isCompleted(this.quest);
        boolean isStarted = isCompleted || teamData.isStarted(this.quest);
        boolean canStart = teamData.areDependenciesComplete(this.quest) && !teamData.isExcludedByOtherQuestline(this.quest);
        ClientPlayerEntity player = MinecraftClient.getInstance().player;
        if (canStart) {
            if (isCompleted) {
                if (teamData.hasUnclaimedRewards(player.getUuid(), this.quest)) {
                    questIcon = ThemeProperties.ALERT_ICON.get(this.quest);
                } else if (teamData.isQuestPinned(player, this.quest.id)) {
                    questIcon = ThemeProperties.PIN_ICON_ON.get();
                } else {
                    questIcon = ThemeProperties.CHECK_ICON.get(this.quest);
                }

                outlineColor = ThemeProperties.QUEST_COMPLETED_COLOR.get(this.quest);
            } else if (isStarted) {
                if (teamData.areDependenciesComplete(this.quest)) {
                    outlineColor = ThemeProperties.QUEST_STARTED_COLOR.get(this.quest);
                }

                if (this.quest.getProgressionMode() == ProgressionMode.FLEXIBLE && this.quest.allTasksCompleted(teamData)) {
                    questIcon = new ThemeProperties.CheckIcon(Color4I.rgb(6316128), Color4I.rgb(8421504));
                }
            }
        } else {
            outlineColor = ThemeProperties.QUEST_LOCKED_COLOR.get(this.quest);
        }

        if (questIcon == Color4I.empty() && teamData.isQuestPinned(player, this.quest.id)) {
            questIcon = ThemeProperties.PIN_ICON_ON.get();
        }

        if (file.canEdit() && !this.quest.isVisible(teamData)) {
            hiddenIcon = ThemeProperties.HIDDEN_ICON.get();
        }

        MatrixStack poseStack = graphics.getMatrices();
        QuestShape shape = QuestShape.get(this.getShape());
        float scale = 1.0f + 0.1f * factor;
        float dx = (w * scale - w) / 2f;
        float dy = (h * scale - h) / 2f;

        // Draw selected/viewed quest indicator BEFORE main shape to avoid overlapping quest details text
        if (this.questScreen.getViewedQuest() == this.quest || selectedObjects.contains(this.moveAndDeleteFocus())) {
            poseStack.push();
            poseStack.translate(x - dx, y - dy, -1.0F);
            poseStack.scale(scale, scale, scale);
            Color4I col = Color4I.WHITE.withAlpha((int) ((double) 190.0F + Math.sin((double) System.currentTimeMillis() * 0.003) * (double) 50.0F));
            shape.getOutline().withColor(col).draw(graphics, 0, 0, w, h);
            shape.getBackground().withColor(col).draw(graphics, 0, 0, w, h);
            poseStack.pop();
        }

        if (shape.shouldDraw()) {
            poseStack.push();
            poseStack.translate(x - dx, y - dy, 0);
            poseStack.scale(scale, scale, scale);
            shape.getShape().withColor(Color4I.DARK_GRAY).draw(graphics, 0, 0, w, h);
            shape.getBackground().withColor(Color4I.WHITE.withAlpha(150)).draw(graphics, 0, 0, w, h);
            shape.getOutline().withColor(outlineColor).draw(graphics, 0, 0, w, h);
            poseStack.pop();
        }

        if (!this.icon.isEmpty()) {
            int s = (int) ((float) w * 0.6666667F * (float) this.quest.getIconScale());
            poseStack.push();
            poseStack.translate((double) x + (double) (w - s) / (double) 2.0F, (double) y + (double) (h - s) / (double) 2.0F, (double) 0.0F);
            poseStack.translate(-dx, -dy, 0);
            poseStack.scale(scale, scale, scale);
            this.icon.draw(graphics, 0, 0, s, s);
            poseStack.pop();
        }

        GuiHelper.setupDrawing();

        if (!canStart || !teamData.areDependenciesComplete(this.quest)) {
            if (shape.shouldDraw()) {
                shape.getShape().withColor(Color4I.BLACK.withAlpha(100)).draw(graphics, x, y, w, h);
            }

            if (this.quest.getQuestFile().showLockIcons() && (Boolean) FTBQuestsClientConfig.SHOW_LOCK_ICON.get()) {
                lockIcon = (Icon) ThemeProperties.LOCK_ICON.get();
            }
        }

        if (this.isMouseOver()) {
            shape.getShape().withColor(Color4I.WHITE.withAlpha((int) (QuestAnimationsConfig.QUEST_HOVER_GLOW_ALPHA.get() * factor))).draw(graphics, x, y, w, h);
        }

        //? if >= 1.21.1 {
        var offset = 900f;
        //?} else {
        /*var offset = 0f;
        *///?}

        if (!questIcon.isEmpty()) {
            int s = (int) ((float) w / 8.0F * 3.0F);
            poseStack.push();
            poseStack.translate(x + w - s - dx, y - dy, offset);
            poseStack.scale(scale, scale, scale);
            questIcon.draw(graphics, 0, 0, s, s);
            poseStack.pop();
        }

        if (!hiddenIcon.isEmpty()) {
            int s = (int) ((float) w / 8.0F * 3.0F);
            poseStack.push();
            poseStack.translate(x - dx, y - dy, offset);
            poseStack.scale(scale, scale, scale);
            hiddenIcon.draw(graphics, 0, 0, s, s);
            poseStack.pop();
        }

        if (!lockIcon.isEmpty() && !this.quest.shouldHideLockIcon()) {
            int s = (int) ((float) w / 8.0F * 3.0F);
            poseStack.push();
            poseStack.translate(x + w - s - dx, y + h - 1 - s - dy, offset);
            poseStack.scale(scale, scale, scale);
            lockIcon.draw(graphics, 0, 0, s, s);
            poseStack.pop();
        }

        ci.cancel();
    }

    @Inject(method = "onClicked", at = @At(value = "INVOKE", target = "Ldev/ftb/mods/ftbquests/client/gui/quests/QuestScreen;open(Ldev/ftb/mods/ftbquests/quest/QuestObject;Z)V", shift = At.Shift.AFTER))
    private void cqa$onClicked(MouseButton button, CallbackInfo ci) {
        ((QuestPanelAnimator) questScreen.viewQuestPanel).cqa$triggerAnimation();
    }
}
