package org.pixel.content;

import java.nio.ByteBuffer;

public interface ResourceLoader {
    /**
     * Load a resource from the given path.
     * 
     * @param path
     * @return
     */
    ByteBuffer load(String path);
}
