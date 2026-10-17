package ru.hollowhorizon.additions.questing.mixins;

import com.llamalad7.mixinextras.MixinExtrasBootstrap;
//? if forge {
/*import net.minecraftforge.fml.loading.LoadingModList;
*///?} elif fabric {
/*import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.Version;
import net.fabricmc.loader.api.VersionParsingException;
*///?} elif neoforge {
import net.neoforged.fml.loading.LoadingModList;
//?}
import org.objectweb.asm.tree.ClassNode;
//? if !fabric {
import org.apache.maven.artifact.versioning.ArtifactVersion;
import org.apache.maven.artifact.versioning.DefaultArtifactVersion;
//?}
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import java.util.List;
import java.util.Set;

public final class CqaMixinConfigPlugin implements IMixinConfigPlugin {
    private static final String EMI_RECIPE_SCREEN_MIXIN = "ru.hollowhorizon.additions.questing.mixins.EmiRecipeScreenMixin";
    private static final String CHAPTER_IMAGE_CONFIG_GROUP_MIXIN = "ru.hollowhorizon.additions.questing.mixins.ChapterImageConfigGroupMixin";
    private static final String QUEST_OBJECT_CONFIG_GROUP_MIXIN = "ru.hollowhorizon.additions.questing.mixins.QuestObjectConfigGroupMixin";
    // FTB Quests 2101.1.28 made chapter images quest objects and moved their properties editor.
    private static final String QUEST_OBJECT_IMAGE_EDITOR_VERSION = "2101.1.28";

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
        if (CHAPTER_IMAGE_CONFIG_GROUP_MIXIN.equals(mixinClassName)) {
            return !usesQuestObjectImageEditor();
        }
        if (QUEST_OBJECT_CONFIG_GROUP_MIXIN.equals(mixinClassName)) {
            return usesQuestObjectImageEditor();
        }

        return true;
    }

    @SuppressWarnings("removal")
    private static boolean usesQuestObjectImageEditor() {
        //? if >= 1.21.1 && < 1.21.11 {
        /*//? if fabric {
        var ftbQuests = FabricLoader.getInstance().getModContainer("ftbquests")
                .orElseThrow(() -> new IllegalStateException("FTB Quests metadata is unavailable during mixin selection"));
        return isQuestObjectImageEditorVersion(ftbQuests.getMetadata().getVersion());
        //?} else {
        /^var loadingModList = LoadingModList.get();
        if (loadingModList == null) {
            throw new IllegalStateException("Mod metadata is unavailable during mixin selection");
        }
        var ftbQuests = loadingModList.getMods().stream()
                .filter(mod -> mod.getModId().equals("ftbquests"))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("FTB Quests metadata is unavailable during mixin selection"));
        return isQuestObjectImageEditorVersion(ftbQuests.getVersion());
        ^///?}
        *///?} else {
        return false;
        //?}
    }

    //? if fabric {
    /*static boolean isQuestObjectImageEditorVersion(Version version) {
        try {
            return version.compareTo(Version.parse(QUEST_OBJECT_IMAGE_EDITOR_VERSION)) >= 0;
        } catch (VersionParsingException e) {
            throw new IllegalStateException("Invalid FTB Quests image editor version boundary", e);
        }
    }
    *///?} else {
    static boolean isQuestObjectImageEditorVersion(ArtifactVersion version) {
        return version.compareTo(new DefaultArtifactVersion(QUEST_OBJECT_IMAGE_EDITOR_VERSION)) >= 0;
    }
    //?}

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
