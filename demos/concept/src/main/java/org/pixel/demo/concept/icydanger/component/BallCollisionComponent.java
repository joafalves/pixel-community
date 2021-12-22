package org.pixel.demo.concept.icydanger.component;

import java.util.List;
import org.pixel.commons.DeltaTime;
import org.pixel.demo.concept.icydanger.IcyPointsBar;
import org.pixel.ext.ecs.GameComponent;
import org.pixel.ext.ecs.GameObject;
import org.pixel.ext.ecs.Sprite;
import org.pixel.ext.ecs.Text;
import org.pixel.math.Rectangle;

public class BallCollisionComponent extends GameComponent {

    private List<Rectangle> collisionList;
    private Sprite enemySprite;

    @Override
    public void attached(GameObject parent, GameObject previousParent) {
        super.attached(parent, previousParent);
        collisionList = getGameObject().getAttributeMap().getList("staticCollisions");
        enemySprite = (Sprite) getGameObject().getAttributeMap().get("enemy");
    }

    @Override
    public void update(DeltaTime delta) {
        super.update(delta);

        var position = getTransform().getPosition();
        var boundingRect = ((Sprite) getGameObject()).getBoundingRect();
        boundingRect.shrink(1f);

        // enemy collision check
        var enemyBoundingRect = enemySprite.getBoundingRect();
        enemyBoundingRect.shrink(9f, 5f);
        if (boundingRect.intersects(enemyBoundingRect)) {
            var animComponent = enemySprite.getComponent(FlashAnimationComponent.class);
            if (!animComponent.isPlaying()) {
                // please notice that this is for demonstration purposes only, ideally there should be a manager
                // that handles the points and health of the players
                animComponent.start(3000f);
                ((Text) getGameObject().getAttributeMap().get("points")).setText(
                        Integer.parseInt(((Text) getGameObject().getAttributeMap().get("points")).getText()) + 1 + "");
                ((IcyPointsBar) getGameObject().getAttributeMap().get("pointsBar")).addValue(
                        getGameObject().getAttributeMap().getInteger("playerIndex") == 1 ? 0.1f : -0.1f);
                getGameObject().dispose();
                return;
            }
        }

        // static collision check
        for (Rectangle collisionRect : collisionList) {
            if (boundingRect.intersects(collisionRect)) {
                var stackPositionX = (position.getX() > collisionRect.getX() + collisionRect.getWidth() / 2)
                        ? collisionRect.getX() + collisionRect.getWidth() : collisionRect.getX();

                getTransform().setPositionX(stackPositionX);
                getGameObject().getComponents(BallMovementComponent.class).forEach(BallMovementComponent::dispose);
                dispose();
                return;
            }
        }
    }
}
