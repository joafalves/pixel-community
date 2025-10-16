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
import org.pixel.graphics.render.canvas.TextAlign;
import org.pixel.graphics.render.canvas.TextStyle;
import org.pixel.graphics.render.canvas.text.SdfFont;

/**
 * Comprehensive text alignment demonstration.
 * Shows all combinations of horizontal and vertical text alignment.
 */
public class CanvasTextAlignmentDemoA extends DemoGame {

    private Camera2D camera;
    private GlCanvasRenderer canvas;
    private ContentManager content;
    private SdfFont font;
    private SdfFont titleFont;

    public CanvasTextAlignmentDemoA(WindowSettings settings) {
        super(settings);
    }

    @Override
    public void load() {
        super.load();
        camera = new Camera2D(this);
        canvas = new GlCanvasRenderer(getVirtualWidth(), getVirtualHeight());
        content = ServiceProvider.get(ContentManager.class);
        
        font = content.load("fonts/roboto-regular.ttf", SdfFont.class,
            new FontImporterSettings(20, 3));
        titleFont = content.load("fonts/gidole-regular.ttf", SdfFont.class,
            new FontImporterSettings(32, 3));
    }

    @Override
    public void draw(org.pixel.commons.DeltaTime delta) {
        super.draw(delta);
        if (font == null) return;

        canvas.begin(camera.getViewMatrix());

        // Background
        canvas.fillRect(0, 0, getVirtualWidth(), getVirtualHeight(), 
            new Color(0.1f, 0.1f, 0.15f, 1.0f));

        // Title
        TextStyle titleStyle = new TextStyle(Color.WHITE)
            .withStroke(new Color(0.2f, 0.4f, 0.8f, 1.0f), 2.5f)
            .withAlign(TextAlign.topCenter());
        canvas.drawText("Text Alignment Showcase", titleFont, getVirtualWidth() / 2f, 20, titleStyle);

        // === SECTION 1: Horizontal Alignment ===
        drawSection("Horizontal Alignment", 50, 80);
        demonstrateHorizontalAlignment(50, 115);

        // === SECTION 2: Vertical Alignment ===
        drawSection("Vertical Alignment", 50, 260);
        demonstrateVerticalAlignment(50, 295);

        // === SECTION 3: Combined Alignment (9-Grid) ===
        drawSection("Combined Alignment (All 9 Positions)", 50, 440);
        demonstrate9GridAlignment(50, 475);

        // === SECTION 4: Multi-line Text Alignment ===
        drawSection("Multi-line Text Alignment", 50, 670);
        demonstrateMultilineAlignment(50, 705);

        canvas.end();
    }

    /**
     * Demonstrate horizontal alignment: LEFT, CENTER, RIGHT
     */
    private void demonstrateHorizontalAlignment(float x, float y) {
        float refX = x + 300;
        float boxY = y;
        float boxWidth = 200;
        float boxHeight = 120;

        // Reference line (vertical) to show alignment point
        canvas.fillRect(refX - 1, boxY, 2, boxHeight, new Color(1, 0, 0, 0.5f));

        // LEFT alignment
        canvas.fillRoundedRect(refX - boxWidth, boxY, boxWidth, 35, 4, 
            new Color(0.2f, 0.25f, 0.3f, 0.8f));
        TextStyle leftStyle = new TextStyle(new Color(0.5f, 1, 0.5f, 1))
            .withAlign(TextAlign.Horizontal.LEFT, TextAlign.Vertical.TOP);
        canvas.drawText("LEFT", font, refX, boxY + 8, leftStyle);
        
        // CENTER alignment
        canvas.fillRoundedRect(refX - boxWidth / 2, boxY + 42, boxWidth, 35, 4, 
            new Color(0.2f, 0.25f, 0.3f, 0.8f));
        TextStyle centerStyle = new TextStyle(new Color(1, 1, 0.5f, 1))
            .withAlign(TextAlign.Horizontal.CENTER, TextAlign.Vertical.TOP);
        canvas.drawText("CENTER", font, refX, boxY + 50, centerStyle);
        
        // RIGHT alignment
        canvas.fillRoundedRect(refX, boxY + 84, boxWidth, 35, 4, 
            new Color(0.2f, 0.25f, 0.3f, 0.8f));
        TextStyle rightStyle = new TextStyle(new Color(0.5f, 0.8f, 1, 1))
            .withAlign(TextAlign.Horizontal.RIGHT, TextAlign.Vertical.TOP);
        canvas.drawText("RIGHT", font, refX, boxY + 92, rightStyle);
    }

