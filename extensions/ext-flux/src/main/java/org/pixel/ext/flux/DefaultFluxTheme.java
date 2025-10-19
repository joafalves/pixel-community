package org.pixel.ext.flux;

import lombok.Data;
import org.pixel.commons.Color;
import org.pixel.graphics.render.canvas.text.SdfFont;

/**
 * Default Flux theme implementation.
 *
 * <p>Provides a dark theme with Cornflower Blue accents suitable for
 * game UIs and editor tools.
 *
 * <p>Uses Lombok @Data for automatic getter/setter generation.
 */
@Data
public class DefaultFluxTheme implements FluxTheme {

    // === Window Colors ===
    private Color windowBg = new Color(30/255f, 30/255f, 35/255f, 240/255f);
    private Color windowBorder = new Color(100/255f, 149/255f, 237/255f, 1f);

    // === Button Colors ===
    private Color buttonBg = new Color(100/255f, 149/255f, 237/255f, 1f);
    private Color buttonHoverBg = new Color(120/255f, 169/255f, 255/255f, 1f);
    private Color buttonActiveBg = new Color(80/255f, 129/255f, 217/255f, 1f);
    private Color buttonText = Color.WHITE;
    private Color buttonBorder = new Color(80/255f, 129/255f, 217/255f, 1f);

    // === Text Field Colors ===
    private Color textFieldBg = new Color(25/255f, 25/255f, 30/255f, 1f);
    private Color textFieldBorder = new Color(70/255f, 70/255f, 75/255f, 1f);
    private Color textFieldFocusBorder = new Color(100/255f, 149/255f, 237/255f, 1f);
    private Color textFieldText = Color.WHITE;
    private Color textFieldCursor = Color.WHITE;

    // === Label Colors ===
    private Color labelText = Color.WHITE;

    // === Panel Colors ===
    private Color panelBg = new Color(35/255f, 35/255f, 40/255f, 230/255f);
    private Color panelBorder = new Color(70/255f, 70/255f, 75/255f, 1f);

    // === General Colors ===
    private Color text = Color.WHITE;
    private Color textDisabled = new Color(128/255f, 128/255f, 128/255f, 1f);
    private Color border = new Color(70/255f, 70/255f, 75/255f, 1f);

    // === Typography ===
    private SdfFont font;
    private int fontSize = 16;

    // === Spacing ===
    private float padding = 8.0f;
    private float itemSpacing = 4.0f;
    private float windowPadding = 10.0f;
    private float framePadding = 4.0f;

    // === Rounding ===
    private float buttonRadius = 4.0f;
    private float windowRadius = 8.0f;
    private float panelRadius = 4.0f;
    private float textFieldRadius = 4.0f;

    // === Borders ===
    private float borderWidth = 1.0f;
    private float buttonBorderWidth = 1.0f;

    @Override
    public FluxTheme copy() {
        DefaultFluxTheme theme = new DefaultFluxTheme();

        // Window
        theme.setWindowBg(this.getWindowBg());
        theme.setWindowBorder(this.getWindowBorder());

        // Button
        theme.setButtonBg(this.getButtonBg());
        theme.setButtonHoverBg(this.getButtonHoverBg());
        theme.setButtonActiveBg(this.getButtonActiveBg());
        theme.setButtonText(this.getButtonText());
        theme.setButtonBorder(this.getButtonBorder());

        // Text Field
        theme.setTextFieldBg(this.getTextFieldBg());
        theme.setTextFieldBorder(this.getTextFieldBorder());
        theme.setTextFieldFocusBorder(this.getTextFieldFocusBorder());
        theme.setTextFieldText(this.getTextFieldText());
        theme.setTextFieldCursor(this.getTextFieldCursor());

        // Label
        theme.setLabelText(this.getLabelText());

        // Panel
        theme.setPanelBg(this.getPanelBg());
        theme.setPanelBorder(this.getPanelBorder());

        // General
        theme.setText(this.getText());
        theme.setTextDisabled(this.getTextDisabled());
        theme.setBorder(this.getBorder());

        // Typography
        theme.setFont(this.getFont());
        theme.setFontSize(this.getFontSize());

        // Spacing
        theme.setPadding(this.getPadding());
        theme.setItemSpacing(this.getItemSpacing());
        theme.setWindowPadding(this.getWindowPadding());
        theme.setFramePadding(this.getFramePadding());

        // Rounding
        theme.setButtonRadius(this.getButtonRadius());
        theme.setWindowRadius(this.getWindowRadius());
        theme.setPanelRadius(this.getPanelRadius());
        theme.setTextFieldRadius(this.getTextFieldRadius());

        // Borders
        theme.setBorderWidth(this.getBorderWidth());
        theme.setButtonBorderWidth(this.getButtonBorderWidth());

        return theme;
    }
}
