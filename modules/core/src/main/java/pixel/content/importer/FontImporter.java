/*
 * This software is available under Apache License
 * Copyright (c) 2020
 */

package pixel.content.importer;

import pixel.content.*;

/**
 * @author João Filipe Alves
 */
@ContentImporterInfo(type = Font.class, extension = {".ttf", ".otf"})
public class FontImporter implements ContentImporter<Font> {
    @Override
    public Font process(ImportContext ctx) {
        FontData fontData = new FontData(ctx.getBuffer());
        return new Font(fontData);
    }
}
