package org.pixel.demo.learning.decs.system;

import org.pixel.commons.Color;
import org.pixel.commons.DeltaTime;
import org.pixel.commons.service.ServiceProvider;
import org.pixel.content.ContentManager;
import org.pixel.content.importer.settings.FontImporterSettings;
import org.pixel.core.Camera2D;
import org.pixel.core.Game;
import org.pixel.demo.learning.decs.component.HealthComponent;
import org.pixel.demo.learning.decs.component.PlayerGameComponent;
import org.pixel.ext.decs.GameEntity;
import org.pixel.ext.decs.GameGroup;
import org.pixel.ext.decs.GameSystem;
import org.pixel.ext.decs.GameWorld;
import org.pixel.graphics.render.canvas.CanvasRenderer;
import org.pixel.graphics.render.canvas.TextStyle;
import org.pixel.graphics.render.canvas.text.SdfFont;
import org.pixel.math.Size;
import org.pixel.math.Vector2;

public class NeonHudSystem extends GameSystem {

    private final GameWorld world;
    private final CanvasRenderer canvas;
    private final Game game;
    private final ScoreSystem scoreSystem;
    private final WaveSystem waveSystem;

    private Camera2D camera;
    private SdfFont font;
    private GameGroup players;

    private boolean isPaused = false;

    public NeonHudSystem(GameWorld world, CanvasRenderer canvas, Game game,
                         ScoreSystem scoreSystem, WaveSystem waveSystem) {
        this.world = world;
        this.canvas = canvas;
        this.game = game;
        this.scoreSystem = scoreSystem;
        this.waveSystem = waveSystem;
    }

    @Override
    public void load() {
        this.camera = world.getData().get(Camera2D.class);
        this.players = world.getGroup(PlayerGameComponent.class, HealthComponent.class);

        // Load font for HUD
        this.font = ServiceProvider.get(ContentManager.class)
                .load("fonts/roboto-regular.ttf", SdfFont.class, new FontImporterSettings(32, 1));
    }

    @Override
    public void draw(DeltaTime delta) {
        // Use screen-space rendering (no camera transform)
        canvas.begin();

        // Draw HUD elements
        drawScore();
        drawWave();
        drawPlayerHealth();

        if (isPaused) {
            drawPausedOverlay();
        }

        canvas.end();
    }

    private void drawFPS() {
        // Top-left corner - compact design
        float x = 10;
        float y = 10;
        float width = 90;
        float height = 28;

        // Glowing background
        canvas.fillRoundedRect(x - 2, y - 2, width + 4, height + 4, 6, new Color(0, 1f, 0, 0.3f));

        // Dark background with gradient
        canvas.fillRectGradient(x, y, width, height,
            new Color(0.05f, 0.15f, 0.05f, 0.85f), new Color(0.05f, 0.15f, 0.05f, 0.85f),
            new Color(0.1f, 0.2f, 0.1f, 0.85f), new Color(0.1f, 0.2f, 0.1f, 0.85f));
        canvas.fillRoundedRect(x, y, width, height, 5, new Color(0, 0, 0, 0));

        // Border
        canvas.strokeRoundedRect(x, y, width, height, 5, 2, new Color(0.3f, 1f, 0.3f, 0.8f));

        // FPS text
        String fpsText = game.getFps() + " FPS";
        TextStyle style = new TextStyle(new Color(0.5f, 1f, 0.5f)).setStroke(new Color(0, 0.3f, 0), 2);

        canvas.save();
        canvas.scale(0.4f, 0.4f);
        canvas.drawText(fpsText, font, (x + 8) / 0.4f, (y + 7) / 0.4f, style);
        canvas.restore();
    }

