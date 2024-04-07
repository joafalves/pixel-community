package org.pixel.content;

import java.nio.ByteBuffer;

import org.pixel.io.FileUtils;

public class DesktopResourceLoader implements ResourceLoader {

    @Override
    public ByteBuffer load(String path) {
        return FileUtils.loadFile(path);
    }

}
