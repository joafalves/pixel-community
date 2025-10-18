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
import org.pixel.graphics.render.canvas.Canvas;
import org.pixel.graphics.render.canvas.GLCanvas;
import org.pixel.graphics.render.canvas.text.SdfFont;
import org.pixel.math.Size;
import org.pixel.math.Vector2;

/**
 * Demo showcasing text measurement and alignment helpers.
 * Shows how to properly center and position text using measureText() and helper methods.
 */
public class CanvasTextMeasurementDemo extends DemoGame {

    private Camera2D camera;
    private Canvas canvas;
    private ContentManager content;
    private SdfFont font;
    private SdfFont smallFont;

    public CanvasTextMeasurementDemo(WindowSettings settings) {
        super(settings);
    }

    @Override
    public void load() {
        super.load();

        camera = new Camera2D(this);
        canvas = new GLCanvas(getViewportWidth(), getViewportHeight());
        content = ContentManager.create();
        font = content.load("fonts/roboto-medium.ttf", SdfFont.class,
            new FontImporterSettings(18, 3));
        smallFont = content.load("fonts/roboto-medium.ttf", SdfFont.class,
            new FontImporterSettings(12, 3));
    }

    @Override
    public void draw(DeltaTime delta) {
        super.draw(delta);

        if (font == null || smallFont == null) return;

        // Begin with camera's view matrix
        canvas.begin(camera.getViewMatrix());

        // Title
        canvas.text("Text Measurement & Alignment Demo", font, 20, 20)
            .withFill(Color.WHITE)
            .apply();

        // Demo 1: Measuring text dimensions
        drawDemo1_MeasureText();

        // Demo 2: Horizontal centering
        drawDemo2_HorizontalCentering();

        // Demo 3: Vertical centering
        drawDemo3_VerticalCentering();

        // Demo 4: Full centering (both axes)
        drawDemo4_FullCentering();

        canvas.end();
    }

    /**
     * Demo 1: Using measureText() to get text dimensions.
     */
    private void drawDemo1_MeasureText() {
        // Background
        canvas.rect(20, 60, 370, 150)
            .withRoundedCorners(10)
            .withFill(new Color(0.15f, 0.15f, 0.2f, 0.9f))
            .apply();
        canvas.text("Demo 1: Text Measurement", font, 30, 70)
            .withFill(Color.WHITE)
            .apply();

        // Sample texts
        String[] texts = {"Short", "Medium text", "This is a longer text sample"};
        float y = 100;

        for (String text : texts) {
            // Measure the text (now directly on Canvas!)
            Size textSize = canvas.measureText(text, font);

            // Draw the text
            float x = 30;
            canvas.text(text, font, x, y)
                .withFill(new Color(0.9f, 0.9f, 1.0f, 1.0f))
                .apply();

            // Draw a box around it showing the measured dimensions
            canvas.rect(x, y, textSize.getWidth(), textSize.getHeight())
                .withStroke(1, new Color(1, 1, 0, 0.5f))
                .apply();

            // Show dimensions
            String dims = String.format("%.0f × %.0f px", textSize.getWidth(), textSize.getHeight());
            canvas.text(dims, smallFont, x + textSize.getWidth() + 10, y + 5)
                .withFill(new Color(0.7f, 0.7f, 0.7f, 1.0f))
                .apply();

            y += 35;
        }
    }

    /**
     * Demo 2: Horizontal text centering.
     */
    private void drawDemo2_HorizontalCentering() {
        // Background
        canvas.rect(410, 60, 370, 150)
            .withRoundedCorners(10)
            .withFill(new Color(0.15f, 0.15f, 0.2f, 0.9f))
            .apply();
        canvas.text("Demo 2: Horizontal Centering", font, 420, 70)
            .withFill(Color.WHITE)
            .apply();

        // Container boxes
        float[] boxWidths = {100, 200, 300};
        float y = 100;

        for (float boxWidth : boxWidths) {
            float boxX = 420 + (350 - boxWidth) / 2; // Center the box itself

            // Draw container
            canvas.rect(boxX, y, boxWidth, 30)
                .withStroke(2, new Color(0, 1, 1, 0.5f))
                .apply();

            // Text to center
            String text = String.format("%.0f px wide", boxWidth);

            // Center text horizontally
            float textX = canvas.getRenderer().centerTextHorizontally(boxX, boxWidth, text, font);
            canvas.text(text, font, textX, y + 7)
                .withFill(new Color(1, 1, 0.5f, 1.0f))
                .apply();

            // Show center line
            canvas.rect(boxX + boxWidth / 2 - 0.5f, y, 1, 30)
                .withFill(new Color(1, 0, 0, 0.3f))
                .apply();

            y += 40;
        }
    }

