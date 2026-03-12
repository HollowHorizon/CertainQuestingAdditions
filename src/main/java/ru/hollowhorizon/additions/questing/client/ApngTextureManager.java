package ru.hollowhorizon.additions.questing.client;

import com.mojang.blaze3d.systems.RenderSystem;
import dev.ftb.mods.ftblibrary.icon.Icon;
import dev.ftb.mods.ftblibrary.icon.ImageIcon;
import dev.ftb.mods.ftblibrary.ui.Button;
import dev.ftb.mods.ftblibrary.ui.Panel;
import dev.ftb.mods.ftblibrary.ui.Widget;
import dev.ftb.mods.ftblibrary.util.client.ImageComponent;
import dev.ftb.mods.ftbquests.client.gui.ImageComponentWidget;
import dev.ftb.mods.ftbquests.client.gui.quests.QuestPanel;
import dev.ftb.mods.ftbquests.client.gui.quests.QuestScreen;
import dev.ftb.mods.ftbquests.client.gui.quests.ViewQuestPanel;
import dev.ftb.mods.ftbquests.quest.Chapter;
import dev.ftb.mods.ftbquests.quest.theme.property.ThemeProperties;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.resource.Resource;
import net.minecraft.util.Identifier;
import ru.hollowhorizon.additions.questing.CertainQuestingAdditions;
import ru.hollowhorizon.additions.questing.mixins.ButtonAccessor;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicLong;

public final class ApngTextureManager {
    private static final int MAX_ACTIVE_STREAMS = 3;
    private static final int DECODE_THREADS = 2;
    private static final double CANVAS_PADDING_FACTOR = 0.5D;
    private static final long CANVAS_BIND_PRIORITY_GRACE_NANOS = 250_000_000L;

    private static final Map<Identifier, ApngStreamEntry> ENTRIES = new ConcurrentHashMap<>();
    private static final Set<Identifier> CANVAS_SCOPE_TEXTURES = ConcurrentHashMap.newKeySet();
    private static final Set<Identifier> DETAILS_SCOPE_TEXTURES = ConcurrentHashMap.newKeySet();
    private static final Set<Identifier> UNSUPPORTED = ConcurrentHashMap.newKeySet();
    private static final ThreadLocal<Deque<ApngScope>> ACTIVE_SCOPE_STACK = ThreadLocal.withInitial(ArrayDeque::new);
    private static final AtomicLong SESSION_COUNTER = new AtomicLong();
    private static final ExecutorService DECODE_EXECUTOR = Executors.newFixedThreadPool(DECODE_THREADS, new ApngThreadFactory());

    private static volatile QuestScreen activeScreen;

    private ApngTextureManager() {
    }

    public static void prefetchCanvas(QuestScreen screen) {
        ensureScreen(screen);

        Set<Identifier> textures = new HashSet<>();
        collectPanelTextures(screen.questPanel, textures);
        collectChapterThemeTextures(screen.getSelectedChapter().orElse(null), textures);
        replaceScope(ApngScope.QUEST_SCREEN_CANVAS, textures);
    }

    public static void updateCanvasPlayback(QuestScreen screen) {
        ensureScreen(screen);
        if (CANVAS_SCOPE_TEXTURES.isEmpty()) {
            prefetchCanvas(screen);
        }

        QuestPanel panel = screen.questPanel;
        int panelX = panel.getX();
        int panelY = panel.getY();
        int panelWidth = panel.getWidth();
        int panelHeight = panel.getHeight();

        int paddingX = (int) Math.round(panelWidth * CANVAS_PADDING_FACTOR);
        int paddingY = (int) Math.round(panelHeight * CANVAS_PADDING_FACTOR);

        int paddedX = panelX - paddingX;
        int paddedY = panelY - paddingY;
        int paddedWidth = panelWidth + paddingX * 2;
        int paddedHeight = panelHeight + paddingY * 2;

        Map<Identifier, PlaybackPriority> priorities = new HashMap<>();
        collectPanelPriorities(panel, panelX, panelY, panelWidth, panelHeight, paddedX, paddedY, paddedWidth, paddedHeight, priorities);
        applyCanvasPriorities(priorities);
    }

