package org.pixel.graphics.text;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.pixel.content.importer.settings.ContentImporterSettings;

/**
 * Import settings for {@link SdfFont} atlases generated from {@code .ttf}/{@code .otf} files. Ignored for pre-baked
 * ({@code .json}) atlases, whose settings are fixed when they are baked.
 */
@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class SdfFontSettings implements ContentImporterSettings {

    /** Printable ASCII. */
    public static final String ASCII = range(0x20, 0x7E);
    /** Printable ASCII plus Latin-1 (accented letters of most Western European languages), "…" and "�". */
    public static final String LATIN_1 = ASCII + range(0xA0, 0xFF) + "…�";

    /**
     * Atlas pixels per em. Larger values keep finer detail (thin serifs) at large sizes; 48 is plenty for most UI
     * and game fonts, since the distance field stays sharp when magnified.
     */
    @Builder.Default
    private int glyphSize = 48;

    /**
     * Distance range encoded around every glyph (atlas pixels, edge to edge). Outlines, glows, shadows and weight
     * changes can reach up to half of it outside the glyph edge, scaled by {@code drawSize / glyphSize}. Raise it
     * for wide effects; the cost is atlas space.
     */
    @Builder.Default
    private float distanceRange = 8f;

    /**
     * Characters to include (code points the font doesn't have are skipped).
     */
    @Builder.Default
    private String characters = LATIN_1;

    /**
     * Directory where generated atlases are cached (as msdf-atlas-gen JSON + PNG, keyed by font and settings), or
     * {@code null} to always generate. The cached files can also be shipped as pre-baked atlases.
     */
    @Builder.Default
    private String cacheDirectory = null;

    private static String range(int first, int last) {
        StringBuilder sb = new StringBuilder();
        for (int c = first; c <= last; c++) {
            sb.appendCodePoint(c);
        }
        return sb.toString();
    }

    @Override
    public SdfFontSettings copy() {
        return toBuilder().build();
    }
}
