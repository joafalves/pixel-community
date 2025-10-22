package org.pixel.ext.rune.widget;

import org.pixel.ext.rune.layout.Alignment;
import org.pixel.ext.rune.layout.VerticalLayout;
import org.pixel.math.Rectangle;

/**
 * Panel widget with title bar and body container.
 * 
 * <p>Features:
 * <ul>
 *   <li>Title bar with text (draggable by default)</li>
 *   <li>Body container for child widgets</li>
 *   <li>Styled via CSS-like classes: "panel", "panel-title", "panel-body"</li>
 *   <li>Draggable via title bar area</li>
 * </ul>
 * 
 * <p>Example:
 * <pre>{@code
 * RunePanel panel = new RunePanel("File Explorer");
 * panel.setBounds(100, 100, 300, 400);
 * panel.add(new RuneLabel("Content goes here"));
 * }</pre>
 */
public class RunePanel extends RuneContainer {
    
    private String title;
    protected RuneLabel titleLabel;
    protected RuneContainer bodyContainer;
    
    /**
     * Create a panel with the given title.
     * 
     * @param title Panel title text
     */
    public RunePanel(String title) {
        super(title.toLowerCase().replace(" ", "_").replace(".", "_"));
        this.title = title;
        
        // Add style classes
        addClass("panel");
        
        // Use vertical layout to stack title + body
        setLayout(new VerticalLayout(0).align(Alignment.topLeft()));
        
        // Create title label
        titleLabel = new RuneLabel();
        titleLabel.setText(title);
        titleLabel.addClass("panel-title");
        
        // Create body container
        bodyContainer = new RuneContainer(id + "_body");
        bodyContainer.addClass("panel-body");
        
        // Add to this container (layout will position them)
        super.add(titleLabel);
        super.add(bodyContainer);
        
        // Enable dragging via title bar
        setDraggable(true);
    }
    
    /**
     * Override to check if drag handle (title bar) was hit.
     * Uses the title label's bounds as the drag area.
     */
    @Override
    protected boolean isDragHandleHit(float localX, float localY) {
        Rectangle titleBounds = titleLabel.getBox().borderBounds;
        // Convert title bounds to local coordinates (relative to this panel)
        float titleLocalX = titleBounds.getX() - getX();
        float titleLocalY = titleBounds.getY() - getY();
        
        return localX >= titleLocalX 
            && localX < titleLocalX + titleBounds.getWidth()
            && localY >= titleLocalY
            && localY < titleLocalY + titleBounds.getHeight();
    }
    
    /**
     * Add a widget to the panel body (not the title area).
     * Delegates to the internal body container.
     */
    @Override
    public RuneContainer add(RuneWidget child) {
        bodyContainer.add(child);
        return this;
    }
    
    /**
     * Get the panel title.
     */
    public String getTitle() {
        return title;
    }
    
    /**
     * Set the panel title.
     * 
     * @param title New title text
     * @return This panel for chaining
     */
    public RunePanel setTitle(String title) {
        this.title = title;
        titleLabel.setText(title);
        return this;
    }
    
    /**
     * Get the title label widget (for customization).
     */
    public RuneLabel getTitleLabel() {
        return titleLabel;
    }
    
    /**
     * Get the body container (for layout customization).
     */
    public RuneContainer getBodyContainer() {
        return bodyContainer;
    }
}
