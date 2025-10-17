package org.pixel.demo.concept.simulation.event;

import org.pixel.math.SizeInt;

public class ViewPortResizeEvent extends Event<SizeInt> {
    public ViewPortResizeEvent(SizeInt data) {
        super(data);
    }
}
