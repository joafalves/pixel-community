/*
 * This software is available under Apache License
 * Copyright (c) 2020
 */

package pixel.content.importer;

import pixel.commons.util.TextUtils;
import pixel.content.ContentImporter;
import pixel.content.ContentImporterInfo;
import pixel.content.ImportContext;

/**
 * @author João Filipe Alves
 */
@ContentImporterInfo(type = String.class, extension = ".txt")
public class TextImporter implements ContentImporter<String> {

    @Override
    public String process(ImportContext ctx) {
        return TextUtils.convertToString(ctx.getBuffer());
    }
}
