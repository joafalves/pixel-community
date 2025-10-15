/*
 * This software is available under Apache License
 * Copyright (c) 2020
 */

package org.pixel.demo.learning.canvas;

import org.pixel.commons.Color;
import org.pixel.commons.DeltaTime;
import org.pixel.commons.service.ServiceProvider;
import org.pixel.content.ContentManager;
import org.pixel.content.importer.settings.FontImporterSettings;
import org.pixel.core.Camera2D;
import org.pixel.core.WindowSettings;
import org.pixel.demo.learning.common.DemoGame;
import org.pixel.graphics.render.canvas.GlCanvasRenderer;
import org.pixel.graphics.render.canvas.text.SdfFont;

/**
 * Demo showcasing all Canvas shape rendering capabilities.
 * Tests filled and stroked shapes: rectangles, rounded rectangles, and circles.
 */
public class CanvasShapesDemo extends DemoGame {

    private Camera2D camera;
    private GlCanvasRenderer canvas;
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

        // Initialize camera
        camera = new Camera2D(this);

        // Create canvas renderer with viewport dimensions
        canvas = new GlCanvasRenderer(getVirtualWidth(), getVirtualHeight());

        // Initialize content manager
        content = ServiceProvider.get(ContentManager.class);

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
        canvas.begin(camera.getViewMatrix());

        // Main title
        canvas.drawText("Canvas Shapes Showcase - All SDF Rendered!", font, 20, 20, 
            new Color(1.0f, 1.0f, 1.0f, 1.0f));

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
        canvas.fillRoundedRect(20, 60, 370, 180, 10, new Color(0.15f, 0.15f, 0.2f, 0.9f));
        canvas.drawText("Filled Shapes", font, 30, 70, new Color(0.9f, 0.9f, 1.0f, 1.0f));
        canvas.drawText("All rendered with 6 vertices (2 triangles)!", font, 30, 90, 
            new Color(0.6f, 0.6f, 0.7f, 1.0f));

        // Regular rectangle
        canvas.fillRect(40, 120, 60, 60, new Color(0.8f, 0.3f, 0.3f, 1.0f));
        canvas.drawText("Rect", font, 45, 190, new Color(0.9f, 0.9f, 0.9f, 1.0f));

        // Rounded rectangles with different radii
        canvas.fillRoundedRect(130, 120, 60, 60, 5, new Color(0.3f, 0.8f, 0.3f, 1.0f));
        canvas.drawText("R=5", font, 140, 190, new Color(0.9f, 0.9f, 0.9f, 1.0f));

        canvas.fillRoundedRect(220, 120, 60, 60, 15, new Color(0.3f, 0.5f, 0.9f, 1.0f));
        canvas.drawText("R=15", font, 230, 190, new Color(0.9f, 0.9f, 0.9f, 1.0f));

