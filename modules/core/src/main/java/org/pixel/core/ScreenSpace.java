package org.pixel.core;

import org.pixel.graphics.pass.View;
import org.pixel.math.Matrix4;

/**
 * A view in the game's virtual (screen) units, origin at the top-left: for HUDs and UI that don't move with the
 * camera. Follows changes of the virtual resolution.
 */
public final class ScreenSpace implements View {

    private final GameContext context;
    private final Matrix4 matrix = new Matrix4();
    private float width = -1;
    private float height = -1;

    /**
     * @param context The game context (virtual resolution and display density).
     */
    public ScreenSpace(GameContext context) {
        if (context == null) {
            throw new IllegalArgumentException("Game context cannot be null.");
        }
        this.context = context;
    }

    @Override
    public Matrix4 getViewMatrix() {
        float w = context.settings().getVirtualWidth(), h = context.settings().getVirtualHeight();
        if (w != width || h != height) {
            width = w;
            height = h;
            matrix.setOrthographic(0, w, h, 0, 0f, 1f);
        }
        return matrix;
    }

    @Override
    public float getPixelsPerUnit() {
        GameWindow window = context.window();
        float w = context.settings().getVirtualWidth(), h = context.settings().getVirtualHeight();
        return w > 0 && h > 0 ? Math.min(window.getWidth() / w, window.getHeight() / h) : 1f;
    }
}
