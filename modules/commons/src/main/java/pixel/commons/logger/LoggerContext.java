/*
 * This software is available under Apache License
 * Copyright (c) 2020
 */

package pixel.commons.logger;

public class LoggerContext {

    //region Fields & Properties

    private final String identifier;
    private LogLevel level;

    //endregion

    //region Constructors

    /**
     * Constructor
     *
     * @param identifier
     */
    public LoggerContext(String identifier) {
        this.identifier = identifier;
        this.level = LogLevel.DEBUG;
    }

    //endregion

    //region Public Functions

    /**
     * Get context identifier
     *
     * @return
     */
    public String getIdentifier() {
        return identifier;
    }

    /**
     * Get context log level
     *
     * @return
     */
    public LogLevel getLevel() {
        return level;
    }

    /**
     * Set context log level
     *
     * @param level
     */
    public void setLevel(LogLevel level) {
        this.level = level;
    }

    //endregion

}
