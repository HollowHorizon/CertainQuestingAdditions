package ru.hollowhorizon.additions.questing.mixins;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.mojang.blaze3d.systems.RenderSystem;
import dev.ftb.mods.ftblibrary.icon.Color4I;
import dev.ftb.mods.ftblibrary.icon.Icon;
import dev.ftb.mods.ftblibrary.icon.ImageIcon;
import dev.ftb.mods.ftblibrary.ui.Panel;
import dev.ftb.mods.ftblibrary.ui.Theme;
import dev.ftb.mods.ftblibrary.ui.Widget;
import dev.ftb.mods.ftbquests.client.gui.quests.QuestButton;
import dev.ftb.mods.ftbquests.client.gui.quests.QuestPanel;
import dev.ftb.mods.ftbquests.client.gui.quests.QuestScreen;
import dev.ftb.mods.ftbquests.quest.ProgressionMode;
import dev.ftb.mods.ftbquests.quest.Quest;
import dev.ftb.mods.ftbquests.quest.QuestShape;
import dev.ftb.mods.ftbquests.quest.theme.property.ThemeProperties;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.util.math.MatrixStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.hollowhorizon.additions.questing.client.Animator;
import ru.hollowhorizon.additions.questing.client.QuestButtonAnimator;
import ru.hollowhorizon.additions.questing.client.QuestScreenZoom;
import ru.hollowhorizon.additions.questing.config.QuestAnimationsConfig;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Mixin(value = QuestPanel.class, remap = false)
public abstract class QuestPanelMixin extends Panel {
    @Shadow @Final private QuestScreen questScreen;
    @Shadow @Final private static ImageIcon DEFAULT_DEPENDENCY_LINE_TEXTURE;

    //? if >= 1.21.1 {
    /*@Shadow protected abstract void renderConnection(Widget widget, QuestButton button, MatrixStack poseStack, float s, int r, int g, int b, int a, int a1, float mu, Tessellator tesselator);
    *///?} else {
    @Shadow protected abstract void renderConnection(Widget widget, QuestButton button, MatrixStack poseStack, BufferBuilder buffer, float s, int r, int g, int b, int a, int a1, float mu, Tessellator tesselator);
    //?}
    public QuestPanelMixin(Panel panel) {
        super(panel);
    }

