package org.pixel.demo.concept.icydanger.component;

import org.pixel.commons.DeltaTime;
import org.pixel.ext.ecs.GameComponent;
import org.pixel.ext.ecs.GameObject;
import org.pixel.ext.ecs.Sprite;
import org.pixel.math.MathHelper;

public class FlashAnimationComponent extends GameComponent {

    private Sprite parent;
    private float elapsed;
    private float duration;

    @Override
    public void attached(GameObject parent, GameObject previousParent) {
        super.attached(parent, previousParent);
        if (!(parent instanceof Sprite)) {
            throw new RuntimeException("Parent of this component must be a Sprite");
        }

        this.parent = (Sprite) parent;
        this.elapsed = -1;
    }

    @Override
    public void update(DeltaTime delta) {
        super.update(delta);

        if (elapsed >= 0) {
            parent.getOverlayColor().setAlpha(
                    MathHelper.clamp(1 - MathHelper.sin(elapsed / 60f), 0.20f, 1f)
            );

            elapsed += delta.getElapsedMs();
            if (elapsed >= duration) {
                elapsed = -1;

                parent.getOverlayColor().setAlpha(1);
            }
        }
    }

    /**
     * Start the flash animation.
     *
     * @param duration The duration of the flash animation in millis.
     */
    public void start(float duration) {
        if (this.elapsed == -1 || this.elapsed >= duration) {
            this.elapsed = 0;
            this.duration = duration;
        }
    }

    public boolean isPlaying() {
        return elapsed >= 0;
    }
}
