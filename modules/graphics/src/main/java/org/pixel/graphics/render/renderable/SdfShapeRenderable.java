package org.pixel.graphics.render.renderable;

import org.pixel.graphics.render.Renderable;
import org.pixel.graphics.render.SdfShapeRenderer;
import org.pixel.math.Matrix4;
import org.pixel.math.Rectangle;

/**
 * A renderable for SDF (Signed Distance Field) based shapes.
 * Stores shape properties and uses SdfShapeRenderer for efficient GPU-based rendering.
 */
public class SdfShapeRenderable extends Renderable<SdfShapeRenderer> {
    
    public enum ShapeType {
        FILLED_RECT,
        STROKED_RECT,
        FILLED_ROUNDED_RECT,
        STROKED_ROUNDED_RECT,
        FILLED_CIRCLE,
        STROKED_CIRCLE
    }
    
    private ShapeType shapeType;
    private float width;
    private float height;
    private float radius;        // Corner radius for rounded rect, or circle radius
    private float strokeWidth;
    private Matrix4 transform;   // Baked transform (local + parent transforms)
    
    /**
     * Public constructor.
     */
    public SdfShapeRenderable() {
        super(SdfShapeRenderer.class);
        this.shapeType = ShapeType.FILLED_RECT;
        this.width = 0;
        this.height = 0;
        this.radius = 0;
        this.strokeWidth = 0;
        this.transform = new Matrix4();
    }
    
    @Override
    public void render(SdfShapeRenderer renderer, Matrix4 viewMatrix) {
        // Use the transform as-is (it already contains currentTransform * viewMatrix from Canvas)
        // Do NOT multiply by viewMatrix again - that would apply it twice!
        
        // Render based on shape type
        switch (shapeType) {
            case FILLED_RECT:
                renderer.fillRoundedRect(position.getX(), position.getY(), width, height, 
                    0, color, transform);
                break;
                
            case STROKED_RECT:
                renderer.strokeRoundedRect(position.getX(), position.getY(), width, height, 
                    0, strokeWidth, color, transform);
                break;
                
            case FILLED_ROUNDED_RECT:
                renderer.fillRoundedRect(position.getX(), position.getY(), width, height, 
                    radius, color, transform);
                break;
                
            case STROKED_ROUNDED_RECT:
                renderer.strokeRoundedRect(position.getX(), position.getY(), width, height, 
                    radius, strokeWidth, color, transform);
                break;
                
            case FILLED_CIRCLE:
                renderer.fillCircle(position.getX(), position.getY(), radius, color, transform);
                break;
                
            case STROKED_CIRCLE:
                renderer.strokeCircle(position.getX(), position.getY(), radius, strokeWidth, 
                    color, transform);
                break;
        }
    }
    
    @Override
    public Rectangle getBounds() {
        // Calculate bounds based on shape type
        float padding = strokeWidth > 0 ? strokeWidth / 2 : 0;

        return switch (shapeType) {
            case FILLED_CIRCLE, STROKED_CIRCLE -> {
                float circlePadding = radius + padding;
                yield new Rectangle(
                        position.getX() - circlePadding,
                        position.getY() - circlePadding,
                        circlePadding * 2,
                        circlePadding * 2
                );
            }
            default -> // Rectangles
                    new Rectangle(
                            position.getX() - padding,
                            position.getY() - padding,
                            width + padding * 2,
                            height + padding * 2
                    );
        };
    }
    
    //<editor-fold desc="Shape-Specific Getters and Setters">
    public ShapeType getShapeType() { return shapeType; }
    public SdfShapeRenderable setShapeType(ShapeType shapeType) { this.shapeType = shapeType; return this; }
    
    public float getWidth() { return width; }
    public SdfShapeRenderable setWidth(float width) { this.width = width; return this; }
    
    public float getHeight() { return height; }
    public SdfShapeRenderable setHeight(float height) { this.height = height; return this; }
    
    public SdfShapeRenderable setSize(float width, float height) { 
        this.width = width; 
        this.height = height; 
        return this; 
    }
    
    public float getRadius() { return radius; }
    public SdfShapeRenderable setRadius(float radius) { this.radius = radius; return this; }
    
    public float getStrokeWidth() { return strokeWidth; }
    public SdfShapeRenderable setStrokeWidth(float strokeWidth) { this.strokeWidth = strokeWidth; return this; }
    
    public Matrix4 getTransform() { return transform; }
    public SdfShapeRenderable setTransform(Matrix4 transform) { 
        this.transform = new Matrix4(transform); 
        return this; 
    }
    //</editor-fold>
    
    //<editor-fold desc="Fluent Setters Override for Method Chaining">
    @Override
    public SdfShapeRenderable setPosition(float x, float y) { super.setPosition(x, y); return this; }
    
    @Override
    public SdfShapeRenderable setTint(org.pixel.commons.Color color) { super.setTint(color); return this; }
    
    @Override
    public SdfShapeRenderable setDepth(int depth) { super.setDepth(depth); return this; }
    //</editor-fold>
}
