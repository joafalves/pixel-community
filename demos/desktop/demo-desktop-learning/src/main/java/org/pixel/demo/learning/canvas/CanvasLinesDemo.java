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
import org.pixel.graphics.render.canvas.GLCanvasRenderer;
import org.pixel.graphics.render.canvas.text.SdfFont;
import org.pixel.math.Vector2;

/**
 * Demo showcasing Canvas line and point rendering capabilities.
 * Tests line drawing and point drawing similar to HTML5 Canvas API.
 */
public class CanvasLinesDemo extends DemoGame {

    private Camera2D camera;
    private GLCanvasRenderer canvas;
    private ContentManager content;
    private SdfFont font;
    private float time = 0;

    /**
     * Constructor.
     *
     * @param settings Window settings
     */
    public CanvasLinesDemo(WindowSettings settings) {
        super(settings);
    }

    @Override
    public void load() {
        super.load();

        // Initialize camera
        camera = new Camera2D(this);

        // Create canvas renderer with viewport dimensions
        canvas = new GLCanvasRenderer(getViewportWidth(), getViewportHeight());

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
        canvas.drawText("Canvas Lines & Points Showcase", font, 20, 20, 
            new Color(1.0f, 1.0f, 1.0f, 1.0f));

        // Section 1: Basic Lines
        drawBasicLines();

        // Section 2: Points
        drawPoints();

        // Section 3: Grid Pattern
        drawGrid();

        // Section 4: Animated Lines
        drawAnimatedLines();

        canvas.end();
    }

    /**
     * Section 1: Basic line drawing with different widths and colors.
     */
    private void drawBasicLines() {
        // Header
        canvas.drawText("Basic Lines", font, 20, 60, Color.YELLOW);

        float startX = 40;
        float startY = 90;

        // Horizontal lines with varying widths
        canvas.strokeLine(startX, startY, startX + 200, startY, 1, Color.RED);
        canvas.strokeLine(startX, startY + 20, startX + 200, startY + 20, 2, Color.GREEN);
        canvas.strokeLine(startX, startY + 45, startX + 200, startY + 45, 4, Color.BLUE);
        canvas.strokeLine(startX, startY + 75, startX + 200, startY + 75, 8, Color.MAGENTA);

        // Vertical lines
        float vertX = startX + 250;
        canvas.strokeLine(vertX, startY, vertX, startY + 100, 2, new Color(0.0f, 1.0f, 1.0f, 1.0f)); // Cyan
        canvas.strokeLine(vertX + 30, startY, vertX + 30, startY + 100, 4, Color.ORANGE);
        canvas.strokeLine(vertX + 70, startY, vertX + 70, startY + 100, 6, Color.WHITE);

        // Diagonal lines
        float diagX = startX + 370;
        canvas.strokeLine(diagX, startY, diagX + 100, startY + 100, 3, Color.YELLOW);
        canvas.strokeLine(diagX + 100, startY, diagX, startY + 100, 3, new Color(0.5f, 1.0f, 0.5f, 1.0f));

        // Lines with Vector2 (using helper method)
        Vector2 v1 = new Vector2(diagX + 130, startY + 20);
        Vector2 v2 = new Vector2(diagX + 200, startY + 80);
        canvas.strokeLine(v1, v2, 5, new Color(1.0f, 0.5f, 0.0f, 1.0f));
    }

    /**
     * Section 2: Point drawing with different sizes and colors.
     */
    private void drawPoints() {
        // Header
        canvas.drawText("Points", font, 20, 220, Color.YELLOW);

        float startX = 40;
        float startY = 250;

        // Row of points with increasing size
        for (int i = 0; i < 10; i++) {
            float size = 2 + i * 2;
            float x = startX + i * 40;
            canvas.fillPoint(x, startY, size, Color.RED);
        }

        // Points with different colors
        Color[] colors = {Color.RED, Color.GREEN, Color.BLUE, Color.YELLOW, 
                         new Color(0.0f, 1.0f, 1.0f, 1.0f), Color.MAGENTA, Color.ORANGE, Color.WHITE}; // Cyan as new Color
        for (int i = 0; i < colors.length; i++) {
            float x = startX + i * 50;
            canvas.fillPoint(x, startY + 50, 10, colors[i]);
        }

        // Points using Vector2
        Vector2 pos = new Vector2(startX + 150, startY + 100);
        canvas.fillPoint(pos, 15, new Color(1.0f, 0.5f, 1.0f, 1.0f));
    }

