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
import org.pixel.math.Vector2;

/**
 * Demo showcasing Canvas clipping functionality.
 * Tests rectangular clipping regions with save/restore.
 */
public class CanvasClippingDemo extends DemoGame {

    private Camera2D camera;
    private GlCanvasRenderer canvas;
    private ContentManager content;
    private SdfFont font;
    private float time = 0;

    public CanvasClippingDemo(WindowSettings settings) {
        super(settings);
    }

    @Override
    public void load() {
        super.load();

        camera = new Camera2D(this);
        canvas = new GlCanvasRenderer(getVirtualWidth(), getVirtualHeight());
        content = ServiceProvider.get(ContentManager.class);
        font = content.load("fonts/roboto-medium.ttf", SdfFont.class,
            new FontImporterSettings(18, 3));
    }

    @Override
    public void draw(DeltaTime delta) {
        super.draw(delta);

        if (font == null) return;

        time += delta.getElapsed();

        // Begin with camera's view matrix
        canvas.begin(camera.getViewMatrix());

        // Title
        canvas.drawText("Canvas Clipping Demo", font, 20, 20, Color.WHITE);

        // Demo 1: Basic clipping
        drawDemo1_BasicClipping();

        // Demo 2: Nested clipping with save/restore
        drawDemo2_NestedClipping();

        // Demo 3: Clipped scrolling content
        drawDemo3_ScrollingContent();

        // Demo 4: Animated clipping
        drawDemo4_AnimatedClipping();

        canvas.end();
    }

    /**
     * Demo 1: Basic rectangular clipping.
     */
    private void drawDemo1_BasicClipping() {
        // Background
        canvas.fillRoundedRect(20, 60, 370, 120, 10, new Color(0.15f, 0.15f, 0.2f, 0.9f));
        canvas.drawText("Demo 1: Basic Clipping", font, 30, 70, Color.WHITE);
        
        // Define clip region
        float clipX = 30;
        float clipY = 95;
        float clipW = 150;
        float clipH = 70;
        
        // Show clip boundary
        canvas.strokeRect(clipX, clipY, clipW, clipH, 2, new Color(1, 1, 0, 0.5f));
        
        // Apply clipping
        canvas.clipRect(clipX, clipY, clipW, clipH);
        
        // Draw shapes that extend beyond clip region
        canvas.fillCircle(60, 130, 40, new Color(0.8f, 0.3f, 0.3f, 1.0f));
        canvas.fillCircle(120, 130, 40, new Color(0.3f, 0.8f, 0.3f, 1.0f));
        canvas.fillRect(80, 110, 80, 60, new Color(0.3f, 0.5f, 0.9f, 0.8f));
        
        // Reset clipping
        canvas.resetClip();
        
        // Label
        canvas.drawText("Only content inside\nyellow box is visible", font, 200, 120,
            new Color(0.7f, 0.7f, 0.7f, 1.0f));
    }

    /**
     * Demo 2: Nested clipping with save/restore.
     */
    private void drawDemo2_NestedClipping() {
        // Background
        canvas.fillRoundedRect(20, 200, 370, 180, 10, new Color(0.15f, 0.15f, 0.2f, 0.9f));
        canvas.drawText("Demo 2: Nested Clipping", font, 30, 210, Color.WHITE);
        
        // Outer clip region
        float outer1X = 30;
        float outer1Y = 240;
        canvas.strokeRect(outer1X, outer1Y, 160, 120, 2, new Color(1, 0, 0, 0.5f));
        
        canvas.save();
        canvas.clipRect(outer1X, outer1Y, 160, 120);
        
        // Fill background in outer clip
        canvas.fillRect(outer1X, outer1Y, 160, 120, new Color(0.3f, 0.2f, 0.2f, 0.5f));
        
        // Inner clip region (intersection with outer)
        float inner1X = 60;
        float inner1Y = 270;
        canvas.strokeRect(inner1X, inner1Y, 100, 60, 2, new Color(0, 1, 0, 0.5f));
        
        canvas.save();
        canvas.clipRect(inner1X, inner1Y, 100, 60);
        
        // This circle is clipped by the INTERSECTION of both regions
        canvas.fillCircle(110, 300, 50, new Color(0.8f, 0.6f, 0.2f, 1.0f));
        
        canvas.restore(); // Back to outer clip
        
        // This text is only clipped by outer (red) region
        canvas.drawText("Outer clip only", font, 40, 340, Color.WHITE);
        
        canvas.restore(); // No clipping
        
        // Label
        canvas.drawText("Red = outer\nGreen = inner\nClips intersect!", font, 210, 280,
            new Color(0.7f, 0.7f, 0.7f, 1.0f));
    }

