package org.pixel.ext.rune.widget;

/**
 * Box sizing mode (like CSS box-sizing property).
 * Determines how width/height are interpreted relative to padding and border.
 */
public enum BoxSizing {
    /**
     * Width/height specifies content area only.
     * Total size = content + padding + border.
     * This is the CSS default but less intuitive.
     */
    CONTENT_BOX,
    
    /**
     * Width/height includes padding and border (modern CSS default).
     * Content area = width - padding - border.
     * This is more intuitive and matches WinForms behavior.
     */
    BORDER_BOX
}
