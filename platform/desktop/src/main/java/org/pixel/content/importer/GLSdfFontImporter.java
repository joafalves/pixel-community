package org.pixel.content.importer;

import org.pixel.graphics.opengl.LwjglGLApi;
import org.lwjgl.system.MemoryStack;
import org.pixel.commons.logger.Logger;
import org.pixel.commons.logger.LoggerFactory;
import org.pixel.content.ContentImporter;
import org.pixel.content.ContentImporterInfo;
import org.pixel.content.ImportContext;
import org.pixel.content.opengl.GLTexture;
import org.pixel.graphics.text.SdfFont;
import org.pixel.graphics.text.SdfFontData;
import org.pixel.graphics.text.SdfFontSettings;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

import static org.lwjgl.BufferUtils.createByteBuffer;
import static org.lwjgl.opengl.GL11C.*;
import static org.lwjgl.opengl.GL12C.GL_CLAMP_TO_EDGE;
import static org.lwjgl.stb.STBImage.*;
import static org.lwjgl.stb.STBImageWrite.stbi_write_png;

/**
 * Loads {@link SdfFont}s:
 * <ul>
 *   <li>{@code .ttf} / {@code .otf}: generates an MTSDF atlas with msdfgen ({@link SdfFontSettings}); optionally
 *   cached on disk ({@link SdfFontSettings#getCacheDirectory()}).</li>
 *   <li>{@code .json}: a pre-baked msdf-atlas-gen atlas; the image is the {@code .png} with the same name.</li>
 * </ul>
 */
@ContentImporterInfo(type = SdfFont.class, extension = {".ttf", ".otf", ".json"})
public class GLSdfFontImporter implements ContentImporter<SdfFont> {

    private static final Logger log = LoggerFactory.getLogger(GLSdfFontImporter.class);

    @Override
    public SdfFont process(ImportContext ctx) {
        SdfFontData data;
        if (ctx.getFilepath().toLowerCase().endsWith(".json")) {
            data = loadPrebaked(ctx);
        } else {
            SdfFontSettings settings = ctx.getSettings() instanceof SdfFontSettings s
                    ? s : SdfFontSettings.builder().build();
            data = loadGenerated(ctx.getData(), settings);
        }
        return new SdfFont(data, upload(data));
    }

    private SdfFontData loadPrebaked(ImportContext ctx) {
        SdfFontData data = SdfFontData.fromJson(new String(ctx.getData(), StandardCharsets.UTF_8));
        String image = ctx.getFilepath().substring(0, ctx.getFilepath().length() - ".json".length()) + ".png";
        ByteBuffer png = ctx.getContentManager().load(image, ByteBuffer.class, null, false);
        if (png == null) {
            throw new IllegalStateException("Atlas image not found: " + image);
        }
        return data.pixels(decodePng(png, data, image));
    }

    private SdfFontData loadGenerated(byte[] fontBytes, SdfFontSettings settings) {
        Path json = null, png = null;
        if (settings.getCacheDirectory() != null) {
            String key = cacheKey(fontBytes, settings);
            Path dir = Path.of(settings.getCacheDirectory());
            json = dir.resolve(key + ".json");
            png = dir.resolve(key + ".png");
            if (Files.isRegularFile(json) && Files.isRegularFile(png)) {
                try {
                    SdfFontData data = SdfFontData.fromJson(Files.readString(json));
                    byte[] image = Files.readAllBytes(png);
                    ByteBuffer buffer = createByteBuffer(image.length).put(image).flip();
                    return data.pixels(decodePng(buffer, data, png.toString()));
                } catch (IOException | RuntimeException e) {
                    log.warn("Ignoring unreadable font cache entry {0}.", json, e);
                }
            }
        }

        ByteBuffer font = createByteBuffer(fontBytes.length).put(fontBytes).flip();
        long start = System.nanoTime();
        SdfFontData data = MsdfAtlasGenerator.generate(font, settings);
        log.debug("Generated a {0}x{1} MTSDF atlas ({2} glyphs) in {3} ms.", data.getAtlasWidth(),
                data.getAtlasHeight(), data.getGlyphCount(), (System.nanoTime() - start) / 1_000_000);

        if (json != null) {
            try {
                Files.createDirectories(json.getParent());
                if (!stbi_write_png(png.toString(), data.getAtlasWidth(), data.getAtlasHeight(), 4,
                        data.getPixels(), data.getAtlasWidth() * 4)) {
                    throw new IOException("stbi_write_png failed");
                }
                Files.writeString(json, data.toJson());
            } catch (IOException e) {
                log.warn("Unable to write the font cache entry {0}.", json, e);
            }
        }
        return data;
    }

    private static ByteBuffer decodePng(ByteBuffer png, SdfFontData data, String name) {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            IntBuffer w = stack.mallocInt(1), h = stack.mallocInt(1), comp = stack.mallocInt(1);
            ByteBuffer pixels = stbi_load_from_memory(png, w, h, comp, 4);
            if (pixels == null) {
                throw new IllegalStateException("Unable to decode " + name + ": " + stbi_failure_reason());
            }
            try {
                if (w.get(0) != data.getAtlasWidth() || h.get(0) != data.getAtlasHeight()) {
                    throw new IllegalStateException("Atlas image " + name + " is " + w.get(0) + "x" + h.get(0)
                            + ", but its layout says " + data.getAtlasWidth() + "x" + data.getAtlasHeight() + ".");
                }
                // copy from a duplicate: stbi_image_free needs the buffer's position untouched
                return ByteBuffer.allocateDirect(pixels.remaining()).put(pixels.duplicate()).flip();
            } finally {
                stbi_image_free(pixels);
            }
        }
    }

    private static GLTexture upload(SdfFontData data) {
        int id = glGenTextures();
        int previous = glGetInteger(GL_TEXTURE_BINDING_2D);
        glBindTexture(GL_TEXTURE_2D, id);
        glPixelStorei(GL_UNPACK_ALIGNMENT, 4);
        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA8, data.getAtlasWidth(), data.getAtlasHeight(), 0, GL_RGBA,
                GL_UNSIGNED_BYTE, data.getPixels());
        // distance fields are interpolated: linear filtering, no mipmaps (they would average distances away)
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
        glBindTexture(GL_TEXTURE_2D, previous);
        data.pixels(null); // uploaded: let the CPU copy go
        return new GLTexture(LwjglGLApi.INSTANCE, id, data.getAtlasWidth(), data.getAtlasHeight());
    }

    private static String cacheKey(byte[] font, SdfFontSettings settings) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            digest.update(font);
            digest.update(("v1|" + settings.getGlyphSize() + "|" + settings.getDistanceRange() + "|"
                    + settings.getCharacters()).getBytes(StandardCharsets.UTF_8));
            return "sdf-" + HexFormat.of().formatHex(digest.digest(), 0, 12);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }
}
