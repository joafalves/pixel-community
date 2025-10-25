# Weaver Layout Engine - Architecture Specification

## Overview

This document specifies the architecture for Weaver's layout engine, responsible for calculating widget boundaries, positions, and z-ordering based on CSS-like styling. The layout engine works in conjunction with the existing StyleEngine to transform computed styles into concrete pixel-based boundaries.

## Goals

- Implement CSS box model with configurable `box-sizing` (content-box, border-box)
- Support CSS positioning modes: `static`, `relative`, `absolute`, `fixed`
- Enable programmatic positioning (`setX()/setY()`) as syntactic sugar for inline styles
- Provide responsive layout capabilities (flexbox as future consideration)
- Implement efficient dirty-flag system for layout recalculation
- Support z-index based rendering order
- Maintain single source of truth: all positioning flows through StyleEngine

## Widget Registration & Discovery

### How LayoutEngine Discovers Widgets

LayoutEngine maintains a registry of all widgets in the system. Widgets are registered in two ways:

1. **Root-level widgets**: Added directly to Weaver via `addWidget()`
2. **Child widgets**: Added to ContainerWidget via `addChild()`

```java
// In Weaver
public void addWidget(Widget widget) {
    widgets.add(widget);
    widget.setParent(null);  // Mark as root-level
    context.getLayoutEngine().registerRootWidget(widget);
}

// In ContainerWidget
public void addChild(Widget child) {
    children.add(child);
    child.setParent(this);  // Establish parent-child relationship

    // If this container is already registered, register the child too
    if (getContext() != null) {
        getContext().getLayoutEngine().registerWidgetRecursive(child);
    }

    markLayoutDirty();
}
```

**Key Insights**:
- Weaver's flat list API is preserved, but internally each widget is treated as a "root" that can have children via ContainerWidget
- This gives us both simplicity (flat API) and power (hierarchical layout)
- LayoutEngine only tracks root widgets explicitly; all descendants are discovered via tree traversal
- Widget registration status is determined by whether `layoutInfo` exists (created during first layout pass)

## Core Components

### 1. LayoutEngine

**Responsibility**: Orchestrates layout calculation across the widget tree, maintains widget registry, and manages dirty flags.

**Key Properties**:
```java
public class LayoutEngine {
    private final WeaverContext context;
    private int viewportWidth;
    private int viewportHeight;

    // Widget registry - only track roots, traverse to find all descendants
    private final List<Widget> rootWidgets = new ArrayList<>();

    // Dirty flag tracking
    private final Set<Widget> dirtyWidgets = new HashSet<>();
    private boolean needsFullLayout = true;

    // Cached z-index sorted render order
    private List<Widget> renderOrder;
    private boolean renderOrderDirty = true;
}
```

**Key Methods**:
```java
// Widget registration (called by Weaver and ContainerWidget)
public void registerRootWidget(Widget widget);
public void unregisterRootWidget(Widget widget);
void registerWidgetRecursive(Widget widget);  // Package-private
void unregisterWidgetRecursive(Widget widget);  // Package-private

// Full layout pass - called when viewport changes
public void calculateLayout(int viewportWidth, int viewportHeight);

// Incremental layout - only recalculate dirty widgets and descendants
public void recalculateDirty();

// Mark widget for layout recalculation
public void markDirty(Widget widget);

// Mark entire tree for layout
public void markNeedsLayout();

// Get render order sorted by z-index and tree order
public List<Widget> getRenderOrder();

// Internal layout calculation
private void layoutWidget(Widget widget, ContainingBlock containingBlock, ContainingBlock viewport);
private ContainingBlock findContainingBlockForWidget(Widget widget, ContainingBlock viewport);
private void applyDefaultRootStyles(Widget widget);
```

**Implementation**:
```java
public void registerRootWidget(Widget widget) {
    rootWidgets.add(widget);
    registerWidgetRecursive(widget);
    markNeedsLayout();
}

void registerWidgetRecursive(Widget widget) {
    // No explicit tracking needed - widgets discovered via tree traversal
    // Just ensure children are registered recursively
    if (widget instanceof ContainerWidget) {
        ContainerWidget container = (ContainerWidget) widget;
        for (Widget child : container.getChildren()) {
            registerWidgetRecursive(child);
        }
    }
}

public void unregisterRootWidget(Widget widget) {
    rootWidgets.remove(widget);
    unregisterWidgetRecursive(widget);
    markNeedsLayout();
}

void unregisterWidgetRecursive(Widget widget) {
    // Clean up any dirty references
    dirtyWidgets.remove(widget);

    if (widget instanceof ContainerWidget) {
        ContainerWidget container = (ContainerWidget) widget;
        for (Widget child : container.getChildren()) {
            unregisterWidgetRecursive(child);
        }
    }
}

public void calculateLayout(int viewportWidth, int viewportHeight) {
    this.viewportWidth = viewportWidth;
    this.viewportHeight = viewportHeight;

    ContainingBlock viewport = ContainingBlock.fromViewport(viewportWidth, viewportHeight);

    // Layout each root widget tree
    for (Widget root : rootWidgets) {
        // Apply default positioning for root-level widgets
        applyDefaultRootStyles(root);

        root.initLayoutInfo();
        layoutWidget(root, viewport, viewport);
    }

    rebuildRenderOrder();
    dirtyWidgets.clear();
    needsFullLayout = false;
}

public void recalculateDirty() {
    if (dirtyWidgets.isEmpty() && !needsFullLayout) {
        return;
    }

    if (needsFullLayout) {
        calculateLayout(viewportWidth, viewportHeight);
        return;
    }

    ContainingBlock viewport = ContainingBlock.fromViewport(viewportWidth, viewportHeight);

    // Find dirty roots (dirty widgets with no dirty ancestors)
    Set<Widget> dirtyRoots = findDirtyRoots();

    for (Widget dirtyRoot : dirtyRoots) {
        ContainingBlock containingBlock = findContainingBlockForWidget(dirtyRoot, viewport);
        layoutWidget(dirtyRoot, containingBlock, viewport);
    }

    rebuildRenderOrder();
    dirtyWidgets.clear();
}

public void markDirty(Widget widget) {
    // Check if widget has been laid out (registered widgets have layoutInfo)
    if (widget.getLayoutInfo() == null) {
        // Widget not registered yet - might be added to container before container added to Weaver
        return;
    }

    widget.getLayoutInfo().setDirty(true);
    dirtyWidgets.add(widget);

    // Mark all descendants dirty
    if (widget instanceof ContainerWidget) {
        markDescendantsDirty((ContainerWidget) widget);
    }

    renderOrderDirty = true;
}

private void applyDefaultRootStyles(Widget widget) {
    // Root-level widgets (no parent) default to absolute positioning
    // This makes them "floating" UI elements positioned at 0,0 by default
    if (widget.getParent() == null) {
        ComputedStyle style = context.getStyleEngine().getComputedStyles(widget);
        String position = style.get("position");

        if (position == null || "static".equals(position)) {
            // Apply default absolute positioning for roots
            context.getStyleEngine().setDefaultStyle(widget, "position", "absolute");
            context.getStyleEngine().setDefaultStyle(widget, "top", "0");
            context.getStyleEngine().setDefaultStyle(widget, "left", "0");
        }
    }
}
```

