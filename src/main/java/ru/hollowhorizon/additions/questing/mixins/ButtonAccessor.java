package ru.hollowhorizon.additions.questing.mixins;

import dev.ftb.mods.ftblibrary.icon.Icon;
import dev.ftb.mods.ftblibrary.ui.Button;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value = Button.class, remap = false)
public interface ButtonAccessor {
    @Accessor("icon")
    Icon cqa$getIcon();
}
