package ru.hollowhorizon.additions.questing.mixins;

//? if < 1.21.11 {
/*import net.minecraft.client.render.entity.EntityRenderDispatcher;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(EntityRenderDispatcher.class)
public interface EntityRenderDispatcherAccessor {
    @Accessor("renderShadows")
    boolean cqa$getRenderShadows();
}
*///?} else {
public interface EntityRenderDispatcherAccessor {
}
//?}
