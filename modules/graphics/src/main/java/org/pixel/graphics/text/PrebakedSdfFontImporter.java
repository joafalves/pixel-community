package org.pixel.graphics.text;

import org.pixel.content.ContentImporter;
import org.pixel.content.ContentImporterInfo;
import org.pixel.content.ImportContext;
import org.pixel.content.Texture;
import org.pixel.content.importer.settings.TextureImporterSettings;

import java.nio.charset.StandardCharsets;

/**
 * Loads pre-baked MTSDF fonts on any platform: an msdf-atlas-gen JSON layout ({@code fonts/ui.json}) and its atlas
 * image beside it ({@code fonts/ui.png}), decoded by the platform's texture importer. Bake them on desktop with
 * {@code SdfFontBaker}. Platforms that can generate fonts (desktop) register an importer that also reads {@code .json}
 * files; this one is for the others (Android).
 */
@ContentImporterInfo(type = SdfFont.class, extension = ".json")
public class PrebakedSdfFontImporter implements ContentImporter<SdfFont> {

    private static final int GL_LINEAR = 0x2601;
    private static final int GL_CLAMP_TO_EDGE = 0x812F;

    @Override
    public SdfFont process(ImportContext ctx) {
        String layout = ctx.getFilepath();
        if (!layout.toLowerCase().endsWith(".json")) {
            throw new IllegalArgumentException("Pre-baked fonts are .json layouts with a .png atlas: " + layout);
        }
        SdfFontData data = SdfFontData.fromJson(new String(ctx.getData(), StandardCharsets.UTF_8));
        String image = layout.substring(0, layout.length() - ".json".length()) + ".png";
        // distance fields are interpolated: linear filtering, clamped; straight (not premultiplied) channels
        TextureImporterSettings settings = TextureImporterSettings.builder().wrapSMode(GL_CLAMP_TO_EDGE)
                .wrapTMode(GL_CLAMP_TO_EDGE).minFilterMode(GL_LINEAR).magFilterMode(GL_LINEAR)
                .premultiplyAlpha(false) // the channels are distances, not colors
                .build();
        Texture atlas = ctx.getContentManager().load(image, Texture.class, settings, false); // owned by the font
        if (atlas == null) {
            throw new IllegalStateException("Atlas image not found: " + image);
        }
        try {
            return new SdfFont(data, atlas);
        } catch (RuntimeException e) {
            atlas.dispose();
            throw e;
        }
    }
}
