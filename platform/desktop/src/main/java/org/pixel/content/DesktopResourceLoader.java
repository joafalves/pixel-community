package org.pixel.content;

import org.pixel.io.FileUtils;

import java.io.IOException;

public class DesktopResourceLoader implements ResourceLoader {

    @Override
    public byte[] load(String path) throws IOException {
        return FileUtils.loadFile(path);
    }

}
