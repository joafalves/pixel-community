package org.pixel.ext.rune.event;

import lombok.Getter;
import lombok.Setter;
import org.pixel.commons.lifecycle.Disposable;
import org.pixel.commons.data.Pool;

/**
 * Mouse event for Rune GUI system.
 * Uses object pooling to avoid GC pressure.
 */
@Getter
@Setter
public class RuneMouseEvent implements Disposable {
    
    /**
     * Mouse event types.
     */
    public enum Type {
        MOVE,       // Mouse moved
        PRESS,      // Mouse button pressed
        RELEASE,    // Mouse button released
        DRAG,       // Mouse moved while button pressed
        SCROLL,     // Mouse wheel scrolled
        ENTER,      // Mouse entered widget bounds
        EXIT        // Mouse exited widget bounds
    }
    
    private static final Pool<RuneMouseEvent> POOL = new Pool<>(RuneMouseEvent::new, 50);
    
    private Type type;
    private float x;
    private float y;
    private float deltaX;
    private float deltaY;
    private int button;         // 0 = left, 1 = right, 2 = middle
    private float scrollDelta;  // For SCROLL events
    private boolean consumed;   // Event consumed (stop propagation)
    
    /**
     * Private constructor - use obtain() to get instance.
     */
    private RuneMouseEvent() {
        reset();
    }
    
    /**
     * Obtain a mouse event from the pool.
     */
    public static RuneMouseEvent obtain() {
        RuneMouseEvent event = POOL.obtain();
        event.reset();
        return event;
    }
    
    /**
     * Obtain a mouse event with values.
     */
    public static RuneMouseEvent obtain(Type type, float x, float y) {
        RuneMouseEvent event = obtain();
        event.type = type;
        event.x = x;
        event.y = y;
        return event;
    }
    
    /**
     * Obtain a drag event.
     */
    public static RuneMouseEvent obtainDrag(float x, float y, float deltaX, float deltaY) {
        RuneMouseEvent event = obtain();
        event.type = Type.DRAG;
        event.x = x;
        event.y = y;
        event.deltaX = deltaX;
        event.deltaY = deltaY;
        return event;
    }
    
    /**
     * Obtain a scroll event.
     */
    public static RuneMouseEvent obtainScroll(float x, float y, float scrollDelta) {
        RuneMouseEvent event = obtain();
        event.type = Type.SCROLL;
        event.x = x;
        event.y = y;
        event.scrollDelta = scrollDelta;
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
        this.type = Type.MOVE;
        this.x = 0;
        this.y = 0;
        this.deltaX = 0;
        this.deltaY = 0;
        this.button = 0;
        this.scrollDelta = 0;
        this.consumed = false;
    }
    
    @Override
    public void dispose() {
        reset();
        POOL.free(this);
    }
    
    @Override
    public String toString() {
        return String.format("RuneMouseEvent{type=%s, x=%.1f, y=%.1f, button=%d, consumed=%b}", 
            type, x, y, button, consumed);
    }
}
