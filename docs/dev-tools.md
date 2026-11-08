# Dev tools

Pixel has built-in, in-game dev tools: a window over the game with live performance and rendering statistics, the
loaded assets, recent log messages, and the game's own values and switches. They are drawn by the engine after the
game (unaffected by its cameras and effects), with a bundled font, on desktop and Android.

## Showing them

| | |
|---|---|
| Desktop | **F1** shows or hides them |
| Android | a **three-finger tap** (see Input below) |
| In code | `devTools().setVisible(true)`, `toggleVisible()`, `showTab(DevTools.Tab.RENDERING)` |

They are available when the game runs in dev mode, and can be forced on or off:

```java
settings.setDevMode(true);                         // available (DevToolsMode.DEV_MODE, the default)
settings.setDevToolsMode(DevToolsMode.ON);         // always available (e.g. a test build)
settings.setDevToolsMode(DevToolsMode.OFF);        // never: no overlay, no log capture, no cost
settings.setDevToolsVisible(true);                 // start visible
```

The window starts in the top-right corner at 380 × 440. Drag it by its title; resize it from any edge or corner (the
edge under the pointer lights up); the arrow collapses it to an FPS badge. Tabs never change its size: long content
scrolls (mouse wheel or the scroll bar). Numbers refresh four times a second, averaged over that time, so they can be
read; the panels' text is only rebuilt then.

While hidden they cost a few timers per frame (and copy log messages); nothing is loaded and nothing is allocated. If
anything inside them fails, they log the error and turn off; a watch or action that throws shows "error" in its row
instead. The game keeps running either way.

## Tabs

- **Perf**: FPS and its last minute (min, average, max, and a graph), frame times (the last 180 frames, scaled to
  what happens: update and draw time inside each frame interval, with 60 and 30 FPS lines), the dev tools' own cost,
  heap use, and garbage collections (desktop).
- **Render**: the device's draw calls (the game's, and the dev tools' separately), primitives, uploads, texture binds;
  and for every tracked pipeline, its passes and layers: commands, culled commands, batches, and why batches broke.
- **Assets**: what each content manager (and child) holds, with the memory of each asset, of each manager and in
  total (textures as RGBA8, font atlases, sound samples, text), sorted by size, name or type. Assets report their size
  through `SizedAsset`.
- **Log**: the latest messages, warnings and errors counted since the last clear.
- **Game**: your own watches, switches, sliders and buttons.

## Your game's values

```java
@Override
public void load() {
    ...
    devTools().track(pipeline, "world");                     // its passes and layers in the Render tab
    devTools().watch("enemies", () -> enemies.size(), 0)    // numbers (decimals)
              .watch("player x", () -> player.x)
              .watchText("state", () -> state);              // any value as text
    colliders = devTools().addToggle("show colliders", false);
    speed = devTools().addSlider("player speed", 0, 400, 180);
    devTools().addAction("respawn", this::respawn);
}

@Override
public void update(DeltaTime delta) {
    player.move(speed.get() * delta.getElapsed());
    if (colliders.isOn()) { ... }
}
```

Toggles and sliders work (with their initial values) when the dev tools are unavailable, so game code needs no
branches. Watches are read only while the Game tab is shown.

## Input

While the pointer is over the window, `devTools().isPointerOverUi()` is true: ignore clicks (and scrolling) then.

```java
if (!devTools().isPointerOverUi() && Mouse.isMouseButtonDown(MouseButton.LEFT)) { ... }
```

On Android, pass touches through the host first, so the dev tools get the gestures meant for them and the game gets
the rest:

```java
@Override
public boolean dispatchTouchEvent(MotionEvent event) {
    return host.onTouch(event) || super.dispatchTouchEvent(event);
}
```

A gesture that starts on the window, or the three-finger tap, belongs to the dev tools until all fingers lift.

## Notes

- The window keeps its physical size: one unit per screen point on desktop (HiDPI aware), per density-independent
  pixel on Android.
- The bundled font is JetBrains Mono (SIL Open Font License 1.1, `engine/font/OFL.txt`), baked with
  `./gradlew :pixel-demo-desktop-learning:bakeEngineFont`.
- Log capture covers the engine's console logger (`Logger.addListener` lets other tools listen too).
