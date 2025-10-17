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
import org.pixel.graphics.render.canvas.Canvas;
import org.pixel.graphics.render.canvas.GLCanvas;
import org.pixel.graphics.render.canvas.TextStyle;
import org.pixel.graphics.render.canvas.text.SdfFont;

/**
 * Canvas Renderer Text Demo.
 * Demonstrates SDF text rendering with various styles, spacing, and effects.
 */
public class CanvasTextDemo extends DemoGame {

    private ContentManager content;
    private Camera2D camera;
    private Canvas canvas;
    private SdfFont font;
    private SdfFont smallFont;
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

        // Initialize canvas with viewport dimensions
        canvas = new GLCanvas(getViewportWidth(), getViewportHeight());

        // Initialize content manager
        content = ServiceProvider.get(ContentManager.class);

        // Load fonts using the content pipeline!
        smallFont = content.load("fonts/roboto-regular.ttf", SdfFont.class,
            new FontImporterSettings(16, 2));  // 16pt font
        font = content.load("fonts/gidole-regular.ttf", SdfFont.class,
            new FontImporterSettings(24, 3));  // 24pt font
        titleFont = content.load("fonts/roboto-medium.ttf", SdfFont.class,
            new FontImporterSettings(36, 3));  // 36pt font
        
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

        // Draw background for better visibility
        canvas.rect(0, 0, getViewportWidth(), getViewportHeight())
            .withFill(new Color(0.1f, 0.1f, 0.15f, 1.0f))
            .apply();

        float x = 30;
        float y = 20;
        
        // === TITLE ===
        TextStyle titleStyle = new TextStyle(Color.WHITE)
            .withStroke(new Color(0.2f, 0.4f, 0.8f, 1.0f), 3.0f);
        canvas.text("Canvas Text Renderer - Comprehensive Demo", titleFont, x, y)
            .withStyle(titleStyle)
            .apply();
        y += 50;

        // === SECTION 1: BASIC STYLES ===
        drawSection("1. Basic Text Styles", x, y);
        y += 35;
        
        canvas.text("Default white text", font, x + 20, y)
            .withFill(Color.WHITE)
            .apply();
        y += 30;
        
        canvas.text("Colored text (cyan)", font, x + 20, y)
            .withFill(new Color(0, 1, 1, 1))
            .apply();
        y += 30;
        
        TextStyle outlineStyle = new TextStyle(new Color(1, 1, 0, 1))
            .withStroke(Color.RED, 2.5f);
        canvas.text("Text with outline", font, x + 20, y)
            .withStyle(outlineStyle)
            .apply();
        y += 30;
        
        TextStyle shadowStyle = new TextStyle(Color.WHITE)
            .withShadow(3, 3, new Color(0, 0, 0, 0.8f));
        canvas.text("Text with drop shadow", font, x + 20, y)
            .withStyle(shadowStyle)
            .apply();
        y += 40;

        // === SECTION 2: LETTER SPACING ===
        drawSection("2. Letter Spacing Examples", x, y);
        y += 35;
        
        canvas.text("Normal spacing", font, x + 20, y)
            .withFill(Color.WHITE)
            .apply();
        y += 30;
        
        TextStyle spacing1 = new TextStyle(new Color(0.5f, 1, 0.5f, 1))
            .withLetterSpacing(3);
        canvas.text("Letter Spacing: 3px", font, x + 20, y)
            .withStyle(spacing1)
            .apply();
        y += 30;
        
        TextStyle spacing2 = new TextStyle(new Color(1, 0.8f, 0.3f, 1))
            .withLetterSpacing(8);
        canvas.text("Letter Spacing: 8px", font, x + 20, y)
            .withStyle(spacing2)
            .apply();
        y += 30;
        
        TextStyle spacing3 = new TextStyle(new Color(1, 0.5f, 0.8f, 1))
            .withLetterSpacing(15);
        canvas.text("W I D E   S P A C I N G", font, x + 20, y)
            .withStyle(spacing3)
            .apply();
        y += 40;

        // === SECTION 3: LINE SPACING (MULTI-LINE) ===
        drawSection("3. Line Spacing (Multi-line Text)", x, y);
        y += 35;
        
        String multiLineText = "First line\nSecond line\nThird line";
        TextStyle normalLineSpacing = new TextStyle(Color.WHITE);
        canvas.text(multiLineText, smallFont, x + 20, y)
            .withStyle(normalLineSpacing)
            .apply();
        y += 80;
        
