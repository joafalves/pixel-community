package org.pixel.demo.concept.spaceshooter.entity;

import org.pixel.commons.DeltaTime;
import org.pixel.content.TextureFrame;
import org.pixel.ext.ecs.GameObject;
import org.pixel.ext.ecs.Sprite;
import org.pixel.ext.ecs.component.AutoDisposeComponent;
import org.pixel.ext.ecs.component.MoveTowardsComponent;
import org.pixel.math.MathHelper;
import org.pixel.math.Rectangle;
import org.pixel.math.Vector2;

public class EnemySpawner extends GameObject {

    private final static int WAVE_DELAY_MS = 1000;

    private final PlayerSprite playerSprite;
    private final Rectangle spawnArea;
    private final TextureFrame fireBlazerTextureFrame;
    private final TextureFrame sentinelTextureFrame;

    private float waveElapsed = 5000;

    public EnemySpawner(String name, Rectangle spawnArea, TextureFrame fireBlazerTextureFrame,
            TextureFrame sentinelTextureFrame, PlayerSprite playerSprite) {
        super(name);
        this.spawnArea = spawnArea;
        this.fireBlazerTextureFrame = fireBlazerTextureFrame;
        this.sentinelTextureFrame = sentinelTextureFrame;
        this.playerSprite = playerSprite;
    }

    @Override
    public void update(DeltaTime delta) {
        super.update(delta);

        waveElapsed += delta.getElapsedMs();
        if (waveElapsed > WAVE_DELAY_MS) {
            waveElapsed = 0;

            // begin spawning wave...
            if (MathHelper.random()) {
                // spawn enemy FireBlazers
                spawnFireBlazerWave();

            } else {
                // spawn enemy Sentinels
                spawnSentinelWave();
            }
        }
    }

    private void spawnFireBlazerWave() {

    }

    private void spawnSentinelWave() {
        var origin = generateSpawnPosition();
        for (var i = 0; i < 1; i++) {
            var sprite = new SpaceShipSprite("Sentinel", sentinelTextureFrame, 3);
            sprite.getTransform().setPosition(origin);
            sprite.getTransform().setRotation(-MathHelper.PIo2);
            sprite.getTransform().lookAt(playerSprite.getTransform().getWorldPosition());
            sprite.addComponent(new MoveTowardsComponent(playerSprite.getTransform().getPosition(), 120f, 20f, false));
            sprite.addComponent(new AutoDisposeComponent(20));

            addChild(sprite);
        }
    }

    private Vector2 generateSpawnPosition() {
        return new Vector2(
                MathHelper.random(spawnArea.getX(), spawnArea.getX() + spawnArea.getWidth()),
                MathHelper.random(spawnArea.getY(), spawnArea.getY() + spawnArea.getHeight()));
    }

    private Sprite createEnemy() {
        return null;
    }

    private Sprite createEnemySpanner() {
        return null;
    }
}
