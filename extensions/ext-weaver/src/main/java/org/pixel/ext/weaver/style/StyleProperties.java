package org.pixel.ext.weaver.style;

import lombok.NoArgsConstructor;
import org.pixel.commons.Color;

/**
 * Registry of standard style properties with typed definitions.
 * Use these constants instead of magic strings when working with Style.
 */
@NoArgsConstructor(access = lombok.AccessLevel.PRIVATE)
public final class StyleProperties {

    // ===== Color Properties =====
    public static final StyleProperty<Color> BACKGROUND_COLOR =
        new StyleProperty<>("background-color", Color.class, null);

    public static final StyleProperty<Color> COLOR =
        new StyleProperty<>("color", Color.class, Color.BLACK);

    public static final StyleProperty<Color> BORDER_COLOR =
        new StyleProperty<>("border-color", Color.class, Color.BLACK);

    public static final StyleProperty<Color> BORDER_TOP_COLOR =
        new StyleProperty<>("border-top-color", Color.class, Color.BLACK);

    public static final StyleProperty<Color> BORDER_RIGHT_COLOR =
        new StyleProperty<>("border-right-color", Color.class, Color.BLACK);

    public static final StyleProperty<Color> BORDER_BOTTOM_COLOR =
        new StyleProperty<>("border-bottom-color", Color.class, Color.BLACK);

    public static final StyleProperty<Color> BORDER_LEFT_COLOR =
        new StyleProperty<>("border-left-color", Color.class, Color.BLACK);

    public static final StyleProperty<Color> CARET_COLOR =
        new StyleProperty<>("caret-color", Color.class, Color.BLACK);

    public static final StyleProperty<Color> OUTLINE_COLOR =
        new StyleProperty<>("outline-color", Color.class, Color.BLACK);

    // ===== Dimension/Size Properties =====
    public static final StyleProperty<Float> WIDTH =
        new StyleProperty<>("width", Float.class, 0f);

    public static final StyleProperty<Float> HEIGHT =
        new StyleProperty<>("height", Float.class, 0f);

    public static final StyleProperty<Float> MIN_WIDTH =
        new StyleProperty<>("min-width", Float.class, 0f);

    public static final StyleProperty<Float> MIN_HEIGHT =
        new StyleProperty<>("min-height", Float.class, 0f);

    public static final StyleProperty<Float> MAX_WIDTH =
        new StyleProperty<>("max-width", Float.class, Float.MAX_VALUE);

    public static final StyleProperty<Float> MAX_HEIGHT =
        new StyleProperty<>("max-height", Float.class, Float.MAX_VALUE);

    // ===== Border Properties =====
    public static final StyleProperty<Float> BORDER_RADIUS =
        new StyleProperty<>("border-radius", Float.class, 0f);

    public static final StyleProperty<Float> BORDER_WIDTH =
        new StyleProperty<>("border-width", Float.class, 0f);

    public static final StyleProperty<Float> BORDER_TOP_WIDTH =
        new StyleProperty<>("border-top-width", Float.class, 0f);

    public static final StyleProperty<Float> BORDER_RIGHT_WIDTH =
        new StyleProperty<>("border-right-width", Float.class, 0f);

    public static final StyleProperty<Float> BORDER_BOTTOM_WIDTH =
        new StyleProperty<>("border-bottom-width", Float.class, 0f);

    public static final StyleProperty<Float> BORDER_LEFT_WIDTH =
        new StyleProperty<>("border-left-width", Float.class, 0f);

    // ===== Outline Properties =====
    public static final StyleProperty<Float> OUTLINE_WIDTH =
        new StyleProperty<>("outline-width", Float.class, 0f);

    public static final StyleProperty<Float> OUTLINE_OFFSET =
        new StyleProperty<>("outline-offset", Float.class, 0f);

    // ===== Spacing Properties =====
    public static final StyleProperty<Float> PADDING =
        new StyleProperty<>("padding", Float.class, 0f);

    public static final StyleProperty<Float> PADDING_TOP =
        new StyleProperty<>("padding-top", Float.class, 0f);

    public static final StyleProperty<Float> PADDING_RIGHT =
        new StyleProperty<>("padding-right", Float.class, 0f);

    public static final StyleProperty<Float> PADDING_BOTTOM =
        new StyleProperty<>("padding-bottom", Float.class, 0f);

