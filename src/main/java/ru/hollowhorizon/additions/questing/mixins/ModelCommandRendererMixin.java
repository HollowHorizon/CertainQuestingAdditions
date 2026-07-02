package ru.hollowhorizon.additions.questing.mixins;

//? if >= 1.21.11 {
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.model.Model;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.command.ModelCommandRenderer;
import net.minecraft.client.util.math.MatrixStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import ru.hollowhorizon.additions.questing.client.EntityIconLighting;
import ru.hollowhorizon.additions.questing.client.EntityIconRenderContext;

@Mixin(ModelCommandRenderer.class)
public abstract class ModelCommandRendererMixin {
    @WrapOperation(
            method = {
                    "render(Lnet/minecraft/client/render/command/OrderedRenderCommandQueueImpl$ModelCommand;Lnet/minecraft/client/render/RenderLayer;Lnet/minecraft/client/render/VertexConsumer;Lnet/minecraft/client/render/OutlineVertexConsumerProvider;Lnet/minecraft/client/render/VertexConsumerProvider$Immediate;)V",
                    "method_73000(Lnet/minecraft/class_11661$class_11670;Lnet/minecraft/class_1921;Lnet/minecraft/class_4588;Lnet/minecraft/class_4618;Lnet/minecraft/class_4597$class_4598;)V",
                    "a(Lhpp$h;Lijs;Lfzp;Lhoq;Lhon$a;)V"
            },
            at = {
                    @At(
                            value = "INVOKE",
                            target = "Lnet/minecraft/client/model/Model;render(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumer;III)V",
                            remap = false
                    ),
                    @At(
                            value = "INVOKE",
                            target = "Lnet/minecraft/class_3879;method_62100(Lnet/minecraft/class_4587;Lnet/minecraft/class_4588;III)V",
                            remap = false
                    ),
                    @At(
                            value = "INVOKE",
                            target = "Lgzp;a(Lfzm;Lfzp;III)V",
                            remap = false
                    )
            },
            remap = false
    )
    private <S> void cqa$renderEntityIconModelFlat(
            Model<S> model,
            MatrixStack matrices,
            VertexConsumer vertices,
            int light,
            int overlay,
            int color,
            Operation<Void> original
    ) {
        VertexConsumer iconVertices = EntityIconRenderContext.isRenderingEntityIcon()
                ? EntityIconLighting.flat(vertices)
                : vertices;
        original.call(model, matrices, iconVertices, light, overlay, color);
    }
}
//?} else {
/*public abstract class ModelCommandRendererMixin {
}
*///?}
