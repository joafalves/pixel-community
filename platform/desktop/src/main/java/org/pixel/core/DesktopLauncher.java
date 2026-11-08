package org.pixel.core;

/**
 * Runs a {@link GameApp} on the desktop (GLFW window, OpenGL, OpenAL).
 *
 * <pre>
 * public static void main(String[] args) {
 *     DesktopLauncher.run(new MyGame(), new WindowSettings("My Game", 1280, 720));
 * }
 * </pre>
 */
public final class DesktopLauncher {

    private DesktopLauncher() {
    }

    /**
     * Create the window, run the game until it exits, then release everything. Blocks the calling thread (on macOS
     * it must be the main thread, see {@code -XstartOnFirstThread}).
     *
     * @param app      The game.
     * @param settings The window and game settings.
     */
    public static void run(GameApp app, WindowSettings settings) {
        // LWJGL reads this once, when it loads: argument checks only while debugging GL
        org.lwjgl.system.Configuration.DISABLE_CHECKS.set(!settings.isGlfwDebugMode());
        new DesktopGameHost(app, settings).start();
    }
}
