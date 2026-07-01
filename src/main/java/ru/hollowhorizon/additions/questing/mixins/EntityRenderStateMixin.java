package ru.hollowhorizon.additions.questing.mixins;

//? if >= 1.21.11 {
import net.minecraft.client.render.entity.state.EntityRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import ru.hollowhorizon.additions.questing.client.EntityIconRenderStateMarker;

@Mixin(EntityRenderState.class)
public abstract class EntityRenderStateMixin implements EntityIconRenderStateMarker {
    @Unique
    private boolean cqa$entityIcon;

    @Override
    public void cqa$markEntityIcon() {
        cqa$entityIcon = true;
    }

    @Override
    public boolean cqa$isEntityIcon() {
        return cqa$entityIcon;
    }
}
//?} else {
/*public abstract class EntityRenderStateMixin {
}
*///?}