**Algorithm Flow**:
```
calculateLayout(root):
  1. Reset dirty flags
  2. Start from root with viewport as containing block
  3. Recursively layout each widget:
     a. Resolve style dimensions to pixels using containing block
     b. Calculate box model (content, padding, border, margin)
     c. Calculate position based on position type
     d. Store in LayoutInfo
     e. Determine containing block for children
     f. Recurse to children
  4. Rebuild z-index render order
  5. Clear dirty flags

recalculateDirty():
  1. For each dirty widget in tree order:
     a. Find containing block from parent's LayoutInfo
     b. Recalculate this widget's layout
     c. Mark all descendants dirty (position change affects children)
     d. Recurse to dirty descendants
  2. Rebuild z-index render order if any absolute/fixed widgets dirty
  3. Clear dirty flags
```

### 2. LayoutInfo

**Responsibility**: Stores computed layout information for a single widget.

**Structure**:
```java
@Getter
@Setter
public class LayoutInfo {
    // Position type (affects how bounds are calculated)
    private PositionType positionType; // STATIC, RELATIVE, ABSOLUTE, FIXED

    // Box model rectangles (all in absolute viewport coordinates)
    private Rectangle contentBox;   // Inner content area
    private Rectangle paddingBox;   // Content + padding
    private Rectangle borderBox;    // Padding + border
    private Rectangle marginBox;    // Border + margin

    // For positioned elements (relative/absolute/fixed)
    private float offsetX = 0;  // Computed from top/right/bottom/left
    private float offsetY = 0;

    // The containing block dimensions for this widget's children
    private ContainingBlock childContainingBlock;

    // Z-index information
    private int zIndex = 0;
    private int stackingOrder; // Global rendering order considering tree position

    // Box-sizing mode
    private BoxSizing boxSizing; // CONTENT_BOX, BORDER_BOX

    // Overflow handling
    private OverflowMode overflowX = OverflowMode.VISIBLE;
    private OverflowMode overflowY = OverflowMode.VISIBLE;

    // Scroll state (for scrollable containers)
    private float scrollX = 0;  // Horizontal scroll offset
    private float scrollY = 0;  // Vertical scroll offset
    private Rectangle clipBounds; // Clipping rectangle for overflow:hidden/scroll

    // Dirty flag for incremental layout
    private boolean dirty = true;
}

public enum OverflowMode {
    VISIBLE,   // Content not clipped, renders outside bounds
    HIDDEN,    // Content clipped to bounds, no scrolling
    SCROLL,    // Content clipped, scrollbars always visible
    AUTO       // Content clipped, scrollbars only if needed
}
```

**Key Methods**:
```java
// Get the appropriate bounds based on box-sizing
public Rectangle getBounds() {
    return boxSizing == BoxSizing.BORDER_BOX ? borderBox : contentBox;
}

// Get absolute position (top-left of content box)
public float getX() {
    return contentBox.getX();
}

public float getY() {
    return contentBox.getY();
}

// Get dimensions based on box-sizing
public float getWidth() {
    return getBounds().getWidth();
}

public float getHeight() {
    return getBounds().getHeight();
}

// Overflow and clipping
public boolean shouldClip() {
    return overflowX != OverflowMode.VISIBLE || overflowY != OverflowMode.VISIBLE;
}

public Rectangle getClipBounds() {
    return clipBounds;
}

public void setScrollOffset(float x, float y) {
    this.scrollX = Math.max(0, x);
    this.scrollY = Math.max(0, y);
}

public float getScrollX() {
    return scrollX;
}

public float getScrollY() {
    return scrollY;
}

// Get maximum scroll offsets based on content size
public float getMaxScrollX(float contentWidth) {
    return Math.max(0, contentWidth - contentBox.getWidth());
}

public float getMaxScrollY(float contentHeight) {
    return Math.max(0, contentHeight - contentBox.getHeight());
}
```

### 3. ContainingBlock

**Responsibility**: Represents the reference box for percentage and position calculations.

**Structure**:
```java
@Getter
@AllArgsConstructor
public class ContainingBlock {
    private final float x;
    private final float y;
    private final float width;
    private final float height;

    // Type of containing block (affects percentage resolution)
    private final ContainingBlockType type; // VIEWPORT, CONTENT, PADDING

    public static ContainingBlock fromViewport(int width, int height) {
        return new ContainingBlock(0, 0, width, height, ContainingBlockType.VIEWPORT);
    }

    public static ContainingBlock fromContentBox(Rectangle contentBox) {
        return new ContainingBlock(
            contentBox.getX(), contentBox.getY(),
            contentBox.getWidth(), contentBox.getHeight(),
            ContainingBlockType.CONTENT
        );
    }

    public static ContainingBlock fromPaddingBox(Rectangle paddingBox) {
        return new ContainingBlock(
            paddingBox.getX(), paddingBox.getY(),
            paddingBox.getWidth(), paddingBox.getHeight(),
            ContainingBlockType.PADDING
        );
    }
}
```

### 4. BoxModelCalculator

**Responsibility**: Calculates box model rectangles from resolved styles.

