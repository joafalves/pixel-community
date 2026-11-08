package org.pixel.core.dev;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.pixel.commons.logger.LogLevel;
import org.pixel.commons.logger.LoggerFactory;
import org.pixel.content.ContentManager;
import org.pixel.content.Texture;
import org.pixel.core.GameContext;
import org.pixel.core.GameSettings;
import org.pixel.core.GameWindow;
import org.pixel.graphics.GraphicsDevice;
import org.pixel.graphics.RenderStats;
import org.pixel.graphics.backend.BackendCapabilities;
import org.pixel.graphics.backend.RenderBackend;
import org.pixel.graphics.backend.RenderData;

import java.lang.management.ManagementFactory;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

/**
 * The dev tools without a GPU: availability, the hidden state costing nothing, the window's widgets driven by the
 * pointer, the log buffer and number formatting.
 */
class DevToolsTest {

    private static final int SCREEN_W = 1280, SCREEN_H = 720;
    // the window starts at the top-right: x = 1280 - 380 - 8; its column starts 10 units in, below title and tabs
    private static final float COLUMN_X = 902, CONTENT_Y = 70;

    private final RenderBackend backend = mock(RenderBackend.class);
    private final GraphicsDevice graphics = mock(GraphicsDevice.class);
    private final GameContext context = mock(GameContext.class);
    private DevTools tools;

    private GameContext context() {
        when(backend.capabilities()).thenReturn(new BackendCapabilities(16, 16384, 256, 128, 64));
        when(backend.getScreenWidth()).thenReturn(SCREEN_W);
        when(backend.getScreenHeight()).thenReturn(SCREEN_H);
        when(backend.createTexture(anyInt(), anyInt(), any(ByteBuffer.class), anyBoolean()))
                .thenAnswer(call -> new Texture(call.getArgument(0), call.getArgument(1)) {
                    @Override
                    public void dispose() {
                    }
                });
        when(graphics.getRenderBackend()).thenReturn(backend);
        when(graphics.getStats()).thenReturn(new RenderStats());
        GameWindow window = mock(GameWindow.class);
        when(window.getWidth()).thenReturn(SCREEN_W);
        when(window.getHeight()).thenReturn(SCREEN_H);
        ContentManager content = mock(ContentManager.class);
        when(content.getName()).thenReturn("root");
        when(context.graphics()).thenReturn(graphics);
        when(context.window()).thenReturn(window);
        when(context.content()).thenReturn(content);
        when(context.settings()).thenReturn(new GameSettings("test", SCREEN_W, SCREEN_H));
        return context;
    }

    @AfterEach
    void dispose() {
        if (tools != null) {
            tools.dispose();
        }
    }

    /** One host frame: pointer, timings, then the overlay. */
    private void frame(float x, float y, boolean down) {
        tools.pointer(x, y, down);
        tools.recordUpdate(1_000_000);
        tools.beginFrame();
        tools.recordDraw(2_000_000, 1 / 60f);
        tools.render();
    }

    private void click(float x, float y) {
        frame(x, y, true);
        frame(x, y, false);
    }

    @Test
    void unavailableToolsCostNothingAndKeepGameValues() {
        tools = new DevTools(context, false, true);
        DevTools.Toggle toggle = tools.addToggle("debug", true);
        DevTools.Slider slider = tools.addSlider("speed", 0, 10, 20); // clamped
        tools.setVisible(true);
        tools.requestToggle();
        frame(10, 10, true);
        assertFalse(tools.isVisible());
        assertFalse(tools.isPointerOverUi());
        assertTrue(toggle.isOn());
        assertEquals(10f, slider.get());
        verifyNoInteractions(context);
    }

    @Test
    void hiddenToolsDrawNothingUntilToggled() {
        tools = new DevTools(context(), true, false);
        frame(10, 10, false);
        verify(backend, never()).createTexture(anyInt(), anyInt(), any(), anyBoolean());
        tools.requestToggle();
        frame(10, 10, false);
        assertTrue(tools.isVisible());
        verify(backend).createTexture(anyInt(), anyInt(), any(), anyBoolean()); // the engine font, once
        verify(backend, atLeastOnce()).submit(any(RenderData.class));
    }

