package org.pixel.graphics.text;

import org.junit.jupiter.api.Test;
import org.pixel.graphics.testing.TestFonts;

import java.lang.management.ManagementFactory;

import static org.junit.jupiter.api.Assertions.*;

class TextLayoutTest {

    private static final float EPS = 1e-4f;
    private static final float SIZE = 10f; // every glyph advances 5

    private final SdfFont font = TestFonts.mono();
    private final TextLayout layout = new TextLayout();

    private String line(int l) {
        StringBuilder sb = new StringBuilder();
        for (int i = layout.lineStart(l); i < layout.lineEnd(l); i++) {
            sb.appendCodePoint(layout.codepoint(i));
        }
        return sb.toString();
    }

    @Test
    void singleLineAdvancesWithKerningAndLetterSpacing() {
        layout.layout(font, "AVA", SIZE);

        assertEquals(3, layout.glyphCount());
        assertEquals(0f, layout.x(0), EPS);
        assertEquals(4f, layout.x(1), EPS); // "AV" kerning: -0.1 em = -1
        assertEquals(9f, layout.x(2), EPS);
        assertEquals(8f, layout.y(0), EPS); // baseline = ascender
        assertEquals(14f, layout.width(), EPS);
        assertEquals(10f, layout.height(), EPS); // ascender + descender
        assertEquals(1, layout.lineCount());

        layout.reset().letterSpacing(2).layout(font, "ab", SIZE);
        assertEquals(7f, layout.x(1), EPS);
        assertEquals(12f, layout.width(), EPS); // no spacing after the last glyph
    }

    @Test
    void newlinesStartLinesAtTheLineHeight() {
        layout.reset().lineSpacing(2f).layout(font, "ab\ncd\r\n\nef", SIZE);

        assertEquals(4, layout.lineCount());
        assertEquals("ab", line(0));
        assertEquals("cd", line(1));
        assertEquals("", line(2));
        assertEquals("ef", line(3));
        assertEquals(0f, layout.x(layout.lineStart(3)), EPS);
        assertEquals(8f + 3 * 25f, layout.y(layout.lineStart(3)), EPS); // 1.25 em * 2
        assertEquals(3 * 25f + 10f, layout.height(), EPS);
    }

    @Test
    void wrapsAfterSpacesAndIgnoresTrailingSpaces() {
        layout.reset().maxWidth(40).layout(font, "one two three", SIZE); // 8 glyphs per line

        assertEquals(2, layout.lineCount());
        assertEquals("one two ", line(0));
        assertEquals("three", line(1));
        assertEquals(35f, layout.lineWidth(0), EPS);
        assertEquals(25f, layout.lineWidth(1), EPS);
        assertEquals(0f, layout.x(layout.lineStart(1)), EPS);
        assertEquals(40f, layout.boxWidth(), EPS);
        assertEquals(35f, layout.width(), EPS);
    }

    @Test
    void breaksWordsLongerThanTheLine() {
        layout.reset().maxWidth(20).layout(font, "abcdefghij", SIZE);

        assertEquals(3, layout.lineCount());
        assertEquals("abcd", line(0));
        assertEquals("efgh", line(1));
        assertEquals("ij", line(2));
        assertEquals(0f, layout.x(4), EPS);
    }

    @Test
    void aMovedWordThatStillOverflowsBreaksAgain() {
        SdfFontData data = new SdfFontData(4, 32, 64, 64).metrics(0.8f, 0.2f, 1f)
                .addGlyph(' ', 0.25f).addGlyph('i', 1f).addGlyph('W', 3f);
        SdfFont varied = new SdfFont(data, new org.pixel.graphics.testing.TestTexture(64, 64));

        layout.reset().maxWidth(10).layout(varied, "i iiiiiiiiW", 1f);

        assertEquals("i ", line(0));
        assertEquals("iiiiiiii", line(1)); // the word moved down, then "W" still did not fit
        assertEquals("W", line(2));
        for (int l = 0; l < layout.lineCount(); l++) {
            assertTrue(layout.lineWidth(l) <= 10f + EPS);
        }
    }

    @Test
    void aTrailingNewlineIsNotAnExtraLineBeyondTheLimit() {
        layout.reset().maxLines(1).layout(font, "abc\n", SIZE);
        assertEquals(1, layout.lineCount());
        assertFalse(layout.isTruncated());
        assertEquals(10f, layout.height(), EPS);

        layout.reset().maxLines(2).layout(font, "a\nb\n", SIZE);
        assertEquals(2, layout.lineCount());
        layout.reset().layout(font, "a\n", SIZE); // without a limit the empty last line is kept
        assertEquals(2, layout.lineCount());
    }

