package org.pixel.content;

import org.pixel.commons.ServiceFactory;
import org.pixel.content.importer.GLES30TextureImporter;
import org.pixel.content.importer.TextImporter;
import org.pixel.content.importer.TexturePackImporter;

public class GLES30ContentManagerFactory implements ServiceFactory<ContentManager> {

    @Override
    public ContentManager create() {
        return new ContentManager(new MobileResourceLoader(),
                // MOBILE SPECIFIC IMPORTERS
                new GLES30TextureImporter(),

                // COMMON IMPORTERS
                new TextImporter(),
                new TexturePackImporter()
        );
    }

}
