package ru.hollowhorizon.additions.questing.mixins;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
//? if >= 1.21.11 {
import dev.ftb.mods.ftblibrary.client.config.EditableConfigGroup;
import dev.ftb.mods.ftblibrary.client.config.editable.EditableColor;
import dev.ftb.mods.ftblibrary.client.config.editable.EditableConfigValue;
import dev.ftb.mods.ftblibrary.client.config.editable.EditableInt;
//?} else {
/*import dev.ftb.mods.ftblibrary.config.ColorConfig;
import dev.ftb.mods.ftblibrary.config.ConfigGroup;
import dev.ftb.mods.ftblibrary.config.ConfigValue;
import dev.ftb.mods.ftblibrary.config.IntConfig;
*///?}
import dev.ftb.mods.ftblibrary.icon.Color4I;
import dev.ftb.mods.ftblibrary.icon.Icon;
import dev.ftb.mods.ftbquests.quest.ChapterImage;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import ru.hollowhorizon.additions.questing.client.EntityIconConfigEntries;
import ru.hollowhorizon.additions.questing.client.EntityIcon;

import java.util.function.Consumer;

@Mixin(value = ChapterImage.class, remap = false)
public abstract class ChapterImageMixin {
    @Shadow private Icon image;

    @Shadow
    public abstract ChapterImage setImage(Icon image);

    //? if >= 1.21.11 {
    @WrapOperation(method = "fillConfigGroup", at = @At(value = "INVOKE", target = "Ldev/ftb/mods/ftblibrary/client/config/EditableConfigGroup;add(Ljava/lang/String;Ldev/ftb/mods/ftblibrary/client/config/editable/EditableConfigValue;Ljava/lang/Object;Ljava/util/function/Consumer;Ljava/lang/Object;)Ldev/ftb/mods/ftblibrary/client/config/editable/EditableConfigValue;", ordinal = 0))
    //?} else {
    /*@WrapOperation(method = "fillConfigGroup", at = @At(value = "INVOKE", target = "Ldev/ftb/mods/ftblibrary/config/ConfigGroup;add(Ljava/lang/String;Ldev/ftb/mods/ftblibrary/config/ConfigValue;Ljava/lang/Object;Ljava/util/function/Consumer;Ljava/lang/Object;)Ldev/ftb/mods/ftblibrary/config/ConfigValue;", ordinal = 0))
    *///?}
    @SuppressWarnings({"rawtypes"})
    //? if >= 1.21.11 {
    private EditableConfigValue<?> cqa$useEntityImageConfig(EditableConfigGroup group, String id, EditableConfigValue<?> type, Object value, Consumer setter, Object defaultValue, Operation<EditableConfigValue<?>> original) {
    //?} else {
    /*private ConfigValue cqa$useEntityImageConfig(ConfigGroup group, String id, ConfigValue type, Object value, Consumer setter, Object defaultValue, Operation<ConfigValue> original) {
    *///?}
        if (image instanceof EntityIcon entityIcon) {
            return EntityIconConfigEntries.add(group, id, entityIcon, this::setImage);
        }

        return original.call(group, id, type, value, setter, defaultValue);
    }