    @Test
    void alignsLinesInsideTheBox() {
        layout.reset().maxWidth(40).align(TextAlign.CENTER).layout(font, "ab\nabcd", SIZE);
        assertEquals(15f, layout.x(0), EPS); // "ab" = 10 wide: (40 - 10) / 2
        assertEquals(10f, layout.x(layout.lineStart(1)), EPS); // (40 - 20) / 2
        layout.reset().align(TextAlign.RIGHT).layout(font, "ab\nabcd", SIZE); // box = widest line (20)
        assertEquals(10f, layout.x(0), EPS);
        assertEquals(0f, layout.x(layout.lineStart(1)), EPS);
    }

    @Test
    void maxLinesCutsTheTextWithAnEllipsis() {
        layout.reset().maxWidth(40).maxLines(2).ellipsis(true).layout(font, "one two three four five", SIZE);

        assertTrue(layout.isTruncated());
        assertEquals(2, layout.lineCount());
        assertEquals("one two ", line(0));
        assertEquals("three f…", line(1)); // cut by character to fit the ellipsis
        assertTrue(layout.lineWidth(1) <= 40f + EPS);

        layout.reset().maxLines(1).ellipsis(true).layout(font, "ab\ncd", SIZE);
        assertEquals(1, layout.lineCount());
        assertEquals("ab…", line(0));

        layout.reset().maxWidth(40).maxLines(1).layout(font, "one two three", SIZE); // no ellipsis: just cut
        assertEquals("one two ", line(0));
        assertTrue(layout.isTruncated());
    }

    @Test
    void unwrappedLinesAreCutWithAnEllipsisOrOverflow() {
        layout.reset().maxWidth(30).wrap(false).ellipsis(true).layout(font, "abcdefgh\nxy", SIZE);
        assertEquals(2, layout.lineCount());
        assertEquals("abcde…", line(0));
        assertEquals(30f, layout.lineWidth(0), EPS);
        assertEquals("xy", line(1));

        layout.reset().maxWidth(30).wrap(false).layout(font, "abcdefgh", SIZE);
        assertEquals("abcdefgh", line(0));
        assertFalse(layout.isTruncated());
    }

    @Test
    void ellipsisFallsBackToThreeDots() {
        SdfFont noEllipsis = TestFonts.mono(false);
        layout.reset().maxWidth(30).wrap(false).ellipsis(true).layout(noEllipsis, "abcdefgh", SIZE);
        assertEquals("abc...", line(0));
    }

    @Test
    void tabsAndMissingCharactersAndSurrogates() {
        layout.reset().layout(font, "a\tb", SIZE);
        assertEquals(25f, layout.x(2), EPS); // tab = 4 spaces

        layout.reset().layout(font, "aé😀b", SIZE); // é and an emoji: not in the font
        assertEquals(4, layout.glyphCount());
        assertEquals(font.glyph('?'), layout.glyph(1));
        assertEquals(0x1F600, layout.codepoint(2));
        assertEquals(15f, layout.x(3), EPS);
    }

    @Test
    void emptyTextIsOneEmptyLine() {
        layout.reset().layout(font, "", SIZE);
        assertEquals(0, layout.glyphCount());
        assertEquals(1, layout.lineCount());
        assertEquals(0f, layout.width(), EPS);
        assertEquals(10f, layout.height(), EPS);
    }

    @Test
    void measureMatchesASingleLineLayout() {
        assertEquals(14f, font.measure("AVA", SIZE), EPS);
    }

    @Test
    void steadyStateLayoutDoesNotAllocate() {
        StringBuilder text = new StringBuilder();
        for (int i = 0; i < 50; i++) {
            text.append("word").append(i).append(i % 7 == 0 ? '\n' : ' ');
        }
        Runnable run = () -> layout.reset().maxWidth(120).maxLines(30).ellipsis(true).align(TextAlign.CENTER)
                .layout(font, text, SIZE);
        for (int i = 0; i < 50; i++) {
            run.run();
        }
        com.sun.management.ThreadMXBean threads = (com.sun.management.ThreadMXBean) ManagementFactory.getThreadMXBean();
        long thread = Thread.currentThread().getId();
        long before = threads.getThreadAllocatedBytes(thread);
        for (int i = 0; i < 100; i++) {
            run.run();
        }
        assertEquals(0, threads.getThreadAllocatedBytes(thread) - before);
    }

    @Test
    void measureAgreesWithTheLayout() {
        SdfFont font = org.pixel.graphics.testing.TestFonts.mono();
        for (String text : new String[]{"ab", "a\nbcd", "a\tb", "a\r\nb", "ab ab"}) {
            TextLayout layout = new TextLayout();
            layout.layout(font, text, 10f);
            assertEquals(layout.width(), font.measure(text, 10f), 1e-3f, '"' + text + '"');
        }
        TextLayout spaced = new TextLayout().letterSpacing(2f);
        spaced.layout(font, "abc", 10f);
        assertEquals(spaced.width(), font.measure("abc", 10f, 2f), 1e-3f);
    }
}
