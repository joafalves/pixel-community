/*
 * This software is available under Apache License
 * Copyright (c) 2020
 */

package org.pixel.commons.logger;

public class LoggerContext {

    //region Fields & Properties

    private final String identifier;
    private LogLevel level;

    //endregion

    //region Constructors

    /**
     * Constructor.
     *
     * @param identifier Context identifier.
     */
    public LoggerContext(String identifier) {
        this.identifier = identifier;
        this.level = LogLevel.DEBUG;
    }

    //endregion

    //region Public Functions

    /**
     * Get context identifier.
     *
     * @return Context identifier.
     */
    public String getIdentifier() {
        return identifier;
    }

    /**
     * Get context log level.
     *
     * @return Context log level.
     */
    public LogLevel getLevel() {
        return level;
    }

    /**
     * Set context log level.
     *
     * @param level Context log level.
     */
    public void setLevel(LogLevel level) {
        this.level = level;
    }

    //endregion

}
