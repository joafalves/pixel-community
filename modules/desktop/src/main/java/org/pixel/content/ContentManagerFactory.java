package org.pixel.content;

import org.pixel.content.importer.ByteBufferImporter;
import org.pixel.content.importer.GLFontImporter;
import org.pixel.content.importer.GLTextureImporter;
import org.pixel.content.importer.TextImporter;
import org.pixel.content.importer.TexturePackImporter;
import org.pixel.content.importer.VorbisAudioImporter;

public class ContentManagerFactory {

    /**
     * Create content manager with default importers.
     * 
     * @return The content manager instance.
     */
    public static ContentManager create() {
        return new ContentManager(
                // DESKTOP SPECIFIC IMPORTERS
                new GLTextureImporter(),
                new GLFontImporter(),
                new VorbisAudioImporter(),

                // COMMON IMPORTERS
                new TextImporter(),
                new TexturePackImporter(),
                new ByteBufferImporter());
    }

}
