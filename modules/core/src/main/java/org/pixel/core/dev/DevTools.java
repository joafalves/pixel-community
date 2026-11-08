package org.pixel.core.dev;

import org.pixel.commons.Color;
import org.pixel.commons.logger.LogLevel;
import org.pixel.commons.logger.Logger;
import org.pixel.commons.logger.LoggerFactory;
import org.pixel.content.ContentManager;
import org.pixel.core.GameContext;
import org.pixel.graphics.RenderStats;
import org.pixel.graphics.backend.RenderBackend;
import org.pixel.graphics.canvas.Canvas;
import org.pixel.graphics.draw.BatchBreak;
import org.pixel.graphics.draw.Layer;
import org.pixel.graphics.pass.FrameInfo;
import org.pixel.graphics.pass.RenderPipeline;
import org.pixel.graphics.pass.View;
import org.pixel.graphics.text.SdfFont;
import org.pixel.graphics.text.TextAlign;
import org.pixel.math.Matrix4;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.DoubleSupplier;
import java.util.function.Supplier;

/**
 * The in-game dev tools: a window drawn over the game (after it, unaffected by its cameras and effects) with tabs for
 * performance (frame times, memory), rendering (draw calls, and per pipeline its passes, layers and why batches
 * broke), loaded assets, recent log messages, and the game's own values and switches:
 * <pre>
 * devTools().watch("enemies", () -> enemies.size());
 * DevTools.Toggle colliders = devTools().addToggle("show colliders", false);
 * DevTools.Slider speed = devTools().addSlider("player speed", 0, 400, 180);
 * devTools().addAction("respawn", this::respawn);
 * devTools().track(pipeline, "world");
 * ...
 * if (colliders.isOn()) { ... }       // read every frame; works (with the initial values) when unavailable too
 * </pre>
 * Toggle it with F1 (desktop) or a three-finger tap (Android). It is available in dev mode by default
 * ({@code GameSettings.setDevToolsMode}). Numbers are refreshed four times a second (averaged over that time), so they
 * can be read and cost little: the panels' text is only rebuilt then. While hidden the tools only keep frame timers
 * and copy log messages; nothing is loaded until they are first shown. Drag the window by its title; the arrow
 * collapses it to an FPS badge; long tabs scroll with the bar on the right. While the pointer is over the window,
 * {@link #isPointerOverUi()} is true, so the game can ignore that click.
 * <p>
 * Game-facing methods are for the game's thread; the {@code host} methods are called by the platform host.
 */
public final class DevTools {

    private static final Logger log = LoggerFactory.getLogger(DevTools.class);
    private static final Layer LAYER = Layer.sorted("dev-tools");
    private static final int SAMPLES = 180;
    private static final float WIDTH = 380f, HEIGHT = 440f, MIN_WIDTH = 260f, MIN_HEIGHT = 150f, BADGE = 150f,
            TITLE = 26f, MARGIN = 8f, SCROLLBAR = 6f, GRIP = 14f;
    private static final int FPS_SAMPLES = 240; // one per refresh: the last minute
    private static final long REFRESH_NANOS = 250_000_000L;
    private static final CharSequence[] TABS = {"Perf", "Render", "Assets", "Log", "Game"};
    private static final BatchBreak[] REASONS = BatchBreak.values();
    private static final float[] MARKS = {1000f / 60f, 1000f / 30f};
    private static final int LOG_LINES = 40, ASSET_LINES = 60;

    private final GameContext context;
    private final boolean available;
    private volatile boolean visible; // also read by the host's input thread (isOverUi)
    private boolean broken;
    private volatile boolean toggleRequested;
    private boolean rendered; // render() already ran this frame

    // game items and tracked pipelines
    private final List<Item> items = new ArrayList<>();
    private final List<RenderPipeline> pipelines = new ArrayList<>();
    private final List<String> pipelineNames = new ArrayList<>();

    // timing (milliseconds)
    private final float[] interval = new float[SAMPLES], update = new float[SAMPLES], draw = new float[SAMPLES];
    private int head = -1, count;
    private float pendingUpdate;

    // what is shown: refreshed every REFRESH_NANOS (averages over that time)
    private final float[] shownInterval = new float[SAMPLES], shownUpdate = new float[SAMPLES],
            shownDraw = new float[SAMPLES];
    private int shownHead, shownCount;
    private long nextRefresh;
    private boolean refresh = true;
    private int windowFrames;
    private float windowInterval, windowUpdate, windowDraw, windowMax;
    private long windowOwn;
    private int shownFps;
    private float shownFrame, shownMax, shownUpdateMs, shownDrawMs, shownOwnMs;
    private final float[] fpsHistory = new float[FPS_SAMPLES];
    private int fpsHead = -1, fpsCount;
    private final Lines lines = new Lines();
    private int linesTab = -1;

    // input (overlay units)
    private volatile float scale = 1f;
    private float pointerX = -1, pointerY = -1;
    private boolean down, wasDown;

    // drawing (created on first show)
    private RenderPipeline pipeline;
    private SdfFont font;
    private int fontGeneration;
    private final DevUi ui = new DevUi();
    private final OverlayView view = new OverlayView();
    private final DevLog logBuffer;
    private final LogLevel[] logLevels = new LogLevel[LOG_LINES];
    private final String[] logContexts = new String[LOG_LINES], logMessages = new String[LOG_LINES];
    private final JvmStats jvm = new JvmStats();

    // window (overlay units); x < 0: placed at the top-right corner on first show
    private float windowX = -1, windowY = 12, windowWidth = WIDTH, windowHeight = HEIGHT;
    // the window as last drawn, for other threads (isOverUi): x, y, width, height in whole units, 16 bits each, so one
    // volatile write publishes all four at once (-1 = not drawn yet)
    private volatile long hitBox = -1L;
    private int nextItemId = 1; // widget ids of the Game tab's entries
    private float width = WIDTH, height = HEIGHT; // the size chosen by the user (the grip at the bottom-right)
    private boolean collapsed;
    private boolean dragging;
    private int resizing; // edges being dragged: RESIZE_* bits (0 = none)
    private float dragX, dragY;
    private static final int RESIZE_LEFT = 1, RESIZE_RIGHT = 2, RESIZE_TOP = 4, RESIZE_BOTTOM = 8;
    private static final float EDGE = 6f; // the grab zone of each edge (inside the window)
    private int tab;
    private float scroll, scrollGrab = -1f;
    private float wheel; // this frame's scrolling, in lines

