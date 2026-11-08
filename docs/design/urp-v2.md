# URP v2 — Unified Render Pipeline

| | |
|---|---|
| **Status** | Draft for review |
| **Branch** | `task/urp-v2` (based on `task/devel-hardening`; rebase onto `devel` once that is merged) |
| **Replaces** | `task/urp` (kept for reference only; not merged) |
| **Scope** | Rendering only. GUI and editor come later. Mobile is a design requirement, but its backend is a follow-up (§5.8). |

---

## 1. Why a v2

`task/urp` tried to unify rendering by adding a dispatcher on top of many independent renderers:
`RenderPipeline` picked a renderer by the `Renderable`'s class. Each of the ~8 renderers (`SpriteBatch`, `DirectRenderer`,
`SdfShapeRenderer`, `SdfTextRenderer`, `GLCanvasRenderer`, …) had its own shader, buffers and `begin`/`end`. The results:

- The batch was flushed whenever the renderer type changed, so mixing sprites, shapes and text was slow. The
  `groupByRenderer` option "fixed" that by breaking depth order.
- `Canvas` (which the GUIs needed for clipping, text and shapes) could not live inside the pipeline. All three GUI
  attempts (Flux, Rune, Weaver) therefore drew through a private `Canvas` and bypassed the URP.
- Scissor clipping ignored transforms and the Retina pixel ratio, and every clip change flushed the batch.

The one genuinely unified piece was the SDF uber-shader behind `Canvas`, which draws shapes, text glyphs and images in
one draw call. **v2 makes that the foundation.** Instead of unifying many renderers at the top, there is one draw list
and one batcher at the bottom, with several front-ends on top.

## 2. Goals and non-goals

**Goals**
1. One rendering path for game world, HUD and (later) GUI, with correct ordering and few draw calls.
2. Existing games keep working: the `SpriteBatch` API stays and becomes a front-end.
3. Correct on HiDPI/Retina: logical units everywhere, pixels only inside the backend.
4. Clipping, transforms, custom shaders, render targets and post-processing are first-class.
5. Zero allocations per frame in steady state.
6. Testable without a GPU: recording, sorting, batching decisions and text layout run in unit tests.
7. Editor-ready (§9): several pipelines per process, inspectable frames, picking. No static state.

**Non-goals (for this branch)**
- GUI framework (Flux/Rune/Weaver successor). It comes later on its own branch and builds on `Canvas`.
- Editor, game-loop decoupling, scene serialization.
- Vulkan/Metal backends. The backend SPI (§5.7) keeps that door open but nothing is implemented.
- 3D, lighting, normal maps.

## 3. Architecture

```
 Front-ends (what to draw)   SpriteBatch (compat) · Canvas (shapes / images / text / paths) · [GUI later]
                                        │ record commands (plain data, no GL)
 Draw list                  per layer: command arrays + sort keys + clip table + pick ids
                                        │ sort (sorted layers) / keep order (ordered layers), then merge into batches
 Pipeline (passes)          LayerPass(view, target) → PostPass(effects) → LayerPass(UI) → …
                                        │ batches: (material, texture slots, vertex range)
 Backend SPI                RenderBackend  ── implemented by ──►  GLRenderBackend (platform/desktop)
```

Everything above the backend line lives in `modules/graphics` and contains **no GL calls**.

## 4. Core concepts

### 4.1 Layer
A named bucket of draw commands that is consumed by one pass. Each layer declares a mode:

| Mode | Ordering | Batching | Used for |
|---|---|---|---|
| `ORDERED` | submission order (painter's) | consecutive compatible commands merge | HUD, GUI, overlays |
| `SORTED` | by depth (float); **equal depth keeps submission order** | consecutive compatible commands merge after the sort | game world, y-sorting, `SpriteBatch` compat |
| `SORTED` + `batchByMaterial()` (opt-in) | by depth; **within equal depth, grouped by material and texture** (order among them unspecified) | maximal | tiles, particles, anything where same-depth items don't overlap or their overlap order doesn't matter |

"Compatible" means same material and the texture fits in the batch's texture slots (§5.3). Mixing these behaviours in
one queue is what broke `task/urp`. Here it is a property of the layer.

The opt-in mode exists because keeping submission order and grouping by texture conflict at equal depth: if two
overlapping sprites share a depth, regrouping can change which one is on top. That is never done silently. Today's
`SpriteBatch` keeps submission order at equal depth, so its compat path uses plain `SORTED`.

### 4.2 Draw command
Almost everything is a **quad command**: position/size or 4 corners, UVs, colour, a *shape type* (textured, SDF rect,
round rect, circle, line, point, glyph), shape parameters, a material, a texture and a clip index. Path fills
(triangulated polygons) are **geometry commands** that reference a vertex range in the same buffer.

Commands are stored struct-of-arrays in reusable primitive arrays, and the arrays grow but never shrink. No object is
allocated per command.

### 4.3 Sort key (sorted layers)
- `SORTED`: the key is the depth only (float → order-preserving 32-bit int). A **stable** sort over command indices
  keeps submission order within equal depth.
- `SORTED` + `batchByMaterial()`: 64-bit key `[ depth : 32 | material : 16 | texture : 16 ]`, also stable. As built
  (3c), the material part is the material's id (1 to 65535) and the texture part a 16-bit identity hash: no lookup
  tables, and a collision only costs batching, never correctness (the order within equal depth is unspecified).
- `ORDERED`: no sort.

In all modes, batching then walks the commands in their final order and merges consecutive compatible commands.

### 4.4 Material (how multiple shaders work)
`Material` = shader program + blend mode + uniform values + bound textures. There are three kinds of shader, from
most to least common:

| Kind | What the user writes | What they get for free | Batching |
|---|---|---|---|
| **Built-in** (default) | nothing | sprites, SDF shapes, SDF text, clipping, all in one program | everything batches together |
| **Effect material** | one GLSL function: `vec4 effect(vec4 color, vec2 uv, Fragment f)` | same vertex format, texture slots, clipping and SDF coverage; the snippet is injected into the uber-shader template and compiled as a *variant* | batches with other commands using the same material + uniform values |
| **Raw material** | full vertex + fragment shader | a documented vertex layout and uniforms | always its own batch |

Effect materials cover most real cases (water, dissolve, flash-on-hit, palette swap, outline-by-texture) without the
user re-implementing clipping, texture slots or AA. Example:

```glsl
// shaders/dissolve.effect.glsl
uniform sampler2D uNoise;
uniform float uProgress;
vec4 effect(vec4 color, vec2 uv, Fragment f) {
    if (texture(uNoise, uv).r < uProgress) discard;
    return color;
}
```
```java
Material dissolve = Material.effect(content.loadText("shaders/dissolve.effect.glsl"))
                            .texture("uNoise", noise);
world.image(atlas, enemy.frame(), enemy.position()).material(dissolve.with("uProgress", t));
```

Compiled programs are cached per (template, variant, snippet). In sorted layers the sort key groups commands by
material, so N enemies with the same dissolve material and progress value still produce one batch. Changing a
uniform value creates a new material *instance*, which batches only with commands that share it (frame stats show
why a batch broke, §6.3). This replaces `DirectRenderer`. Post effects (§5.6) are a separate, full-screen kind of
material.

**As built (3c), the model changed in two places:**
- *No material instances.* Uniforms and textures belong to the `Material` and are read when the frame renders, so
  `with(...)` (an allocation per call) was dropped. Values that differ per command go in the command's **material
  data**, a vec4 (`.material(m, x, y, z, w)`, read as `f.data`), stored in the command's style record. N enemies
  with the same dissolve material and different progress values share one batch.
- *Blending stays per layer.* A blend mode per material would break batches mid-layer and make draw order depend on
  materials; additive materials go in an additive layer.

The snippet signature is `vec4 effect(vec4 color, vec2 uv, Fragment f)`: `color` is what the built-in shader computed
(premultiplied, after coverage, stroke and texture), and `Fragment` has `position` (layer space), `local` (from the
center of shapes and images), `data` and `type`; `sampleImage(uv)` samples the command's own texture. Materials have
up to 4 textures on the last 4 texture units (material batches use 12 of the 16 slots for command textures).

### 4.5 Clipping
Each command carries a **clip id** into the draw list's frame-local `ClipTable` (axis-aligned rectangles in layer
space; 0 = no clip). The batcher copies the clips a batch uses into that batch's slice of the pass clip table,
which the backend binds as a uniform-buffer range. Vertices carry the 16-bit batch-local index. The vertex shader
passes the layer-space position through, and the fragment shader drops fragments outside the rectangle.
Consequences:
- Clip changes do **not** flush the batch, so nested scroll panes cost nothing extra. A batch holds up to 256 clips;
  more start a new batch.
