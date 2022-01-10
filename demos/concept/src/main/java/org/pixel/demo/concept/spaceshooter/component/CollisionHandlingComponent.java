package org.pixel.demo.concept.spaceshooter.component;

import org.pixel.commons.DeltaTime;
import org.pixel.demo.concept.spaceshooter.SpaceShooterAttribute;
import org.pixel.demo.concept.spaceshooter.SpaceShooterGame;
import org.pixel.demo.concept.spaceshooter.entity.PlayerSprite;
import org.pixel.demo.concept.spaceshooter.entity.SpaceShipSprite;
import org.pixel.demo.concept.spaceshooter.model.CollisionData;
import org.pixel.ext.ecs.GameComponent;
import org.pixel.ext.ecs.GameObject;
import org.pixel.ext.ecs.Sprite;

public class CollisionHandlingComponent extends GameComponent {

    private PlayerSprite playerSprite;
    private GameObject bulletContainer;
    private GameObject enemyContainer;
    private GameObject miscContainer;

    @Override
    public void attached(GameObject parent, GameObject previousParent) {
        super.attached(parent, previousParent);
        this.playerSprite = parent.getParent().getChild(PlayerSprite.class);
        this.bulletContainer = parent.getParent().getChild("BulletContainer");
        this.enemyContainer = parent.getParent().getChild("EnemyContainer");
        this.miscContainer = parent.getParent().getChild("MiscContainer");
        if (this.playerSprite == null || this.bulletContainer == null || this.enemyContainer == null
                || this.miscContainer == null) {
            throw new IllegalStateException(
                    "PlayerSprite, BulletContainer, EnemyContainer or MiscContainer could not be found.");
        }
    }

    @Override
    public void update(DeltaTime delta) {
        handleBulletCollision();
    }

    private void handleBulletCollision() {
        bulletContainer.getChildren(Sprite.class).forEach(bullet -> { // collision check all the bullets
            if (!bullet.isEnabled()) {
                return;
            }

            var type = bullet.getAttributeMap().getInteger(SpaceShooterAttribute.BULLET_TYPE);
            if (type == 0) { // player bullet
                var collisionTarget = collidesWithEnemy(bullet);
                if (collisionTarget != null) {
                    handleCollision(bullet, collisionTarget);
                }

            } else if (playerSprite.getBoundingBox().overlaps(bullet.getBoundingBox())) { // enemy bullet
                //handleCollision(bullet, playerSprite);
            }
        });
    }

    private SpaceShipSprite collidesWithEnemy(Sprite bullet) {
        var bulletBoundingBox = bullet.getBoundingBox();
        for (SpaceShipSprite enemy : enemyContainer.getChildren(SpaceShipSprite.class)) {
            if (enemy.getBoundingBox().overlaps(bulletBoundingBox)) {
                return enemy;
            }
        }

        return null;
    }

    private void handleCollision(Sprite bullet, SpaceShipSprite target) {
        bullet.dispose();
        target.dispose();

        SpaceShooterGame.$.publish("collision",
                CollisionData.builder()
                        .position(bullet.getTransform().getPosition())
                        .target(target)
                        .build());
    }
}
