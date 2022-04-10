package org.pixel.core;

import java.util.Locale;

import static org.lwjgl.opengl.GL11C.*;

public class DeviceInfo {

    private static String VENDOR = null;
    private static String RENDERER = null;

    /**
     * Get the device vendor. This can only be called after the GL context has been created.
     *
     * @return The device vendor.
     */
    public static String getVendor() {
        if (VENDOR == null) {
            VENDOR = glGetString(GL_VENDOR);
        }
        return VENDOR;
    }

    /**
     * Get the device renderer. This can only be called after the GL context has been created.
     *
     * @return The device renderer.
     */
    public static String getRenderer() {
        if (RENDERER == null) {
            RENDERER = glGetString(GL_RENDERER);
        }
        return RENDERER;
    }

    /**
     * Determines if the current device is Intel based. This can only be called after the GL context has been created.
     *
     * @return True if the device is Intel based.
     */
    public static boolean isIntel() {
        return getVendor().toLowerCase(Locale.ROOT).contains("intel");
    }

    /**
     * Determines if the current device is AMD based. This can only be called after the GL context has been created.
     *
     * @return True if the device is AMD based.
     */
    public static boolean isAmd() {
        var vendorLowerCase = getVendor().toLowerCase(Locale.ROOT);
        return vendorLowerCase.startsWith("ati") || vendorLowerCase.contains("amd");
    }

    /**
     * Determines if the current device is NVIDIA based. This can only be called after the GL context has been created.
     *
     * @return True if the device is NVIDIA based.
     */
    public static boolean isNvidia() {
        var vendorLowerCase = getVendor().toLowerCase(Locale.ROOT);
        return vendorLowerCase.contains("nvidia")
                || vendorLowerCase.contains("nouveau")
                || vendorLowerCase.contains("geforce");
    }

    /**
     * Determines if the current device is an Apple device. This can only be called after the GL context has been created.
     *
     * @return True if the device is Apple based.
     */
    public static boolean isApple() {
        return getVendor().toLowerCase(Locale.ROOT).contains("apple");
    }
}
