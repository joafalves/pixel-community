# Rendering guide

Pixel draws through one renderer, **URP** (the unified render pipeline), on desktop (OpenGL 3.3) and Android
(OpenGL ES 3.0). Games describe *what* to draw on **canvases**; a **render pipeline** decides *where and in which
order* it is drawn. This guide covers the parts a game uses. The design and its history are in
[design/urp-v2.md](design/urp-v2.md).

## The model

| Concept | What it is |
|---|---|
| `Layer` | A named bucket of draw commands. `Layer.ordered(name)` draws in submission order (HUD, UI); `Layer.sorted(name)` sorts by `depth` (worlds, y-sorting). Declare them once, as constants. |
| `Canvas` | Records commands into a layer: shapes, paths, images, nine-patches, text. One per layer. |
| `View` | A matrix plus how many pixels one unit covers: a `Camera2D`, or `ScreenSpace` for HUDs. |
| `RenderPipeline` | An ordered list of passes, built once and rendered every frame. |
| Pass | Draws layers through a view, to the screen or into a `RenderTarget`; or runs post effects; or custom code. |

```java
static final Layer WORLD = Layer.sorted("world");
static final Layer GLOW = Layer.ordered("glow").blend(BlendMode.ADDITIVE);
static final Layer HUD = Layer.ordered("hud");

@Override
public void load() {
    camera = new Camera2D(context());
    pipeline = RenderPipeline.builder(graphics().getRenderBackend())
            .pass(camera, WORLD, GLOW)               // the world, through the camera
            .pass(new ScreenSpace(context()), HUD)   // the HUD, in screen units, on top
            .build();
}

@Override
public void draw(DeltaTime delta) {
    Canvas world = pipeline.canvas(WORLD);
    world.image(hero, x, y).anchor(0.5f, 1f).depth(y);   // y-sorted
    world.circle(x, y, 40).fillRadial(x, y, 40, GOLD, Color.TRANSPARENT);
    pipeline.canvas(HUD).text(font, "Score " + score, 16, 16).size(24);
    pipeline.render(); // draws every pass, then clears the layers for the next frame
}

@Override
public void dispose() {
    pipeline.dispose(); // its own targets and effects too
}
```

Commands are *builders*: a command is committed when the next one starts (or at `render()`), so setters can come in
any order, and nothing is allocated per command. Don't keep builder references.

Layers keep their blend mode (`NORMAL_BLEND`, `ADDITIVE` for light, `MULTIPLY` for shade). Commands outside a pass's
view are culled. A layer can be drawn by several passes (the world again into a minimap target).

## Canvas

- **Shapes** are signed distance fields: edges stay one pixel soft at any zoom or rotation, and fill, stroke, dashes,
  gradients and soft shadows are drawn by a single quad.
  `rect`, `roundRect` (per-corner radii), `circle`, `ellipse`, `line` (caps, dashes).
- **Paths**: `moveTo/lineTo/quadTo/cubicTo/arc/close`, filled (any simple polygon) and/or stroked (joins, caps, miter
  limit).
- **Paint**: `fill(color)`, `fillLinear(...)`, `fillRadial(...)`, `stroke(width, color)` with `strokeAlign`
  (inside, center, outside), `shadow(dx, dy, blur[, spread], color)`.
- **Images and nine-patches**: `image(texture, x, y).size(...).source(...).tint(...).anchor(...)`,
  `ninePatch(texture, x, y, w, h).insets(...)`.
- **Text** with MTSDF fonts (sharp at any size): `text(font, "Hello", x, y).size(32).align(...).maxWidth(...)`, plus
  `stroke`, `shadow`/glow, `weight`, gradients, wrapping, ellipsis, kerning. Load fonts with
  `content().load("fonts/ui.ttf", SdfFont.class)` (generated on desktop, optionally cached), or a pre-baked atlas
  (`fonts/ui.json` + `.png`, see `SdfFontBaker`) on any platform.
- **State**: `save/restore`, `translate/rotate/scale`, `clip(x, y, w, h)` (nested clips never break batches).

## Render targets and post effects

