package ru.hollowhorizon.additions.questing.mixins;

//? if >= 1.21.1 && < 1.21.11 {
/*import dev.ftb.mods.ftbquests.quest.ChapterImage;
import dev.ftb.mods.ftbquests.quest.QuestObjectBase;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import ru.hollowhorizon.additions.questing.client.EntityIcon;
import ru.hollowhorizon.additions.questing.client.EntityIconConfigEntries;

@Pseudo
@Mixin(targets = "dev.ftb.mods.ftbquests.quest.QuestObjectBase$1", remap = false)
public abstract class QuestObjectConfigGroupMixin {
    @Shadow @Final private QuestObjectBase this$0;

    @ModifyArg(method = "getName", at = @At(value = "INVOKE", target = "Lnet/minecraft/text/Text;translatable(Ljava/lang/String;)Lnet/minecraft/text/MutableText;", remap = true), index = 0)
    private String cqa$useEntityGroupName(String key) {
        return this$0 instanceof ChapterImage image && EntityIcon.isEntityIconString(image.getImage().toString())
                ? EntityIconConfigEntries.getGroupNameKey() : key;
    }
}
*///?} else {
public abstract class QuestObjectConfigGroupMixin {
}
//?}
