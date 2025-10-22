/*
 * This software is available under Apache License
 * Copyright (c) 2020
 */

package org.pixel.ext.rune.input;

/**
 * Pointer button enumeration for platform-agnostic input.
 * Maps to mouse buttons on desktop, touch gestures on mobile, etc.
 */
public enum PointerButton {
    /**
     * Primary button (left mouse button, touch press, primary gamepad action)
     */
    PRIMARY(0),
    
    /**
     * Secondary button (right mouse button, long press on touch, secondary gamepad action)
     */
    SECONDARY(1),
    
    /**
     * Tertiary button (middle mouse button, two-finger tap on touch)
     */
    TERTIARY(2);
    
    private final int code;
    
    PointerButton(int code) {
        this.code = code;
    }
    
    /**
     * Get the numeric code for this button.
     * Matches standard mouse button codes: 0=left, 1=right, 2=middle
     * 
     * @return Button code
     */
    public int getCode() {
        return code;
    }
    
    /**
     * Get PointerButton from numeric code.
     * 
     * @param code Button code (0=PRIMARY, 1=SECONDARY, 2=TERTIARY)
     * @return PointerButton, or PRIMARY if code is invalid
     */
    public static PointerButton fromCode(int code) {
        for (PointerButton button : values()) {
            if (button.code == code) {
                return button;
            }
        }
        return PRIMARY; // Default fallback
    }
}
