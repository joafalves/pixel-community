package org.pixel.graphics.text;

import org.junit.jupiter.api.Test;
import org.pixel.graphics.testing.TestFonts;
import org.pixel.graphics.testing.TestTexture;

import static org.junit.jupiter.api.Assertions.*;

class SdfFontDataTest {

    private static final float EPS = 1e-5f;

    // trimmed msdf-atlas-gen output (default y origin: bottom, y up)
    private static final String MSDF_ATLAS_GEN = """
            {"atlas":{"type":"mtsdf","distanceRange":4,"distanceRangeMiddle":0,"size":32,"width":128,"height":64,
              "yOrigin":"bottom"},
             "metrics":{"emSize":1,"lineHeight":1.2,"ascender":0.9,"descender":-0.25,"underlineY":-0.1,
              "underlineThickness":0.05},
             "glyphs":[{"unicode":32,"advance":0.25},
               {"unicode":65,"advance":0.6,"planeBounds":{"left":-0.05,"bottom":-0.1,"right":0.65,"top":0.8},
                "atlasBounds":{"left":10.5,"bottom":20.5,"right":32.5,"top":49.5}}],
             "kerning":[{"unicode1":65,"unicode2":86,"advance":-0.08}]}
            """;

    @Test
    void parsesMsdfAtlasGenLayoutsIntoYDownCoordinates() {
        SdfFont font = new SdfFont(SdfFontData.fromJson(MSDF_ATLAS_GEN), new TestTexture(128, 64));

        assertEquals(4f, font.getDistanceRange(), EPS);
        assertEquals(32f, font.getGlyphSize(), EPS);
        assertEquals(0.9f, font.getAscender(), EPS);
        assertEquals(0.25f, font.getDescender(), EPS);
        assertEquals(1.2f, font.getLineHeight(), EPS);
        assertTrue(font.hasTrueDistance());

        int space = font.glyph(' ');
        assertFalse(font.isVisible(space));
        assertEquals(0.25f, font.advance(space), EPS);

        int a = font.glyph('A');
        assertTrue(font.isVisible(a));
        assertEquals(-0.05f, font.left(a), EPS);
        assertEquals(-0.8f, font.top(a), EPS);   // above the baseline
        assertEquals(0.1f, font.bottom(a), EPS); // below it
        assertEquals(10.5f / 128, font.u0(a), EPS);
        assertEquals((64 - 49.5f) / 64, font.v0(a), EPS); // atlas y flipped
        assertEquals((64 - 20.5f) / 64, font.v1(a), EPS);
        assertEquals(-0.08f, font.kerning('A', 'V'), EPS);
        assertEquals(0f, font.kerning('V', 'A'), EPS);
        assertEquals(-1, font.glyph('B'));
        assertEquals(-1, font.glyphOrFallback('B')); // no '?' or U+FFFD in this font
    }

    @Test
    void jsonRoundTripKeepsEverything() {
        SdfFontData original = TestFonts.monoData(true);
        SdfFont expected = new SdfFont(original, new TestTexture(256, 256));
        SdfFont parsed = new SdfFont(SdfFontData.fromJson(original.toJson()), new TestTexture(256, 256));

        assertEquals(expected.glyphCount(), parsed.glyphCount());
        assertEquals(expected.getAscender(), parsed.getAscender(), EPS);
        assertEquals(expected.getDescender(), parsed.getDescender(), EPS);
        assertEquals(expected.getLineHeight(), parsed.getLineHeight(), EPS);
        for (int g = 0; g < expected.glyphCount(); g++) {
            assertEquals(expected.codepoint(g), parsed.codepoint(g));
            assertEquals(expected.isVisible(g), parsed.isVisible(g));
            assertEquals(expected.advance(g), parsed.advance(g), EPS);
            assertEquals(expected.top(g), parsed.top(g), EPS);
            assertEquals(expected.right(g), parsed.right(g), EPS);
            assertEquals(expected.u1(g), parsed.u1(g), EPS);
            assertEquals(expected.v0(g), parsed.v0(g), EPS);
        }
        assertEquals(-0.1f, parsed.kerning('A', 'V'), EPS);
    }

    @Test
    void glyphsAreSortedAndLookedUp() {
        SdfFont font = TestFonts.mono();
        for (int g = 1; g < font.glyphCount(); g++) {
            assertTrue(font.codepoint(g) > font.codepoint(g - 1));
        }
        assertEquals('z', font.codepoint(font.glyph('z')));
        assertEquals('…', font.codepoint(font.glyph('…')));
        assertEquals(font.glyph('?'), font.glyphOrFallback('é'));
        assertEquals(10f, font.distanceRange(40f), EPS); // 8 px range at 32 px per em, drawn at 40
    }

    @Test
    void rejectsDuplicatesAndMismatchedAtlases() {
        SdfFontData duplicated = new SdfFontData(4, 32, 64, 64).addGlyph('b', 0.5f).addGlyph('a', 0.5f)
                .addGlyph('a', 0.5f);
        assertThrows(IllegalStateException.class, () -> new SdfFont(duplicated, new TestTexture(64, 64)));
        assertThrows(IllegalArgumentException.class,
                () -> new SdfFont(TestFonts.monoData(true), new TestTexture(128, 128)));
    }
}
