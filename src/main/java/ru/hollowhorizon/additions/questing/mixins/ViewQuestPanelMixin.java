package ru.hollowhorizon.additions.questing.mixins;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
//? if >= 1.21.11 {
import dev.ftb.mods.ftblibrary.client.config.EditableConfigGroup;
import dev.ftb.mods.ftblibrary.client.config.editable.EditableConfigValue;
//?} else {
/*import dev.ftb.mods.ftblibrary.config.ConfigGroup;
import dev.ftb.mods.ftblibrary.config.ConfigValue;
*///?}
import dev.ftb.mods.ftblibrary.icon.Icon;
//? if >= 1.21.11 {
import dev.ftb.mods.ftblibrary.client.gui.widget.BaseScreen;
//?} else {
/*import dev.ftb.mods.ftblibrary.ui.BaseScreen;
*///?}
//? if >= 1.21.11 {
import dev.ftb.mods.ftblibrary.client.gui.widget.ContextMenu;
//?} else {
/*import dev.ftb.mods.ftblibrary.ui.ContextMenu;
*///?}
//? if >= 1.21.11 {
import dev.ftb.mods.ftblibrary.client.gui.widget.ContextMenuItem;
//?} else {
/*import dev.ftb.mods.ftblibrary.ui.ContextMenuItem;
*///?}
//? if >= 1.21.11 {
import dev.ftb.mods.ftblibrary.client.gui.widget.ModalPanel;
//?} else {
/*import dev.ftb.mods.ftblibrary.ui.ModalPanel;
*///?}
//? if >= 1.21.11 {
import dev.ftb.mods.ftblibrary.client.gui.widget.Panel;
//?} else {
/*import dev.ftb.mods.ftblibrary.ui.Panel;
*///?}
//? if >= 1.21.11 {
import dev.ftb.mods.ftblibrary.client.gui.theme.Theme;
//?} else {
/*import dev.ftb.mods.ftblibrary.ui.Theme;
*///?}
//? if >= 1.21.11 {
import dev.ftb.mods.ftblibrary.client.util.ImageComponent;
//?} else {
/*import dev.ftb.mods.ftblibrary.util.client.ImageComponent;
*///?}
import dev.ftb.mods.ftbquests.client.gui.quests.ViewQuestPanel;
import dev.ftb.mods.ftbquests.quest.Quest;
import net.minecraft.client.gui.DrawContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.hollowhorizon.additions.questing.client.ApngTextureManager;
import ru.hollowhorizon.additions.questing.client.Animator;
import ru.hollowhorizon.additions.questing.client.EntityAttachmentActions;
import ru.hollowhorizon.additions.questing.client.EntityIcon;
import ru.hollowhorizon.additions.questing.client.EntityIconConfigEntries;
import ru.hollowhorizon.additions.questing.client.QuestPanelAnimator;
import ru.hollowhorizon.additions.questing.config.QuestAnimationsConfig;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

@Mixin(value = ViewQuestPanel.class, remap = false)
public abstract class ViewQuestPanelMixin extends ModalPanel implements QuestPanelAnimator {

    @Unique
    private final Animator mou$animator = new Animator(0f, 0.3f, (i) -> 1 - (1 - i) * (1 - i) * (1 - i));

    @Shadow private Quest quest;

    @Shadow
    private int getCurrentPage() {
        throw new AssertionError();
    }

    @Shadow
    private void appendToPage(List<String> list, List<String> toAdd, int pageNumber) {
        throw new AssertionError();
    }

    public ViewQuestPanelMixin(Panel panel) {
        super(panel);
    }

    @Override
    public void cqa$triggerAnimation() {
        mou$animator.set(1f, 0f);
    }

    @WrapMethod(method = "draw")
    private void onDraw(DrawContext graphics, Theme theme, int x, int y, int w, int h, Operation<Void> original) {
        ViewQuestPanel panel = (ViewQuestPanel) (Object) this;
        ApngTextureManager.syncDetailsScope(panel);
        ApngTextureManager.pushScope(ApngTextureManager.ApngScope.QUEST_DETAILS_PANEL);
        int originalY = getY();
        try {
            if(!QuestAnimationsConfig.PANEL_ANIMATION.get()) {
                original.call(graphics, theme, x, y, w, h);
                return;
            }

            mou$animator.update();
            setY(y + Math.round(20f - 20f * mou$animator.get()));
            original.call(graphics, theme, x, y + Math.round(20f - 20f * mou$animator.get()), w, h);
        } finally {
            setY(originalY);
            ApngTextureManager.popScope(ApngTextureManager.ApngScope.QUEST_DETAILS_PANEL);
        }
    }

