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
import org.pixel.graphics.render.NinePatch;
import org.pixel.graphics.render.canvas.Canvas;
import org.pixel.graphics.render.canvas.GLCanvas;
import org.pixel.graphics.render.canvas.text.SdfFont;
import org.pixel.math.MathHelper;

/**
 * Demonstrates 9-patch rendering with the Canvas API.
 *
 * <p>Shows:
 * <ul>
 *   <li>Creating 9-patches from textures with different border sizes</li>
 *   <li>Rendering 9-patches at various sizes (small, medium, large)</li>
 *   <li>Animated scaling demonstrating intelligent border preservation</li>
 *   <li>Using 9-patches for UI panels and buttons</li>
 * </ul>
 */
public class CanvasNinePatchDemo extends DemoGame {

    private static final Logger log = LoggerFactory.getLogger(CanvasNinePatchDemo.class);

    private ContentManager content;
    private Canvas canvas;
    private Camera2D camera;
    private SdfFont font;

    // Textures for 9-patches
    private Texture texture;

    // 9-patches
    private NinePatch panelPatch;
    private NinePatch buttonPatch;
    private NinePatch thinBorderPatch;

    private float time = 0;

    /**
     * Constructor.
     *
     * @param settings Game settings.
     */
    public CanvasNinePatchDemo(WindowSettings settings) {
        super(settings);
    }

    @Override
    public void load() {
        super.load();

        content = ServiceProvider.get(ContentManager.class);

        // Load textures
        texture = content.load("images/window-bg-128x128.png", Texture.class);

        // Load font
        font = content.load("fonts/roboto-medium.ttf", SdfFont.class,
                new FontImporterSettings(16, 2));

        // Create 9-patches with different border configurations
        // Panel: thick borders (8px all sides)
        panelPatch = new NinePatch(texture, 8);

        // Button: medium borders (4px all sides)
        buttonPatch = new NinePatch(texture, 8);

        // Thin border: asymmetric borders
        thinBorderPatch = new NinePatch(texture, 2, 2, 2, 2);

        // Create canvas
        canvas = new GLCanvas(getViewportWidth(), getViewportHeight());

        // Create camera
        camera = new Camera2D(this);

        log.info("Canvas 9-Patch Demo loaded!");
        log.info("Demonstrating intelligent texture scaling with border preservation");
    }

    @Override
    public void update(DeltaTime delta) {
        super.update(delta);
        time += delta.getElapsed();
    }

    @Override
    public void draw(DeltaTime delta) {
        super.draw(delta);

        canvas.begin(camera.getViewMatrix());

        // === Section 1: Different Sizes ===
        drawText(10, 10, "Section 1: Different Sizes (same 9-patch)");

        // Small panel
        canvas.ninePatch(panelPatch, 50, 50, 80, 60);
        drawLabel("Small", 90, 75);

        // Medium panel
        canvas.ninePatch(panelPatch, 150, 50, 150, 100);
        drawLabel("Medium", 225, 90);

        // Large panel
        canvas.ninePatch(panelPatch, 320, 50, 250, 150);
        drawLabel("Large", 445, 115);

        // === Section 2: Different Border Sizes ===
        drawText(10, 230, "Section 2: Different Border Configurations");

        // Thick borders (panelPatch - 8px)
        canvas.ninePatch(panelPatch, 50, 270, 120, 80);
        drawLabel("8px borders", 110, 300);

        // Medium borders (buttonPatch - 6px)
        canvas.ninePatch(buttonPatch, 200, 270, 120, 80);
        drawLabel("6px borders", 260, 300);

        // Thin borders (2px)
        canvas.ninePatch(thinBorderPatch, 350, 270, 120, 80);
        drawLabel("2px borders", 410, 300);

        // === Section 3: Animated Scaling ===
        drawText(10, 380, "Section 3: Animated Scaling");

        // Oscillating width
        float animWidth = 100 + MathHelper.sin(time * 2) * 50;
        canvas.ninePatch(panelPatch, 50, 420, animWidth, 80);
        drawLabel("Width", 50 + animWidth / 2, 450);

        // Oscillating height
        float animHeight = 80 + MathHelper.sin(time * 2 + 1.5f) * 40;
        canvas.ninePatch(panelPatch, 230, 420, 100, animHeight);
        drawLabel("Height", 280, 420 + animHeight / 2);

        // Both directions
        float animBoth = 80 + MathHelper.sin(time * 1.5f) * 30;
        canvas.ninePatch(panelPatch, 380, 420, animBoth, animBoth);
        drawLabel("Both", 380 + animBoth / 2, 420 + animBoth / 2);

        // === Section 4: UI Elements ===
        drawText(10, 530, "Section 4: UI Panel & Buttons");

        // Large panel background
        canvas.ninePatch(panelPatch, 50, 570, 520, 120);

        // Three buttons on the panel
        for (int i = 0; i < 3; i++) {
            float btnX = 70 + i * 170;
            float btnY = 590;

            // Button background (earth texture 9-patch)
            canvas.ninePatch(buttonPatch, btnX, btnY, 140, 40);

            // Button label
            String[] labels = {"Start", "Options", "Exit"};
            drawLabel(labels[i], btnX + 70, btnY + 18);
        }

        // Panel title
        if (font != null) {
            canvas.text("Game Menu", font, 260, 645)
                    .withFill(Color.WHITE);
        }

        canvas.end();
    }

    /**
     * Helper to draw section labels
     */
    private void drawText(float x, float y, String text) {
        // Draw background
        canvas.rect(x - 5, y - 5, 520, 25)
                .withFill(new Color(0, 0, 0, 0.8f))
                .apply();

        // Draw text
        if (font != null) {
            canvas.text(text, font, x, y)
                    .withFill(Color.WHITE)
                    .apply();
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
                    .withFill(Color.WHITE)
                    .apply();
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
        var settings = new WindowSettings(640, 720);
        settings.setTitle("Canvas 9-Patch Demo - Intelligent Texture Scaling");
        settings.setWindowResizable(false);

        CanvasNinePatchDemo game = new CanvasNinePatchDemo(settings);
        game.start();
    }
}
