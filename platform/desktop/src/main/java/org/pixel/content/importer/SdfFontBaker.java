package org.pixel.content.importer;

import org.lwjgl.system.MemoryUtil;
import org.pixel.graphics.text.SdfFontData;
import org.pixel.graphics.text.SdfFontSettings;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.lwjgl.stb.STBImageWrite.stbi_write_png;

/**
 * Bakes TrueType/OpenType fonts into MTSDF atlases ahead of time (desktop only: generation uses msdfgen). The output
 * is an msdf-atlas-gen JSON layout plus a PNG image with the same name, which any platform can load
 * ({@code content().load("fonts/ui.json", SdfFont.class)}), Android included, where fonts can't be generated:
 * <pre>
 * java -cp ... org.pixel.content.importer.SdfFontBaker fonts/ui.ttf build/ui.json [glyphSize] [distanceRange]
 * </pre>
 */
public final class SdfFontBaker {

    private SdfFontBaker() {
    }

    /**
     * Generate an atlas.
     *
     * @param font     The font file contents.
     * @param settings Glyph size, distance range and characters (the cache directory is ignored).
     * @return The font data, with its atlas pixels (RGBA8, rows top to bottom).
     */
    public static SdfFontData bake(byte[] font, SdfFontSettings settings) {
        ByteBuffer buffer = MemoryUtil.memAlloc(font.length);
        try {
            buffer.put(font).flip();
            return MsdfAtlasGenerator.generate(buffer, settings);
        } finally {
            MemoryUtil.memFree(buffer);
        }
    }

    /**
     * Write a baked atlas: the layout to {@code json}, the image to the {@code .png} beside it.
     *
     * @param data The font data, with pixels.
     * @param json The layout file (ending in {@code .json}).
     */
    public static void write(SdfFontData data, Path json) throws IOException {
        if (data.getPixels() == null) {
            throw new IllegalArgumentException("The font data has no atlas pixels.");
        }
        String name = json.toString();
        if (!name.endsWith(".json")) {
            throw new IllegalArgumentException("The layout file must end in .json: " + json);
        }
        if (json.toAbsolutePath().getParent() != null) {
            Files.createDirectories(json.toAbsolutePath().getParent());
        }
        Path png = Path.of(name.substring(0, name.length() - ".json".length()) + ".png");
        if (!stbi_write_png(png.toString(), data.getAtlasWidth(), data.getAtlasHeight(), 4, data.getPixels(),
                data.getAtlasWidth() * 4)) {
            throw new IOException("Unable to write " + png);
        }
        Files.writeString(json, data.toJson());
    }

    public static void main(String[] args) throws IOException {
        if (args.length < 2) {
            System.err.println("Usage: SdfFontBaker <font.ttf> <out.json> [glyphSize] [distanceRange]");
            System.exit(2);
        }
        SdfFontSettings.SdfFontSettingsBuilder settings = SdfFontSettings.builder();
        if (args.length > 2) {
            settings.glyphSize(Integer.parseInt(args[2]));
        }
        if (args.length > 3) {
            settings.distanceRange(Float.parseFloat(args[3]));
        }
        SdfFontData data = bake(Files.readAllBytes(Path.of(args[0])), settings.build());
        write(data, Path.of(args[1]));
        System.out.println("Baked " + data.getGlyphCount() + " glyphs into a " + data.getAtlasWidth() + "x"
                + data.getAtlasHeight() + " atlas: " + args[1]);
    }
}
