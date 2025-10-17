/*
 * This software is available under Apache License
 * Copyright (c) 2020
 */

package org.pixel.demo.learning.canvas;

import org.pixel.commons.Color;
import org.pixel.commons.DeltaTime;
import org.pixel.commons.logger.Logger;
import org.pixel.commons.logger.LoggerFactory;
import org.pixel.commons.service.ServiceProvider;
import org.pixel.content.ContentManager;
import org.pixel.content.Texture;
import org.pixel.content.importer.settings.FontImporterSettings;
import org.pixel.core.Camera2D;
import org.pixel.core.WindowSettings;
import org.pixel.demo.learning.common.DemoGame;
import org.pixel.graphics.render.canvas.Canvas;
import org.pixel.graphics.render.canvas.GLCanvas;
import org.pixel.graphics.render.canvas.text.SdfFont;
import org.pixel.math.MathHelper;

/**
 * Demonstrates multi-texture batching with the Canvas image API.
 * <p>
 * Shows:
 * - Drawing images with the fluent API
 * - Tinting and alpha blending
 * - Multiple textures batched together
 * - Mixing shapes and images in the same batch
 */
public class CanvasImageDemo extends DemoGame {

    private static final Logger log = LoggerFactory.getLogger(CanvasImageDemo.class);

    private ContentManager content;
    private Canvas canvas;
    private Camera2D camera;
    private Texture earthTexture;
    private SdfFont font;

    private float time = 0;

    /**
     * Constructor.
     *
     * @param settings Game settings.
     */
    public CanvasImageDemo(WindowSettings settings) {
        super(settings);
    }

    @Override
    public void load() {
        super.load();

        content = ServiceProvider.get(ContentManager.class);

        // Load the earth texture
        earthTexture = content.load("images/earth-48x48.png", Texture.class);

        // Load font
        font = content.load("fonts/roboto-regular.ttf", SdfFont.class,
                new FontImporterSettings(16, 2));

        // Create canvas with fluent API
        canvas = new GLCanvas(getVirtualWidth(), getVirtualHeight());

        // Create camera
        camera = new Camera2D(this);

        log.info("Canvas Image Demo loaded!");
        log.info("This demo shows multi-texture batching with the fluent Canvas API");
    }

    @Override
    public void update(DeltaTime delta) {
        super.update(delta);
        time += delta.getElapsed();
    }