```java
RenderPipeline.Builder builder = RenderPipeline.builder(graphics().getRenderBackend());
RenderTarget scene = builder.screenTarget(1f);      // follows the window size (0.5f: half resolution)
scene.setClearColor(BACKGROUND);
RenderTarget minimap = builder.target(320, 240);
pipeline = builder
        .pass(camera, scene, WORLD, GLOW)           // into the scene target
        .pass(minimapCamera, minimap, WORLD)        // the same layer again, into the minimap
        .post(scene, Effects.bloom(0.6f, 0.9f), Effects.vignette(0.4f))   // effects, then onto the screen
        .pass(new ScreenSpace(context()), HUD)      // crisp HUD on top
        .build();
...
hud.image(minimap.getTexture(), 16, 16).size(240, 180); // a target draws like any texture
```

Built-in effects (`Effects`): sepia, tint, pixelate, noise (film grain), border, vignette, bloom. Their setters can
change every frame. Write your own with `ShaderEffect`: one GLSL function `vec4 effect(vec2 uv)` that reads
`uSource` (premultiplied colors). Targets are cleared to their clear color the first time a pass draws into them in a
frame.

## Materials (custom shaders)

An **effect material** is one GLSL function that post-processes what the built-in shader computed for a fragment, so
clipping, strokes, text and anti-aliasing keep working:

```java
Material dissolve = Material.effect("""
        uniform sampler2D uNoise;
        vec4 effect(vec4 color, vec2 uv, Fragment f) {
            if (texture(uNoise, uv).r < f.data.x) discard;   // f.data: this command's values
            return color;
        }
        """).texture("uNoise", noiseTexture);
world.image(enemy, x, y).material(dissolve, progress);       // one batch for every enemy, whatever its progress
```

- `color` is premultiplied, and so must the result be. `Fragment` has `position` (layer space), `local` (from the
  shape's or image's center), `data` and `type`; `sampleImage(uv)` samples the command's own texture.
- Uniforms (`material.set("uTime", t)`) and up to 4 textures (`material.texture("uNoise", tex)`) belong to the
  material and apply to all its commands in a frame; per-command values go in the command's data (`f.data`).
- **Raw materials** (`Material.raw(vertex, fragment)`) are complete shaders, e.g. to move vertices (swaying grass).
- **Hot reload**: `material.setSource(...)` recompiles on next use; a compile error keeps the previous version and is
  reported by `getError()`. `SourceWatcher` reloads a shader file whenever it changes on disk.
- Commands with different materials can't share a draw call. In a sorted layer,
  `Layer.sorted("tiles").batchByMaterial()` groups equal-depth commands by material and texture (tiles, particles).

## Inspecting a frame

```java
canvas.rect(x, y, w, h).pick(entity.id);             // any command can carry a pick id
...
int id = pipeline.lastFrame().pick(WORLD, worldX, worldY);   // top-most pickable command at a point
FrameInfo frame = pipeline.lastFrame();              // per pass and layer: commands, culled, batches, draw calls,
frame.getBreaks(BatchBreak.TEXTURE_SLOTS);           // and why batches broke (material, texture slots, ...)
```

Picks use the shapes as drawn (round corners, strokes, dash gaps, glyph boxes, clips). The [dev tools](dev-tools.md)
show these statistics in game.

## Textures

Imported images are stored with premultiplied alpha, so smooth (linear) filtering has no dark or bright fringes; the
renderer works in premultiplied colors throughout. Data textures whose channels aren't colors (e.g. distance-field
atlases) turn it off with `TextureImporterSettings.builder().premultiplyAlpha(false)`. Material shaders sampling
their own textures get premultiplied texels.

## Performance notes

- Everything is allocation-free per frame once warmed up (draw lists, batching, stats, picking).
- Layers holding only plain images (no style, clip or material) use the light sprite vertex format (96 instead of
  208 bytes per quad); commands outside their clip rectangle are culled.
- A texture used by consecutive batches keeps its texture unit, so it isn't bound again.
- The backend queries the GPU's state once per frame (synchronous queries stall the CPU, badly on Android).
- A batch breaks when the vertex layout, the material, the texture slots (16; 12 with a material), the clip table or
  the style table (128 distinct strokes/gradients/shadows per batch) runs out. `FrameInfo` and the dev tools'
  Render tab say which.
- Sprites through `SpriteBatch` (`graphics().createSpriteBatch()`) use a lighter vertex format than canvas images;
  both go through the same backend.
- Textures created in a lost GL context (Android) are not restored yet; targets and materials are.
