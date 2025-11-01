package org.pixel.content;

import org.pixel.content.importer.*;

public class GLContentManagerFactory implements ContentManagerFactory {

    @Override
    public ContentManager create() {
        return new ContentManager(new DesktopResourceLoader(),
                // DESKTOP SPECIFIC IMPORTERS
                new GLTextureImporter(),
                new GLFontImporter(),
                new GLSdfFontImporter(),
                new ALVorbisAudioImporter(),

                // COMMON IMPORTERS
                new TextImporter(),
                new TexturePackImporter(),
                new ByteBufferImporter()
        );
    }

}
