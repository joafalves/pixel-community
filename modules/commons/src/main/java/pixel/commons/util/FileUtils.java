/*
 * This software is available under Apache License
 * Copyright (c) 2020
 */

package pixel.commons.util;

import pixel.commons.logger.Logger;
import pixel.commons.logger.LoggerFactory;

import java.io.File;

/**
 * @author João Filipe Alves
 */
public class FileUtils {

    public static final String FILE_SEPARATOR = "/";
    private static final Logger LOG = LoggerFactory.getLogger(FileUtils.class);

    /**
     * Gets the parent directory of a given path
     *
     * @param path
     * @return
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
