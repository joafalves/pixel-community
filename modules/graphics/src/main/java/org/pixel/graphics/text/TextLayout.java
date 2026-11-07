package org.pixel.graphics.text;

import java.util.Arrays;

/**
 * Positions the glyphs of a text block: kerning, letter and line spacing, explicit ({@code \n}) and automatic line
 * breaks, alignment, and truncation with an ellipsis. GL-free and allocation-free once warmed up; reuse one
 * instance.
 * <p>
 * Coordinates are relative to the top-left of the block, with y pointing down: {@link #x(int)} is the pen position
 * of a glyph and {@link #y(int)} its baseline. The block is {@link #boxWidth()} wide (the maximum width when one is
 * set, otherwise the widest line) and {@link #height()} tall (from the first line's ascender to the last line's
 * descender). Lines are aligned inside the box.
 * <p>
 * Line breaking happens after spaces; a word longer than the maximum width is broken between characters. Trailing
 * spaces don't count towards a line's width. Tabs advance by four spaces. Complex shaping (ligatures, right-to-left
 * scripts) is not supported.
 */
public final class TextLayout {

    private static final int ELLIPSIS = 0x2026;

    // settings
    private float maxWidth;
    private boolean wrap = true;
    private int maxLines;
    private boolean ellipsis;
    private TextAlign align = TextAlign.LEFT;
    private float letterSpacing;
    private float lineSpacing = 1f;

    // result
    private SdfFont font;
    private float size;
    private int glyphCount;
    private int[] glyphs = new int[64];
    private int[] codepoints = new int[64];
    private float[] xs = new float[64];
    private float[] ys = new float[64];
    private int lineCount;
    private int[] lineStarts = new int[8];
    private int[] lineEnds = new int[8];
    private float[] lineWidths = new float[8];
    private float width;
    private float boxWidth;
    private float height;
    private boolean truncated;

    // layout state
    private int lineStart;
    private int breakAt;
    private float pen;
    private int previous;

    // region settings

    /**
     * Restore the default settings: no maximum width, wrapping on, no line limit, no ellipsis, left aligned, no
     * extra spacing.
     */
    public TextLayout reset() {
        maxWidth = 0f;
        wrap = true;
        maxLines = 0;
        ellipsis = false;
        align = TextAlign.LEFT;
        letterSpacing = 0f;
        lineSpacing = 1f;
        return this;
    }

    /**
     * Maximum line width (0 = unlimited). Lines longer than this wrap (see {@link #wrap(boolean)}) or are cut with
     * an ellipsis (see {@link #ellipsis(boolean)}); otherwise they overflow.
     */
    public TextLayout maxWidth(float maxWidth) {
        this.maxWidth = Math.max(0f, maxWidth);
        return this;
    }

    /**
     * Whether lines longer than the maximum width wrap (default true).
     */
    public TextLayout wrap(boolean wrap) {
        this.wrap = wrap;
        return this;
    }

    /**
     * Maximum number of lines (0 = unlimited); the rest of the text is dropped (with an ellipsis if enabled).
     */
    public TextLayout maxLines(int maxLines) {
        this.maxLines = Math.max(0, maxLines);
        return this;
    }

    /**
     * End cut lines with an ellipsis ("…", or "..." when the font has no ellipsis glyph).
     */
    public TextLayout ellipsis(boolean ellipsis) {
        this.ellipsis = ellipsis;
        return this;
    }

    public TextLayout align(TextAlign align) {
        this.align = align != null ? align : TextAlign.LEFT;
        return this;
    }

    /**
     * Extra space after every character (output units).
     */
    public TextLayout letterSpacing(float letterSpacing) {
        this.letterSpacing = letterSpacing;
        return this;
    }

    /**
     * Line height multiplier (1 = the font's line height).
     */
    public TextLayout lineSpacing(float lineSpacing) {
        this.lineSpacing = lineSpacing;
        return this;
    }

    // endregion

