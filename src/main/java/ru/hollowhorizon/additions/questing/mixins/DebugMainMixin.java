package ru.hollowhorizon.additions.questing.mixins;

import net.minecraft.client.main.Main;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import java.nio.file.Files;
import java.nio.file.Path;

@Mixin(Main.class)
public class DebugMainMixin {
    @Inject(method = "main", at = @At("HEAD"), remap = false)
    private static void preMain(CallbackInfo ci) {

        String path = System.getProperty("java.library.path");
        String name = System.mapLibraryName("renderdoc");
        boolean detected = false;
        for (String folder : path.split(";")) {
            if (Files.exists(Path.of(folder + "/" + name))) {
                detected = true;
                break;
            }
        }


        if (!detected) {
            var renderDoc = Path.of("C:/Program Files/RenderDoc/renderdoc.dll");
            if (Files.exists(renderDoc)) {
                try {
                    System.load("C:/Program Files/RenderDoc/renderdoc.dll");
                } catch (Throwable e) {
                }
            }
            return;
        }

        try {
            System.loadLibrary("renderdoc");
        } catch (Throwable e) {
        }
    }
}
