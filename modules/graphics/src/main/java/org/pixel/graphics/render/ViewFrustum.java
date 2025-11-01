package org.pixel.graphics.render;

import org.pixel.math.Rectangle;

public class ViewFrustum {
    
    private final Rectangle bounds;
    private final Rectangle expandedBounds;
    private float margin;
    
    public ViewFrustum() {
        this.bounds = new Rectangle();
        this.expandedBounds = new Rectangle();
        this.margin = 0.0f;
    }
    
    public ViewFrustum(float x, float y, float width, float height) {
        this.bounds = new Rectangle(x, y, width, height);
        this.expandedBounds = new Rectangle();
        this.margin = 0.0f;
        updateExpandedBounds();
    }
    
    public void set(float x, float y, float width, float height) {
        this.bounds.set(x, y, width, height);
        updateExpandedBounds();
    }
    
    public void setMargin(float margin) {
        this.margin = margin;
        updateExpandedBounds();
    }
    
    public float getMargin() {
        return margin;
    }
    
    private void updateExpandedBounds() {
        float marginX = bounds.getWidth() * margin;
        float marginY = bounds.getHeight() * margin;
        expandedBounds.set(
            bounds.getX() - marginX,
            bounds.getY() - marginY,
            bounds.getWidth() + marginX * 2,
            bounds.getHeight() + marginY * 2
        );
    }
    
    public boolean intersects(Rectangle objectBounds) {
        if (objectBounds == null) {
            return true;
        }
        return expandedBounds.overlaps(objectBounds);
    }
    
    public Rectangle getBounds() {
        return bounds;
    }
}
