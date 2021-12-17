/*
 * This software is available under Apache License
 * Copyright (c) 2020
 */

package org.pixel.gui.model;

public enum ComponentState {
    ACTIVE, FOCUS, HOVER, DISABLED;

    /**
     * @param value
     * @return
     */
    public static ComponentState fromString(String value) {
        switch (value.toLowerCase()) {
            case "focus":
                return FOCUS;
            case "hover":
                return HOVER;
            case "disabled":
                return DISABLED;
            default:
                // all non-mapped shall default to 'active':
                return ACTIVE;
        }
    }

    /**
     * @return
     */
    public String toPathString() {
        if (this == ACTIVE) {
            return "";
        }

        return ":" + this.toString();
    }

    /**
     * Returns the name of this enum constant, as contained in the
     * declaration.  This method may be overridden, though it typically
     * isn't necessary or desirable.  An enum type should override this
     * method when a more "programmer-friendly" string form exists.
     *
     * @return the name of this enum constant
     */
    @Override
    public String toString() {
        return super.toString().toLowerCase();
    }
}
