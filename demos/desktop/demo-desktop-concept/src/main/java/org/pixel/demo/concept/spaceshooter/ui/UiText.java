package org.pixel.demo.concept.spaceshooter.ui;

import org.pixel.commons.Color;
import org.pixel.demo.concept.spaceshooter.render.Palette;
import org.pixel.graphics.canvas.Canvas;
import org.pixel.graphics.text.SdfFont;
import org.pixel.graphics.text.TextAlign;
import org.pixel.graphics.text.TextBaseline;

/**
 * Text in the game's style: the pixel font with a dark outline (readable over anything), and number formatting
 * without allocation.
 * <p>
 * Texts are placed by their vertical middle ({@link TextBaseline#MIDDLE}): rows of labels, numbers and icons line up
 * on one center line. ({@code TOP} would place the font's ascender line at y, and this pixel font's ascender is
 * 1.5 em while its capitals are 1.17 em tall, so capitals would start a third of the size lower.)
 */
public final class UiText {

    private final SdfFont font;
    private final Color scratch = new Color();

    public UiText(SdfFont font) {
        this.font = font;
    }

    /**
     * @param y The text's vertical middle.
     */
    public Canvas.Text draw(Canvas canvas, CharSequence text, float x, float y, float size, Color color,
                            TextAlign align) {
        return canvas.text(font, text, Math.round(x), y).size(size).align(align)
                .baseline(TextBaseline.MIDDLE).fill(color).stroke(Math.max(1f, size * 0.16f), Palette.INK);
    }

    /** The same, faded by {@code alpha}. */
    public Canvas.Text draw(Canvas canvas, CharSequence text, float x, float y, float size, Color color, float alpha,
                            TextAlign align) {
        scratch.set(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha() * alpha);
        Canvas.Text t = draw(canvas, text, x, y, size, scratch, align);
        scratch.set(Palette.INK.getRed(), Palette.INK.getGreen(), Palette.INK.getBlue(), alpha);
        return t.stroke(Math.max(1f, size * 0.16f), scratch);
    }

    public float width(CharSequence text, float size) {
        return font.measure(text, size);
    }

    /** Append a number padded with zeros to {@code digits} (arcade style). */
    public static StringBuilder padded(StringBuilder out, int value, int digits) {
        int length = 1;
        for (int v = Math.abs(value); v >= 10; v /= 10) {
            length++;
        }
        for (int i = length; i < digits; i++) {
            out.append('0');
        }
        return out.append(value);
    }
}
