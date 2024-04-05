package org.pixel.content.importer;

import static org.lwjgl.opengl.GL11C.*;
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
import org.pixel.content.opengl.GLTexture;

@ContentImporterInfo(type = Texture.class, extension = {".png", ".jpeg", ".jpg", ".bmp"})
public class GLTextureImporter implements ContentImporter<Texture> {

    private static final Logger log = LoggerFactory.getLogger(GLTextureImporter.class);

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
            int textureId = glGenTextures();
            int width = w.get();
            int height = h.get();

            glBindTexture(GL_TEXTURE_2D, textureId);

            glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, width, height, 0, GL_RGBA, GL_UNSIGNED_BYTE, imageData);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, wrapS);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, wrapT);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, minFilter);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, magFilter);

            glBindTexture(GL_TEXTURE_2D, 0); // unbind
            free(imageData);

            return new GLTexture(textureId, width, height);

        } catch (Exception e) {
            log.error("Exception caught!", e);
            return null;
        }
    }
}
