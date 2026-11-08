package org.pixel.demo.learning.content;

import org.pixel.commons.Color;
import org.pixel.commons.DeltaTime;
import org.pixel.content.ContentManager;
import org.pixel.content.Texture;
import org.pixel.core.DesktopLauncher;
import org.pixel.core.WindowSettings;
import org.pixel.demo.learning.common.DemoGame;
import org.pixel.demo.learning.common.Screenshot;
import org.pixel.graphics.canvas.Canvas;
import org.pixel.graphics.canvas.CanvasBatch;
import org.pixel.graphics.text.SdfFont;
import org.pixel.input.keyboard.Keyboard;
import org.pixel.input.keyboard.KeyboardKey;
import org.pixel.math.Vector2;

import java.io.IOException;
import java.nio.file.Path;

/**
 * Asset lifetimes with {@link ContentManager#child(String)}: the HUD font is loaded once into the game's content
 * manager, and each level loads its textures into its own child. Switching levels (SPACE) disposes the previous
 * child, which frees exactly its textures; the font stays loaded. The panel shows what each manager holds.
 * <p>
 * Run with {@code -Dpixel.screenshot=build/content-levels.png} to switch once, save a PNG and exit.
 */
public class ContentLevelsDemo extends DemoGame {

    private static final String[][] LEVEL_TEXTURES = {
            {"images/earth-48x48.png", "images/blue-32x32.png"},
            {"images/red-32x32.png", "images/green-32x32.png", "images/earth-48x48.png"},
    };
    private static final Color PANEL = new Color(0.12f, 0.14f, 0.20f, 0.95f);
    private static final Color MUTED = new Color(0.65f, 0.70f, 0.78f, 1f);

    private final String screenshot = System.getProperty("pixel.screenshot");
    private CanvasBatch canvasBatch;
    private SdfFont font;
    private ContentManager level;
    private int levelIndex = -1;
    private Texture[] textures;
    private boolean switched;
    private float time;
    private int frames;

    @Override
    public void load() {
        super.load();
        gameCamera.setOrigin(Vector2.zero());
        canvasBatch = new CanvasBatch(graphics().getRenderBackend());
        font = content().load("fonts/gidole-regular.ttf", SdfFont.class); // shared: lives as long as the game
        loadLevel(0);
    }

    private void loadLevel(int index) {
        if (level != null) {
            level.dispose(); // frees this level's textures, nothing else
            switched = true;
        }
        levelIndex = index;
        level = content().child("level-" + (index + 1));
        String[] files = LEVEL_TEXTURES[index];
        textures = new Texture[files.length];
        for (int i = 0; i < files.length; i++) {
            textures[i] = level.loadTexture(files[i]);
        }
        log.info("Loaded {0}; game content: {1}", level, content());
    }

    @Override
    public void update(DeltaTime delta) {
        super.update(delta);
        time += delta.getElapsed();
        if (Keyboard.isKeyPressed(KeyboardKey.SPACE) || (screenshot != null && frames == 2)) {
            loadLevel((levelIndex + 1) % LEVEL_TEXTURES.length);
        }
    }

    @Override
    public void draw(DeltaTime delta) {
        Canvas c = canvasBatch.begin(gameCamera.getViewMatrix());

        // the level: its textures bouncing in a row
        for (int i = 0; i < textures.length; i++) {
            float x = 120 + i * 160, y = 260 + (float) Math.sin(time * 2 + i) * 30;
            c.image(textures[i], x, y).size(96, 96).anchor(0.5f, 0.5f);
        }

        // what each manager holds
        c.roundRect(560, 40, 380, 230, 12).fill(PANEL);
        c.text(font, "Level " + (levelIndex + 1), 580, 56).size(28).fill(Color.WHITE);
        c.text(font, "SPACE: next level (disposes this one)", 580, 96).size(15).fill(MUTED);
        c.text(font, content().toString(), 580, 140).size(15).fill(Color.WHITE);
        c.text(font, level.toString(), 580, 166).size(15).fill(Color.WHITE);
        if (switched) {
            c.text(font, "Previous level's textures disposed with its child.", 580, 206).size(15).fill(MUTED)
                    .maxWidth(340);
        }
        canvasBatch.end();

        if (screenshot != null && ++frames == 5) {
            try {
                Screenshot.capture(context(), Path.of(screenshot));
                log.info("Screenshot written to {0}", Path.of(screenshot).toAbsolutePath());
            } catch (IOException e) {
                log.error("Unable to write screenshot.", e);
            }
            exit();
        }
    }

    @Override
    public void dispose() {
        canvasBatch.dispose();
        // the level child is disposed with the game's content manager; disposing it here is fine too
        super.dispose();
    }

    public static void main(String[] args) {
        var settings = new WindowSettings("Pixel - Content children", 960, 400);
        settings.setWindowResizable(false);
        settings.setVsync(true);
        settings.setBackgroundColor(new Color(0.08f, 0.09f, 0.12f, 1f));
        DesktopLauncher.run(new ContentLevelsDemo(), settings);
    }
}
