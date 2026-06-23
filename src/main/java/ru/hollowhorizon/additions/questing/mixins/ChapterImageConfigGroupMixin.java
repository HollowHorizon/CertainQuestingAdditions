package ru.hollowhorizon.additions.questing.mixins;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import ru.hollowhorizon.additions.questing.client.EntityIcon;

@Pseudo
@Mixin(targets = "dev.ftb.mods.ftbquests.client.gui.quests.ChapterImageButton$3", remap = false)
public abstract class ChapterImageConfigGroupMixin {
    private static final String ENTITY_GROUP_NAME_KEY = "certain_questing_additions.entity_attachment.config_group";

    @Shadow @Final private String val$name;

    @ModifyConstant(method = "getName", constant = @Constant(stringValue = "ftbquests.chapter.image"))
    private String cqa$useEntityGroupName(String key) {
        return EntityIcon.isEntityIconString(val$name) ? ENTITY_GROUP_NAME_KEY : key;
    }
}
