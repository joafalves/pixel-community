This file provides guidance AI when working with code in this repository.

## Project Overview

**Pixel Framework** is a modular, high-performance OpenGL 2D game development framework for Java 17+. It provides platform-independent abstractions for rendering, audio, input, and game architecture, with desktop (LWJGL3) and experimental Android (OpenGL ES) implementations.

## Build System & Commands

### Build Tool
- **Gradle 8.x+** with wrapper included
- Java 17+ required
- All modules use `build.gradle` files with shared configuration from root

### Essential Commands

```bash
# Build all modules
./gradlew build

# Run all tests
./gradlew test

# Run a specific test class
./gradlew :pixel-<module>:test --tests <TestClassName>

# Build and run a demo
./gradlew :pixel-demo-desktop-learning:run

# Change demo main class (edit demos/desktop/demo-desktop-learning/build.gradle)
# Update: application { mainClass.set('org.pixel.demo.learning...') }

# Clean build artifacts
./gradlew clean

# Publish to Maven Local (for testing integration)
./gradlew publishToMavenLocal -DDESKTOP_BUILD_TARGET_PLATFORM=all -DPIXEL_RELEASE=LOCAL

# Generate Javadoc
./gradlew javadoc
```

### Platform-Specific Notes
- **macOS**: Add `-XstartOnFirstThread` VM argument (GLFW requirement)
- **Windows**: Standard Java launch works
- **Linux**: Requires OpenGL 3.3+ driver support

### Module Testing
Tests use JUnit 5 (Jupiter). Test classes in `src/test/java/` directories across modules:
- **modules/math**: Matrix, Vector, Rectangle, Polygon tests
- **modules/blueprint**: Dependency injection system tests
- **extensions/ext-ecs**: GameObject/Component system tests
- **extensions/ext-network**: Network utilities tests

## Architecture & Code Organization

### Module System

The framework is divided into core modules and platform implementations:

**Core Modules** (`/modules/`):
- **commons**: Lifecycle interfaces (Initializable, Loadable, Updatable, Drawable, Disposable), ServiceProvider pattern, event system, object pooling
- **math**: Vector2, Matrix4, Rectangle, Size, Camera math utilities
- **graphics**: Rendering abstractions (SpriteBatch, Canvas, CanvasRenderer, Shader, BlendMode)
- **content**: ContentManager, asset loading/caching, Importer pattern for resources
- **core**: GameContainer lifecycle, GameSettings, Camera2D, AudioPlayer interface
- **blueprint**: Dependency injection via annotations (@Auto, @Scheduled, @AfterAssembly)
- **pipeline**: Generic data processing pipelines for asset transformation

**Platform Implementations** (`/platform/`):
- **desktop**: LWJGL3 + OpenGL (GLFWWindowManager, GLGraphicsDevice, GLSpriteBatch, GlCanvas)
- **android**: OpenGL ES (experimental, disabled by default in settings.gradle)

**Extensions** (`/extensions/`):
- **ext-ecs**: Entity-Component-System (GameObject, GameComponent, SceneManager)
- **ext-ecs-extra**: Additional ECS components (Sprite, Text, etc.)
- **ext-decs**: Advanced ECS variant
- **ext-tween**: Animation tweening library
- **ext-network**: Networking utilities
- **ext-log4j2**: Advanced logging integration

### Game Lifecycle

All games extend `Game` (which extends `WindowGameContainer` → `GameContainer`):

```java
public class MyGame extends Game {
    public MyGame(GameWindowSettings settings) {
        super(settings);
    }

    @Override
    public void load() {
        // Load resources and initialize services
        // Called once after init, before first update
    }

    @Override
    public void update(DeltaTime delta) {
        // Game logic update (called every frame)
    }

    @Override
    public void draw(DeltaTime delta) {
        // Rendering (called every frame after update)
    }

    @Override
    public void dispose() {
        // Cleanup resources
        super.dispose();
    }
}
```

