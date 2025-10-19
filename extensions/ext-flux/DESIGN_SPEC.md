# Pixel IMGUI Extension - Design Specification

## Overview

The `ext-imgui` extension provides an **Immediate Mode GUI (IMGUI)** system for building game editors, debug tools, and in-game UI using the Pixel Framework's Canvas API as the rendering backend. This extension enables rapid development of complete UI tools with minimal boilerplate, leveraging Canvas's zero-GC, fluent drawing API.

## Goals

1. **Tooling-First**: Optimized for game editors, inspectors, and debug overlays
2. **Zero-GC**: Leverage Canvas object pooling for real-time rendering without garbage collection
3. **Simple API**: Immediate mode paradigm - UI state mirrors application state
4. **Extensible**: Easy to add custom widgets and layouts
5. **Engine Foundation**: Serve as the UI layer for future game engine tools (scene editor, asset browser, etc.)

## Design Philosophy

### Immediate Mode Paradigm

Unlike retained-mode GUIs (Swing, JavaFX), IMGUI redeclares UI every frame:

```java
@Override
public void draw(DeltaTime delta) {
    gui.begin();

    if (gui.button("Start Game", 100, 100, 200, 40)) {
        startGame();
    }

    gui.label("Score: " + score, 100, 150);

    selectedWeapon = gui.dropdown(weapons, selectedWeapon, 100, 200, 200, 30);

    gui.end();
}
```

**Benefits**:
- No state synchronization - UI always reflects current application state
- Simple logic - no event listeners or observers needed
- Easy debugging - UI code co-located with game logic
- Perfect for tools where UI mirrors editor state

**Trade-offs**:
- No persistent widget references (acceptable for tools)
- Harder for complex animations (can be addressed with retained hybrid widgets later)

## Architecture

### Core Components

```
GuiContext
├── Input State (mouse position, clicks, keys, focus)
├── Style/Theme (colors, fonts, spacing)
├── Layout State (cursor position, spacing, nesting)
├── Widget ID System (for state tracking across frames)
└── Canvas Backend (rendering)
```

### Component Hierarchy

```
Widget Functions (no classes initially - pure IMGUI)
├── Layout Containers
│   ├── vbox() - vertical stacking
│   ├── hbox() - horizontal stacking
│   ├── grid() - rows/columns
│   ├── panel() - bordered container
│   └── window() - movable/closable window
├── Interactive Controls
│   ├── button() - clickable button
│   ├── textField() - text input
│   ├── slider() - numeric range input
│   ├── checkbox() - boolean toggle
│   ├── dropdown() - selection list
│   └── colorPicker() - color selection
└── Display Elements
    ├── label() - static text
    ├── image() - texture display
    ├── separator() - visual divider
    └── progressBar() - progress indicator
```

## Key Design Considerations

### 1. Canvas Integration

All rendering goes through Pixel's Canvas API:

```java
class GuiContext {
    private final Canvas canvas;

    public boolean button(String text, float x, float y, float w, float h) {
        boolean hovered = isMouseOver(x, y, w, h);
        boolean clicked = hovered && isMouseClicked();

        // Render using Canvas
        canvas.save();
        canvas.rect(x, y, w, h)
            .withFill(hovered ? theme.buttonHoverBg : theme.buttonBg)
            .withStroke(2, theme.buttonBorder)
            .withRoundedCorners(theme.buttonRadius);

        canvas.text(text, theme.font, x + w/2, y + h/2)
            .withFill(theme.buttonText)
            .withAlign(TextAlign.middleCenter());
        canvas.restore();

        return clicked;
    }
}
```

**Benefits**:
- Zero-GC from Canvas object pooling
- Consistent rendering with game graphics
- Full Canvas feature set (gradients, shadows, transforms)
- Hardware-accelerated via OpenGL

### 2. Input Handling

GuiContext tracks input state per frame:

```java
class GuiContext {
    private Vector2 mousePos;
    private boolean mouseDown;
    private boolean mouseClicked; // true only on down-edge
    private int mouseButton;
    private Set<Integer> keysPressed;
    private String textInput; // accumulated text this frame

    public void beginFrame() {
        mouseClicked = false;
        textInput = "";
        // Update from InputProcessor
    }
}
```

**Input Processing**:
- Mouse click detection: AABB tests against widget bounds
- Keyboard focus: Track active widget ID for text input
- Event consumption: Widgets "consume" input to prevent pass-through

### 3. Widget ID System

Track persistent state across frames (e.g., text field contents, window positions):

```java
class GuiContext {
    private Map<String, WidgetState> stateMap = new HashMap<>();

    public String textField(String id, String value, float x, float y, float w, float h) {
        WidgetState state = stateMap.computeIfAbsent(id, k -> new WidgetState());

        if (state.isFocused) {
            // Apply text input from this frame
            value += textInput;
        }

        // Handle focus changes
        if (isMouseClicked() && isMouseOver(x, y, w, h)) {
            state.isFocused = true;
        }

        // Render text field...

        return value;
    }
}
```

**ID Generation**: Use string IDs (manual) or auto-generate from stack trace position.

### 4. Layout System

Start with **cursor-based layouts** (simple and flexible):

