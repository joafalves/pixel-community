package org.pixel.ext.weaver.style.property;

import lombok.NoArgsConstructor;
import org.pixel.commons.Color;
import org.pixel.ext.weaver.style.property.model.Measurement;
import org.pixel.ext.weaver.style.property.type.BoxSizingType;
import org.pixel.ext.weaver.style.property.type.DisplayType;
import org.pixel.ext.weaver.style.property.type.OverflowType;
import org.pixel.ext.weaver.style.property.type.PositionType;

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
    public static final StyleProperty<Measurement> WIDTH =
            new StyleProperty<>("width", Measurement.class, Measurement.defaultValue());

    public static final StyleProperty<Measurement> HEIGHT =
            new StyleProperty<>("height", Measurement.class, Measurement.defaultValue());

    public static final StyleProperty<Measurement> MIN_WIDTH =
            new StyleProperty<>("min-width", Measurement.class, Measurement.defaultValue());

    public static final StyleProperty<Measurement> MIN_HEIGHT =
            new StyleProperty<>("min-height", Measurement.class, Measurement.defaultValue());

    public static final StyleProperty<Measurement> MAX_WIDTH =
            new StyleProperty<>("max-width", Measurement.class, Measurement.defaultValue());

    public static final StyleProperty<Measurement> MAX_HEIGHT =
            new StyleProperty<>("max-height", Measurement.class, Measurement.defaultValue());

    // ===== Border Properties =====
    public static final StyleProperty<Measurement> BORDER_RADIUS =
            new StyleProperty<>("border-radius", Measurement.class, Measurement.defaultValue());

    public static final StyleProperty<Measurement> BORDER_TOP_LEFT_RADIUS =
            new StyleProperty<>("border-top-left-radius", Measurement.class, Measurement.defaultValue());

    public static final StyleProperty<Measurement> BORDER_TOP_RIGHT_RADIUS =
            new StyleProperty<>("border-top-right-radius", Measurement.class, Measurement.defaultValue());

    public static final StyleProperty<Measurement> BORDER_BOTTOM_RIGHT_RADIUS =
            new StyleProperty<>("border-bottom-right-radius", Measurement.class, Measurement.defaultValue());

    public static final StyleProperty<Measurement> BORDER_BOTTOM_LEFT_RADIUS =
            new StyleProperty<>("border-bottom-left-radius", Measurement.class, Measurement.defaultValue());

    public static final StyleProperty<Measurement> BORDER_WIDTH =
            new StyleProperty<>("border-width", Measurement.class, Measurement.defaultValue());

    public static final StyleProperty<Measurement> BORDER_TOP_WIDTH =
            new StyleProperty<>("border-top-width", Measurement.class, Measurement.defaultValue());

    public static final StyleProperty<Measurement> BORDER_RIGHT_WIDTH =
            new StyleProperty<>("border-right-width", Measurement.class, Measurement.defaultValue());

    public static final StyleProperty<Measurement> BORDER_BOTTOM_WIDTH =
            new StyleProperty<>("border-bottom-width", Measurement.class, Measurement.defaultValue());

    public static final StyleProperty<Measurement> BORDER_LEFT_WIDTH =
            new StyleProperty<>("border-left-width", Measurement.class, Measurement.defaultValue());

    // ===== Outline Properties =====
    public static final StyleProperty<Measurement> OUTLINE_WIDTH =
            new StyleProperty<>("outline-width", Measurement.class, Measurement.defaultValue());

    public static final StyleProperty<Measurement> OUTLINE_OFFSET =
            new StyleProperty<>("outline-offset",Measurement.class, Measurement.defaultValue());

    // ===== Spacing Properties =====
    public static final StyleProperty<Measurement> PADDING =
            new StyleProperty<>("padding", Measurement.class, Measurement.defaultValue());

    public static final StyleProperty<Measurement> PADDING_TOP =
            new StyleProperty<>("padding-top", Measurement.class, Measurement.defaultValue());

    public static final StyleProperty<Measurement> PADDING_RIGHT =
            new StyleProperty<>("padding-right", Measurement.class, Measurement.defaultValue());

    public static final StyleProperty<Measurement> PADDING_BOTTOM =
            new StyleProperty<>("padding-bottom", Measurement.class, Measurement.defaultValue());

    public static final StyleProperty<Measurement> PADDING_LEFT =
            new StyleProperty<>("padding-left", Measurement.class, Measurement.defaultValue());

    public static final StyleProperty<Measurement> MARGIN =
            new StyleProperty<>("margin", Measurement.class, Measurement.defaultValue());

    public static final StyleProperty<Measurement> MARGIN_TOP =
            new StyleProperty<>("margin-top", Measurement.class, Measurement.defaultValue());

    public static final StyleProperty<Measurement> MARGIN_RIGHT =
            new StyleProperty<>("margin-right",  Measurement.class, Measurement.defaultValue());

    public static final StyleProperty<Measurement> MARGIN_BOTTOM =
            new StyleProperty<>("margin-bottom",  Measurement.class, Measurement.defaultValue());

    public static final StyleProperty<Measurement> MARGIN_LEFT =
            new StyleProperty<>("margin-left",  Measurement.class, Measurement.defaultValue());

    // ===== Positioning Properties =====
    public static final StyleProperty<Measurement> TOP =
            new StyleProperty<>("top",  Measurement.class, Measurement.defaultValue());

    public static final StyleProperty<Measurement> RIGHT =
            new StyleProperty<>("right",  Measurement.class, Measurement.defaultValue());

    public static final StyleProperty<Measurement> BOTTOM =
            new StyleProperty<>("bottom",  Measurement.class, Measurement.defaultValue());

    public static final StyleProperty<Measurement> LEFT =
            new StyleProperty<>("left",  Measurement.class, Measurement.defaultValue());

    // ===== Layout Properties =====
    public static final StyleProperty<BoxSizingType> BOX_SIZING =
            new StyleProperty<>("box-sizing", BoxSizingType.class, BoxSizingType.defaultValue());

    // ===== Typography Properties =====
    public static final StyleProperty<Measurement> FONT_SIZE =
            new StyleProperty<>("font-size", Measurement.class, Measurement.defaultValue());

    public static final StyleProperty<String> FONT_FAMILY =
            new StyleProperty<>("font-family", String.class, "roboto");

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

    public static final StyleProperty<Measurement> TEXT_INDENT =
            new StyleProperty<>("text-indent", Measurement.class, Measurement.defaultValue());

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
    public static final StyleProperty<DisplayType> DISPLAY =
            new StyleProperty<>("display", DisplayType.class, DisplayType.defaultValue());

    public static final StyleProperty<PositionType> POSITION =
            new StyleProperty<>("position", PositionType.class, PositionType.defaultValue());

    public static final StyleProperty<OverflowType> OVERFLOW =
            new StyleProperty<>("overflow", OverflowType.class, OverflowType.defaultValue());
}