**Key Methods**:
```java
public class BoxModelCalculator {

    /**
     * Calculate all box model rectangles for a widget
     *
     * @param computedStyle The pre-computed styles for this widget
     * @param styleEngine The style engine (for resolveLength utility)
     * @param widget The widget (for context in resolveLength)
     * @param containingBlock The containing block for percentage resolution
     * @param computedX The computed X position (after position calculation)
     * @param computedY The computed Y position (after position calculation)
     * @return BoxModel containing all rectangles
     */
    public static BoxModel calculate(
        ComputedStyle computedStyle,
        StyleEngine styleEngine,
        Widget widget,
        ContainingBlock containingBlock,
        float computedX,
        float computedY
    ) {
        // Get box-sizing mode
        BoxSizing boxSizing = resolveBoxSizing(computedStyle);

        // Resolve dimensions
        float specifiedWidth = resolveDimension(
            computedStyle.get("width"), containingBlock.getWidth(), styleEngine, widget
        );
        float specifiedHeight = resolveDimension(
            computedStyle.get("height"), containingBlock.getHeight(), styleEngine, widget
        );

        // Resolve padding (percentages relative to containing block WIDTH in CSS)
        float paddingTop = resolveLength(computedStyle.get("padding-top"), containingBlock.getWidth(), styleEngine, widget);
        float paddingRight = resolveLength(computedStyle.get("padding-right"), containingBlock.getWidth(), styleEngine, widget);
        float paddingBottom = resolveLength(computedStyle.get("padding-bottom"), containingBlock.getWidth(), styleEngine, widget);
        float paddingLeft = resolveLength(computedStyle.get("padding-left"), containingBlock.getWidth(), styleEngine, widget);

        // Resolve border
        float borderTop = resolveLength(computedStyle.get("border-top-width"), containingBlock.getWidth(), styleEngine, widget);
        float borderRight = resolveLength(computedStyle.get("border-right-width"), containingBlock.getWidth(), styleEngine, widget);
        float borderBottom = resolveLength(computedStyle.get("border-bottom-width"), containingBlock.getWidth(), styleEngine, widget);
        float borderLeft = resolveLength(computedStyle.get("border-left-width"), containingBlock.getWidth(), styleEngine, widget);

        // Resolve margin
        float marginTop = resolveLength(computedStyle.get("margin-top"), containingBlock.getWidth(), styleEngine, widget);
        float marginRight = resolveLength(computedStyle.get("margin-right"), containingBlock.getWidth(), styleEngine, widget);
        float marginBottom = resolveLength(computedStyle.get("margin-bottom"), containingBlock.getWidth(), styleEngine, widget);
        float marginLeft = resolveLength(computedStyle.get("margin-left"), containingBlock.getWidth(), styleEngine, widget);

        // Calculate boxes based on box-sizing
        Rectangle contentBox, paddingBox, borderBox, marginBox;

        if (boxSizing == BoxSizing.BORDER_BOX) {
            // Specified dimensions include border and padding
            borderBox = new Rectangle(computedX, computedY, specifiedWidth, specifiedHeight);

            paddingBox = new Rectangle(
                computedX + borderLeft,
                computedY + borderTop,
                specifiedWidth - borderLeft - borderRight,
                specifiedHeight - borderTop - borderBottom
            );

            contentBox = new Rectangle(
                paddingBox.getX() + paddingLeft,
                paddingBox.getY() + paddingTop,
                paddingBox.getWidth() - paddingLeft - paddingRight,
                paddingBox.getHeight() - paddingTop - paddingBottom
            );

        } else { // CONTENT_BOX
            // Specified dimensions are content dimensions
            contentBox = new Rectangle(computedX, computedY, specifiedWidth, specifiedHeight);

            paddingBox = new Rectangle(
                computedX - paddingLeft,
                computedY - paddingTop,
                specifiedWidth + paddingLeft + paddingRight,
                specifiedHeight + paddingTop + paddingBottom
            );

            borderBox = new Rectangle(
                paddingBox.getX() - borderLeft,
                paddingBox.getY() - borderTop,
                paddingBox.getWidth() + borderLeft + borderRight,
                paddingBox.getHeight() + borderTop + borderBottom
            );
        }

        marginBox = new Rectangle(
            borderBox.getX() - marginLeft,
            borderBox.getY() - marginTop,
            borderBox.getWidth() + marginLeft + marginRight,
            borderBox.getHeight() + marginTop + marginBottom
        );

        return new BoxModel(contentBox, paddingBox, borderBox, marginBox, boxSizing);
    }

    private static float resolveDimension(
        String value, float containerSize, StyleEngine engine, Widget widget
    ) {
        // Handle width/height resolution including 'auto', percentages, fixed values
        // For now, assume explicit values or percentages
        // 'auto' will be phase 2 (content-based sizing)
        return engine.resolveLength(value, containerSize, widget);
    }

    private static float resolveLength(String value, float containerSize, StyleEngine engine, Widget widget) {
        return engine.resolveLength(value, containerSize, widget);
    }

    private static BoxSizing resolveBoxSizing(ComputedStyle style) {
        String value = style.get("box-sizing");
        return "border-box".equalsIgnoreCase(value) ? BoxSizing.BORDER_BOX : BoxSizing.CONTENT_BOX;
    }
}

@Getter
@AllArgsConstructor
class BoxModel {
    private final Rectangle contentBox;
    private final Rectangle paddingBox;
    private final Rectangle borderBox;
    private final Rectangle marginBox;
    private final BoxSizing boxSizing;
}
```

### 5. PositionCalculator

**Responsibility**: Calculates widget position based on position type and offsets.

**Key Methods**:
```java
public class PositionCalculator {

    /**
     * Calculate the absolute position for a widget
     *
     * @param computedStyle The pre-computed styles for this widget
     * @param styleEngine The style engine (for resolveLength utility)
     * @param widget The widget (for context)
     * @param containingBlock The containing block
     * @param parent The parent widget (null for root)
     * @return Position { x, y }
     */
    public static Position calculate(
        ComputedStyle computedStyle,
        StyleEngine styleEngine,
        Widget widget,
        ContainingBlock containingBlock,
        Widget parent
    ) {
        PositionType positionType = resolvePositionType(computedStyle);

        switch (positionType) {
            case STATIC:
                return calculateStatic(widget, parent);

            case RELATIVE:
                Position staticPos = calculateStatic(widget, parent);
                return applyRelativeOffset(computedStyle, styleEngine, widget, staticPos, containingBlock);

            case ABSOLUTE:
                return calculateAbsolute(computedStyle, styleEngine, widget, containingBlock);

            case FIXED:
                return calculateFixed(computedStyle, styleEngine, widget, containingBlock);

            default:
                throw new IllegalStateException("Unknown position type: " + positionType);
        }
    }

    private static Position calculateStatic(Widget widget, Widget parent) {
        // Static positioning: part of normal flow
        // For now, simple stacking (block-level behavior)
        // Phase 2: proper flow layout with inline/block

        if (parent == null) {
            return new Position(0, 0);
        }

        LayoutInfo parentLayout = parent.getLayoutInfo();
        Rectangle parentContent = parentLayout.getContentBox();

        float x = parentContent.getX();
        float y = parentContent.getY();

        // Stack below previous sibling
        Widget previousSibling = getPreviousSibling(widget);
        if (previousSibling != null) {
            LayoutInfo siblingLayout = previousSibling.getLayoutInfo();
            Rectangle siblingMargin = siblingLayout.getMarginBox();
            y = siblingMargin.getY() + siblingMargin.getHeight();
        }

        return new Position(x, y);
    }

    private static Position applyRelativeOffset(
        ComputedStyle computedStyle,
        StyleEngine styleEngine,
        Widget widget,
        Position staticPos,
        ContainingBlock containingBlock
    ) {
        // Relative: offset from static position
        float offsetX = 0;
        float offsetY = 0;

        // top/bottom/left/right only work on positioned elements
        String top = computedStyle.get("top");
        String left = computedStyle.get("left");
        String right = computedStyle.get("right");
        String bottom = computedStyle.get("bottom");

        // Phase 1: left takes precedence over right, ignore right if left is present
        if (!isAuto(left)) {
            offsetX = styleEngine.resolveLength(left, containingBlock.getWidth(), widget);
        }
        // Phase 1: ignore right (defer to Phase 2)

        // Phase 1: top takes precedence over bottom, ignore bottom if top is present
        if (!isAuto(top)) {
            offsetY = styleEngine.resolveLength(top, containingBlock.getHeight(), widget);
        }
        // Phase 1: ignore bottom (defer to Phase 2)

        return new Position(staticPos.x + offsetX, staticPos.y + offsetY);
    }

    private static Position calculateAbsolute(
        ComputedStyle computedStyle,
        StyleEngine styleEngine,
        Widget widget,
        ContainingBlock containingBlock
    ) {
        // Absolute: positioned relative to containing block (nearest positioned ancestor)
        String top = computedStyle.get("top");
        String left = computedStyle.get("left");
        String right = computedStyle.get("right");
        String bottom = computedStyle.get("bottom");

        float x = containingBlock.getX();
        float y = containingBlock.getY();

        // Phase 1: left takes precedence over right, ignore right if left is present
        if (!isAuto(left)) {
            x += styleEngine.resolveLength(left, containingBlock.getWidth(), widget);
        }
        // Phase 1: ignore right (defer to Phase 2)
        // Phase 2 will handle: else if (!isAuto(right)) { x = containingBlock.getX() + containingBlock.getWidth() - widgetWidth - rightOffset; }

        // Phase 1: top takes precedence over bottom, ignore bottom if top is present
        if (!isAuto(top)) {
            y += styleEngine.resolveLength(top, containingBlock.getHeight(), widget);
        }
        // Phase 1: ignore bottom (defer to Phase 2)
        // Phase 2 will handle: else if (!isAuto(bottom)) { y = containingBlock.getY() + containingBlock.getHeight() - widgetHeight - bottomOffset; }

        return new Position(x, y);
    }

    private static Position calculateFixed(
        ComputedStyle computedStyle,
        StyleEngine styleEngine,
        Widget widget,
        ContainingBlock containingBlock
    ) {
        // Fixed: positioned relative to viewport (containingBlock is viewport)
        // Same logic as absolute, but containing block is always viewport
        return calculateAbsolute(computedStyle, styleEngine, widget, containingBlock);
    }

    private static PositionType resolvePositionType(ComputedStyle style) {
        String value = style.get("position");
        if (value == null) return PositionType.STATIC;

        switch (value.toLowerCase()) {
            case "relative": return PositionType.RELATIVE;
            case "absolute": return PositionType.ABSOLUTE;
            case "fixed": return PositionType.FIXED;
            default: return PositionType.STATIC;
        }
    }

    private static boolean isAuto(String value) {
        return value == null || "auto".equalsIgnoreCase(value);
    }

    private static Widget getPreviousSibling(Widget widget) {
        // Requires parent.getChildren() and finding index
        // Implementation depends on ContainerWidget structure
        return null; // Placeholder
    }
}

@Getter
@AllArgsConstructor
class Position {
    public final float x;
    public final float y;
}
```

