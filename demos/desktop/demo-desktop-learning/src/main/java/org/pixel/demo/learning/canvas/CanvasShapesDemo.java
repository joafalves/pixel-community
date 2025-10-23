/*
 * This software is available under Apache License
 * Copyright (c) 2020
 */

package org.pixel.demo.learning.canvas;

import org.pixel.commons.Color;
import org.pixel.commons.DeltaTime;
import org.pixel.content.ContentManager;
import org.pixel.content.importer.settings.FontImporterSettings;
import org.pixel.core.WindowSettings;
import org.pixel.demo.learning.common.DemoGame;
import org.pixel.graphics.render.canvas.Canvas;
import org.pixel.graphics.render.canvas.GLCanvas;
import org.pixel.graphics.render.canvas.text.SdfFont;

/**
 * Demo showcasing all Canvas shape rendering capabilities.
 * Tests filled and stroked shapes: rectangles, rounded rectangles, and circles.
 * Now using the fluent Canvas API!
 */
public class CanvasShapesDemo extends DemoGame {

    private Canvas canvas;
    private ContentManager content;
    private SdfFont font;
    private float time = 0;

    /**
     * Constructor.
     *
     * @param settings Window settings
     */
    public CanvasShapesDemo(WindowSettings settings) {
        super(settings);
    }

    @Override
    public void load() {
        super.load();

        // Create canvas with fluent API
        canvas = new GLCanvas(getViewportWidth(), getViewportHeight());

        // Initialize content manager
        content = ContentManager.create();

        // Load font
        font = content.load("fonts/roboto-regular.ttf", SdfFont.class,
                new FontImporterSettings(18, 3));

        if (font != null) {
            System.out.println("Font loaded successfully!");
        } else {
            System.err.println("Failed to load font!");
        }
    }

    @Override
    public void draw(DeltaTime delta) {
        super.draw(delta);

        if (font == null) {
            return;
        }

        time += delta.getElapsed();

        // Begin with camera's view matrix for world-space rendering
        canvas.begin();

        // Main title - using fluent API
        canvas.text("Canvas Shapes Showcase - Fluent API!", font, 20, 20)
                .withFill(Color.WHITE);

        // Section 1: Filled Shapes
        drawFilledShapes();

        // Section 2: Stroked Shapes
        drawStrokedShapes();

        // Section 3: Combined Shapes
        drawCombinedShapes();

        // Section 4: Animated Shapes
        drawAnimatedShapes();

        canvas.end();
    }

    /**
     * Section 1: Filled shapes (rectangles, rounded rectangles, circles).
     */
    private void drawFilledShapes() {
        // Header
        canvas.rect(20, 60, 370, 180)
                .withFill(new Color(0.15f, 0.15f, 0.2f, 0.9f))
                .withRoundedCorners(10)
                .apply();

        canvas.text("Filled Shapes", font, 30, 70)
                .withFill(new Color(0.9f, 0.9f, 1.0f, 1.0f))
                .apply();

        canvas.text("All rendered with 6 vertices (2 triangles)!", font, 30, 90)
                .withFill(new Color(0.6f, 0.6f, 0.7f, 1.0f))
                .apply();

        // Regular rectangle
        canvas.rect(40, 120, 60, 60)
                .withFill(new Color(0.8f, 0.3f, 0.3f, 1.0f))
                .apply();

        canvas.text("Rect", font, 45, 190)
                .withFill(new Color(0.9f, 0.9f, 0.9f, 1.0f))
                .apply();

        // Rounded rectangles with different radii
        canvas.rect(130, 120, 60, 60)
                .withFill(new Color(0.3f, 0.8f, 0.3f, 1.0f))
                .withRoundedCorners(5)
                .apply();

        canvas.text("R=5", font, 140, 190)
                .withFill(new Color(0.9f, 0.9f, 0.9f, 1.0f))
                .apply();

        canvas.rect(220, 120, 60, 60)
                .withFill(new Color(0.3f, 0.5f, 0.9f, 1.0f))
                .withRoundedCorners(15)
                .apply();

        canvas.text("R=15", font, 230, 190)
                .withFill(new Color(0.9f, 0.9f, 0.9f, 1.0f))
                .apply();

        // Circle
        canvas.circle(340, 150, 30)
                .withFill(new Color(0.9f, 0.6f, 0.2f, 1.0f))
                .apply();

        canvas.text("Circle", font, 315, 190)
                .withFill(new Color(0.9f, 0.9f, 0.9f, 1.0f))
                .apply();
    }

