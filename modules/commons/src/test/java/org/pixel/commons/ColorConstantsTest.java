package org.pixel.commons;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ColorConstantsTest {

    @Test
    void constantsAreReadOnly() {
        assertThrows(UnsupportedOperationException.class, () -> Color.WHITE.set(0, 0, 0));
        assertThrows(UnsupportedOperationException.class, () -> Color.RED.setAlpha(0.5f));
        assertThrows(UnsupportedOperationException.class, () -> Color.BLACK.set(Color.WHITE));
        assertEquals(1f, Color.WHITE.getRed());
        assertEquals(1f, Color.WHITE.getAlpha());
    }

    @Test
    void copiesAreMutable() {
        Color c = new Color(Color.WHITE);
        c.setRed(0f);
        assertEquals(0f, c.getRed());
        assertEquals(1f, Color.WHITE.getRed());
    }
}
