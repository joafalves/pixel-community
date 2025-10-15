package org.pixel.content.importer.settings;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class FontImporterSettings implements ContentImporterSettings {
    @Builder.Default
    private int fontSize = 24;
    @Builder.Default
    private int oversampling = 1;
}
