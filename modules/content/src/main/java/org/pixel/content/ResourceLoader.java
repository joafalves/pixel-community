package org.pixel.content;

public interface ResourceLoader {
    /**
     * Load a resource from the given path.
     * 
     * @param path The resource path.
     * @return The resource data.
     */
    byte[] load(String path);
}
