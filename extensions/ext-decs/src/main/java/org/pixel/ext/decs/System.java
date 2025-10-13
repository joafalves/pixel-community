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
public abstract class System implements Initializable, Loadable, Updatable, Drawable, Disposable {
    protected World world;
    private boolean enabled = true;

    /**
     * Constructor.
     *
     * @param world The world this system belongs to.
     */
    public System(World world) {
        this.world = world;
    }

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
     * Initializes the system. Can be used to get dependencies from the ServiceProvider.
     */
    @Override
    public boolean init() {
        // intentionally left blank
        return true;
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
