package org.pixel.core.dev;

import org.pixel.commons.Color;
import org.pixel.graphics.canvas.Canvas;
import org.pixel.graphics.text.SdfFont;
import org.pixel.graphics.text.TextAlign;

/**
 * A small immediate-mode UI drawn on a {@link Canvas}, for the dev tools: lines of text, key/value rows, buttons,
 * check boxes, sliders, tabs and bar graphs, laid out top to bottom inside a column. Widgets are identified by an int
 * chosen by the caller (stable from frame to frame). Allocation-free; text is passed as {@code CharSequence} and
 * copied by the canvas, so callers reuse their builders.
 */
final class DevUi {

    static final float TEXT = 12.5f;
    static final float LINE = 18f;
    static final float PAD = 8f;

    static final Color PANEL = new Color(0.06f, 0.07f, 0.1f, 0.93f);
    static final Color BAR = new Color(0.11f, 0.13f, 0.18f, 1f);
    static final Color EDGE = new Color(0.3f, 0.34f, 0.44f, 1f);
    static final Color TEXT_COLOR = new Color(0.88f, 0.9f, 0.95f, 1f);
    static final Color MUTED = new Color(0.58f, 0.62f, 0.72f, 1f);
    static final Color ACCENT = new Color(0.3f, 0.78f, 0.75f, 1f);
    static final Color WARN = new Color(1f, 0.72f, 0.35f, 1f);
    static final Color ERROR = new Color(1f, 0.42f, 0.42f, 1f);
    static final Color GOOD = new Color(0.5f, 0.88f, 0.5f, 1f);
    static final Color HOVER = new Color(1f, 1f, 1f, 0.08f);
    static final Color CONTROL = new Color(0.17f, 0.2f, 0.27f, 1f);

    private Canvas canvas;
    private SdfFont font;
    float pointerX, pointerY;
    boolean down, pressed;
    int active; // widget being dragged (0 = none)
    private float left, top, width;
    private float y;
    private float clipTop = Float.NEGATIVE_INFINITY, clipBottom = Float.POSITIVE_INFINITY; // visible rows
    private float charWidth; // the font is monospaced: one advance per character

    void begin(Canvas canvas, SdfFont font, float pointerX, float pointerY, boolean down, boolean wasDown) {
        this.canvas = canvas;
        this.font = font;
        this.pointerX = pointerX;
        this.pointerY = pointerY;
        this.down = down;
        this.pressed = down && !wasDown;
        if (!down) {
            active = 0;
        }
        this.charWidth = font.measure("M", TEXT);
        noClip();
    }

    /** Only rows between {@code top} and {@code bottom} are visible (and can be hovered); see {@link #noClip()}. */
    void clipRows(float top, float bottom) {
        clipTop = top;
        clipBottom = bottom;
    }

    void noClip() {
        clipTop = Float.NEGATIVE_INFINITY;
        clipBottom = Float.POSITIVE_INFINITY;
    }

    Canvas canvas() {
        return canvas;
    }

    SdfFont font() {
        return font;
    }

    /** Start a column at (x, y) of the given width. */
    void column(float x, float y, float width) {
        this.left = x;
        this.top = y;
        this.width = width;
        this.y = y;
    }

    float cursorY() {
        return y;
    }

    float columnHeight() {
        return y - top;
    }

    void space(float amount) {
        y += amount;
    }

    boolean hover(float x, float y, float w, float h) {
        return pointerX >= x && pointerX < x + w && pointerY >= y && pointerY < y + h
                && pointerY >= clipTop && pointerY < clipBottom;
    }

    /** Pressed this frame inside the rectangle. */
    boolean clicked(float x, float y, float w, float h) {
        return pressed && hover(x, y, w, h);
    }

    // region widgets

    /** One line of text, cut with "…" if it is wider than the column. */
    void text(CharSequence text, Color color) {
        canvas.text(font, fit(text, width), left, y + 2).size(TEXT).fill(color);
        y += LINE;
    }

    void heading(CharSequence text) {
        y += 4;
        canvas.text(font, text, left, y + 2).size(TEXT).fill(ACCENT);
        canvas.line(left, y + LINE - 1, left + width, y + LINE - 1).width(1).color(EDGE);
        y += LINE + 3;
    }

