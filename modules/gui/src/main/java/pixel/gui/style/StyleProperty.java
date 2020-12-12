/*
 * This software is available under Apache License
 * Copyright (c) 2020
 */

package pixel.gui.style;

public abstract class StyleProperty implements Cloneable {

    /**
     * Merge property into existing values (when applicable)
     *
     * @param other
     */
    public abstract void merge(StyleProperty other);

    /**
     * Get style with default values
     *
     * @return
     */
    public abstract void setUnassignedProperties();

    /**
     * Clone property
     *
     * @return
     */
    @Override
    public abstract StyleProperty clone();

    /**
     * Get style name (identifier)
     *
     * @return
     */
    public abstract String getName();

}