    private final StringBuilder sliderText = new StringBuilder(), titleText = new StringBuilder();

    /**
     * Created by the game host.
     *
     * @param context   The game.
     * @param available Whether the dev tools can be shown at all.
     * @param visible   Whether they start visible.
     */
    public DevTools(GameContext context, boolean available, boolean visible) {
        this.context = context;
        this.available = available;
        this.visible = available && visible;
        this.logBuffer = available ? new DevLog() : null;
        if (logBuffer != null) {
            Logger.addListener(logBuffer);
        }
    }

    // region game API

    /** Whether the dev tools can be shown (see {@code GameSettings.setDevToolsMode}). */
    public boolean isAvailable() {
        return available;
    }

    public boolean isVisible() {
        return visible;
    }

    public void setVisible(boolean visible) {
        boolean show = available && visible && !broken;
        if (show != this.visible) {
            refresh = true;
        }
        this.visible = show;
    }

    public void toggleVisible() {
        setVisible(!visible);
    }

    /** The dev tools' tabs. */
    public enum Tab {PERFORMANCE, RENDERING, ASSETS, LOG, GAME}

    /** Open a tab (and expand the window if it was collapsed). */
    public void showTab(Tab tab) {
        this.tab = tab.ordinal();
        this.collapsed = false;
        this.scroll = 0f;
        this.refresh = true;
    }

    /**
     * @return Whether the pointer is over the dev tools window (the game should ignore clicks then). Up to date as soon
     * as the host passed this frame's pointer, i.e. during the game's update.
     */
    public boolean isPointerOverUi() {
        return visible && (overWindow(pointerX, pointerY) || dragging || resizing != 0 || scrollGrab >= 0f
                || ui.active != 0);
    }

    /**
     * Host (any thread): whether a pointer at these window coordinates would land on the dev tools (to keep that touch
     * or click from the game). Uses the window's last drawn position.
     */
    public boolean isOverUi(float windowX, float windowY) {
        long box = hitBox;
        if (!visible || box == -1L) {
            return false;
        }
        float s = scale, x = windowX / s, y = windowY / s;
        float left = (box >>> 48) & 0xFFFF, top = (box >>> 32) & 0xFFFF;
        return x >= left && x < left + ((box >>> 16) & 0xFFFF) && y >= top && y < top + (box & 0xFFFF);
    }

    private static long packBox(float x, float y, float w, float h) {
        return (u16((float) Math.floor(x)) << 48) | (u16((float) Math.floor(y)) << 32)
                | (u16((float) Math.ceil(w)) << 16) | u16((float) Math.ceil(h));
    }

    private static long u16(float value) {
        return (long) Math.max(0f, Math.min(65534f, value)); // 65535 in every field is the "none" value
    }

    private boolean overWindow(float x, float y) {
        return windowX >= 0f && x >= windowX && x < windowX + windowWidth && y >= windowY
                && y < windowY + windowHeight;
    }

    /** Show a number in the Game tab (2 decimals). */
    public DevTools watch(String name, DoubleSupplier value) {
        return watch(name, value, 2);
    }

    /** Show a number in the Game tab with the given decimals (0-3), read when the numbers are refreshed. */
    public DevTools watch(String name, DoubleSupplier value, int decimals) {
        if (value == null) {
            throw new IllegalArgumentException("Value cannot be null.");
        }
        return add(new Item(name, Item.WATCH, value, null, null, Math.max(0, Math.min(3, decimals))));
    }

    /** Show any value as text in the Game tab ({@code String.valueOf}, when the numbers are refreshed). */
    public DevTools watchText(String name, Supplier<?> value) {
        if (value == null) {
            throw new IllegalArgumentException("Value cannot be null.");
        }
        return add(new Item(name, Item.TEXT, null, value, null, 0));
    }

    /** A switch in the Game tab. */
    public Toggle addToggle(String name, boolean initial) {
        Item item = new Item(name, Item.TOGGLE, null, null, null, 0);
        item.toggle = new Toggle(initial);
        add(item);
        return item.toggle;
    }

    /** A number to tune in the Game tab, from {@code min} to {@code max}. */
    public Slider addSlider(String name, float min, float max, float initial) {
        if (!(max > min)) {
            throw new IllegalArgumentException("max must be greater than min.");
        }
        Item item = new Item(name, Item.SLIDER, null, null, null, 0);
        item.slider = new Slider(min, max, initial);
        add(item);
        return item.slider;
    }

    /** A button in the Game tab (run on the render thread when pressed; a failure is logged). */
    public DevTools addAction(String name, Runnable action) {
        if (action == null) {
            throw new IllegalArgumentException("Action cannot be null.");
        }
        return add(new Item(name, Item.ACTION, null, null, action, 0));
    }

    /** Remove the Game tab entries with that name. */
    public void remove(String name) {
        items.removeIf(item -> item.name.equals(name));
    }

    /**
     * Show a pipeline's last frame in the Render tab: its passes and layers, batches, culled commands and why batches
     * broke. Forgotten when the pipeline is disposed.
     */
    public DevTools track(RenderPipeline pipeline, String name) {
        if (pipeline == null) {
            throw new IllegalArgumentException("Pipeline cannot be null.");
        }
        pruneDisposedPipelines();
        if (!pipelines.contains(pipeline)) {
            pipelines.add(pipeline);
            pipelineNames.add(name != null ? name : "pipeline " + pipelines.size());
        }
        return this;
    }

    private DevTools add(Item item) {
        item.id = nextItemId++;
        items.add(item);
        return this;
    }

    /** A switch added with {@link #addToggle}. */
    public static final class Toggle {
        private boolean on;

