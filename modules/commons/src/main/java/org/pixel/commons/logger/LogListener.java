package org.pixel.commons.logger;

/**
 * Receives every message logged through the engine's loggers (the console logger and the log4j2 extension; see
 * {@link Logger#addListener}), e.g. to show recent warnings in a game's dev tools. Listeners are process-wide: with
 * several games in one process, each listener hears all of them. Called on the logging thread, so implementations
 * must be thread-safe and quick.
 */
@FunctionalInterface
public interface LogListener {

    /**
     * @param level   The level.
     * @param context The logger's context (usually a class name).
     * @param message The formatted message (parameters substituted; exceptions as their {@code toString()}).
     */
    void onLog(LogLevel level, String context, String message);
}
