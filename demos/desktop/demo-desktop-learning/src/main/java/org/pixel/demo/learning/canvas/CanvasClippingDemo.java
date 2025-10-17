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
import org.pixel.graphics.render.canvas.TextAlign;
import org.pixel.graphics.render.canvas.text.SdfFont;

/**
 * Demo showcasing Canvas clipping functionality.
 * Tests rectangular clipping regions with save/restore.
 */
public class CanvasClippingDemo extends DemoGame {

    private Camera2D camera;
    private Canvas canvas;
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
        canvas = new GLCanvas(getVirtualWidth(), getVirtualHeight());
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
        canvas.text("Canvas Clipping Demo", font, 20, 20)
                .withFill(Color.WHITE)
                .apply();

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
        canvas.rect(20, 60, 370, 120)
                .withRoundedCorners(10)
                .withFill(new Color(0.15f, 0.15f, 0.2f, 0.9f))
                .apply();
        canvas.text("Demo 1: Basic Clipping", font, 30, 70)
                .withFill(Color.WHITE)
                .apply();

        // Define clip region
        float clipX = 30;
        float clipY = 95;
        float clipW = 150;
        float clipH = 70;

        // Show clip boundary
        canvas.rect(clipX, clipY, clipW, clipH)
                .withStroke(2, new Color(1, 1, 0, 0.5f))
                .apply();

        // Apply clipping
        canvas.clipRect(clipX, clipY, clipW, clipH);

        // Draw shapes that extend beyond clip region
        canvas.circle(60, 130, 40)
                .withFill(new Color(0.8f, 0.3f, 0.3f, 1.0f))
                .apply();
        canvas.circle(120, 130, 40)
                .withFill(new Color(0.3f, 0.8f, 0.3f, 1.0f))
                .apply();
        canvas.rect(80, 110, 80, 60)
                .withFill(new Color(0.3f, 0.5f, 0.9f, 0.8f))
                .apply();

        // Reset clipping
        canvas.resetClip();

        // Label
        canvas.text("Only content inside\nyellow box is visible", font, 200, 120)
                .withFill(new Color(0.7f, 0.7f, 0.7f, 1.0f))
                .apply();
    }

    /**
     * Demo 2: Nested clipping with save/restore.
     */
    private void drawDemo2_NestedClipping() {
        // Background
        canvas.rect(20, 200, 370, 180)
                .withRoundedCorners(10)
                .withFill(new Color(0.15f, 0.15f, 0.2f, 0.9f))
                .apply();
        canvas.text("Demo 2: Nested Clipping", font, 30, 210)
                .withFill(Color.WHITE)
                .apply();

        // Outer clip region
        float outer1X = 30;
        float outer1Y = 240;
        canvas.rect(outer1X, outer1Y, 160, 120)
                .withStroke(2, new Color(1, 0, 0, 0.5f))
                .apply();

        canvas.save();
        canvas.clipRect(outer1X, outer1Y, 160, 120);

        // Fill background in outer clip
        canvas.rect(outer1X, outer1Y, 160, 120)
                .withFill(new Color(0.3f, 0.2f, 0.2f, 0.5f))
                .apply();

        // Inner clip region (intersection with outer)
        float inner1X = 60;
        float inner1Y = 270;
        canvas.rect(inner1X, inner1Y, 100, 60)
                .withStroke(2, new Color(0, 1, 0, 0.5f))
                .apply();

        canvas.save();
        canvas.clipRect(inner1X, inner1Y, 100, 60);

        // This circle is clipped by the INTERSECTION of both regions
        canvas.circle(110, 300, 50)
                .withFill(new Color(0.8f, 0.6f, 0.2f, 1.0f))
                .apply();

        canvas.restore(); // Back to outer clip

        // This text is only clipped by outer (red) region
        canvas.text("Outer clip only", font, 110, 340)
                .withAlign(TextAlign.MIDDLE_CENTER)
                .withFill(Color.WHITE)
                .apply();

        canvas.restore(); // No clipping

        // Label
        canvas.text("Red = outer\nGreen = inner\nClips intersect!", font, 210, 280)
                .withFill(new Color(0.7f, 0.7f, 0.7f, 1.0f))
                .apply();
    }

    /**
     * Demo 3: Scrolling content with clipping.
     */
    private void drawDemo3_ScrollingContent() {
        // Background
        canvas.rect(410, 60, 370, 180)
                .withRoundedCorners(10)
                .withFill(new Color(0.15f, 0.15f, 0.2f, 0.9f))
                .apply();
        canvas.text("Demo 3: Scrolling Content", font, 420, 70)
                .withFill(Color.WHITE)
                .apply();

        // Scrollable area boundary
        float scrollX = 420;
        float scrollY = 100;
        float scrollW = 350;
        float scrollH = 120;

        canvas.rect(scrollX, scrollY, scrollW, scrollH)
                .withStroke(2, new Color(0, 1, 1, 0.5f))
                .apply();

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

            canvas.rect(x, y, 60, 60)
                    .withRoundedCorners(10)
                    .withFill(color)
                    .apply();

            // Center the text in the box
            String num = "" + (i + 1);
            canvas.text(num, font, x + 30, y + 30)
                    .withFill(Color.WHITE)
                    .withAlign(TextAlign.MIDDLE_CENTER)
                    .apply();
        }

        canvas.restore();
        canvas.resetClip();

        canvas.text("<- Content scrolls, clipped by cyan box ->", font, 440, 220)
                .withFill(new Color(0.7f, 0.7f, 0.7f, 1.0f))
                .apply();
    }

    /**
     * Demo 4: Animated expanding/contracting clip region.
     */
    private void drawDemo4_AnimatedClipping() {
        // Background
        canvas.rect(410, 260, 370, 120)
                .withRoundedCorners(10)
                .withFill(new Color(0.15f, 0.15f, 0.2f, 0.9f))
                .apply();
        canvas.text("Demo 4: Animated Clipping", font, 420, 270)
                .withFill(Color.WHITE)
                .apply();

        // Animated clip size
        float clipSize = 50 + (float) Math.abs(Math.sin(time * 2)) * 100;
        float clipX = 520;
        float clipY = 305;

        // Show animated clip boundary
        canvas.rect(clipX, clipY, clipSize, clipSize)
                .withRoundedCorners(8)
                .withStroke(3, new Color(1, 0, 1, 0.8f))
                .apply();

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

                canvas.circle(px + 15, py + 15, 12)
                        .withFill(color)
                        .apply();
            }
        }

        canvas.resetClip();

        canvas.text("Clip region\nexpands/contracts", font, 640, 320)
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
        settings.setTitle("Canvas Clipping Demo");
        settings.setWindowResizable(false);

        CanvasClippingDemo demo = new CanvasClippingDemo(settings);
        demo.start();
    }
}
