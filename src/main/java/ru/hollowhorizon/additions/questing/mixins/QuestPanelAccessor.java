package ru.hollowhorizon.additions.questing.mixins;

import dev.ftb.mods.ftbquests.client.gui.quests.QuestPanel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value = QuestPanel.class, remap = false)
public interface QuestPanelAccessor {
    @Accessor("centerQuestX")
    double getCenterQuestX();

    @Accessor("centerQuestY")
    double getCenterQuestY();


}