    //? if >= 1.21.11 {
    @WrapOperation(method = "openEditButtonContextMenu", at = @At(value = "INVOKE", target = "Ldev/ftb/mods/ftblibrary/client/gui/widget/BaseScreen;openContextMenu(Ljava/util/List;)Ldev/ftb/mods/ftblibrary/client/gui/widget/ContextMenu;"))
    //?} else {
    /*@WrapOperation(method = "openEditButtonContextMenu", at = @At(value = "INVOKE", target = "Ldev/ftb/mods/ftblibrary/ui/BaseScreen;openContextMenu(Ljava/util/List;)Ldev/ftb/mods/ftblibrary/ui/ContextMenu;"))
    *///?}
    private ContextMenu cqa$addEntityDescriptionContextItem(BaseScreen screen, List<ContextMenuItem> menuItems, Operation<ContextMenu> original) {
        EntityAttachmentActions.addDescriptionContextMenuEntry((ViewQuestPanel) (Object) this, menuItems, this::cqa$appendEntityDescription);
        return original.call(screen, menuItems);
    }

    //? if >= 1.21.11 {
    @WrapOperation(method = "editImage", at = @At(value = "INVOKE", target = "Ldev/ftb/mods/ftblibrary/client/config/EditableConfigGroup;add(Ljava/lang/String;Ldev/ftb/mods/ftblibrary/client/config/editable/EditableConfigValue;Ljava/lang/Object;Ljava/util/function/Consumer;Ljava/lang/Object;)Ldev/ftb/mods/ftblibrary/client/config/editable/EditableConfigValue;", ordinal = 0))
    //?} else {
    /*@WrapOperation(method = "editImage", at = @At(value = "INVOKE", target = "Ldev/ftb/mods/ftblibrary/config/ConfigGroup;add(Ljava/lang/String;Ldev/ftb/mods/ftblibrary/config/ConfigValue;Ljava/lang/Object;Ljava/util/function/Consumer;Ljava/lang/Object;)Ldev/ftb/mods/ftblibrary/config/ConfigValue;", ordinal = 0))
    *///?}
    @SuppressWarnings({"rawtypes"})
    //? if >= 1.21.11 {
    private EditableConfigValue<?> cqa$useEntityDescriptionImageConfig(
            EditableConfigGroup group,
            String id,
            EditableConfigValue<?> type,
            Object value,
            Consumer setter,
            Object defaultValue,
            Operation<EditableConfigValue<?>> original,
            int line,
            ImageComponent component
    ) {
    //?} else {
    /*private ConfigValue cqa$useEntityDescriptionImageConfig(
            ConfigGroup group,
            String id,
            ConfigValue type,
            Object value,
            Consumer setter,
            Object defaultValue,
            Operation<ConfigValue> original,
            int line,
            ImageComponent component
    ) {
    *///?}
        Icon image = cqa$getImage(component);
        if (image instanceof EntityIcon entityIcon) {
            return EntityIconConfigEntries.add(group, id, entityIcon, icon -> cqa$setImage(component, icon));
        }

        return original.call(group, id, type, value, setter, defaultValue);
    }

    @Unique
    private void cqa$appendEntityDescription(EntityIcon icon) {
        if (quest == null) {
            return;
        }

        List<String> componentLine = List.of(EntityAttachmentActions.createDescriptionComponent(icon).toString());
        //? if >= 1.21.1 {
        List<String> description = new ArrayList<>(quest.getRawDescription());
        appendToPage(description, componentLine, getCurrentPage());
        quest.setRawDescription(List.copyOf(description));
        //?} else {
        /*appendToPage(quest.getRawDescription(), componentLine, getCurrentPage());
        quest.clearCachedData();
        *///?}
        EntityAttachmentActions.sendEditObject(quest);
        refreshWidgets();
    }

    @Unique
    private Icon cqa$getImage(ImageComponent component) {
        //? if >= 1.21.1 {
        return component.getImage();
        //?} else {
        /*return component.image;
        *///?}
    }

    @Unique
    private void cqa$setImage(ImageComponent component, EntityIcon icon) {
        //? if >= 1.21.1 {
        component.setImage(icon);
        //?} else {
        /*component.image = icon;
        *///?}
    }

    @Inject(method = "onClosed", at = @At("TAIL"))
    private void cqa$closeDetailsApngScope(CallbackInfo ci) {
        ApngTextureManager.closeDetailsScope();
    }

    @Inject(method = "setViewedQuest", at = @At("TAIL"))
    private void cqa$syncApngScopeOnQuestChange(Quest newQuest, CallbackInfo ci) {
        ViewQuestPanel panel = (ViewQuestPanel) (Object) this;
        if (newQuest == null) {
            ApngTextureManager.closeDetailsScope();
        } else {
            ApngTextureManager.syncDetailsScope(panel);
        }
    }
}