    @Override
    public void draw(DeltaTime delta) {
        super.draw(delta);

        // Begin canvas drawing
        canvas.begin(camera.getViewMatrix());

        // === Section 1: Basic Image Drawing ===
        drawText(10, 10, "Section 1: Basic Image Drawing");

        // Original size
        canvas.image(earthTexture, 50, 50).apply();

        // Scaled up
        canvas.image(earthTexture, 120, 50, 96, 96).apply();

        // Scaled down
        canvas.image(earthTexture, 240, 50, 24, 24).apply();

        // === Section 2: Tinting and Alpha ===
        drawText(10, 170, "Section 2: Tinting and Alpha");

        // Red tint
        canvas.image(earthTexture, 50, 210, 64, 64)
                .withTint(Color.RED).apply();

        // Blue tint
        canvas.image(earthTexture, 130, 210, 64, 64)
                .withTint(Color.BLUE).apply();

        // Green tint with alpha
        canvas.image(earthTexture, 210, 210, 64, 64)
                .withTint(Color.GREEN)
                .withAlpha(0.5f).apply();

        // Yellow tint, half transparent
        canvas.image(earthTexture, 290, 210, 64, 64)
                .withTint(Color.YELLOW)
                .withAlpha(0.7f).apply();

        // === Section 3: Multi-Texture Batching (Mixed with Shapes) ===
        drawText(10, 300, "Section 3: Mixed Batching (Shapes + Images)");

        // This demonstrates the power of the multi-texture batching system:
        // All these images AND shapes are batched together!

        float y = 340;
        for (int i = 0; i < 6; i++) {
            float x = 50 + i * 70;

            // Alternate between images and shapes
            if (i % 2 == 0) {
                // Draw image with animated tint
                float hue = (time + i * 0.3f) % 1.0f;
                // Simple color cycling using sine waves
                float r = 0.5f + 0.5f * MathHelper.sin(hue * 6.28f); // 2*PI
                float g = 0.5f + 0.5f * MathHelper.sin((hue + 0.33f) * 6.28f);
                float b = 0.5f + 0.5f * MathHelper.sin((hue + 0.67f) * 6.28f);
                Color tint = new Color(r, g, b, 1.0f);

                canvas.image(earthTexture, x, y, 48, 48)
                        .withTint(tint).apply();
            } else {
                // Draw circle (demonstrates mixed batching)
                canvas.circle(x + 24, y + 24, 20)
                        .withFill(new Color(0, 1, 1, 1)).apply(); // Cyan
            }
        }

        // === Section 4: Animated Images ===
        drawText(10, 420, "Section 4: Animated Scaling");

        for (int i = 0; i < 5; i++) {
            float x = 50 + i * 100;
            float scale = 1.0f + MathHelper.sin(time * 2 + i * 0.5f) * 0.3f;
            float size = 48 * scale;
            float offset = (48 - size) / 2; // Center the scaling

            canvas.image(earthTexture, x + offset, 460 + offset, size, size)
                    .withAlpha(0.5f + scale * 0.5f).apply();
        }

        // === Section 5: Overlapping with Shapes ===
        drawText(10, 540, "Section 5: Layering Test");

        // Background rectangle
        canvas.rect(50, 580, 200, 100)
                .withFill(new Color(0, 0, 0.5f, 1)).apply(); // Dark blue

        // Image on top
        canvas.image(earthTexture, 100, 600, 64, 64)
                .withAlpha(0.8f).apply();

        // Overlapping circle
        canvas.circle(170, 630, 30)
                .withFill(Color.RED)
                .withStroke(2, Color.WHITE)
                .apply();

        // Another image overlapping
        canvas.image(earthTexture, 150, 610, 48, 48)
                .withTint(Color.YELLOW)
                .withAlpha(0.9f).apply();

        // === Section 6: Rotation and Anchor ===
        drawText(10, 700, "Section 6: Rotation and Anchor");

        // No rotation, default anchor (top-left)
        canvas.rect(50, 740, 64, 64)
                .withStroke(1, new Color(1, 0, 0, 0.5f)).apply();
        canvas.image(earthTexture, 50, 740, 64, 64)
                .withAlpha(0.8f).apply();
        drawLabel("No rotation", 82, 755);

        // Rotate 45° around top-left (0, 0)
        canvas.rect(140, 740, 64, 64)
                .withStroke(1, new Color(1, 0, 0, 0.5f)).apply();
        canvas.image(earthTexture, 140, 740, 64, 64)
                .withRotation((float) Math.toRadians(45), 0.0f, 0.0f)
                .withAlpha(0.8f).apply();
        drawLabel("45° TL", 172, 755);

        // Rotate 45° around center (0.5, 0.5)
        canvas.rect(230, 740, 64, 64)
                .withStroke(1, new Color(1, 0, 0, 0.5f)).apply();
        canvas.image(earthTexture, 230, 740, 64, 64)
                .withRotation((float) Math.toRadians(45), 0.5f, 0.5f)
                .withAlpha(0.8f).apply();
        drawLabel("45° Center", 262, 755);

        // Animated rotation around center
        float animRotation = time * 2; // 2 radians per second
        canvas.rect(320, 740, 64, 64)
                .withStroke(1, new Color(1, 0, 0, 0.5f)).apply();
        canvas.image(earthTexture, 320, 740, 64, 64)
                .withRotation(animRotation, 0.5f, 0.5f)
                .withAlpha(0.8f).apply();
        drawLabel("Animated", 352, 755);

        // Rotation with right-center anchor
        canvas.rect(410, 740, 64, 64)
                .withStroke(1, new Color(1, 0, 0, 0.5f)).apply();
        canvas.image(earthTexture, 410, 740, 64, 64)
                .withRotation((float) Math.toRadians(30), 1.0f, 1.0f)
                .withAlpha(0.8f).apply();
        drawLabel("30° BR", 442, 755);

        canvas.end();
    }

    /**
     * Helper to draw section labels
     */
    private void drawText(float x, float y, String text) {
        // Draw background
        canvas.rect(x - 5, y - 7, 500, 30)
                .withFill(new Color(0, 0, 0, 0.7f)).apply();

        // Draw text
        if (font != null) {
            canvas.text(text, font, x, y)
                    .withFill(Color.WHITE).apply();
        }
    }

    /**
     * Helper to draw centered labels
     */
    private void drawLabel(String text, float centerX, float centerY) {
        if (font != null) {
            // Rough centering (would need text metrics for perfect centering)
            float approxWidth = text.length() * 7; // Approximate character width
            canvas.text(text, font, centerX - approxWidth / 2, centerY - 6)
                    .withFill(Color.WHITE).apply();
        }
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

    public static void main(String[] args) {
        var settings = new WindowSettings(640, 820);
        settings.setTitle("Canvas Image Demo - Multi-Texture Batching");
        settings.setWindowResizable(false);

        CanvasImageDemo game = new CanvasImageDemo(settings);
        game.start();
    }
}
