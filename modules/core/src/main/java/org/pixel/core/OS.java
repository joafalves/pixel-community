package org.pixel.core;

public enum OS {
    WINDOWS,
    MACOS,
    LINUX,
    ANDROID,
    UNKNOWN;

    private static final OS current;

    static {
        current = detect();
    }

    private static OS detect() {
        String osName = System.getProperty("os.name").toLowerCase();
        if (osName.contains("win")) {
            return WINDOWS;
        } else if (osName.contains("mac")) {
            return MACOS;
        } else if (osName.contains("linux") || osName.contains("nix") || osName.contains("nux")) {
            return LINUX;
        } else if (osName.contains("android")) {
            return ANDROID;
        } else {
            return UNKNOWN;
        }
    }

    public static OS get() {
        return current;
    }

}
