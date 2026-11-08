package org.pixel.demo.concept.spaceshooter;

import org.pixel.commons.DeltaTime;
import org.pixel.core.DesktopLauncher;
import org.pixel.core.GameApp;
import org.pixel.core.WindowSettings;
import org.pixel.demo.concept.spaceshooter.assets.Assets;
import org.pixel.demo.concept.spaceshooter.debug.Capture;
import org.pixel.demo.concept.spaceshooter.debug.DevPanel;
import org.pixel.demo.concept.spaceshooter.debug.ShaderHotReload;
import org.pixel.demo.concept.spaceshooter.input.Controls;
import org.pixel.demo.concept.spaceshooter.input.KeyboardControls;
import org.pixel.demo.concept.spaceshooter.screen.PlayScreen;
import org.pixel.demo.concept.spaceshooter.screen.Screen;
import org.pixel.demo.concept.spaceshooter.screen.TitleScreen;

/**
 * Space Shooter: a horizontal shoot 'em up (see the package description for its layout).
 * <p>
 * Run: {@code ./gradlew :pixel-demo-desktop-concept:runSpaceShooter}.
 */
public class SpaceShooterGame extends GameApp {

    private final Controls controls = new Controls();
    private Stage stage;
    private Screen screen;
    private DevPanel devPanel;
    private ShaderHotReload shaders;
    private Capture capture;

    @Override
    public void load() {
        stage = new Stage(context(), new Assets(content()), System.nanoTime());
        devPanel = new DevPanel(devTools(), stage);
        if (settings().isDevMode()) {
            shaders = ShaderHotReload.create(stage.assets, stage.fx.shockwave);
        }
        capture = Capture.fromSystemProperties();
        int firstWave = Capture.firstWave();
        show(Capture.autoplay() ? new PlayScreen(stage, firstWave).autopilot().lives(Capture.lives())
                : new TitleScreen(stage, firstWave));
    }

    @Override
    public void update(DeltaTime delta) {
        if (capture == null) { // capture runs ignore the keyboard (keys pressed elsewhere would interfere)
            KeyboardControls.poll(controls);
        }
        if (devTools().isPointerOverUi()) {
            controls.fire = false; // clicks on the dev tools aren't for the game
        }
        devPanel.apply();
        if (shaders != null) {
            shaders.poll();
        }
        Screen next = screen.update(delta.getElapsed(), controls);
        controls.consumePresses();
        if (next != screen) {
            show(next);
        }
    }

    @Override
    public void draw(DeltaTime delta) {
        screen.draw();
        stage.pipeline.render();
        if (capture != null && capture.afterDraw(context(), delta.getElapsed())) {
            exit();
        }
    }

    private void show(Screen next) {
        screen = next;
        screen.enter();
    }

    @Override
    public void dispose() {
        stage.dispose();
    }

    public static void main(String[] args) {
        WindowSettings settings = new WindowSettings("Space Shooter", GameConfig.WIDTH, GameConfig.HEIGHT);
        settings.setWindowWidth(GameConfig.WIDTH * GameConfig.WINDOW_SCALE);
        settings.setWindowHeight(GameConfig.HEIGHT * GameConfig.WINDOW_SCALE);
        settings.setWindowResizable(false); // pixel art: a fixed integer scale
        settings.setVsync(true);
        settings.setDevMode(true); // F1: the engine's dev tools (with this game's own page)
        DesktopLauncher.run(new SpaceShooterGame(), settings);
    }
}
