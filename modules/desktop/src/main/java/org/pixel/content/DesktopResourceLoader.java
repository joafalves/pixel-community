package org.pixel.content;

import java.nio.ByteBuffer;

import org.pixel.desktop.io.IOUtils;

public class DesktopResourceLoader implements ResourceLoader {

    @Override
    public ByteBuffer load(String path) {
        return IOUtils.loadFile(path);
    }

}
