package org.pixel.demo.concept.icydanger.component;

import org.pixel.commons.DeltaTime;
import org.pixel.demo.concept.commons.PlayerIndex;
import org.pixel.ext.ecs.GameComponent;
import org.pixel.ext.ecs.Sprite;
import org.pixel.ext.ecs.component.AutoDisposeComponent;
import org.pixel.input.keyboard.Keyboard;
import org.pixel.input.keyboard.KeyboardKey;
import org.pixel.math.Rectangle;

public class PlayerThrowComponent extends GameComponent {

    private static final float THROW_DELAY_SEC = 1f;
    private static final Rectangle BALL_TEX_SOURCE = new Rectangle(1, 65, 6, 6);
    private static final Rectangle BALL_SHADOW_SOURCE = new Rectangle(2, 72, 4, 3);

    private final float ballSpeed;
    private final PlayerIndex playerIndex;
    private float elapsed = THROW_DELAY_SEC;

    public PlayerThrowComponent(PlayerIndex playerIndex) {
        this.playerIndex = playerIndex;
        this.ballSpeed = 250f * (playerIndex == PlayerIndex.P1 ? 1f : -1f);
    }

    @Override
    public void update(DeltaTime delta) {
        super.update(delta);

        elapsed += delta.getElapsed();
        if (isPlayerThrowing() && elapsed > THROW_DELAY_SEC) {
            elapsed = 0f;
            throwBall();
        }
    }

    private void throwBall() {
        var ballSprite = new Sprite("ball", ((Sprite) getGameObject()).getTexture(), BALL_TEX_SOURCE);
        ballSprite.getAttributeMap().putAll(getGameObject().getAttributeMap());
        ballSprite.getTransform().translate(getTransform().getWorldPosition());
        ballSprite.getTransform().translate(0, 6);
        ballSprite.addComponent(new BallMovementComponent(150, ballSpeed, 15f));
        ballSprite.addComponent(new AutoDisposeComponent(1.5f));
        ballSprite.addComponent(new BallCollisionComponent());

        var ballShadow = new Sprite("ballShadow", ((Sprite) getGameObject()).getTexture(), BALL_SHADOW_SOURCE);
        ballSprite.addChild(ballShadow); // add ball-shadow child to the ball object

        getGameObject().getParent().addChild(ballSprite);
    }

    private boolean isPlayerThrowing() {
        return Keyboard.isKeyDown(playerIndex == PlayerIndex.P1 ? KeyboardKey.SPACE : KeyboardKey.ENTER);
    }
}
