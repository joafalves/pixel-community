package org.pixel.ext.rune.event;

import lombok.Getter;
import lombok.Setter;
import org.pixel.commons.lifecycle.Disposable;
import org.pixel.commons.data.Pool;

/**
 * Keyboard event for Rune GUI system.
 * Uses object pooling to avoid GC pressure.
 */
@Getter
@Setter
public class RuneKeyEvent implements Disposable {
    
    /**
     * Keyboard event types.
     */
    public enum Type {
        PRESS,      // Key pressed
        RELEASE,    // Key released
        TYPED       // Character typed (with modifiers applied)
    }
    
    private static final Pool<RuneKeyEvent> POOL = new Pool<>(RuneKeyEvent::new, 50);
    
    private Type type;
    private int keyCode;        // Physical key code
    private char character;     // Character produced (for TYPED events)
    private boolean shift;
    private boolean ctrl;
    private boolean alt;
    private boolean consumed;   // Event consumed (stop propagation)
    
    /**
     * Private constructor - use obtain() to get instance.
     */
    private RuneKeyEvent() {
        reset();
    }
    
    /**
     * Obtain a key event from the pool.
     */
    public static RuneKeyEvent obtain() {
        RuneKeyEvent event = POOL.obtain();
        event.reset();
        return event;
    }
    
    /**
     * Obtain a key event with values.
     */
    public static RuneKeyEvent obtain(Type type, int keyCode, char character) {
        RuneKeyEvent event = obtain();
        event.type = type;
        event.keyCode = keyCode;
        event.character = character;
        return event;
    }
    
    /**
     * Mark this event as consumed (stops propagation).
     */
    public void consume() {
        this.consumed = true;
    }
    
    /**
     * Reset to default values.
     */
    private void reset() {
        this.type = Type.PRESS;
        this.keyCode = 0;
        this.character = '\0';
        this.shift = false;
        this.ctrl = false;
        this.alt = false;
        this.consumed = false;
    }
    
    @Override
    public void dispose() {
        reset();
        POOL.free(this);
    }
    
    @Override
    public String toString() {
        return String.format("RuneKeyEvent{type=%s, keyCode=%d, char='%c', consumed=%b}", 
            type, keyCode, character, consumed);
    }
}
