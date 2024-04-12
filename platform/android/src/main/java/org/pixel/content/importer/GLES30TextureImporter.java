package org.pixel.content.importer;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLES30;
import android.opengl.GLUtils;
import org.pixel.commons.logger.Logger;
import org.pixel.commons.logger.LoggerFactory;
import org.pixel.content.ContentImporter;
import org.pixel.content.ContentImporterInfo;
import org.pixel.content.ImportContext;
import org.pixel.content.Texture;
import org.pixel.content.opengl.GLES30Texture;

@ContentImporterInfo(type = Texture.class, extension = {".png", ".jpeg", ".jpg", ".bmp"})
public class GLES30TextureImporter implements ContentImporter<Texture> {
    private static final Logger log = LoggerFactory.getLogger(GLES30TextureImporter.class);

    @Override
    public Texture process(ImportContext ctx) {
        // Load the bitmap from the image stream
        Bitmap bitmap = BitmapFactory.decodeByteArray(ctx.getData(), 0, ctx.getData().length);
        if (bitmap == null) {
            throw new RuntimeException("Failed to transform image stream into bitmap");
        }

        // Create texture ID
        int[] textures = new int[1];
        GLES30.glGenTextures(1, textures, 0);
        int textureId = textures[0];

        // Bind texture ID
        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, textureId);

        // Set filtering
        GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_MIN_FILTER, GLES30.GL_NEAREST);
        GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_MAG_FILTER, GLES30.GL_NEAREST);

        // Set wrapping mode
        GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_WRAP_S, GLES30.GL_REPEAT);
        GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_WRAP_T, GLES30.GL_REPEAT);

        // Load the bitmap into the bound texture
        GLUtils.texImage2D(GLES30.GL_TEXTURE_2D, 0, bitmap, 0);

        // Recycle the bitmap, as its data has been loaded into OpenGL
        GLES30Texture texture = new GLES30Texture(textureId, bitmap.getWidth(), bitmap.getHeight());
        bitmap.recycle();

        // Unbind texture
        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, 0);

        return texture;
    }
}
