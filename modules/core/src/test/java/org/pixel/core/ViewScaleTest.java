package org.pixel.core;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Pixels per unit of {@link Camera2D} and {@link ScreenSpace}: what canvases use to size anti-aliased edges.
 */
class ViewScaleTest {

    private static final float EPS = 1e-5f;

    /** A 1280 x 720 game in a 2560 x 1440 framebuffer (e.g. Retina). */
    private static GameContext context(int framebufferWidth, int framebufferHeight) {
        GameContext context = mock(GameContext.class);
        GameWindow window = mock(GameWindow.class);
        when(window.getWidth()).thenReturn(framebufferWidth);
        when(window.getHeight()).thenReturn(framebufferHeight);
        when(context.window()).thenReturn(window);
        when(context.settings()).thenReturn(new GameSettings("test", 1280, 720));
        return context;
    }

    @Test
    void cameraPixelsPerUnitFollowFramebufferZoomAndCameraSize() {
        Camera2D camera = new Camera2D(context(2560, 1440));
        assertEquals(2f, camera.getPixelsPerUnit(), EPS);
        camera.setZoom(0.25f); // zoomed out: 4x more world on screen
        assertEquals(0.5f, camera.getPixelsPerUnit(), EPS);
        camera.setZoom(1f);
        camera.setSize(64, 36); // a camera measured in metres
        assertEquals(40f, camera.getPixelsPerUnit(), EPS);
    }

    @Test
    void stretchedWindowsUseTheTighterAxis() {
        Camera2D camera = new Camera2D(context(2560, 720)); // twice as wide, same height
        assertEquals(1f, camera.getPixelsPerUnit(), EPS);
        assertEquals(1f, new ScreenSpace(context(2560, 720)).getPixelsPerUnit(), EPS);
        assertEquals(2f, new ScreenSpace(context(2560, 1440)).getPixelsPerUnit(), EPS);
    }

    @Test
    void camerasWithoutAContextAssumeOnePixelPerUnit() {
        Camera2D camera = new Camera2D(800, 600);
        assertEquals(1f, camera.getPixelsPerUnit(), EPS);
        camera.setZoom(3f);
        assertEquals(3f, camera.getPixelsPerUnit(), EPS);
    }
}
