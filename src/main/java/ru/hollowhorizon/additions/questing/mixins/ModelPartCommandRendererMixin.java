package ru.hollowhorizon.additions.questing.mixins;

//? if >= 1.21.11 {
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.command.ModelPartCommandRenderer;
import net.minecraft.client.util.math.MatrixStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import ru.hollowhorizon.additions.questing.client.EntityIconLighting;
import ru.hollowhorizon.additions.questing.client.EntityIconRenderContext;

@Mixin(ModelPartCommandRenderer.class)
public abstract class ModelPartCommandRendererMixin {
    @WrapOperation(
            method = {
                    "render(Lnet/minecraft/client/render/command/BatchingRenderCommandQueue;Lnet/minecraft/client/render/VertexConsumerProvider$Immediate;Lnet/minecraft/client/render/OutlineVertexConsumerProvider;Lnet/minecraft/client/render/VertexConsumerProvider$Immediate;)V",
                    "method_73543(Lnet/minecraft/class_11788;Lnet/minecraft/class_4597$class_4598;Lnet/minecraft/class_4618;Lnet/minecraft/class_4597$class_4598;)V",
                    "a(Lhpn;Lhon$a;Lhoq;Lhon$a;)V"
            },
            at = {
                    @At(
                            value = "INVOKE",
                            target = "Lnet/minecraft/client/model/ModelPart;render(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumer;III)V",
                            remap = false
                    ),
                    @At(
                            value = "INVOKE",
                            target = "Lnet/minecraft/class_630;method_22699(Lnet/minecraft/class_4587;Lnet/minecraft/class_4588;III)V",
                            remap = false
                    ),
                    @At(
                            value = "INVOKE",
                            target = "Lhdg;a(Lfzm;Lfzp;III)V",
                            remap = false
                    )
            },
            remap = false
    )
    private void cqa$renderEntityIconModelPartFlat(
            ModelPart part,
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
        original.call(part, matrices, iconVertices, light, overlay, color);
    }
}
//?} else {
/*public abstract class ModelPartCommandRendererMixin {
}
*///?}
