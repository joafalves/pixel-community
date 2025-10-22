package org.pixel.ext.rune.theme;

import lombok.Getter;
import lombok.Setter;
import org.pixel.commons.Color;
import org.pixel.ext.rune.style.RuneStyleSheet;

/**
 * Rune theme - holds semantic colors, font settings, and stylesheet.
 *
 * <p>Themes are created via static factory methods in theme classes like
 * {@link RuneDarkTheme} and {@link RuneLightTheme}.
 *
 * <p>Usage: {@code RuneUI gui = new RuneUI(width, height, RuneDarkTheme.create());}
 */
@Getter
@Setter
public class RuneTheme {

    // === Semantic Colors (Application-level, for programmatic access) ===

    // Main background color (for screen/canvas clear color)
    private Color background;

    // Primary brand/accent color
    private Color primary;
    private Color primaryDark;
    private Color primaryLight;

    // Secondary accent color
    private Color secondary;
    private Color secondaryDark;
    private Color secondaryLight;

    // Success/Warning/Error semantic colors
    private Color success;
    private Color warning;
    private Color error;
    private Color info;

    // Scrollbar colors (accessed directly by RuneContainer for now)
    private Color scrollbarBg;
    private Color scrollbarThumb;
    private Color scrollbarThumbHover;

    // === Font Settings ===

    // Font family/name to use (like CSS font-family)
    private String fontFamily;
    private float baseFontSize;

    // === Style Sheet (CSS-like) ===

    private RuneStyleSheet styleSheet;

    /**
     * Package-private constructor. Use static factory methods in theme classes.
     */
    RuneTheme() {
        // Created by factory methods
    }

    /**
     * Get the stylesheet for this theme.
     * Creates a default empty stylesheet if none exists.
     *
     * @return The stylesheet
     */
    public RuneStyleSheet getStyleSheet() {
        if (styleSheet == null) {
            styleSheet = new RuneStyleSheet();
        }
        return styleSheet;
    }
}