    /**
     * Section 3: Grid pattern using lines.
     */
    private void drawGrid() {
        // Header
        canvas.drawText("Grid Pattern", font, 20, 390, Color.YELLOW);

        float gridX = 40;
        float gridY = 420;
        float gridSize = 200;
        int divisions = 10;
        float cellSize = gridSize / divisions;

        Color gridColor = new Color(0.3f, 0.3f, 0.3f, 1.0f);

        // Vertical lines
        for (int i = 0; i <= divisions; i++) {
            float x = gridX + i * cellSize;
            canvas.strokeLine(x, gridY, x, gridY + gridSize, 1, gridColor);
        }

        // Horizontal lines
        for (int i = 0; i <= divisions; i++) {
            float y = gridY + i * cellSize;
            canvas.strokeLine(gridX, y, gridX + gridSize, y, 1, gridColor);
        }

        // Border (thicker lines)
        canvas.strokeLine(gridX, gridY, gridX + gridSize, gridY, 2, Color.WHITE);
        canvas.strokeLine(gridX + gridSize, gridY, gridX + gridSize, gridY + gridSize, 2, Color.WHITE);
        canvas.strokeLine(gridX + gridSize, gridY + gridSize, gridX, gridY + gridSize, 2, Color.WHITE);
        canvas.strokeLine(gridX, gridY + gridSize, gridX, gridY, 2, Color.WHITE);
    }

    /**
     * Section 4: Animated lines and points.
     */
    private void drawAnimatedLines() {
        // Header
        canvas.drawText("Animated", font, 280, 390, Color.YELLOW);

        float centerX = 380;
        float centerY = 520;
        float radius = 80;

        // Rotating lines from center
        int numLines = 12;
        for (int i = 0; i < numLines; i++) {
            float angle = time * 2 + (i * (float)Math.PI * 2 / numLines);
            float x = centerX + (float)Math.cos(angle) * radius;
            float y = centerY + (float)Math.sin(angle) * radius;
            
            Color lineColor = new Color(
                0.5f + 0.5f * (float)Math.sin(time + i * 0.5f),
                0.5f + 0.5f * (float)Math.cos(time + i * 0.3f),
                0.5f + 0.5f * (float)Math.sin(time + i * 0.7f),
                1.0f
            );
            
            canvas.strokeLine(centerX, centerY, x, y, 2, lineColor);
            canvas.fillPoint(x, y, 8, Color.WHITE);
        }

        // Pulsating circle of points
        float pulseRadius = 50 + 20 * (float)Math.sin(time * 3);
        int numPoints = 8;
        for (int i = 0; i < numPoints; i++) {
            float angle = i * (float)Math.PI * 2 / numPoints;
            float x = centerX + (float)Math.cos(angle) * pulseRadius;
            float y = centerY + (float)Math.sin(angle) * pulseRadius;
            
            canvas.fillPoint(x, y, 12, Color.YELLOW);
        }
    }

    @Override
    public void dispose() {
        canvas.dispose();
        super.dispose();
    }

    public static void main(String[] args) {
        var settings = new WindowSettings(640, 640);
        settings.setTitle("Canvas Lines & Points Demo");
        settings.setWindowResizable(false);
        settings.setMultisampling(4); // Enable antialiasing

        CanvasLinesDemo demo = new CanvasLinesDemo(settings);
        demo.start();
    }
}
