/*
 * This software is available under Apache License
 * Copyright (c) 2020
 */

package org.pixel.content;

import org.pixel.commons.annotations.Nullable;
import org.pixel.commons.lifecycle.Disposable;
import org.pixel.commons.logger.Logger;
import org.pixel.commons.logger.LoggerFactory;
import org.pixel.commons.util.IOUtils;
import org.pixel.content.importer.*;
import org.pixel.content.importer.settings.ContentImporterSettings;

import java.nio.ByteBuffer;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author João Filipe Alves
 */
public class ContentManager implements PixelContentManager, Disposable {

    private static final Logger log = LoggerFactory.getLogger(ContentManager.class);

    private final ConcurrentHashMap<String, Object> assetCache;
    private final ConcurrentHashMap<Class, ContentImporter> importers;

    /**
     * Constructor
     */
    public ContentManager() {
        this.assetCache = new ConcurrentHashMap<>();
        this.importers = new ConcurrentHashMap<>();

        this.init();
    }

    /**
     * Init function
     */
    private void init() {
        // default importers:
        this.addContentImporter(new TextureImporter());
        this.addContentImporter(new VorbisAudioImporter());
        this.addContentImporter(new TextImporter());
        this.addContentImporter(new TexturePackImporter());
        this.addContentImporter(new FontImporter());
    }

    /**
     * @param filename
     * @param type
     * @return
     */
    private String getCacheReference(String filename, Class type) {
        return type.getCanonicalName() + ":" + filename;
    }

    /**
     * Add a custom content importer
     *
     * @param importer
     */
    public void addContentImporter(ContentImporter importer) {
        ContentImporterInfo importerDetails = importer.getClass().getAnnotation(ContentImporterInfo.class);
        if (importerDetails == null) {
            log.warn("Unable to add content importer without ContentImporter annotation");
            return;
        }

        this.importers.put(importerDetails.type(), importer);
    }

    /**
     * Load a resource file. Supports both full and relative paths (based on the project resource folder)
     *
     * @param filepath
     * @param type
     * @param <T>
     * @return
     */
    public <T> T load(String filepath, Class<T> type) {
        return load(filepath, type, null);
    }

    /**
     * Load a resource file. Supports both full and relative paths (based on the project resource folder)
     *
     * @param filepath
     * @param type
     * @param settings
     * @param <T>
     * @return
     */
    public <T> T load(String filepath, Class<T> type, @Nullable ContentImporterSettings settings) {
        String assetRef = getCacheReference(filepath, type);
        if (assetCache.contains(assetRef)) {
            Object o = assetCache.get(assetRef);
            if (type.isInstance(o)) {
                return (T) assetCache.get(assetRef);
            }
        }

        ContentImporter<T> fileImporter = this.importers.get(type);
        if (fileImporter == null) {
            log.warn("Unable to load asset due to unavailable importer for %s", type.getCanonicalName());
            return null;
        }

        ByteBuffer resourceData = IOUtils.loadFile(filepath);
        if (resourceData == null) {
            log.warn("Unable to load asset %s; target could not be found", filepath);
            return null;
        }

        ImportContext ctx = new ImportContext(this, resourceData, filepath);
        ctx.setSettings(settings);

        T asset = fileImporter.process(ctx);
        if (asset != null) {
            assetCache.put(assetRef, asset);
        }

        return asset;
    }

    /**
     * Dispose function
     */
    @Override
    public void dispose() {
        assetCache.forEach((assetName, asset) -> {
            // dispose all "disposable" assets
            if (asset instanceof Disposable) {
                ((Disposable) asset).dispose();
            }
        });

        importers.forEach((importerName, importer) -> {
            // dispose all "disposable" assets
            if (importer instanceof Disposable) {
                ((Disposable) importer).dispose();
            }
        });
    }
}
