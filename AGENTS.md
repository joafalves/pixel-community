# AGENTS.md

## Build & test commands

```bash
# Full build (all modules)
./gradlew build

# Run all tests
./gradlew test

# Run a single module's tests (modules are named pixel-* in Gradle)
./gradlew :pixel-math:test
./gradlew :pixel-ext-ecs:test

# Build a single module
./gradlew :pixel-core:build
```

No CI is configured (no GitHub Actions). All builds are local.

The wrapper is Gradle 9.8.2 and runs on any JDK 17+ (including 25). Bytecode always targets Java 17 via `options.release = 17`, so don't use newer Java APIs in library modules.

## Project structure

This is a Gradle 9 multi-module Java 17 game framework (LWJGL/OpenGL). Kotlin 1.9.25 is available but optional.

- **`modules/`** — 6 core library modules (commons, math, graphics, content, core, pipeline)
- **`platform/`** — `opengl` (the renderer shared by both platforms, behind `GLApi`), `desktop` (LWJGL3, GL 3.3) and `android` (OpenGL ES 3.0, experimental)
- **`extensions/`** — 6 optional extensions (ext-ecs, ext-log4j2, ext-network, ext-tween, ext-ecs-extra, ext-decs)
- **`demos/`** — example applications (excluded from release builds)
- **`tools/arkade`** — multiplayer game server (Netty-based)

Module names in `settings.gradle` use directory paths (e.g. `modules/math`) but are renamed to `pixel-{name}` at bootstrap. In Gradle commands, reference them as `:pixel-math:`, `:pixel-ext-ecs:`, etc.

Inter-module dependency graph: math has no dependencies; commons ← pipeline; commons + pipeline + math ← content ← graphics ← core; graphics + content ← platform/opengl ← platform/desktop, platform/android; extensions build on core.

## Rendering (URP v2)

One renderer for everything; docs: `docs/rendering.md` (user guide), `docs/design/urp-v2.md` (design and phase notes).