        Toggle(boolean on) {
            this.on = on;
        }

        public boolean isOn() {
            return on;
        }

        public void set(boolean on) {
            this.on = on;
        }
    }

    /** A number added with {@link #addSlider}. */
    public static final class Slider {
        private final float min, max;
        private float value;

        Slider(float min, float max, float value) {
            this.min = min;
            this.max = max;
            set(value);
        }

        public float get() {
            return value;
        }

        public void set(float value) {
            this.value = Math.max(min, Math.min(max, value));
        }
    }

    private static final class Item {
        static final int WATCH = 0, TEXT = 1, TOGGLE = 2, SLIDER = 3, ACTION = 4;
        final String name;
        final int kind;
        final DoubleSupplier number;
        final Supplier<?> text;
        final Runnable action;
        final int decimals;
        final StringBuilder shown = new StringBuilder(); // the value as last refreshed
        int id; // stable widget id, set by add() (an index would move when entries are removed)
        boolean failed; // its callback threw (logged once)
        Toggle toggle;
        Slider slider;

        Item(String name, int kind, DoubleSupplier number, Supplier<?> text, Runnable action, int decimals) {
            if (name == null || name.isEmpty()) {
                throw new IllegalArgumentException("Name cannot be empty.");
            }
            this.name = name;
            this.kind = kind;
            this.number = number;
            this.text = text;
            this.action = action;
            this.decimals = decimals;
        }
    }

    // endregion

    // region host API

    /**
     * Host: overlay units per pointer unit (the display scale: the OS content scale on desktop, the density on
     * Android), so the window keeps its physical size. Default 1.
     */
    public void setScale(float scale) {
        this.scale = scale > 0f ? scale : 1f;
    }

    /**
     * Host: the pointer (mouse, or the first touch) in window coordinates ({@code GameWindow} units), once per frame
     * before the game's update. Pass negative coordinates when there is no pointer (no touch).
     */
    public void pointer(float x, float y, boolean down) {
        this.pointerX = x < 0f ? -1f : x / scale;
        this.pointerY = y < 0f ? -1f : y / scale;
        this.wasDown = this.down;
        this.down = down;
    }

    /**
     * Host: scrolling (mouse wheel, trackpad) this frame, in lines of text as the OS scrolls them (its own lines per
     * wheel notch), positive "up" (away from the user); scrolls the part of the window under the pointer. Call after
     * {@link #pointer}.
     */
    public void scroll(float lines) {
        this.wheel = lines;
    }

    /** Host: the toggle key or gesture happened (any thread). */
    public void requestToggle() {
        toggleRequested = true;
    }

    /** Host: a frame's drawing starts (before the game's {@code draw()}): {@link #render()} may draw once again. */
    public void beginFrame() {
        rendered = false;
    }

    /** Host: time spent in the game's {@code update()} this frame. */
    public void recordUpdate(long nanos) {
        pendingUpdate += nanos / 1e6f; // several updates per frame (fixed steps) add up
    }

    /**
     * Host: time spent in the game's {@code draw()}, and the time since the previous frame.
     */
    public void recordDraw(long nanos, float frameSeconds) {
        head = (head + 1) % SAMPLES;
        count = Math.min(count + 1, SAMPLES);
        float frame = frameSeconds * 1000f, drawn = nanos / 1e6f;
        interval[head] = frame;
        update[head] = pendingUpdate;
        draw[head] = drawn;
        windowFrames++;
        windowInterval += frame;
        windowUpdate += pendingUpdate;
        windowDraw += drawn;
        windowMax = Math.max(windowMax, frame);
        pendingUpdate = 0f;
    }

    /**
     * Host: draw the window (after the game's frame, before the frame ends; extra calls in the same frame do
     * nothing). Never throws, except for errors of the VM itself: a failure is logged and the dev tools turn off.
     */
    public void render() {
        if (!available || broken || rendered) {
            return;
        }
        rendered = true;
        if (toggleRequested) {
            toggleRequested = false;
            setVisible(!visible);
        }
        if (!visible) {
            wheel = 0f; // scrolling while hidden never reaches a later frame
            return;
        }
        long start = System.nanoTime();
        try {
            RenderBackend backend = context.graphics().getRenderBackend();
            if (pipeline == null) {
                pipeline = RenderPipeline.builder(backend).pass(view, LAYER).build();
            }
            if (font == null || fontGeneration != backend.getContextGeneration()) {
                // first show, or the context was lost: its texture died with the old one (nothing to delete)
                font = DevFont.load(backend);
                fontGeneration = backend.getContextGeneration();
            }
            if (start - nextRefresh >= 0 || shownFrame <= 0f) { // until there is a measurement: every frame
                nextRefresh = start + REFRESH_NANOS;
                refresh = true;
            }
            if (refresh) {
                snapshotTimes();
            }
            view.refresh(); // one screen size query per frame, not one per command
            drawWindow(pipeline.canvas(LAYER));
            wheel = 0f; // used: a frame the host passes no scrolling doesn't repeat it
            pipeline.render();
            refresh = false;
        } catch (Throwable e) {
            if (e instanceof VirtualMachineError error) {
                throw error; // out of memory, stack overflow: not ours to hide
            }
            broken = true;
            visible = false;
            log.error("Dev tools failed and were turned off.", e);
        }
        windowOwn += System.nanoTime() - start;
    }

    /** Host: release the GPU resources (with the context alive) and stop capturing the log. */
    public void dispose() {
        if (logBuffer != null) {
            Logger.removeListener(logBuffer);
        }
        if (pipeline != null) {
            pipeline.dispose();
            pipeline = null;
        }
        if (font != null) {
            if (fontGeneration == context.graphics().getRenderBackend().getContextGeneration()) {
                font.dispose(); // and its atlas
            }
            font = null;
        }
    }

    // endregion

    // region window