    /**
     * Section 2: Stroked shapes (outlines only).
     */
    private void drawStrokedShapes() {
        // Header
        canvas.rect(410, 60, 370, 180)
                .withFill(new Color(0.15f, 0.15f, 0.2f, 0.9f))
                .withRoundedCorners(10)
                .apply();

        canvas.text("Stroked Shapes", font, 420, 70)
                .withFill(new Color(0.9f, 0.9f, 1.0f, 1.0f))
                .apply();

        canvas.text("Different stroke widths, same efficiency!", font, 420, 90)
                .withFill(new Color(0.6f, 0.6f, 0.7f, 1.0f))
                .apply();

        // Rectangle with thin stroke
        canvas.rect(430, 120, 60, 60)
                .withStroke(2, new Color(0.8f, 0.3f, 0.3f, 1.0f))
                .apply();

        canvas.text("W=2", font, 440, 190)
                .withFill(new Color(0.9f, 0.9f, 0.9f, 1.0f))
                .apply();

        // Rounded rectangle with medium stroke
        canvas.rect(520, 120, 60, 60)
                .withStroke(4, new Color(0.3f, 0.8f, 0.3f, 1.0f))
                .withRoundedCorners(10)
                .apply();

        canvas.text("W=4", font, 530, 190)
                .withFill(new Color(0.9f, 0.9f, 0.9f, 1.0f))
                .apply();

        // Rounded rectangle with thick stroke
        canvas.rect(610, 120, 60, 60)
                .withStroke(6, new Color(0.3f, 0.5f, 0.9f, 1.0f))
                .withRoundedCorners(15)
                .apply();

        canvas.text("W=6", font, 620, 190)
                .withFill(new Color(0.9f, 0.9f, 0.9f, 1.0f))
                .apply();

        // Circle with stroke
        canvas.circle(730, 150, 30)
                .withStroke(3, new Color(0.9f, 0.6f, 0.2f, 1.0f))
                .apply();

        canvas.text("W=3", font, 710, 190)
                .withFill(new Color(0.9f, 0.9f, 0.9f, 1.0f))
                .apply();
    }

    /**
     * Section 3: Combined filled and stroked shapes.
     * Note: Combined fill+stroke requires two fluent calls since execute() happens after each withX() method.
     */
    private void drawCombinedShapes() {
        // Header
        canvas.rect(20, 260, 760, 160)
                .withFill(new Color(0.15f, 0.15f, 0.2f, 0.9f))
                .withRoundedCorners(10)
                .apply();

        canvas.text("Combined Fill + Stroke", font, 30, 270)
                .withFill(new Color(0.9f, 0.9f, 1.0f, 1.0f))
                .apply();

        canvas.text("Fluent API with layered operations", font, 30, 290)
                .withFill(new Color(0.6f, 0.6f, 0.7f, 1.0f))
                .apply();

        // Button-like element 1 - fill then stroke
        float button1Y = 320;
        float button1Height = 40;

        canvas.rect(50, button1Y, 100, button1Height)
                .withFill(new Color(0.2f, 0.4f, 0.8f, 1.0f))
                .withRoundedCorners(8)
                .apply();

        canvas.rect(50, button1Y, 100, button1Height)
                .withStroke(2, new Color(0.4f, 0.6f, 1.0f, 1.0f))
                .withRoundedCorners(8)
                .apply();

        String button1Text = "Button 1";
        canvas.text(button1Text, font,
                        canvas.centerTextHorizontally(50, 100, button1Text, font),
                        canvas.centerTextVertically(button1Y, button1Height, font))
                .withFill(Color.WHITE)
                .apply();

        // Button-like element 2
        canvas.rect(180, button1Y, 100, button1Height)
                .withFill(new Color(0.2f, 0.7f, 0.3f, 1.0f))
                .withRoundedCorners(8)
                .apply();

        canvas.rect(180, button1Y, 100, button1Height)
                .withStroke(2, new Color(0.4f, 1.0f, 0.5f, 1.0f))
                .withRoundedCorners(8)
                .apply();

        String button2Text = "Button 2";
        canvas.text(button2Text, font,
                        canvas.centerTextHorizontally(180, 100, button2Text, font),
                        canvas.centerTextVertically(button1Y, button1Height, font))
                .withFill(Color.WHITE)
                .apply();

        // Checkbox-like element
        float checkboxY = 325;
        float checkboxSize = 30;

        canvas.rect(320, checkboxY, checkboxSize, checkboxSize)
                .withFill(new Color(0.9f, 0.9f, 0.9f, 1.0f))
                .apply();

        canvas.rect(320, checkboxY, checkboxSize, checkboxSize)
                .withStroke(2, new Color(0.3f, 0.3f, 0.3f, 1.0f))
                .apply();

        canvas.text("Checkbox", font, 360, canvas.centerTextVertically(checkboxY, checkboxSize, font))
                .withFill(new Color(0.9f, 0.9f, 0.9f, 1.0f))
                .apply();

        // Radio button-like element
        float radioCenterY = 340;

        canvas.circle(490, radioCenterY, 12)
                .withFill(new Color(0.9f, 0.9f, 0.9f, 1.0f))
                .apply();

        canvas.circle(490, radioCenterY, 12)
                .withStroke(2, new Color(0.3f, 0.3f, 0.3f, 1.0f))
                .apply();

        canvas.circle(490, radioCenterY, 6)
                .withFill(new Color(0.2f, 0.4f, 0.8f, 1.0f))
                .apply();

        canvas.text("Radio", font, 510, radioCenterY - font.getFontSize() / 2)
                .withFill(new Color(0.9f, 0.9f, 0.9f, 1.0f))
                .apply();

        // Badge/notification
        float badgeCenterY = 340;

        canvas.circle(610, badgeCenterY, 15)
                .withFill(new Color(0.9f, 0.2f, 0.2f, 1.0f))
                .apply();

        canvas.circle(610, badgeCenterY, 15)
                .withStroke(2, new Color(1.0f, 0.4f, 0.4f, 1.0f))
                .apply();

        canvas.text("Badge", font, 640, badgeCenterY - font.getFontSize() / 2)
                .withFill(new Color(0.9f, 0.9f, 0.9f, 1.0f))
                .apply();
    }

