package ru.hollowhorizon.additions.questing.mixins;

//? if >= 1.21.11 {
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.minecraft.client.gui.render.EntityGuiElementRenderer;
import net.minecraft.client.gui.render.state.special.EntityGuiElementRenderState;
import net.minecraft.client.util.math.MatrixStack;
import org.spongepowered.asm.mixin.Mixin;
import ru.hollowhorizon.additions.questing.client.EntityIconRenderContext;
import ru.hollowhorizon.additions.questing.client.EntityIconRenderStateMarker;

@Mixin(EntityGuiElementRenderer.class)
public abstract class EntityGuiElementRendererMixin {
    @WrapMethod(
            method = {
                    "render(Lnet/minecraft/client/gui/render/state/special/EntityGuiElementRenderState;Lnet/minecraft/client/util/math/MatrixStack;)V",
                    "method_70909(Lnet/minecraft/class_11252;Lnet/minecraft/class_4587;)V",
                    "a(Lgqn;Lfzm;)V"
            },
            remap = false
    )
    private void cqa$renderEntityIcon(
            EntityGuiElementRenderState state,
            MatrixStack matrices,
            Operation<Void> original
    ) {
        if (state.renderState() instanceof EntityIconRenderStateMarker marker && marker.cqa$isEntityIcon()) {
            EntityIconRenderContext.renderingEntityIcon(() -> {
                original.call(state, matrices);
            });
            return;
        }

        original.call(state, matrices);
    }
}
//?} else {
/*public abstract class EntityGuiElementRendererMixin {
}
*///?}