    /** Averages since the last refresh, and a copy of the samples for the graph. */
    private void snapshotTimes() {
        if (windowFrames == 0 && count > 0) { // just shown: average the latest frames, recorded while hidden
            for (int i = 0, n = Math.min(count, 15); i < n; i++) {
                int at = ((head - i) % SAMPLES + SAMPLES) % SAMPLES;
                windowFrames++;
                windowInterval += interval[at];
                windowUpdate += update[at];
                windowDraw += draw[at];
                windowMax = Math.max(windowMax, interval[at]);
            }
        }
        if (windowFrames > 0) {
            shownFrame = windowInterval / windowFrames;
            shownFps = shownFrame > 0f ? Math.round(1000f / shownFrame) : 0;
            fpsHead = (fpsHead + 1) % FPS_SAMPLES;
            fpsCount = Math.min(fpsCount + 1, FPS_SAMPLES);
            fpsHistory[fpsHead] = shownFps;
            shownMax = windowMax;
            shownUpdateMs = windowUpdate / windowFrames;
            shownDrawMs = windowDraw / windowFrames;
            shownOwnMs = windowOwn / 1e6f / windowFrames;
        }
        windowFrames = 0;
        windowInterval = windowUpdate = windowDraw = windowMax = 0f;
        windowOwn = 0L;
        System.arraycopy(interval, 0, shownInterval, 0, SAMPLES);
        System.arraycopy(update, 0, shownUpdate, 0, SAMPLES);
        System.arraycopy(draw, 0, shownDraw, 0, SAMPLES);
        shownHead = Math.max(0, head);
        shownCount = count;
    }

    private void drawWindow(Canvas c) {
        float screenW = view.width(), screenH = view.height();
        ui.begin(c, font, pointerX, pointerY, down, wasDown);

        if (windowX < 0f) { // first show: the top-right corner
            width = Math.min(width, screenW - 2 * MARGIN);
            windowX = Math.max(0f, screenW - width - MARGIN);
        }
        if (dragging) {
            if (down) {
                windowX = pointerX - dragX;
                windowY = pointerY - dragY;
            } else {
                dragging = false;
            }
        }
        if (resizing != 0) {
            if (down) {
                resize();
            } else {
                resizing = 0;
            }
        }
        // keep it on screen: only while the screen is smaller than the chosen size does the window shrink (the size
        // is kept, so it grows back with the screen)
        float fullW = Math.min(width, screenW), fullH = Math.min(height, screenH);
        float w = collapsed ? Math.min(BADGE, fullW) : fullW;
        float h = collapsed ? TITLE : fullH;
        windowX = Math.max(0f, Math.min(windowX, screenW - w));
        windowY = Math.max(0f, Math.min(windowY, screenH - h));
        float x = windowX, y = windowY;

        // edges and corners resize (expanded window only); hovered edges are highlighted
        int edges = collapsed || dragging ? 0 : resizing != 0 ? resizing : edgesAt(x, y, w, h);
        if (edges != 0 && ui.pressed && resizing == 0) {
            resizing = edges;
            dragX = (edges & RESIZE_LEFT) != 0 ? pointerX - x : (edges & RESIZE_RIGHT) != 0 ? x + w - pointerX : 0f;
            dragY = (edges & RESIZE_TOP) != 0 ? pointerY - y : (edges & RESIZE_BOTTOM) != 0 ? y + h - pointerY : 0f;
        }

        // title bar: drag it, or collapse with the arrow
        boolean overArrow = edges == 0 && ui.hover(x + w - 26, y, 26, TITLE);
        if (edges == 0 && ui.clicked(x + w - 26, y, 26, TITLE)) {
            collapsed = !collapsed;
            float newWidth = collapsed ? Math.min(BADGE, fullW) : fullW;
            x = windowX = Math.max(0f, x + w - newWidth); // the arrow (right edge) stays under the pointer
            w = newWidth;
            h = collapsed ? TITLE : fullH;
        } else if (edges == 0 && ui.clicked(x, y, w - 26, TITLE)) {
            dragging = true;
            dragX = pointerX - x;
            dragY = pointerY - y;
        }
        StringBuilder title = titleText;
        title.setLength(0);
        title.append(shownFps).append(" fps");
        if (!collapsed) {
            title.append("  ");
            DevUi.fixed(title, shownFrame, 1).append(" ms");
        }
        c.text(font, collapsed ? "dev" : "pixel dev tools", x + 10, y + 6).size(DevUi.TEXT).fill(DevUi.ACCENT);
        c.text(font, title, x + w - 30, y + 6).size(DevUi.TEXT).align(TextAlign.RIGHT).fill(fpsColor(shownFps));
        c.text(font, collapsed ? "▶" : "▼", x + w - 13, y + 6).size(DevUi.TEXT).align(TextAlign.CENTER)
                .fill(overArrow ? DevUi.TEXT_COLOR : DevUi.MUTED);

        if (!collapsed) {
            float columnX = x + DevUi.PAD + 2, columnW = w - 2 * DevUi.PAD - 4 - SCROLLBAR;
            ui.column(columnX, y + TITLE + 4, columnW);
            int selected = edges == 0 ? ui.tabs(TABS, tab) : drawTabsOnly();
            if (selected != tab) {
                tab = selected;
                scroll = 0f;
                refresh = true;
            }
            float bodyTop = ui.cursorY();
            float bodyHeight = Math.max(DevUi.LINE, y + h - DevUi.PAD - bodyTop); // fixed: the content scrolls

            // the body, scrolled and clipped to the window (no widget reacts while an edge is grabbed)
            c.save();
            c.clip(x, bodyTop, w, bodyHeight);
            ui.clipRows(bodyTop, edges != 0 ? bodyTop : bodyTop + bodyHeight);
            ui.column(columnX, bodyTop - scroll, columnW);
            body();
            float content = ui.columnHeight();
            ui.noClip();
            c.restore();
            if (wheel != 0f && ui.hover(x, bodyTop, w, bodyHeight)) { // the body and its scroll bar
                scroll -= wheel * DevUi.LINE; // the OS's lines, ours tall (clamped by the scroll bar)
            }
            scrollbar(c, x + w - DevUi.PAD - SCROLLBAR, bodyTop, bodyHeight, content);

            // a grip in the bottom-right corner, and the hovered or dragged edges highlighted
            Color grip = (edges & (RESIZE_RIGHT | RESIZE_BOTTOM)) == (RESIZE_RIGHT | RESIZE_BOTTOM)
                    ? DevUi.TEXT_COLOR : DevUi.EDGE;
            for (int i = 0; i < 3; i++) {
                float o = 4 + i * 3.5f;
                c.line(x + w - o, y + h - 2, x + w - 2, y + h - o).width(1.2f).color(grip);
            }
            if ((edges & RESIZE_LEFT) != 0) {
                c.line(x + 1, y + 4, x + 1, y + h - 4).width(2).color(DevUi.ACCENT);
            }
            if ((edges & RESIZE_RIGHT) != 0) {
                c.line(x + w - 1, y + 4, x + w - 1, y + h - 4).width(2).color(DevUi.ACCENT);
            }
            if ((edges & RESIZE_TOP) != 0) {
                c.line(x + 4, y + 1, x + w - 4, y + 1).width(2).color(DevUi.ACCENT);
            }
            if ((edges & RESIZE_BOTTOM) != 0) {
                c.line(x + 4, y + h - 1, x + w - 4, y + h - 1).width(2).color(DevUi.ACCENT);
            }
        }
        windowWidth = w;
        windowHeight = h;
        hitBox = packBox(x, y, w, h);
        // background last, below everything (the layer is sorted: lower depth first)
        c.roundRect(x, y, w, h, 8).fill(DevUi.PANEL).stroke(1, DevUi.EDGE).depth(-2);
        c.roundRect(x + 1, y + 1, w - 2, TITLE - 1, 7).fill(DevUi.BAR).depth(-1);
    }

