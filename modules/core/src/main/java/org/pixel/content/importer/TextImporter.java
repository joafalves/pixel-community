/*
 * This software is available under Apache License
 * Copyright (c) 2020
 */

package org.pixel.content.importer;

import org.pixel.commons.util.TextUtils;
import org.pixel.content.ContentImporter;
import org.pixel.content.ContentImporterInfo;
import org.pixel.content.ImportContext;

@ContentImporterInfo(type = String.class, extension = ".txt")
public class TextImporter implements ContentImporter<String> {

    @Override
    public String process(ImportContext ctx) {
        return TextUtils.convertToString(ctx.getBuffer());
    }
}