    public static void syncDetailsScope(ViewQuestPanel panel) {
        QuestScreen screen = (QuestScreen) panel.getGui();
        ensureScreen(screen);

        if (panel.getViewedQuest() == null) {
            closeDetailsScope();
            return;
        }

        Set<Identifier> textures = new HashSet<>();
        collectIcon(panel.getViewedQuest().getIcon(), textures);
        collectPanelTextures(panel, textures);
        replaceScope(ApngScope.QUEST_DETAILS_PANEL, textures);
    }

    public static void closeDetailsScope() {
        replaceScope(ApngScope.QUEST_DETAILS_PANEL, Set.of());
    }

    public static void pushScope(ApngScope scope) {
        ACTIVE_SCOPE_STACK.get().addLast(scope);
    }

    public static void popScope(ApngScope scope) {
        Deque<ApngScope> stack = ACTIVE_SCOPE_STACK.get();
        if (!stack.isEmpty() && stack.peekLast() == scope) {
            stack.removeLast();
        } else {
            stack.remove(scope);
        }
    }

    public static boolean bindIfAnimated(Identifier textureId) {
        if (!isAPngPath(textureId) || UNSUPPORTED.contains(textureId)) {
            return false;
        }

        ApngScope scope = currentScope();
        if (scope == null) {
            return false;
        }

        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null || activeScreen == null) {
            return false;
        }

        ApngStreamEntry entry = ENTRIES.computeIfAbsent(textureId, ApngStreamEntry::new);
        registerTextureForScope(entry, textureId, scope);

        synchronized (entry) {
            long now = System.nanoTime();
            entry.lastUsedNanos = now;
            if (scope == ApngScope.QUEST_DETAILS_PANEL) {
                entry.detailsActive = true;
            } else {
                entry.canvasPriority = PlaybackPriority.VISIBLE;
                entry.lastCanvasBindNanos = now;
            }
        }