    /**
     * Demonstrate vertical alignment: TOP, MIDDLE, BASELINE, BOTTOM
     */
    private void demonstrateVerticalAlignment(float x, float y) {
        float spacing = 145;
        
        for (int i = 0; i < 4; i++) {
            float boxX = x + i * spacing;
            float boxY = y;
            float boxW = 130;
            float boxH = 100;
            float refY = boxY + boxH / 2;
            
            // Background box
            canvas.fillRoundedRect(boxX, boxY, boxW, boxH, 4, 
                new Color(0.2f, 0.25f, 0.3f, 0.8f));
            
            // Reference line (horizontal) to show alignment point
            canvas.fillRect(boxX, refY - 1, boxW, 2, new Color(1, 0, 0, 0.5f));
            
            TextAlign.Vertical vAlign;
            String label;
            Color color;
            
            switch (i) {
                case 0:
                    vAlign = TextAlign.Vertical.TOP;
                    label = "TOP";
                    color = new Color(0.5f, 1, 0.5f, 1);
                    canvas.fillRect(boxX, boxY - 1, boxW, 2, new Color(0, 1, 0, 0.8f));
                    break;
                case 1:
                    vAlign = TextAlign.Vertical.MIDDLE;
                    label = "MIDDLE";
                    color = new Color(1, 1, 0.5f, 1);
                    break;
                case 2:
                    vAlign = TextAlign.Vertical.BASELINE;
                    label = "BASE\nLINE";
                    color = new Color(1, 0.7f, 0.5f, 1);
                    break;
                case 3:
                default:
                    vAlign = TextAlign.Vertical.BOTTOM;
                    label = "BOTTOM";
                    color = new Color(0.5f, 0.8f, 1, 1);
                    canvas.fillRect(boxX, boxY + boxH - 1, boxW, 2, new Color(0, 0.5f, 1, 0.8f));
                    break;
            }
            
            TextStyle style = new TextStyle(color)
                .withAlign(TextAlign.Horizontal.CENTER, vAlign)
                .withStroke(new Color(0.2f, 0.2f, 0.2f, 1), 1.5f);
            canvas.drawText(label, font, boxX + boxW / 2, refY, style);
        }
    }

