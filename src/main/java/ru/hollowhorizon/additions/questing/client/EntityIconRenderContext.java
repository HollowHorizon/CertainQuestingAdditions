package ru.hollowhorizon.additions.questing.client;

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
        ENTITY_ICON_RENDER_DEPTH.set(ENTITY_ICON_RENDER_DEPTH.get() + 1);
        try {
            renderer.run();
        } finally {
            int renderDepth = ENTITY_ICON_RENDER_DEPTH.get() - 1;
            if (renderDepth <= 0) {
                ENTITY_ICON_RENDER_DEPTH.remove();
            } else {
                ENTITY_ICON_RENDER_DEPTH.set(renderDepth);
            }
        }
    }
}