        rebalanceSessions();
        return bindEntry(client, entry);
    }

    public static void clearCache() {
        clearCacheInternal();
    }

    private static boolean bindEntry(MinecraftClient client, ApngStreamEntry entry) {
        PlaybackSession session;
        synchronized (entry) {
            session = entry.session;
        }

        if (session == null) {
            return false;
        }

        try {
            synchronized (entry) {
                if (entry.session != session || session.closed) {
                    return false;
                }

                promoteReadyFrames(client, entry, session);
                scheduleDecodeIfNeeded(entry, session);

                if (session.texture == null) {
                    entry.state = entry.discovered != null ? EntryState.READY_STATIC : EntryState.DISCOVERED;
                    return false;
                }

                RenderSystem.setShaderTexture(0, session.textureId);
                entry.state = EntryState.STREAMING;
                return true;
            }
        } catch (Throwable t) {
            CertainQuestingAdditions.LOGGER.warn("Failed to bind streamed APNG {}", entry.textureId, t);
            failEntry(entry, t);
            return false;
        }
    }

    private static void promoteReadyFrames(MinecraftClient client, ApngStreamEntry entry, PlaybackSession session) {
        DecodedFrame readyFrame = session.pendingFrame;
        if (readyFrame == null) {
            return;
        }

        if (session.texture == null) {
            session.pendingFrame = null;
            session.texture = new NativeImageBackedTexture(readyFrame.image());
            client.getTextureManager().registerTexture(session.textureId, session.texture);
            session.texture.upload();
            session.nextFrameAtMs = System.currentTimeMillis() + readyFrame.delayMillis();
            entry.state = EntryState.STREAMING;
            return;
        }

        long now = System.currentTimeMillis();
        if (now < session.nextFrameAtMs) {
            return;
        }

        session.pendingFrame = null;
        NativeImage currentImage = session.texture.getImage();
        session.texture.setImage(readyFrame.image());
        if (currentImage != null) {
            currentImage.close();
        }
        session.texture.upload();
        session.nextFrameAtMs = now + readyFrame.delayMillis();
    }

    private static void scheduleDecodeIfNeeded(ApngStreamEntry entry, PlaybackSession session) {
        if (session.closed || session.decoding || session.pendingFrame != null) {
            return;
        }

        if (entry.discovered == null) {
            return;
        }

        if (session.frameStream == null) {
            session.frameStream = ApngParser.openFrameStream(entry.discovered, () -> openStream(entry.textureId));
        }

        session.decoding = true;
        DECODE_EXECUTOR.execute(() -> decodeNextFrame(entry, session));
    }

    private static void decodeNextFrame(ApngStreamEntry entry, PlaybackSession session) {
        DecodedFrame decodedFrame = null;
        Throwable failure = null;

        try {
            ApngParser.RenderedFrame renderedFrame = session.frameStream.nextFrame();
            decodedFrame = new DecodedFrame(toNativeImage(renderedFrame.image()), Math.max(1, renderedFrame.delayMillis()));
        } catch (Throwable t) {
            failure = t;
        }

        synchronized (entry) {
            session.decoding = false;

            if (entry.session != session || session.closed) {
                closeFrame(decodedFrame);
                return;
            }

            if (failure != null) {
                closeFrame(decodedFrame);
                failEntry(entry, failure);
                return;
            }

            if (decodedFrame == null) {
                failEntry(entry, new IOException("APNG decoder returned no frame"));
                return;
            }

            if (session.pendingFrame != null) {
                closeFrame(session.pendingFrame);
            }
            session.pendingFrame = decodedFrame;
        }
    }

    private static synchronized void replaceScope(ApngScope scope, Set<Identifier> newTextures) {
        Set<Identifier> currentTextures = scope == ApngScope.QUEST_SCREEN_CANVAS ? CANVAS_SCOPE_TEXTURES : DETAILS_SCOPE_TEXTURES;
        Set<Identifier> removed = new HashSet<>(currentTextures);
        removed.removeAll(newTextures);

        currentTextures.clear();
        currentTextures.addAll(newTextures);

        for (Identifier textureId : newTextures) {
            ApngStreamEntry entry = ENTRIES.computeIfAbsent(textureId, ApngStreamEntry::new);
            registerTextureForScope(entry, textureId, scope);
        }

        for (Identifier textureId : removed) {
            ApngStreamEntry entry = ENTRIES.get(textureId);
            if (entry != null) {
                unregisterTextureForScope(entry, scope);
            }
        }

        rebalanceSessions();
    }

    private static void applyCanvasPriorities(Map<Identifier, PlaybackPriority> priorities) {
        long now = System.nanoTime();
        for (Identifier textureId : CANVAS_SCOPE_TEXTURES) {
            ApngStreamEntry entry = ENTRIES.get(textureId);
            if (entry == null) {
                continue;
            }

            synchronized (entry) {
                PlaybackPriority computed = priorities.getOrDefault(textureId, PlaybackPriority.NONE);
                if (computed == PlaybackPriority.NONE && now - entry.lastCanvasBindNanos <= CANVAS_BIND_PRIORITY_GRACE_NANOS) {
                    computed = PlaybackPriority.VISIBLE;
                }
                entry.canvasPriority = computed;
                if (entry.canvasPriority != PlaybackPriority.NONE) {
                    entry.lastUsedNanos = now;
                }
            }
        }

        rebalanceSessions();
    }

    private static synchronized void rebalanceSessions() {
        List<ApngStreamEntry> desired = new ArrayList<>();
        for (ApngStreamEntry entry : ENTRIES.values()) {
            synchronized (entry) {
                if (entry.wantsStreaming()) {
                    desired.add(entry);
                }
            }
        }

        desired.sort(Comparator
                .comparingInt((ApngStreamEntry entry) -> entry.priority().rank()).reversed()
                .thenComparing(Comparator.comparingLong((ApngStreamEntry entry) -> entry.lastUsedNanos).reversed())
        );

        Set<ApngStreamEntry> active = new HashSet<>();
        for (int i = 0; i < desired.size() && i < MAX_ACTIVE_STREAMS; i++) {
            active.add(desired.get(i));
        }

        for (ApngStreamEntry entry : ENTRIES.values()) {
            if (active.contains(entry)) {
                startSession(entry);
            } else {
                stopSession(entry);
            }
        }
    }

    private static void startSession(ApngStreamEntry entry) {
        synchronized (entry) {
            if (entry.session != null || UNSUPPORTED.contains(entry.textureId)) {
                return;
            }

            entry.session = new PlaybackSession(dynamicTextureId(entry.textureId));
            entry.state = EntryState.READY_STATIC;
            scheduleDecodeIfNeeded(entry, entry.session);
        }
    }

    private static void stopSession(ApngStreamEntry entry) {
        PlaybackSession session;
        synchronized (entry) {
            session = entry.session;
            if (session == null) {
                return;
            }

            entry.session = null;
            entry.state = entry.discovered != null ? EntryState.DISCOVERED : EntryState.READY_STATIC;
            session.closed = true;
            closeFrame(session.pendingFrame);
            session.pendingFrame = null;
            closeStream(session.frameStream);
            session.frameStream = null;
        }

        MinecraftClient client = MinecraftClient.getInstance();
        if (client != null && session.texture != null) {
            client.getTextureManager().destroyTexture(session.textureId);
        } else if (session.texture != null) {
            session.texture.close();
        }
    }

    private static void failEntry(ApngStreamEntry entry, Throwable failure) {
        CertainQuestingAdditions.LOGGER.warn("Failed to stream APNG texture {}", entry.textureId, failure);
        UNSUPPORTED.add(entry.textureId);
        stopSession(entry);
        synchronized (entry) {
            entry.canvasPriority = PlaybackPriority.NONE;
            entry.detailsActive = false;
        }
    }

    private static synchronized void clearCacheInternal() {
        for (ApngStreamEntry entry : List.copyOf(ENTRIES.values())) {
            stopSession(entry);
        }

        ENTRIES.clear();
        CANVAS_SCOPE_TEXTURES.clear();
        DETAILS_SCOPE_TEXTURES.clear();
        UNSUPPORTED.clear();
        activeScreen = null;
    }

    private static void ensureScreen(QuestScreen screen) {
        if (activeScreen != screen) {
            clearCacheInternal();
            activeScreen = screen;
        }
    }

    private static void registerTextureForScope(ApngStreamEntry entry, Identifier textureId, ApngScope scope) {
        synchronized (entry) {
            if (scope == ApngScope.QUEST_DETAILS_PANEL) {
                entry.registeredDetails = true;
                entry.detailsActive = true;
                DETAILS_SCOPE_TEXTURES.add(textureId);
            } else {
                entry.registeredCanvas = true;
                CANVAS_SCOPE_TEXTURES.add(textureId);
            }

            if (entry.discovered == null && !entry.discoveryScheduled && !UNSUPPORTED.contains(textureId)) {
                entry.discoveryScheduled = true;
                DECODE_EXECUTOR.execute(() -> discoverEntry(entry));
            }
        }
    }

    private static void unregisterTextureForScope(ApngStreamEntry entry, ApngScope scope) {
        boolean removeEntry;
        synchronized (entry) {
            if (scope == ApngScope.QUEST_DETAILS_PANEL) {
                entry.registeredDetails = false;
                entry.detailsActive = false;
            } else {
                entry.registeredCanvas = false;
                entry.canvasPriority = PlaybackPriority.NONE;
            }

            removeEntry = !entry.registeredCanvas && !entry.registeredDetails;
        }

        if (removeEntry) {
            stopSession(entry);
            ENTRIES.remove(entry.textureId, entry);
        }
    }

    private static void discoverEntry(ApngStreamEntry entry) {
        ApngParser.DiscoveredApng discovered = null;
        Throwable failure = null;

        try (InputStream stream = openStream(entry.textureId)) {
            if (stream == null) {
                throw new IOException("Resource not found");
            }

            discovered = ApngParser.discover(stream);
            if (discovered == null) {
                throw new IOException("Resource is not an APNG");
            }
        } catch (Throwable t) {
            failure = t;
        }

        synchronized (entry) {
            entry.discoveryScheduled = false;

            if (failure != null) {
                CertainQuestingAdditions.LOGGER.warn("Failed to inspect APNG texture {}", entry.textureId, failure);
                UNSUPPORTED.add(entry.textureId);
                stopSession(entry);
                return;
            }

            entry.discovered = discovered;
            if (entry.state != EntryState.STREAMING) {
                entry.state = EntryState.DISCOVERED;
            }

            if (entry.session != null && !entry.session.closed) {
                scheduleDecodeIfNeeded(entry, entry.session);
            }
        }
    }

    private static void collectPanelTextures(Panel panel, Set<Identifier> textures) {
        for (Widget widget : panel.getWidgets()) {
            collectWidgetTextures(widget, textures);
        }
    }

    private static void collectWidgetTextures(Widget widget, Set<Identifier> textures) {
        if (widget instanceof Button button) {
            collectIcon(((ButtonAccessor) button).cqa$getIcon(), textures);
        } else if (widget instanceof ImageComponentWidget imageWidget) {
            ImageComponent component = imageWidget.getComponent();
            collectIcon(component.getImage(), textures);
        }

        if (widget instanceof Panel nestedPanel) {
            for (Widget child : nestedPanel.getWidgets()) {
                collectWidgetTextures(child, textures);
            }
        }
    }

    private static void collectChapterThemeTextures(Chapter chapter, Set<Identifier> textures) {
        if (chapter == null) {
            return;
        }

        collectIcon((Icon) ThemeProperties.DEPENDENCY_LINE_TEXTURE.get(chapter), textures);
    }

    private static void collectPanelPriorities(Panel panel, int visibleX, int visibleY, int visibleWidth, int visibleHeight, int paddedX, int paddedY, int paddedWidth, int paddedHeight, Map<Identifier, PlaybackPriority> priorities) {
        for (Widget widget : panel.getWidgets()) {
            collectWidgetPriorities(widget, visibleX, visibleY, visibleWidth, visibleHeight, paddedX, paddedY, paddedWidth, paddedHeight, priorities);
        }
    }

    private static void collectWidgetPriorities(Widget widget, int visibleX, int visibleY, int visibleWidth, int visibleHeight, int paddedX, int paddedY, int paddedWidth, int paddedHeight, Map<Identifier, PlaybackPriority> priorities) {
        PlaybackPriority priority = PlaybackPriority.NONE;
        if (widget.collidesWith(visibleX, visibleY, visibleWidth, visibleHeight)) {
            priority = PlaybackPriority.VISIBLE;
        } else if (widget.collidesWith(paddedX, paddedY, paddedWidth, paddedHeight)) {
            priority = PlaybackPriority.NEARBY;
        }

        if (priority != PlaybackPriority.NONE) {
            if (widget instanceof Button button) {
                mergePriority(priorities, ((ButtonAccessor) button).cqa$getIcon(), priority);
            } else if (widget instanceof ImageComponentWidget imageWidget) {
                mergePriority(priorities, imageWidget.getComponent().getImage(), priority);
            }
        }

        if (widget instanceof Panel nestedPanel) {
            for (Widget child : nestedPanel.getWidgets()) {
                collectWidgetPriorities(child, visibleX, visibleY, visibleWidth, visibleHeight, paddedX, paddedY, paddedWidth, paddedHeight, priorities);
            }
        }
    }

    private static void mergePriority(Map<Identifier, PlaybackPriority> priorities, Icon icon, PlaybackPriority priority) {
        if (!(icon instanceof ImageIcon imageIcon)) {
            return;
        }

        Identifier textureId = imageIcon.getResourceLocation();
        if (!isAPngPath(textureId)) {
            return;
        }

        priorities.merge(textureId, priority, (left, right) -> left.rank() >= right.rank() ? left : right);
    }

    private static void collectIcon(Icon icon, Set<Identifier> textures) {
        if (icon instanceof ImageIcon imageIcon) {
            Identifier textureId = imageIcon.getResourceLocation();
            if (isAPngPath(textureId)) {
                textures.add(textureId);
            }
        }
    }

    private static InputStream openStream(Identifier textureId) throws IOException {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null) {
            return null;
        }

        Resource resource = client.getResourceManager().getResource(textureId).orElse(null);
        return resource != null ? resource.getInputStream() : null;
    }

    private static Identifier dynamicTextureId(Identifier textureId) {
        return Identifier.of(
                CertainQuestingAdditions.MOD_ID,
                "apng/" + sanitize(textureId.toString()) + "/" + SESSION_COUNTER.incrementAndGet()
        );
    }

    private static NativeImage toNativeImage(BufferedImage image) {
        BufferedImage argbImage = image.getType() == BufferedImage.TYPE_INT_ARGB ? image : copyToArgb(image);
        int width = argbImage.getWidth();
        int height = argbImage.getHeight();
        int[] pixels = ((DataBufferInt) argbImage.getRaster().getDataBuffer()).getData();

        NativeImage nativeImage = new NativeImage(width, height, true);
        for (int y = 0; y < height; y++) {
            int row = y * width;
            for (int x = 0; x < width; x++) {
                nativeImage.setColor(x, y, argbToAbgr(pixels[row + x]));
            }
        }

        return nativeImage;
    }

    private static BufferedImage copyToArgb(BufferedImage image) {
        BufferedImage copy = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_ARGB);
        var graphics = copy.createGraphics();
        try {
            graphics.drawImage(image, 0, 0, null);
        } finally {
            graphics.dispose();
        }
        return copy;
    }

    private static int argbToAbgr(int argb) {
        return (argb & 0xFF00FF00)
                | ((argb & 0x00FF0000) >> 16)
                | ((argb & 0x000000FF) << 16);
    }

    private static void closeFrame(DecodedFrame frame) {
        if (frame != null) {
            frame.image().close();
        }
    }

    private static void closeStream(ApngParser.ApngFrameStream stream) {
        if (stream == null) {
            return;
        }

        try {
            stream.close();
        } catch (IOException ignored) {
        }
    }

    private static ApngScope currentScope() {
        Deque<ApngScope> stack = ACTIVE_SCOPE_STACK.get();
        return stack.isEmpty() ? null : stack.peekLast();
    }

    private static boolean isAPngPath(Identifier textureId) {
        String path = textureId.getPath().toLowerCase(Locale.ROOT);
        return path.endsWith(".apng.png");
    }

    private static String sanitize(String value) {
        StringBuilder out = new StringBuilder(value.length());
        for (int i = 0; i < value.length(); i++) {
            char c = Character.toLowerCase(value.charAt(i));
            if ((c >= 'a' && c <= 'z') || (c >= '0' && c <= '9') || c == '/' || c == '_' || c == '-' || c == '.') {
                out.append(c);
            } else if (c == ':') {
                out.append('/');
            } else {
                out.append('_');
            }
        }
        return out.toString();
    }

    public enum ApngScope {
        QUEST_SCREEN_CANVAS,
        QUEST_DETAILS_PANEL
    }

    private enum EntryState {
        DISCOVERED,
        READY_STATIC,
        STREAMING
    }

    private enum PlaybackPriority {
        NONE(0),
        NEARBY(1),
        VISIBLE(2),
        DETAILS(3);

        private final int rank;

        PlaybackPriority(int rank) {
            this.rank = rank;
        }

        private int rank() {
            return rank;
        }
    }

    private static final class ApngStreamEntry {
        private final Identifier textureId;

        private ApngParser.DiscoveredApng discovered;
        private EntryState state = EntryState.READY_STATIC;
        private PlaybackSession session;
        private boolean registeredCanvas;
        private boolean registeredDetails;
        private boolean detailsActive;
        private boolean discoveryScheduled;
        private PlaybackPriority canvasPriority = PlaybackPriority.NONE;
        private long lastUsedNanos;
        private long lastCanvasBindNanos;

        private ApngStreamEntry(Identifier textureId) {
            this.textureId = textureId;
            this.lastUsedNanos = System.nanoTime();
            this.lastCanvasBindNanos = Long.MIN_VALUE;
        }

        private boolean wantsStreaming() {
            return priority() != PlaybackPriority.NONE;
        }

        private PlaybackPriority priority() {
            if (detailsActive) {
                return PlaybackPriority.DETAILS;
            }
            return canvasPriority;
        }
    }

    private static final class PlaybackSession {
        private final Identifier textureId;

        private ApngParser.ApngFrameStream frameStream;
        private DecodedFrame pendingFrame;
        private NativeImageBackedTexture texture;
        private boolean decoding;
        private boolean closed;
        private long nextFrameAtMs;

        private PlaybackSession(Identifier textureId) {
            this.textureId = textureId;
        }
    }

    private record DecodedFrame(NativeImage image, int delayMillis) {
    }

    private static final class ApngThreadFactory implements ThreadFactory {
        private final AtomicLong counter = new AtomicLong();

        @Override
        public Thread newThread(Runnable runnable) {
            Thread thread = new Thread(runnable, "CQA-APNG-" + counter.incrementAndGet());
            thread.setDaemon(true);
            return thread;
        }
    }
}