    /**
     * Lay out a text block.
     *
     * @param font The font.
     * @param text The text.
     * @param size The font size (output units per em).
     * @return This layout, holding the result until the next call.
     */
    public TextLayout layout(SdfFont font, CharSequence text, float size) {
        if (font == null || text == null) {
            throw new IllegalArgumentException("Font and text cannot be null.");
        }
        this.font = font;
        this.size = size;
        glyphCount = 0;
        lineCount = 0;
        truncated = false;
        lineStart = 0;
        breakAt = -1;
        pen = 0f;
        previous = -1;

        boolean skipToNewline = false;
        boolean stopped = false;
        int length = text.length();
        for (int i = 0; i < length && !stopped; ) {
            int cp = Character.codePointAt(text, i);
            i += Character.charCount(cp);
            if (cp == '\n') {
                skipToNewline = false;
                if (maxLines > 0 && lineCount + 1 >= maxLines) {
                    if (i < length) { // more text follows (a trailing newline is not a cut)
                        truncated = true;
                        if (ellipsis) {
                            appendEllipsis();
                        }
                    }
                    stopped = true;
                    break;
                }
                endLine(glyphCount);
                continue;
            }
            if (cp == '\r' || skipToNewline) {
                continue;
            }
            boolean tab = cp == '\t';
            boolean space = tab || Character.isWhitespace(cp);
            int g = font.glyphOrFallback(tab ? ' ' : cp);
            if (g < 0) {
                continue;
            }
            float advance = font.advance(g) * size * (tab ? 4 : 1);
            float x = pen + (previous >= 0 ? font.kerning(previous, cp) * size : 0f);

            if (maxWidth > 0f && !space && x + advance > maxWidth + 1e-3f && glyphCount > lineStart
                    && (wrap || ellipsis)) {
                if (wrap && (maxLines == 0 || lineCount + 1 < maxLines)) {
                    int moveFrom = breakAt > lineStart ? breakAt : glyphCount;
                    if (moveFrom < glyphCount) { // move the last word to the next line
                        float shift = xs[moveFrom];
                        endLine(moveFrom);
                        for (int k = moveFrom; k < glyphCount; k++) {
                            xs[k] -= shift;
                        }
                        pen -= shift;
                        x -= shift;
                        if (x + advance > maxWidth + 1e-3f) { // the moved word plus this character still overflow
                            if (maxLines == 0 || lineCount + 1 < maxLines) {
                                endLine(glyphCount);
                                x = 0f;
                            } else {
                                truncated = true;
                                if (ellipsis) {
                                    appendEllipsis();
                                }
                                stopped = true;
                                continue;
                            }
                        }
                    } else { // a word wider than the line: break before this character
                        endLine(moveFrom);
                        x = 0f;
                    }
                } else {
                    truncated = true;
                    if (ellipsis) {
                        appendEllipsis();
                    }
                    if (wrap) {
                        stopped = true; // out of lines
                    } else {
                        skipToNewline = true;
                    }
                    continue;
                }
            }
            add(g, cp, x);
            pen = x + advance + letterSpacing;
            previous = cp;
            if (space) {
                breakAt = glyphCount;
            }
        }
        endLine(glyphCount);
        finishBlock();
        return this;
    }

    private void add(int glyph, int codepoint, float x) {
        if (glyphCount == glyphs.length) {
            int capacity = glyphCount * 2;
            glyphs = Arrays.copyOf(glyphs, capacity);
            codepoints = Arrays.copyOf(codepoints, capacity);
            xs = Arrays.copyOf(xs, capacity);
            ys = Arrays.copyOf(ys, capacity);
        }
        glyphs[glyphCount] = glyph;
        codepoints[glyphCount] = codepoint;
        xs[glyphCount] = x;
        glyphCount++;
    }

    /** Close the current line at glyph {@code end}; glyphs from {@code end} on start the next line. */
    private void endLine(int end) {
        if (lineCount == lineStarts.length) {
            int capacity = lineCount * 2;
            lineStarts = Arrays.copyOf(lineStarts, capacity);
            lineEnds = Arrays.copyOf(lineEnds, capacity);
            lineWidths = Arrays.copyOf(lineWidths, capacity);
        }
        lineStarts[lineCount] = lineStart;
        lineEnds[lineCount] = end;
        lineWidths[lineCount] = inkWidth(lineStart, end);
        lineCount++;
        lineStart = end;
        breakAt = -1;
        if (end == glyphCount) {
            pen = 0f;
            previous = -1;
        }
    }