    /**
     * Demonstrate all 9 alignment combinations in a grid
     */
    private void demonstrate9GridAlignment(float x, float y) {
        float gridW = 600;
        float gridH = 150;
        float cellW = gridW / 3;
        float cellH = gridH / 3;
        
        // Draw grid background
        canvas.fillRoundedRect(x, y, gridW, gridH, 6, new Color(0.15f, 0.2f, 0.25f, 0.9f));
        
        // Draw grid lines
        for (int i = 1; i <= 2; i++) {
            canvas.fillRect(x + i * cellW, y, 1, gridH, new Color(0.3f, 0.3f, 0.4f, 0.5f));
            canvas.fillRect(x, y + i * cellH, gridW, 1, new Color(0.3f, 0.3f, 0.4f, 0.5f));
        }
        
        // Draw crosshairs at each cell center
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 3; col++) {
                float centerX = x + col * cellW + cellW / 2;
                float centerY = y + row * cellH + cellH / 2;
                
                // Crosshair
                canvas.fillRect(centerX - 10, centerY - 0.5f, 20, 1, new Color(1, 0, 0, 0.3f));
                canvas.fillRect(centerX - 0.5f, centerY - 10, 1, 20, new Color(1, 0, 0, 0.3f));
            }
        }
        
        // Labels for each position
        String[][] labels = {
            {"TOP-LEFT", "TOP-CENTER", "TOP-RIGHT"},
            {"MIDDLE-LEFT", "MIDDLE-CENTER", "MIDDLE-RIGHT"},
            {"BOTTOM-LEFT", "BOTTOM-CENTER", "BOTTOM-RIGHT"}
        };
        
        TextAlign.Horizontal[] hAligns = {
            TextAlign.Horizontal.LEFT, 
            TextAlign.Horizontal.CENTER, 
            TextAlign.Horizontal.RIGHT
        };
        
        TextAlign.Vertical[] vAligns = {
            TextAlign.Vertical.TOP, 
            TextAlign.Vertical.MIDDLE, 
            TextAlign.Vertical.BOTTOM
        };
        
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 3; col++) {
                float centerX = x + col * cellW + cellW / 2;
                float centerY = y + row * cellH + cellH / 2;
                
                TextStyle style = new TextStyle(new Color(0.8f, 0.9f, 1, 1))
                    .withAlign(hAligns[col], vAligns[row])
                    .withStroke(new Color(0.2f, 0.2f, 0.3f, 1), 1.2f);
                
                canvas.drawText(labels[row][col], font, centerX, centerY, style);
            }
        }
    }

    /**
     * Demonstrate multi-line text alignment
     */
    private void demonstrateMultilineAlignment(float x, float y) {
        String multiLine = "Line 1\nLine 2\nLine 3";
        float spacing = 200;
        float boxH = 80;
        float boxW = 180;
        
        // LEFT aligned
        float x1 = x + 50;
        canvas.fillRoundedRect(x1 - 10, y, boxW, boxH, 4, new Color(0.2f, 0.25f, 0.3f, 0.8f));
        canvas.fillRect(x1 - 1, y, 2, boxH, new Color(1, 0, 0, 0.3f));
        TextStyle leftMulti = new TextStyle(new Color(0.5f, 1, 0.5f, 1))
            .withAlign(TextAlign.Horizontal.LEFT, TextAlign.Vertical.TOP);
        canvas.drawText(multiLine, font, x1, y + 10, leftMulti);
        canvas.drawText("LEFT", font, x1, y + boxH + 5, new TextStyle(Color.GRAY));
        
        // CENTER aligned
        float x2 = x + spacing + 50;
        canvas.fillRoundedRect(x2 - boxW / 2, y, boxW, boxH, 4, new Color(0.2f, 0.25f, 0.3f, 0.8f));
        canvas.fillRect(x2 - 1, y, 2, boxH, new Color(1, 0, 0, 0.3f));
        TextStyle centerMulti = new TextStyle(new Color(1, 1, 0.5f, 1))
            .withAlign(TextAlign.Horizontal.CENTER, TextAlign.Vertical.TOP);
        canvas.drawText(multiLine, font, x2, y + 10, centerMulti);
        canvas.drawText("CENTER", font, x2, y + boxH + 5, 
            new TextStyle(Color.GRAY).withAlign(TextAlign.topCenter()));
        
        // RIGHT aligned
        float x3 = x + spacing * 2 + 50;
        canvas.fillRoundedRect(x3 - boxW + 10, y, boxW, boxH, 4, new Color(0.2f, 0.25f, 0.3f, 0.8f));
        canvas.fillRect(x3 - 1, y, 2, boxH, new Color(1, 0, 0, 0.3f));
        TextStyle rightMulti = new TextStyle(new Color(0.5f, 0.8f, 1, 1))
            .withAlign(TextAlign.Horizontal.RIGHT, TextAlign.Vertical.TOP);
        canvas.drawText(multiLine, font, x3, y + 10, rightMulti);
        canvas.drawText("RIGHT", font, x3, y + boxH + 5, 
            new TextStyle(Color.GRAY).withAlign(TextAlign.topRight()));
    }

    private void drawSection(String title, float x, float y) {
        canvas.fillRoundedRect(x, y, getVirtualWidth() - x * 2, 28, 4, 
            new Color(0.2f, 0.3f, 0.5f, 0.7f));
        TextStyle sectionStyle = new TextStyle(new Color(1, 1, 0.8f, 1))
            .withStroke(new Color(0.3f, 0.3f, 0.3f, 1), 1.5f);
        canvas.drawText(title, font, x + 10, y + 4, sectionStyle);
    }

    @Override
    public void dispose() {
        super.dispose();
        if (canvas != null) canvas.dispose();
        if (content != null) content.dispose();
    }

    public static void main(String[] args) {
        var settings = new WindowSettings(700, 850);
        settings.setTitle("Canvas Text Alignment Demo");
        settings.setWindowResizable(false);
        settings.setBackgroundColor(new Color(0.05f, 0.05f, 0.1f, 1.0f));
        settings.setMultisampling(4);
        
        CanvasTextAlignmentDemoA demo = new CanvasTextAlignmentDemoA(settings);
        demo.start();
    }
}