        TextStyle tightLineSpacing = new TextStyle(new Color(1, 0.8f, 0.5f, 1))
            .withLineSpacing(-4);
        canvas.text("Tight spacing (lineSpacing: -4)\nLines closer together\nCompact text", 
            smallFont, x + 20, y)
            .withStyle(tightLineSpacing)
            .apply();
        y += 60;
        
        TextStyle wideLineSpacing = new TextStyle(new Color(0.5f, 0.8f, 1, 1))
            .withLineSpacing(10);
        canvas.text("Wide spacing (lineSpacing: 10)\nLines farther apart\nMore breathing room", 
            smallFont, x + 20, y)
            .withStyle(wideLineSpacing)
            .apply();
        y += 90;

        // === SECTION 4: COMBINED EFFECTS ===
        drawSection("4. Combined Effects", x, y);
        y += 35;
        
        TextStyle fancy1 = new TextStyle(new Color(1, 0.3f, 0.3f, 1))
            .withStroke(new Color(0.5f, 0, 0, 1), 2.0f)
            .withShadow(2, 2, new Color(0, 0, 0, 0.6f))
            .withLetterSpacing(4);
        canvas.text("Outline + Shadow + Spacing", font, x + 20, y)
            .withStyle(fancy1)
            .apply();
        y += 35;
        
        TextStyle fancy2 = new TextStyle(new Color(0.3f, 1, 0.3f, 1))
            .withStroke(Color.BLACK, 3.0f)
            .withLetterSpacing(2);
        canvas.text("Multi-line with effects\nSecond line here", smallFont, x + 20, y)
            .withStyle(fancy2)
            .apply();
        y += 60;

        // === SECTION 5: TRANSFORMATIONS ===
        drawSection("5. Transformations", x, y);
        y += 35;
        
        // Rotated text
        canvas.save();
        canvas.translate(x + 150, y + 10);
        canvas.rotate((float) Math.toRadians(15));
        TextStyle rotatedStyle = new TextStyle(new Color(1, 0, 1, 1))
            .withStroke(Color.BLACK, 1.5f);
        canvas.text("Rotated 15°", font, 0, 0)
            .withStyle(rotatedStyle)
            .apply();
        canvas.restore();
        
        // Scaled text
        canvas.save();
        canvas.translate(x + 350, y + 20);
        canvas.scale(1.5f);
        TextStyle scaledStyle = new TextStyle(new Color(0, 1, 1, 1));
        canvas.text("Scaled 1.5x", font, 0, 0)
            .withStyle(scaledStyle)
            .apply();
        canvas.restore();
        
        y += 80;

        // === SECTION 6: DIFFERENT SIZES ===
        drawSection("6. Font Size Comparison", x, y);
        y += 35;
        
        canvas.text("Small font (16pt)", smallFont, x + 20, y)
            .withFill(Color.WHITE)
            .apply();
        y += 25;
        canvas.text("Medium font (24pt)", font, x + 20, y)
            .withFill(new Color(0.8f, 0.8f, 1, 1))
            .apply();
        y += 35;
        canvas.text("Large font (36pt)", titleFont, x + 20, y)
            .withFill(new Color(1, 0.8f, 0.5f, 1))
            .apply();

        canvas.end();
    }
    
    /**
     * Helper method to draw section headers
     */
    private void drawSection(String title, float x, float y) {
        // Background bar
        canvas.rect(x, y, 940, 28)
            .withRoundedCorners(4)
            .withFill(new Color(0.2f, 0.3f, 0.5f, 0.6f))
            .apply();
        
        // Section title
        TextStyle sectionStyle = new TextStyle(new Color(1, 1, 0.7f, 1))
            .withStroke(new Color(0.3f, 0.3f, 0.3f, 1), 1.5f);
        canvas.text(title, font, x + 10, y + 2)
            .withStyle(sectionStyle)
            .apply();
    }

    @Override
    public void dispose() {
        super.dispose();
        if (canvas != null) {
            canvas.dispose();
        }
    }

    public static void main(String[] args) {
        var settings = new WindowSettings(1000, 900);
        settings.setTitle("Canvas Text Demo - Comprehensive Examples");
        settings.setWindowResizable(false);
        settings.setMultisampling(4);
        settings.setBackgroundColor(new Color(0.05f, 0.05f, 0.1f, 1.0f));
        CanvasTextDemo game = new CanvasTextDemo(settings);
        game.start();
    }
}
