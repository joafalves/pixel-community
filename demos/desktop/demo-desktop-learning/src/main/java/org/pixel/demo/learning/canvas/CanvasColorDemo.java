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
import org.pixel.graphics.render.canvas.Canvas;
import org.pixel.graphics.render.canvas.GLCanvas;
import org.pixel.graphics.render.canvas.text.SdfFont;

/**
 * Canvas Color Demo - Showcasing animated color effects and gradients.
 * Features rainbow gradients, pulsing colors, color wheels, and animated transitions.
 */
public class CanvasColorDemo extends DemoGame {

    private Canvas canvas;
    private ContentManager content;
    private SdfFont font;
    private SdfFont titleFont;
    private float time = 0;

    public CanvasColorDemo(WindowSettings settings) {
        super(settings);
    }

    @Override
    public void load() {
        super.load();

        // Create canvas
        canvas = new GLCanvas(getViewportWidth(), getViewportHeight());

        // Load content
        content = ServiceProvider.get(ContentManager.class);
        font = content.load("fonts/roboto-regular.ttf", SdfFont.class,
                new FontImporterSettings(15, 3));
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
        canvas.text("Canvas Color & Gradient Demo", titleFont, 20, 20)
            .withFill(Color.WHITE)
            .apply();
        canvas.text("Animated color transitions, gradients, and color theory", 
                       font, 20, 50)
            .withFill(new Color(0.7f, 0.7f, 0.7f, 1.0f))
            .apply();

        // Demo 1: Rainbow Gradient Bar (Horizontal)
        drawDemo1_RainbowGradient();

        // Demo 2: Pulsing Color Circles
        drawDemo2_PulsingCircles();

        // Demo 3: Color Wheel
        drawDemo3_ColorWheel();

        // Demo 4: Animated Gradient Wave
        drawDemo4_GradientWave();

        // Demo 5: RGB Color Mixer
        drawDemo5_RGBMixer();

        // Demo 6: Hue Rotation Animation
        drawDemo6_HueRotation();

        canvas.end();
    }

    /**
     * Demo 1: Animated rainbow gradient bar.
     */
    private void drawDemo1_RainbowGradient() {
        float x = 30;
        float y = 100;
        float width = 840;
        float height = 40;

        canvas.text("Rainbow Gradient (GPU-Accelerated, Animated)", font, x, y - 20)
            .withFill(Color.YELLOW)
            .apply();

        // NEW: Draw gradient as multiple quad segments for multi-stop rainbow
        // Much more efficient than 100 strips - only 6 quads!
        Color[] colors = new Color[7];
        for (int i = 0; i < 7; i++) {
            float hue = ((float) i / 6 + time * 0.1f) % 1.0f;
            colors[i] = hsvToRgb(hue, 1.0f, 1.0f);
        }
        
        // Draw 6 gradient segments
        for (int i = 0; i < 6; i++) {
            float segmentX = x + (width * i / 6);
            float segmentWidth = width / 6;
            
            // Use linear gradient for smooth transitions
            canvas.rect(segmentX, y, segmentWidth, height)
                .withFillLinearGradient(0, colors[i], colors[i + 1])
                .apply();
        }

        // Border
        canvas.rect(x, y, width, height)
            .withStroke(2, Color.WHITE)
            .apply();
    }

    /**
     * Demo 2: Pulsing color circles with different phases.
     */
    private void drawDemo2_PulsingCircles() {
        float y = 160;
        canvas.text("Pulsing Colors (Different Phases)", font, 30, y - 10)
            .withFill(Color.SKY)
            .apply();

        float[] phases = {0, 0.33f, 0.66f, 1.0f, 1.33f};
        
        for (int i = 0; i < 5; i++) {
            float centerX = 110 + i * 150;
            float centerY = y + 50;
            
            // Pulsing animation
            float pulse = 0.7f + 0.3f * (float) Math.sin(time * 3 + phases[i] * Math.PI * 2);
            float radius = 30 * pulse;
            
            // Color based on phase
            float hue = (phases[i] * 0.6f) % 1.0f;
            Color edgeColor = hsvToRgb(hue, 1.0f, 1.0f);
            edgeColor.setAlpha(0.9f);
            
            // NEW: Use radial gradient from bright center to saturated edge
            Color centerColor = new Color(1.0f, 1.0f, 1.0f, 0.8f); // White center
            canvas.circle(centerX, centerY, radius)
                .withFillRadialGradient(centerColor, edgeColor)
                .apply();
            
            canvas.circle(centerX, centerY, radius)
                .withStroke(2, Color.WHITE)
                .apply();
        }
    }

