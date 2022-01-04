/*
 * This software is available under Apache License
 * Copyright (c) 2020
 */

package org.pixel.content.importer;

import java.nio.ByteBuffer;
import org.pixel.content.ContentImporter;
import org.pixel.content.ContentImporterInfo;
import org.pixel.content.ImportContext;

@ContentImporterInfo(type = ByteBuffer.class, extension = "*")
public class ByteBufferImporter implements ContentImporter<ByteBuffer> {

    @Override
    public ByteBuffer process(ImportContext ctx) {
        return ctx.getBuffer();
    }
}
