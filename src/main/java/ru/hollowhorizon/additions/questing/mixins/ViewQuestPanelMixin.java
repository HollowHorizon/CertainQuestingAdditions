package ru.hollowhorizon.additions.questing.mixins;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import dev.ftb.mods.ftblibrary.ui.ModalPanel;
import dev.ftb.mods.ftblibrary.ui.Panel;
import dev.ftb.mods.ftblibrary.ui.Theme;
import dev.ftb.mods.ftbquests.client.gui.quests.ViewQuestPanel;
import net.minecraft.client.gui.DrawContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import ru.hollowhorizon.additions.questing.client.Animator;
import ru.hollowhorizon.additions.questing.config.QuestAnimationsConfig;

@Mixin(value = ViewQuestPanel.class, remap = false)
public abstract class ViewQuestPanelMixin extends ModalPanel {

    @Unique
    private final Animator mou$animator = new Animator(0f, 0.3f, (i) -> 1 - (1 - i) * (1 - i) * (1 - i));

    public ViewQuestPanelMixin(Panel panel) {
        super(panel);
    }

    @Override
    public void refreshWidgets() {
        super.refreshWidgets();
        mou$animator.set(1f, 0f);
    }

    @WrapMethod(method = "draw")
    private void onDraw(DrawContext graphics, Theme theme, int x, int y, int w, int h, Operation<Void> original) {
        if(!QuestAnimationsConfig.PANEL_ANIMATION.get()) {
            original.call(graphics, theme, x, y, w, h);
            return;
        }

        mou$animator.update();
        setY(y + Math.round(20f - 20f * mou$animator.get()));
        original.call(graphics, theme, x, y + Math.round(20f - 20f * mou$animator.get()), w, h);
        setY(y);
    }
}