    private void drawScore() {
        // Top-right corner
        float width = 280;
        float height = 70;
        float x = 800 - width - 15;
        float y = 15;

        // Animated glow pulse
        float pulse = (float) Math.sin(System.currentTimeMillis() / 500.0) * 0.2f + 0.4f;
        canvas.fillRoundedRect(x - 4, y - 4, width + 8, height + 8, 12, new Color(1f, 0.8f, 0, pulse));

        // Background with gradient
        canvas.fillRectGradient(x, y, width, height,
            new Color(0.1f, 0.08f, 0.02f, 0.95f), new Color(0.1f, 0.08f, 0.02f, 0.95f),
            new Color(0.15f, 0.12f, 0.03f, 0.95f), new Color(0.15f, 0.12f, 0.03f, 0.95f));
        canvas.fillRoundedRect(x, y, width, height, 10, new Color(0, 0, 0, 0));

        // Shiny top highlight
        canvas.fillRectGradient(x, y, width, height * 0.4f,
            new Color(1f, 1f, 1f, 0.2f), new Color(1f, 1f, 1f, 0.2f),
            new Color(1f, 1f, 1f, 0f), new Color(1f, 1f, 1f, 0f));

        // Border
        canvas.strokeRoundedRect(x, y, width, height, 10, 3, new Color(1f, 0.9f, 0.2f, 0.95f));

        // Score value - BIG
        String scoreText = String.format("%,d", scoreSystem.getScore());
        TextStyle scoreStyle = new TextStyle(new Color(1f, 1f, 0.3f)).setStroke(new Color(0.3f, 0.25f, 0), 4);

        canvas.save();
        canvas.scale(1.0f, 1.0f);
        canvas.drawText(scoreText, font, x + 15, y + 12, scoreStyle);
        canvas.restore();
    }

    private void drawWave() {
        // Top-left corner
        float width = 250;
        float height = 70;
        float x = 15;
        float y = 15;

        // Cyan glow
        canvas.fillRoundedRect(x - 4, y - 4, width + 8, height + 8, 12, new Color(0, 1f, 1f, 0.35f));

        // Dark background
        canvas.fillRectGradient(x, y, width, height,
            new Color(0.02f, 0.08f, 0.1f, 0.95f), new Color(0.02f, 0.08f, 0.1f, 0.95f),
            new Color(0.05f, 0.12f, 0.15f, 0.95f), new Color(0.05f, 0.12f, 0.15f, 0.95f));
        canvas.fillRoundedRect(x, y, width, height, 10, new Color(0, 0, 0, 0));

        // Highlight
        canvas.fillRectGradient(x, y, width, height * 0.4f,
            new Color(1f, 1f, 1f, 0.15f), new Color(1f, 1f, 1f, 0.15f),
            new Color(1f, 1f, 1f, 0f), new Color(1f, 1f, 1f, 0f));

        // Border
        canvas.strokeRoundedRect(x, y, width, height, 10, 3, new Color(0.3f, 1f, 1f, 0.95f));

        // Wave number - BIG
        String waveText = "WAVE " + waveSystem.getCurrentWave();
        TextStyle waveStyle = new TextStyle(new Color(0.3f, 1f, 1f)).setStroke(new Color(0, 0.3f, 0.4f), 4);

        canvas.save();
        canvas.scale(0.8f, 0.8f);
        canvas.drawText(waveText, font, (x + 15) / 0.8f, (y + 10) / 0.8f, waveStyle);
        canvas.restore();

        // Enemy count
        String enemyText = waveSystem.getEnemiesRemaining() + " Enemies";
        TextStyle enemyStyle = new TextStyle(new Color(1f, 0.5f, 0.5f)).setStroke(new Color(0.4f, 0, 0), 3);

        canvas.save();
        canvas.scale(0.6f, 0.6f);
        canvas.drawText(enemyText, font, (x + 15) / 0.6f, (y + 40) / 0.6f, enemyStyle);
        canvas.restore();
    }