### 6. ZIndexCalculator

**Responsibility**: Determines rendering order based on z-index and tree position.

**Key Concepts**:
- Widgets with higher z-index render on top
- Within same z-index, tree order determines rendering (later siblings on top)
- Only positioned elements (non-static) participate in z-index stacking
- Stacking contexts: positioned element with z-index creates new stacking context for descendants

**Key Methods**:
```java
public class ZIndexCalculator {

    /**
     * Calculate render order for all widgets in tree
     * Widgets are sorted by stacking context, z-index, and tree order
     */
    public static List<Widget> calculateRenderOrder(Widget root) {
        List<WidgetWithOrder> widgets = new ArrayList<>();
        collectWidgets(root, 0, new StackingContext(0), widgets);

        // Sort by stacking context level, then z-index, then tree order
        widgets.sort(Comparator
            .comparingInt((WidgetWithOrder w) -> w.stackingContextLevel)
            .thenComparingInt(w -> w.zIndex)
            .thenComparingInt(w -> w.treeOrder));

        return widgets.stream()
            .map(w -> w.widget)
            .collect(Collectors.toList());
    }

    private static void collectWidgets(
        Widget widget,
        int treeOrder,
        StackingContext stackingContext,
        List<WidgetWithOrder> result
    ) {
        LayoutInfo layout = widget.getLayoutInfo();

        // Z-index only applies to positioned elements (non-static)
        // Static elements always use z-index of 0 for sorting, regardless of styled value
        int effectiveZIndex = layout.getPositionType() == PositionType.STATIC
            ? 0
            : layout.getZIndex();

        // Check if this widget creates a new stacking context
        // Only positioned elements with non-zero z-index create stacking contexts
        boolean createsContext = layout.getPositionType() != PositionType.STATIC
            && layout.getZIndex() != 0;

        StackingContext currentContext = createsContext
            ? stackingContext.createChild()
            : stackingContext;

        result.add(new WidgetWithOrder(
            widget,
            effectiveZIndex,
            treeOrder,
            currentContext.getLevel()
        ));

        // Recurse to children
        if (widget instanceof ContainerWidget) {
            ContainerWidget container = (ContainerWidget) widget;
            List<Widget> children = container.getChildren();
            for (int i = 0; i < children.size(); i++) {
                collectWidgets(children.get(i), treeOrder + i + 1, currentContext, result);
            }
        }
    }

    @Getter
    @AllArgsConstructor
    private static class WidgetWithOrder {
        Widget widget;
        int zIndex;
        int treeOrder;
        int stackingContextLevel;
    }

    private static class StackingContext {
        private final int level;

        StackingContext(int level) {
            this.level = level;
        }

        int getLevel() {
            return level;
        }

        StackingContext createChild() {
            return new StackingContext(level + 1);
        }
    }
}
```

### 7. Widget Integration

**Changes to Widget class**:

```java
public abstract class Widget implements Drawable, Disposable {
    // ... existing fields ...

    @Getter
    private LayoutInfo layoutInfo;

    // ... existing methods ...

    /**
     * Get the bounds of this widget based on box-sizing
     * This is what external code uses for hit-testing, rendering, etc.
     */
    public Rectangle getBounds() {
        if (layoutInfo == null) {
            throw new IllegalStateException("Layout not calculated - call layout engine first");
        }
        return layoutInfo.getBounds();
    }

    /**
     * Get computed X position (top-left of content box)
     */
    public float getX() {
        return layoutInfo != null ? layoutInfo.getX() : 0;
    }

    /**
     * Get computed Y position (top-left of content box)
     */
    public float getY() {
        return layoutInfo != null ? layoutInfo.getY() : 0;
    }

    /**
     * Get computed width based on box-sizing
     */
    public float getWidth() {
        return layoutInfo != null ? layoutInfo.getWidth() : 0;
    }

    /**
     * Get computed height based on box-sizing
     */
    public float getHeight() {
        return layoutInfo != null ? layoutInfo.getHeight() : 0;
    }

    /**
     * Set X position programmatically
     * This sets inline style: left={x}px, position=absolute
     */
    public Widget setX(float x) {
        ensureAbsolutePositioning();
        setInlineStyle("left", x + "px");
        markLayoutDirty();
        return this;
    }

    /**
     * Set Y position programmatically
     * This sets inline style: top={y}px, position=absolute
     */
    public Widget setY(float y) {
        ensureAbsolutePositioning();
        setInlineStyle("top", y + "px");
        markLayoutDirty();
        return this;
    }

    /**
     * Set position programmatically
     */
    public Widget setPosition(float x, float y) {
        ensureAbsolutePositioning();
        setInlineStyle("left", x + "px");
        setInlineStyle("top", y + "px");
        markLayoutDirty();
        return this;
    }

    /**
     * Set width programmatically
     */
    public Widget setWidth(float width) {
        setInlineStyle("width", width + "px");
        markLayoutDirty();
        return this;
    }

    /**
     * Set height programmatically
     */
    public Widget setHeight(float height) {
        setInlineStyle("height", height + "px");
        markLayoutDirty();
        return this;
    }

    /**
     * Set size programmatically
     */
    public Widget setSize(float width, float height) {
        setInlineStyle("width", width + "px");
        setInlineStyle("height", height + "px");
        markLayoutDirty();
        return this;
    }

    /**
     * Set z-index programmatically
     */
    public Widget setZIndex(int zIndex) {
        setInlineStyle("z-index", String.valueOf(zIndex));
        markLayoutDirty();
        return this;
    }

    /**
     * Helper to ensure widget is absolutely positioned
     */
    private void ensureAbsolutePositioning() {
        StyleEngine styleEngine = context.getStyleEngine();
        String currentPosition = styleEngine.getComputedStyle(this, "position");
        if (currentPosition == null || "static".equals(currentPosition)) {
            setInlineStyle("position", "absolute");
        }
    }

    /**
     * Set inline style (highest priority, like HTML style attribute)
     */
    private void setInlineStyle(String property, String value) {
        // Delegate to StyleEngine to set inline style
        context.getStyleEngine().setInlineStyle(this, property, value);
    }

    /**
     * Mark this widget and descendants for layout recalculation
     */
    protected void markLayoutDirty() {
        context.getLayoutEngine().markDirty(this);
    }

    /**
     * Internal: Initialize layout info (called by layout engine)
     */
    public void initLayoutInfo() {
        if (layoutInfo == null) {
            layoutInfo = new LayoutInfo();
        }
    }
}
```

