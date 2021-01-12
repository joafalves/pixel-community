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
    private ContentManager contentManager;
    private ContentImporterSettings settings;
    private ByteBuffer buffer;
    private String filepath;

    /**
     * Constructor
     *
     * @param contentManager
     * @param buffer
     * @param filepath
     */
    public ImportContext(ContentManager contentManager, ByteBuffer buffer, String filepath) {
        this.contentManager = contentManager;
        this.buffer = buffer;
        this.filepath = filepath;
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

    public void setSettings(ContentImporterSettings settings) {
        this.settings = settings;
    }
}
