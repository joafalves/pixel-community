package org.pixel.content.importer;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLES30;
import org.pixel.content.ContentImporter;
import org.pixel.content.ContentImporterInfo;
import org.pixel.content.ImportContext;
import org.pixel.content.Texture;
import org.pixel.content.importer.settings.TextureImporterSettings;
import org.pixel.content.opengl.GLTexture;
import org.pixel.graphics.opengl.AndroidGLApi;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * Loads images into OpenGL ES textures. Like the desktop importer, colors are stored premultiplied by alpha (no
 * fringes with smooth filtering; {@link TextureImporterSettings#isPremultiplyAlpha()} turns it off for data textures),
 * and the defaults are repeat wrapping with nearest filtering (override with {@link TextureImporterSettings}).
 */
@ContentImporterInfo(type = Texture.class, extension = {".png", ".jpeg", ".jpg", ".bmp"})
public class GLES30TextureImporter implements ContentImporter<Texture> {

    @Override
    public Texture process(ImportContext ctx) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPremultiplied = false; // Android premultiplies by default; the renderer expects straight alpha
        options.inScaled = false;        // no density scaling
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
        Bitmap bitmap = BitmapFactory.decodeByteArray(ctx.getData(), 0, ctx.getData().length, options);
        if (bitmap == null) {
            throw new IllegalStateException("Unable to decode image " + ctx.getFilepath());
        }
        if (bitmap.getConfig() != Bitmap.Config.ARGB_8888) {
            // inPreferredConfig is only a preference: 16-bit images decode to RGBA_F16 (8 bytes per pixel)
            Bitmap converted = bitmap.copy(Bitmap.Config.ARGB_8888, false);
            bitmap.recycle();
            if (converted == null) {
                throw new IllegalStateException("Unable to convert image " + ctx.getFilepath() + " to RGBA8.");
            }
            bitmap = converted;
        }
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        ByteBuffer pixels = ByteBuffer.allocateDirect(width * height * 4).order(ByteOrder.nativeOrder());
        bitmap.copyPixelsToBuffer(pixels); // ARGB_8888 is stored as R, G, B, A bytes
        bitmap.recycle();
        pixels.flip();

        int wrapS = GLES30.GL_REPEAT;
        int wrapT = GLES30.GL_REPEAT;
        int minFilter = GLES30.GL_NEAREST;
        int magFilter = GLES30.GL_NEAREST;
        boolean premultiply = true;
        if (ctx.getSettings() instanceof TextureImporterSettings settings) {
            wrapS = settings.getWrapSMode();
            wrapT = settings.getWrapTMode();
            minFilter = settings.getMinFilterMode();
            magFilter = settings.getMagFilterMode();
            premultiply = settings.isPremultiplyAlpha();
        }
        if (premultiply) {
            GLTexture.premultiply(pixels); // no fringes with smooth filtering (decoded straight, premultiplied here)
        }

        AndroidGLApi gl = AndroidGLApi.INSTANCE;
        int previous = gl.getInteger(GLES30.GL_TEXTURE_BINDING_2D); // the renderer caches unit bindings: restore it
        int id = gl.genTexture();
        gl.bindTexture(GLES30.GL_TEXTURE_2D, id);
        GLES30.glPixelStorei(GLES30.GL_UNPACK_ALIGNMENT, 4);
        gl.texImage2D(GLES30.GL_TEXTURE_2D, 0, GLES30.GL_RGBA8, width, height, GLES30.GL_RGBA,
                GLES30.GL_UNSIGNED_BYTE, pixels);
        gl.texParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_WRAP_S, wrapS);
        gl.texParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_WRAP_T, wrapT);
        gl.texParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_MIN_FILTER, minFilter);
        gl.texParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_MAG_FILTER, magFilter);
        gl.bindTexture(GLES30.GL_TEXTURE_2D, previous);
        return new GLTexture(gl, id, width, height, premultiply);
    }
}
