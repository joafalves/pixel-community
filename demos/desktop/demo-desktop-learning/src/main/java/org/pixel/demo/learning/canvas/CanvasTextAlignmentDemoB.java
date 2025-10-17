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

/**
 * Comprehensive text alignment demonstration.
 * Shows EXACTLY where text should be positioned according to the API contract.
 * 
 * <p>API Contract:
 * drawText(text, font, x, y, color) positions text such that:
 * - X = left edge of the first glyph's bounding box
 * - Y = TOP of the text line (top of tallest glyph)
 * - Baseline is at Y + font.getAscent()
 * - Capital letters extend from baseline upward (negative offsetY in glyph data)
 */
public class CanvasTextAlignmentDemoB extends DemoGame {

    private Camera2D camera;
    private GLCanvasRenderer canvas;
    private ContentManager content;
    private SdfFont font;

    public CanvasTextAlignmentDemoB(WindowSettings settings) {
        super(settings);
    }

    @Override
    public void load() {
        super.load();
        camera = new Camera2D(this);
        canvas = new GLCanvasRenderer(getViewportWidth(), getViewportHeight());
        content = ServiceProvider.get(ContentManager.class);
        font = content.load("fonts/roboto-regular.ttf", SdfFont.class,
            new FontImporterSettings(24, 3));

        if (font != null) {
            System.out.println("=== FONT METRICS (size=24) ===");
            System.out.println("Font Size: " + font.getFontSize());
            System.out.println("Line Height: " + font.getLineHeight());
            System.out.println("Ascent: " + font.getAscent() + " pixels (distance from top to baseline)");
        }
    }

    @Override
    public void draw(DeltaTime delta) {
        super.draw(delta);
        if (font == null) return;

        // Begin with camera's view matrix
        canvas.begin(camera.getViewMatrix());

        // Title
        canvas.drawText("Text Alignment Reference Guide", font, 20, 20, Color.WHITE);
        canvas.drawText("Font size: 24px, Ascent: " + font.getAscent() + "px", font, 20, 50,
            new Color(0.8f, 0.8f, 0.8f, 1.0f));

        // Test 1: Y coordinate should be TOP of text
        drawTest1_YIsTop();

        // Test 2: Baseline positioning
        drawTest2_BaselineReference();

        // Test 3: Alignment with shapes
        drawTest3_ShapeAlignment();

        // Test 4: Grid alignment
        drawTest4_GridAlignment();

        canvas.end();
    }

    /**
     * Test 1: Y coordinate represents TOP of text line.
     * Expected: Green line should touch the TOP of capital letters.
     */
    private void drawTest1_YIsTop() {
        int testY = 100;
        
        // Background
        canvas.fillRoundedRect(20, 90, 360, 80, 8, new Color(0.15f, 0.15f, 0.2f, 0.9f));
        
        // GREEN line at Y coordinate - this is where text TOP should be
        canvas.fillRect(30, testY, 340, 2, new Color(0, 1, 0, 1));
        
        // Draw text at Y=100
        canvas.drawText("HELLO World", font, 30, testY, Color.WHITE);
        
        // Explanation (properly positioned below)
        canvas.drawText("Test 1: Y=" + testY + " (GREEN line)", font, 30, 150,
            new Color(0, 1, 0, 1));
    }

    /**
     * Test 2: Baseline should be at Y + Ascent.
     * Expected: Red line should be at baseline (bottom of capital letters, top of lowercase bodies).
     */
    private void drawTest2_BaselineReference() {
        int testY = 210;
        int baseline = testY + font.getAscent();
        
        // Background
        canvas.fillRoundedRect(20, 200, 360, 100, 8, new Color(0.15f, 0.15f, 0.2f, 0.9f));
        
        // GREEN line at Y coordinate (top of text)
        canvas.fillRect(30, testY, 340, 1, new Color(0, 1, 0, 0.5f));
        
        // RED line at baseline (Y + ascent)
        canvas.fillRect(30, baseline, 340, 2, new Color(1, 0, 0, 1));
        
        // Draw text
        canvas.drawText("Typography", font, 30, testY, Color.WHITE);
        
        // Explanation (positioned below)
        canvas.drawText("Test 2: Baseline=" + baseline + " (RED line)", font, 30, 280,
            new Color(1, 0.5f, 0.5f, 1));
    }