    /**
     * Demo 3: Vertical text centering.
     */
    private void drawDemo3_VerticalCentering() {
        // Background
        canvas.rect(20, 230, 370, 150)
            .withRoundedCorners(10)
            .withFill(new Color(0.15f, 0.15f, 0.2f, 0.9f))
            .apply();
        canvas.text("Demo 3: Vertical Centering", font, 30, 240)
            .withFill(Color.WHITE)
            .apply();

        // Container boxes with different heights
        float[] boxHeights = {30, 50, 70};
        float x = 30;

        for (float boxHeight : boxHeights) {
            float boxY = 270;

            // Draw container
            canvas.rect(x, boxY, 100, boxHeight)
                .withStroke(2, new Color(0, 1, 0, 0.5f))
                .apply();

            // Text to center
            String text = "Centered";

            // Center text vertically
            float textY = canvas.getRenderer().centerTextVertically(boxY, boxHeight, font);
            canvas.text(text, font, x + 10, textY)
                .withFill(new Color(0.5f, 1, 1, 1.0f))
                .apply();

            // Show center line
            canvas.rect(x, boxY + boxHeight / 2 - 0.5f, 100, 1)
                .withFill(new Color(1, 0, 0, 0.3f))
                .apply();

            // Label
            canvas.text(String.format("%.0f px", boxHeight), smallFont, x + 30, boxY + boxHeight + 5)
                .withFill(new Color(0.7f, 0.7f, 0.7f, 1.0f))
                .apply();

            x += 120;
        }
    }

    /**
     * Demo 4: Full centering (both horizontal and vertical).
     */
    private void drawDemo4_FullCentering() {
        // Background
        canvas.rect(410, 230, 370, 150)
            .withRoundedCorners(10)
            .withFill(new Color(0.15f, 0.15f, 0.2f, 0.9f))
            .apply();
        canvas.text("Demo 4: Full Centering", font, 420, 240)
            .withFill(Color.WHITE)
            .apply();

        // Different sized boxes
        float[][] boxes = {
            {80, 60},   // width, height
            {120, 80},
            {100, 100}
        };

        float startX = 430;
        float y = 270;

        for (int i = 0; i < boxes.length; i++) {
            float boxWidth = boxes[i][0];
            float boxHeight = boxes[i][1];
            float boxX = startX + i * 120;

            // Draw container with fill
            Color fillColor = new Color(
                0.2f + i * 0.15f,
                0.3f + i * 0.1f,
                0.5f + i * 0.15f,
                0.8f
            );
            canvas.rect(boxX, y, boxWidth, boxHeight)
                .withRoundedCorners(8)
                .withFill(fillColor)
                .apply();
            canvas.rect(boxX, y, boxWidth, boxHeight)
                .withRoundedCorners(8)
                .withStroke(2, new Color(1, 1, 1, 0.3f))
                .apply();

            // Text to center
            String text = String.format("%d", i + 1);

            // Center text both ways using the helper method
            Vector2 textPos = canvas.getRenderer().centerText(boxX, y, boxWidth, boxHeight, text, font);
            canvas.text(text, font, textPos.getX(), textPos.getY())
                .withFill(Color.WHITE)
                .apply();

            // Show crosshair at center
            float centerX = boxX + boxWidth / 2;
            float centerY = y + boxHeight / 2;
            canvas.rect(centerX - 5, centerY - 0.5f, 10, 1)
                .withFill(new Color(1, 1, 0, 0.5f))
                .apply();
            canvas.rect(centerX - 0.5f, centerY - 5, 1, 10)
                .withFill(new Color(1, 1, 0, 0.5f))
                .apply();
        }

        // Info text
        canvas.text("Using centerText() for perfect centering", smallFont, 430, y + 110)
            .withFill(new Color(0.7f, 0.7f, 0.7f, 1.0f))
            .apply();
    }

    @Override
    public void dispose() {
        super.dispose();
        if (canvas != null) canvas.dispose();
        if (content != null) content.dispose();
    }

    public static void main(String[] args) {
        var settings = new WindowSettings(800, 400);
        settings.setTitle("Text Measurement Demo");
        settings.setWindowResizable(false);

        CanvasTextMeasurementDemo demo = new CanvasTextMeasurementDemo(settings);
        demo.start();
    }
}
