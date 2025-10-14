package org.pixel.demo.learning.urp;

import org.pixel.commons.Color;
import org.pixel.commons.DeltaTime;
import org.pixel.commons.service.ServiceProvider;
import org.pixel.content.importer.settings.FontImporterSettings;
import org.pixel.content.ContentManager;
import org.pixel.content.Font;
import org.pixel.content.Texture;
import org.pixel.core.Camera2D;
import org.pixel.core.WindowSettings;
import org.pixel.demo.learning.common.DemoGame;
import org.pixel.graphics.render.RenderPipeline;
import org.pixel.graphics.render.SpriteRenderable;
import org.pixel.graphics.render.TextRenderable;
import org.pixel.math.Vector2;

/**
 * Demo showing mixed sprite and text rendering in the unified render pipeline.
 */
public class UrpTextDemo extends DemoGame {

    private ContentManager content;
    private RenderPipeline renderPipeline;
    private Camera2D camera;

    private SpriteRenderable backgroundSprite;
    private TextRenderable titleText;
    private TextRenderable fpsText;
    private TextRenderable instructionsText;

    public UrpTextDemo(WindowSettings settings) {
        super(settings);
    }

    @Override
    public void load() {
        super.load();
        content = ServiceProvider.get(ContentManager.class);
        camera = new Camera2D(this);
        camera.setOrigin(0);

        // Create the render pipeline
        renderPipeline = new RenderPipeline();

        // Load content
        Texture earthTexture = content.loadTexture("images/earth-48x48.png");
        Font defaultFont = content.loadFont("fonts/gidole-regular.ttf", new FontImporterSettings(16, 1));

        // Create a background sprite
        backgroundSprite = new SpriteRenderable()
                .setTexture(earthTexture)
                .setPosition(getVirtualWidth() / 2f, getVirtualHeight() / 2f)
                .setAnchor(Vector2.HALF)
                .setScale(3)
                .setDepth(10); // Behind text

        // Create title text
        titleText = new TextRenderable()
                .setFont(defaultFont)
                .setFontSize(defaultFont.getFontSize())
                .setText("Unified Render Pipeline - Text Demo")
                .setPosition(20, 30)
                .setColor(Color.WHITE)
                .setDepth(0); // In front

        // Create FPS counter text
        fpsText = new TextRenderable()
                .setFont(defaultFont)
                .setFontSize(defaultFont.getFontSize())
                .setText("FPS: 0")
                .setPosition(20, 70)
                .setColor(Color.LIME)
                .setFontSize(18)
                .setDepth(0);

        // Create instructions
        instructionsText = new TextRenderable()
                .setFont(defaultFont)
                .setFontSize(defaultFont.getFontSize())
                .setText("Sprites and Text rendered together!\nDepth sorting works for both types.")
                .setPosition(20, getVirtualHeight() - 60)
                .setColor(new Color(0, 1f, 1f, 1f)) // Cyan
                .setDepth(0);

        // Submit all renderables once
        renderPipeline.submit(backgroundSprite);
        renderPipeline.submit(titleText);
        renderPipeline.submit(fpsText);
        renderPipeline.submit(instructionsText);
    }

    @Override
    public void update(DeltaTime delta) {
        // Animate the background sprite
        backgroundSprite.setRotation(delta.getTotalElapsed() * 0.5f);

        // Update FPS counter
        fpsText.setText("FPS: " + getSmoothedFps());

        super.update(delta);
    }

    @Override
    public void draw(DeltaTime delta) {
        // Render everything with the pipeline (no clearing - persistent queue!)
        renderPipeline.render(camera.getViewMatrix());
    }

    @Override
    public void dispose() {
        super.dispose();
        renderPipeline.dispose();
        content.dispose();
    }

    public static void main(String[] args) {
        var settings = new WindowSettings(1280, 720);
        settings.setMultisampling(4);
        settings.setVsync(false);
        settings.setTitle("Unified Render Pipeline - Text Demo");

        var game = new UrpTextDemo(settings);
        game.start();
    }
}