    /**
     * Test 3: Text aligned with shape tops.
     * Expected: Text top and shape top should align at same Y coordinate.
     */
    private void drawTest3_ShapeAlignment() {
        int shapeY = 330;
        int shapeHeight = 50;
        int textY = shapeY + (shapeHeight - font.getFontSize()) / 2; // Vertically centered
        
        // Background
        canvas.fillRoundedRect(20, 320, 360, 100, 8, new Color(0.15f, 0.15f, 0.2f, 0.9f));
        
        // Shape with yellow top border
        canvas.fillRoundedRect(30, shapeY, 100, shapeHeight, 5, new Color(0.3f, 0.4f, 0.8f, 1.0f));
        canvas.fillRect(30, shapeY, 100, 2, new Color(1, 1, 0, 1)); // Yellow line at shape top
        
        // Text label vertically centered in shape
        canvas.drawText("Label", font, 140, textY, Color.WHITE);
        
        // Reference line at text Y
        canvas.fillRect(140, textY, 100, 1, new Color(0, 1, 0, 0.5f));
        
        // Explanation
        canvas.drawText("Test 3: Text centered in shape", font, 30, 395,
            new Color(1, 1, 0.5f, 1));
    }

    /**
     * Test 4: Multiple texts on a grid.
     * Expected: All texts should align their tops to the grid lines.
     */
    private void drawTest4_GridAlignment() {
        // Background
        canvas.fillRoundedRect(400, 90, 380, 330, 8, new Color(0.15f, 0.15f, 0.2f, 0.9f));
        
        canvas.drawText("Test 4: Grid Alignment", font, 410, 100, Color.WHITE);
        canvas.drawText("Each line Y = grid line", font, 410, 130,
            new Color(0.6f, 0.6f, 0.7f, 1.0f));
        
        // Draw grid and text
        String[] texts = {
            "Line 1: CAPITALS",
            "Line 2: lowercase",
            "Line 3: Mixed Case",
            "Line 4: Typography",
            "Line 5: gjpqy descenders"
        };
        
        int startY = 170;
        int lineSpacing = 40;
        
        for (int i = 0; i < texts.length; i++) {
            int y = startY + i * lineSpacing;
            
            // Grid line (CYAN) - this is the Y coordinate we pass to drawText
            canvas.fillRect(410, y, 360, 1, new Color(0, 1, 1, 0.6f));
            
            // Baseline indicator (RED, semi-transparent)
            int baseline = y + font.getAscent();
            canvas.fillRect(410, baseline, 360, 1, new Color(1, 0, 0, 0.3f));
            
            // Draw text
            canvas.drawText(texts[i], font, 420, y, Color.WHITE);
            
            // Y coordinate label
            canvas.drawText("Y=" + y, font, 700, y, new Color(0, 1, 1, 0.8f));
        }
        
        // Legend
        int legendY = 375;
        canvas.fillRect(410, legendY + 8, 30, 2, new Color(0, 1, 1, 1));
        canvas.drawText("= Text Y (top)", font, 445, legendY - 4,
            new Color(0.7f, 0.7f, 0.7f, 1.0f));
        
        canvas.fillRect(410, legendY + 28, 30, 2, new Color(1, 0, 0, 1));
        canvas.drawText("= Baseline", font, 445, legendY + 16,
            new Color(0.7f, 0.7f, 0.7f, 1.0f));
    }

    @Override
    public void dispose() {
        super.dispose();
        if (canvas != null) canvas.dispose();
        if (content != null) content.dispose();
    }

    public static void main(String[] args) {
        var settings = new WindowSettings(800, 450);
        settings.setTitle("Text Alignment Demo - API Contract Verification");
        settings.setWindowResizable(false);
        settings.setBackgroundColor(new Color(0.05f, 0.05f, 0.1f, 1.0f));
        
        CanvasTextAlignmentDemoB demo = new CanvasTextAlignmentDemoB(settings);
        demo.start();
    }
}
