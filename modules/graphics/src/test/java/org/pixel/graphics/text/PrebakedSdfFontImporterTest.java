package org.pixel.graphics.text;

import org.junit.jupiter.api.Test;
import org.pixel.content.ContentImporter;
import org.pixel.content.ContentImporterInfo;
import org.pixel.content.ContentManager;
import org.pixel.content.ImportContext;
import org.pixel.content.Texture;
import org.pixel.content.importer.settings.TextureImporterSettings;
import org.pixel.graphics.testing.TestTexture;

import java.io.FileNotFoundException;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Pre-baked fonts on platforms without font generation: the layout from JSON, the atlas through the platform's
 * texture importer (linear, clamped).
 */
class PrebakedSdfFontImporterTest {

    @ContentImporterInfo(type = Texture.class, extension = ".png")
    static final class FakeTextureImporter implements ContentImporter<Texture> {
        TextureImporterSettings settings;
        String path;

        @Override
        public Texture process(ImportContext ctx) {
            settings = (TextureImporterSettings) ctx.getSettings();
            path = ctx.getFilepath();
            return new TestTexture(64, 32);
        }
    }

    @Test
    void loadsTheLayoutAndItsAtlasImage() {
        String json = new SdfFontData(4f, 32f, 64, 32).metrics(0.8f, 0.2f, 1.2f).addGlyph('A', 0.6f)
                .toJson();
        Map<String, byte[]> files = Map.of("fonts/ui.json", json.getBytes(StandardCharsets.UTF_8),
                "fonts/ui.png", new byte[]{1});
        FakeTextureImporter textures = new FakeTextureImporter();
        ContentManager content = new ContentManager(path -> {
            byte[] data = files.get(path);
            if (data == null) {
                throw new FileNotFoundException(path);
            }
            return data;
        }, textures, new PrebakedSdfFontImporter());

        SdfFont font = content.load("fonts/ui.json", SdfFont.class);
        assertNotNull(font);
        assertEquals("fonts/ui.png", textures.path);
        assertEquals(0x2601, textures.settings.getMinFilterMode()); // linear: distances are interpolated
        assertEquals(0x812F, textures.settings.getWrapSMode());     // clamped
        assertFalse(textures.settings.isPremultiplyAlpha());         // distances, not colors
        assertTrue(font.glyph('A') >= 0);
        assertEquals(64, font.getAtlas().getWidth());
        content.dispose();
    }
}
