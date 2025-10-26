package ru.hollowhorizon.additions.questing.mixins;

import dev.ftb.mods.ftblibrary.ui.Panel;
import dev.ftb.mods.ftbquests.client.gui.quests.ChapterPanel;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.math.MathHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import ru.hollowhorizon.additions.questing.config.QuestAnimationsConfig;

@Mixin(value = ChapterPanel.class, remap = false)
public abstract class ChapterPanelMixin extends Panel {
    public ChapterPanelMixin(Panel panel) {
        super(panel);
    }

    //? if <= 1.20.1 {
    /*@Shadow
    boolean expanded;
    @Unique
    private int cqa$curX;
    @Unique
    private int cqa$prevX;

    @Inject(method = "alignWidgets", at = @At("TAIL"))
    private void onAlign(CallbackInfo ci) {
        if(!QuestAnimationsConfig.CHAPTER_PANEL.get()) return;
        cqa$curX = expanded ? 0 : -width;
    }

    @Override
    public void tick() {
        super.tick();
        if(!QuestAnimationsConfig.CHAPTER_PANEL.get()) return;
        cqa$prevX = cqa$curX;
        if (expanded && cqa$curX < 0) {
            cqa$curX = Math.min(cqa$curX + 40, 0);
        } else if (!expanded && cqa$curX > -width) {
            cqa$curX = Math.max(cqa$curX - 40, -width);
        }
    }

    @Inject(method = "getX", at = @At("HEAD"), cancellable = true)
    public void onGetX(CallbackInfoReturnable<Integer> cir) {
        if(!QuestAnimationsConfig.CHAPTER_PANEL.get()) return;
        cir.setReturnValue(MathHelper.lerp(MinecraftClient.getInstance().getTickDelta(), cqa$prevX, cqa$curX));
    }

    *///?}
}
