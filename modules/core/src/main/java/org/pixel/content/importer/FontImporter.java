/*
 * This software is available under Apache License
 * Copyright (c) 2020
 */

package org.pixel.content.importer;

import org.pixel.content.ContentImporter;
import org.pixel.content.ContentImporterInfo;
import org.pixel.content.Font;
import org.pixel.content.FontData;
import org.pixel.content.ImportContext;
import org.pixel.content.importer.settings.FontImporterSettings;

@ContentImporterInfo(type = Font.class, extension = {".ttf", ".otf"})
public class FontImporter implements ContentImporter<Font> {

    @Override
    public Font process(ImportContext ctx) {
        FontData fontData = new FontData(ctx.getBuffer());

        if (ctx.getSettings() instanceof FontImporterSettings) {
            var settings = (FontImporterSettings) ctx.getSettings();
            return new Font(fontData, settings.getFontSize(), settings.getHorizontalSpacing(),
                    settings.getVerticalSpacing(), settings.getOversampling());
        }

        return new Font(fontData);
    }
}