    /** The edges of the window under the pointer (corners: two edges). */
    private int edgesAt(float x, float y, float w, float h) {
        if (!ui.hover(x - 2, y - 2, w + 4, h + 4)) {
            return 0;
        }
        float px = pointerX, py = pointerY;
        int edges = 0;
        float corner = 12f; // corners are easier to grab than edges
        boolean nearTop = py < y + EDGE, nearBottom = py >= y + h - EDGE;
        boolean nearLeft = px < x + EDGE, nearRight = px >= x + w - EDGE;
        if (nearLeft || ((nearTop || nearBottom) && px < x + corner)) {
            edges |= RESIZE_LEFT;
        }
        if (nearRight || ((nearTop || nearBottom) && px >= x + w - corner)) {
            edges |= RESIZE_RIGHT;
        }
        if (nearTop || ((nearLeft || nearRight) && py < y + corner)) {
            edges |= RESIZE_TOP;
        }
        if (nearBottom || ((nearLeft || nearRight) && py >= y + h - corner)) {
            edges |= RESIZE_BOTTOM;
        }
        return edges;
    }

    /**
     * Move the dragged edges to the pointer. The opposite edges stay where they are; an edge stops at the screen and
     * at the minimum size.
     */
    private void resize() {
        float screenW = view.width(), screenH = view.height();
        float right = windowX + Math.min(width, screenW), bottom = windowY + Math.min(height, screenH);
        if ((resizing & RESIZE_RIGHT) != 0) {
            width = clamp(pointerX + dragX - windowX, MIN_WIDTH, screenW - windowX);
        } else if ((resizing & RESIZE_LEFT) != 0) {
            float left = clamp(pointerX - dragX, 0f, right - MIN_WIDTH);
            width = right - left;
            windowX = left;
        }
        if ((resizing & RESIZE_BOTTOM) != 0) {
            height = clamp(pointerY + dragY - windowY, MIN_HEIGHT, screenH - windowY);
        } else if ((resizing & RESIZE_TOP) != 0) {
            float top = clamp(pointerY - dragY, 0f, bottom - MIN_HEIGHT);
            height = bottom - top;
            windowY = top;
        }
    }

    private static float clamp(float value, float min, float max) {
        return Math.max(min, Math.min(max, value));
    }

    /** The tabs without reacting to the pointer (while an edge is grabbed). */
    private int drawTabsOnly() {
        boolean pressed = ui.pressed;
        ui.pressed = false;
        int selected = ui.tabs(TABS, tab);
        ui.pressed = pressed;
        return selected;
    }

    /** A scroll bar when the content is taller than the space: drag the thumb, or press the track to jump. */
    private void scrollbar(Canvas c, float x, float top, float visibleHeight, float content) {
        float max = Math.max(0f, content - visibleHeight);
        if (max <= 0f) {
            scroll = 0f;
            scrollGrab = -1f;
            return;
        }
        float thumb = Math.max(24f, visibleHeight * visibleHeight / content);
        float travel = visibleHeight - thumb;
        if (ui.pressed && ui.hover(x - 4, top, SCROLLBAR + 8, visibleHeight)) {
            float thumbY = top + travel * scroll / max;
            scrollGrab = ui.pointerY >= thumbY && ui.pointerY < thumbY + thumb ? ui.pointerY - thumbY : thumb * 0.5f;
        }
        if (scrollGrab >= 0f) {
            if (ui.down && travel > 0f) {
                scroll = (ui.pointerY - scrollGrab - top) / travel * max;
            } else {
                scrollGrab = -1f;
            }
        }
        scroll = Math.max(0f, Math.min(scroll, max));
        c.roundRect(x, top, SCROLLBAR, visibleHeight, 3).fill(DevUi.CONTROL);
        c.roundRect(x, top + travel * scroll / max, SCROLLBAR, thumb, 3)
                .fill(scrollGrab >= 0f ? DevUi.TEXT_COLOR : DevUi.EDGE);
    }

    private void body() {
        if (tab == 4) {
            game(); // live widgets; watched values follow the refresh
            return;
        }
        if (tab == 3) {
            logControls();
        } else if (tab == 2) {
            assetControls();
        }
        if (refresh || linesTab != tab) {
            lines.clear();
            switch (tab) {
                case 0 -> performance();
                case 1 -> rendering();
                case 2 -> assets();
                default -> logLines();
            }
            linesTab = tab;
        }
        lines.draw();
    }

