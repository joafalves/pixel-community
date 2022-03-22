/*
 * This software is available under Apache License
 * Copyright (c) 2020
 */

package org.pixel.commons.util;

import java.io.File;

public class FileUtils {

    public static final String FILE_SEPARATOR = "/";
    public static final String SYSTEM_FILE_SEPARATOR = File.separator;

    /**
     * Gets the parent directory of a given path.
     *
     * @param path The input path.
     * @return The parent directory of the input path.
     */
    public static String getParentDirectory(String path) {
        int index = path.lastIndexOf(FILE_SEPARATOR);
        if (index < 0) {
            // try with system separator...
            index = path.lastIndexOf(File.separator);
        }

        return index < 0 ? path : path.substring(0, index);
    }
}
