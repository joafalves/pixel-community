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

public class ContentManager implements Disposable {

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
     * Get a reference to the asset cache.
     *
     * @param filename The filename of the asset.
     * @param type     The type of the asset.
     * @return A reference to the asset cache.
     */
    private String getCacheReference(String filename, Class type) {
        return type.getCanonicalName() + ":" + filename;
    }

    /**
     * Add a custom content importer to the content manager.
     *
     * @param importer The importer to add.
     */
    public void addContentImporter(ContentImporter importer) {
        ContentImporterInfo importerDetails = importer.getClass().getAnnotation(ContentImporterInfo.class);
        if (importerDetails == null) {
            log.warn("Unable to add content importer without ContentImporter annotation.");
            return;
        }

        this.importers.put(importerDetails.type(), importer);
    }

    /**
     * Load a resource file - supports both absolute and relative paths (based on the project resource folder).
     *
     * @param filepath The filepath of the resource - supports both relative and absolute paths.
     * @param type     The class type of the resource.
     * @param <T>      The type of the resource.
     * @return The loaded resource.
     */
    public <T> T load(String filepath, Class<T> type) {
        return load(filepath, type, null);
    }

    /**
     * Load a resource file with a custom importer settings - supports both absolute and relative paths (based on the
     * project resource folder).
     *
     * @param filepath The filepath of the resource - supports both relative and absolute paths.
     * @param type     The class type of the resource.
     * @param settings The settings to use for the importer.
     * @param <T>      The type of the resource.
     * @return The loaded resource.
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
            log.warn("Unable to load asset due to unavailable importer for '{}'.", type.getCanonicalName());
            return null;
        }

        ByteBuffer resourceData = IOUtils.loadFile(filepath);
        if (resourceData == null) {
            log.warn("Unable to load asset '{}'; target could not be found.", filepath);
            return null;
        }

        ImportContext ctx = new ImportContext(this, resourceData, filepath, settings);
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
