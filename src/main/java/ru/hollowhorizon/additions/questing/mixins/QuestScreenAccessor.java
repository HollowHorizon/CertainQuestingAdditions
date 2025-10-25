package ru.hollowhorizon.additions.questing.mixins;

import dev.ftb.mods.ftbquests.client.ClientQuestFile;
import dev.ftb.mods.ftbquests.client.gui.quests.QuestScreen;
import dev.ftb.mods.ftbquests.quest.Chapter;
import dev.ftb.mods.ftbquests.quest.Movable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.List;

@Mixin(value = QuestScreen.class, remap = false)
public interface QuestScreenAccessor {
    @Accessor("file")
    ClientQuestFile getFile();

    @Accessor("selectedObjects")
    List<Movable> getSelectedObjects();

    @Accessor("selectedChapter")
    Chapter getSelectedChapter();

    @Accessor("scrollWidth")
    double getScrollWidth();
    @Accessor("scrollHeight")
    double getScrollHeight();
}
