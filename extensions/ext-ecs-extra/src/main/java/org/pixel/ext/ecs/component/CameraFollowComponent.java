package org.pixel.ext.ecs.component;

import org.pixel.commons.DeltaTime;
import org.pixel.core.Camera2D;
import org.pixel.ext.ecs.GameComponent;

/**
 * Sets the given camera to follow the entity associated to this component (make sure to have only one of this component
 * per game camera).
 */
public class CameraFollowComponent extends GameComponent {

    private Camera2D camera;

    /**
     * Constructor.
     *
     * @param camera The camera to follow.
     */
    public CameraFollowComponent(Camera2D camera) {
        super(CameraFollowComponent.class.getSimpleName());
        this.camera = camera;
    }

    @Override
    public void update(DeltaTime delta) {
        if (camera != null) {
            camera.setPosition(getTransform().getWorldPosition());
        }
    }

    /**
     * Get the camera instance.
     *
     * @return The camera instance.
     */
    public Camera2D getCamera() {
        return camera;
    }

    /**
     * Set the camera instance.
     *
     * @param camera The camera instance.
     */
    public void setCamera(Camera2D camera) {
        this.camera = camera;
    }
}
