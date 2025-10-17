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
import org.pixel.core.WindowSettings;
import org.pixel.demo.learning.common.DemoGame;
import org.pixel.graphics.render.canvas.GLCanvasRenderer;
import org.pixel.graphics.render.canvas.text.SdfFont;

/**
 * Canvas Path API Demo.
 * Showcases the HTML5 Canvas-style path API for drawing custom polygons and shapes.
 */
public class CanvasPathDemo extends DemoGame {

    private GLCanvasRenderer canvas;
    private ContentManager content;
    private SdfFont font;
    private SdfFont titleFont;
    private float time = 0;

    public CanvasPathDemo(WindowSettings settings) {
        super(settings);
    }

    @Override
    public void load() {
        super.load();

        // Create canvas renderer
        canvas = new GLCanvasRenderer(getViewportWidth(), getViewportHeight());

        // Load content
        content = ServiceProvider.get(ContentManager.class);
        font = content.load("fonts/roboto-regular.ttf", SdfFont.class,
                new FontImporterSettings(16, 3));
        titleFont = content.load("fonts/roboto-medium.ttf", SdfFont.class,
                new FontImporterSettings(24, 3));
    }

    @Override
    public void update(DeltaTime delta) {
        super.update(delta);
        time += delta.getElapsed();
    }

    @Override
    public void draw(DeltaTime delta) {
        super.draw(delta);

        canvas.begin();

        // Title
        canvas.drawText("Canvas Path API Demo (HTML5 Canvas-style)", titleFont, 20, 20, Color.WHITE);
        canvas.drawText("beginPath() -> moveTo() -> lineTo() -> closePath() -> fill()/stroke()", 
                       font, 20, 55, new Color(0.7f, 0.7f, 0.7f, 1.0f));
        //canvas.drawText("Using ear clipping triangulation - works for any simple polygon (convex or concave)",
        //               font, 20, 75, new Color(0.5f, 1.0f, 0.5f, 1.0f));

        // Demo 1: Simple Triangle
        drawDemo1_Triangle();

        // Demo 2: Pentagon
        drawDemo2_Pentagon();

        // Demo 3: Star
        drawDemo3_Star();

        // Demo 4: Complex Polygon
        drawDemo4_ComplexPolygon();

        // Demo 5: Animated Shape
        drawDemo5_AnimatedShape();

        // Demo 6: Stroke vs Fill
        drawDemo6_StrokeVsFill();

        canvas.end();
    }

    /**
     * Demo 1: Simple triangle using path API.
     */
    private void drawDemo1_Triangle() {
        float x = 120;
        float y = 150;
        float size = 60;

        // Label above shape (with better spacing)
        canvas.drawText("Triangle", font, x - 30, y - 35, Color.YELLOW);
        canvas.drawText("(filled)", font, x - 25, y - 20, new Color(0.7f, 0.7f, 0.7f, 1.0f));

        // Draw filled triangle
        canvas.beginPath();
        canvas.moveTo(x, y);
        canvas.lineTo(x + size, y + size);
        canvas.lineTo(x - size, y + size);
        canvas.closePath();
        canvas.fill(new Color(1, 0.5f, 0, 0.8f)); // Orange
    }

    /**
     * Demo 2: Pentagon using path API.
     */
    private void drawDemo2_Pentagon() {
        float centerX = 320;
        float centerY = 180;
        float radius = 50;
        int sides = 5;

        // Label above shape (with better spacing)
        canvas.drawText("Pentagon", font, centerX - 35, centerY - 85, new Color(0, 1, 1, 1));
        canvas.drawText("(stroked)", font, centerX - 30, centerY - 70, new Color(0.7f, 0.7f, 0.7f, 1.0f));

        // Draw pentagon
        canvas.beginPath();
        for (int i = 0; i < sides; i++) {
            float angle = (float) (2 * Math.PI * i / sides - Math.PI / 2);
            float x = centerX + radius * (float) Math.cos(angle);
            float y = centerY + radius * (float) Math.sin(angle);
            
            if (i == 0) {
                canvas.moveTo(x, y);
            } else {
                canvas.lineTo(x, y);
            }
        }
        canvas.closePath();
        canvas.stroke(new Color(0, 1, 1, 1), 2);
    }

    /**
     * Demo 3: Star shape using path API.
     * Stars are concave - ear clipping handles them perfectly!
     */
    private void drawDemo3_Star() {
        float centerX = 540;
        float centerY = 180;
        float outerRadius = 50;
        float innerRadius = 20;
        int points = 5;

        // Label above shape (with better spacing)
        canvas.drawText("Star", font, centerX - 15, centerY - 85, Color.YELLOW);
        canvas.drawText("(filled)", font, centerX - 25, centerY - 70, new Color(0.7f, 0.7f, 0.7f, 1.0f));

        // Draw star
        canvas.beginPath();
        for (int i = 0; i < points * 2; i++) {
            float angle = (float) (Math.PI * i / points - Math.PI / 2);
            float radius = (i % 2 == 0) ? outerRadius : innerRadius;
            float x = centerX + radius * (float) Math.cos(angle);
            float y = centerY + radius * (float) Math.sin(angle);
            
            if (i == 0) {
                canvas.moveTo(x, y);
            } else {
                canvas.lineTo(x, y);
            }
        }
        canvas.closePath();
        canvas.fill(Color.YELLOW); // Ear clipping makes this work perfectly!
    }

