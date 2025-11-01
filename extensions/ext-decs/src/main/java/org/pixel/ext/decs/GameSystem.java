package org.pixel.ext.decs;

import org.pixel.commons.DeltaTime;
import org.pixel.commons.lifecycle.Disposable;
import org.pixel.commons.lifecycle.Drawable;
import org.pixel.commons.lifecycle.Initializable;
import org.pixel.commons.lifecycle.Loadable;
import org.pixel.commons.lifecycle.Updatable;

/**
 * A base class for systems, which contain the logic of the ECS.
 */
public abstract class GameSystem implements Loadable, Updatable, Drawable, Disposable {

    private boolean enabled = true;

    /**
     * Checks if the system is enabled.
     *
     * @return True if the system is enabled, false otherwise.
     */
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * Sets the enabled state of the system.
     *
     * @param enabled The new enabled state.
     */
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    /**
     * Loads content for the system.
     */
    @Override
    public void load() {
        // intentionally left blank
    }

    /**
     * Updates the system.
     *
     * @param delta The time since the last update.
     */
    @Override
    public void update(DeltaTime delta) {
        // intentionally left blank
    }

    /**
     * Draws the system.
     *
     * @param delta The time since the last update.
     */
    @Override
    public void draw(DeltaTime delta) {
        // intentionally left blank
    }

    /**
     * Disposes the system.
     */
    @Override
    public void dispose() {
        // intentionally left blank
    }
}
