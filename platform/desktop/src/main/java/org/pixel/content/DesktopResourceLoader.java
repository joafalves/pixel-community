package org.pixel.content;

import org.pixel.io.FileUtils;

public class DesktopResourceLoader implements ResourceLoader {

    @Override
    public byte[] load(String path) {
        return FileUtils.loadFile(path);
    }

}
