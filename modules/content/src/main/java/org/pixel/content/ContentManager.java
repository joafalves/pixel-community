/*
 * This software is available under Apache License
 * Copyright (c) 2020
 */

package org.pixel.content;

import org.pixel.commons.annotation.Nullable;
import org.pixel.commons.lifecycle.Disposable;
import org.pixel.commons.logger.Logger;
import org.pixel.commons.logger.LoggerFactory;
import org.pixel.content.importer.settings.ContentImporterSettings;
import org.pixel.pipeline.DataPipeline;

import java.io.IOException;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Loads assets through the registered importers and caches them: loading the same file, type and settings again
 * returns the same instance. Cached assets are owned by the manager and disposed with it.
 * <p>
 * <b>Children</b> ({@link #child()}) give groups of assets their own lifetime, e.g. one per level or screen. A child
 * looks assets up in its own cache and then in its ancestors', loads anything else into its own cache, and frees
 * only those with {@link #unload()} or {@link #dispose()}. Importers and the data pipeline are inherited.
 * <ul>
 *   <li>Load shared assets (UI fonts, common sprites) into the parent <b>first</b>: lookups only go upwards, so an
 *   asset a child loaded is invisible to its parent and siblings, which would each load their own copy.</li>
 *   <li>A child may use its parent's assets, so unloading or disposing a manager does the same to its children
 *   first. References kept to a manager's assets are invalid once it is unloaded.</li>
 *   <li>Assets loaded with {@code useCache = false} are not tracked: the caller owns (and disposes) them.</li>
 * </ul>
 * Loading from several threads is supported; an asset whose load finishes after its manager was disposed is
 * disposed immediately and the load fails.
 */
public class ContentManager implements Disposable {

    private static final Logger log = LoggerFactory.getLogger(ContentManager.class);

    private final ResourceLoader resourceLoader;
    private final ConcurrentHashMap<CacheKey, Object> assetCache;
    private final ConcurrentHashMap<Class<?>, ContentImporter<?>> importers;
    private final ContentManager parent;
    private final String name;
    private final Set<ContentManager> children = ConcurrentHashMap.newKeySet();
    private final AtomicInteger unnamedChildren = new AtomicInteger();
    private final AtomicBoolean disposed = new AtomicBoolean();

    private DataPipeline<byte[]> dataPipeline;

    /**
     * Constructor (no importers: add them with {@link #addContentImporter}).
     *
     * @param resourceLoader The resource loader to use.
     */
    public ContentManager(ResourceLoader resourceLoader) {
        this(resourceLoader, new ContentImporter<?>[0]);
    }

    /**
     * Constructor. By default, it only includes the given importers.
     *
     * @param resourceLoader The resource loader to use.
     * @param importers      The importers to include.
     */
    public ContentManager(ResourceLoader resourceLoader, ContentImporter<?>... importers) {
        if (resourceLoader == null) {
            throw new IllegalArgumentException("Resource loader cannot be null.");
        }
        this.resourceLoader = resourceLoader;
        this.assetCache = new ConcurrentHashMap<>();
        this.importers = new ConcurrentHashMap<>();
        this.parent = null;
        this.name = "root";
        this.addContentImporter(importers);
    }

    private ContentManager(ContentManager parent, String name) {
        this.resourceLoader = parent.resourceLoader;
        this.assetCache = new ConcurrentHashMap<>();
        this.importers = new ConcurrentHashMap<>();
        this.parent = parent;
        this.name = name != null ? name : parent.name + "/child-" + parent.unnamedChildren.incrementAndGet();
    }

    // region children and lifetime

    /**
     * Create a child manager (see the class description). The caller owns it: {@link #unload()} it to free its assets
     * and reuse it, {@link #dispose()} it when done. Disposing this manager disposes its children too.
     *
     * @return A new, empty child.
     */
    public ContentManager child() {
        return child(null);
    }

    /**
     * Create a named child manager (see {@link #child()}); the name shows up in logs, e.g. {@code "level-1"}.
     *
     * @param name The child's name ({@code null} for a generated one).
     * @return A new, empty child.
     */
    public ContentManager child(@Nullable String name) {
        checkNotDisposed();
        ContentManager child = new ContentManager(this, name);
        children.add(child);
        if (disposed.get()) { // disposed concurrently, after the check: don't leave an orphan behind
            child.dispose();
            checkNotDisposed();
        }
        return child;
    }

    /**
     * @return The manager this one was created from, or {@code null} for the root.
     */
    @Nullable
    public ContentManager getParent() {
        return parent;
    }

    /**
     * @return The name given to {@link #child(String)} ({@code "root"} for a root manager).
     */
    public String getName() {
        return name;
    }

    /**
     * @return Whether {@link #dispose()} was called (directly or through a parent).
     */
    public boolean isDisposed() {
        return disposed.get();
    }

    /**
     * Dispose the assets this manager loaded (after unloading its children) and empty its cache. The manager stays
     * usable: later loads start from scratch. Assets of its ancestors are not affected.
     */
    public void unload() {
        for (ContentManager child : children) {
            child.unload();
        }
        int count = 0;
        for (CacheKey key : assetCache.keySet()) {
            Object asset = assetCache.remove(key); // remove first: a disposed asset is never handed out
            if (asset != null && disposeQuietly(asset)) {
                count++;
            }
        }
        if (count > 0) {
            log.debug("Content \"{0}\" unloaded {1} assets.", name, count);
        }
    }

    /**
     * {@link #unload()}, then dispose the children and the importers registered on this manager (inherited ones belong
     * to the ancestor that registered them). The manager cannot be used afterwards.
     */
    @Override
    public void dispose() {
        if (!disposed.compareAndSet(false, true)) {
            return; // already disposed (or being disposed by another thread)
        }
        for (ContentManager child : children) {
            disposeQuietly(child);
        }
        unload();
        importers.values().forEach(ContentManager::disposeQuietly);
        if (parent != null) {
            parent.children.remove(this);
        }
    }

    /**
     * Dispose an object if it is {@link Disposable}; a failure is logged so the remaining objects still get released.
     *
     * @return Whether the object is disposable.
     */
    private static boolean disposeQuietly(Object object) {
        if (!(object instanceof Disposable disposable)) {
            return false;
        }
        try {
            disposable.dispose();
        } catch (RuntimeException e) {
            log.error("Unable to dispose {0}.", object, e);
        }
        return true;
    }

    private void checkNotDisposed() {
        if (disposed.get()) {
            throw new IllegalStateException("Content manager \"" + name + "\" has been disposed.");
        }
    }

    @Override
    public String toString() {
        return "ContentManager[" + name + ", " + assetCache.size() + " assets, " + children.size() + " children]";
    }

    // endregion

    /**
     * Asset cache key. The importer settings are part of the key: loading the same file with different settings
     * (e.g. two font sizes) must produce different assets. Settings classes use value equality (Lombok @Data).
     */
    private record CacheKey(String filename, Class<?> type, ContentImporterSettings settings) {
    }

    /**
     * Add a custom content importer to the content manager (and its children, which inherit importers).
     *
     * @param importers The importer(s) to add.
     */
    public void addContentImporter(ContentImporter<?>... importers) {
        for (ContentImporter<?> importer : importers) {
            var importerDetails = importer.getClass().getAnnotation(ContentImporterInfo.class);
            if (importerDetails == null) {
                log.warn("Unable to add content importer {0} without ContentImporterInfo annotation.",
                        importer.getClass().getName());
                continue; // keep registering the remaining importers
            }

            log.trace("Adding content importer {0}.", importer.getClass().getSimpleName());

            this.importers.put(importerDetails.type(), importer);
        }
    }

    /**
     * Load an image resource file with a custom importer settings - supports both
     * absolute and relative paths (based on the project resource folder).
     *
     * @param filepath The filepath of the resource - supports both relative and
     *                 absolute paths.
     * @return The loaded resource or null if the resource could not be loaded.
     */
    public Texture loadTexture(String filepath) {
        return load(filepath, Texture.class);
    }

    /**
     * Load an image resource file with a custom importer settings - supports both
     * absolute and relative paths (based on the project resource folder).
     *
     * @param filepath The filepath of the resource - supports both relative and
     *                 absolute paths.
     * @param settings The settings to use for the importer.
     * @return The loaded resource or null if the resource could not be loaded.
     */
    public Texture loadTexture(String filepath, @Nullable ContentImporterSettings settings) {
        return load(filepath, Texture.class, settings, true);
    }

    /**
     * Load an image resource file with a custom importer settings - supports both
     * absolute and relative paths (based on the project resource folder).
     *
     * @param filepath The filepath of the resource - supports both relative and
     *                 absolute paths.
     * @param settings The settings to use for the importer.
     * @param useCache Whether to use the cache or not.
     * @return The loaded resource or null if the resource could not be loaded.
     */
    public Texture loadTexture(String filepath, @Nullable ContentImporterSettings settings, boolean useCache) {
        return load(filepath, Texture.class, settings, useCache);
    }

    /**
     * Load a texture pack resource file with a custom importer settings - supports
     * both absolute and relative paths (based on the project resource folder).
     *
     * @param filepath The filepath of the resource - supports both relative and
     *                 absolute paths.
     * @return The loaded resource or null if the resource could not be loaded.
     */
    public TexturePack loadTexturePack(String filepath) {
        return load(filepath, TexturePack.class);
    }

    /**
     * Load a texture pack resource file with a custom importer settings - supports
     * both absolute and relative paths (based on the project resource folder).
     *
     * @param filepath The filepath of the resource - supports both relative and
     *                 absolute paths.
     * @param settings The settings to use for the importer.
     * @return The loaded resource or null if the resource could not be loaded.
     */
    public TexturePack loadTexturePack(String filepath, @Nullable ContentImporterSettings settings) {
        return load(filepath, TexturePack.class, settings, true);
    }

    /**
     * Load a texture pack resource file with a custom importer settings - supports
     * both absolute and relative paths (based on the project resource folder).
     *
     * @param filepath The filepath of the resource - supports both relative and
     *                 absolute paths.
     * @param settings The settings to use for the importer.
     * @param useCache Whether to use the cache or not.
     * @return The loaded resource or null if the resource could not be loaded.
     */
    public TexturePack loadTexturePack(String filepath, @Nullable ContentImporterSettings settings, boolean useCache) {
        return load(filepath, TexturePack.class, settings, useCache);
    }

    /**
     * Load an audio resource file with a custom importer settings - supports both
     * absolute and relative paths (based on the project resource folder).
     *
     * @param filepath The filepath of the resource - supports both relative and
     *                 absolute paths.
     * @return The loaded resource or null if the resource could not be loaded.
     */
    public Sound loadSound(String filepath) {
        return load(filepath, Sound.class);
    }

    /**
     * Load a sound resource file with a custom importer settings - supports both
     * absolute and relative paths (based on the project resource folder).
     *
     * @param filepath The filepath of the resource - supports both relative and
     *                 absolute paths.
     * @param settings The settings to use for the importer.
     * @return The loaded resource or null if the resource could not be loaded.
     */
    public Sound loadSound(String filepath, @Nullable ContentImporterSettings settings) {
        return load(filepath, Sound.class, settings, true);
    }

    /**
     * Load a sound resource file with a custom importer settings - supports both
     * absolute and relative paths (based on the project resource folder).
     *
     * @param filepath The filepath of the resource - supports both relative and
     *                 absolute paths.
     * @param settings The settings to use for the importer.
     * @param useCache Whether to use the cache or not.
     * @return The loaded resource or null if the resource could not be loaded.
     */
    public Sound loadSound(String filepath, @Nullable ContentImporterSettings settings, boolean useCache) {
        return load(filepath, Sound.class, settings, useCache);
    }

    /**
     * Load a font resource file with a custom importer settings - supports both
     * absolute and relative paths (based on the project resource folder).
     *
     * @param filepath The filepath of the resource - supports both relative and
     *                 absolute paths.
     * @return The loaded resource or null if the resource could not be loaded.
     */
    public Font loadFont(String filepath) {
        return load(filepath, Font.class);
    }

    /**
     * Load a font resource file with a custom importer settings - supports both
     * absolute and relative paths (based on the project resource folder).
     *
     * @param filepath The filepath of the resource - supports both relative and
     *                 absolute paths.
     * @param settings The settings to use for the importer.
     * @return The loaded resource or null if the resource could not be loaded.
     */
    public Font loadFont(String filepath, @Nullable ContentImporterSettings settings) {
        return load(filepath, Font.class, settings, true);
    }

    /**
     * Load a font resource file with a custom importer settings - supports both
     * absolute and relative paths (based on the project resource folder).
     *
     * @param filepath The filepath of the resource - supports both relative and
     *                 absolute paths.
     * @param settings The settings to use for the importer.
     * @param useCache Whether to use the cache or not.
     * @return The loaded resource or null if the resource could not be loaded.
     */
    public Font loadFont(String filepath, @Nullable ContentImporterSettings settings, boolean useCache) {
        return load(filepath, Font.class, settings, useCache);
    }

    /**
     * Load a text resource file with a custom importer settings - supports both
     * absolute and relative paths (based on the project resource folder).
     *
     * @param filepath The filepath of the resource - supports both relative and
     *                 absolute paths.
     * @return The loaded resource or null if the resource could not be loaded.
     */
    public String loadText(String filepath) {
        return load(filepath, String.class);
    }

    /**
     * Load a text resource file with a custom importer settings - supports both
     * absolute and relative paths (based on the project resource folder).
     *
     * @param filepath The filepath of the resource - supports both relative and
     *                 absolute paths.
     * @param settings The settings to use for the importer.
     * @return The loaded resource or null if the resource could not be loaded.
     */
    public String loadText(String filepath, @Nullable ContentImporterSettings settings) {
        return load(filepath, String.class, settings, true);
    }

    /**
     * Load a text resource file with a custom importer settings - supports both
     * absolute and relative paths (based on the project resource folder).
     *
     * @param filepath The filepath of the resource - supports both relative and
     *                 absolute paths.
     * @param settings The settings to use for the importer.
     * @param useCache Whether to use the cache or not.
     * @return The loaded resource or null if the resource could not be loaded.
     */
    public String loadText(String filepath, @Nullable ContentImporterSettings settings, boolean useCache) {
        return load(filepath, String.class, settings, useCache);
    }

    /**
     * Load a resource file - supports both absolute and relative paths (based on
     * the project resource folder).
     *
     * @param filepath The filepath of the resource - supports both relative and
     *                 absolute paths.
     * @param type     The class type of the resource.
     * @param <T>      The type of the resource.
     * @return The loaded resource or null if the resource could not be loaded.
     */
    public <T> T load(String filepath, Class<T> type) {
        return load(filepath, type, null);
    }

    /**
     * Load a resource file with a custom importer settings - supports both absolute
     * and relative paths (based on the
     * project resource folder).
     *
     * @param filepath The filepath of the resource - supports both relative and
     *                 absolute paths.
     * @param type     The class type of the resource.
     * @param settings The settings to use for the importer.
     * @param <T>      The type of the resource.
     * @return The loaded resource or null if the resource could not be loaded.
     */
    public <T> T load(String filepath, Class<T> type, @Nullable ContentImporterSettings settings) {
        return load(filepath, type, settings, true);
    }

    /**
     * Load a resource file with a custom importer settings - supports both absolute
     * and relative paths (based on the
     * project resource folder).
     *
     * @param filepath The filepath of the resource - supports both relative and
     *                 absolute paths.
     * @param type     The class type of the resource.
     * @param settings The settings to use for the importer.
     * @param useCache Determines whether to use the asset cache or not.
     * @param <T>      The type of the resource.
     * @return The loaded resource or null if the resource could not be loaded.
     */
    @SuppressWarnings("unchecked")
    public <T> T load(String filepath, Class<T> type, @Nullable ContentImporterSettings settings, boolean useCache) {
        checkNotDisposed();
        CacheKey assetRef = new CacheKey(filepath, type, settings);
        if (useCache) {
            // this manager first, then its ancestors: assets already loaded higher up are shared
            for (ContentManager manager = this; manager != null; manager = manager.parent) {
                Object cached = manager.assetCache.get(assetRef);
                if (type.isInstance(cached)) {
                    return (T) cached;
                }
            }
        }

        ContentImporter<T> fileImporter = (ContentImporter<T>) findImporter(type);
        if (fileImporter == null) {
            log.warn("Unable to load asset due to unavailable importer for {0}.", type.getCanonicalName());
            return null;
        }

        byte[] resourceData;
        try {
            resourceData = this.resourceLoader.load(filepath);
        } catch (IOException e) {
            log.warn("Unable to load asset {0}; exception found!", filepath, e);
            return null;
        }
        if (resourceData == null) {
            log.warn("Unable to load asset {0}; target could not be found.", filepath);
            return null;
        }

        DataPipeline<byte[]> pipeline = getDataPipeline();
        if (pipeline != null) {
            try {
                resourceData = pipeline.begin(resourceData).get();

            } catch (InterruptedException | ExecutionException e) {
                log.error("Unable to execute data pipeline on asset {0}.", filepath, e);
                return null;
            }
        }

        // dependencies the importer loads through the context (e.g. a texture pack's image) belong to this manager
        ImportContext ctx = new ImportContext(this, resourceData, filepath, settings);
        T asset = fileImporter.process(ctx);
        if (useCache && asset != null) {
            assetCache.put(assetRef, asset);
            if (disposed.get() && assetCache.remove(assetRef, asset)) { // disposed while this was loading
                disposeQuietly(asset);
                checkNotDisposed();
            }
        }

        return asset;
    }

    private ContentImporter<?> findImporter(Class<?> type) {
        for (ContentManager manager = this; manager != null; manager = manager.parent) {
            ContentImporter<?> importer = manager.importers.get(type);
            if (importer != null) {
                return importer;
            }
        }
        return null;
    }

    /**
     * Get the data pipeline in effect: this manager's, or else the nearest ancestor's (if any).
     *
     * @return The data pipeline.
     */
    public DataPipeline<byte[]> getDataPipeline() {
        for (ContentManager manager = this; manager != null; manager = manager.parent) {
            if (manager.dataPipeline != null) {
                return manager.dataPipeline;
            }
        }
        return null;
    }

    /**
     * Set the data pipeline for this instance (and its children, unless they set their own). If defined, this
     * pipeline allows custom processing of any asset data before it's passed to the importer.
     *
     * @param dataPipeline The data pipeline to use.
     */
    public void setDataPipeline(DataPipeline<byte[]> dataPipeline) {
        this.dataPipeline = dataPipeline;
    }
}