### 8. ContainerWidget Integration

**Layout parent-child relationships**:

```java
public class ContainerWidget extends Widget {
    @Getter
    private List<Widget> children = new ArrayList<>();

    public void addChild(Widget child) {
        children.add(child);
        child.setParent(this);
        child.setContext(getContext()); // Propagate context to child

        // Register with LayoutEngine if this container is already registered
        if (getContext() != null) {
            getContext().getLayoutEngine().registerWidgetRecursive(child);
        }

        markLayoutDirty();
    }

    public void removeChild(Widget child) {
        children.remove(child);
        child.setParent(null);

        // Unregister from LayoutEngine
        if (getContext() != null) {
            getContext().getLayoutEngine().unregisterWidgetRecursive(child);
        }

        markLayoutDirty();
    }

    /**
     * Calculate total content size (for overflow/scrolling)
     * This is the size needed to contain all children
     */
    public Size calculateContentSize() {
        if (children.isEmpty()) {
            return new Size(0, 0);
        }

        float maxRight = 0;
        float maxBottom = 0;

        for (Widget child : children) {
            LayoutInfo childLayout = child.getLayoutInfo();
            if (childLayout == null) continue;

            // For static/relative children, include in content size
            if (childLayout.getPositionType() == PositionType.STATIC ||
                childLayout.getPositionType() == PositionType.RELATIVE) {

                Rectangle marginBox = childLayout.getMarginBox();
                maxRight = Math.max(maxRight, marginBox.getX() + marginBox.getWidth());
                maxBottom = Math.max(maxBottom, marginBox.getY() + marginBox.getHeight());
            }
            // Absolute/fixed children don't contribute to content size
        }

        return new Size(maxRight, maxBottom);
    }

    // Note: getContainingBlockForChildren() removed - this logic is now in LayoutEngine
    // The correct containing block depends on the CHILD's position type, not the parent's
}
```

### 9. StyleEngine Extensions

**New methods needed in StyleEngine**:

```java
public class StyleEngine {
    // ... existing code ...

    // Store inline styles (highest priority)
    private Map<Widget, Map<String, String>> inlineStyles = new WeakHashMap<>();

    // Store default styles (lowest priority, used for root widgets)
    private Map<Widget, Map<String, String>> defaultStyles = new WeakHashMap<>();

    /**
     * Set default style (lowest priority, overridden by everything)
     * Used for applying default positioning to root widgets
     */
    void setDefaultStyle(Widget widget, String property, String value) {
        defaultStyles
            .computeIfAbsent(widget, k -> new HashMap<>())
            .put(property, value);
    }

    /**
     * Set inline style on a widget (like HTML style attribute)
     * Inline styles have highest priority in cascade
     */
    public void setInlineStyle(Widget widget, String property, String value) {
        inlineStyles
            .computeIfAbsent(widget, k -> new HashMap<>())
            .put(property, value);
        // Mark style cache dirty for this widget
    }

    /**
     * Get all computed styles for a widget at once
     * This should be called once per widget per layout pass
     * Returns a ComputedStyle object containing all resolved property values
     */
    public ComputedStyle getComputedStyles(Widget widget) {
        // Collect all applicable styles (cascade, inheritance, inline)
        // Return as ComputedStyle object for efficient access
        // This avoids multiple getComputedStyle() calls per widget
        Map<String, String> styles = new HashMap<>();

        // Resolve all properties considering cascade and inline styles
        // ... (existing cascade logic)

        return new ComputedStyle(styles);
    }

    /**
     * Get computed style value, considering inline styles first
     */
    @Override
    public String getComputedStyle(Widget widget, String property) {
        // Priority order: inline > cascade > defaults

        // 1. Check inline styles (highest priority)
        Map<String, String> inline = inlineStyles.get(widget);
        if (inline != null && inline.containsKey(property)) {
            return inline.get(property);
        }

        // 2. Fall back to cascade resolution (stylesheets, classes, etc)
        String cascadeValue = super.getComputedStyle(widget, property);
        if (cascadeValue != null) {
            return cascadeValue;
        }

        // 3. Check default styles (lowest priority)
        Map<String, String> defaults = defaultStyles.get(widget);
        if (defaults != null && defaults.containsKey(property)) {
            return defaults.get(property);
        }

        return null;
    }

    /**
     * Resolve length value to pixels
     * Handles: px, %, em, rem, vw, vh
     */
    public float resolveLength(String value, float containerSize, Widget widget) {
        if (value == null || "auto".equals(value)) {
            return 0; // or handle auto differently
        }

        // Delegate to existing unit resolution logic
        // This already exists based on StyleEngineTest
        return resolveLengthValue(value, containerSize, widget);
    }
}

/**
 * Immutable snapshot of computed styles for a widget
 * Avoids repeated getComputedStyle() calls during layout
 */
@Getter
public class ComputedStyle {
    private final Map<String, String> properties;

    public ComputedStyle(Map<String, String> properties) {
        this.properties = Collections.unmodifiableMap(properties);
    }

    public String get(String property) {
        return properties.get(property);
    }

    public String getOrDefault(String property, String defaultValue) {
        return properties.getOrDefault(property, defaultValue);
    }
}
```

## Style Properties Supported

### Position and Layout
- `position`: static | relative | absolute | fixed
- `top`, `right`, `bottom`, `left`: length | percentage | auto
- `width`, `height`: length | percentage | auto
- `min-width`, `max-width`, `min-height`, `max-height`: length | percentage
- `box-sizing`: content-box | border-box
- `z-index`: integer
- `overflow`: visible | hidden | scroll | auto (shorthand for overflow-x and overflow-y)
- `overflow-x`: visible | hidden | scroll | auto
- `overflow-y`: visible | hidden | scroll | auto

### Box Model
- `margin[-top|-right|-bottom|-left]`: length | percentage | auto
- `padding[-top|-right|-bottom|-left]`: length | percentage
- `border[-top|-right|-bottom|-left]-width`: length

### Units Supported
- `px`: Pixels (absolute)
- `%`: Percentage (relative to containing block)
- `em`: Relative to element's font-size
- `rem`: Relative to root font-size
- `vw`/`vh`: Viewport percentage

## Layout Algorithm Pseudocode

