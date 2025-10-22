package org.pixel.ext.rune.theme;

import org.pixel.commons.Color;
import org.pixel.ext.rune.style.RuneStyleSheet;
import org.pixel.ext.rune.widget.BoxSizing;

import static org.pixel.ext.rune.style.StyleProperties.*;

/**
 * Dark theme for Rune GUI.
 * Professional dark color scheme suitable for editors and tools.
 */
public class RuneDarkTheme {

    // Semantic colors (public static for programmatic access)
    public static final Color PRIMARY = new Color(0.3f, 0.6f, 1f, 1f);
    public static final Color SECONDARY = new Color(1f, 0.5f, 0.2f, 1f);
    public static final Color SUCCESS = new Color(0.3f, 0.8f, 0.4f, 1f);
    public static final Color WARNING = new Color(0.9f, 0.7f, 0.2f, 1f);
    public static final Color ERROR = new Color(0.9f, 0.3f, 0.3f, 1f);
    public static final Color INFO = PRIMARY;

    // Scrollbar colors (accessed by RuneContainer)
    public static final Color SCROLLBAR_BG = new Color(0.1f, 0.1f, 0.1f, 0.3f);
    public static final Color SCROLLBAR_THUMB = new Color(0.4f, 0.4f, 0.4f, 0.8f);
    public static final Color SCROLLBAR_THUMB_HOVER = new Color(0.6f, 0.6f, 0.6f, 0.9f);

    /**
     * Create a dark theme stylesheet.
     *
     * @return Configured dark theme stylesheet
     */
    public static RuneStyleSheet create() {
        RuneStyleSheet sheet = new RuneStyleSheet();

        // === TEXT SIZE CLASSES ===

        sheet.styleClass("large", s -> s.set(FONT_SIZE, 24f));
        sheet.styleClass("medium", s -> s.set(FONT_SIZE, 16f));
        sheet.styleClass("small", s -> s.set(FONT_SIZE, 12f));

        // === TYPE STYLES ===

        // Label
        sheet.type("label", s -> s
                .set(TEXT_COLOR, Color.WHITE)
                .set(BACKGROUND_COLOR, null)
                .set(FONT_FAMILY, "roboto")
                .set(FONT_SIZE, 14f)
                .set(BORDER_RADIUS, 4f)
                .set(MARGIN_BOTTOM, 4f)
                .set(BOX_SIZING, BoxSizing.BORDER_BOX));

        // Button - base state
        sheet.type("button", s -> s
                .set(BACKGROUND_COLOR, new Color(0.2f, 0.2f, 0.2f, 1f))
                .set(TEXT_COLOR, Color.WHITE)
                .set(BORDER_COLOR, new Color(0.3f, 0.3f, 0.3f, 1f))
                .set(BORDER_WIDTH, 1f)
                .set(BORDER_RADIUS, 4f)
                .set(PADDING_TOP, 8f)
                .set(PADDING_RIGHT, 16f)
                .set(PADDING_BOTTOM, 8f)
                .set(PADDING_LEFT, 16f)
                .set(MARGIN_RIGHT, 8f)
                .set(MARGIN_BOTTOM, 8f)
                .set(FONT_SIZE, 14f)
                .set(BOX_SIZING, BoxSizing.BORDER_BOX));

        // Button pseudo-classes
        sheet.type("button:hover", s -> s
                .set(BACKGROUND_COLOR, new Color(0.35f, 0.35f, 0.35f, 1f))  // More visible hover
                .set(BORDER_COLOR, new Color(0.6f, 0.6f, 0.6f, 1f)));

        sheet.type("button:pressed", s -> s
                .set(BACKGROUND_COLOR, new Color(0.12f, 0.12f, 0.12f, 1f)));

        sheet.type("button:disabled", s -> s
                .set(BACKGROUND_COLOR, new Color(0.1f, 0.1f, 0.1f, 0.5f))
                .set(TEXT_COLOR, new Color(0.5f, 0.5f, 0.5f, 1f)));

        // Container
        sheet.type("container", s -> s
                .set(BACKGROUND_COLOR, new Color(0.12f, 0.12f, 0.12f, 0.9f))
                .set(BORDER_COLOR, new Color(0.3f, 0.3f, 0.3f, 1f))
                .set(BORDER_WIDTH, 1f)
                .set(BORDER_RADIUS, 4f)
                .set(PADDING, 8f)
                .set(BOX_SIZING, BoxSizing.BORDER_BOX));

        // === SEMANTIC STYLE CLASSES ===

        sheet.styleClass("primary", s -> s
                .set(TEXT_COLOR, PRIMARY)
                .set(BACKGROUND_COLOR, PRIMARY));
        
        sheet.styleClass("secondary", s -> s
                .set(TEXT_COLOR, SECONDARY)
                .set(BACKGROUND_COLOR, SECONDARY));
        
        sheet.styleClass("success", s -> s
                .set(TEXT_COLOR, SUCCESS)
                .set(BACKGROUND_COLOR, SUCCESS));
        
        sheet.styleClass("warning", s -> s
                .set(TEXT_COLOR, WARNING)
                .set(BACKGROUND_COLOR, WARNING));
        
        sheet.styleClass("error", s -> s
                .set(TEXT_COLOR, ERROR)
                .set(BACKGROUND_COLOR, ERROR));
        
        sheet.styleClass("info", s -> s
                .set(TEXT_COLOR, INFO)
                .set(BACKGROUND_COLOR, INFO));

        sheet.styleClass("title", s -> s
                .set(FONT_SIZE, 24f)
                .set(TEXT_COLOR, PRIMARY));

        return sheet;
    }
}
