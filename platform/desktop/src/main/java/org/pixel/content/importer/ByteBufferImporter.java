package org.pixel.content.importer;

import org.pixel.content.ContentImporter;
import org.pixel.content.ContentImporterInfo;
import org.pixel.content.ImportContext;

import java.nio.ByteBuffer;

import static org.lwjgl.BufferUtils.createByteBuffer;

@ContentImporterInfo(type = ByteBuffer.class, extension = "*")
public class ByteBufferImporter implements ContentImporter<ByteBuffer> {
    @Override
    public ByteBuffer process(ImportContext ctx) {
        // Allocates a native buffer using MemoryUtil (LWJGL) and copies the data from the context
        ByteBuffer buffer = createByteBuffer(ctx.getData().length);
        buffer.put(ctx.getData());
        buffer.flip(); // reset position to 0
        return buffer;
    }
}
