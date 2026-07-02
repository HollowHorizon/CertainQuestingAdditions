package ru.hollowhorizon.additions.questing.mixins;

//? if >= 1.21.11 {
import com.mojang.blaze3d.pipeline.RenderPipeline;
import net.minecraft.client.gl.RenderPipelines;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(RenderPipelines.class)
public interface RenderPipelinesAccessor {
    @Invoker("register")
    static RenderPipeline cqa$register(RenderPipeline pipeline) {
        throw new AssertionError();
    }
}
//?} else {
/*public interface RenderPipelinesAccessor {
}
*///?}