    private static Color fpsColor(int fps) {
        return fps >= 55 ? DevUi.GOOD : fps >= 30 ? DevUi.WARN : DevUi.ERROR;
    }

    // endregion

    // region tabs

    private void performance() {
        float minFps = Float.MAX_VALUE, maxFps = 0f, sumFps = 0f;
        for (int i = 0; i < fpsCount; i++) {
            minFps = Math.min(minFps, fpsHistory[i]);
            maxFps = Math.max(maxFps, fpsHistory[i]);
            sumFps += fpsHistory[i];
        }
        lines.row("fps", fpsColor(shownFps)).append(shownFps);
        if (fpsCount > 0) {
            StringBuilder v = lines.row("min / avg / max", DevUi.MUTED);
            v.append(Math.round(minFps)).append(" / ").append(Math.round(sumFps / fpsCount)).append(" / ")
                    .append(Math.round(maxFps));
        }
        fpsScale = Math.max(60f, maxFps * 1.1f);
        lines.fpsGraph().append("fps, 1 min (0-").append(Math.round(fpsScale)).append(')');

        float graphMax = 0f;
        for (int i = 0; i < shownCount; i++) {
            graphMax = Math.max(graphMax, shownInterval[i]);
        }
        frameScale = Math.max(2f, graphMax * 1.15f); // scaled to what happens: fast frames are visible too
        StringBuilder v = lines.row("frame", shownMax > MARKS[1] ? DevUi.WARN : DevUi.TEXT_COLOR);
        DevUi.fixed(v, shownFrame, 2).append(" ms avg, ");
        DevUi.fixed(v, shownMax, 1).append(" ms max");
        DevUi.fixed(lines.graph().append("frame ms (0-"), frameScale, 1).append(')');
        DevUi.fixed(lines.row("update", DevUi.ACCENT), shownUpdateMs, 2).append(" ms");
        DevUi.fixed(lines.row("draw", DevUi.GOOD), shownDrawMs, 2).append(" ms");
        DevUi.fixed(lines.row("dev tools", DevUi.MUTED), shownOwnMs, 2).append(" ms");

        lines.heading().append("memory");
        Runtime runtime = Runtime.getRuntime();
        v = lines.row("heap used / max", DevUi.TEXT_COLOR);
        DevUi.bytes(v, runtime.totalMemory() - runtime.freeMemory()).append(" / ");
        DevUi.bytes(v, runtime.maxMemory());
        if (jvm.refresh()) {
            v = lines.row("garbage collections", DevUi.TEXT_COLOR);
            v.append(jvm.collections).append(" (");
            DevUi.fixed(v, jvm.collectionMillis / 1000.0, 2).append(" s total)");
        }
    }

    private float fpsScale = 60f, frameScale = 34f;

    private void rendering() {
        RenderStats.Frame frame = context.graphics().getStats().lastFrame();
        long own = pipeline.lastFrame().getDrawCalls();
        lines.row("draw calls", DevUi.TEXT_COLOR).append(frame.getDrawCalls() - own).append(" (+").append(own)
                .append(" dev tools)");
        lines.row("primitives (with dev tools)", DevUi.TEXT_COLOR).append(frame.getPrimitives());
        DevUi.bytes(lines.row("uploaded (with dev tools)", DevUi.TEXT_COLOR), frame.getUploadedBytes());
        lines.row("texture binds (with dev tools)", DevUi.TEXT_COLOR).append(frame.getTextureBinds());

        pruneDisposedPipelines();
        if (pipelines.isEmpty()) {
            lines.space();
            lines.text(DevUi.MUTED).append("devTools().track(pipeline, name) shows a pipeline here");
            return;
        }
        for (int p = 0; p < pipelines.size(); p++) {
            FrameInfo info = pipelines.get(p).lastFrame();
            StringBuilder h = lines.heading();
            h.append(pipelineNames.get(p)).append(": ").append(info.getDrawCalls()).append(" draws, ")
                    .append(info.getBatchCount()).append(" batches, ");
            DevUi.fixed(h, info.getNanos() / 1e6, 2).append(" ms");
            for (FrameInfo.LayerStats layer : info.getLayers()) {
                StringBuilder v = lines.row(DevUi.TEXT_COLOR);
                lines.key().append(layer.getPass()).append(' ').append(layer.getLayer().getName());
                v.append(layer.getCommands()).append(" cmds");
                if (layer.getCulled() > 0) {
                    v.append(", ").append(layer.getCulled()).append(" culled");
                }
                v.append(", ").append(layer.getBatches()).append(" batches");
                for (BatchBreak reason : REASONS) {
                    int n = layer.getBreaks(reason);
                    if (reason != BatchBreak.FIRST && n > 0) {
                        lines.text(DevUi.WARN).append("    ").append(n).append(" x ").append(reason.getDescription());
                    }
                }
            }
        }
    }

    private void pruneDisposedPipelines() {
        for (int p = pipelines.size() - 1; p >= 0; p--) {
            if (pipelines.get(p).isDisposed()) {
                pipelines.remove(p);
                pipelineNames.remove(p);
            }
        }
    }

    // region assets: collected into pooled arrays, sorted, with their memory

    private static final CharSequence[] SORTS = {"size", "name", "type"};
    private int assetSort; // 0 size, 1 name, 2 type
    private String[] assetPaths = new String[64];
    private Class<?>[] assetTypes = new Class<?>[64];
    private long[] assetSizes = new long[64];
    private int[] assetOrder = new int[64];
    private int assetCount;
    private long assetsTotal;
    private int assetDepth;
    private final ContentManager.AssetVisitor assetVisitor = (path, type, asset) -> {
        if (assetCount == assetPaths.length) {
            int capacity = assetCount * 2;
            assetPaths = Arrays.copyOf(assetPaths, capacity);
            assetTypes = Arrays.copyOf(assetTypes, capacity);
            assetSizes = Arrays.copyOf(assetSizes, capacity);
            assetOrder = Arrays.copyOf(assetOrder, capacity);
        }
        assetPaths[assetCount] = path;
        assetTypes[assetCount] = type;
        assetSizes[assetCount] = memoryOf(asset);
        assetCount++;
    };
    private final Consumer<ContentManager> childVisitor = this::manager;