    /** A key on the left, a value on the right. */
    void row(CharSequence key, CharSequence value, Color valueColor) {
        float keyWidth = key.length() * charWidth;
        canvas.text(font, key, left, y + 2).size(TEXT).fill(MUTED);
        canvas.text(font, fit(value, width - keyWidth - 12), left + width, y + 2).size(TEXT).align(TextAlign.RIGHT)
                .fill(valueColor);
        y += LINE;
    }

    private final StringBuilder fitted = new StringBuilder();

    /**
     * The text, or its longest prefix that fits with "…" appended (copied by the canvas right away). The font is
     * monospaced, so widths are character counts: no measuring, no allocation.
     */
    private CharSequence fit(CharSequence text, float maxWidth) {
        int fits = (int) (maxWidth / charWidth);
        if (text.length() <= fits) {
            return text;
        }
        fitted.setLength(0);
        return fitted.append(text, 0, Math.max(0, fits - 1)).append('…');
    }

    /** A button the width of the text; returns whether it was pressed. */
    boolean button(CharSequence label) {
        float w = label.length() * charWidth + 16, h = LINE + 2;
        boolean clicked = buttonAt(label, left, y, w, h);
        y += h + 4;
        return clicked;
    }

    boolean buttonAt(CharSequence label, float x, float y, float w, float h) {
        boolean hover = hover(x, y, w, h);
        canvas.roundRect(x, y, w, h, 4).fill(hover ? EDGE : CONTROL);
        canvas.text(font, label, x + w * 0.5f, y + 3).size(TEXT).align(TextAlign.CENTER).fill(TEXT_COLOR);
        return pressed && hover;
    }

    /** A check box with a label; returns the (possibly toggled) value. */
    boolean checkbox(CharSequence label, boolean value) {
        float box = 13;
        boolean hover = hover(left, y, width, LINE);
        if (hover) {
            canvas.rect(left - 4, y, width + 8, LINE).fill(HOVER);
        }
        canvas.roundRect(left, y + 2.5f, box, box, 3).fill(value ? ACCENT : CONTROL).stroke(1, EDGE);
        if (value) {
            canvas.path().moveTo(left + 3, y + 9).lineTo(left + 5.5f, y + 12).lineTo(left + 10.5f, y + 5.5f)
                    .stroke(1.8f, PANEL);
        }
        canvas.text(font, label, left + box + 8, y + 2).size(TEXT).fill(TEXT_COLOR);
        y += LINE;
        return hover && pressed ? !value : value;
    }

    /** A labelled slider; returns the (possibly dragged) value. */
    float slider(int id, CharSequence label, CharSequence valueText, float value, float min, float max) {
        canvas.text(font, label, left, y + 2).size(TEXT).fill(TEXT_COLOR);
        canvas.text(font, valueText, left + width, y + 2).size(TEXT).align(TextAlign.RIGHT).fill(MUTED);
        y += LINE;
        float trackY = y + 6, trackH = 4;
        boolean hover = hover(left, y, width, 16);
        if (hover && pressed) {
            active = id;
        }
        if (active == id && down && max > min) {
            float t = Math.max(0f, Math.min(1f, (pointerX - left) / width));
            value = min + t * (max - min);
        }
        float t = max > min ? (value - min) / (max - min) : 0f;
        canvas.roundRect(left, trackY, width, trackH, 2).fill(CONTROL);
        canvas.roundRect(left, trackY, width * t, trackH, 2).fill(ACCENT);
        canvas.circle(left + width * t, trackY + trackH * 0.5f, active == id || hover ? 7 : 6).fill(TEXT_COLOR);
        y += 18;
        return value;
    }

    /** Tabs across the column; returns the selected index. */
    int tabs(CharSequence[] names, int selected) {
        float x = left, h = LINE + 4;
        for (int i = 0; i < names.length; i++) {
            float w = names[i].length() * charWidth + 14;
            boolean hover = hover(x, y, w, h);
            if (i == selected) {
                canvas.roundRect(x, y, w, h, 4).fill(CONTROL);
                canvas.line(x + 3, y + h - 1, x + w - 3, y + h - 1).width(2).color(ACCENT);
            } else if (hover) {
                canvas.roundRect(x, y, w, h, 4).fill(HOVER);
            }
            canvas.text(font, names[i], x + w * 0.5f, y + 4).size(TEXT).align(TextAlign.CENTER)
                    .fill(i == selected ? TEXT_COLOR : MUTED);
            if (hover && pressed) {
                selected = i;
            }
            x += w + 2;
        }
        y += h + 6;
        return selected;
    }