    public static final StyleProperty<Float> PADDING_LEFT =
        new StyleProperty<>("padding-left", Float.class, 0f);

    public static final StyleProperty<Float> MARGIN =
        new StyleProperty<>("margin", Float.class, 0f);

    public static final StyleProperty<Float> MARGIN_TOP =
        new StyleProperty<>("margin-top", Float.class, 0f);

    public static final StyleProperty<Float> MARGIN_RIGHT =
        new StyleProperty<>("margin-right", Float.class, 0f);

    public static final StyleProperty<Float> MARGIN_BOTTOM =
        new StyleProperty<>("margin-bottom", Float.class, 0f);

    public static final StyleProperty<Float> MARGIN_LEFT =
        new StyleProperty<>("margin-left", Float.class, 0f);

    // ===== Positioning Properties =====
    public static final StyleProperty<Float> TOP =
        new StyleProperty<>("top", Float.class, 0f);

    public static final StyleProperty<Float> RIGHT =
        new StyleProperty<>("right", Float.class, 0f);

    public static final StyleProperty<Float> BOTTOM =
        new StyleProperty<>("bottom", Float.class, 0f);

    public static final StyleProperty<Float> LEFT =
        new StyleProperty<>("left", Float.class, 0f);

    // ===== Layout Properties =====
    public static final StyleProperty<Float> GAP =
        new StyleProperty<>("gap", Float.class, 0f);

    public static final StyleProperty<Float> ROW_GAP =
        new StyleProperty<>("row-gap", Float.class, 0f);

    public static final StyleProperty<Float> COLUMN_GAP =
        new StyleProperty<>("column-gap", Float.class, 0f);

    // ===== Typography Properties =====
    public static final StyleProperty<Float> FONT_SIZE =
        new StyleProperty<>("font-size", Float.class, 16f);

    public static final StyleProperty<String> FONT_FAMILY =
        new StyleProperty<>("font-family", String.class, "sans-serif");

    public static final StyleProperty<Integer> FONT_WEIGHT =
        new StyleProperty<>("font-weight", Integer.class, 400);

    public static final StyleProperty<String> FONT_STYLE =
        new StyleProperty<>("font-style", String.class, "normal");

    public static final StyleProperty<Float> LINE_HEIGHT =
        new StyleProperty<>("line-height", Float.class, 1.2f);

    public static final StyleProperty<Float> LETTER_SPACING =
        new StyleProperty<>("letter-spacing", Float.class, 0f);

    public static final StyleProperty<Float> WORD_SPACING =
        new StyleProperty<>("word-spacing", Float.class, 0f);

    public static final StyleProperty<String> TEXT_ALIGN =
        new StyleProperty<>("text-align", String.class, "left");

    public static final StyleProperty<Float> TEXT_INDENT =
        new StyleProperty<>("text-indent", Float.class, 0f);

    public static final StyleProperty<String> TEXT_TRANSFORM =
        new StyleProperty<>("text-transform", String.class, "none");

    public static final StyleProperty<String> WHITE_SPACE =
        new StyleProperty<>("white-space", String.class, "normal");

    // ===== Visual Properties =====
    public static final StyleProperty<Float> OPACITY =
        new StyleProperty<>("opacity", Float.class, 1f);

    public static final StyleProperty<Integer> Z_INDEX =
        new StyleProperty<>("z-index", Integer.class, 0);

    // ===== User Interface Properties =====
    public static final StyleProperty<String> CURSOR =
        new StyleProperty<>("cursor", String.class, "default");

    public static final StyleProperty<String> VISIBILITY =
        new StyleProperty<>("visibility", String.class, "visible");

    public static final StyleProperty<String> POINTER_EVENTS =
        new StyleProperty<>("pointer-events", String.class, "auto");

    public static final StyleProperty<String> USER_SELECT =
        new StyleProperty<>("user-select", String.class, "auto");

    public static final StyleProperty<String> DIRECTION =
        new StyleProperty<>("direction", String.class, "ltr");

    // ===== Display Properties =====
    public static final StyleProperty<String> DISPLAY =
        new StyleProperty<>("display", String.class, "block");

    public static final StyleProperty<String> POSITION =
        new StyleProperty<>("position", String.class, "static");

    public static final StyleProperty<String> OVERFLOW =
        new StyleProperty<>("overflow", String.class, "visible");
}