**Lifecycle States** (in order):
1. `NEW` → `INITIALIZING` → `INITIALIZED` (platform setup, services registered)
2. `LOADING` → `LOADED` (user's `load()` called)
3. `UPDATING` / `DRAWING` (game loop active)
4. `DISPOSING` → `DISPOSED` (cleanup)

### Service Provider Pattern

Framework uses static service registry for platform-specific implementations:

```java
// Retrieve services (registered during platform initialization)
SpriteBatch spriteBatch = SpriteBatch.create();
ContentManager content = ContentManager.create();
AudioPlayer audio = ServiceProvider.get(AudioPlayer.class);
SdfTextRenderer textRenderer = ServiceProvider.get(SdfTextRenderer.class);

// Services are singletons per game instance
```

Common services:
- `SpriteBatch` - Batched 2D sprite rendering
- `ContentManager` - Asset loading/caching
- `AudioPlayer` - Sound playback
- `SdfTextRenderer` - Signed distance field text rendering

### Canvas API (Modern Rendering)

The Canvas API provides a fluent interface for 2D drawing with auto-execution:

```java
Canvas canvas = new GlCanvas(800, 600);
canvas.begin();

// Rectangles
canvas.rect(x, y, w, h)
    .withFill(Color.BLUE)
    .withStroke(2, Color.WHITE)
    .withRoundedCorners(8);

// Circles with gradients
canvas.circle(x, y, radius)
    .withFillRadialGradient(Color.YELLOW, Color.RED);

// Text rendering
canvas.text("Hello", font, x, y)
    .withFill(Color.WHITE)
    .withStroke(Color.BLACK, 2)
    .withShadow(Color.GRAY, 2, 2, 0.5f)
    .withAlign(TextAlign.middleCenter());

// Lines and points
        canvas.line(x1, y1, x2, y2)
    .withStroke(2, Color.RED);

canvas.point(x, y)
    .withSize(5)
    .withFill(Color.GREEN);

canvas.end();
```

**Key Design Features**:
- **Auto-execution**: No `.apply()` or `.draw()` needed - last setter triggers rendering
- **Zero-GC**: Object pooling via single builder instance per shape type
- **Composition**: Canvas wraps CanvasRenderer; can access low-level renderer via `canvas.getRenderer()`
- **Fill + Stroke**: Both can coexist (like SVG/Canvas2D)
- **Transform/State**: `translate()`, `rotate()`, `scale()`, `save()`, `restore()` for coordinate transforms

## Content/Asset Loading

ContentManager provides centralized asset loading with caching:

```java
ContentManager content = ContentManager.create();

// Load assets (automatically cached by path)
Texture texture = content.load("path/to/texture.png", Texture.class);
SdfFont font = content.load("path/to/font.ttf", SdfFont.class);
Sound sound = content.load("path/to/audio.wav", Sound.class);

// Convenience methods
Texture tex = content.loadTexture("texture.png");
SdfFont font = content.loadFont("font.ttf", 24); // size in pixels

// Dispose all loaded assets
content.dispose();
```

**Importer Pattern**: Extend `ContentImporter<T>` and annotate with `@ContentImporterInfo(type = YourType.class)` to add custom asset types.

## Common Patterns

### Object Pooling
Use `Pool<T>` from commons module for zero-GC object reuse:
```java
Pool<Vector2> vectorPool = new Pool<>(Vector2::new, 100);
Vector2 v = vectorPool.obtain();
// ... use vector ...
vectorPool.free(v); // returns to pool
```

### Rendering Batches
SpriteBatch automatically batches draw calls for performance:
```java
SpriteBatch batch = SpriteBatch.create();
batch.begin(camera.getViewMatrix(), BlendMode.NORMAL_BLEND);

// All draws batched together
        for (Sprite sprite : sprites) {
        batch.draw(sprite.getTexture(), sprite.getPosition());
        }

        batch.end(); // Submits all batched draws to GPU
```

### Custom Shaders
```java
Shader customShader = new Shader(vertSource, fragSource);
batch.begin(camera.getViewMatrix(), BlendMode.NORMAL_BLEND, customShader);
        batch.setUniform("myUniform", value);
// ... draw with custom shader ...
batch.end();
```

## Demos & Examples

Comprehensive examples in `/demos/desktop/` subdirectories:

**demo-desktop-learning** - Feature demonstrations organized by topic:
- `canvas/` - Canvas API examples (gradients, text, shapes)
- `sprite/` - Sprite rendering (single, multi, animated)
- `text/` - Text rendering and styling
- `ecs/` - ECS system usage
- `audio/` - Sound playback and panning
- `input/` - Keyboard and gamepad input
- `physics/` - Physics integration examples

**demo-desktop-concept** - Full game concepts:
- Space shooter, terrain generation, platformer prototypes

**demo-desktop-imgui** - ImGui integration demo

To run demos, edit `demos/desktop/demo-desktop-learning/build.gradle` and change:
```gradle
application {
    mainClass.set('org.pixel.demo.learning.canvas.CanvasGradientDemo')
}
```

Then run: `./gradlew :pixel-demo-desktop-learning:run`

## Common Gotchas

1. **macOS crashes on start**: Add `-XstartOnFirstThread` to VM arguments (GLFW requirement)
2. **Canvas vs CanvasRenderer**: Use `Canvas` for fluent API, access `canvas.getRenderer()` for direct low-level calls
3. **Service retrieval timing**: Services only available after `GameContainer.init()` completes
4. **Asset paths**: Relative to classpath root; use `src/main/resources/` for demo assets
5. **OpenGL version**: Desktop requires OpenGL 3.3+ support
6. **Gradle module names**: All prefixed with `pixel-` (e.g., `:pixel-core`, `:pixel-desktop`)
7. **DrawOp auto-execution**: Last fluent method triggers rendering; avoid saving builder references
8. **Transform state**: Canvas transform methods (`translate()`, `rotate()`) affect subsequent draws until `restore()` called

## IMPORTANT: Running Games/Demos

**NEVER run game applications yourself** - they are GUI applications that require user interaction:
- Do NOT use `./gradlew run` or execute demo main classes
- Do NOT run background processes for games
- After making changes, BUILD the project to verify compilation: `./gradlew build`
- Let the USER run and test the game applications themselves
- You can verify the build succeeds, but leave execution to the user
