/*
 * This software is available under Apache License
 * Copyright (c) 2020
 */

package org.pixel.graphics;

public class Color {

    //region <static>

    public static final Color RED = new Color(0xff0000ff);
    public static final Color GREEN = new Color(0x00ff00ff);
    public static final Color BLUE = new Color(0x0000ffff);
    public static final Color WHITE = new Color(0xffffffff);
    public static final Color BLACK = new Color(0x000000ff);
    public static final Color ROYAL = new Color(0x4169e1ff);
    public static final Color SLATE = new Color(0x708090ff);
    public static final Color SKY = new Color(0x87ceebff);
    public static final Color CORNFLOWER_BLUE = new Color(0x6495edff);
    public static final Color CHARTREUSE = new Color(0x7fff00ff);
    public static final Color YELLOW = new Color(0xffff00ff);
    public static final Color GOLD = new Color(0xffd700ff);
    public static final Color GOLDENROD = new Color(0xdaa520ff);
    public static final Color ORANGE = new Color(0xffa500ff);
    public static final Color LIME = new Color(0x32cd32ff);
    public static final Color FOREST = new Color(0x228b22ff);
    public static final Color OLIVE = new Color(0x6b8e23ff);
    public static final Color SCARLET = new Color(0xff341cff);
    public static final Color CORAL = new Color(0xff7f50ff);
    public static final Color SALMON = new Color(0xfa8072ff);
    public static final Color PINK = new Color(0xff69b4ff);
    public static final Color MAGENTA = new Color(0xff00ffff);
    public static final Color PURPLE = new Color(0xa020f0ff);
    public static final Color VIOLET = new Color(0xee82eeff);
    public static final Color MAROON = new Color(0xb03060ff);
    public static final Color TRANSPARENT = new Color(0x00000000);

    //endregion

    //region <properties>

    private float r = 0;
    private float g = 0;
    private float b = 0;
    private float a = 0;

    //endregion

    //region <constructors>

    /**
     * Constructor.
     */
    public Color() {
    }

    /**
     * Constructor.
     *
     * @param color The color to copy
     */
    public Color(Color color) {
        if (color != null) {
            this.r = color.r;
            this.g = color.g;
            this.b = color.b;
            this.a = color.a;
        }
    }

    /**
     * Constructor.
     *
     * @param r [0-1] Red.
     * @param g [0-1] Green.
     * @param b [0-1] Blue.
     */
    public Color(float r, float g, float b) {
        this.r = r;
        this.g = g;
        this.b = b;
        this.a = 1.0f;
    }

    /**
     * Constructor.
     *
     * @param r [0-1] Red.
     * @param g [0-1] Green.
     * @param b [0-1] Blue.
     * @param a [0-1] Alpha.
     */
    public Color(float r, float g, float b, float a) {
        this.r = r;
        this.g = g;
        this.b = b;
        this.a = a;
    }

    /**
     * Constructor.
     *
     * @param rgba8888 The color to apply in rgba 8888 format.
     */
    public Color(int rgba8888) {
        rgba8888(this, rgba8888);
    }

    //endregion

    //region <public methods>

    /**
     * Set the color based on another.
     *
     * @param color The color to copy.
     */
    public void set(Color color) {
        this.r = color.r;
        this.g = color.g;
        this.b = color.b;
        this.a = color.a;
    }

    /**
     * Set the color from the given values.
     *
     * @param r [0-1] Red.
     * @param g [0-1] Green.
     * @param b [0-1] Blue.
     */
    public void set(float r, float g, float b) {
        this.r = r;
        this.g = g;
        this.b = b;
    }

    /**
     * Set the color from the given values.
     *
     * @param r [0-1] Red.
     * @param g [0-1] Green.
     * @param b [0-1] Blue.
     * @param a [0-1] Alpha.
     */
    public void set(float r, float g, float b, float a) {
        this.r = r;
        this.g = g;
        this.b = b;
        this.a = a;
    }

    /**
     * Set the red color value.
     *
     * @param value [0-1] Red.
     */
    private void setRed(float value) {
        this.r = value;
    }

    /**
     * Set the green color value.
     *
     * @param value [0-1] Green.
     */
    private void setGreen(float value) {
        this.g = value;
    }

    /**
     * Set the blue color value.
     *
     * @param value [0-1] Blue.
     */
    private void setBlue(float value) {
        this.b = value;
    }

    /**
     * Set the alpha color value.
     *
     * @param value [0-1] Alpha.
     */
    public void setAlpha(float value) {
        this.a = value;
    }

    /**
     * Get the red color value.
     *
     * @param rgba8888 The color to apply in rgba 8888 format.
     */
    public void set(int rgba8888) {
        rgba8888(this, rgba8888);
    }