    /**
     * Demo 3: Rotating color wheel.
     */
    private void drawDemo3_ColorWheel() {
        float centerX = 120;
        float centerY = 350;
        float radius = 60;

        canvas.text("Color Wheel (Rotating)", font, centerX - 68, centerY + 90)
            .withFill(new Color(1, 0, 1, 1))
            .apply();

        canvas.save();
        canvas.translate(centerX, centerY);
        canvas.rotate(time * 0.5f);
        canvas.translate(-centerX, -centerY);

        // Draw color wheel as segments
        int segments = 60;
        for (int i = 0; i < segments; i++) {
            float angle1 = (float) (2 * Math.PI * i / segments);
            float angle2 = (float) (2 * Math.PI * (i + 1) / segments);
            
            float hue = (float) i / segments;
            Color color = hsvToRgb(hue, 1.0f, 1.0f);
            
            // Draw triangle segment
            canvas.path()
                .moveTo(centerX, centerY)
                .lineTo(centerX + radius * (float) Math.cos(angle1),
                         centerY + radius * (float) Math.sin(angle1))
                .lineTo(centerX + radius * (float) Math.cos(angle2),
                         centerY + radius * (float) Math.sin(angle2))
                .closePath()
                .withFill(color)
                .apply();
        }

        // White center circle
        canvas.circle(centerX, centerY, 15)
            .withFill(Color.WHITE)
            .apply();

        canvas.restore();
    }

    /**
     * Demo 4: Animated gradient wave effect.
     */
    private void drawDemo4_GradientWave() {
        float x = 280;
        float y = 280;
        float width = 590;
        float height = 120;

        canvas.text("Gradient Wave Animation (NEW: 4-Corner Gradients)", font, x, y - 20)
            .withFill(Color.GREEN)
            .apply();

        // NEW: Draw vertical gradient strips with animated colors
        int strips = 10;
        for (int i = 0; i < strips; i++) {
            float stripX = x + (width * i / strips);
            float stripWidth = width / strips;
            
            // Wave effect - creates vertical color bands that move
            float t = (float) i / strips;
            float wave1 = (float) Math.sin(t * Math.PI * 2 + time * 2) * 0.5f + 0.5f;
            float wave2 = (float) Math.sin(t * Math.PI * 2 + time * 2 + Math.PI) * 0.5f + 0.5f;
            
            // Top colors
            Color topColor1 = hsvToRgb(0.5f + wave1 * 0.3f, 0.8f, 1.0f);
            Color topColor2 = hsvToRgb(0.5f + wave2 * 0.3f, 0.8f, 1.0f);
            
            // Bottom colors (shifted)
            Color bottomColor1 = hsvToRgb(0.8f + wave1 * 0.2f, 0.9f, 0.9f);
            Color bottomColor2 = hsvToRgb(0.8f + wave2 * 0.2f, 0.9f, 0.9f);
            
            // Draw 4-corner gradient quad
            canvas.rect(stripX, y, stripWidth, height)
                .withFillGradient(topColor1, topColor2, bottomColor2, bottomColor1)
                .apply();
        }

        // Border
        canvas.rect(x, y, width, height)
            .withStroke(2, Color.WHITE)
            .apply();
    }