- Clip rects are transformed on the CPU with the current transform and intersected with the enclosing clip, so
  translated and scaled content clips correctly. Under a rotated transform the clip is the bounding box of the
  rotated rectangle, with a one-time warning (implemented in Phase 2a; stencil clipping can come later).
- No `glScissor` and no viewport readback, so Retina is correct by construction.

### 4.6 View
`View` provides the projection·view matrix and the visible bounds of a pass (for culling). `Camera2D` implements it.
`ScreenSpace.logical()` is a view in logical window units, used for HUD/UI.

### 4.7 Pass and pipeline
A `RenderPipeline` is an ordered list of passes, built once:

- `LayerPass(layers…, view, target, clear?)`: draws the batches of one or more layers.
- `PostPass(source, effects…, destination)`: full-screen effects (bloom, vignette, the existing sepia/pixeler/…).
- `CustomPass(callback)`: escape hatch with direct backend access.

Output is the screen or any `RenderTarget`. Several pipelines can exist at once, which is what the editor needs.

### 4.8 Front-ends
- **Canvas**: the main API. Shapes, images, nine-patch, text, paths, a transform stack and a clip stack. Records into
  one layer. Fluent builder:
  `canvas.rect(x, y, w, h).fill(c).stroke(2, c2).radius(8).depth(d)`.
  Builder rule: the builder is one reused cursor object. The command is committed when the next command starts or
  when the layer is rendered, so setters can be called in any order and nothing is allocated. Don't keep builder
  references.
- **SpriteBatch (compatibility)**: the current abstract API, unchanged. A new implementation records into an internal
  single-layer pipeline and renders at `end()`, which matches today's behaviour. It is registered in place of
  `GLSpriteBatch` once parity is proven (Phase 1).
- **GUI**: later. It will paint through `Canvas` into an `ORDERED` layer, or a `SORTED` one for world-space UI.

### 4.9 Text

Text quality is a first-class requirement, not a by-product of the shape shader.

**Glyph atlas format: MTSDF.** This is multi-channel signed distance fields with a true SDF in the alpha channel,
using Chlumsky's msdfgen algorithm.
- The **RGB channels (MSDF)** keep corners sharp at any scale. Plain single-channel SDF, which is what
  `stbtt_GetGlyphSDF` produces and what `task/urp` used, rounds corners when text is drawn much larger than the atlas
  size.
- The **alpha channel (true SDF)** gives accurate distances away from the edge. Wide outlines, glows and soft shadows
  need this, and the MSDF median alone produces artifacts there.

**Generation (two sources, one runtime format)**
1. **Runtime, desktop:** LWJGL ships `lwjgl-msdfgen` (msdfgen with `msdf_generate_mtsdf`, glyph shapes loaded through
   FreeType by `msdfgen-ext`). Importing a `.ttf`/`.otf` generates the atlas at load time (about 50 ms for 192
   glyphs, rendered in parallel). Caching it on disk is opt-in (`SdfFontSettings.cacheDirectory`); the cache is
   written in the pre-baked format below, so cached atlases can be shipped as is.
2. **Offline, any platform including mobile:** atlases pre-baked with `msdf-atlas-gen` (PNG + JSON metrics),
   imported as is. This is how shipped and mobile builds avoid native generation at runtime.

Both produce the same `SdfFont` (atlas texture + glyph metrics + kerning + `distanceRange`). Import settings: glyph
size in the atlas (default 48 px), distance range (default 8 px), character sets.

**Rendering (in the uber-shader, glyph shape type)**
- **Screen-space anti-aliasing:** `screenPxRange = distanceRange × (on-screen glyph size ÷ atlas glyph size)`,
  computed with derivatives (`fwidth`) and clamped to ≥ 1. Edges stay one pixel soft at any zoom, rotation or Retina
  ratio. This is the method recommended by msdfgen.
- **Effects, all in one pass per glyph** and composited back-to-front in premultiplied alpha:

| Effect | Parameters | Uses |
|---|---|---|
| Fill | colour or per-vertex gradient | MSDF median |
| Stroke / outline | width (px), colour, alignment (outside / centre / inside) | MSDF for the inner edge, true SDF for the outer edge |
| Shadow / glow | offset, softness, colour | true SDF (alpha) |
| Weight | bold/thin via threshold shift | MSDF median |

- **Limit (documented):** outline, glow and weight can only extend as far as the distance range encoded in the atlas.
  With the defaults (8 px range at 48 px), an outline can be about 4 px wide when text is drawn at 48 px. The font
  import settings raise the range when bigger effects are needed. The API clamps and warns instead of rendering
  artifacts.
- **Small text:** distance fields get soft below about 12 px. For pixel-art and very small UI text, bitmap fonts
  (`Font`/`GLFont`) remain supported and render as plain textured quads in the same batch.

**Layout and measurement** (`TextLayout` and `SdfFont.measure`, GL-free and unit-tested): kerning, line breaking,
alignment, letter and line spacing, and max width with ellipsis. The future GUI layout depends on them.
**Out of scope:** complex shaping (ligatures, right-to-left, Indic scripts). LWJGL ships `lwjgl-harfbuzz` for that
later, and the layout API is shaped so it can be added behind `TextLayout`.

### 4.10 Shapes and their effects
Shapes use the same distance-field idea, with **exact analytic distance functions** instead of an atlas: rect, round
rect (with per-corner radii), circle, ellipse, line/capsule, point, and arc/ring. Because every shape is a distance
field, shapes and text share the same effect vocabulary and the same anti-aliasing code:

- **fill**: solid, linear or radial gradient
- **stroke**: width, alignment (outside / centre / inside), optional dash pattern on lines/rects
- **soft shadow / glow**: offset, blur, spread. Rounded-rect shadows use the closed-form Gaussian approximation
  (one quad, no blur pass).
- **combined fill + stroke + shadow in one command**, as the HUD and GUI will need

Arbitrary paths (polygons, curves) are triangulated on the CPU. Their strokes are expanded into geometry with joins
and caps and anti-aliased with a 1 px edge fade. This is the one place without an exact distance field, so dashing
and soft shadows on arbitrary paths are not in v2.

## 5. GPU side (backend)

### 5.1 One uber-shader (plus variants)
One template program handles textured quads, SDF shapes (§4.10), MTSDF glyphs (§4.9) and raw triangles. Branching is
by a flat per-vertex shape type. All fragments of a quad take the same branch, so the cost is low. The template has
two injection points used to build **variants**: the effect snippet of an effect material (§4.4) and compile-time
feature flags. An example flag is a sprites-only variant with no SDF code, which is used automatically for layers that
contain only images, if the benchmark shows it matters.

The SDF shape math is ported from `task/urp`'s `sdf_batch.*.glsl`. The glyph path is rewritten for MTSDF. Changes:
- Texture sampling keeps the **switch-over-constant-indices** approach (as in `multiTexture.frag.glsl`; the branch
  shader already does this). Indexing a sampler array with a non-constant value is invalid GLSL and fails on
  Intel/AMD and GLES.
- There is no special font-atlas unit: an atlas is just a texture in a slot.
- A per-vertex clip test is added.

### 5.2 Vertex format (initial proposal; the spike decides)
Indexed quads: 4 vertices plus a shared static index buffer.

| attribute | type | bytes |
|---|---|---|
| position | 2 × f32 | 8 |
| uv | 2 × f32 | 8 |
| colour | 4 × u8 normalized | 4 |
| local coords (for SDF) | 2 × f32 | 8 |
| shape params (radius, stroke width, …) | 4 × f32 | 16 |
| shape type · texture slot · clip index | u8 · u8 · u16 | 4 |
| style index | u16 (+ u16 padding) | 4 |
| **total** | | **52 B/vertex, 208 B/quad** |

