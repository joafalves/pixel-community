/*
 * This software is available under Apache License
 * Copyright (c) 2020
 */

package org.pixel.content;

import org.pixel.content.importer.settings.ContentImporterSettings;

import java.nio.ByteBuffer;

/**
 * @author João Filipe Alves
 */
public class ImportContext {

    private final ContentManager contentManager;
    private final ByteBuffer buffer;
    private final String filepath;
    private final ContentImporterSettings settings;

    /**
     * Constructor
     *
     * @param contentManager
     * @param buffer
     * @param filepath
     * @param settings
     */
    public ImportContext(ContentManager contentManager, ByteBuffer buffer, String filepath,
            ContentImporterSettings settings) {
        this.contentManager = contentManager;
        this.buffer = buffer;
        this.filepath = filepath;
        this.settings = settings;
    }

    public ContentManager getContentManager() {
        return contentManager;
    }

    public ByteBuffer getBuffer() {
        return buffer;
    }

    public String getFilepath() {
        return filepath;
    }

    public ContentImporterSettings getSettings() {
        return settings;
    }
}
