package org.pixel.graphics.testing;

import org.pixel.graphics.text.SdfFont;
import org.pixel.graphics.text.SdfFontData;

/**
 * Fonts for tests (no GL): a monospaced font where every glyph advances 0.5 em.
 */
public final class TestFonts {

    private TestFonts() {
    }

    /**
     * Monospaced test font: letters, digits, ".", ",", "?", "!" and "…" advance 0.5 em and have a 0.4 x 0.8 em quad
     * (left 0.05, top -0.7, bottom 0.1); space has no image. Ascender 0.8, descender 0.2, line height 1.25 em.
     * Kerning: "AV" -0.1 em. Atlas: 256 x 256, glyph size 32, distance range 8.
     */
    public static SdfFontData monoData(boolean ellipsis) {
        SdfFontData data = new SdfFontData(8f, 32f, 256, 256).metrics(0.8f, 0.2f, 1.25f);
        data.addGlyph(' ', 0.5f);
        String chars = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789.,?!" + (ellipsis ? "…" : "");
        int slot = 0;
        for (int i = chars.length() - 1; i >= 0; i--) { // reverse order: the font must sort them
            float ax = (slot % 16) * 16, ay = (slot / 16) * 16;
            data.addGlyph(chars.charAt(i), 0.5f, 0.05f, -0.7f, 0.45f, 0.1f, ax, ay, ax + 14, ay + 14);
            slot++;
        }
        data.addKerning('A', 'V', -0.1f);
        return data;
    }

    public static SdfFont mono() {
        return mono(true);
    }

    public static SdfFont mono(boolean ellipsis) {
        return new SdfFont(monoData(ellipsis), new TestTexture(256, 256));
    }
}
