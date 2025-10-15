/*
 * This software is available under Apache License
 * Copyright (c) 2020
 */

package org.pixel.content.importer;

import org.pixel.content.ContentImporter;
import org.pixel.content.ContentImporterInfo;
import org.pixel.content.ImportContext;
import org.pixel.content.importer.settings.FontImporterSettings;
import org.pixel.graphics.render.canvas.text.GlSdfFontGenerator;
import org.pixel.graphics.render.canvas.text.SdfFont;

import java.nio.ByteBuffer;

import static org.lwjgl.BufferUtils.createByteBuffer;

/**
 * Content importer for SDF fonts.
 * Loads TrueType fonts and generates SDF texture atlases for high-quality rendering.
 */
@ContentImporterInfo(type = SdfFont.class, extension = {".ttf", ".otf"})
public class GLSdfFontImporter implements ContentImporter<SdfFont> {

    @Override
    public SdfFont process(ImportContext ctx) {
        // Convert byte array to ByteBuffer
        ByteBuffer ttfData = createByteBuffer(ctx.getData().length);
        ttfData.put(ctx.getData()).flip();

        // Get settings or use defaults
        FontImporterSettings settings = ctx.getSettings() instanceof FontImporterSettings
                ? (FontImporterSettings) ctx.getSettings()
                : FontImporterSettings.builder().build();

        int fontSize = settings.getFontSize();

        // Generate the SDF font using the generator
        GlSdfFontGenerator generator = new GlSdfFontGenerator();
        return generator.generate(ttfData, fontSize);
    }
}