    /**
     * Demo 3: Scrolling content with clipping.
     */
    private void drawDemo3_ScrollingContent() {
        // Background
        canvas.fillRoundedRect(410, 60, 370, 180, 10, new Color(0.15f, 0.15f, 0.2f, 0.9f));
        canvas.drawText("Demo 3: Scrolling Content", font, 420, 70, Color.WHITE);
        
        // Scrollable area boundary
        float scrollX = 420;
        float scrollY = 100;
        float scrollW = 350;
        float scrollH = 120;
        
        canvas.strokeRect(scrollX, scrollY, scrollW, scrollH, 2, new Color(0, 1, 1, 0.5f));
        
        // Apply clipping to scrollable area
        canvas.clipRect(scrollX, scrollY, scrollW, scrollH);
        
        // Scrolling offset (animated)
        float scrollOffset = (float) Math.sin(time * 0.5f) * 100;
        
        canvas.save();
        canvas.translate(scrollOffset, 0);
        
        // Draw content that scrolls
        for (int i = 0; i < 8; i++) {
            float x = scrollX + 20 + i * 80;
            float y = scrollY + 30;
            
            Color color = new Color(
                0.5f + (float) Math.sin(i * 0.5f) * 0.5f,
                0.5f + (float) Math.cos(i * 0.7f) * 0.5f,
                0.7f,
                1.0f
            );
            
            canvas.fillRoundedRect(x, y, 60, 60, 10, color);
            
            // Center the text in the box
            String num = "" + (i + 1);
            Vector2 textPos = canvas.centerText(x, y, 60, 60, num, font);
            canvas.drawText(num, font, textPos.getX(), textPos.getY(), Color.WHITE);
        }
        
        canvas.restore();
        canvas.resetClip();
        
        canvas.drawText("<- Content scrolls, clipped by cyan box ->", font, 440, 220,
            new Color(0.7f, 0.7f, 0.7f, 1.0f));
    }

    /**
     * Demo 4: Animated expanding/contracting clip region.
     */
    private void drawDemo4_AnimatedClipping() {
        // Background
        canvas.fillRoundedRect(410, 260, 370, 120, 10, new Color(0.15f, 0.15f, 0.2f, 0.9f));
        canvas.drawText("Demo 4: Animated Clipping", font, 420, 270, Color.WHITE);
        
        // Animated clip size
        float clipSize = 50 + (float) Math.abs(Math.sin(time * 2)) * 100;
        float clipX = 520;
        float clipY = 305;
        
        // Show animated clip boundary
        canvas.strokeRoundedRect(clipX, clipY, clipSize, clipSize, 8, 3,
            new Color(1, 0, 1, 0.8f));
        
        // Apply animated clipping
        canvas.clipRect(clipX, clipY, clipSize, clipSize);
        
        // Draw pattern that gets clipped
        for (int y = 0; y < 3; y++) {
            for (int x = 0; x < 3; x++) {
                float px = clipX + x * 35;
                float py = clipY + y * 35;
                
                Color color = new Color(
                    (x + 1) / 3.0f,
                    (y + 1) / 3.0f,
                    0.5f,
                    1.0f
                );
                
                canvas.fillCircle(px + 15, py + 15, 12, color);
            }
        }
        
        canvas.resetClip();
        
        canvas.drawText("Clip region\nexpands/contracts", font, 640, 320,
            new Color(0.7f, 0.7f, 0.7f, 1.0f));
    }

    @Override
    public void dispose() {
        super.dispose();
        if (canvas != null) canvas.dispose();
        if (content != null) content.dispose();
    }

    public static void main(String[] args) {
        var settings = new WindowSettings(800, 400);
        settings.setTitle("Canvas Clipping Demo");
        settings.setWindowResizable(false);
        
        CanvasClippingDemo demo = new CanvasClippingDemo(settings);
        demo.start();
    }
}
