package org.pixel.content;

import org.pixel.commons.annotations.Nullable;
import org.pixel.content.importer.settings.ContentImporterSettings;

public interface PixelContentManager {
    public <T> void addContentImporter(ContentImporter<T> importer);
    public <T> T load(String filepath, Class<T> type);
    public <T> T load(String filepath, Class<T> type, @Nullable ContentImporterSettings settings);
}
