package org.pixel.ext.flux;

import org.pixel.commons.Color;
import org.pixel.graphics.render.canvas.text.SdfFont;

/**
 * Interface for Flux UI themes.
 *
 * <p>Defines all styling properties that can be customized for the Flux UI system.
 * Implementations provide different visual styles (dark, light, custom).
 */
public interface FluxTheme {

    // === Window Colors ===
    Color getWindowBg();
    Color getWindowBorder();

    // === Button Colors ===
    Color getButtonBg();
    Color getButtonHoverBg();
    Color getButtonActiveBg();
    Color getButtonText();
    Color getButtonBorder();

    // === Text Field Colors ===
    Color getTextFieldBg();
    Color getTextFieldBorder();
    Color getTextFieldFocusBorder();
    Color getTextFieldText();
    Color getTextFieldCursor();

    // === Label Colors ===
    Color getLabelText();

    // === Panel Colors ===
    Color getPanelBg();
    Color getPanelBorder();

    // === General Colors ===
    Color getText();
    Color getTextDisabled();
    Color getBorder();

    // === Typography ===
    SdfFont getFont();
    void setFont(SdfFont font);
    int getFontSize();

    // === Spacing ===
    float getPadding();
    float getItemSpacing();
    float getWindowPadding();
    float getFramePadding();

    // === Rounding ===
    float getButtonRadius();
    float getWindowRadius();
    float getPanelRadius();
    float getTextFieldRadius();

    // === Borders ===
    float getBorderWidth();
    float getButtonBorderWidth();

    /**
     * Create a copy of this theme.
     *
     * @return A new theme instance with the same values
     */
    FluxTheme copy();
}
