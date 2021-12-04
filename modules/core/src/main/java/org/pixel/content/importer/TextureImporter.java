/*
 * This software is available under Apache License
 * Copyright (c) 2020
 */

package org.pixel.content.importer;

import static org.lwjgl.opengl.GL11C.GL_NEAREST;
import static org.lwjgl.opengl.GL11C.GL_REPEAT;
import static org.lwjgl.opengl.GL11C.glGenTextures;
import static org.lwjgl.stb.STBImage.stbi_failure_reason;
import static org.lwjgl.stb.STBImage.stbi_load_from_memory;
import static org.lwjgl.system.libc.LibCStdlib.free;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import org.lwjgl.system.MemoryStack;
import org.pixel.commons.logger.Logger;
import org.pixel.commons.logger.LoggerFactory;
import org.pixel.content.ContentImporter;
import org.pixel.content.ContentImporterInfo;
import org.pixel.content.ImportContext;
import org.pixel.content.Texture;
import org.pixel.content.importer.settings.TextureImporterSettings;

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

            int wrapS = GL_REPEAT;
            int wrapT = GL_REPEAT;
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
