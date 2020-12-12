/*
 * This software is available under Apache License
 * Copyright (c) 2020
 */

package pixel.content.importer;

import org.lwjgl.system.MemoryStack;
import pixel.commons.logger.Logger;
import pixel.commons.logger.LoggerFactory;
import pixel.content.ContentImporter;
import pixel.content.ContentImporterInfo;
import pixel.content.ImportContext;
import pixel.content.Texture;
import pixel.content.importer.settings.TextureImporterSettings;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.stb.STBImage.stbi_failure_reason;
import static org.lwjgl.stb.STBImage.stbi_load_from_memory;
import static org.lwjgl.system.libc.LibCStdlib.free;

/**
 * @author João Filipe Alves
 */
@ContentImporterInfo(type = Texture.class, extension = {".png", ".jpeg", ".jpg", ".bmp"})
public class TextureImporter implements ContentImporter<Texture> {

    private static final Logger log = LoggerFactory.getLogger(TextureImporter.class);

    @Override
    public Texture process(ImportContext ctx) {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            // prepare the buffers:
            IntBuffer w = stack.mallocInt(1);
            IntBuffer h = stack.mallocInt(1);
            IntBuffer comp = stack.mallocInt(1);

            // load image data from buffer
            ByteBuffer imageData = stbi_load_from_memory(ctx.getBuffer(), w, h, comp, 4);
            if (imageData == null) {
                throw new RuntimeException("Failed to process texture file: " + stbi_failure_reason());
            }

            int wrapS = GL_CLAMP;
            int wrapT = GL_CLAMP;
            int minFilter = GL_NEAREST;
            int magFilter = GL_NEAREST;

            if (ctx.getSettings() instanceof TextureImporterSettings) {
                var settings = (TextureImporterSettings) ctx.getSettings();
                wrapS = settings.getWrapSMode();
                wrapT = settings.getWrapTMode();
                minFilter = settings.getMinFilterMode();
                magFilter = settings.getMagFilterMode();
            }

            // create and setup texture
            Texture texture = new Texture(glGenTextures());
            texture.bind();
            texture.setTextureWrap(wrapS, wrapT);
            texture.setTextureMinMag(minFilter, magFilter);
            texture.setData(imageData, w.get(), h.get());
            texture.unbind();

            free(imageData);

            return texture;

        } catch (Exception e) {
            log.error("Exception caught: %s", e);
            return null;
        }
    }
}