For comparison, `task/urp`'s SDF batch used 76 B/vertex with 6 vertices per quad, which is 456 B/quad. But today's
`GLSpriteBatch` uses only **216 B per sprite** (6 × 36 B, measured in the baseline). So this full format is no win
for plain sprites, which are the most common primitive. Phase 1 therefore uses **two vertex layouts** from the start:

| layout | attributes | bytes/vertex | bytes/quad |
|---|---|---|---|
| **sprite** (textured quads, glyphs of bitmap fonts) | position 2×f32 · uv 2×f32 · colour 4×u8 · slot u8 + 3 reserved bytes (clip index from Phase 2) | 24 | 96 |
| **full** (SDF shapes, MTSDF text, effects) | as in the table above | 52 | 208 |

Each batch uses one layout, and a layout switch counts as a batch break. A layer full of sprites uploads about 2.25×
less than today (measured: 1,875 KB vs 4,219 KB per frame for 20k sprites). **Phase 1 decision:** UVs stay `f32`
instead of normalized `u16`. The 4 bytes saved per vertex weren't worth the risk of texel-edge sampling differences
on large atlases, and the upload gate (≤ 50% of baseline) is met either way.

**Style table.** Effects need more data than fits in a vertex: stroke colour and alignment, shadow offset, blur and
colour, gradient stops, dash pattern, text weight. These go into a per-batch **style table** in a UBO, indexed by the
style index. Identical styles are de-duplicated, so a paragraph of outlined text uses one entry. Like the clip table,
it is sized to stay within GLES 3.0's 16 KB UBO minimum (about 256 styles per batch). A full table closes the batch
and is recorded as a break reason. Plain sprites use style 0 and never touch the table.

### 5.3 Texture slots
Each batch binds up to `min(GL_MAX_TEXTURE_IMAGE_UNITS, 16)` textures. A new texture that does not fit closes the
batch (recorded as a break reason).

### 5.4 Streaming
Vertices are written to a persistent CPU buffer and uploaded once per pass with buffer orphaning
(`glBufferData(null)` then `glBufferSubData`). The instanced path from `GLFastSpriteBatch` is kept as a benchmark
reference. Instancing for plain sprites is an optimisation for later, if the numbers ask for it.

### 5.5 Render targets
`RenderTarget` is an FBO with a colour texture and an optional stencil. It is sized in logical units and allocated at
the pixel ratio. It is ported from `task/urp`'s `GLRenderTarget`.

### 5.6 Post effects
`PostEffect` is a full-screen material that ping-pongs between two targets. The existing effect shaders (border,
noise, overlay, pixeler, sepia) are ported as effects.

### 5.7 Backend SPI (in `graphics`, implemented by the platform)

```java
public interface RenderBackend extends Disposable {
    BackendCapabilities capabilities();                    // max texture slots, max target size, …
    RenderTarget createTarget(int width, int height, TargetOptions options);
    MaterialHandle compile(Material material);             // cached per material
    void beginPass(PassState state);                       // target, viewport, clear, view matrix
    void submit(VertexStream vertices, BatchList batches); // batches = material + slots + index range
    void endPass();
}
```

This is the only interface that touches GL. Unit tests use a recording fake backend, so batching decisions can be
asserted without a GPU.

### 5.8 Mobile / GLES portability rules
Mobile is supported by design. These rules keep the desktop GL 3.3 backend portable to a GLES 3.0 backend (Android,
and iOS through ANGLE/MoltenGL if ever needed):

- **Shaders** are written once in a common GLSL subset. A small preamble selects `#version 330 core` or
  `#version 300 es` and adds `precision highp float;`, because SDF math needs highp in the fragment shader on mobile.
- **Allowed:** VAOs, UBOs, instancing, FBOs, MRT, `texelFetch`, integer vertex attributes, `flat` varyings. All of
  these are in GLES 3.0.
- **Not used:** geometry shaders, texture buffer objects, persistent mapped buffers, multi-draw indirect, `GL_QUADS`,
  dynamic sampler-array indexing.
