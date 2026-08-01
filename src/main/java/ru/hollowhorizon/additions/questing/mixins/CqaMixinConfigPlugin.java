package ru.hollowhorizon.additions.questing.mixins;

import com.llamalad7.mixinextras.MixinExtrasBootstrap;
//? if forge {
/*import net.minecraftforge.fml.loading.LoadingModList;
*///?} elif fabric {
/*import net.fabricmc.loader.api.FabricLoader;
*///?} elif neoforge {
import net.neoforged.fml.loading.LoadingModList;
//?}
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import java.util.List;
import java.util.Set;

public final class CqaMixinConfigPlugin implements IMixinConfigPlugin {
    private static final String EMI_RECIPE_SCREEN_MIXIN = "ru.hollowhorizon.additions.questing.mixins.EmiRecipeScreenMixin";

    @Override
    public void onLoad(String mixinPackage) {
        MixinExtrasBootstrap.init();
    }

    @Override
    public String getRefMapperConfig() {
        return null;
    }

    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        if (EMI_RECIPE_SCREEN_MIXIN.equals(mixinClassName)) {
            return isModLoaded("emi");
        }

        return true;
    }

    @SuppressWarnings("removal")
    private static boolean isModLoaded(String modId) {
        //? if fabric {
        /*return FabricLoader.getInstance().isModLoaded(modId);
        *///?} else {
        var loadingModList = LoadingModList.get();
        return loadingModList != null && loadingModList.getModFileById(modId) != null;
        //?}
    }

    @Override
    public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {
    }

    @Override
    public List<String> getMixins() {
        return null;
    }

    @Override
    public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
    }

    @Override
    public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
    }
}