```java
class GuiContext {
    private float cursorX, cursorY;
    private float spacing = 10;

    public void vbox(float x, float y, Runnable contents) {
        cursorX = x;
        cursorY = y;
        contents.run(); // Widgets inside advance cursorY
    }

    public boolean button(String text, float w, float h) {
        boolean result = button(text, cursorX, cursorY, w, h);
        cursorY += h + spacing; // Advance cursor
        return result;
    }
}
```

**Future Layouts**:
- Grid layout with row/column spanning
- Anchor layout (relative to parent edges)
- Flex layout (when needed - possibly via Yoga integration)

### 5. Styling & Theming

Centralized theme system:

```java
class GuiTheme {
    // Colors
    public Color windowBg = new Color(0.15f, 0.15f, 0.15f, 0.95f);
    public Color buttonBg = new Color(0.26f, 0.59f, 0.98f, 1.0f);
    public Color buttonHoverBg = new Color(0.36f, 0.69f, 1.0f, 1.0f);
    public Color buttonActiveBg = new Color(0.16f, 0.49f, 0.88f, 1.0f);
    public Color text = Color.WHITE;
    public Color border = new Color(0.4f, 0.4f, 0.4f, 1.0f);

    // Typography
    public SdfFont font;
    public int fontSize = 16;

    // Spacing
    public float padding = 8;
    public float itemSpacing = 4;
    public float windowPadding = 10;

    // Rounding
    public float buttonRadius = 4;
    public float windowRadius = 8;
}
```

### 6. Focus Management

```java
class GuiContext {
    private String focusedWidgetId = null;
    private String hoveredWidgetId = null;

    public void setFocus(String widgetId) {
        focusedWidgetId = widgetId;
    }

    public boolean hasFocus(String widgetId) {
        return widgetId.equals(focusedWidgetId);
    }
}
```

**Focus Behavior**:
- Click on widget → gain focus
- Press Tab → cycle focus (future)
- Click outside → lose focus

## Implementation Roadmap

### Phase 1: Core IMGUI System (MVP)
- `GuiContext` class
- Input state tracking
- Basic widgets: `button()`, `label()`, `textField()`
- Simple cursor-based layout
- Default theme

### Phase 2: Layout Containers
- `vbox()`, `hbox()` layout helpers
- `panel()` for grouped widgets
- `window()` with title bar and close button
- Clipping for scrollable regions

### Phase 3: Advanced Controls
- `slider()`, `checkbox()`, `dropdown()`
- `colorPicker()` with preview
- `treeNode()` for hierarchies (scene graph)
- `table()` / `grid()` for data

### Phase 4: Tooling Features
- Dockable windows
- Menu bars and context menus
- Drag & drop support
- Tooltips
- Property grids (key-value pairs)

### Phase 5: Polish & Optimization
- Render culling (off-screen widgets)
- Input event batching
- Custom widget API for extensions
- Theme editor

## Integration with Pixel Framework

### Service Registration
```java
// In platform initialization (e.g., DesktopPlatform)
GuiContext gui = new GuiContext(canvas);
ServiceProvider.register(GuiContext.class, gui);
```

### Usage in Game/Tool
```java
public class EditorTool extends Game {
    private GuiContext gui;

    @Override
    public void load() {
        gui = ServiceProvider.get(GuiContext.class);
    }

    @Override
    public void draw(DeltaTime delta) {
        // Draw game content
        // ...

        // Draw UI overlay
        gui.begin();
        if (gui.button("Save", 10, 10, 100, 30)) {
            save();
        }
        gui.end();
    }
}
```

### Dependencies
- **Required**: `pixel-core` (Canvas, ServiceProvider)
- **Required**: `pixel-graphics` (rendering types)
- **Required**: `pixel-math` (Vector2, Rectangle)
- **Optional**: `lwjgl-yoga` (advanced flex layout - future)

## Comparison to Existing Solutions

| Feature | Pixel IMGUI | Dear ImGui | JavaFX/Swing |
|---------|-------------|------------|--------------|
| Mode | Immediate | Immediate | Retained |
| GC Pressure | Zero (Canvas pooling) | Zero (C++) | High (Java objects) |
| Integration | Native Canvas | Custom renderer | Separate UI thread |
| Use Case | Game tools | Debug UIs | General applications |
| Learning Curve | Low | Medium | High |

## References & Inspiration

- **Dear ImGui**: Industry-standard IMGUI for C++ (paradigm reference)
- **Unity's IMGUI**: Editor tools and OnGUI() (tooling patterns)
- **Nuklear**: Minimal IMGUI in C (single-header simplicity)
- **egui**: Rust IMGUI (pure immediate mode design)

## Open Questions

1. **Widget ID strategy**: Manual strings vs. auto-generated from call site?
2. **Retained hybrid widgets**: When to introduce for animations/complex state?
3. **Accessibility**: Screen reader support for tooling?
4. **Serialization**: Save/load window layouts for editor?

## Success Criteria

The extension is successful when:
1. A sprite editor can be built in < 500 lines of code
2. Zero garbage collection during UI interaction
3. Custom widgets can be created in < 50 lines
4. Visual consistency with modern game editor UIs (Unity, Unreal, Godot)
5. Can support full game engine editor UI (scene view, inspector, hierarchy, asset browser)

---

**Status**: Design Phase
**Target Pixel Version**: 0.4.x+
**Maintainer**: TBD
