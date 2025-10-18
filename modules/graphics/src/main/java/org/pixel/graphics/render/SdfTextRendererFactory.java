package org.pixel.graphics.render;

import org.pixel.commons.factory.Factory;

/**
 * Factory interface for creating platform-specific SdfTextRenderer instances.
 *
 * <p>SdfTextRenderer provides efficient GPU-based text rendering using
 * Signed Distance Field (SDF) techniques.
 */
public interface SdfTextRendererFactory extends Factory {

    /**
     * Create a SdfTextRenderer instance.
     *
     * @return A new SdfTextRenderer instance appropriate for the current platform
     */
    SdfTextRenderer create();
}
