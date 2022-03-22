## 0.6.1

### New

* [extension] ECS extra new components (`pixel-ext-ecs-extra`).

### Changes

* [extension] ECS performance improvements.
* Updated gradlew wrapper (7.0 -> 7.4).

### Fixes

* (#9) Spritebatch drawing counter bug.
* SpaceShooter demo background texture native buffers.

## 0.6.0

> **Note**: There is no link for tasks as up until now they were defined using an internal management tool (this is going to change from this version on).

### New

* [extension] - LDTK support (`ext-ldtk`).
* [extension] - Entity component system (`ext-ecs`).
* [extension] - Entity component system utilities (`ext-ecs-extra`).
* [extension] - Log4J2 support (`ext-log4j2`).
* SpriteBatch manual depth sorting.
* AudioEngine features (panning, pitch, time position, etc...).
* Added functions across the available math helper classes.
* Multiple new helper classes.
---
* More demo games.
* PUBLISH.md file.
* CODE_OF_CONDUCT.md file.
* CHANGELOG.md file.

### Changes

* Render screen can now be manually cleared via `window.clear()` function.
* Modified source files to include Javadoc comments.
* Build system (for publishing purposes).
* Changed `physics` and `gui` modules to the "extensions" directory.
* Multiple optimization improvements across the project.
* Updated dependencies:
    * JUNIT (5.8.2).
    * JSON.ORG (20210307).
* Modified logging system to be easier to override.
---
* Moved GitHub resources to the private `.github` directory.
* Updated README.md.

### Fixes

* Matrix4 rotation inverted y-axis.
* KeyboardKey.KP_DEVIDE typo (`KeyboardKey.KP_DIVIDE`).
* Minor fixes across the project.
