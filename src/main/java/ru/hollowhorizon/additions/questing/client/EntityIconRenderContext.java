package ru.hollowhorizon.additions.questing.client;

import java.util.function.Supplier;

public final class EntityIconRenderContext {
    private static final ThreadLocal<Integer> ENTITY_ICON_RENDER_DEPTH = ThreadLocal.withInitial(() -> 0);

    private EntityIconRenderContext() {
    }

    public static boolean suppressesVanillaLabels() {
        return isRenderingEntityIcon();
    }

    public static void withoutVanillaLabels(Runnable renderer) {
        renderingEntityIcon(renderer);
    }

    public static boolean isRenderingEntityIcon() {
        return ENTITY_ICON_RENDER_DEPTH.get() > 0;
    }

    public static void renderingEntityIcon(Runnable renderer) {
        enterEntityIconRender();
        try {
            renderer.run();
        } finally {
            exitEntityIconRender();
        }
    }

    public static <T> T renderingEntityIcon(Supplier<T> renderer) {
        enterEntityIconRender();
        try {
            return renderer.get();
        } finally {
            exitEntityIconRender();
        }
    }

    private static void enterEntityIconRender() {
        ENTITY_ICON_RENDER_DEPTH.set(ENTITY_ICON_RENDER_DEPTH.get() + 1);
    }

    private static void exitEntityIconRender() {
        int renderDepth = ENTITY_ICON_RENDER_DEPTH.get() - 1;
        if (renderDepth <= 0) {
            ENTITY_ICON_RENDER_DEPTH.remove();
        } else {
            ENTITY_ICON_RENDER_DEPTH.set(renderDepth);
        }
    }
}
