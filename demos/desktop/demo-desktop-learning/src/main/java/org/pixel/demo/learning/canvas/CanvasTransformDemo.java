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
 * Demo showcasing Canvas Renderer transform stack functionality.
 * Tests save/restore, translate, rotate, scale operations.
 */
public class CanvasTransformDemo extends DemoGame {

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
    public CanvasTransformDemo(WindowSettings settings) {
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
        font = content.load("fonts/roboto-medium.ttf", SdfFont.class,
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

        // Begin with camera's view matrix
        canvas.begin(camera.getViewMatrix());

        // Demo 1: Simple translation
        drawDemo1_SimpleTranslation();

        // Demo 2: Nested save/restore
        drawDemo2_NestedSaveRestore();

        // Demo 3: Rotation
        drawDemo3_Rotation();

        // Demo 4: Scaling
        drawDemo4_Scaling();

        // Demo 5: Combined transforms with stack
        drawDemo5_CombinedTransforms();

        // Demo 6: Hierarchical transforms
        drawDemo6_HierarchicalTransforms();

        canvas.end();
    }

    /**
     * Demo 1: Simple translation - draw three squares in a row.
     */
    private void drawDemo1_SimpleTranslation() {
        // Header with rounded corners
        canvas.fillRoundedRect(20, 20, 230, 160, 10, new Color(0.15f, 0.15f, 0.2f, 0.9f));
        canvas.drawText("Translation", font, 30, 30, new Color(0.8f, 0.8f, 1.0f, 1.0f));
        canvas.drawText("Cumulative translate()", font, 30, 50, new Color(0.6f, 0.6f, 0.7f, 1.0f));

        canvas.save();
        canvas.translate(40, 80);

        // Draw 3 rounded squares with cumulative translation
        for (int i = 0; i < 3; i++) {
            Color color = new Color(1.0f - i * 0.25f, 0.3f, 0.3f + i * 0.3f, 1.0f);
            canvas.fillRoundedRect(0, 0, 35, 35, 8, color);
            canvas.translate(50, 0); // Move right for next square
        }

        canvas.restore(); // Back to origin
    }

    /**
     * Demo 2: Nested save/restore - demonstrate stack behavior.
     */
    private void drawDemo2_NestedSaveRestore() {
        // Header with rounded corners
        canvas.fillRoundedRect(270, 20, 250, 160, 10, new Color(0.15f, 0.15f, 0.2f, 0.9f));
        canvas.drawText("Save/Restore Stack", font, 280, 30, new Color(0.8f, 0.8f, 1.0f, 1.0f));
        canvas.drawText("Nested transforms", font, 280, 50, new Color(0.6f, 0.6f, 0.7f, 1.0f));

        canvas.save(); // Level 1
        canvas.translate(305, 80);
        canvas.fillRoundedRect(0, 0, 70, 70, 12, new Color(0.2f, 0.4f, 0.8f, 1.0f));

        canvas.save(); // Level 2
        canvas.translate(15, 15);
        canvas.fillRoundedRect(0, 0, 40, 40, 8, new Color(0.4f, 0.8f, 0.4f, 1.0f));

        canvas.save(); // Level 3
        canvas.translate(10, 10);
        canvas.fillRoundedRect(0, 0, 20, 20, 5, new Color(0.8f, 0.8f, 0.2f, 1.0f));

        canvas.restore(); // Back to Level 2
        canvas.restore(); // Back to Level 1
        canvas.restore(); // Back to origin
    }

    /**
     * Demo 3: Rotation - rotating rectangle.
     */
    private void drawDemo3_Rotation() {
        // Header
        canvas.fillRoundedRect(540, 20, 240, 160, 10, new Color(0.15f, 0.15f, 0.2f, 0.9f));
        canvas.drawText("Rotation", font, 550, 30, new Color(0.8f, 0.8f, 1.0f, 1.0f));
        canvas.drawText("Animated rotate()", font, 550, 50, new Color(0.6f, 0.6f, 0.7f, 1.0f));

        canvas.save();
        canvas.translate(660, 110); // Move to center of rotation

        // Rotate around center
        canvas.rotate(time * 2); // Rotate based on time

        // Draw rectangle centered at origin
        canvas.fillRect(-25, -25, 50, 50, new Color(0.8f, 0.3f, 0.8f, 1.0f));

        canvas.restore();
    }

    /**
     * Demo 4: Scaling - growing/shrinking rectangles.
     */
    private void drawDemo4_Scaling() {
        // Header
        canvas.fillRoundedRect(20, 200, 230, 160, 10, new Color(0.15f, 0.15f, 0.2f, 0.9f));
        canvas.drawText("Scaling", font, 30, 210, new Color(0.8f, 0.8f, 1.0f, 1.0f));
        canvas.drawText("Animated scale()", font, 30, 230, new Color(0.6f, 0.6f, 0.7f, 1.0f));

        canvas.save();
        canvas.translate(135, 290);

        // Animated scale (oscillates between 0.5 and 1.5)
        float scale = 1.0f + 0.5f * (float) Math.sin(time * 3);
        canvas.scale(scale, scale);

        canvas.fillRect(-30, -30, 60, 60, new Color(0.3f, 0.8f, 0.8f, 1.0f));

        canvas.restore();
    }

    /**
     * Demo 5: Combined transforms - translate, rotate, scale together.
     */
    private void drawDemo5_CombinedTransforms() {
        // Header
        canvas.fillRoundedRect(270, 200, 510, 160, 10, new Color(0.15f, 0.15f, 0.2f, 0.9f));
        canvas.drawText("Combined Transforms", font, 280, 210, new Color(0.8f, 0.8f, 1.0f, 1.0f));
        canvas.drawText("translate + rotate + scale", font, 280, 230, new Color(0.6f, 0.6f, 0.7f, 1.0f));

        // Draw multiple boxes with combined transforms
        for (int i = 0; i < 5; i++) {
            canvas.save();

            // Translate to position
            canvas.translate(310 + i * 90, 290);

            // Rotate
            canvas.rotate(time + i * 0.6f);

            // Scale
            float scale = 0.4f + i * 0.12f;
            canvas.scale(scale, scale);

            // Draw centered square
            Color color = new Color(
                0.3f + i * 0.15f,
                0.5f,
                0.8f - i * 0.1f,
                1.0f
            );
            canvas.fillRect(-20, -20, 40, 40, color);

            canvas.restore();
        }
    }

    /**
     * Demo 6: Hierarchical transforms - parent/child relationship.
     */
    private void drawDemo6_HierarchicalTransforms() {
        // Header
        canvas.fillRoundedRect(20, 380, 760, 200, 10, new Color(0.15f, 0.15f, 0.2f, 0.9f));
        canvas.drawText("Hierarchical Transforms (Solar System)", font, 30, 390, new Color(0.8f, 0.8f, 1.0f, 1.0f));
        canvas.drawText("Nested save/restore creates parent-child relationships", font, 30, 410, new Color(0.6f, 0.6f, 0.7f, 1.0f));

        // System 1: Sun-Planet-Moon
        canvas.save();
        canvas.translate(140, 490);
        
        // Sun
        canvas.fillCircle(0, -12, 20, Color.YELLOW);
        canvas.drawText("Sun", font, -10, -20, new Color(1.0f, 0.9f, 0.5f, 1.0f));

        // Planet orbiting sun
        canvas.save();
        canvas.rotate(time * 0.5f);
        canvas.translate(70, 0);
        canvas.fillRect(-8, -8, 16, 16, new Color(0.3f, 0.5f, 0.9f, 1.0f));
        canvas.drawText("Planet", font, -15, -15, new Color(0.7f, 0.8f, 1.0f, 1.0f));

        // Moon orbiting planet
        canvas.save();
        canvas.rotate(time * 2);
        canvas.translate(25, 0);
        canvas.fillRect(-4, -4, 8, 8, new Color(0.7f, 0.7f, 0.7f, 1.0f));
        canvas.drawText("Moon", font, -12, -10, new Color(0.9f, 0.9f, 0.9f, 1.0f));
        canvas.restore(); // Moon
        canvas.restore(); // Planet
        canvas.restore(); // Sun

        // System 2: Larger system
        canvas.save();
        canvas.translate(400, 490);
        
        // Central star
        canvas.fillRect(-15, -15, 30, 30, new Color(1.0f, 0.6f, 0.1f, 1.0f));

        // Inner planet
        canvas.save();
        canvas.rotate(-time * 0.8f);
        canvas.translate(60, 0);
        canvas.fillRect(-6, -6, 12, 12, new Color(0.9f, 0.4f, 0.3f, 1.0f));
        canvas.restore();

        // Outer planet with rings
        canvas.save();
        canvas.rotate(time * 0.3f);
        canvas.translate(100, 0);
        canvas.fillRect(-10, -10, 20, 20, new Color(0.8f, 0.7f, 0.4f, 1.0f));
        
        // Two moons
        canvas.save();
        canvas.rotate(time * 1.5f);
        canvas.translate(22, 0);
        canvas.fillRect(-3, -3, 6, 6, new Color(0.6f, 0.6f, 0.7f, 1.0f));
        canvas.restore();
        
        canvas.save();
        canvas.rotate(-time * 2.5f);
        canvas.translate(30, 0);
        canvas.fillRect(-3, -3, 6, 6, new Color(0.5f, 0.5f, 0.6f, 1.0f));
        canvas.restore();
        
        canvas.restore();
        canvas.restore(); // System
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
        settings.setTitle("Canvas Transform Stack Demo");
        settings.setWindowResizable(false);
        
        CanvasTransformDemo demo = new CanvasTransformDemo(settings);
        demo.start();
    }
}
