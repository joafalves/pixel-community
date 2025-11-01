package org.pixel.demo.learning.decs.system;

import org.pixel.commons.Color;
import org.pixel.commons.DeltaTime;
import org.pixel.core.Camera2D;
import org.pixel.demo.learning.decs.component.*;
import org.pixel.ext.decs.GameGroup;
import org.pixel.ext.decs.GameSystem;
import org.pixel.ext.decs.GameWorld;
import org.pixel.graphics.render.canvas.CanvasRenderer;
import org.pixel.input.mouse.Mouse;
import org.pixel.math.Vector2;

/**
 * Fancy neon-style rendering using Canvas API with gradients, shadows, and glow effects.
 */
public class NeonRenderSystem extends GameSystem {

    private final CanvasRenderer canvas;
    private final GameWorld world;
    private Camera2D camera;

    private GameGroup players;
    private GameGroup enemies;
    private GameGroup projectiles;
    private GameGroup particles;

    public NeonRenderSystem(GameWorld world, CanvasRenderer canvas) {
        this.world = world;
        this.canvas = canvas;
    }

    @Override
    public void load() {
        this.camera = world.getData().get(Camera2D.class);
        this.players = world.getGroup(PlayerGameComponent.class, PositionGameComponent.class);
        this.enemies = world.getGroup(EnemyComponent.class, PositionGameComponent.class);
        this.projectiles = world.getGroup(ProjectileComponent.class, PositionGameComponent.class);
        this.particles = world.getGroup(ParticleComponent.class, PositionGameComponent.class);
    }

    @Override
    public void draw(DeltaTime delta) {
        canvas.begin(camera.getViewMatrix());

        // Draw background grid effect
        drawBackgroundGrid();

        // Draw particles (bottom layer)
        for (var entity : particles) {
            drawParticle(entity);
        }

        // Draw projectiles with glow
        for (var entity : projectiles) {
            drawProjectile(entity);
        }

        // Draw enemies with health bars
        for (var entity : enemies) {
            drawEnemy(entity);
        }

        // Draw player with glow effect
        for (var entity : players) {
            drawPlayer(entity);
        }

        // Draw lighting overlay
        //drawLightingOverlay();

        canvas.end();
    }

    private void drawBackgroundGrid() {
        // Subtle grid lines
        Color gridColor = new Color(0.1f, 0.15f, 0.2f, 0.3f);
        float spacing = 50;

        for (float x = 0; x < 800; x += spacing) {
            canvas.strokeLine(x, 0, x, 600, 1, gridColor);
        }
        for (float y = 0; y < 600; y += spacing) {
            canvas.strokeLine(0, y, 800, y, 1, gridColor);
        }
    }

