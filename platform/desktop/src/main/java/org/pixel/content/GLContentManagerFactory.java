package org.pixel.content;

import org.pixel.commons.service.ServiceFactory;
import org.pixel.content.importer.*;

public class GLContentManagerFactory implements ServiceFactory<ContentManager> {

    @Override
    public ContentManager get() {
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
