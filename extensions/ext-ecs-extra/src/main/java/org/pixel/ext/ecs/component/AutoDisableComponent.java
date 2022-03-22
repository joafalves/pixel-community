package org.pixel.ext.ecs.component;

import org.pixel.commons.DeltaTime;
import org.pixel.ext.ecs.GameComponent;

/**
 * Auto disables the game object entity after the configured amount of seconds.
 */
public class AutoDisableComponent extends GameComponent {

    private final float seconds;

    private float elapsed;

    /**
     * Constructor.
     *
     * @param seconds The amount of seconds to wait before disposing the game object entity.
     */
    public AutoDisableComponent(float seconds) {
        super(AutoDisableComponent.class.getSimpleName());
        this.seconds = seconds;
        this.elapsed = 0f;
    }

    @Override
    public void update(DeltaTime delta) {
        super.update(delta);

        this.elapsed += delta.getElapsed();
        if (elapsed >= seconds) {
            this.getGameObject().setEnabled(false);
        }
    }
}
