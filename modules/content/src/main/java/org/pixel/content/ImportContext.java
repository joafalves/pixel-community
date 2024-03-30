/*
 * This software is available under Apache License
 * Copyright (c) 2020
 */

package org.pixel.content;

import org.pixel.content.importer.settings.ContentImporterSettings;

import java.nio.ByteBuffer;

public class ImportContext {

    private final ContentManager contentManager;
    private final ByteBuffer buffer;
    private final String filepath;
    private final ContentImporterSettings settings;

    /**
     * Constructor.
     *
     * @param contentManager The content manager.
     * @param buffer         The content data buffer.
     * @param filepath       The filepath.
     * @param settings       The settings.
     */
    public ImportContext(ContentManager contentManager, ByteBuffer buffer, String filepath,
            ContentImporterSettings settings) {
        this.contentManager = contentManager;
        this.buffer = buffer;
        this.filepath = filepath;
        this.settings = settings;
    }

    /**
     * Get the content manager.
     *
     * @return The content manager.
     */
    public ContentManager getContentManager() {
        return contentManager;
    }

    /**
     * Get the content data buffer.
     * @return The content data buffer.
     */
    public ByteBuffer getBuffer() {
        return buffer;
    }

    /**
     * Get the filepath.
     * @return The filepath.
     */
    public String getFilepath() {
        return filepath;
    }

    /**
     * Get the settings.
     * @return The settings.
     */
    public ContentImporterSettings getSettings() {
        return settings;
    }
}