    /** Approximate bytes an asset keeps (see {@code SizedAsset}); -1 if unknown. */
    private static long memoryOf(Object asset) {
        if (asset instanceof org.pixel.content.SizedAsset sized) {
            return sized.getMemorySize();
        } else if (asset instanceof CharSequence text) {
            return text.length() * 2L;
        } else if (asset instanceof byte[] bytes) {
            return bytes.length;
        } else if (asset instanceof java.nio.Buffer buffer) {
            return buffer.capacity();
        }
        return -1;
    }

    private void assetControls() {
        float x = ui.left(), y = ui.cursorY();
        ui.canvas().text(font, "sort by", x, y + 2).size(DevUi.TEXT).fill(DevUi.MUTED);
        x += ui.font().measure("sort by", DevUi.TEXT) + 8;
        for (int i = 0; i < SORTS.length; i++) {
            if (ui.chip(SORTS[i], x, y, assetSort == i)) {
                assetSort = i;
                refresh = true;
            }
            x += ui.chipWidth(SORTS[i]) + 4;
        }
        ui.space(DevUi.LINE + 6);
    }

    private void assets() {
        assetsTotal = 0;
        assetDepth = 0;
        int heading = lines.size(); // the total goes first, once it is known
        lines.heading();
        manager(context.content());
        StringBuilder total = lines.valueAt(heading);
        total.append("all managers: ");
        DevUi.bytes(total, assetsTotal);
    }

    private void manager(ContentManager manager) {
        if (manager == null || manager.isDisposed()) {
            return;
        }
        assetCount = 0;
        manager.forEachAsset(assetVisitor);
        long sum = 0;
        for (int i = 0; i < assetCount; i++) {
            assetOrder[i] = i;
            sum += Math.max(0, assetSizes[i]);
        }
        assetsTotal += sum;
        sortAssets();
        StringBuilder h = lines.heading();
        for (int i = 0; i < assetDepth; i++) {
            h.append("  ");
        }
        h.append(manager.getName()).append(": ").append(assetCount).append(" assets, ");
        DevUi.bytes(h, sum);
        int shown = Math.min(assetCount, ASSET_LINES);
        for (int k = 0; k < shown; k++) {
            int i = assetOrder[k];
            StringBuilder v = lines.row(DevUi.TEXT_COLOR);
            v.append(assetTypes[i].getSimpleName()).append("  ");
            if (assetSizes[i] >= 0) {
                DevUi.bytes(v, assetSizes[i]);
            } else {
                v.append('?');
            }
            lines.key().append(assetPaths[i]);
        }
        if (assetCount > shown) {
            lines.text(DevUi.MUTED).append("+ ").append(assetCount - shown).append(" more");
        }
        Arrays.fill(assetPaths, 0, assetCount, null); // no references kept between refreshes
        Arrays.fill(assetTypes, 0, assetCount, null);
        assetDepth++;
        manager.forEachChild(childVisitor);
        assetDepth--;
    }

    /** Insertion sort of the indices (a few hundred assets at most, four times a second; no allocation). */
    private void sortAssets() {
        for (int k = 1; k < assetCount; k++) {
            int i = assetOrder[k], j = k - 1;
            while (j >= 0 && assetBefore(i, assetOrder[j])) {
                assetOrder[j + 1] = assetOrder[j];
                j--;
            }
            assetOrder[j + 1] = i;
        }
    }

    private boolean assetBefore(int a, int b) {
        return switch (assetSort) {
            case 0 -> assetSizes[a] > assetSizes[b];
            case 1 -> assetPaths[a].compareTo(assetPaths[b]) < 0;
            default -> {
                int byType = assetTypes[a].getSimpleName().compareTo(assetTypes[b].getSimpleName());
                yield byType != 0 ? byType < 0 : assetSizes[a] > assetSizes[b];
            }
        };
    }

    // endregion

    private void logControls() {
        if (ui.button("clear")) {
            logBuffer.clear();
            refresh = true;
        }
    }

    private void logLines() {
        lines.row("since cleared", logBuffer.errors() > 0 ? DevUi.ERROR
                        : logBuffer.warnings() > 0 ? DevUi.WARN : DevUi.GOOD)
                .append(logBuffer.warnings()).append(" warnings, ").append(logBuffer.errors()).append(" errors");
        int n = logBuffer.snapshot(logLevels, logContexts, logMessages); // consistent while other threads log
        if (n == 0) {
            lines.text(DevUi.MUTED).append("nothing logged");
        }
        for (int age = 0; age < n; age++) {
            LogLevel level = logLevels[age];
            lines.text(level == LogLevel.ERROR ? DevUi.ERROR : level == LogLevel.WARN ? DevUi.WARN
                            : level == LogLevel.INFO ? DevUi.TEXT_COLOR : DevUi.MUTED)
                    .append(level == LogLevel.ERROR ? "E " : level == LogLevel.WARN ? "W "
                            : level == LogLevel.INFO ? "I " : "D ")
                    .append(logContexts[age]).append(": ").append(logMessages[age]);
        }
        Arrays.fill(logContexts, null); // don't keep messages past the snapshot
        Arrays.fill(logMessages, null);
    }

    private void game() {
        if (items.isEmpty()) {
            ui.text("devTools().watch / addToggle / addSlider / addAction", DevUi.MUTED);
            ui.text("show the game's own values here", DevUi.MUTED);
            return;
        }
        for (int i = 0; i < items.size(); i++) {
            Item item = items.get(i);
            switch (item.kind) {
                case Item.WATCH, Item.TEXT -> {
                    if (refresh || item.shown.length() == 0) {
                        refreshValue(item);
                    }
                    ui.row(item.name, item.shown, item.failed ? DevUi.ERROR : DevUi.TEXT_COLOR);
                }
                case Item.TOGGLE -> item.toggle.set(ui.checkbox(item.name, item.toggle.isOn()));
                case Item.SLIDER -> {
                    sliderText.setLength(0);
                    DevUi.fixed(sliderText, item.slider.get(), 2);
                    item.slider.set(ui.slider(item.id, item.name, sliderText, item.slider.get(), item.slider.min,
                            item.slider.max));
                }
                default -> {
                    if (ui.button(item.name)) {
                        try {
                            item.action.run();
                        } catch (RuntimeException e) {
                            log.error("Dev tools action \"{0}\" failed.", item.name, e);
                        }
                    }
                }
            }
        }
    }

