package org.pixel.ext.ecs;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.pixel.commons.DeltaTime;
import org.pixel.content.Font;
import org.pixel.graphics.Color;
import org.pixel.graphics.render.SpriteBatch;
import org.pixel.math.Vector2;

/**
 * Text game object.
 */
public class Text extends GameObject {

    private final List<TextLine> textLineList;
    private final Vector2 pivot;
    private Color color;
    private transient Font font;
    private String text;
    private Alignment alignment;
    private int fontSize;
    private int verticalSpacing;
    private Vector2 previousPosition;
    private boolean dirty;

    /**
     * Constructor.
     *
     * @param name The name of the game object.
     * @param font The font to use.
     * @param text The text to display.
     */
    public Text(String name, Font font, String text) {
        this(name, font, text, Color.WHITE);
    }

    /**
     * Constructor.
     *
     * @param name  The name of the game object.
     * @param font  The font to use.
     * @param text  The text to display.
     * @param color The text color.
     */
    public Text(String name, Font font, String text, Color color) {
        this(name, font, text, color, Alignment.LEFT);
    }

    /**
     * Constructor.
     *
     * @param name      The name of the game object.
     * @param font      The font to use.
     * @param text      The text to display.
     * @param color     The text color.
     * @param alignment The text alignment.
     */
    public Text(String name, Font font, String text, Color color, Alignment alignment) {
        super(name);
        this.font = font;
        this.text = text;
        this.color = color;
        this.alignment = alignment;
        this.dirty = true;
        this.pivot = Vector2.zero();
        this.fontSize = font.getFontSize();
        this.verticalSpacing = font.getVerticalSpacing();
        this.textLineList = new ArrayList<>();
    }

    @Override
    public void update(DeltaTime delta) {
        super.update(delta);

        if (!getTransform().getWorldPosition().equals(previousPosition) || dirty) {
            cacheTextData();
        }
    }

    @Override
    public void draw(DeltaTime delta, SpriteBatch spriteBatch) {
        super.draw(delta, spriteBatch);

        for (TextLine textLine : textLineList) {
            spriteBatch.drawText(font, textLine.text, textLine.position, color, fontSize);
        }
    }

    @Override
    public GameObjectContainer copy() {
        var copy = (Text) super.copy();
        copy.font = this.font;
        return copy;
    }

    /**
     * Compute and cache text line data.
     */
    private void cacheTextData() {
        textLineList.clear();

        var lines = text.split("\n");
        var totalHeight = (fontSize + verticalSpacing) * lines.length;
        var lineMaxWidth = 0;
        for (String line : lines) {
            var lineWidth = font.computeTextWidth(line, fontSize);
            if (lineWidth > lineMaxWidth) {
                lineMaxWidth = lineWidth;
            }

            textLineList.add(new TextLine(line, lineWidth));
        }

        for (int i = 0; i < textLineList.size(); i++) {
            var textLine = textLineList.get(i);
            var positionY = (fontSize + verticalSpacing) * i;
            switch (alignment) {
                case LEFT:
                    textLine.position.set(0, positionY);
                    break;
                case CENTER:
                    textLine.position.set((lineMaxWidth / 2f) - (textLine.lineWidth / 2f), positionY);
                    break;
                case RIGHT:
                    textLine.position.set(lineMaxWidth - textLine.lineWidth, positionY);
                    break;
            }
            textLine.position.add(getTransform().getWorldPosition());
            textLine.position.add(-lineMaxWidth * pivot.getX(), -totalHeight * pivot.getY());
        }

        previousPosition = new Vector2(getTransform().getWorldPosition());
        dirty = false;
    }

    /**
     * Get the text value.
     *
     * @return The text value.
     */
    public String getText() {
        return text;
    }

    /**
     * Set the text value.
     *
     * @param text The text value.
     */
    public void setText(String text) {
        this.text = text;
        this.dirty = true;
    }

    /**
     * Get the text alignment.
     *
     * @return The text alignment.
     */
    public Alignment getAlignment() {
        return alignment;
    }

    /**
     * Set the text alignment.
     *
     * @param alignment The text alignment.
     */
    public void setAlignment(Alignment alignment) {
        this.alignment = alignment;
        this.dirty = true;
    }

    /**
     * Get the draw font size.
     *
     * @return The font size.
     */
    public int getFontSize() {
        return fontSize;
    }

    /**
     * Set the draw font size (this function does not update the source font size).
     *
     * @param fontSize The font size.
     */
    public void setFontSize(int fontSize) {
        this.fontSize = fontSize;
        this.dirty = true;
    }

    /**
     * Restore the font size based on the source {@link Font#getFontSize()}.
     */
    public void restoreFontSize() {
        setFontSize(font.getFontSize());
    }

    /**
     * Set the font source of this text.
     *
     * @param font The font source.
     */
    public void setFont(Font font) {
        this.font = font;
        this.dirty = true;
    }

    /**
     * Get the font source of this text.
     *
     * @return The font source.
     */
    public Font getFont() {
        return this.font;
    }

    /**
     * Restore the vertical spacing based on the source {@link Font#getVerticalSpacing()}.
     */
    public void restoreVerticalSpacing() {
        this.setVerticalSpacing(font.getVerticalSpacing());
    }

    /**
     * Get the vertical spacing between lines.
     *
     * @return The vertical spacing.
     */
    public int getVerticalSpacing() {
        return verticalSpacing;
    }

    /**
     * Set the vertical spacing between lines.
     *
     * @param verticalSpacing The vertical spacing.
     */
    public void setVerticalSpacing(int verticalSpacing) {
        this.verticalSpacing = verticalSpacing;
        this.dirty = true;
    }

    /**
     * Get the drawing pivot of the Text.
     *
     * @return The pivot of the Text.
     */
    public Vector2 getPivot() {
        return pivot;
    }

    /**
     * Set the drawing pivot of the Text by the given vector.
     *
     * @param pivot The new pivot values of the Text.
     */
    public void setPivot(Vector2 pivot) {
        this.pivot.set(pivot);
    }

    /**
     * Set the drawing pivot of the Text by the given values.
     *
     * @param x The new x-axis value of the pivot.
     * @param y The new y-axis value of the pivot.
     */
    public void setPivot(float x, float y) {
        this.pivot.set(x, y);
        this.dirty = true;
    }

    /**
     * Get the text color.
     *
     * @return The text color.
     */
    public Color getColor() {
        return this.color;
    }

    /**
     * Set the text color.
     *
     * @param color The text color.
     */
    public void setColor(Color color) {
        this.color = color;
    }

    /**
     * Text alignment enumeration.
     */
    public enum Alignment {
        LEFT, CENTER, RIGHT;
    }

    @RequiredArgsConstructor
    private static class TextLine implements Serializable {

        private final Vector2 position = new Vector2();
        private final String text;
        private final int lineWidth;
    }
}
