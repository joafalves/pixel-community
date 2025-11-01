package org.pixel.ext.rune.style;

import org.pixel.commons.Color;
import org.pixel.ext.rune.widget.BoxSizing;

/**
 * Registry of standard style properties.
 * Widget implementations can define their own additional properties.
 */
public class StyleProperties {
    // Text properties
    public static final StyleProperty<Color> TEXT_COLOR = 
        new StyleProperty<>("textColor", Color.class, Color.WHITE);
    
    public static final StyleProperty<String> FONT_FAMILY = 
        new StyleProperty<>("fontFamily", String.class, "Arial");
    
    public static final StyleProperty<Float> FONT_SIZE = 
        new StyleProperty<>("fontSize", Float.class, 14f);
    
    // Background properties
    public static final StyleProperty<Color> BACKGROUND_COLOR = 
        new StyleProperty<>("backgroundColor", Color.class, null);
    
    // Border properties
    public static final StyleProperty<Color> BORDER_COLOR = 
        new StyleProperty<>("borderColor", Color.class, null);
    
    public static final StyleProperty<Float> BORDER_WIDTH = 
        new StyleProperty<>("borderWidth", Float.class, 0f);
    
    public static final StyleProperty<Float> BORDER_RADIUS =
        new StyleProperty<>("borderRadius", Float.class, 0f);

    public static final StyleProperty<Float> BORDER_RADIUS_TOP_LEFT =
        new StyleProperty<>("borderRadiusTopLeft", Float.class, 0f);

    public static final StyleProperty<Float> BORDER_RADIUS_TOP_RIGHT =
        new StyleProperty<>("borderRadiusTopRight", Float.class, 0f);

    public static final StyleProperty<Float> BORDER_RADIUS_BOTTOM_RIGHT =
        new StyleProperty<>("borderRadiusBottomRight", Float.class, 0f);

    public static final StyleProperty<Float> BORDER_RADIUS_BOTTOM_LEFT =
        new StyleProperty<>("borderRadiusBottomLeft", Float.class, 0f);

    // Padding properties
    public static final StyleProperty<Float> PADDING = 
        new StyleProperty<>("padding", Float.class, 0f);
    
    public static final StyleProperty<Float> PADDING_TOP = 
        new StyleProperty<>("paddingTop", Float.class, 0f);
    
    public static final StyleProperty<Float> PADDING_RIGHT = 
        new StyleProperty<>("paddingRight", Float.class, 0f);
    
    public static final StyleProperty<Float> PADDING_BOTTOM = 
        new StyleProperty<>("paddingBottom", Float.class, 0f);
    
    public static final StyleProperty<Float> PADDING_LEFT = 
        new StyleProperty<>("paddingLeft", Float.class, 0f);
    
    // Margin properties
    public static final StyleProperty<Float> MARGIN = 
        new StyleProperty<>("margin", Float.class, 0f);
    
    public static final StyleProperty<Float> MARGIN_TOP = 
        new StyleProperty<>("marginTop", Float.class, 0f);
    
    public static final StyleProperty<Float> MARGIN_RIGHT = 
        new StyleProperty<>("marginRight", Float.class, 0f);
    
    public static final StyleProperty<Float> MARGIN_BOTTOM = 
        new StyleProperty<>("marginBottom", Float.class, 0f);
    
    public static final StyleProperty<Float> MARGIN_LEFT = 
        new StyleProperty<>("marginLeft", Float.class, 0f);
    
    // Box model
    public static final StyleProperty<BoxSizing> BOX_SIZING = 
        new StyleProperty<>("boxSizing", BoxSizing.class, BoxSizing.BORDER_BOX);
    
    // Visual effects
    public static final StyleProperty<Float> OPACITY = 
        new StyleProperty<>("opacity", Float.class, 1f);
    
    public static final StyleProperty<String> CURSOR = 
        new StyleProperty<>("cursor", String.class, "default");

    private StyleProperties() {
        // Utility class
    }
}