    /** Read a watch; a callback that throws shows "error" (logged once) instead of turning the tools off. */
    private void refreshValue(Item item) {
        item.shown.setLength(0);
        try {
            if (item.kind == Item.WATCH) {
                DevUi.fixed(item.shown, item.number.getAsDouble(), item.decimals);
            } else {
                item.shown.append(item.text.get());
            }
            item.failed = false;
        } catch (RuntimeException e) {
            item.shown.setLength(0);
            item.shown.append("error");
            if (!item.failed) {
                log.error("Dev tools watch \"{0}\" failed.", item.name, e);
            }
            item.failed = true;
        }
    }

    // endregion

    /**
     * The text lines of a tab, rebuilt only when the numbers are refreshed: drawing them every frame costs canvas
     * commands but no formatting. Pooled builders: allocation-free once warmed up.
     */
    private final class Lines {
        private static final int ROW = 0, TEXT = 1, HEADING = 2, SPACE = 3, GRAPH = 4, FPS = 5;
        private int size;
        private int[] kinds = new int[64];
        private StringBuilder[] keys = new StringBuilder[64], values = new StringBuilder[64];
        private Color[] colors = new Color[64];

        void clear() {
            size = 0;
        }

        private int next(int kind, Color color) {
            if (size == kinds.length) {
                kinds = Arrays.copyOf(kinds, size * 2);
                keys = Arrays.copyOf(keys, size * 2);
                values = Arrays.copyOf(values, size * 2);
                colors = Arrays.copyOf(colors, size * 2);
            }
            if (keys[size] == null) {
                keys[size] = new StringBuilder();
                values[size] = new StringBuilder();
            }
            keys[size].setLength(0);
            values[size].setLength(0);
            kinds[size] = kind;
            colors[size] = color;
            return size++;
        }

        /** A key/value row; returns the value to append to. */
        StringBuilder row(String key, Color color) {
            int i = next(ROW, color);
            keys[i].append(key);
            return values[i];
        }

        /** A row whose key is appended with {@link #key()}; returns the value. */
        StringBuilder row(Color color) {
            return values[next(ROW, color)];
        }

        /** The key of the last row. */
        StringBuilder key() {
            return keys[size - 1];
        }

        StringBuilder text(Color color) {
            return values[next(TEXT, color)];
        }

        StringBuilder heading() {
            return values[next(HEADING, DevUi.ACCENT)];
        }

        void space() {
            next(SPACE, null);
        }

        /** The frame-time graph; returns its label. */
        StringBuilder graph() {
            return keys[next(GRAPH, null)];
        }

        /** The FPS history graph; returns its label. */
        StringBuilder fpsGraph() {
            return keys[next(FPS, null)];
        }

        int size() {
            return size;
        }

        /** The value of line {@code i} (e.g. a heading filled in afterwards). */
        StringBuilder valueAt(int i) {
            return values[i];
        }

        void draw() {
            for (int i = 0; i < size; i++) {
                switch (kinds[i]) {
                    case ROW -> ui.row(keys[i], values[i], colors[i]);
                    case TEXT -> ui.text(values[i], colors[i]);
                    case HEADING -> ui.heading(values[i]);
                    case SPACE -> ui.space(6);
                    case FPS -> ui.bars(fpsHistory, fpsCount, Math.max(0, fpsHead), fpsScale, DevUi.ACCENT, 44,
                            keys[i]);
                    default -> ui.graph(shownInterval, shownUpdate, shownDraw, shownCount, shownHead, frameScale,
                            MARKS, 60, keys[i]);
                }
            }
        }
    }

    /**
     * Overlay units: window coordinates divided by the scale, origin at the top-left; anti-aliasing follows the
     * framebuffer's pixels per unit (measured once per frame: canvases ask for it on every command, and querying the
     * GPU that often is slow, very much so on Android).
     */
    private final class OverlayView implements View {
        private final Matrix4 matrix = new Matrix4();
        private float width = -1, height = -1;
        private float pixelsPerUnit = 1f;

        void refresh() {
            pixelsPerUnit = context.graphics().getRenderBackend().getScreenWidth() / width();
        }

        float width() {
            return Math.max(1f, context.window().getWidth() / scale);
        }

        float height() {
            return Math.max(1f, context.window().getHeight() / scale);
        }

        @Override
        public Matrix4 getViewMatrix() {
            float w = width(), h = height();
            if (w != width || h != height) {
                width = w;
                height = h;
                matrix.setOrthographic(0, w, h, 0, 0f, 1f);
            }
            return matrix;
        }

        @Override
        public float getPixelsPerUnit() {
            return pixelsPerUnit;
        }
    }

    /**
     * Garbage collector counters where {@code java.lang.management} exists (desktop JVMs; not Android).
     */
    private static final class JvmStats {
        private List<java.lang.management.GarbageCollectorMXBean> collectors;
        private boolean unsupported;
        long collections;
        long collectionMillis;

        boolean refresh() {
            if (unsupported) {
                return false;
            }
            try {
                if (collectors == null) {
                    collectors = java.lang.management.ManagementFactory.getGarbageCollectorMXBeans();
                }
                long n = 0, ms = 0;
                for (int i = 0; i < collectors.size(); i++) {
                    n += Math.max(0, collectors.get(i).getCollectionCount());
                    ms += Math.max(0, collectors.get(i).getCollectionTime());
                }
                collections = n;
                collectionMillis = ms;
                return true;
            } catch (LinkageError | RuntimeException e) {
                unsupported = true;
                return false;
            }
        }
    }
}