    @Inject(method = "drawOffsetBackground", at = @At("HEAD"), cancellable = true)
    private void onDrawOffscreenBackground(DrawContext graphics, Theme theme, int x, int y, int w, int h, CallbackInfo ci) {
        var selectedChapter = ((QuestScreenAccessor) questScreen).getSelectedChapter();
        var file = ((QuestScreenAccessor) questScreen).getFile();

        //? if >= 1.21.1 {
        /*if (selectedChapter != null && file.selfTeamData != null) {
            Tessellator tesselator = Tessellator.getInstance();
            Icon icon = (Icon)ThemeProperties.DEPENDENCY_LINE_TEXTURE.get(selectedChapter);
            if (icon instanceof ImageIcon) {
                ImageIcon img = (ImageIcon)icon;
                img.bindTexture();
            } else {
                DEFAULT_DEPENDENCY_LINE_TEXTURE.bindTexture();
            }

            Quest selectedQuest = this.questScreen.getViewedQuest();
            if (selectedQuest == null) {
                Collection<Quest> sel = this.questScreen.getSelectedQuests();
                if (sel.size() == 1) {
                    selectedQuest = this.questScreen.getSelectedQuests().stream().findFirst().orElse(null);
                }
            }

            double mt = -((double)System.currentTimeMillis() * 0.001);
            float zoom = QuestAnimationsConfig.SMOOTH_SCROLLING.get() ? ((QuestScreenZoom) this.questScreen).cqa$getZoom() : questScreen.getZoom();
            float lineWidth = (float)(zoom * ThemeProperties.DEPENDENCY_LINE_THICKNESS.get(selectedChapter) / (double)4.0F * (double)3.0F);

            RenderSystem.setShader(GameRenderer::getPositionTexColorProgram);
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
            float mu = (float)(mt * (Double)ThemeProperties.DEPENDENCY_LINE_UNSELECTED_SPEED.get(selectedChapter) % (double)1.0F);

            for(Widget widget : this.widgets) {
                if (widget.shouldDraw() && widget instanceof QuestButton) {
                    QuestButton qb = (QuestButton)widget;
                    var quest = ((QuestButtonAccessor) qb).getQuest();
                    if (!quest.shouldHideDependencyLines() || qb.isMouseOver()) {
                        boolean unavailable = !file.selfTeamData.canStartTasks(quest);
                        boolean complete = !unavailable && file.selfTeamData.isCompleted(quest);
                        Color4I c = complete ? ThemeProperties.DEPENDENCY_LINE_COMPLETED_COLOR.get(selectedChapter) : ThemeProperties.DEPENDENCY_LINE_UNCOMPLETED_COLOR.get(selectedChapter);
                        if (unavailable || quest.getProgressionMode() == ProgressionMode.FLEXIBLE && !file.selfTeamData.areDependenciesComplete(quest)) {
                            c = c.withAlpha(Math.max(30, c.alphai() / 2));
                        }

                        for(QuestButton button : qb.getDependencies()) {
                            if (button.shouldDraw() && quest != selectedQuest && !quest.shouldHideDependentLines()) {
                                this.renderConnection(widget, button, graphics.getMatrices(), lineWidth, c.redi(), c.greeni(), c.bluei(), c.alphai(), c.alphai(), mu, tesselator);
                            }
                        }
                    }
                }
            }

            float ms = (float)(mt * (Double)ThemeProperties.DEPENDENCY_LINE_SELECTED_SPEED.get(selectedChapter) % (double)1.0F);
            List<QuestButton> toOutline = new ArrayList();

            for(Widget widget : this.widgets) {
                if (widget.shouldDraw() && widget instanceof QuestButton) {
                    QuestButton qb = (QuestButton) widget;
                    var quest = ((QuestButtonAccessor) qb).getQuest();
                    if (!quest.shouldHideDependencyLines() || qb.isMouseOver()) {
                        for(QuestButton button : qb.getDependencies()) {
                            if (button.shouldDraw()) {
                                boolean unavailable = !file.selfTeamData.canStartTasks(quest);
                                boolean complete = !unavailable && file.selfTeamData.isCompleted(quest);
                                Color4I original = complete ? ThemeProperties.DEPENDENCY_LINE_COMPLETED_COLOR.get(selectedChapter) : ThemeProperties.DEPENDENCY_LINE_UNCOMPLETED_COLOR.get(selectedChapter);
                                if (((QuestButtonAccessor) button).getQuest() != selectedQuest && !button.isMouseOver()) {
                                    if (quest == selectedQuest || qb.isMouseOver()) {
                                        Color4I c = original.lerp(ThemeProperties.DEPENDENCY_LINE_REQUIRES_COLOR.get(selectedChapter), ((QuestButtonAnimator) qb).cqa$getAnimator().get());
                                        this.renderConnection(widget, button, graphics.getMatrices(), lineWidth, c.redi(), c.greeni(), c.bluei(), c.alphai(), c.alphai(), ms, tesselator);
                                    }
                                } else {
                                    Color4I c = original.lerp(ThemeProperties.DEPENDENCY_LINE_REQUIRED_FOR_COLOR.get(selectedChapter), ((QuestButtonAnimator) button).cqa$getAnimator().get());
                                    int a;
                                    int a2;
                                    if (qb.shouldDraw()) {
                                        a = a2 = c.alphai();
                                    } else {
                                        a = c.alphai() / 4 * 3;
                                        a2 = 30;
                                        toOutline.add(qb);
                                    }

                                    this.renderConnection(widget, button, graphics.getMatrices(), lineWidth, c.redi(), c.greeni(), c.bluei(), a2, a, ms, tesselator);
                                }
                            }
                        }
                    }
                }
            }

            toOutline.forEach((qbx) -> {
                var quest = ((QuestButtonAccessor) qbx).getQuest();
                QuestShape.get(quest.getShape()).getShape().withColor(Color4I.BLACK.withAlpha(30)).draw(graphics, qbx.getX(), qbx.getY(), qbx.width, qbx.height);
                QuestShape.get(quest.getShape()).getOutline().withColor(Color4I.BLACK.withAlpha(90)).draw(graphics, qbx.getX(), qbx.getY(), qbx.width, qbx.height);
            });
        }
        *///?} else {
        

        if (selectedChapter != null && file.selfTeamData != null) {
            Tessellator tesselator = Tessellator.getInstance();
            BufferBuilder buffer = tesselator.getBuffer();
            Icon icon = (Icon) ThemeProperties.DEPENDENCY_LINE_TEXTURE.get(selectedChapter);
            if (icon instanceof ImageIcon) {
                ImageIcon img = (ImageIcon)icon;
                img.bindTexture();
            } else {
                DEFAULT_DEPENDENCY_LINE_TEXTURE.bindTexture();
            }

            Quest selectedQuest = this.questScreen.getViewedQuest();
            if (selectedQuest == null) {
                Collection<Quest> sel = this.questScreen.getSelectedQuests();
                if (sel.size() == 1) {
                    selectedQuest = (Quest)this.questScreen.getSelectedQuests().stream().findFirst().orElse(null);
                }
            }

            double mt = -((double)System.currentTimeMillis() * 0.001);
            float zoom = QuestAnimationsConfig.SMOOTH_SCROLLING.get() ? ((QuestScreenZoom) this.questScreen).cqa$getZoom() : questScreen.getZoom();
            float lineWidth = (float)(zoom * ThemeProperties.DEPENDENCY_LINE_THICKNESS.get(selectedChapter) / (double)4.0F * (double)3.0F);
            RenderSystem.setShader(GameRenderer::getPositionColorTexProgram);
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
            float mu = (float)(mt * (Double)ThemeProperties.DEPENDENCY_LINE_UNSELECTED_SPEED.get(selectedChapter) % (double)1.0F);

            for(Widget widget : this.widgets) {
                if (widget.shouldDraw() && widget instanceof QuestButton) {
                    QuestButton qb = (QuestButton) widget;
                    var quest = ((QuestButtonAccessor) qb).getQuest();
                    if (!quest.shouldHideDependencyLines() || qb.isMouseOver()) {
                        boolean unavailable = !file.selfTeamData.canStartTasks(quest);
                        boolean complete = !unavailable && file.selfTeamData.isCompleted(quest);
                        Color4I c = complete ? ThemeProperties.DEPENDENCY_LINE_COMPLETED_COLOR.get(selectedChapter) : ThemeProperties.DEPENDENCY_LINE_UNCOMPLETED_COLOR.get(selectedChapter);
                        if (unavailable || quest.getProgressionMode() == ProgressionMode.FLEXIBLE && !file.selfTeamData.areDependenciesComplete(quest)) {
                            c = c.withAlpha(Math.max(30, c.alphai() / 2));
                        }

                        for(QuestButton button : qb.getDependencies()) {
                            if (button.shouldDraw() && quest != selectedQuest && quest != selectedQuest && !quest.shouldHideDependentLines()) {
                                this.renderConnection(widget, button, graphics.getMatrices(), buffer, lineWidth, c.redi(), c.greeni(), c.bluei(), c.alphai(), c.alphai(), mu, tesselator);
                            }
                        }
                    }
                }
            }

            float ms = (float)(mt * ThemeProperties.DEPENDENCY_LINE_SELECTED_SPEED.get(selectedChapter) % (double)1.0F);
            List<QuestButton> toOutline = new ArrayList();

            for(Widget widget : this.widgets) {
                if (widget.shouldDraw() && widget instanceof QuestButton) {
                    QuestButton qb = (QuestButton)widget;
                    var quest = ((QuestButtonAccessor) qb).getQuest();
                    if (!quest.shouldHideDependencyLines()) {
                        for(QuestButton button : qb.getDependencies()) {
                            if (button.shouldDraw()) {
                                boolean unavailable = !file.selfTeamData.canStartTasks(quest);
                                boolean complete = !unavailable && file.selfTeamData.isCompleted(quest);
                                Color4I original = complete ? ThemeProperties.DEPENDENCY_LINE_COMPLETED_COLOR.get(selectedChapter) : ThemeProperties.DEPENDENCY_LINE_UNCOMPLETED_COLOR.get(selectedChapter);
                                if (unavailable || quest.getProgressionMode() == ProgressionMode.FLEXIBLE && !file.selfTeamData.areDependenciesComplete(quest)) {
                                    original = original.withAlpha(Math.max(30, original.alphai() / 2));
                                }

                                if (((QuestButtonAccessor) button).getQuest() != selectedQuest && !button.isMouseOver()) {
                                    if (quest == selectedQuest || qb.isMouseOver()) {
                                        Color4I c = original.lerp(ThemeProperties.DEPENDENCY_LINE_REQUIRES_COLOR.get(selectedChapter), ((QuestButtonAnimator) qb).cqa$getAnimator().get());
                                        this.renderConnection(widget, button, graphics.getMatrices(), buffer, lineWidth, c.redi(), c.greeni(), c.bluei(), c.alphai(), c.alphai(), ms, tesselator);
                                    }
                                } else {
                                    Color4I c = original.lerp(ThemeProperties.DEPENDENCY_LINE_REQUIRED_FOR_COLOR.get(selectedChapter), ((QuestButtonAnimator) button).cqa$getAnimator().get());
                                    int a;
                                    int a2;
                                    if (qb.shouldDraw()) {
                                        a = a2 = c.alphai();
                                    } else {
                                        a = c.alphai() / 4 * 3;
                                        a2 = 30;
                                        toOutline.add(qb);
                                    }

                                    this.renderConnection(widget, button, graphics.getMatrices(), buffer, lineWidth, c.redi(), c.greeni(), c.bluei(), a2, a, ms, tesselator);
                                }
                            }
                        }
                    }
                }
            }

            toOutline.forEach((qbx) -> {
                var quest = ((QuestButtonAccessor) qbx).getQuest();
                QuestShape.get(quest.getShape()).getShape().withColor(Color4I.BLACK.withAlpha(30)).draw(graphics, qbx.getX(), qbx.getY(), qbx.width, qbx.height);
                QuestShape.get(quest.getShape()).getOutline().withColor(Color4I.BLACK.withAlpha(90)).draw(graphics, qbx.getX(), qbx.getY(), qbx.width, qbx.height);
            });

        }
        //?}

        ci.cancel();
    }
}