- **Clip table:** a UBO (fits GLES 3.0's minimum 16 KB, about 1000 rects), with one table per batch.
- **Fonts on mobile:** pre-baked MTSDF atlases (§4.9 source 2), since LWJGL's msdfgen natives are desktop-only.
- **Texture slots:** capped by `capabilities()`. GLES 3.0 guarantees 16 fragment units.

Implemented (see "Platform base: Android" in §8): one `GLRenderBackend` in `platform/opengl` runs on both, through
`GLApi` (desktop: LWJGL, Android: `GLES30`). The shaders are the same files; only the preamble differs.

## 6. API sketch

### 6.1 Setup and frame

```java
public class DungeonGame extends Game {
    static final Layer WORLD = Layer.sorted("world");
    static final Layer UI    = Layer.ordered("ui");

    private RenderPipeline pipeline;
    private Canvas world, hud;

    @Override
    public void load() {
        var scene = RenderTarget.create(getGraphicsDevice(), getVirtualWidth(), getVirtualHeight());
        pipeline = RenderPipeline.builder(getGraphicsDevice())   // no static access
                .pass(WORLD, camera, scene)
                .post(scene, Effects.bloom(0.6f), Effects.vignette(0.3f))
                .pass(UI, ScreenSpace.logical())
                .build();
        world = pipeline.canvas(WORLD);
        hud   = pipeline.canvas(UI);
    }

    @Override
    public void draw(DeltaTime delta) {
        world.image(atlas, player.frame(), player.position()).anchor(0.5f, 1f).depth(player.y());
        world.image(atlas, Sprites.WATER, waterArea).material(water.with("uTime", delta.getTotalElapsed()));

        hud.roundRect(16, 16, 220, 64, 8).fill(PANEL_BG);
        hud.text(font, "HP " + player.hp(), 32, 40).size(20).fill(Color.WHITE);
        hud.clip(16, 96, 220, 160, () -> drawLog(hud));

        pipeline.render();   // sort, batch, run passes; layers are cleared for the next frame
    }

    @Override
    public void dispose() {
        pipeline.dispose();  // owns its targets, buffers and compiled materials
        super.dispose();
    }
}
```

### 6.2 Compatibility (unchanged game code)

```java
spriteBatch.begin(camera.getViewMatrix());
spriteBatch.draw(texture, position, Color.WHITE, Vector2.HALF, 2f);
spriteBatch.end();   // now: record → sort → batch → draw, same visual result
```

### 6.3 Frame inspection (editor and debugging)

Two levels of statistics, one source of truth:

- **Device counters** (exist since Phase 0): `getGraphicsDevice().getStats().lastFrame()` gives draw calls,
  primitives, vertices, uploaded bytes and texture binds of the last completed frame. Whatever talks to the GPU
  records into them (today `GLSpriteBatch`, from Phase 1 `GLRenderBackend`), and the game loop publishes them at
  the end of each frame. They are per device, not per front-end, because several front-ends share draw calls.
- **Pipeline frame stats** (Phase 3) add *why* on top: per pass and layer breakdown, commands, batches and break
  reasons. They're built from the same backend submissions, not counted separately.

```java
RenderStats.Frame gpu = getGraphicsDevice().getStats().lastFrame();   // draw calls, uploads, binds (device-wide)
FrameStats stats = pipeline.lastFrame().stats();   // commands, batches, draw calls, per-pass timings
stats.batchBreaks();                               // e.g. "material changed: water", "texture slots full"
pipeline.lastFrame().pick(worldPos, WORLD);        // top-most command with a pick id at that point
```

## 7. Module changes

No new modules. `graphics` already is the rendering abstraction module, and adding a module would only move
churn around.

| Module | Change |
|---|---|
| `modules/commons` | None. |
| `modules/math` | None (an order-preserving float→int helper goes in `graphics`). |
| `modules/content` | None required. `Texture` stays the texture abstraction. The `SdfFont` importers are registered by the platform like the other importers. |
| `modules/graphics` | **New packages** (all GL-free):<br>• `org.pixel.graphics.draw`: `DrawList`, `Layer`, `LayerMode`, `SortKey`, `ClipTable`, `Material`, `FrameSnapshot`, `FrameStats`<br>• `org.pixel.graphics.canvas`: `Canvas`, command builders, `Paint`, `Path` + triangulation, `NinePatch`<br>• `org.pixel.graphics.text`: `SdfFont`, `SdfFontData`, `SdfFontSettings`, `TextLayout`, `TextAlign`, `TextBaseline`, `GlyphPacker`<br>• `org.pixel.graphics.pass`: `RenderPipeline` + builder, `View`, `ScreenSpace`, `RenderTarget`, `PostEffect`, `Effects`<br>• `org.pixel.graphics.backend`: `RenderBackend` SPI, `BatchList`, `VertexStream`, `PassState`, `BackendCapabilities`<br>**Changed:** `SpriteBatch` gets a draw-list implementation (`DrawListSpriteBatch`). `GraphicsDevice` exposes `backend()`.<br>**Since Phase 0:** `org.pixel.graphics.RenderStats` (device-level per-frame GPU counters); `GraphicsDevice.getStats()` and `GraphicsDevice.endFrame()` (called by the game loop).<br>**Deprecated (removed before merge, §8 Phase 4):** `RenderEngine`, `StatefulRenderEngine`, `RenderEngine2D`, `DrawBatch`. **Deleted:** `PostProcessor` (unused). |
| `modules/core` | **Since Phase 3.0:** `GameApp`, `GameContext`, `GameWindow`; `GameContainer` is the platform host base (no longer extended by games); `Camera2D(GameContext)`. Blueprint dependency removed. **Phase 3:** `Camera2D implements View`. |
| `modules/blueprint` | **Removed in Phase 3.0.** |
| `modules/commons` (service package) | **Removed in Phase 3.0:** `ServiceProvider`, `ServiceFactory`, `ServiceRegistrar`, ... |
| `platform/desktop` | **New** `org.pixel.graphics.opengl.render`: `GLRenderBackend`, `GLBatchRenderer`, `GLRenderTarget`, `GLMaterialCompiler`, `GLTextureSlots`.<br>**New** shaders in `engine/shader/opengl/urp/` (uber, post effects).<br>**New** `GLSdfFontImporter`: MTSDF generation through `lwjgl-msdfgen` + `lwjgl-freetype` (`MsdfAtlasGenerator`), plus
pre-baked `msdf-atlas-gen` atlases. **New dependencies:** `org.lwjgl:lwjgl-msdfgen` and `org.lwjgl:lwjgl-freetype`
(+ natives), same LWJGL version (3.4.3).<br>**Changed:** `GLGraphicsDevice` creates the backend. `GLContentManagerFactory` registers the SDF font importer. The `SpriteBatch` service switches to `DrawListSpriteBatch` after parity.<br>**Deprecated → removed before merge:** `GLSpriteBatch`, `NvgRenderEngine` (and the `lwjgl-nanovg` dependency), the `GL*EffectShader` classes (replaced by effects). |
| `platform/opengl` | **New** (plain Java): `GLApi` (the GL functions the renderer uses), `GLProgram`, `GLTexture`, and the shared `GLRenderBackend` with its programs and shaders. |
| `platform/android` | Runs the shared backend through `AndroidGLApi` (OpenGL ES 3.0). Optional build: `pixel.android=true`. |
| `extensions/*` | Unchanged. `ext-ecs` keeps using `SpriteBatch` through the compat path. |
| `demos` | New `learning/urp/*` demos per phase. The 4 NanoVG-based demos (`RoadCarGame`, `CelestialGravitationGame`, `IsometricGridDemo`, `PrimitivesRenderDemo`) are ported to `Canvas`. A sprite benchmark demo is added. |

## 8. Plan (all on `task/urp-v2`)

Each phase ends green (`./gradlew build`) and is reviewable on its own.

**Phase 0: foundations** ✅ (see [urp-v2-baseline.md](urp-v2-baseline.md))
- This document (reviewed).
- LWJGL upgraded 3.3.6 → 3.4.3 (no API changes needed). The new font bindings (§4.9) use the same version.
- `RenderStats` on the **graphics device** (`getStats().lastFrame()`: draw calls, primitives, vertices, uploaded
  bytes, texture binds), published by the game loop through `GraphicsDevice.endFrame()`. `GLSpriteBatch` receives the
  device's stats through its factory and records into them. The Phase 1 backend does the same, so nothing moves
  later.
- Render benchmark demo (`./gradlew :pixel-demo-desktop-learning:renderBenchmark`) with 6 scenarios. It reports CPU
  time, GPU time (timer queries), frame-time percentiles and draw calls, and writes a CSV.
- Baseline recorded. It found that today's depth sorting is only correct within 256 sprites, and that texture-slot
  overflow costs 951 draw calls per frame.

**Phase 1: draw list, batcher and compat sprites** ✅ (results in
[urp-v2-baseline.md](urp-v2-baseline.md#phase-1-results))
- `org.pixel.graphics.draw`: `DrawList` (struct-of-arrays, `ORDERED`/`SORTED`), `SortKeys`, `Colors`, `Batcher`.
  The `SORTED` order is a stable LSD radix sort on the depth part of the key: O(n) and allocation-free, where
  `Arrays.sort(long[])` may allocate a merge buffer.
- `org.pixel.graphics.backend`: `RenderBackend` (`capabilities`, `beginPass`, `submit`, `endPass`; targets and
  materials join in Phase 3), `BackendCapabilities`, `PassState`, `VertexLayout.SPRITE`, `VertexStream`, `BatchList`.
- `DrawListSpriteBatch` + `DrawListSpriteBatchServiceFactory`: the default `SpriteBatch` service since the gates
  passed. (`GLSpriteBatch` was kept for the benchmark and parity check until the legacy cleanup.)
- `platform/desktop`: `GLRenderBackend` (indexed quads from a static 16-bit index buffer, at most 16,384 quads per
  draw, chunked uploads by address with index offsets so no base-vertex call is needed and it stays GLES 3.0
  compatible) and the sprite program (`engine/shader/opengl/urp/sprite.*.glsl`, version preamble added in code).
  `GLGraphicsDevice` owns the backend.
- `Font.getTexture()`: bitmap fonts expose their atlas as a `Texture`, so text batches with sprites.
- Behaviour changes: global stable depth order (it was per 256-sprite chunk), `'\n'` in `drawText` starts a new line
  (it used to be ignored unless the font had a glyph for it), and `SpriteBatch.getShader()` is deprecated.
- Tests (GL-free, fake backend): stable/global sort, batching rules, vertex contents, geometry identical to the
  classic `Matrix4` math on 500 random sprites, text placement, begin/end contract, and **0 bytes allocated** for 20
  frames of 20k sprites plus text.
- GPU checks: `renderParity` (7 scenes, classic vs draw list, pixel comparison: no mismatches) and `renderBenchmark`
  (both renderers in one session). All gates passed.

**Phase 2: Canvas and text** (split into reviewable slices)

*2a: Canvas core* ✅
- `VertexLayout.FULL` (52 B) and the shape program (`engine/shader/opengl/urp/shape.*.glsl`): SDF rounded rect
  (per-corner radii), ellipse (exact circle), segment (butt/round/square caps), textured quads. Coverage uses
  `fwidth`, so edges stay one pixel soft under any scale or rotation (no MSAA needed). Fill and stroke are
  composited in one pass (premultiplied).
- `StyleTable` (frame-local, de-duplicated stroke styles) and `ClipTable`. The batcher remaps both into
  per-batch, uniform-aligned table slices, and the backend binds them with `glBindBufferRange` (GLES 3.0
  compatible).
- `org.pixel.graphics.canvas`: `Canvas` (fluent builders committed on the next command or state change, a
  `save`/`restore` stack, translate/rotate/scale, nested clipping), `CanvasBatch` (immediate begin/end owner until
  Phase 3 layers), `StrokeAlign`, `LineCap`.
- One pass can mix sprite and shape batches: `RenderData` holds one vertex stream per layout, and a layout switch is a
  batch break.
- Tests: batcher (layout breaks, table remapping/alignment/capacity), canvas geometry, stroke padding, transforms,
  commit-on-state-change, clipping (nested, restored, rotated), lines, images, depth order, and **0 bytes
  allocated** for 20 frames of 8,000 canvas commands.
- Known limitation: the anti-aliasing margin around shapes is about 2 layer units, scaled by the canvas
  transform but not by the camera. Under a strongly zoomed-out view the soft edge can be slightly cut. Phase 3
  fixes this: the pass knows its view scale and passes it to its canvases.
- Demo: `urp/CanvasShapesDemo`. Its 53-shape frame (strokes, radii, lines, rotation, a clipped scrolling panel with
  images and nested clips) is **1 draw call**. Sprite parity and benchmark unchanged.

*2b: paints and effects* ✅
- Style record widened to 7 vec4 (`StyleTable`): stroke, dash, gradient geometry and end color, shadow color and
  geometry, paint type. Styles are de-duplicated per frame (50 identically styled cards = 1 entry). A batch holds 128
  styles (14 KB block, within GLES 3.0's 16 KB guarantee).
- `fillLinear(...)` / `fillRadial(...)` on every shape. Gradient points are given in the shape's coordinate space and
  converted to its local frame, so they follow transforms and line directions.
- `shadow(offsetX, offsetY, blur[, spread], color)`: closed-form Gaussian for rounded rectangles (Evan Wallace's
  method, 4 samples), a Gaussian across the edge distance for ellipses and lines, and a hard shadow when blur is 0.
  With no offset it doubles as a glow. The shadow, fill and stroke of a shape are one quad.
- `dash(length, gap)` / `dashPhase(phase)` on lines (the line body) and ellipses (the stroke; exact for circles,
  mean-radius approximation for ellipses). On ellipses the pattern is stretched slightly so a whole number of
  dashes fits the outline, which avoids a seam where it wraps. Dash ends are flat. **Not yet:** dashed rectangle strokes, which need
  an arc-length parameter along a rounded rectangle.
- `ninePatch(texture, x, y, w, h).insets(...)`: 9 textured quads. Corners shrink proportionally when the target is
  smaller than the borders.
- Tests: local-frame gradient conversion, shadow reach and padding, dash records, style sharing, nine-patch
  geometry, and still 0 bytes allocated per frame. Demo: `urp/CanvasPaintDemo`, all effects in 1 draw call.

*2c: paths* ✅
- `canvas.path()`: `moveTo/lineTo/quadTo/cubicTo/arc/close`, then `fill`/`fillLinear`/`fillRadial` and/or
  `stroke(width, color)` with `join(MITER|BEVEL|ROUND)`, `miterLimit`, `cap(BUTT|SQUARE|ROUND)`.
- Curves are flattened in path space to a quarter-pixel tolerance, using Wang's formula for Béziers and chord error
  for arcs.
- Fills: ear-clipping triangulation (convex or concave, either winding) plus a one-pixel anti-aliasing fringe
  (coverage interpolated across `PATH_FILL` quads) on every edge, with wedges at convex corners. Gradients work in
  path coordinates.
- Strokes: one `PATH_STROKE` quad per segment, with a distance field across the line so both long edges are
  anti-aliased at any width. Miter and bevel joins are triangles plus fringe; round joins and caps are SDF circles.
- Draw-list change: shape commands now store per-corner uv and local coordinates, so arbitrary geometry
  (triangles = quads with a repeated corner) uses the same batching path.
- Limitations (documented in the API): no holes or self-intersections in fills (sub-paths fill independently);
  overlapping parts of *translucent* strokes (inner side of joins, caps) blend twice; butt ends of open strokes are
  not anti-aliased along the line direction.
- Tests: triangulation area and coverage (convex, concave, star, collinear, windings), flattening error bound,
  fringe coverage, join/cap geometry, miter-limit fallback, closed strokes, path gradients, and zero allocations.
  Demo: `urp/CanvasPathDemo` (1 draw call).

*2d: MTSDF text* ✅
- `SdfFont` (atlas texture + glyph metrics + kerning, em units, allocation-free lookups) loaded with
  `content().load(path, SdfFont.class, settings)`:
  - `.ttf`/`.otf`: generated with msdfgen (`lwjgl-msdfgen` + `lwjgl-freetype`). Shapes are normalized and edge-colored
    (msdfgen's simple coloring), each glyph cell is padded by half the distance range, glyphs are rendered in
    parallel and shelf-packed into a power-of-two atlas. Vertical metrics and kerning come from stb_truetype, which
    also reads GPOS pair kerning (FreeType's `FT_Get_Kerning` only reads the old `kern` table).
    `SdfFontSettings`: glyph size (48 px per em), distance range (8 px), characters (ASCII + Latin-1 + "…" + "�"),
    optional cache directory.
  - `.json` + `.png`: pre-baked msdf-atlas-gen atlases (either y origin, any `emSize`; MTSDF, MSDF or SDF). The
    disk cache uses the same format (`SdfFontData.toJson()`), so atlases generated on desktop can be shipped to
    platforms without msdfgen.
- `canvas.text(font, string, x, y)`: `size`, `align` (also the x anchor), `baseline` (top / middle / baseline /
  bottom), `maxWidth`, `wrap`, `maxLines`, `ellipsis`, `letterSpacing`, `lineSpacing`, `weight`, plus the shared
  `fill`/`fillLinear`/`fillRadial`/`stroke`/`strokeAlign`/`shadow`/`depth`. `canvas.text(layout, x, y)` draws a block
  laid out beforehand (GUI measuring).
- Shader (`GLYPH` shape type): fill and the inner edge of outlines use the MSDF median (sharp corners); the outer
  edge of outlines and shadows/glows use the true distance in the alpha channel. Anti-aliasing uses the screen
  scale from derivatives, clamped so at least one pixel of range is used (msdfgen's recommendation), so text stays
  one pixel soft under any zoom, rotation or pixel ratio.
- Glyphs are drawn in layers: all shadows, then all outlines, then all fills. A thick outline never covers the
  neighbouring glyph, which per-glyph compositing gets wrong on tight pairs like "AV". The outline and fill layers
  compute the same fill + outline, and each pixel belongs to one of them (fill layer: pixels the glyph covers;
  outline layer: pixels outside it), so nothing blends twice.
- Fill + stroke compositing (all shapes, not only text) treats the interior, the stroke over the shape and the
  stroke outside the shape as disjoint regions whose coverages add up. Before, "stroke over fill" left the edge
  pixel between an outside stroke and the fill only 75% opaque: a faint ring of background.
- Effects can only reach as far as the encoded range (half of `(distanceRange - 1) × size / glyphSize` beyond the
  edge; the half atlas pixel covers the inset quads of msdf-atlas-gen atlases. 3.5 px at 48 px with the defaults). Weight, outline and shadow are clamped to it, with a one-time warning;
  fonts loaded with a larger range allow wider effects.
- `TextLayout`: kerning, explicit and automatic line breaks (after spaces; long words break between characters),
  trailing spaces ignored, tabs as four spaces, alignment, letter and line spacing, maximum lines, ellipsis ("…" or
  "..."), missing characters drawn as "�" or "?". Zero allocations once warmed up.
- Tests: layout (kerning, wrapping, word breaks including a moved word that still overflows, trailing newlines
  with line limits, alignment, max lines, ellipsis with and without wrapping, tabs,
  fallbacks, surrogates, zero allocations), JSON parsing of msdf-atlas-gen output and round trips, packing (no
  overlaps, spacing, size limit), canvas text (quads, uv/local/params, layer order, shadow offset, clamping,
  anchors, prepared layouts under transforms, zero allocations).
- Demo: `urp/CanvasTextDemo`: sizes 8 to 220 px, outline, glow, drop shadow, weight, inner outline, gradients,
  wrapping, ellipsis, rotation and zoom, on a Retina display. The whole frame is **1 draw call**.
- Limitations: no complex shaping (ligatures, right-to-left), no hinting (text below ~10 px is soft; bitmap fonts
  stay available for pixel-art UI), no runtime generation on Android yet (pre-baked atlases are the path there;
  the Android importer comes with the Android backend work).

*2e: NanoVG demos ported* ✅
- `render/PrimitivesRenderDemo`, `render/IsometricGridDemo`, `concept/physics/CelestialGravitationGame` and
  `concept/roadcar/RoadCarGame` draw with `Canvas` through the camera's view matrix; no demo uses NanoVG any more.
- The isometric grid is drawn as 84 lines along the two axes instead of 1,681 outlined diamonds: 84 primitives in 1
  draw call (the per-tile path version was 33,620 primitives in 3 draw calls and 7 MB of vertices per frame). Every
  other port is 1 draw call.
- RoadCar's virtual resolution is now its window size (800x600): NanoVG drew in window units, so the half-size
  virtual resolution only showed part of the road. CelestialGravitation no longer prints on every update.
- Retina (2x framebuffer): clipping (the scrolling panel of `urp/CanvasShapesDemo`) and text from 8 to 220 px
  (`urp/CanvasTextDemo`) checked on screenshots at full framebuffer resolution: clip edges land on the right
  framebuffer pixels and glyph edges stay one pixel soft.

**Legacy cleanup** ✅ (done after 2e instead of in Phase 4: nothing used the old code any more)
- **Removed:** NanoVG (`NvgRenderEngine` and the `lwjgl-nanovg` dependency); `RenderEngine`, `StatefulRenderEngine`,
  `RenderEngine2D`, `DrawBatch`, `PostProcessor`; the classic `GLSpriteBatch` with its program and buffer classes
  (`GLMultiTextureShader`, `GLTextureShader`, `GLPrimitiveShader`, `GLRenderBufferShader`, `GLVertexArrayObject`,
  `GLVertexBufferObject`, `GLElementBufferObject`) and their GLSL; the five `GL*EffectShader` classes (border,
  noise, overlay, pixeler, sepia; none had a user); `GLTextureExporter`, `GLPrimitiveType`; the `Shader` interface
  and `SpriteBatch.getShader()`. Outside graphics: `DataArray` (deprecated for removal), `DataSerializer` (Java
  serialization helper) with its demo, `KeyboardState.downKeys()` and the non-owning `ALSound(int)` constructor.
- The `renderParity` check is gone with the classic batch it compared against; it did its job as the Phase 1
  gate. Its successor is golden-image render tests on an offscreen `RenderTarget` of fixed size (Phase 3), which
  also makes them independent of the display's pixel ratio. `renderBenchmark` now measures the draw list only.
- Android's own `GLES30SpriteBatch` was replaced right after, by the shared backend (next section).

**Platform base: Android** ✅ (before the pipeline, so Phase 3 is built once for every platform)
- *Build (M1):* the Android modules are opt-in with `pixel.android=true` (`-P`, `gradle.properties`,
  `local.properties` or `PIXEL_ANDROID`); off by default, so an installed SDK doesn't affect desktop builds, and
  enabling it without an SDK is an error instead of a silent skip. Android Gradle Plugin 9.5.1, compile and target
  SDK 36, min SDK 30; versions live in `gradle.properties`. The demo is plain Java (no Kotlin or AndroidX).
  `androidRun` builds, installs and starts the demo on a device or emulator, prints the app's warnings and errors,
  and can save a screenshot.
- *Shared renderer (M2):* new module `platform/opengl`. `GLApi` holds the ~45 GL functions the renderer uses (the
  same in OpenGL 3.3 and OpenGL ES 3.0, constants included) and `shaderPreamble()`. `GLRenderBackend`, the sprite
  and shape programs, `GLProgram` (replaces `GLShader`), `GLTexture` and the GLSL moved there; shader sources load
  through the class loader on both platforms. `LwjglGLApi` (desktop) uploads by address; `AndroidGLApi` moves the
  buffer position instead; neither allocates per call.
- Android: `GLES30GraphicsDevice` creates the backend with the GL surface, `createSpriteBatch()` returns the
  draw-list batch, `readPixels()` works. The texture importer keeps straight alpha (Android decodes premultiplied
  by default, which darkened edges) and has the desktop defaults and settings. Removed: `GLES30SpriteBatch`,
  `GLES30Shader`, `GLES30TextureShader`, the GLES buffer/VAO wrappers, `GLES30Texture` and their shaders.
- Robustness (from review): 16-bit images (decoded by Android as RGBA_F16) are converted to RGBA8; a recreated GL
  context rebuilds the backend's GL objects in place (`contextRecreated()`, so batches keep working) and logs that
  older textures are invalid; the demo activity disposes the game in `onDestroy` and handles density, dark mode
  and resize itself; `androidRun` checks every adb call, needs one device (or `ANDROID_SERIAL`) and keeps adb's
  errors out of screenshots.
- Tests: `GLRenderBackendTest` runs the backend against a recording `GLApi` (no GPU): shader preamble and
  templates, single upload and indexed draw, chunking beyond 16-bit indices, clip/style table ranges, stats,
  context recreation, and **0 bytes allocated** for 20 frames of 20,000 sprites plus 500 shapes.
- Verified: desktop Canvas demos render as before (differences only in their animated regions), and the Android
  demo (sprites, Canvas shapes, gradients, shadow, path) runs on the API 36.1 emulator with no errors.
- Verified on the emulator: pause and resume keeps rendering (the context is preserved).
- Not done yet: reloading textures after a lost context (lifecycle work), touch input, audio, text on Android
  (pre-baked atlas importer).

**Content children** ✅
- `content().child()` / `child("level-1")` gives assets a shorter lifetime than the game (a level, a screen). A
  child looks assets up in its own cache and then its ancestors' (shared assets are never loaded twice), loads
  anything else into its own cache (dependencies an importer loads through it too), and frees only those:
  `unload()` keeps it usable for the next level, `dispose()` ends it. Children are unloaded or disposed before their
  parent, since they may use its assets. Importers and the data pipeline are inherited, with local overrides; a
  child never disposes importers it inherited. Uncached loads stay owned by the caller.
- Shared assets belong in the parent: lookups only go upwards, so siblings never see each other's assets.
- `TexturePack` no longer disposes its texture: the texture is its own cache entry, owned by whichever manager
  loaded it (possibly an ancestor), so a level could otherwise free the HUD's atlas. Disposal is exception-safe
  (one failing asset is logged, the rest are still released) and safe against concurrent loads: `dispose()` is
  claimed atomically and an asset finishing after it is released at once. BlueGem's title scene uses its own
  child instead of disposing the game's content manager.
- Tests: `ContentManagerChildTest` (sharing, ownership, dependency ownership including texture packs, unload,
  cascading disposal, failing disposals, disposal during a load, importer and pipeline inheritance, uncached loads,
  names). Demo: `learning/content/ContentLevelsDemo`.

**Phase 3.0: game model (prerequisite of the pipeline)** ✅
- **Blueprint removed.** It was reflection-based DI with classpath scanning at startup (fragile in jars, needs
  `-parameters`, and core depended on it). Gone: `modules/blueprint`, the `blueprintPackages` setting, the hook in
  `GameContainer`, and the two blueprint learning demos. BlueGem and SpaceShooter are ported to explicit wiring,
  which also fixed SpaceShooter's event lookups that could never have found the event bus.
- **`ServiceProvider` removed** (with `ServiceFactory`, `ServiceRegistrar` and every `*ServiceFactory`). It was a
  global static registry, and every `get()` built a new instance (each `ContentManager` had its own cache).
- **Platform-free games**: games extend `org.pixel.core.GameApp` and are run by a platform launcher:
  `DesktopLauncher.run(app, settings)` or `AndroidLauncher.create(app, settings, context)`. The host
  (`DesktopGameHost`, `AndroidGameHost`, both extending `GameContainer`) owns the platform and implements
  `GameContext`.
- **`GameContext`** (per game, no statics): `graphics()` (the `GraphicsDevice`: render backend, stats,
  `createSpriteBatch()`), `content()` (one shared `ContentManager`, disposed by the host), `audio()`, `window()`
  (`GameWindow`: size, pixel ratio, focus, title), `settings()`, `exit()`. `GameApp` has shortcuts for each,
  available from `load()` on. Desktop-only features: `DesktopWindowManager.of(context())`.
- **Disposal order** owned by the host: game → content → graphics device → audio → window, and the game's
  `dispose()` only runs if its `load()` ran.
- **ext-network**: `Network.createServer(settings)` / `Network.createClient(settings)` replace the service
  registrar.
- **Android**: `AndroidGameHost`, `AndroidWindow` and `AndroidLauncher`. Assets load through the host's context
  (`MobileResourceLoader(context)`, `GLES30TextureShader(context)`). The URP backend and audio report
  "not implemented on Android yet". *Written, not compiled* (Android is not part of the default build).
- Verified: full build and tests; every learning and concept demo runs on the new host; parity and benchmark are
  unchanged. (`ImGuiDemo` cannot start on Apple Silicon because `imgui-java` 1.86.2 ships x86_64 macOS natives
  only. That is not caused by this change.)

**Phase 3: pipeline** (split into reviewable slices)

*3a: pipeline core* ✅
- `Layer` (`org.pixel.graphics.draw`): immutable, `Layer.sorted(name)` / `Layer.ordered(name)`, optional
  `.blend(mode)`. `View` (`org.pixel.graphics.pass`): the view matrix plus framebuffer pixels per unit; `Camera`
  extends it, so every `Camera2D` is a view (pixels per unit = display density × zoom), and `ScreenSpace(context)`
  is the virtual-resolution view for HUDs.
- `RenderPipeline.builder(backend).pass(view, layers...).custom(callback).build()`; `pipeline.canvas(layer)`
  records, `pipeline.render()` runs the passes in order and clears the layers (canvases are reset). Each layer is
  sorted once per frame and batched once per pass that draws it (a layer can be in several passes, e.g. a minimap),
  with its own blend mode. Commands whose quad (margins, strokes, shadows included) misses the view are culled; the
  bounds come from the inverse view matrix, so any affine view works.
- Canvases take their pixel scale from their views, which fixes the soft-edge cut-off under camera zoom noted in
  2a: quad margins, path fringes (one framebuffer pixel, also on Retina) and curve flattening (a quarter pixel) all
  follow it. A camera's pixels per unit come from the window's framebuffer over the area it shows (camera size and
  zoom, tighter axis), so cameras in metres work too; cameras built without a context assume one pixel per unit.
  When several passes draw a layer, margins follow the coarsest view (edges never cut) and fringes and curves the
  finest (the sharpest view stays sharp).
- Recording is only allowed outside `render()` (`canvas()` throws while rendering and after `dispose()`).
- `CanvasBatch` is removed; every demo and test records through a pipeline.
- Also fixed: text commands copy their characters (a reused `StringBuilder` drew its final contents for every
  pending line), and `ADDITIVE` blends with `(SRC_ALPHA, ONE)`, so fades and anti-aliased edges work in additive
  layers (it was `(ONE, ONE)`, which ignored alpha).
- Tests: `RenderPipelineTest` (pass and layer order, blend per layer, view per pass, empty and culled layers, culling
  at edges and margins, one layer in two passes, anti-aliasing per view scale, reset between frames, custom passes,
  misuse, zero allocations across three passes); canvas and backend tests run through the pipeline.
- Demo: `urp/PipelineDemo`: a 4000 x 3000 y-sorted world through a panning, zooming camera, additive glow and a
  screen-space HUD in 3 draw calls at normal zoom (3812 commands recorded, ~460 quads drawn after culling).

*3b: render targets and post effects* ✅
- `RenderTarget` (backend SPI): an FBO with an RGBA8 texture that draws like any image (minimaps, portals). Contents
  are stored top row first (the backend flips clip-space y for passes into targets) and **premultiplied**.
- Premultiplied output everywhere: the sprite and shape shaders premultiply straight textures (a per-batch slot
  bitmask says which textures already are, e.g. targets) and write premultiplied colors; glyph atlases are sampled
  raw so the distance field stays intact. NORMAL is `ONE, ONE_MINUS_SRC_ALPHA`; ADDITIVE is pure emission (adds
  color, leaves alpha), so light drawn into a transparent target still shows when the target is composited;
  MULTIPLY keeps the destination alpha. Fragments are discarded only when they carry neither coverage nor light.
- Pipeline: `builder.pass(view, target, layers...)`, `builder.screenTarget(scale)` (follows the screen size, owned
  by the pipeline), `builder.target(w, h)`, `builder.post(source[, destination], effects...)`. A target is cleared
  to its clear color the first time a pass draws into it in a frame (also when nothing is drawn). Passes into
  targets measure pixels per unit against the target, so a 320 px minimap of a 2400-unit world gets correct edges.
- Post effects: `PostEffect` (a chain step), `ShaderEffect` (one GLSL function `vec4 effect(vec2 uv)` with named
  uniforms; the same source on GL 3.3 and GLES 3.0), and `Effects`: sepia, tint (was "overlay"), pixelate (was
  "pixeler"), noise (film grain), border, vignette, and bloom (bright pass, two separable 9-tap Gaussian blurs at
  half resolution, combine; light is added without coverage). Effects have setters, so they animate without
  rebuilding the pipeline. Each post pass has its own `PostContext` with scratch targets (chains ping-pong between
  two; multi-pass effects ask for temporaries by slot), so passes of different sizes do not reallocate every
  frame. User effects chain steps through `PostContext.apply(effect, source, destination, blend)`. The last step
  composites over the screen (premultiplied), or replaces a destination target's contents. Custom passes manage
  their own clearing. The backend draws them as one full-screen
  triangle from `gl_VertexID` (no vertex buffers).
- Devices set their clear color in `clear()` (target clears change the GL clear color).
- Golden-image check (successor of the removed parity check): `./gradlew :pixel-demo-desktop-learning:renderGolden`
  renders 9 scenes (shapes, paints, paths, text, images, blend modes, an effect chain, bloom, and transparency: a
  transparent target with normal and additive layers composited twice over a checkerboard) into 480 x 320 targets
  through context-free cameras (independent of the display's pixel ratio), compares them with `golden/*.png`
  (6/255 per channel, at most 0.2% of pixels beyond) and fails the build step on a mismatch, writing diffs to
  `build/golden/`. `-Dgolden.record=true` re-records after an intended change.
- Tests: pipeline routing (clear once per frame, clears without content, effect chains into screen vs targets,
  screen target resizing, target pixel scale, bloom scratch targets, ownership on dispose, zero allocations with
  targets, a minimap and a bloom + vignette + sepia chain) and backend framebuffer handling (bind, viewport,
  clear, restore, post draw destination, read back) against the fake GL.
- Demo: `urp/PostEffectsDemo`: a neon world into a screen target, bloom and vignette, keys 1-5 for sepia, pixelate,
  grain, tint and border, a crisp HUD on top and a live minimap from the same world layer. Android: the demo's
  canvas goes through a screen target and a vignette.
- Context loss: the backend counts context generations (`getContextGeneration`); targets reallocate their GL objects
  on first use in a new context (without deleting the dead names), and effect programs recompile per backend and
  generation. Screen targets start at the screen size, so the first frame already has the right pixel scale.
- Not yet: image textures are not reloaded after a lost context (lifecycle work).

*3c: materials* ✅
- `Material.effect(source)`: an `effect()` function compiled into a variant of the shape program
  (`#define PIXEL_MATERIAL`, the function inserted before `main`, which applies it to the final premultiplied color).
  `Material.raw(vertex, fragment)`: complete shaders for the shape vertex layout; the fragment shader gets
  `uTextures`, `uPremultiplied` and `pixelSample(slot, uv)`, and the uniform blocks are bound if declared. Both run
  on GL 3.3 and GLES 3.0.
- Values: named float uniforms (1, 2 or 4 components) and up to 4 named textures per material, uploaded only when
  they changed (a values serial). Per-command material data: a vec4 in the style record, which grew to 8 vec4 (128
  styles = 16 KB, the GLES 3.0 minimum block size).
- Canvas: `.material(m)` and `.material(m, x[, y, z, w])` on every command; the draw list keeps a material per
  command. Batches break on a material change; material batches have `maxTextureSlots - 4` command slots.
- `Layer.sorted(name).batchByMaterial()` (`LayerMode.SORTED_BY_MATERIAL`): the radix sort now carries a payload
  index array, so keys can use all 64 bits. A row of alternating grass and water tiles: 100 batches to 2.
- Backend: a program state per program (matrix uploaded once per pass that uses it, premultiplied mask); a material
  compiles through `RenderBackend.compileMaterial` on first use, again after `setSource` or a lost context.
- Hot reload: `setSource` recompiles on next use; a source that fails keeps the previous program running and is
  reported by `getError()` (logged once); if the context is lost while an edit is broken, the last source that
  compiled is rebuilt. `SourceWatcher` polls a file (four times a second, no threads; time and size count as changes)
  and feeds it to a material; contents the material rejects (an editor caught mid-save) are reported, not thrown.
  A material keeps one compiled program, so it is meant for one render backend.
- Images and nine-patches get local coordinates from their center, so `f.local` works for them like for shapes.
- Golden scene "materials": flash (data), dissolve (data, discard), a material texture mask, an effect over text and
  its outline, and a raw material moving vertices.
- Tests: material values, compiling, failed reloads and context loss; material data merged into styles; batch
  breaks and slot limits; grouping order; canvas material scope; fewer draw calls with grouping; zero allocations
  with materials, data and per-frame uniform changes; the GL backend binding material programs, uploading values
  only on change, material texture units, failed reloads and recreated contexts; the file watcher.
- Demo: `urp/MaterialsDemo`: animated water from `shaders/water.glsl` (edit it while the demo runs), tiles grouped
  by material (B compares with a plain sorted layer), hit flash and dissolve with per-command data, grass swaying
  through a raw material, an effect over the title text. Android: pulsing planets (data) and a raw material.

*3d: inspection* ✅
- Pick ids: `.pick(id)` on every canvas command (0 = not pickable); all quads of a command (path triangles, glyphs,
  nine-patch parts) carry it. The draw list stores one per command.
- `pipeline.lastFrame()` returns a `FrameInfo`, updated in place by every `render()`:
  - per pass: kind, target, draw calls, CPU time;
  - per (pass, layer): commands, culled commands, batches, quads, draw calls and batch breaks by reason;
  - the batch list in drawing order: layer, `BatchBreak` reason, material, quads, textures. Reasons: `FIRST`,
    `LAYOUT`, `MATERIAL`, `QUAD_LIMIT`, `TEXTURE_SLOTS`, `CLIP_TABLE`, `STYLE_TABLE`, recorded by the batcher.
  Draw calls come from `RenderBackend.getDrawCallCount()` (a running counter), so post and custom passes count too.
- Picking: `lastFrame().pick(layer, x, y)` (top-most id, in layer coordinates) and `pickAll` (top-most first). The
  pickable commands are copied before the layers are cleared, so the last frame stays queryable while the next one
  is recorded (the game picks what the player sees). Hit tests follow the shader: the two triangles the GPU draws,
  interpolated local and uv coordinates, the shape's distance function against its stroke's reach, path fill
  coverage, path stroke width, dash gaps; glyphs by their boxes (not their shadow), images by their quads; clips
  honoured. Picks are captured when a layer is prepared for drawing (again if a custom pass added commands before a
  later pass drew it), so they are what was drawn, and `render()` can't fail while cleaning up.
- `Material.setName` for readable statistics.
- Tests: break reasons, culling, batch list and reset between frames; draw calls of layer, post and custom passes;
  hits and misses on round corners, outside strokes, round caps, ellipses, a concave path, path strokes, text,
  rotated images, clipped shapes, dashed lines and ellipses, nine-patches; top-most order and `pickAll`; picks kept
  until the next frame; a custom pass recording into a drawn layer; a failing frame followed by a good one; a layer
  in two passes and a fully culled pass; zero allocations with stats and picks.
- Demo: `urp/InspectorDemo`: 60 props of every kind with pick ids; hover and click select with exact hits, the
  selection drawn in an overlay layer (an editor's gizmo layer); a panel with the last frame's statistics and batch
  breaks (the water material breaks y-sorted batches; M toggles it).

**Phase 4: merge readiness**
- Update docs and `AGENTS.md` (the legacy renderers are already removed).
- Rebase onto `devel`, then open the PR.

## 9. Editor-readiness constraints

These shape the design now so an editor can be built later without reworking the pipeline:

1. **No static state** in the pipeline, backend or front-ends. Everything is reached from the `GraphicsDevice`
   instance.
2. **Any output**: screen or `RenderTarget`, with several pipelines alive at once (scene view, game view, previews).
3. **Layers can be added from outside**, so an editor can append an `EDITOR_OVERLAY` pass (grid, gizmos, selection)
   to a game's pipeline.
4. **Pick ids**: any command can carry an optional `int` pick id. The last frame's commands stay queryable.
5. **Frame introspection**: the command, batch and break-reason lists of the last frame (the basis of a frame
   debugger).
6. **Hot reload**: materials and fonts can be recompiled or reloaded by handle without rebuilding the pipeline.

Done in Phase 3.0: the game is decoupled from the window loop (`GameApp` is hosted, so an editor can be another
host). Still required for an editor later: per-instance input (no static `Mouse`/`Keyboard`) and serializable scenes
with stable asset ids.

## 10. What happens to `task/urp`

`task/urp` is **not merged and not rebased**. It stays as a read-only reference. Specific low-level pieces are ported
by hand, with review:

| Ported (adapted) | Not carried over |
|---|---|
| `sdf_batch.vert/frag.glsl`: SDF shape and glyph math | `RenderPipeline` / `Renderable<R>` / `Renderer` registry |
| `SdfFont`/`SdfGlyph` API shape (reference). The glyph *generation* is replaced: stb single-channel SDF → msdfgen MTSDF | `DirectRenderer`, `SdfShapeRenderer`, `SdfTextRenderer`, `GLShapeRenderer`, `GLCanvasRenderer`, `GLSdfBatchRenderer` (rewritten as `GLBatchRenderer`) |
| `GLRenderTarget` | `FactoryProvider` and the factory re-plumbing (static, against §9) |
| `Canvas` API surface and `NinePatch` (as reference for the new `Canvas`) | `GameContainer`/`Game` API renames (`getVirtualWidth` → `getViewportWidth`) |
| `GLFastSpriteBatch` + `instanced_multitex.*` (benchmark reference only) | **All GUI work: `ext-flux`, `ext-rune`, `ext-weaver`, their desktop modules and demos** |
| | `CLAUDE.md`, `.github/copilot-instructions.md`, ext-decs renames, unrelated demo changes |

**GUI:** none of the three attempts carries over here. When the GUI branch starts (after v2 merges), it will be
designed on top of `Canvas`/`TextLayout`. From `task/urp` it will consider Weaver's CSS parser and `StyleEngine`
(which has tests), and Rune's measure/layout contract and input interface. That is a separate review.

## 11. Testing strategy

- **GL-free unit tests** (the bulk): draw list recording, sort keys and stability, batch merge and break rules, clip
  table transforms, canvas command generation, path triangulation, text layout and measurement, compat
  `SpriteBatch` → commands, pass routing. All of these run against a recording fake `RenderBackend`.
- **Allocation tests**: record a typical frame N times and assert zero bytes allocated after warm-up.
- **Visual checks** (manual, macOS + one other OS): learning demos per phase, Retina and non-Retina, resize.
- **Benchmark demo**: frame time and draw calls compared with the Phase 0 baseline.

## 12. Open questions (for review)

1. **Performance target.** Proposal: the Phase 1 gates in the baseline doc (no slower than today, ≤ 4 draw calls in
   the main scenarios), and ≤ 6 draw calls for the example frame in §6.1. The baseline does 20k sprites at ~158 fps
   on an M5 with 79 draw calls. Is there an absolute target you want (e.g. 100k sprites at 60 fps)?
2. **Rotated clipping.** Proposal: axis-aligned clips only in v2. A rotated clip falls back to the bounding rectangle
   with a logged warning, and a stencil can be added later. OK?
3. **Depth type.** Proposal: `float` depth (natural for y-sorting) in a 32-bit key slot. The alternative is `int`
   like today's `SpriteBatch`, and the compat path maps `int` → `float` either way.
4. **NanoVG removal.** Done: removed with the legacy cleanup after the 4 demos were ported.
5. **Bitmap fonts.** Proposal: keep them, for pixel-art and very small text (§4.9). OK?
7. **Font atlas defaults.** 48 px glyphs, 8 px distance range, ASCII + Latin-1 by default; other sets are opt-in per
   import. OK as defaults?
6. **Package names.** `draw` / `canvas` / `text` / `pass` / `backend` under `org.pixel.graphics`. Fine?
