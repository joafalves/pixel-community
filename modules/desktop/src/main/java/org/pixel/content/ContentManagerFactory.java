package org.pixel.content;

import org.pixel.content.importer.ByteBufferImporter;
import org.pixel.content.importer.GLFontImporter;
import org.pixel.content.importer.GLTextureImporter;
import org.pixel.content.importer.TextImporter;
import org.pixel.content.importer.TexturePackImporter;
import org.pixel.content.importer.ALVorbisAudioImporter;

public class ContentManagerFactory {

    /**
     * Create content manager with default importers.
     * 
     * @return The content manager instance.
     */
    public static ContentManager create() {
        return new ContentManager(new DesktopResourceLoader(),
                // DESKTOP SPECIFIC IMPORTERS
                new GLTextureImporter(),
                new GLFontImporter(),
                new ALVorbisAudioImporter(),

                // COMMON IMPORTERS
                new TextImporter(),
                new TexturePackImporter(),
                new ByteBufferImporter());
    }

}
