# URP v2: Phase 0 baseline

The renderer as it is today (`GLSpriteBatch`), measured with the render benchmark that every later phase is compared
against.

## How to run

```bash
./gradlew :pixel-demo-desktop-learning:renderBenchmark
```

It opens a 1280×720 window for about a minute: vsync off, uncapped frame rate, no idle throttling. Each scenario
runs a 2 s warm-up and then 8 s of measurement. A summary is printed, and a CSV is written to
`demos/desktop/demo-desktop-learning/build/benchmarks/`.
Options: `-Dbench.scale=1.0` (object counts), `-Dbench.warmup=2`, `-Dbench.measure=8`, `-Dbench.only=<scenario>`.

The scenarios only use the public `SpriteBatch` API, so the same benchmark measures the draw-list implementation in
Phase 1 without any changes.

| Scenario | Workload |
|---|---|
| `sprites-1-texture` | 20k moving sprites, 1 texture |
| `sprites-32-textures` | 20k sprites, 32 textures in random order (texture-slot pressure) |
| `sprites-depth-sorted` | 20k sprites, 4 textures, 10 random depth levels |
| `sprites-rotated` | 20k rotating sprites, 4 textures |
| `text` | 400 lines of bitmap text (≈ 13.9k glyphs) |
| `interleaved-sprite-text` | 2k × (sprite, label) in submission order; the case `task/urp` handled worst |

Columns: `frame ms` is wall time between frames (includes swap). `cpu ms` is the CPU time of `draw()` including
`end()`. `gpu ms` is the GPU time of the same section (`GL_TIME_ELAPSED` queries, read a few frames late).
`draws/f`, `prims/f`, `binds/f` and `upload KB/f` are draw calls, primitives, texture binds and vertex data uploaded
per frame. These come from the **graphics device's** `RenderStats` (`getGraphicsDevice().getStats().lastFrame()`),
which counts what was actually submitted to the GPU no matter which front-end recorded it. The Phase 1 backend
records into the same counters, so the benchmark compares both renderers without changes.

## Results: 2026-11-06

`GLSpriteBatch` (default buffer, 256 sprites) · Apple M5 · macOS (aarch64) · LWJGL 3.4.3 · Java 25.0.5

| scenario | objects | fps | frame ms | p95 ms | p99 ms | cpu ms | gpu ms | draws/f | prims/f | binds/f | upload KB/f |
|---|---:|---:|---:|---:|---:|---:|---:|---:|---:|---:|---:|
| sprites-1-texture | 20000 | 165.1 | 6.057 | 8.285 | 9.212 | 1.125 | 3.883 | 79 | 20000 | 1 | 4218.8 |
| sprites-32-textures | 20000 | 85.4 | 11.710 | 14.007 | 14.641 | 4.402 | 6.044 | **951** | 20000 | **14031** | 4218.8 |
| sprites-depth-sorted | 20000 | 120.5 | 8.298 | 8.853 | 10.001 | 1.901 | 4.707 | 79 | 20000 | 4 | 4218.8 |
| sprites-rotated | 20000 | 119.6 | 8.359 | 9.121 | 10.208 | 2.378 | 4.873 | 79 | 20000 | 4 | 4218.8 |
| text | 400 | 508.8 | 1.966 | 3.731 | 4.288 | 0.836 | 0.583 | 55 | 13890 | 1 | 2929.9 |
| interleaved-sprite-text | 4000 | 407.7 | 2.453 | 4.124 | 5.143 | 0.682 | 1.185 | 43 | 10890 | 5 | 2297.1 |

Run-to-run variance on this machine is roughly ±5–10% on fps and CPU time (compare with an earlier run: 158 vs 165 fps
on `sprites-1-texture`, 391 vs 509 fps on `text`). Gates use CPU/GPU time with a tolerance for that reason.

## Findings

1. **The batch size is the main cost.** With one texture, 20k sprites still take 79 draw calls, because the default
   buffer holds 256 sprites. Every flush also re-uploads its vertices (`glBufferData`, 6 non-indexed vertices ×
   36 B per sprite). The GPU spends about 3.9 ms on 20k small quads, which points to per-draw and upload overhead
   rather than fill rate. This is a hypothesis: macOS implements OpenGL on top of Metal on Apple Silicon, which
   likely makes per-draw costs higher, but it hasn't been profiled.
2. **Texture-slot overflow is very expensive.** With 32 textures in random order, draw calls jump to **951** per
   frame, texture binds to **14,031**, and CPU time is about 4× higher. The batch can only bind 16 textures, so it flushes whenever the run of sprites
   needs a 17th. Keeping submission order (which `SpriteBatch` semantics require) cannot avoid this. v2 helps in two
   other ways: bigger batches make each break cheaper, and the opt-in `batchByMaterial()` layer mode (design §4.1)
   groups by texture when same-depth overlap order doesn't matter. Texture atlases remain the real fix on the game
   side.
3. **Bug: depth sorting is only correct within 256 sprites.** `GLSpriteBatch` sorts by depth only inside each full
   buffer before drawing it. With more than 256 sprites per `begin`/`end`, a later chunk is always drawn on top of
   an earlier one, whatever its depth. v2 fixes this by design: a sorted layer is sorted as a whole at `render()`.
   **The Phase 1 parity check must compare against the *correct* order, not the current output.**
4. **Frame time is much larger than CPU + GPU time of `draw()`.** For example, 6.3 ms per frame against 1.2 ms CPU
   and 3.9 ms GPU. The remainder (scenario update, buffer swap, driver work) is not broken down by the benchmark.
   Comparisons between phases should therefore look at `cpu ms`, `gpu ms` and `draws/f` first, and fps second.

5. **Uploads are large: 216 B per sprite** (6 vertices × 36 B), so 4.2 MB per frame for 20k sprites. The
   v2 vertex format proposed in design §5.2 (52 B × 4 vertices = 208 B per quad) would **not** reduce this. Phase 1
   must use a compact sprite vertex format (see design §5.2).

## Phase 1 gates (derived from this baseline)

For every scenario, measured on the same machine:
- **Throughput:** `cpu ms` and `gpu ms` are no worse than this baseline (10% noise tolerance, compared on the same
  machine within the same session).
- **Upload size:** `upload KB/f` for the sprite scenarios is at most half of the baseline (≤ 2,110 KB for 20k sprites).
- **Draw calls (through the `SpriteBatch` compat path, which keeps submission order):** `sprites-1-texture`,
  `sprites-depth-sorted`, `sprites-rotated`, `text` and `interleaved-sprite-text` ≤ 4 per frame.
  `sprites-32-textures` no more than baseline (951), because order-preserving batching can't avoid slot overflow on a
  random 32-texture sequence. A `batchByMaterial()` variant of that scenario, added once layers exist (Phase 3),
  must reach ≤ 4.
- **Correctness:** `sprites-depth-sorted` is globally depth-ordered (checked by a GL-free unit test on the sorted
  command order, since the benchmark itself does not check pixels).