```
LayoutEngine.calculateLayout(root, viewportWidth, viewportHeight):
    viewport = ContainingBlock.fromViewport(viewportWidth, viewportHeight)
    root.initLayoutInfo()
    layoutWidget(root, viewport, viewport)  // Pass viewport for absolute/fixed lookups
    renderOrder = ZIndexCalculator.calculateRenderOrder(root)

layoutWidget(widget, containingBlock, viewport):
    // 1. Initialize
    widget.initLayoutInfo()
    layoutInfo = widget.getLayoutInfo()

    // 2. Get computed styles ONCE (performance optimization)
    computedStyle = styleEngine.getComputedStyles(widget)

    // 3. Resolve position type from computed styles
    positionType = PositionCalculator.resolvePositionType(computedStyle)
    layoutInfo.setPositionType(positionType)

    // 4. Calculate position (passing computedStyle to avoid repeated lookups)
    position = PositionCalculator.calculate(
        computedStyle, styleEngine, widget, containingBlock, widget.parent
    )

    // 5. Calculate box model (passing computedStyle to avoid repeated lookups)
    boxModel = BoxModelCalculator.calculate(
        computedStyle, styleEngine, widget, containingBlock, position.x, position.y
    )
    layoutInfo.setContentBox(boxModel.contentBox)
    layoutInfo.setPaddingBox(boxModel.paddingBox)
    layoutInfo.setBorderBox(boxModel.borderBox)
    layoutInfo.setMarginBox(boxModel.marginBox)
    layoutInfo.setBoxSizing(boxModel.boxSizing)

    // 6. Resolve z-index
    zIndex = parseZIndex(computedStyle.get("z-index"))
    layoutInfo.setZIndex(zIndex)

    // 7. Resolve overflow settings
    overflowX = parseOverflow(computedStyle.get("overflow-x") ?: computedStyle.get("overflow"))
    overflowY = parseOverflow(computedStyle.get("overflow-y") ?: computedStyle.get("overflow"))
    layoutInfo.setOverflowX(overflowX)
    layoutInfo.setOverflowY(overflowY)

    // Set clip bounds if overflow is not visible
    if overflowX != VISIBLE or overflowY != VISIBLE:
        layoutInfo.setClipBounds(layoutInfo.getPaddingBox())

    // 8. Layout children if container
    if widget is ContainerWidget:
        for child in widget.children:
            // Get child's position type FIRST (before recursing)
            childComputedStyle = styleEngine.getComputedStyles(child)
            childPositionType = PositionCalculator.resolvePositionType(childComputedStyle)

            // Determine containing block based on CHILD's position type
            if childPositionType == ABSOLUTE or childPositionType == FIXED:
                // Absolute: use nearest positioned ancestor's padding box
                // Fixed: use viewport
                if childPositionType == FIXED:
                    childContainingBlock = viewport
                else:
                    childContainingBlock = findPositionedAncestorContainingBlock(widget, viewport)
                layoutWidget(child, childContainingBlock, viewport)
            else:
                // Static/relative: use parent's content box
                // Account for scroll offset in containing block
                contentBox = layoutInfo.getContentBox()
                scrollOffsetX = layoutInfo.getScrollX()
                scrollOffsetY = layoutInfo.getScrollY()

                childContainingBlock = ContainingBlock.fromContentBox(
                    contentBox.x - scrollOffsetX,
                    contentBox.y - scrollOffsetY,
                    contentBox.width,
                    contentBox.height
                )
                layoutWidget(child, childContainingBlock, viewport)

    // 9. Calculate content size for scrollable containers
    if widget is ContainerWidget and (overflowX == SCROLL or overflowX == AUTO or
                                       overflowY == SCROLL or overflowY == AUTO):
        contentSize = widget.calculateContentSize()

        // Clamp scroll offsets to valid range
        maxScrollX = layoutInfo.getMaxScrollX(contentSize.width)
        maxScrollY = layoutInfo.getMaxScrollY(contentSize.height)
        layoutInfo.setScrollOffset(
            Math.min(layoutInfo.getScrollX(), maxScrollX),
            Math.min(layoutInfo.getScrollY(), maxScrollY)
        )

    // 10. Mark as clean
    layoutInfo.setDirty(false)

findPositionedAncestorContainingBlock(widget, viewport):
    // Find nearest positioned ancestor (non-static)
    current = widget
    while current != null:
        if current.layoutInfo.positionType != STATIC:
            // Use padding box of positioned ancestor
            return ContainingBlock.fromPaddingBox(current.layoutInfo.getPaddingBox())
        current = current.parent

    // No positioned ancestor - use viewport
    return viewport
```

## Dirty Flag Optimization

**When to mark dirty**:
- Widget style changes (via StyleEngine or setInlineStyle)
- Widget added/removed from parent
- Viewport resize
- Programmatic position/size changes (setX, setY, setWidth, setHeight)

**Dirty propagation**:
- When widget marked dirty, all descendants marked dirty (position changes affect children)
- When container layout changes, children may need relayout
- Dirty flag prevents unnecessary recalculation of clean subtrees

**Implementation**:
```java
LayoutEngine.markDirty(widget):
    widget.layoutInfo.setDirty(true)
    dirtyWidgets.add(widget)

    // Mark all descendants dirty
    if widget is ContainerWidget:
        for child in widget.children:
            markDirty(child)

    // Render order may change if positioned widget
    if widget.layoutInfo.positionType != STATIC:
        renderOrderDirty = true

LayoutEngine.recalculateDirty():
    if dirtyWidgets.isEmpty():
        return

    // Sort dirty widgets by tree depth (parents before children)
    sortedDirty = sortByTreeDepth(dirtyWidgets)

    for widget in sortedDirty:
        if !widget.layoutInfo.dirty:
            continue // Already cleaned by ancestor

        containingBlock = findContainingBlock(widget)
        layoutWidget(widget, containingBlock)

    if renderOrderDirty:
        renderOrder = ZIndexCalculator.calculateRenderOrder(root)
        renderOrderDirty = false

    dirtyWidgets.clear()
```

## Integration with Weaver

**WeaverContext changes**:
```java
@Builder
public class WeaverContext {
    @Getter
    private Canvas canvas;

    @Getter
    private StyleEngine styleEngine;

    @Getter
    private LayoutEngine layoutEngine;  // NEW

    @Getter
    @Setter
    private int viewportWidth;

    @Getter
    @Setter
    private int viewportHeight;

    // Builder will need to initialize layoutEngine
    public static class WeaverContextBuilder {
        public WeaverContext build() {
            WeaverContext context = new WeaverContext(/* fields */);
            // Initialize LayoutEngine with reference to context
            context.layoutEngine = new LayoutEngine(context);
            return context;
        }
    }
}
```