    /**
     * Get the red color value.
     *
     * @return [0-1] Red.
     */
    public float getRed() {
        return r;
    }

    /**
     * Get the green color value.
     *
     * @return [0-1] Green.
     */
    public float getGreen() {
        return g;
    }

    /**
     * Get the blue color value.
     *
     * @return [0-1] Blue.
     */
    public float getBlue() {
        return b;
    }

    /**
     * Get the alpha color value.
     *
     * @return [0-1] Alpha.
     */
    public float getAlpha() {
        return a;
    }

    //endregion

    //region <public static methods>

    /**
     * Generates a random color with custom alpha.
     *
     * @param alpha [0-1] Alpha.
     * @return The color.
     */
    public static Color random(float alpha) {
        Color color = new Color();
        color.r = (float) Math.random();
        color.g = (float) Math.random();
        color.b = (float) Math.random();
        color.a = alpha;

        return color;
    }

    /**
     * Generates a random color.
     *
     * @return The color.
     */
    public static Color random() {
        return random(1.f);
    }

    /**
     * Applies the given color (in rgba 8888 format) to the given color.
     *
     * @param color    The color object to be applied.
     * @param rgba8888 The source color.
     */
    public static void rgba8888(Color color, int rgba8888) {
        color.r = ((rgba8888 & 0xff000000) >>> 24) / 255f;
        color.g = ((rgba8888 & 0x00ff0000) >>> 16) / 255f;
        color.b = ((rgba8888 & 0x0000ff00) >>> 8) / 255f;
        color.a = ((rgba8888 & 0x000000ff)) / 255f;
    }

    /**
     * Applies the given color (in rgba 4444 format) to the given color.
     *
     * @param color    The color object to be applied.
     * @param rgba4444 The source color.
     */
    public static void rgba4444(Color color, int rgba4444) {
        color.r = ((rgba4444 & 0x0000f000) >>> 12) / 15f;
        color.g = ((rgba4444 & 0x00000f00) >>> 8) / 15f;
        color.b = ((rgba4444 & 0x000000f0) >>> 4) / 15f;
        color.a = ((rgba4444 & 0x0000000f)) / 15f;
    }

    /**
     * Creates a color from a hexadecimal color string (for eg: #A0A0A0).
     *
     * @param hex The hexadecimal color string.
     * @return The color.
     */
    private static Color fromHex(String hex) {
        if (!hex.startsWith("#")) {
            return null;
        }

        String value;
        if (hex.length() == 4) {
            value = "#" + hex.substring(1, 2) + hex.substring(1, 2) +
                    hex.substring(2, 3) + hex.substring(2, 3) +
                    hex.substring(3, 4) + hex.substring(3, 4) +
                    "ff";

        } else if (hex.length() == 7) {
            value = hex + "ff";

        } else if (hex.length() == 9) {
            value = hex;

        } else {
            return null;
        }

        return new Color(
                Integer.valueOf(value.substring(1, 3), 16) / 255.0f,
                Integer.valueOf(value.substring(3, 5), 16) / 255.0f,
                Integer.valueOf(value.substring(5, 7), 16) / 255.0f,
                Integer.valueOf(value.substring(7, 9), 16) / 255.0f);
    }

    /**
     * Creates a color from string. Supports hexadecimal and rgb/rgba color strings (examples: #FF0000, #FF0000FF,
     * rgb(255, 0, 0), rgba(255, 0, 0, 1)).
     *
     * @param value The color in string format.
     * @return The color instance or null if unable to process.
     */
    public static Color fromString(String value) {
        value = value.trim();

        if (value.startsWith("#")) {
            return Color.fromHex(value);

        } else if (value.equalsIgnoreCase("transparent")) {
            return Color.TRANSPARENT;

        } else if (value.startsWith("rgb")) {
            int left = value.indexOf("(");
            int right = value.indexOf(")");
            if (left < 0 || right < 0) {
                // invalid format..
                return null;
            }

            value = value.substring(left + 1, right);
            String[] parts = value.split(",");

            if (parts.length == 3) {
                return new Color(Float.parseFloat(parts[0].trim()) / 255.f,
                        Float.parseFloat(parts[1].trim()) / 255.f,
                        Float.parseFloat(parts[2].trim()) / 255.f, 1.f);

            } else if (parts.length == 4) {
                return new Color(Float.parseFloat(parts[0].trim()) / 255.f,
                        Float.parseFloat(parts[1].trim()) / 255.f,
                        Float.parseFloat(parts[2].trim()) / 255.f,
                        Float.parseFloat(parts[3].trim()));
            }

        }

        return null;
    }

    //endregion
}
