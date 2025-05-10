package org.pixel.content;

import java.io.IOException;

public interface ResourceLoader {

    /* TODO: might be useful to have a ByteBuffer variant for performance reasons (allowing native-only mem alloc). */

    /**
     * Load a resource from the given path.
     * 
     * @param path The resource path.
     * @return The resource data.
     */
    byte[] load(String path) throws IOException;
}
