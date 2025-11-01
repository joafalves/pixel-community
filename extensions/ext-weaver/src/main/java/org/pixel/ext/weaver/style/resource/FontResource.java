package org.pixel.ext.weaver.style.resource;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class FontResource implements Resource {
    private String name;
    private String path;
    private int size;
}