    /** Width of glyphs [start, end) without trailing spaces. */
    private float inkWidth(int start, int end) {
        for (int k = end - 1; k >= start; k--) {
            if (!Character.isWhitespace(codepoints[k])) {
                return xs[k] + font.advance(glyphs[k]) * size;
            }
        }
        return 0f;
    }

    /** Cut the current line so an ellipsis fits within the maximum width, then append it. */
    private void appendEllipsis() {
        int g = font.glyph(ELLIPSIS);
        int count = 1;
        int cp = ELLIPSIS;
        if (g < 0) {
            g = font.glyph('.');
            cp = '.';
            count = 3;
        }
        if (g < 0) {
            return;
        }
        float advance = font.advance(g) * size;
        float ellipsisWidth = advance * count + letterSpacing * (count - 1);
        float limit = maxWidth > 0f ? maxWidth : Float.MAX_VALUE;
        while (glyphCount > lineStart) {
            int last = glyphCount - 1;
            float end = xs[last] + font.advance(glyphs[last]) * size + letterSpacing;
            if (!Character.isWhitespace(codepoints[last]) && end + ellipsisWidth <= limit + 1e-3f) {
                break;
            }
            glyphCount--;
        }
        float x = glyphCount > lineStart
                ? xs[glyphCount - 1] + font.advance(glyphs[glyphCount - 1]) * size + letterSpacing : 0f;
        for (int k = 0; k < count; k++) {
            add(g, cp, x);
            x += advance + letterSpacing;
        }
        pen = x;
        previous = cp;
    }

    private void finishBlock() {
        width = 0f;
        for (int l = 0; l < lineCount; l++) {
            width = Math.max(width, lineWidths[l]);
        }
        boxWidth = maxWidth > 0f ? maxWidth : width;
        float lineAdvance = font.getLineHeight() * size * lineSpacing;
        float ascent = font.getAscender() * size;
        height = lineCount > 0 ? (lineCount - 1) * lineAdvance + (font.getAscender() + font.getDescender()) * size : 0f;
        float factor = align.factor();
        for (int l = 0; l < lineCount; l++) {
            float dx = (boxWidth - lineWidths[l]) * factor;
            float baseline = ascent + l * lineAdvance;
            for (int k = lineStarts[l]; k < lineEnds[l]; k++) {
                xs[k] += dx;
                ys[k] = baseline;
            }
        }
    }

    // region result

    public SdfFont font() {
        return font;
    }

    /** The alignment lines were laid out with. */
    public TextAlign alignment() {
        return align;
    }

    /** The font size the block was laid out with. */
    public float size() {
        return size;
    }

    public int glyphCount() {
        return glyphCount;
    }

    /** Font glyph index of the i-th positioned glyph. */
    public int glyph(int i) {
        return glyphs[i];
    }

    public int codepoint(int i) {
        return codepoints[i];
    }

    /** Pen x of the i-th glyph (relative to the block's left edge). */
    public float x(int i) {
        return xs[i];
    }

    /** Baseline y of the i-th glyph (relative to the block's top edge). */
    public float y(int i) {
        return ys[i];
    }

    public int lineCount() {
        return lineCount;
    }

    /** First glyph of a line. */
    public int lineStart(int line) {
        return lineStarts[line];
    }

    /** One past the last glyph of a line. */
    public int lineEnd(int line) {
        return lineEnds[line];
    }

    /** Width of a line without trailing spaces. */
    public float lineWidth(int line) {
        return lineWidths[line];
    }

    /** Width of the widest line. */
    public float width() {
        return width;
    }

    /** Width lines are aligned in: the maximum width when set, otherwise {@link #width()}. */
    public float boxWidth() {
        return boxWidth;
    }

    /** Height from the first line's ascender to the last line's descender. */
    public float height() {
        return height;
    }

    /** Whether text was cut (maximum lines, or a line without wrapping that exceeded the maximum width). */
    public boolean isTruncated() {
        return truncated;
    }

    // endregion
}
