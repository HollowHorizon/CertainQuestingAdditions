package ru.hollowhorizon.additions.questing.client;

//? if >= 1.21.11 {
import dev.architectury.networking.NetworkManager;
import dev.ftb.mods.ftbquests.net.CreateObjectMessage;
import dev.ftb.mods.ftblibrary.client.config.editable.EditableString;
import dev.ftb.mods.ftblibrary.client.config.gui.EditStringConfigOverlay;
//?} else {
/*import dev.ftb.mods.ftblibrary.config.StringConfig;
import dev.ftb.mods.ftblibrary.config.ui.EditStringConfigOverlay;
*///?}
import dev.ftb.mods.ftblibrary.icon.Icon;
import dev.ftb.mods.ftblibrary.icon.Icons;
//? if >= 1.21.11 {
import dev.ftb.mods.ftblibrary.client.gui.widget.ContextMenuItem;
//?} else {
/*import dev.ftb.mods.ftblibrary.ui.ContextMenuItem;
*///?}
//? if >= 1.21.11 {
import dev.ftb.mods.ftblibrary.client.gui.widget.Panel;
//?} else {
/*import dev.ftb.mods.ftblibrary.ui.Panel;
*///?}
//? if >= 1.21.11 {
import dev.ftb.mods.ftblibrary.client.util.ImageComponent;
//?} else {
/*import dev.ftb.mods.ftblibrary.util.client.ImageComponent;
*///?}
import dev.ftb.mods.ftbquests.client.gui.quests.QuestScreen;
import dev.ftb.mods.ftbquests.net.EditObjectMessage;
import dev.ftb.mods.ftbquests.quest.Chapter;
import dev.ftb.mods.ftbquests.quest.ChapterImage;
import dev.ftb.mods.ftbquests.quest.QuestObjectBase;
import net.minecraft.text.Text;
import ru.hollowhorizon.additions.questing.mixins.QuestScreenAccessor;

import java.util.List;
import java.util.function.Consumer;

public final class EntityAttachmentActions {
    private static final String DEFAULT_ENTITY_ID = "minecraft:zombie";
    private static final int DEFAULT_DESCRIPTION_SIZE = 96;
    private static final int MIN_INPUT_WIDTH = 180;

    private EntityAttachmentActions() {
    }

    public static void addCanvasContextMenuEntry(QuestScreen screen, List<ContextMenuItem> items, double questX, double questY) {
        items.add(new ContextMenuItem(
                Text.translatable("certain_questing_additions.entity_attachment.canvas"),
                menuIcon(),
                button -> openEntityInput(screen, icon -> createCanvasEntity(screen, icon, questX, questY))
        ));
    }

    public static void addDescriptionContextMenuEntry(Panel panel, List<ContextMenuItem> items, Consumer<EntityIcon> onAccepted) {
        items.add(new ContextMenuItem(
                Text.translatable("certain_questing_additions.entity_attachment.description"),
                menuIcon(),
                button -> openEntityInput(panel, onAccepted)
        ));
    }

    public static ImageComponent createDescriptionComponent(EntityIcon icon) {
        ImageComponent component = new ImageComponent();
        //? if >= 1.21.1 {
        component.setImage(icon);
        component.setWidth(DEFAULT_DESCRIPTION_SIZE);
        component.setHeight(DEFAULT_DESCRIPTION_SIZE);
        component.setAlign(ImageComponent.ImageAlign.CENTER);
        component.setFit(false);
        //?} else {
        /*component.image = icon;
        component.width = DEFAULT_DESCRIPTION_SIZE;
        component.height = DEFAULT_DESCRIPTION_SIZE;
        component.align = ImageComponent.ImageAlign.CENTER;
        component.fit = false;
        *///?}
        return component;
    }

    public static void sendEditObject(QuestObjectBase object) {
        //? if >= 1.21.1 {
        EditObjectMessage.sendToServer(object);
        //?} else {
        /*new EditObjectMessage(object).sendToServer();
        *///?}
    }

    private static void createCanvasEntity(QuestScreen screen, EntityIcon icon, double questX, double questY) {
        Chapter chapter = getSelectedChapter(screen);
        if (chapter == null) {
            return;
        }

        ChapterImage image = createChapterImage(chapter)
                .setImage(icon)
                .setPosition(questX, questY);
        image.fixupAspectRatio(true);
        //? if >= 1.21.11 {
        NetworkManager.sendToServer(CreateObjectMessage.create(image, null));
        //?} else {
        /*chapter.addImage(image);
        *///?}
        sendEditObject(chapter);
        screen.refreshQuestPanel();
    }

    private static ChapterImage createChapterImage(Chapter chapter) {
        //? if >= 1.21.11 {
        return new ChapterImage(chapter.getQuestFile().newID(), chapter);
        //?} else {
        /*return new ChapterImage(chapter);
        *///?}
    }

    private static void openEntityInput(Panel panel, Consumer<EntityIcon> onAccepted) {
        //? if >= 1.21.11 {
        EditableString config = new EditableString();
        //?} else {
        /*StringConfig config = new StringConfig(null);
        *///?}
        config.setValue(DEFAULT_ENTITY_ID);

        EditStringConfigOverlay<String> overlay = new EditStringConfigOverlay<>(
                panel.getGui(),
                config,
                accepted -> {
                    String value = config.getValue();
                    EntityIcon.fromRegisteredEntityId(value).ifPresentOrElse(
                            onAccepted,
                            () -> QuestScreen.displayError(Text.translatable("certain_questing_additions.entity_attachment.invalid", value))
                    );
                },
                Text.translatable("certain_questing_additions.entity_attachment.entity_id")
        );

        overlay.setWidth(Math.max(MIN_INPUT_WIDTH, overlay.getWidth()));
        panel.getGui().pushModalPanel(overlay.atMousePosition());
    }

    private static Icon menuIcon() {
        return EntityIcon.fromRegisteredEntityId(DEFAULT_ENTITY_ID)
                .<Icon>map(icon -> icon)
                .orElse(Icons.ART);
    }

    private static Chapter getSelectedChapter(QuestScreen screen) {
        //? if >= 1.21.1 {
        return screen.getSelectedChapter().orElse(null);
        //?} else {
        /*return ((QuestScreenAccessor) screen).getSelectedChapter();
        *///?}
    }
}
