/*
 * This software is available under Apache License
 * Copyright (c) 2020
 */

package org.pixel.gui.style.model;

import org.lwjgl.glfw.GLFW;

public enum CursorType {
    ARROW, POINTER, CROSSHAIR, IBEAM, HRESIZE, VRESIZE, HIDDEN;

    /**
     * @return
     */
    public int getSystemId() {
        switch (this) {
            case ARROW:
                return GLFW.GLFW_ARROW_CURSOR;
            case POINTER:
                return GLFW.GLFW_HAND_CURSOR;
            case CROSSHAIR:
                return GLFW.GLFW_CROSSHAIR_CURSOR;
            case IBEAM:
                return GLFW.GLFW_IBEAM_CURSOR;
            case HRESIZE:
                return GLFW.GLFW_HRESIZE_CURSOR;
            case VRESIZE:
                return GLFW.GLFW_VRESIZE_CURSOR;
            case HIDDEN:
                return 0;
            default:
                return getDefault().getSystemId();
        }
    }

    /**
     * @param value
     * @return
     */
    public static CursorType fromString(String value) {
        switch (value.toLowerCase()) {
            case "arrow":
                return ARROW;
            case "pointer":
            case "hand":
                return POINTER;
            case "crosshair":
                return CROSSHAIR;
            case "ibeam":
            case "org.pixel.text":
                return IBEAM;
            case "hresize":
            case "ew-resize":
                return HRESIZE;
            case "vresize":
            case "ns-resize":
                return VRESIZE;
            default:
                return getDefault();
        }
    }

    /**
     * @return
     */
    public static CursorType getDefault() {
        return ARROW;
    }
}