    private void drawPlayer(org.pixel.ext.decs.GameEntity entity) {
        var position = world.getComponent(entity, PositionGameComponent.class);
        var health = world.getComponent(entity, HealthComponent.class);

        float x = position.getPosition().getX() + 16;
        float y = position.getPosition().getY() + 16;

        // Calculate angle to mouse for aiming
        Vector2 mousePos = Mouse.getPosition();
        float dx = mousePos.getX() - x;
        float dy = mousePos.getY() - y;
        float angleToMouse = (float) Math.atan2(dy, dx);

        // Pulsing glow effect
        float glowSize = 20 + (float) Math.sin(System.currentTimeMillis() / 200.0) * 3;

        // Outer glow (radial gradient)
        Color glowOuter = new Color(0, 0.8f, 1f, 0);
        Color glowInner = new Color(0, 0.8f, 1f, 0.4f);
        canvas.fillCircleRadialGradient(x, y, glowSize, glowInner, glowOuter);

        // Main ship body (cyan with shadow)
        Color shipColor = new Color(0, 1f, 1f);
        if (health != null && health.isInvincible()) {
            // Flash white when invincible
            float flash = (System.currentTimeMillis() % 200) < 100 ? 1.0f : 0.3f;
            shipColor = new Color(flash, flash, flash);
        }

        canvas.fillCircle(x, y, 12, shipColor);

        // Inner highlight
        canvas.fillCircle(x - 3, y - 3, 4, new Color(0.5f, 1f, 1f, 0.6f));

        // Direction indicator pointing at mouse
        float indicatorLength = 15;
        float tipX = x + (float) Math.cos(angleToMouse) * indicatorLength;
        float tipY = y + (float) Math.sin(angleToMouse) * indicatorLength;
        canvas.strokeLine(x, y, tipX, tipY, 3, new Color(1f, 1f, 1f, 0.8f));

        // Draw aiming line to show bullet trajectory
        float aimLineDistance = 80;
        float aimEndX = x + (float) Math.cos(angleToMouse) * aimLineDistance;
        float aimEndY = y + (float) Math.sin(angleToMouse) * aimLineDistance;

        // Dashed aiming line with fade
        for (int i = 0; i < 5; i++) {
            float segmentStart = indicatorLength + (i * 15);
            float segmentEnd = segmentStart + 8;
            float alpha = 0.5f - (i * 0.08f);

            float sx = x + (float) Math.cos(angleToMouse) * segmentStart;
            float sy = y + (float) Math.sin(angleToMouse) * segmentStart;
            float ex = x + (float) Math.cos(angleToMouse) * segmentEnd;
            float ey = y + (float) Math.sin(angleToMouse) * segmentEnd;

            canvas.strokeLine(sx, sy, ex, ey, 1, new Color(0.3f, 0.8f, 1f, alpha));
        }

        // Crosshair at mouse position
        float crosshairSize = 6;
        canvas.strokeLine(mousePos.getX() - crosshairSize, mousePos.getY(),
                         mousePos.getX() + crosshairSize, mousePos.getY(), 1, new Color(0, 1f, 1f, 0.6f));
        canvas.strokeLine(mousePos.getX(), mousePos.getY() - crosshairSize,
                         mousePos.getX(), mousePos.getY() + crosshairSize, 1, new Color(0, 1f, 1f, 0.6f));
        canvas.strokeCircle(mousePos.getX(), mousePos.getY(), crosshairSize, 1, new Color(0, 1f, 1f, 0.4f));
    }

    private void drawEnemy(org.pixel.ext.decs.GameEntity entity) {
        var position = world.getComponent(entity, PositionGameComponent.class);
        var health = world.getComponent(entity, HealthComponent.class);
        var ai = world.getComponent(entity, AIComponent.class);
        var animation = world.getComponent(entity, AnimationComponent.class);

        float x = position.getPosition().getX() + 12;
        float y = position.getPosition().getY() + 12;

        // Enemy type-specific colors
        Color enemyColor;
        float size = 10;

        if (ai != null) {
            switch (ai.getType()) {
                case CHASER:
                    enemyColor = new Color(1f, 0.3f, 0.3f); // Red
                    size = 10;
                    break;
                case SHOOTER:
                    enemyColor = new Color(1f, 0.8f, 0); // Orange
                    size = 9;
                    break;
                case DASHER:
                    enemyColor = new Color(0.8f, 0, 1f); // Purple
                    size = 8;
                    break;
                default:
                    enemyColor = Color.RED;
            }
        } else {
            enemyColor = Color.RED;
        }

        // Apply pulse animation
        if (animation != null) {
            animation.update(0.016f);
            size *= animation.getScalePulse();
        }

        // Glow effect
        Color glowColor = new Color(enemyColor.getRed(), enemyColor.getGreen(),
                                     enemyColor.getBlue(), 0);
        Color dimmedColor = new Color(enemyColor.getRed() * 0.5f, enemyColor.getGreen() * 0.5f,
                                      enemyColor.getBlue() * 0.5f, enemyColor.getAlpha());
        canvas.fillCircleRadialGradient(x, y, size + 8, dimmedColor, glowColor);

        // Main enemy body
        canvas.fillCircle(x, y, size, enemyColor);

        // Health bar above enemy
        if (health != null) {
            drawHealthBar(x, y - 15, health.getHealthPercentage());
        }
    }

