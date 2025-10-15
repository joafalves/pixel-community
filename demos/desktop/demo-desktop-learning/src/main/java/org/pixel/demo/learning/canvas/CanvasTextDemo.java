/*
 * This software is available under Apache License
 * Copyright (c) 2020
 */

package org.pixel.demo.learning.canvas;

import org.pixel.commons.Color;
import org.pixel.commons.service.ServiceProvider;
import org.pixel.content.ContentManager;
import org.pixel.content.importer.settings.FontImporterSettings;
import org.pixel.core.Camera2D;
import org.pixel.core.WindowSettings;
import org.pixel.demo.learning.common.DemoGame;
import org.pixel.graphics.render.canvas.GlCanvasRenderer;
import org.pixel.graphics.render.canvas.TextStyle;
import org.pixel.graphics.render.canvas.text.SdfFont;

/**
 * Canvas Renderer Text Demo.
 * Demonstrates SDF text rendering with various styles.
 */
public class CanvasTextDemo extends DemoGame {

    private ContentManager content;
    private Camera2D camera;
    private GlCanvasRenderer canvas;
    private SdfFont font;
    private SdfFont titleFont;

    /**
     * Constructor.
     *
     * @param settings Window settings
     */
    public CanvasTextDemo(WindowSettings settings) {
        super(settings);
    }

    @Override
    public void load() {
        super.load();
        
        // Initialize camera
        camera = new Camera2D(this);

        // Initialize canvas renderer with viewport dimensions
        canvas = new GlCanvasRenderer(getVirtualWidth(), getVirtualHeight());

        // Initialize content manager
        content = ServiceProvider.get(ContentManager.class);

        // Load fonts using the content pipeline!
        // Oversampling generates the font at higher resolution for better SDF quality
        font = content.load("fonts/gidole-regular.ttf", SdfFont.class,
            new FontImporterSettings(24, 2));  // 24pt font with 2x oversampling (48pt atlas)
        titleFont = content.load("fonts/roboto-medium.ttf", SdfFont.class,
            new FontImporterSettings(64, 2));  // 64pt font with 2x oversampling (128pt atlas)
        
        if (font != null) {
            System.out.println("Fonts loaded successfully!");
            System.out.println("Font atlas size: " + font.getAtlasWidth() + "x" + font.getAtlasHeight());
        } else {
            System.err.println("Failed to load fonts!");
        }
    }

    @Override
    public void draw(org.pixel.commons.DeltaTime delta) {
        super.draw(delta);
        
        if (font == null) {
            return; // Font loading failed
        }

        // Begin with camera's view matrix
        canvas.begin(camera.getViewMatrix());

        // Test fillRect - background rectangles for text sections
        canvas.fillRect(40, 40, 900, 70, new Color(0.2f, 0.2f, 0.3f, 0.8f));
        canvas.fillRect(40, 152, 400, 40, new Color(0.3f, 0.2f, 0.1f, 0.5f));

        // Title with stroke
        TextStyle titleStyle = new TextStyle(Color.WHITE)
            .withStroke(Color.BLACK, 6.0f);
        canvas.drawText("Canvas SDF Text Renderer", titleFont, 50, 50, titleStyle);

        // Basic white text
        canvas.drawText("This is a simple text with white color.", font, 50, 120, Color.WHITE);

        // Colored text with predefined colors
        canvas.drawText("Colored text", font, 50, 160, new Color(0, 1, 1, 1)); // Cyan

        // Text with stroke/outline
        TextStyle outlineStyle = new TextStyle(new Color(1, 1, 0, 1)) // Yellow
            .withStroke(Color.RED, 2f);
        canvas.drawText("Text with outline", font, 50, 200, outlineStyle);

        // Text with drop shadow
        TextStyle shadowStyle = new TextStyle(Color.WHITE)
            .withShadow(2, 2, Color.BLACK);
        canvas.drawText("Text with shadow", font, 50, 240, shadowStyle);

        // Text with letter spacing
        TextStyle spacedStyle = new TextStyle(Color.GREEN)
            .withLetterSpacing(12);
        canvas.drawText("Letter Spacing", font, 50, 280, spacedStyle);

        // Combined effects
        TextStyle fancyStyle = new TextStyle(new Color(1, 0.5f, 0, 1)) // Orange
            .withStroke(new Color(0.3f, 0.3f, 0.3f, 1), 2.0f)
            .withShadow(2, 2, new Color(0, 0, 0, 0.5f))
            .withLetterSpacing(2);
        canvas.drawText("Fancy Combined Style\nmulti-line!", font, 50, 320, fancyStyle);

        // Transform demo - rotated text
        canvas.save();
        canvas.translate(400, 400);
        canvas.rotate((float) Math.toRadians(45));
        canvas.drawText("Rotated!", font, 0, 0, new TextStyle(new Color(1, 0, 1, 1))); // Magenta
        canvas.restore();

        // Large text demo - use titleFont instead of scaling
        canvas.save();
        canvas.translate(50, 400);
        TextStyle largeStyle = new TextStyle(new Color(0.5f, 1, 0, 1));
        canvas.drawText("Large Text", titleFont, 0, 0, largeStyle);
        canvas.restore();

        canvas.end();
    }

    @Override
    public void dispose() {
        super.dispose();
        if (canvas != null) {
            canvas.dispose();
        }
    }

    public static void main(String[] args) {
        var settings = new WindowSettings(1024, 800);
        settings.setTitle("Canvas Text Demo");
        settings.setWindowResizable(false);
        settings.setMultisampling(4);
        CanvasTextDemo game = new CanvasTextDemo(settings);
        game.start();
    }
}