    /**
     * Demo 5: RGB color mixer - animated mixing of primary colors.
     */
    private void drawDemo5_RGBMixer() {
        float baseX = 280;
        float baseY = 430;
        float size = 60;

        canvas.text("RGB Color Mixing (Animated)", font, baseX, baseY - 20)
            .withFill(new Color(0, 1, 1, 1))
            .apply();

        // Animated RGB values
        float r = (float) Math.sin(time * 1.2f) * 0.5f + 0.5f;
        float g = (float) Math.sin(time * 1.5f) * 0.5f + 0.5f;
        float b = (float) Math.sin(time * 1.8f) * 0.5f + 0.5f;

        // Draw individual color components
        canvas.rect(baseX, baseY, size, size)
            .withRoundedCorners(10)
            .withFill(new Color(r, 0, 0, 0.8f))
            .apply();
        canvas.text("R", font, baseX + 25, baseY + 70)
            .withFill(Color.RED)
            .apply();

        canvas.rect(baseX + 80, baseY, size, size)
            .withRoundedCorners(10)
            .withFill(new Color(0, g, 0, 0.8f))
            .apply();
        canvas.text("G", font, baseX + 105, baseY + 70)
            .withFill(Color.GREEN)
            .apply();

        canvas.rect(baseX + 160, baseY, size, size)
            .withRoundedCorners(10)
            .withFill(new Color(0, 0, b, 0.8f))
            .apply();
        canvas.text("B", font, baseX + 185, baseY + 70)
            .withFill(Color.BLUE)
            .apply();

        // Draw mixed result
        canvas.text("+", font, baseX + 235, baseY + 25)
            .withFill(Color.WHITE)
            .apply();
        canvas.rect(baseX + 260, baseY, size, size)
            .withRoundedCorners(10)
            .withFill(new Color(r, g, b, 1.0f))
            .apply();
        canvas.rect(baseX + 260, baseY, size, size)
            .withRoundedCorners(10)
            .withStroke(2, Color.WHITE)
            .apply();
        canvas.text("Mix", font, baseX + 275, baseY + 70)
            .withFill(Color.WHITE)
            .apply();
    }

    /**
     * Demo 6: Hue rotation through squares.
     */
    private void drawDemo6_HueRotation() {
        float startX = 280;
        float y = 540;
        float size = 50;
        int count = 5;

        canvas.text("Hue Rotation", font, startX, y - 20)
            .withFill(new Color(1, 0.5f, 0, 1))
            .apply();

        for (int i = 0; i < count; i++) {
            float x = startX + i * 65;
            
            // Rotating hue with offset for each square
            float hue = (time * 0.3f + (float) i / count) % 1.0f;
            Color color = hsvToRgb(hue, 0.9f, 1.0f);
            
            // Add slight scale animation
            float scale = 1.0f + 0.1f * (float) Math.sin(time * 2 + i * 0.5f);
            float scaledSize = size * scale;
            float offset = (size - scaledSize) / 2;
            
            canvas.rect(x + offset, y + offset, scaledSize, scaledSize)
                .withRoundedCorners(8)
                .withFill(color)
                .apply();
            canvas.rect(x + offset, y + offset, scaledSize, scaledSize)
                .withRoundedCorners(8)
                .withStroke(2, Color.WHITE)
                .apply();
        }
    }

    /**
now      * Convert HSV to RGB color.
     * 
     * @param h Hue [0-1]
     * @param s Saturation [0-1]
     * @param v Value [0-1]
     * @return RGB Color
     */
    private Color hsvToRgb(float h, float s, float v) {
        float c = v * s;
        float x = c * (1 - Math.abs((h * 6) % 2 - 1));
        float m = v - c;

        float r, g, b;
        int sector = (int) (h * 6);
        
        switch (sector) {
            case 0:  r = c; g = x; b = 0; break;
            case 1:  r = x; g = c; b = 0; break;
            case 2:  r = 0; g = c; b = x; break;
            case 3:  r = 0; g = x; b = c; break;
            case 4:  r = x; g = 0; b = c; break;
            default: r = c; g = 0; b = x; break;
        }

        return new Color(r + m, g + m, b + m, 1.0f);
    }

    @Override
    public void dispose() {
        super.dispose();
        if (canvas != null) {
            canvas.dispose();
        }
    }

    public static void main(String[] args) {
        var settings = new WindowSettings(900, 600);
        settings.setTitle("Canvas Color & Gradient Demo");
        settings.setWindowResizable(false);
        settings.setMultisampling(4);
        settings.setVsync(false);
        settings.setBackgroundColor(new Color(0.05f, 0.05f, 0.1f, 1.0f));

        CanvasColorDemo demo = new CanvasColorDemo(settings);
        demo.start();
    }
}
