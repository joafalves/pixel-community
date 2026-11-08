package org.pixel.input.keyboard;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.lang.management.ManagementFactory;

import static org.junit.jupiter.api.Assertions.*;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_UNKNOWN;
import static org.lwjgl.glfw.GLFW.GLFW_PRESS;
import static org.lwjgl.glfw.GLFW.GLFW_RELEASE;
import static org.lwjgl.glfw.GLFW.GLFW_REPEAT;

/**
 * Key state from GLFW key events (the callback is called directly: no window needed).
 */
class KeyboardTest {

    private final Keyboard.KeyboardInputHandler handler = new Keyboard.KeyboardInputHandler();

    private void event(KeyboardKey key, int action) {
        handler.invoke(0L, key.getValue(), 0, action, 0);
    }

    @AfterEach
    void release() {
        for (KeyboardKey key : new KeyboardKey[]{KeyboardKey.F1, KeyboardKey.SPACE, KeyboardKey.A}) {
            event(key, GLFW_RELEASE);
        }
        Keyboard.clear();
        handler.free();
    }

    @Test
    void downPressedAndUpFollowTheEvents() {
        assertFalse(Keyboard.isKeyDown(KeyboardKey.F1));
        event(KeyboardKey.F1, GLFW_PRESS); // a code above 127
        assertTrue(Keyboard.isKeyDown(KeyboardKey.F1));
        assertTrue(Keyboard.isKeyPressed(KeyboardKey.F1));
        Keyboard.clear(); // end of frame
        assertTrue(Keyboard.isKeyDown(KeyboardKey.F1), "held");
        assertFalse(Keyboard.isKeyPressed(KeyboardKey.F1), "pressed lasts one frame");
        event(KeyboardKey.F1, GLFW_REPEAT);
        assertTrue(Keyboard.isKeyDown(KeyboardKey.F1));
        assertFalse(Keyboard.isKeyPressed(KeyboardKey.F1), "a repeat is not a new press");
        event(KeyboardKey.F1, GLFW_RELEASE);
        assertFalse(Keyboard.isKeyDown(KeyboardKey.F1));
        assertTrue(Keyboard.isKeyUp(KeyboardKey.F1));
    }

    @Test
    void unknownKeysAreIgnoredAndTheSnapshotHasTheState() {
        handler.invoke(0L, GLFW_KEY_UNKNOWN, 0, GLFW_PRESS, 0);
        event(KeyboardKey.SPACE, GLFW_PRESS);
        KeyboardState state = Keyboard.getState();
        assertTrue(state.isKeyDown(KeyboardKey.SPACE));
        assertTrue(state.isKeyPressed(KeyboardKey.SPACE));
        assertFalse(state.isKeyDown(KeyboardKey.A));
    }

    @Test
    void queriesDoNotAllocate() {
        event(KeyboardKey.F1, GLFW_PRESS);
        boolean any = false;
        for (int i = 0; i < 10_000; i++) { // warm up
            any |= Keyboard.isKeyPressed(KeyboardKey.F1) && Keyboard.isKeyDown(KeyboardKey.F1);
        }
        var threads = (com.sun.management.ThreadMXBean) ManagementFactory.getThreadMXBean();
        long before = threads.getCurrentThreadAllocatedBytes();
        for (int i = 0; i < 10_000; i++) {
            any |= Keyboard.isKeyPressed(KeyboardKey.F1) && Keyboard.isKeyDown(KeyboardKey.F1);
        }
        assertEquals(0L, threads.getCurrentThreadAllocatedBytes() - before);
        assertTrue(any);
    }
}