    @Test
    void everyTabDraws() {
        tools = new DevTools(context(), true, true);
        tools.watch("enemies", () -> 3).watchText("state", () -> "running");
        LoggerFactory.getLogger("DevToolsTest").warn("something {0}", "odd");
        for (DevTools.Tab tab : DevTools.Tab.values()) {
            tools.showTab(tab);
            frame(-100, -100, false);
            assertTrue(tools.isVisible(), tab + " failed");
        }
    }

    @Test
    void gameWidgetsFollowThePointer() {
        tools = new DevTools(context(), true, true);
        tools.showTab(DevTools.Tab.GAME);
        AtomicInteger respawns = new AtomicInteger();
        tools.watch("hp", () -> 7, 0);                            // row 0
        DevTools.Toggle colliders = tools.addToggle("colliders", false); // row 1
        DevTools.Slider speed = tools.addSlider("speed", 0, 100, 50);    // label row 2, track below
        tools.addAction("respawn", respawns::incrementAndGet);    // button after the slider
        frame(-1, -1, false);

        click(COLUMN_X + 4, CONTENT_Y + DevUi.LINE * 1.5f);
        assertTrue(colliders.isOn());
        assertTrue(tools.isPointerOverUi());

        float trackY = CONTENT_Y + DevUi.LINE * 3 + 6;
        frame(COLUMN_X + 1, trackY, true);           // grab the slider at its left end...
        frame(COLUMN_X + 400, trackY, true);         // ...and drag past its right end
        assertEquals(100f, speed.get(), 1e-3f);
        frame(COLUMN_X + 400, trackY, false);

        click(COLUMN_X + 6, trackY + 18 + 6);
        assertEquals(1, respawns.get());

        frame(10, 600, false);
        assertFalse(tools.isPointerOverUi(), "the game gets the pointer outside the window");
    }

    @Test
    void theTitleCollapsesAndDragsTheWindow() {
        tools = new DevTools(context(), true, true);
        frame(-1, -1, false);
        click(SCREEN_W - 12 - 13, 12 + 10); // the arrow at the title's right end
        frame(SCREEN_W - 12 - 140, 20, false);
        assertTrue(tools.isPointerOverUi(), "collapsed: the badge is still there");
        frame(SCREEN_W - 12 - 300, 20, false);
        assertFalse(tools.isPointerOverUi(), "collapsed: the rest of the window is gone");

        frame(SCREEN_W - 12 - 100, 20, true); // drag the badge by its title down and left
        frame(400, 300, true);
        frame(400, 300, false);
        frame(400, 300, false);
        assertTrue(tools.isPointerOverUi());
    }

    @Test
    void aFailureTurnsTheToolsOffInsteadOfBreakingTheGame() {
        tools = new DevTools(context(), true, true);
        when(backend.createTexture(anyInt(), anyInt(), any(), anyBoolean())).thenThrow(new IllegalStateException("x"));
        assertDoesNotThrow(() -> frame(10, 10, false));
        assertFalse(tools.isVisible());
        tools.setVisible(true);
        assertFalse(tools.isVisible(), "stays off");
    }

    @Test
    void aWatchThatThrowsShowsAnErrorAndTheToolsStayOn() {
        tools = new DevTools(context(), true, true);
        tools.showTab(DevTools.Tab.GAME);
        tools.watch("broken", () -> {
            throw new IllegalStateException("game bug");
        });
        tools.addAction("also broken", () -> {
            throw new IllegalStateException("game bug");
        });
        frame(-1, -1, false);
        click(COLUMN_X + 6, CONTENT_Y + DevUi.LINE + 6); // the action's button, below the watch row
        frame(-1, -1, false);
        assertTrue(tools.isVisible());
    }

    @Test
    void theWindowDrawsOncePerFrame() {
        tools = new DevTools(context(), true, true);
        frame(-1, -1, false);
        clearInvocations(backend);
        tools.beginFrame(); // a new frame (even one without an update)
        tools.render();
        tools.render(); // e.g. Screenshot.capture, then the host
        verify(backend, times(1)).submit(any(RenderData.class));
    }

