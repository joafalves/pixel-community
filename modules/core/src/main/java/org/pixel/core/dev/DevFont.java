package org.pixel.core.dev;

import org.pixel.content.Texture;
import org.pixel.graphics.backend.RenderBackend;
import org.pixel.graphics.text.SdfFont;
import org.pixel.graphics.text.SdfFontData;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.util.zip.InflaterInputStream;

/**
 * The engine's built-in font (JetBrains Mono, SIL Open Font License 1.1, see {@code engine/font/OFL.txt}): an MTSDF
 * atlas layout plus its pixels as zlib-compressed RGBA8 rows, read from the class path and turned into a texture by
 * the render backend, so it needs no image decoder on any platform. Baked by the demos' {@code bakeEngineFont} task.
 */
final class DevFont {

    private static final String LAYOUT = "engine/font/jetbrains-mono.json";
    private static final String PIXELS = "engine/font/jetbrains-mono.rgba";

    private DevFont() {
    }

    /**
     * Load the font; the caller owns it ({@code dispose()} releases its atlas too).
     */
    static SdfFont load(RenderBackend backend) {
        try {
            SdfFontData data = SdfFontData.fromJson(new String(read(LAYOUT, false), StandardCharsets.UTF_8));
            byte[] rgba = read(PIXELS, true);
            int expected = data.getAtlasWidth() * data.getAtlasHeight() * 4;
            if (rgba.length != expected) {
                throw new IllegalStateException(PIXELS + " has " + rgba.length + " bytes, expected " + expected);
            }
            ByteBuffer pixels = ByteBuffer.allocateDirect(expected).order(ByteOrder.nativeOrder()).put(rgba).flip();
            Texture atlas = backend.createTexture(data.getAtlasWidth(), data.getAtlasHeight(), pixels, true);
            return new SdfFont(data, atlas);
        } catch (IOException e) {
            throw new IllegalStateException("Unable to load the engine font.", e);
        }
    }

    private static byte[] read(String path, boolean inflate) throws IOException {
        InputStream in = DevFont.class.getClassLoader().getResourceAsStream(path);
        if (in == null) {
            throw new IOException(path + " is not on the class path");
        }
        try (InputStream stream = inflate ? new InflaterInputStream(in) : in) {
            return stream.readAllBytes();
        }
    }
}
