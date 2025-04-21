package org.pixel.content.importer.settings;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FontImporterSettings implements ContentImporterSettings {
    @Builder.Default
    private int fontSize = 24;
    @Builder.Default
    private int horizontalSpacing = 0;
    @Builder.Default
    private int verticalSpacing = 0;
    @Builder.Default
    private int oversampling = 1;
}