- **Layers**: `Canvas` / `SpriteBatch` record into a `DrawList` (struct-of-arrays, `modules/graphics/.../draw`); the `Batcher` turns it into `RenderData` (vertex streams + `BatchList`); a `RenderBackend` (SPI in `graphics/backend`) draws it. `RenderPipeline` (`graphics/pass`) orders passes, targets, post effects. Everything above the backend is GL-free and unit-tested with `RecordingBackend` (graphics test sources); the GL backend (`platform/opengl/.../render/GLRenderBackend`) is tested against `FakeGLApi`.
- **Shaders**: `platform/opengl/src/main/resources/engine/shader/opengl/urp/*.glsl`, written once for GL 3.3 and GLES 3.0 (the platform's `GLApi.shaderPreamble()` adds `#version`). Sampler arrays are indexed through a generated `switch` (constant indices only).
- **Colors are premultiplied** everywhere: shader outputs, render targets, imported textures (`TextureImporterSettings.premultiplyAlpha`, off only for data textures such as distance-field atlases).
- **Lost GL contexts** (Android): GL objects record `GLApi.contextGeneration()` when they are created and never delete names from an older generation (the new context reuses them); render targets and materials rebuild themselves, imported textures don't yet.
- **Invariants, enforced by tests**: steady-state frames allocate nothing (tests measure with `ThreadMXBean`, taking the minimum over several windows because the JIT may allocate once); no synchronous GL query per command or pass (the backend reads the screen's viewport and framebuffer once per frame; `FakeGLApi.queries` counts them); `Batcher` never reorders (ordering is the `DrawList`'s job).
- **Visual checks**: `./gradlew :pixel-demo-desktop-learning:renderGolden` renders fixed scenes offscreen and compares them with `demos/desktop/demo-desktop-learning/golden/*.png`; add `-Dgolden.record=true` only after an intended visual change, and look at `build/golden/` first. `renderBenchmark` measures sprite/text throughput (baseline in `docs/design/urp-v2-baseline.md`).
- **Demos** (`demos/desktop/demo-desktop-learning`, `org.pixel.demo.learning.urp.*`) take `-Dpixel.screenshot=<file.png>` to render a few frames, save a PNG and exit — use it to look at a change. URP demos open the dev tools at start (`DemoGame.withDevTools`).
- **Space Shooter** (`demos/desktop/demo-desktop-concept`, `org.pixel.demo.concept.spaceshooter`): a complete small game on URP, laid out like a real project (see its `package-info.java`); its simulation (`world`, `wave`) is unit tested and allocation-free. Run with `./gradlew :pixel-demo-desktop-concept:runSpaceShooter`; for hands-free checks add `-Dspaceshooter.autoplay=true -Dspaceshooter.capture=<dir> -Dspaceshooter.captureAt=3,10` (screenshots, then exit; `-Dspaceshooter.wave=5` starts at the boss).

## Dev tools

`context().devTools()` (`modules/core/.../core/dev`): an in-game overlay owned by the game host (F1 on desktop, a three-finger tap on Android), drawn after the game in its own pipeline with a bundled font. Available in dev mode (`GameSettings.setDevMode`, `setDevToolsMode`). Games add watches, toggles, sliders and actions, and `track(pipeline, name)`. The font (`modules/core/src/main/resources/engine/font/`, JetBrains Mono, OFL) is baked by `./gradlew :pixel-demo-desktop-learning:bakeEngineFont`; the output is committed. Guide: `docs/dev-tools.md`.

## Key conventions

- **Lombok** (1.18.42): Heavily used. `@Getter`, `@Setter`, `@Builder`, `@Data`, `@AllArgsConstructor`, `@NoArgsConstructor`, `@RequiredArgsConstructor` are common. Lombok is configured globally in the root `build.gradle` for every Java project — do not add it per module.
- **JUnit 5** (via the JUnit BOM, plus `junit-platform-launcher`) with Mockito for tests. Test classes are named `*Test.java`. No JaCoCo or static analysis (no Checkstyle, SpotBugs, PMD).
- **Lifecycle interfaces**: `Initializable`, `Loadable`, `Updatable`, `Drawable`, `Disposable` — not standard lifecycle names.
- **Package base**: `org.pixel.*`
- **Version**: Defaults to `DEV-{git-short-hash}`. Release version is set via `-DPIXEL_RELEASE="1.0.0"`.
- **Shared constants are read-only**: `Vector2.ZERO/ONE/...` and `Color.WHITE/...` throw on mutation. Copy them (`new Vector2(Vector2.ZERO)`, `Vector2.zero()`, `new Color(Color.WHITE)`) when a mutable value is needed.
- **Game model**: games extend `GameApp` (core, platform-free) and are started by a platform launcher (`DesktopLauncher.run(new MyGame(), settings)`, `AndroidLauncher.create(...)`). Services come from the per-game context (`graphics()`, `content()`, `audio()`, `window()`, `settings()`), available from `load()` on. There is no global service registry: pass dependencies explicitly. Objects from `graphics().createSpriteBatch()` are owned (and disposed) by the caller; `content()`/`audio()` are owned by the host.
- **Quitting a game**: call `exit()` (requests close; the loop ends and `dispose()` runs once with the GL context still alive). Never tear down GLFW/GL from inside callbacks.
- **macOS only**: LWJGL requires `-XstartOnFirstThread`. This is auto-configured in `build.gradle` for `application` plugin projects and in `.vscode/launch.json`, but external run configurations outside Gradle may need it manually.

## Publishing

Publishing is manual via Sonatype OSSRH. Credentials in `~/.gradle/gradle.properties`. See `PUBLISH.md` for full steps. Key command:

```bash
./gradlew clean publish -DDESKTOP_BUILD_TARGET_PLATFORM=all -DPIXEL_RELEASE="1.0.0"
```

Only modules with `ext.mavenPublish = true` in their `build.gradle` are published.

## Android (experimental)

The Android modules and demo are off by default, even when an SDK is installed. Enable them with `pixel.android=true` as a Gradle property (`-Ppixel.android=true`), in `local.properties`, or with the `PIXEL_ANDROID` environment variable; the SDK comes from `sdk.dir` in `local.properties` or `ANDROID_HOME`, and the build fails if Android is enabled without one.

```bash
# build, install and start the demo on the only connected device/emulator (ANDROID_SERIAL picks one)
./gradlew -Ppixel.android=true :pixel-demo-android-learning:androidRun -Pscreenshot=build/android.png
```

When testing on an emulator, start it with a window (not headless), and force-stop the app (`adb shell am force-stop org.pixel.demo.android`) and close the emulator when done: a running demo keeps the host CPU busy. Fonts can't be generated on Android (msdfgen is desktop-only): bake them with `SdfFontBaker` and load the `.json` + `.png` (`PrebakedSdfFontImporter`). Audio and game touch input are not implemented on Android yet.