    /**
     * Stacked bars of a ring of samples (oldest left): {@code a} at the bottom, {@code b} on top, {@code total} as a
     * faint bar behind (e.g. frame interval vs update and draw time), with reference lines and a label (top-left).
     *
     * @param head  Index of the newest sample.
     * @param scale Value at the top of the graph.
     */
    void graph(float[] total, float[] a, float[] b, int count, int head, float scale, float[] marks, float height,
               CharSequence label) {
        canvas.rect(left, y, width, height).fill(BAR);
        int n = total.length;
        float barWidth = width / n;
        for (int i = 0; i < count; i++) {
            int index = ((head - i) % n + n) % n;
            float x = left + width - (i + 1) * barWidth;
            float ht = Math.min(height, total[index] / scale * height);
            float ha = Math.min(height, a[index] / scale * height);
            float hb = Math.min(height - ha, b[index] / scale * height);
            float bottom = y + height;
            if (ht >= 0.5f) { // bars thinner than half a unit aren't drawn: fewer commands
                canvas.rect(x, bottom - ht, barWidth, ht).fill(total[index] > marks[marks.length - 1] ? ERROR : EDGE);
            }
            if (ha >= 0.5f) {
                canvas.rect(x, bottom - ha, barWidth, ha).fill(ACCENT);
            }
            if (hb >= 0.5f) {
                canvas.rect(x, bottom - ha - hb, barWidth, hb).fill(GOOD);
            }
        }
        for (float mark : marks) {
            if (mark < scale) {
                float my = y + height - mark / scale * height;
                canvas.line(left, my, left + width, my).width(1).color(MUTED);
            }
        }
        canvas.text(font, label, left + 4, y + 3).size(TEXT - 2).fill(MUTED);
        y += height + 6;
    }

    /**
     * Bars of a ring of samples (oldest left), scaled to {@code scale}, with a label (top-left).
     */
    void bars(float[] values, int count, int head, float scale, Color color, float height, CharSequence label) {
        canvas.rect(left, y, width, height).fill(BAR);
        int n = values.length;
        float barWidth = width / n;
        for (int i = 0; i < count; i++) {
            int index = ((head - i) % n + n) % n;
            float h = Math.min(height, values[index] / scale * height);
            if (h >= 0.5f) {
                canvas.rect(left + width - (i + 1) * barWidth, y + height - h, barWidth, h).fill(color);
            }
        }
        canvas.text(font, label, left + 4, y + 3).size(TEXT - 2).fill(MUTED);
        y += height + 6;
    }

    /** A small button at a given place; {@code on} highlights it (e.g. the current choice of a group). */
    boolean chip(CharSequence label, float x, float y, boolean on) {
        float w = label.length() * charWidth + 12, h = LINE;
        boolean hover = hover(x, y, w, h);
        canvas.roundRect(x, y, w, h, 4).fill(on ? EDGE : hover ? CONTROL : BAR);
        canvas.text(font, label, x + w * 0.5f, y + 2).size(TEXT).align(TextAlign.CENTER)
                .fill(on ? TEXT_COLOR : MUTED);
        return pressed && hover;
    }

    /** Width of a {@link #chip}. */
    float chipWidth(CharSequence label) {
        return label.length() * charWidth + 12;
    }

    /** The left of the column. */
    float left() {
        return left;
    }

    // endregion

    // region number formatting (allocation-free)

    /** Append a number with a fixed number of decimals (0-3). */
    static StringBuilder fixed(StringBuilder sb, double value, int decimals) {
        if (Double.isNaN(value) || Double.isInfinite(value)) {
            return sb.append('-');
        }
        long scale = decimals == 0 ? 1 : decimals == 1 ? 10 : decimals == 2 ? 100 : 1000;
        long scaled = Math.round(Math.abs(value) * scale);
        if (value < 0 && scaled != 0) {
            sb.append('-');
        }
        sb.append(scaled / scale);
        if (decimals > 0) {
            sb.append('.');
            long fraction = scaled % scale;
            for (long digit = scale / 10; digit > 0; digit /= 10) {
                sb.append((char) ('0' + fraction / digit % 10));
            }
        }
        return sb;
    }

    /** Append a byte count as KB or MB. */
    static StringBuilder bytes(StringBuilder sb, long bytes) {
        if (bytes >= 1024 * 1024) {
            return fixed(sb, bytes / (1024.0 * 1024.0), 1).append(" MB");
        }
        return fixed(sb, bytes / 1024.0, 1).append(" KB");
    }

    // endregion
}
