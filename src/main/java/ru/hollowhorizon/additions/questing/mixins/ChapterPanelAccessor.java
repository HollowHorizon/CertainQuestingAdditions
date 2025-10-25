package ru.hollowhorizon.additions.questing.mixins;

import dev.ftb.mods.ftbquests.client.gui.quests.ChapterPanel;
import dev.ftb.mods.ftbquests.client.gui.quests.QuestScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value = ChapterPanel.class, remap = false)
public interface ChapterPanelAccessor {
    @Accessor("questScreen")
    QuestScreen getQuestScreen();
}
