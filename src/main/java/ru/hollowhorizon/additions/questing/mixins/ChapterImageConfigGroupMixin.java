package ru.hollowhorizon.additions.questing.mixins;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import ru.hollowhorizon.additions.questing.client.EntityIcon;
import ru.hollowhorizon.additions.questing.client.EntityIconConfigEntries;

// Used before FTB Quests 2101.1.28 on 1.21.1, and by the separate 1.21.11 branch.
@Pseudo
@Mixin(targets = "dev.ftb.mods.ftbquests.client.gui.quests.ChapterImageButton$3", remap = false)
public abstract class ChapterImageConfigGroupMixin {
    @Shadow @Final private String val$name;

    @ModifyConstant(method = "getName", constant = @Constant(stringValue = "ftbquests.chapter.image"))
    private String cqa$useEntityGroupName(String key) {
        return EntityIcon.isEntityIconString(val$name) ? EntityIconConfigEntries.getGroupNameKey() : key;
    }
}