**Weaver changes**:
```java
public class Weaver implements Updatable, Drawable, Disposable {
    private final WeaverContext context;
    private final List<Widget> widgets = new ArrayList<>();  // Changed from List.of()

    public Weaver(int viewportWidth, int viewportHeight) {
        this.context = WeaverContext.builder()
                .canvas(Canvas.create(viewportWidth, viewportHeight))
                .viewportWidth(viewportWidth)
                .viewportHeight(viewportHeight)
                .styleEngine(new StyleEngine())
                .build();

        // Initial layout calculation
        context.getLayoutEngine().calculateLayout(viewportWidth, viewportHeight);
    }

    /**
     * Add a widget to Weaver (root-level)
     */
    public void addWidget(Widget widget) {
        widgets.add(widget);
        widget.setParent(null);  // Mark as root
        widget.setContext(context);  // Provide context access

        // Register with LayoutEngine
        context.getLayoutEngine().registerRootWidget(widget);
    }

    /**
     * Remove a widget from Weaver
     */
    public void removeWidget(Widget widget) {
        widgets.remove(widget);
        context.getLayoutEngine().unregisterRootWidget(widget);
    }

    @Override
    public void update(DeltaTime delta) {
        // 1. Recalculate layout for dirty widgets FIRST
        context.getLayoutEngine().recalculateDirty();

        // 2. Update widgets (they now have correct bounds)
        for (Widget widget : widgets) {
            if (widget.isEnabled()) {
                widget.update(delta, context);
            }
        }
    }

    @Override
    public void draw(DeltaTime delta) {
        // Get z-index sorted render order (includes all widgets and children)
        List<Widget> renderOrder = context.getLayoutEngine().getRenderOrder();

        Canvas canvas = context.getCanvas();
        canvas.begin();

        // Draw in z-index order
        for (Widget widget : renderOrder) {
            if (widget.isEnabled()) {
                // Apply clipping if widget has overflow:hidden/scroll
                LayoutInfo layout = widget.getLayoutInfo();
                if (layout != null && layout.shouldClip()) {
                    canvas.save();
                    canvas.clipRect(layout.getClipBounds());
                }

                widget.draw(delta, context);

                if (layout != null && layout.shouldClip()) {
                    canvas.restore();
                }
            }
        }

        canvas.end();
    }

    /**
     * Called when viewport/window resizes
     */
    public void setViewport(int width, int height) {
        context.setViewportWidth(width);
        context.setViewportHeight(height);
        context.getCanvas().setViewport(width, height);

        // Recalculate entire layout with new viewport
        context.getLayoutEngine().calculateLayout(width, height);
    }
}
```

**Widget changes**:
```java
public abstract class Widget implements Styleable {
    private final Set<String> classes = new HashSet<>();

    @Getter
    @Setter
    private boolean enabled = true;

    @Getter
    @Setter
    private String id;

    @Getter
    @Setter
    private Widget parent;  // NEW - null for root widgets

    @Getter
    @Setter
    private WeaverContext context;  // NEW - set by Weaver/ContainerWidget

    @Getter
    private LayoutInfo layoutInfo;  // NEW - set by LayoutEngine

    /**
     * Initialize layout info (called by LayoutEngine)
     */
    public void initLayoutInfo() {
        if (layoutInfo == null) {
            layoutInfo = new LayoutInfo();
        }
    }

    /**
     * Mark this widget for layout recalculation
     */
    protected void markLayoutDirty() {
        if (context != null) {
            context.getLayoutEngine().markDirty(this);
        }
    }

    /**
     * Programmatic setters that modify inline styles
     */
    public Widget setX(float x) {
        ensureAbsolutePositioning();
        context.getStyleEngine().setInlineStyle(this, "left", x + "px");
        markLayoutDirty();
        return this;
    }

    public Widget setY(float y) {
        ensureAbsolutePositioning();
        context.getStyleEngine().setInlineStyle(this, "top", y + "px");
        markLayoutDirty();
        return this;
    }

    // ... other setters (setWidth, setHeight, setZIndex, etc)

    private void ensureAbsolutePositioning() {
        if (context == null) return;

        ComputedStyle style = context.getStyleEngine().getComputedStyles(this);
        String position = style.get("position");

        if (position == null || "static".equals(position)) {
            context.getStyleEngine().setInlineStyle(this, "position", "absolute");
        }
    }
}
```

## Testing Strategy

### Unit Tests

**BoxModelCalculator tests**:
- Content-box sizing with various padding/border/margin
- Border-box sizing with various padding/border/margin
- Percentage-based dimensions
- Mixed units (px, %, em)

**PositionCalculator tests**:
- Static positioning (simple stacking)
- Relative positioning with offsets
- Absolute positioning relative to parent
- Fixed positioning relative to viewport
- Nested absolute positioning

**ZIndexCalculator tests**:
- Same z-index, different tree order
- Different z-index values
- Stacking contexts
- Nested positioned elements

### Integration Tests

**Layout scenarios**:
- Parent-child layout with various position types
- Viewport resize handling
- Dirty flag propagation
- Programmatic position changes
- Style changes triggering relayout

## Overflow and Scrolling

### Overflow Modes

```java
public enum OverflowMode {
    VISIBLE,   // Content not clipped, can render outside bounds (default)
    HIDDEN,    // Content clipped to padding box, no scrolling
    SCROLL,    // Content clipped, scrollbars always present
    AUTO       // Content clipped, scrollbars only when needed
}
```

### Implementation Details

**Clipping**:
- When `overflow-x` or `overflow-y` is not `visible`, widget content is clipped to the padding box
- Clipping applied during rendering via `Canvas.clipRect()`
- Children positioned outside clip bounds are not rendered (but still laid out)

**Scrolling**:
- ContainerWidget can have scroll offsets (scrollX, scrollY)
- Scroll offsets adjust the containing block origin for children
- Maximum scroll determined by content size vs container size
- Scrollbars rendered separately (not part of Phase 1 layout)

**Content Size Calculation**:
```java
// ContainerWidget calculates total size of its children
public Size calculateContentSize() {
    // Only static/relative children contribute to content size
    // Absolute/fixed children are "out of flow" and don't affect scrolling

    float maxRight = 0;
    float maxBottom = 0;

    for (Widget child : children) {
        if (child.isStatic() || child.isRelative()) {
            Rectangle bounds = child.getLayoutInfo().getMarginBox();
            maxRight = Math.max(maxRight, bounds.getRight());
            maxBottom = Math.max(maxBottom, bounds.getBottom());
        }
    }

    return new Size(maxRight, maxBottom);
}
```

**Scroll Offset Application**:
```java
// When laying out children of scrollable container:
ContainingBlock childContainingBlock = new ContainingBlock(
    contentBox.x - scrollX,  // Shift left by scroll amount
    contentBox.y - scrollY,  // Shift up by scroll amount
    contentBox.width,
    contentBox.height
);
```

### API for Scrolling

```java
// Get current scroll position
float scrollX = widget.getLayoutInfo().getScrollX();
float scrollY = widget.getLayoutInfo().getScrollY();

// Set scroll position (clamped to valid range)
widget.getLayoutInfo().setScrollOffset(100, 50);

// Get maximum scroll range
Size contentSize = containerWidget.calculateContentSize();
float maxScrollX = widget.getLayoutInfo().getMaxScrollX(contentSize.getWidth());
float maxScrollY = widget.getLayoutInfo().getMaxScrollY(contentSize.getHeight());

// Scroll by delta (for mouse wheel, touch gestures)
widget.scrollBy(deltaX, deltaY);
```

## Phase 1 Scope (MVP)

**Include**:
- Basic box model (margin, padding, border)
- Box-sizing: content-box, border-box
- Position types: static, relative, absolute, fixed
- Z-index sorting (only for positioned elements)
- Programmatic positioning (setX/setY as inline styles)
- Dirty flag optimization
- Basic stacking layout (block-like, no inline)
- **Widget registration** via Weaver.addWidget() and ContainerWidget.addChild()
- **Root widget default positioning** (absolute at 0,0)
- **Overflow**: visible, hidden, scroll, auto
- **Clipping**: Content clipped to padding box when overflow != visible
- **Scroll offsets**: Programmatic scrolling with scroll position tracking
- **Position offsets**: `top` and `left` only (Phase 1)
  - When both `left` and `right` are specified, `left` takes precedence and `right` is ignored
  - When both `top` and `bottom` are specified, `top` takes precedence and `bottom` is ignored