    //? if >= 1.21.11 {
    @WrapOperation(method = "fillConfigGroup", at = @At(value = "INVOKE", target = "Ldev/ftb/mods/ftblibrary/client/config/EditableConfigGroup;addColor(Ljava/lang/String;Ldev/ftb/mods/ftblibrary/icon/Color4I;Ljava/util/function/Consumer;Ldev/ftb/mods/ftblibrary/icon/Color4I;)Ldev/ftb/mods/ftblibrary/client/config/editable/EditableColor;"))
    private EditableColor cqa$renameEntityTintConfig(EditableConfigGroup group, String id, Color4I value, Consumer<Color4I> setter, Color4I defaultValue, Operation<EditableColor> original) {
        EditableColor config = original.call(group, id, value, setter, defaultValue);
        return image instanceof EntityIcon ? EntityIconConfigEntries.setEntityTintName(config) : config;
    }
    //?} else {
    /*@WrapOperation(method = "fillConfigGroup", at = @At(value = "INVOKE", target = "Ldev/ftb/mods/ftblibrary/config/ConfigGroup;addColor(Ljava/lang/String;Ldev/ftb/mods/ftblibrary/icon/Color4I;Ljava/util/function/Consumer;Ldev/ftb/mods/ftblibrary/icon/Color4I;)Ldev/ftb/mods/ftblibrary/config/ColorConfig;"))
    private ColorConfig cqa$renameEntityTintConfig(ConfigGroup group, String id, Color4I value, Consumer<Color4I> setter, Color4I defaultValue, Operation<ColorConfig> original) {
        ColorConfig config = original.call(group, id, value, setter, defaultValue);
        return image instanceof EntityIcon ? EntityIconConfigEntries.setEntityTintName(config) : config;
    }
    *///?}

    //? if >= 1.21.11 {
    @WrapOperation(method = "fillConfigGroup", at = @At(value = "INVOKE", target = "Ldev/ftb/mods/ftblibrary/client/config/EditableConfigGroup;addInt(Ljava/lang/String;ILjava/util/function/Consumer;III)Ldev/ftb/mods/ftblibrary/client/config/editable/EditableInt;", ordinal = 0))
    private EditableInt cqa$renameEntityOrderConfig(EditableConfigGroup group, String id, int value, Consumer<Integer> setter, int defaultValue, int min, int max, Operation<EditableInt> original) {
        EditableInt config = original.call(group, id, value, setter, defaultValue, min, max);
        return image instanceof EntityIcon ? EntityIconConfigEntries.setEntityOrderName(config) : config;
    }
    //?} else {
    /*@WrapOperation(method = "fillConfigGroup", at = @At(value = "INVOKE", target = "Ldev/ftb/mods/ftblibrary/config/ConfigGroup;addInt(Ljava/lang/String;ILjava/util/function/Consumer;III)Ldev/ftb/mods/ftblibrary/config/IntConfig;", ordinal = 0))
    private IntConfig cqa$renameEntityOrderConfig(ConfigGroup group, String id, int value, Consumer<Integer> setter, int defaultValue, int min, int max, Operation<IntConfig> original) {
        IntConfig config = original.call(group, id, value, setter, defaultValue, min, max);
        return image instanceof EntityIcon ? EntityIconConfigEntries.setEntityOrderName(config) : config;
    }
    *///?}

    //? if >= 1.21.11 {
    @WrapOperation(method = "fillConfigGroup", at = @At(value = "INVOKE", target = "Ldev/ftb/mods/ftblibrary/client/config/EditableConfigGroup;addInt(Ljava/lang/String;ILjava/util/function/Consumer;III)Ldev/ftb/mods/ftblibrary/client/config/editable/EditableInt;", ordinal = 1))
    private EditableInt cqa$renameEntityAlphaConfig(EditableConfigGroup group, String id, int value, Consumer<Integer> setter, int defaultValue, int min, int max, Operation<EditableInt> original) {
        EditableInt config = original.call(group, id, value, setter, defaultValue, min, max);
        return image instanceof EntityIcon ? EntityIconConfigEntries.setEntityAlphaName(config) : config;
    }
    //?} else {
    /*@WrapOperation(method = "fillConfigGroup", at = @At(value = "INVOKE", target = "Ldev/ftb/mods/ftblibrary/config/ConfigGroup;addInt(Ljava/lang/String;ILjava/util/function/Consumer;III)Ldev/ftb/mods/ftblibrary/config/IntConfig;", ordinal = 1))
    private IntConfig cqa$renameEntityAlphaConfig(ConfigGroup group, String id, int value, Consumer<Integer> setter, int defaultValue, int min, int max, Operation<IntConfig> original) {
        IntConfig config = original.call(group, id, value, setter, defaultValue, min, max);
        return image instanceof EntityIcon ? EntityIconConfigEntries.setEntityAlphaName(config) : config;
    }
    *///?}
}