        // Circle
        canvas.fillCircle(340, 150, 30, new Color(0.9f, 0.6f, 0.2f, 1.0f));
        canvas.drawText("Circle", font, 315, 190, new Color(0.9f, 0.9f, 0.9f, 1.0f));
    }

    /**
     * Section 2: Stroked shapes (outlines only).
     */
    private void drawStrokedShapes() {
        // Header
        canvas.fillRoundedRect(410, 60, 370, 180, 10, new Color(0.15f, 0.15f, 0.2f, 0.9f));
        canvas.drawText("Stroked Shapes", font, 420, 70, new Color(0.9f, 0.9f, 1.0f, 1.0f));
        canvas.drawText("Different stroke widths, same efficiency!", font, 420, 90, 
            new Color(0.6f, 0.6f, 0.7f, 1.0f));

        // Rectangle with thin stroke
        canvas.strokeRect(430, 120, 60, 60, 2, new Color(0.8f, 0.3f, 0.3f, 1.0f));
        canvas.drawText("W=2", font, 440, 190, new Color(0.9f, 0.9f, 0.9f, 1.0f));

        // Rounded rectangle with medium stroke
        canvas.strokeRoundedRect(520, 120, 60, 60, 10, 4, new Color(0.3f, 0.8f, 0.3f, 1.0f));
        canvas.drawText("W=4", font, 530, 190, new Color(0.9f, 0.9f, 0.9f, 1.0f));

        // Rounded rectangle with thick stroke
        canvas.strokeRoundedRect(610, 120, 60, 60, 15, 6, new Color(0.3f, 0.5f, 0.9f, 1.0f));
        canvas.drawText("W=6", font, 620, 190, new Color(0.9f, 0.9f, 0.9f, 1.0f));

        // Circle with stroke
        canvas.strokeCircle(730, 150, 30, 3, new Color(0.9f, 0.6f, 0.2f, 1.0f));
        canvas.drawText("W=3", font, 710, 190, new Color(0.9f, 0.9f, 0.9f, 1.0f));
    }

    /**
     * Section 3: Combined filled and stroked shapes.
     */
    private void drawCombinedShapes() {
        // Header
        canvas.fillRoundedRect(20, 260, 760, 160, 10, new Color(0.15f, 0.15f, 0.2f, 0.9f));
        canvas.drawText("Combined Fill + Stroke", font, 30, 270, new Color(0.9f, 0.9f, 1.0f, 1.0f));
        canvas.drawText("Layered shapes for UI elements", font, 30, 290, 
            new Color(0.6f, 0.6f, 0.7f, 1.0f));

        // Button-like element 1
        float button1Y = 320;
        float button1Height = 40;
        canvas.fillRoundedRect(50, button1Y, 100, button1Height, 8, new Color(0.2f, 0.4f, 0.8f, 1.0f));
        canvas.strokeRoundedRect(50, button1Y, 100, button1Height, 8, 2, new Color(0.4f, 0.6f, 1.0f, 1.0f));
        String button1Text = "Button 1";
        canvas.drawText(button1Text, font, 
            canvas.centerTextHorizontally(50, 100, button1Text, font), 
            canvas.centerTextVertically(button1Y, button1Height, font), 
            new Color(1.0f, 1.0f, 1.0f, 1.0f));

        // Button-like element 2
        canvas.fillRoundedRect(180, button1Y, 100, button1Height, 8, new Color(0.2f, 0.7f, 0.3f, 1.0f));
        canvas.strokeRoundedRect(180, button1Y, 100, button1Height, 8, 2, new Color(0.4f, 1.0f, 0.5f, 1.0f));
        String button2Text = "Button 2";
        canvas.drawText(button2Text, font, 
            canvas.centerTextHorizontally(180, 100, button2Text, font),
            canvas.centerTextVertically(button1Y, button1Height, font),
            new Color(1.0f, 1.0f, 1.0f, 1.0f));

        // Checkbox-like element
        float checkboxY = 325;
        float checkboxSize = 30;
        canvas.fillRect(320, checkboxY, checkboxSize, checkboxSize, new Color(0.9f, 0.9f, 0.9f, 1.0f));
        canvas.strokeRect(320, checkboxY, checkboxSize, checkboxSize, 2, new Color(0.3f, 0.3f, 0.3f, 1.0f));
        canvas.drawText("Checkbox", font, 360, canvas.centerTextVertically(checkboxY, checkboxSize, font),
            new Color(0.9f, 0.9f, 0.9f, 1.0f));

        // Radio button-like element
        float radioCenterY = 340;
        canvas.fillCircle(490, radioCenterY, 12, new Color(0.9f, 0.9f, 0.9f, 1.0f));
        canvas.strokeCircle(490, radioCenterY, 12, 2, new Color(0.3f, 0.3f, 0.3f, 1.0f));
        canvas.fillCircle(490, radioCenterY, 6, new Color(0.2f, 0.4f, 0.8f, 1.0f));
        // Vertically center text with circle (Y is circle center, subtract half font size)
        canvas.drawText("Radio", font, 510, radioCenterY - font.getFontSize() / 2,
            new Color(0.9f, 0.9f, 0.9f, 1.0f));

        // Badge/notification
        float badgeCenterY = 340;
        canvas.fillCircle(610, badgeCenterY, 15, new Color(0.9f, 0.2f, 0.2f, 1.0f));
        canvas.strokeCircle(610, badgeCenterY, 15, 2, new Color(1.0f, 0.4f, 0.4f, 1.0f));
        // Vertically center text with circle (Y is circle center, subtract half font size)
        canvas.drawText("Badge", font, 640, badgeCenterY - font.getFontSize() / 2,
            new Color(0.9f, 0.9f, 0.9f, 1.0f));
    }

    /**
     * Section 4: Animated shapes showing transform capabilities.
     */
    private void drawAnimatedShapes() {
        // Header
        canvas.fillRoundedRect(20, 440, 760, 140, 10, new Color(0.15f, 0.15f, 0.2f, 0.9f));
        canvas.drawText("Animated Shapes", font, 30, 450, new Color(0.9f, 0.9f, 1.0f, 1.0f));
        canvas.drawText("Rotation, scaling, pulsing effects", font, 30, 470, 
            new Color(0.6f, 0.6f, 0.7f, 1.0f));

        // Rotating rectangle
        canvas.save();
        canvas.translate(100, 530);
        canvas.rotate(time * 2);
        canvas.strokeRect(-20, -20, 40, 40, 3, new Color(0.8f, 0.3f, 0.8f, 1.0f));
        canvas.restore();

        // Pulsing circle
        float pulse = 1.0f + 0.3f * (float) Math.sin(time * 4);
        canvas.save();
        canvas.translate(220, 530);
        canvas.scale(pulse);
        canvas.fillCircle(0, 0, 20, new Color(0.3f, 0.8f, 0.8f, 1.0f));
        canvas.restore();

        // Rotating rounded rectangle with trail effect
        for (int i = 0; i < 5; i++) {
            float alpha = 0.2f + i * 0.15f;
            float angle = time * 1.5f - i * 0.3f;
            
            canvas.save();
            canvas.translate(360, 530);
            canvas.rotate(angle);
            canvas.fillRoundedRect(-15, -15, 30, 30, 8, 
                new Color(0.9f, 0.6f, 0.2f, alpha));
            canvas.restore();
        }

        // Orbiting circles
        canvas.save();
        canvas.translate(500, 530);
        
        for (int i = 0; i < 6; i++) {
            float orbitAngle = time + i * (float)(Math.PI * 2 / 6);
            float orbitRadius = 40;
            float cx = (float) Math.cos(orbitAngle) * orbitRadius;
            float cy = (float) Math.sin(orbitAngle) * orbitRadius;
            
            Color orbitColor = new Color(
                0.5f + 0.5f * (float) Math.sin(orbitAngle),
                0.5f + 0.5f * (float) Math.cos(orbitAngle),
                0.8f,
                1.0f
            );
            canvas.fillCircle(cx, cy, 8, orbitColor);
        }
        canvas.restore();

        // Breathing rounded rectangle
        float breathe = 1.0f + 0.2f * (float) Math.sin(time * 3);
        canvas.save();
        canvas.translate(650, 530);
        canvas.scale(breathe, breathe);
        canvas.fillRoundedRect(-25, -25, 50, 50, 12, new Color(0.4f, 0.3f, 0.9f, 1.0f));
        canvas.strokeRoundedRect(-25, -25, 50, 50, 12, 2, new Color(0.7f, 0.6f, 1.0f, 1.0f));
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
        settings.setWindowResizable(false);
        
        CanvasShapesDemo demo = new CanvasShapesDemo(settings);
        demo.start();
    }
}
