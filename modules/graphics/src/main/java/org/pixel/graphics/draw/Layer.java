package org.pixel.graphics.draw;

import org.pixel.graphics.render.BlendMode;

/**
 * A named bucket of draw commands, consumed by the render passes that list it. Declare layers once (they are
 * immutable, typically {@code static final}) and use them to build pipelines and to get their canvases:
 * <pre>
 * static final Layer WORLD = Layer.sorted("world");
 * static final Layer FX = Layer.ordered("fx").blend(BlendMode.ADDITIVE);
 * static final Layer HUD = Layer.ordered("hud");
 * </pre>
 *
 * @see LayerMode
 */
public final class Layer {

    private final String name;
    private final LayerMode mode;
    private final BlendMode blendMode;

    private Layer(String name, LayerMode mode, BlendMode blendMode) {
        if (name == null || mode == null || blendMode == null) {
            throw new IllegalArgumentException("Layer name, mode and blend mode cannot be null.");
        }
        this.name = name;
        this.mode = mode;
        this.blendMode = blendMode;
    }

    /**
     * A layer drawn in submission order (painter's algorithm): HUD, GUI, overlays.
     */
    public static Layer ordered(String name) {
        return new Layer(name, LayerMode.ORDERED, BlendMode.NORMAL_BLEND);
    }

    /**
     * A layer sorted by depth (equal depth keeps submission order): game worlds, y-sorting.
     */
    public static Layer sorted(String name) {
        return new Layer(name, LayerMode.SORTED, BlendMode.NORMAL_BLEND);
    }

    /**
     * @return A copy of this layer drawn with another blend mode (e.g. additive particles).
     */
    public Layer blend(BlendMode blendMode) {
        return new Layer(name, mode, blendMode);
    }

    public String getName() {
        return name;
    }

    public LayerMode getMode() {
        return mode;
    }

    public BlendMode getBlendMode() {
        return blendMode;
    }

    @Override
    public String toString() {
        return "Layer[" + name + ", " + mode + (blendMode != BlendMode.NORMAL_BLEND ? ", " + blendMode : "") + "]";
    }
}