    /**
     * Section 4: Animated shapes showing transform capabilities.
     */
    private void drawAnimatedShapes() {
        // Header
        canvas.rect(20, 440, 760, 140)
                .withFill(new Color(0.15f, 0.15f, 0.2f, 0.9f))
                .withRoundedCorners(10)
                .apply();

        canvas.text("Animated Shapes", font, 30, 450)
                .withFill(new Color(0.9f, 0.9f, 1.0f, 1.0f))
                .apply();

        canvas.text("Rotation, scaling, pulsing effects", font, 30, 470)
                .withFill(new Color(0.6f, 0.6f, 0.7f, 1.0f))
                .apply();

        // Rotating rectangle
        canvas.save();
        canvas.translate(100, 530);
        canvas.rotate(time * 2);

        canvas.rect(-20, -20, 40, 40)
                .withStroke(3, new Color(0.8f, 0.3f, 0.8f, 1.0f))
                .apply();

        canvas.restore();

        // Pulsing circle
        float pulse = 1.0f + 0.3f * (float) Math.sin(time * 4);
        canvas.save();
        canvas.translate(220, 530);
        canvas.scale(pulse);

        canvas.circle(0, 0, 20)
                .withFill(new Color(0.3f, 0.8f, 0.8f, 1.0f))
                .apply();

        canvas.restore();

        // Rotating rounded rectangle with trail effect
        for (int i = 0; i < 5; i++) {
            float alpha = 0.2f + i * 0.15f;
            float angle = time * 1.5f - i * 0.3f;

            canvas.save();
            canvas.translate(360, 530);
            canvas.rotate(angle);

            canvas.rect(-15, -15, 30, 30)
                    .withFill(new Color(0.9f, 0.6f, 0.2f, alpha))
                    .withRoundedCorners(8)
                    .apply();

            canvas.restore();
        }

        // Orbiting circles
        canvas.save();
        canvas.translate(500, 520);

        for (int i = 0; i < 6; i++) {
            float orbitAngle = time + i * (float) (Math.PI * 2 / 6);
            float orbitRadius = 40;
            float cx = (float) Math.cos(orbitAngle) * orbitRadius;
            float cy = (float) Math.sin(orbitAngle) * orbitRadius;

            Color orbitColor = new Color(
                    0.5f + 0.5f * (float) Math.sin(orbitAngle),
                    0.5f + 0.5f * (float) Math.cos(orbitAngle),
                    0.8f,
                    1.0f
            );

            canvas.circle(cx, cy, 6.0f + 2f * (float) Math.sin(time * i))
                    .withFill(orbitColor)
                    .apply();
        }
        canvas.restore();

        // Breathing rounded rectangle with fill + stroke
        float breathe = 1.0f + 0.2f * (float) Math.sin(time * 3);
        canvas.save();
        canvas.translate(650, 530);
        canvas.scale(breathe, breathe);

        canvas.rect(-25, -25, 50, 50)
                .withFill(new Color(0.4f, 0.3f, 0.9f, 1.0f))
                .withRoundedCorners(12)
                .apply();

        canvas.rect(-25, -25, 50, 50)
                .withStroke(2, new Color(0.7f, 0.6f, 1.0f, 1.0f))
                .withRoundedCorners(12)
                .apply();

        canvas.restore();
    }

    @Override
    public void dispose() {
        super.dispose();
        if (canvas != null) {
            canvas.dispose();
        }
        if (content != null) {
            content.dispose();
        }
    }

    /**
     * Main entry point.
     */
    public static void main(String[] args) {
        var settings = new WindowSettings(800, 600);
        settings.setTitle("Canvas Shapes Demo - SDF Rendering");
        settings.setWindowResizable(true);

        CanvasShapesDemo demo = new CanvasShapesDemo(settings);
        demo.start();
    }
}
