package org.pixel.ext.ecs.component;

import org.pixel.commons.DeltaTime;
import org.pixel.ext.ecs.GameComponent;

public class ConstantRotationComponent extends GameComponent {

    private float rotationStrength;

    /**
     * Constructor.
     *
     * @param rotationStrength The constant rotation strength to be applied on the associated object.
     */
    public ConstantRotationComponent(float rotationStrength) {
        super(ConstantRotationComponent.class.getSimpleName());
        this.rotationStrength = rotationStrength;
    }

    @Override
    public void update(DeltaTime delta) {
        getTransform().rotate(rotationStrength * delta.getElapsed());
    }

    /**
     * Get the constant rotation strength.
     *
     * @return The constant rotation strength.
     */
    public float getRotationStrength() {
        return rotationStrength;
    }

    /**
     * Set the constant rotation strength.
     *
     * @param rotationStrength The constant rotation strength to be applied on the associated object.
     */
    public void setRotationStrength(float rotationStrength) {
        this.rotationStrength = rotationStrength;
    }
}
