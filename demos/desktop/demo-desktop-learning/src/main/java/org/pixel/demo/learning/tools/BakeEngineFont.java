package org.pixel.demo.learning.tools;

import org.pixel.content.importer.SdfFontBaker;
import org.pixel.graphics.text.SdfFontData;
import org.pixel.graphics.text.SdfFontSettings;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.zip.Deflater;
import java.util.zip.DeflaterOutputStream;

/**
 * Bakes the engine's built-in font (JetBrains Mono, SIL Open Font License 1.1, used by the dev tools overlay) into
 * {@code modules/core/src/main/resources/engine/font/}: the msdf-atlas-gen JSON layout and the atlas as raw RGBA8
 * rows, zlib-compressed ({@code .rgba}), so the core module can create the texture without an image decoder on any
 * platform. Run with {@code ./gradlew :pixel-demo-desktop-learning:bakeEngineFont} after changing the font or its
 * settings; the output is committed.
 */
public final class BakeEngineFont {

    static final String CHARACTERS = SdfFontSettings.ASCII + "…·×°±µ▲▼▶◀●○■□✓";

    private BakeEngineFont() {
    }

    public static void main(String[] args) throws IOException {
        if (args.length != 1) {
            System.err.println("Usage: BakeEngineFont <output directory>");
            System.exit(2);
        }
        Path out = Path.of(args[0]);
        byte[] font;
        try (InputStream in = BakeEngineFont.class.getClassLoader()
                .getResourceAsStream("fonts/jetbrains-mono-regular.ttf")) {
            if (in == null) {
                throw new IOException("fonts/jetbrains-mono-regular.ttf is not on the class path");
            }
            font = in.readAllBytes();
        }
        // small UI text: 32 px glyphs keep it crisp from 10 to 40 px; a 6 px range leaves room for soft edges
        SdfFontData data = SdfFontBaker.bake(font, SdfFontSettings.builder()
                .glyphSize(32).distanceRange(6f).characters(CHARACTERS).build());
        Files.createDirectories(out);
        ByteBuffer pixels = data.getPixels().duplicate();
        byte[] rgba = new byte[pixels.remaining()];
        pixels.get(rgba);
        Deflater compressor = new Deflater(Deflater.BEST_COMPRESSION);
        try (OutputStream file = Files.newOutputStream(out.resolve("jetbrains-mono.rgba"));
             DeflaterOutputStream deflater = new DeflaterOutputStream(file, compressor)) {
            deflater.write(rgba);
        } finally {
            compressor.end();
        }
        Files.writeString(out.resolve("jetbrains-mono.json"), data.toJson());
        System.out.println("Baked " + data.getGlyphCount() + " glyphs into a " + data.getAtlasWidth() + "x"
                + data.getAtlasHeight() + " atlas in " + out.toAbsolutePath());
    }
}
