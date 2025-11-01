/*
 * This software is available under Apache License
 * Copyright (c) 2020
 */

package org.pixel.ext.rune.widget;

/**
 * Position mode determines how a widget is positioned relative to its parent or viewport.
 * Matches CSS position property behavior.
 *
 * <p>Position modes affect:
 * <ul>
 *   <li>How x/y coordinates are interpreted</li>
 *   <li>Whether the widget is in the normal layout flow</li>
 *   <li>How scrolling and parent transforms affect the widget</li>
 * </ul>
 */
public enum PositionMode {
    /**
     * Position relative to parent's content area (default).
     * The widget is part of normal layout flow.
     * x/y are relative to parent's content origin.
     * Affected by parent scrolling.
     *
     * <p>Example: A button inside a scrollable panel.
     */
    RELATIVE,

    /**
     * Position relative to nearest positioned ancestor (or viewport if none).
     * The widget is removed from normal layout flow.
     * x/y or insets are relative to positioned ancestor's padding box.
     * Affected by ancestor scrolling.
     *
     * <p>Example: Dropdown menu, tooltip, context menu.
     * <p>Note: "Positioned ancestor" = any ancestor with position != RELATIVE
     */
    ABSOLUTE,

    /**
     * Position relative to viewport (UI root).
     * The widget is removed from normal layout flow.
     * x/y or insets are relative to viewport origin (0, 0).
     * NOT affected by any scrolling.
     *
     * <p>Example: Modal overlay, status bar, fixed header.
     * <p>Perfect for UI elements that should stay in place during scrolling.
     */
    FIXED
}