    @Test
    void theFontIsRebuiltAfterTheContextWasLost() {
        tools = new DevTools(context(), true, true);
        frame(-1, -1, false);
        when(backend.getContextGeneration()).thenReturn(1);
        frame(-1, -1, false);
        verify(backend, times(2)).createTexture(anyInt(), anyInt(), any(), anyBoolean());
    }

    @Test
    void narrowScreensGetANarrowerWindow() {
        GameContext small = context();
        when(small.window().getWidth()).thenReturn(320);
        when(small.window().getHeight()).thenReturn(200);
        tools = new DevTools(small, true, true);
        for (int i = 0; i < 30; i++) {
            LoggerFactory.getLogger("DevToolsTest").info("line {0}", i); // taller than the screen
        }
        tools.showTab(DevTools.Tab.LOG);
        frame(-1, -1, false);
        frame(12, 100, false);
        assertTrue(tools.isPointerOverUi(), "8 units from the left edge: inside a window as wide as the screen");
        frame(315, 100, false);
        assertFalse(tools.isPointerOverUi(), "and 8 units from the right edge: outside");
        frame(100, 195, false);
        assertTrue(tools.isPointerOverUi(), "taller than the screen: as tall as the screen (its content scrolls)");
    }

    @Test
    void hiddenFramesDoNotAllocate() {
        tools = new DevTools(context(), true, false);
        for (int i = 0; i < 1000; i++) {
            frame(i, i, i % 2 == 0);
        }
        // five windows: a real per-frame allocation shows in all of them, a late JIT compilation in one at most
        com.sun.management.ThreadMXBean threads = (com.sun.management.ThreadMXBean) ManagementFactory.getThreadMXBean();
        long thread = Thread.currentThread().getId();
        long[] windows = new long[5];
        for (int w = 0; w < windows.length; w++) {
            long before = threads.getThreadAllocatedBytes(thread);
            for (int i = 0; i < 200; i++) {
                frame(i, i, i % 2 == 0);
            }
            windows[w] = threads.getThreadAllocatedBytes(thread) - before;
        }
        assertEquals(0, Arrays.stream(windows).min().orElseThrow(), Arrays.toString(windows));
    }

    @Test
    void theLogKeepsTheNewestMessages() {
        DevLog log = new DevLog();
        for (int i = 0; i < DevLog.CAPACITY + 5; i++) {
            log.onLog(i % 10 == 0 ? LogLevel.WARN : LogLevel.INFO, "ctx", "message " + i);
        }
        assertEquals(DevLog.CAPACITY, log.size());
        assertEquals("message " + (DevLog.CAPACITY + 4), log.message(0));
        assertEquals("message 5", log.message(DevLog.CAPACITY - 1));
        assertEquals(21, log.warnings()); // 0, 10, ..., 200
        log.clear();
        assertEquals(0, log.size());
        assertEquals(0, log.warnings());
    }

    @Test
    void loggedMessagesReachListenersFormatted() {
        DevLog log = new DevLog();
        org.pixel.commons.logger.Logger.addListener(log);
        try {
            LoggerFactory.getLogger("DevToolsTest").error("failed {0} times", 3, new IllegalStateException("boom"));
        } finally {
            org.pixel.commons.logger.Logger.removeListener(log);
        }
        assertEquals(LogLevel.ERROR, log.level(0));
        assertEquals("DevToolsTest", log.context(0));
        assertEquals("failed 3 times (java.lang.IllegalStateException: boom)", log.message(0));
    }

    @Test
    void numbersAreFormattedWithoutAllocating() {
        StringBuilder sb = new StringBuilder();
        assertEquals("3.14", DevUi.fixed(sb, Math.PI, 2).toString());
        sb.setLength(0);
        assertEquals("-0.5", DevUi.fixed(sb, -0.5, 1).toString());
        sb.setLength(0);
        assertEquals("0.000", DevUi.fixed(sb, -0.0001, 3).toString());
        sb.setLength(0);
        assertEquals("1.5 KB", DevUi.bytes(sb, 1536).toString());
        sb.setLength(0);
        assertEquals("2.0 MB", DevUi.bytes(sb, 2 * 1024 * 1024).toString());
        assertTrue(Arrays.stream(DevTools.Tab.values()).count() == 5);
    }
}
