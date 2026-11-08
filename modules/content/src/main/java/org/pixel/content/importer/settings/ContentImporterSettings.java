/*
 * This software is available under Apache License
 * Copyright (c) 2020
 */

package org.pixel.content.importer.settings;

/**
 * Settings an importer reads when it loads an asset. They are part of the content cache key, compared by value
 * ({@code equals}/{@code hashCode}), so loading a file with different settings gives a different asset.
 */
public interface ContentImporterSettings {

    /**
     * A copy the content manager can keep in its cache key, so that changing these settings after a load (e.g. one
     * settings object reused for several loads) doesn't change the key of an asset already cached. Mutable settings
     * must return a copy; immutable ones can return themselves (the default).
     *
     * @return An independent copy of these settings.
     */
    default ContentImporterSettings copy() {
        return this;
    }
}
