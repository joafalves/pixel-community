package org.pixel.content.importer.settings;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FontImporterSettings implements ContentImporterSettings {
    private int fontSize = 24;
    private int horizontalSpacing = 0;
    private int verticalSpacing = 0;
    private int oversampling = 1;
}