    private void drawPlayerHealth() {
        // Find player
        for (var player : players) {
            var health = world.getComponent(player, HealthComponent.class);

            // Almost full width bar at bottom
            float barWidth = 780;
            float barHeight = 12;
            float barX = 10;
            float barY = 600 - 25; // 25px from bottom

            float percentage = health.getHealthPercentage();

            // Outer glow/shadow layer (bigger than bar)
            float glowPadding = 3;
            Color glowColor;
            if (percentage > 0.6f) {
                glowColor = new Color(0, 1f, 0, 0.4f);
            } else if (percentage > 0.3f) {
                glowColor = new Color(1f, 0.8f, 0, 0.5f);
            } else {
                // Pulsing red glow when low health
                float pulse = (float) Math.sin(System.currentTimeMillis() / 150.0) * 0.3f + 0.5f;
                glowColor = new Color(1f, 0, 0, pulse);
            }
            canvas.fillRoundedRect(barX - glowPadding, barY - glowPadding,
                barWidth + (glowPadding * 2), barHeight + (glowPadding * 2), 8, glowColor);

            // Dark background
            canvas.fillRoundedRect(barX, barY, barWidth, barHeight, 6, new Color(0.1f, 0.1f, 0.15f, 0.9f));

            // Health fill with multi-layer gradient
            float fillWidth = barWidth * percentage;
            if (fillWidth > 0) {
                Color healthColorStart, healthColorEnd, healthColorMid;
                if (percentage > 0.6f) {
                    healthColorStart = new Color(0.2f, 1f, 0.4f); // Bright neon green
                    healthColorMid = new Color(0, 0.9f, 0.3f);
                    healthColorEnd = new Color(0, 0.7f, 0.2f);
                } else if (percentage > 0.3f) {
                    healthColorStart = new Color(1f, 1f, 0.2f); // Bright yellow
                    healthColorMid = new Color(1f, 0.8f, 0);
                    healthColorEnd = new Color(1f, 0.5f, 0); // Orange
                } else {
                    // Pulsing intensity when critical
                    float pulse = (float) Math.sin(System.currentTimeMillis() / 150.0) * 0.2f + 0.8f;
                    healthColorStart = new Color(1f * pulse, 0.2f * pulse, 0.2f * pulse);
                    healthColorMid = new Color(0.9f * pulse, 0, 0);
                    healthColorEnd = new Color(0.7f * pulse, 0, 0);
                }

                // Main gradient
                canvas.fillRoundedRect(barX, barY, fillWidth, barHeight, 6,
                    healthColorStart);

                // Additional gradient overlay for depth
                canvas.fillRectGradient(barX, barY, fillWidth, barHeight,
                    healthColorStart, healthColorStart, healthColorEnd, healthColorEnd);

                // Bright top highlight
                canvas.fillRectGradient(barX, barY, fillWidth, barHeight * 0.4f,
                    new Color(1f, 1f, 1f, 0.5f), new Color(1f, 1f, 1f, 0.5f),
                    new Color(1f, 1f, 1f, 0f), new Color(1f, 1f, 1f, 0f));

                // Shimmer effect - moving highlight
                float shimmerPos = ((System.currentTimeMillis() / 20) % (int)fillWidth);
                if (shimmerPos < fillWidth - 10) {
                    canvas.fillRectGradient(barX + shimmerPos, barY, 10, barHeight,
                        new Color(1f, 1f, 1f, 0f), new Color(1f, 1f, 1f, 0.3f),
                        new Color(1f, 1f, 1f, 0.3f), new Color(1f, 1f, 1f, 0f));
                }
            }

            // Bright border
            Color borderColor;
            if (percentage > 0.6f) {
                borderColor = new Color(0.3f, 1f, 0.5f, 0.9f);
            } else if (percentage > 0.3f) {
                borderColor = new Color(1f, 0.9f, 0.3f, 0.9f);
            } else {
                float pulse = (float) Math.sin(System.currentTimeMillis() / 150.0) * 0.3f + 0.7f;
                borderColor = new Color(1f, 0.3f, 0.3f, pulse);
            }
            canvas.strokeRoundedRect(barX, barY, barWidth, barHeight, 6, 2, borderColor);

            break; // Only draw for first player
        }
    }

    private void drawEntityCount() {
        // TODO: FIX
        /*int entityCount = world.getEntityCount();
        String text = "Entities: " + entityCount;
        TextStyle style = new TextStyle(new Color(0.7f, 0.7f, 0.7f)).setStroke(Color.BLACK, 2);

        canvas.save();
        canvas.scale(0.35f, 0.35f);
        canvas.drawText(text, font, 650, 10, style);
        canvas.restore();*/
    }

    private void drawPausedOverlay() {
        // Semi-transparent dark overlay
        canvas.fillRect(0, 0, 800, 600, new Color(0, 0, 0, 0.7f));

        // PAUSED text with glow
        String pausedText = "PAUSED";
        TextStyle style = new TextStyle(Color.WHITE).setStroke(new Color(0, 1f, 1f), 4);

        Size textSize = font.measureText(pausedText);
        float x = (800 - textSize.getWidth()) / 2;
        float y = (600 - textSize.getHeight()) / 2;

        canvas.drawText(pausedText, font, x, y, style);

        // Instructions
        String instructionText = "Press [P] to resume";
        TextStyle instructionStyle = new TextStyle(new Color(0.8f, 0.8f, 0.8f));

        canvas.save();
        canvas.scale(0.4f, 0.4f);
        Size instrSize = font.measureText(instructionText, instructionStyle, new Vector2(0.4f, 0.4f));
        float instrX = (800 - instrSize.getWidth()) / 2;
        canvas.drawText(instructionText, font, instrX, y + 60, instructionStyle);
        canvas.restore();
    }

    public void setPaused(boolean paused) {
        this.isPaused = paused;
    }
}