    private void drawProjectile(org.pixel.ext.decs.GameEntity entity) {
        var position = world.getComponent(entity, PositionGameComponent.class);
        var projectile = world.getComponent(entity, ProjectileComponent.class);

        float x = position.getPosition().getX();
        float y = position.getPosition().getY();

        Color color = projectile.isEnemyProjectile()
            ? new Color(1f, 0.3f, 0.3f)
            : new Color(0.3f, 0.8f, 1f);

        // Glow trail
        Color glowOuter = new Color(color.getRed(), color.getGreen(), color.getBlue(), 0);
        canvas.fillCircleRadialGradient(x, y, 6, color, glowOuter);

        // Core
        canvas.fillCircle(x, y, 3, Color.WHITE);
    }

    private void drawParticle(org.pixel.ext.decs.GameEntity entity) {
        var position = world.getComponent(entity, PositionGameComponent.class);
        var particle = world.getComponent(entity, ParticleComponent.class);

        float x = position.getPosition().getX();
        float y = position.getPosition().getY();

        Color color = new Color(
            particle.getColor().getRed(),
            particle.getColor().getGreen(),
            particle.getColor().getBlue(),
            particle.getAlpha()
        );

        canvas.fillCircle(x, y, particle.getSize(), color);
    }

    private void drawHealthBar(float x, float y, float percentage) {
        float width = 20;
        float height = 3;

        // Background
        canvas.fillRect(x - width/2, y, width, height, new Color(0.2f, 0.2f, 0.2f));

        // Health fill (gradient from green to red)
        Color healthColor;
        if (percentage > 0.6f) {
            healthColor = new Color(0, 1f, 0); // Green
        } else if (percentage > 0.3f) {
            healthColor = new Color(1f, 1f, 0); // Yellow
        } else {
            healthColor = new Color(1f, 0, 0); // Red
        }

        canvas.fillRect(x - width/2, y, width * percentage, height, healthColor);
    }

    private void drawLightingOverlay() {
        // Find player position for light source
        float playerX = 400;
        float playerY = 300;
        float mouseX = 400;
        float mouseY = 300;

        for (var entity : players) {
            var position = world.getComponent(entity, PositionGameComponent.class);
            playerX = position.getPosition().getX() + 16;
            playerY = position.getPosition().getY() + 16;

            Vector2 mousePos = Mouse.getPosition();
            mouseX = mousePos.getX();
            mouseY = mousePos.getY();
            break;
        }

        // Draw semi-transparent darkness overlay
        canvas.fillRect(0, 0, 800, 600, new Color(0, 0, 0, 0.7f));

        // Calculate direction to mouse for flashlight
        float dx = mouseX - playerX;
        float dy = mouseY - playerY;
        float angleToMouse = (float) Math.atan2(dy, dx);

        // Main player light (large ambient circle)
        float lightRadius = 180;
        canvas.fillCircleRadialGradient(playerX, playerY, lightRadius,
            new Color(1f, 1f, 1f, 0.7f),    // Bright at center
            new Color(1f, 1f, 1f, 0f));     // Fade to transparent

        // Flashlight cone effect (elliptical gradient toward mouse)
        float flashlightDistance = 200;
        float flashlightEndX = playerX + (float) Math.cos(angleToMouse) * flashlightDistance;
        float flashlightEndY = playerY + (float) Math.sin(angleToMouse) * flashlightDistance;

        // Draw flashlight as a series of circles creating a cone
        for (int i = 1; i <= 5; i++) {
            float t = i / 5.0f;
            float fx = playerX + (flashlightEndX - playerX) * t;
            float fy = playerY + (flashlightEndY - playerY) * t;
            float radius = 40 + (t * 60); // Expanding radius
            float alpha = 0.4f - (t * 0.35f); // Fading alpha

            canvas.fillCircleRadialGradient(fx, fy, radius,
                new Color(1f, 1f, 0.9f, alpha),
                new Color(1f, 1f, 0.9f, 0f));
        }
    }
}