    /**
     * Demo 4: Complex polygon (diamond/rhombus).
     */
    private void drawDemo4_ComplexPolygon() {
        float x = 750;
        float y = 180;
        float width = 40;
        float height = 60;

        // Label above shape (with better spacing)
        canvas.drawText("Diamond", font, x - 30, y - 95, new Color(1, 0, 1, 1));
        canvas.drawText("(filled)", font, x - 25, y - 80, new Color(0.7f, 0.7f, 0.7f, 1.0f));

        // Draw diamond
        canvas.beginPath();
        canvas.moveTo(x, y - height);        // Top
        canvas.lineTo(x + width, y);         // Right
        canvas.lineTo(x, y + height);        // Bottom
        canvas.lineTo(x - width, y);         // Left
        canvas.closePath();
        canvas.fill(new Color(1, 0, 1, 0.5f)); // Magenta
    }

    /**
     * Demo 5: Animated rotating hexagon.
     */
    private void drawDemo5_AnimatedShape() {
        float centerX = 150;
        float centerY = 380;
        float radius = 50;
        int sides = 6;

        // Label above shape (with better spacing)
        canvas.drawText("Hexagon", font, centerX - 35, centerY - 95, Color.GREEN);
        canvas.drawText("(animated)", font, centerX - 35, centerY - 80, new Color(0.7f, 0.7f, 0.7f, 1.0f));

        // Save transform state and rotate
        canvas.save();
        canvas.translate(centerX, centerY);
        canvas.rotate(time * 2); // Rotate based on time
        canvas.translate(-centerX, -centerY);

        // Draw hexagon
        canvas.beginPath();
        for (int i = 0; i < sides; i++) {
            float angle = (float) (2 * Math.PI * i / sides);
            float x = centerX + radius * (float) Math.cos(angle);
            float y = centerY + radius * (float) Math.sin(angle);
            
            if (i == 0) {
                canvas.moveTo(x, y);
            } else {
                canvas.lineTo(x, y);
            }
        }
        canvas.closePath();
        canvas.fill(new Color(0, 1, 0, 0.4f)); // Green
        canvas.stroke(Color.GREEN, 2);

        canvas.restore();
    }

    /**
     * Demo 6: Comparison of stroke vs fill.
     */
    private void drawDemo6_StrokeVsFill() {
        float baseX = 380;
        float baseY = 350;
        float size = 50;

        // Label above shapes (with better spacing)
        canvas.drawText("Fill vs Stroke vs Both", font, baseX + 30, baseY - 35, Color.WHITE);

        // Filled only
        canvas.beginPath();
        canvas.moveTo(baseX, baseY);
        canvas.lineTo(baseX + size, baseY + size);
        canvas.lineTo(baseX, baseY + size * 2);
        canvas.closePath();
        canvas.fill(new Color(1, 0, 0, 0.6f));

        // Stroked only
        canvas.beginPath();
        canvas.moveTo(baseX + 120, baseY);
        canvas.lineTo(baseX + 120 + size, baseY + size);
        canvas.lineTo(baseX + 120, baseY + size * 2);
        canvas.closePath();
        canvas.stroke(Color.BLUE, 3);

        // Both filled and stroked
        canvas.beginPath();
        canvas.moveTo(baseX + 240, baseY);
        canvas.lineTo(baseX + 240 + size, baseY + size);
        canvas.lineTo(baseX + 240, baseY + size * 2);
        canvas.closePath();
        canvas.fill(new Color(0, 1, 0, 0.4f));
        canvas.stroke(Color.GREEN, 2);

        // Labels below shapes
        canvas.drawText("Fill", font, baseX + 5, baseY + 120, Color.RED);
        canvas.drawText("Stroke", font, baseX + 110, baseY + 120, Color.BLUE);
        canvas.drawText("Both", font, baseX + 240, baseY + 120, Color.GREEN);
    }

    @Override
    public void dispose() {
        super.dispose();
        if (canvas != null) {
            canvas.dispose();
        }
    }

    public static void main(String[] args) {
        var settings = new WindowSettings(900, 500);
        settings.setTitle("Canvas Path API Demo");
        settings.setWindowResizable(false);
        settings.setMultisampling(4);
        settings.setVsync(false);
        settings.setBackgroundColor(new Color(0.1f, 0.1f, 0.15f, 1.0f));
        
        CanvasPathDemo demo = new CanvasPathDemo(settings);
        demo.start();
    }
}