**Defer to Phase 2**:
- Auto dimensions (content-based sizing)
- Min/max width/height constraints
- Flexbox layout
- Grid layout
- Inline layout mode
- Text baseline alignment
- Margin collapse
- Aspect ratio constraints
- **Scrollbar rendering** (visual scrollbars, Phase 1 only handles scroll state)
- **Smooth scrolling animations**
- **Mouse wheel / touch gesture scroll handlers** (user-implemented in Phase 1)
- **Position offsets**: `right` and `bottom` support (requires widget dimensions first)
  - Right-edge positioning: `right: 10px` positions element 10px from right edge
  - Bottom-edge positioning: `bottom: 10px` positions element 10px from bottom edge
  - Stretch behavior: when both edges specified (e.g., `left: 0; right: 0`), widget stretches to fill

## Edge Cases & Considerations

1. **Circular dependencies**: Prevent infinite loops when width depends on height or vice versa
2. **Containing block for absolute**: Must find nearest positioned ancestor, fallback to viewport
3. **Percentage padding/margin**: In CSS, vertical padding/margin percentages use containing block WIDTH (not height)
4. **Z-index without position**: z-index only works on positioned elements (non-static)
5. **Transform-like offsets**: Should we support CSS transforms separately from layout position?
6. **Viewport units during resize**: Must recalculate all widgets using vw/vh units
7. **Font-size for em units**: Need to resolve font-size before resolving em-based dimensions
8. **Inheritance**: Some properties inherit (font-size), some don't (width) - StyleEngine handles this
9. **Overflow and absolute children**: Absolute/fixed positioned children are clipped by overflow:hidden but don't contribute to scroll content size
10. **Scroll and layout interaction**: Changing scroll offset doesn't trigger full layout, only repositions children
11. **Root widget context**: Root widgets need WeaverContext set before they can access LayoutEngine for dirty marking
12. **Registration timing**: Children added to ContainerWidget before the container is added to Weaver won't be registered until container registration happens

## Performance Considerations

1. **Object pooling**: Rectangle objects created frequently during layout - consider pooling
2. **Style cache**: StyleEngine should cache computed styles per widget
3. **Single style lookup per widget**: Call `getComputedStyles()` once per widget, pass `ComputedStyle` to calculators (avoids repeated hash lookups)
4. **Dirty subtree pruning**: Don't traverse clean subtrees during dirty recalculation
5. **Render order caching**: Only rebuild when positioned widgets change or tree structure changes
6. **Incremental layout**: Only recalculate dirty widgets, not entire tree
7. **Layout thrashing**: Batch style changes before triggering layout

## API Usage Examples

### Basic programmatic positioning (like HTML canvas)
```java
Widget button = new ButtonWidget()
    .setX(100)
    .setY(50)
    .setWidth(200)
    .setHeight(40);
// Internally sets: position=absolute, left=100px, top=50px, width=200px, height=40px
```

### CSS-based positioning
```java
Widget panel = new PanelWidget()
    .addClass("sidebar");

// In stylesheet:
// .sidebar {
//   position: fixed;
//   top: 0;
//   right: 0;
//   width: 300px;
//   height: 100vh;
// }
```

### Relative positioning
```java
Widget tooltip = new TooltipWidget()
    .setInlineStyle("position", "relative")
    .setInlineStyle("top", "10px")
    .setInlineStyle("left", "-5px");
// Offset from where it would normally be
```

### Z-index layering
```java
Widget background = new Widget().setZIndex(0);
Widget content = new Widget().setZIndex(1);
Widget overlay = new Widget().setZIndex(2);
// Renders: background -> content -> overlay
```

### Responsive sizing
```java
Widget container = new ContainerWidget()
    .setInlineStyle("width", "80vw")
    .setInlineStyle("height", "60vh")
    .setInlineStyle("position", "fixed")
    .setInlineStyle("left", "10vw")
    .setInlineStyle("top", "20vh");
// Centered, responsive to viewport size
```

### Box-sizing behavior
```java
// Content-box (default)
Widget contentBox = new Widget()
    .setWidth(200)  // Content is 200px
    .setInlineStyle("padding", "10px")  // Total width = 220px (200 + 10*2)
    .setInlineStyle("box-sizing", "content-box");

// Border-box
Widget borderBox = new Widget()
    .setWidth(200)  // Total width is 200px
    .setInlineStyle("padding", "10px")  // Content is 180px (200 - 10*2)
    .setInlineStyle("box-sizing", "border-box");
```

## Implementation Order

### Phase 1A: Core Infrastructure
1. **ComputedStyle class** - Immutable style snapshot
2. **LayoutInfo class** - Data structure for layout results (including overflow fields)
3. **ContainingBlock class** - Reference box for calculations
4. **OverflowMode enum** - Overflow behavior types
5. **Widget changes** - Add parent, context, layoutInfo fields; add markLayoutDirty(), programmatic setters
6. **StyleEngine extensions** - `getComputedStyles()`, inline styles, default styles support

### Phase 1B: Layout Calculation
7. **BoxModelCalculator** - Core box model math (accepting ComputedStyle)
8. **PositionCalculator** - Position calculation logic (accepting ComputedStyle)
9. **ZIndexCalculator** - Rendering order (respecting static elements)

### Phase 1C: Layout Engine
10. **LayoutEngine** - Widget registration, orchestration, tree walking
11. **ContainerWidget** - Parent-child relationships, addChild/removeChild, calculateContentSize()
12. **WeaverContext integration** - Add LayoutEngine to context

### Phase 1D: Weaver Integration
13. **Weaver changes** - addWidget/removeWidget, update loop with recalculateDirty, draw with clipping
14. **Overflow support** - Clip bounds calculation, scroll offset application

### Phase 1E: Testing
15. **Unit tests** - Test each calculator independently
16. **Integration tests** - End-to-end layout scenarios
17. **Overflow tests** - Clipping and scrolling behavior
18. **Registration tests** - Widget registration and discovery

## Success Criteria

- [ ] Widget bounds calculated correctly for all position types
- [ ] Box-sizing (content-box, border-box) works correctly
- [ ] Programmatic setX/setY sets inline styles and triggers layout
- [ ] Z-index ordering produces correct render order
- [ ] Viewport resize recalculates layout
- [ ] Dirty flag prevents unnecessary recalculation
- [ ] Percentage units resolve correctly against containing block
- [ ] Viewport units (vw, vh) resolve correctly
- [ ] Parent-child layout works with nested containers
- [ ] Absolute positioning finds correct containing block
- [ ] **Widget registration works for root and child widgets**
- [ ] **Root widgets default to absolute positioning at 0,0**
- [ ] **Widgets added to Weaver are properly registered with LayoutEngine**
- [ ] **Children added to ContainerWidget are properly registered**
- [ ] **LayoutEngine discovers all widgets through tree traversal**
- [ ] **Overflow clipping works (hidden, scroll, auto)**
- [ ] **Scroll offsets correctly reposition children**
- [ ] **Content size calculation includes only static/relative children**
- [ ] **Clipping applied during rendering via Canvas.clipRect()**
- [ ] Unit tests cover all calculators
- [ ] Integration tests validate end-to-end scenarios
- [ ] Overflow and scrolling integration tests
