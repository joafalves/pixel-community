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
import org.pixel.graphics.render.renderable.SpriteRenderable;
import org.pixel.graphics.render.renderable.SdfTextRenderable;
import org.pixel.graphics.render.renderable.TextRenderable;
import org.pixel.graphics.render.canvas.TextStyle;
import org.pixel.graphics.render.canvas.text.SdfFont;
import org.pixel.math.Vector2;

/**
 * Demo comparing legacy Font vs SDF Font rendering in the unified render pipeline.
 */
public class UrpTextDemo extends DemoGame {

    private ContentManager content;
    private RenderPipeline renderPipeline;
    private Camera2D camera;

    private SpriteRenderable backgroundSprite;
    private SdfTextRenderable titleText;
    
    // Comparison texts - Legacy vs SDF
    private TextRenderable legacyLabel;
    private TextRenderable legacyText;
    private SdfTextRenderable sdfLabel;
    private SdfTextRenderable sdfText;
    
    private SdfTextRenderable fpsText;
    private SdfTextRenderable instructionsText;

    public UrpTextDemo(WindowSettings settings) {
        super(settings);
    }

    @Override
    public void load() {
        super.load();
        content = ContentManager.create();
        camera = new Camera2D(this);
        camera.setOrigin(0);

        // Create the render pipeline
        renderPipeline = new RenderPipeline();

        // Load content
        Texture earthTexture = content.loadTexture("images/earth-48x48.png");
        
        // Load both legacy and SDF fonts at same size for comparison
        Font legacyFont = content.loadFont("fonts/roboto-regular.ttf", new FontImporterSettings(24, 1));
        SdfFont sdfFont = content.load("fonts/roboto-regular.ttf", SdfFont.class, 
            new FontImporterSettings(22, 1));
        SdfFont smallSdfFont = content.load("fonts/roboto-regular.ttf", SdfFont.class, 
            new FontImporterSettings(14, 1));

        // Create a background sprite
        backgroundSprite = new SpriteRenderable()
                .setTexture(earthTexture)
                .setPosition(getViewportWidth() / 2f, getViewportHeight() / 2f)
                .setAnchor(Vector2.HALF)
                .setScale(3)
                .setDepth(10); // Behind text

        // Create title
        titleText = new SdfTextRenderable()
                .setFont(sdfFont)
                .setText("Font Comparison: Legacy vs SDF")
                .setPosition(20, 30)
                .setStyle(new TextStyle(Color.WHITE))
                .setDepth(0);

        // LEGACY FONT DEMO
        float comparisonY = 120;
        legacyLabel = new TextRenderable()
                .setFont(legacyFont)
                .setFontSize(legacyFont.getFontSize())
                .setText("LEGACY FONT (Bitmap):")
                .setPosition(20, comparisonY)
                .setTint(new Color(1f, 0.8f, 0.2f)) // Orange
                .setDepth(0);

        legacyText = new TextRenderable()
                .setFont(legacyFont)
                .setFontSize(legacyFont.getFontSize())
                .setText("The quick brown fox jumps over the lazy dog 0123456789")
                .setPosition(20, comparisonY + 35)
                .setTint(Color.WHITE)
                .setDepth(0);

        // SDF FONT DEMO
        float sdfY = comparisonY + 100;
        sdfLabel = new SdfTextRenderable()
                .setFont(sdfFont)
                .setText("SDF FONT (Scalable):")
                .setPosition(20, sdfY)
                .setStyle(new TextStyle(new Color(0.2f, 1f, 0.8f))) // Cyan-green
                .setDepth(0);

        sdfText = new SdfTextRenderable()
                .setFont(sdfFont)
                .setText("The quick brown fox jumps over the lazy dog 0123456789")
                .setPosition(20, sdfY + 35)
                .setStyle(new TextStyle(Color.WHITE))
                .setDepth(0);

        // FPS counter
        fpsText = new SdfTextRenderable()
                .setFont(smallSdfFont)
                .setText("FPS: 0")
                .setPosition(20, 70)
                .setStyle(new TextStyle(Color.LIME))
                .setDepth(0);

        // Instructions
        instructionsText = new SdfTextRenderable()
                .setFont(smallSdfFont)
                .setText("Compare the text quality above!\n" +
                        "Legacy fonts are bitmap-based (fixed size)\n" +
                        "SDF fonts are distance field-based (scale perfectly)\n" +
                        "Both render together in the Unified Render Pipeline!")
                .setPosition(20, getViewportHeight() - 110)
                .setStyle(new TextStyle(new Color(0.9f, 0.9f, 0.9f))) // Gray
                .setDepth(0);

        // Submit all renderables
        renderPipeline.submit(backgroundSprite);
        renderPipeline.submit(titleText);
        renderPipeline.submit(legacyLabel);
        renderPipeline.submit(legacyText);
        renderPipeline.submit(sdfLabel);
        renderPipeline.submit(sdfText);
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
        settings.setTitle("URP Text Demo - Legacy vs SDF Font Comparison");

        var game = new UrpTextDemo(settings);
        game.start();
    }
}
