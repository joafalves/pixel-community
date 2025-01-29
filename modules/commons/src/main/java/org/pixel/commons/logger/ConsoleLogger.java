/*
 * This software is available under Apache License
 * Copyright (c) 2020
 */

package org.pixel.commons.logger;

import java.util.ArrayList;
import java.util.List;

public class ConsoleLogger extends Logger {

    //region Format Configuration
    private static final String DEFAULT_FORMAT = "[${timestamp}] ${level} - ${context} | ${message}";

    private static volatile String formatTemplate = DEFAULT_FORMAT;
    private static volatile List<FormatSegment> formatSegments = parseTemplate(DEFAULT_FORMAT);
    //endregion

    //region Infrastructure
    private static final ThreadLocal<StringBuilder> CACHED_BUILDER =
            ThreadLocal.withInitial(() -> new StringBuilder(256));

    private static LogLevel logLevel = LogLevel.DEBUG;
    private final String context;
    //endregion

    //region Format Processing
    private enum SegmentType { LEVEL, TIMESTAMP, CONTEXT, MESSAGE, LITERAL }

    private record FormatSegment(SegmentType type, String value) {}

    private static List<FormatSegment> parseTemplate(String template) {
        List<FormatSegment> segments = new ArrayList<>();
        int length = template.length();
        int lastPos = 0;
        boolean hasTimestamp = false;

        for (int i = 0; i < length; i++) {
            if (template.charAt(i) == '$' && i + 1 < length && template.charAt(i + 1) == '{') {
                if (i > lastPos) {
                    segments.add(new FormatSegment(SegmentType.LITERAL, template.substring(lastPos, i)));
                }

                int end = template.indexOf('}', i + 2);
                if (end == -1) break;

                String placeholder = template.substring(i + 2, end);
                SegmentType type = switch (placeholder) {
                    case "level" -> SegmentType.LEVEL;
                    case "timestamp" -> SegmentType.TIMESTAMP;
                    case "context" -> SegmentType.CONTEXT;
                    case "message" -> SegmentType.MESSAGE;
                    default -> SegmentType.LITERAL;
                };

                segments.add(new FormatSegment(type, null));
                i = end;
                lastPos = i + 1;
            }
        }

        if (lastPos < length) {
            segments.add(new FormatSegment(SegmentType.LITERAL, template.substring(lastPos)));
        }

        return segments;
    }

    //endregion

    //region Constructors

    /**
     * Constructor.
     *
     * @param context Logger context.
     */
    public ConsoleLogger(String context) {
        this.context = context;
    }

    //endregion

    //region Private Functions

    private void printToConsole(LogLevel level, String message, Object... params) {
        try {
            StringBuilder sb = CACHED_BUILDER.get();
            sb.setLength(0);

            for (FormatSegment segment : formatSegments) {
                switch (segment.type()) {
                    case LEVEL -> sb.append(level);
                    case TIMESTAMP -> sb.append(System.currentTimeMillis());
                    case CONTEXT -> sb.append(context);
                    case MESSAGE -> formatMessage(sb, message, params);
                    case LITERAL -> sb.append(segment.value());
                }
            }

            // Exception handling (unchanged)
            for (Object param : params) {
                if (param instanceof Exception ex) {
                    sb.append(System.lineSeparator());
                    appendException(sb, ex);
                }
            }

            System.out.println(sb);
            if (sb.capacity() > 4096) {
                sb.setLength(4096); // prevent overflow!
            }

        } catch (Exception e) {
            System.out.println("LOGGER FAILURE: " + e);
        }
    }

    private void formatMessage(StringBuilder sb, String message, Object... params) {
        if (message == null || params == null || params.length == 0) {
            sb.append(message);
            return;
        }

        int length = message.length();
        for (int i = 0; i < length; i++) {
            char c = message.charAt(i);
            if (c == '{' && i + 1 < length) {
                int index = 0;
                int numEnd = i + 1;

                // Direct integer parsing from string
                while (numEnd < length) {
                    char digit = message.charAt(numEnd);
                    if (digit < '0' || digit > '9') break;
                    index = index * 10 + (digit - '0');
                    numEnd++;
                }

                if (numEnd < length && message.charAt(numEnd) == '}' && index < params.length) {
                    sb.append(params[index]);
                    i = numEnd;
                    continue;
                }
            }
            sb.append(c);
        }
    }

    private void appendException(StringBuilder sb, Exception ex) {
        sb.append(ex.getClass().getSimpleName()).append(": ").append(ex.getMessage());
        // For stack traces, consider limiting depth in performance-critical scenarios
        for (StackTraceElement ste : ex.getStackTrace()) {
            sb.append("\n\tat ").append(ste);
        }
    }

    //endregion

    //region Public Functions

    @Override
    public boolean isTraceEnabled() {
        return logLevel.getValue() <= LogLevel.TRACE.getValue();
    }

    @Override
    public boolean isDebugEnabled() {
        return logLevel.getValue() <= LogLevel.DEBUG.getValue();
    }

    @Override
    public boolean isInfoEnabled() {
        return logLevel.getValue() <= LogLevel.INFO.getValue();
    }

    @Override
    public boolean isWarnEnabled() {
        return logLevel.getValue() <= LogLevel.WARN.getValue();
    }

    @Override
    public boolean isErrorEnabled() {
        return logLevel.getValue() <= LogLevel.ERROR.getValue();
    }

    public static void setFormatTemplate(String template) {
        formatTemplate = template != null ? template : DEFAULT_FORMAT;
        formatSegments = parseTemplate(formatTemplate);
    }

    /**
     * Get the current log level.
     *
     * @return The current log level.
     */
    public static LogLevel getLogLevel() {
        return logLevel;
    }

    /**
     * Set the log level.
     *
     * @param logLevel The log level to set.
     */
    public static void setLogLevel(LogLevel logLevel) {
        ConsoleLogger.logLevel = logLevel;
    }

    /**
     * Log a TRACE level message.
     *
     * @param message Message to print.
     * @param params  Parameters to print.
     */
    @Override
    public void trace(String message, Object... params) {
        if (this.isTraceEnabled()) {
            this.printToConsole(LogLevel.TRACE, message, params);
        }
    }

    /**
     * Log a DEBUG level message.
     *
     * @param message Message to print.
     * @param params  Parameters to print.
     */
    @Override
    public void debug(String message, Object... params) {
        if (this.isDebugEnabled()) {
            this.printToConsole(LogLevel.DEBUG, message, params);
        }
    }

    /**
     * Log an INFO level message.
     *
     * @param message Message to print.
     * @param params  Parameters to print.
     */
    @Override
    public void info(String message, Object... params) {
        if (this.isInfoEnabled()) {
            this.printToConsole(LogLevel.INFO, message, params);
        }
    }

    /**
     * Log a WARN level message.
     *
     * @param message Message to print.
     * @param params  Parameters to print.
     */
    @Override
    public void warn(String message, Object... params) {
        if (this.isWarnEnabled()) {
            this.printToConsole(LogLevel.WARN, message, params);
        }
    }

    /**
     * Log an ERROR level message.
     *
     * @param message Message to print.
     * @param params  Parameters to print.
     */
    @Override
    public void error(String message, Object... params) {
        if (this.isErrorEnabled()) {
            this.printToConsole(LogLevel.ERROR, message, params);
        }
    }

    //endregion
}
