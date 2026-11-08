![Pixel - Java Game Framework](./.github/IMAGES/banner.png)

![](https://img.shields.io/badge/platform-Windows%20%7C%20Linux%20%7C%20MacOS-lightgrey)

## Pixel Framework ##

### What is this repository for? ###

This repository contains the Pixel Framework and associated modules/dependencies.

### Description ###

The Pixel Framework aims to provide a high performance and lightweight OpenGL 2D game development workflow. It is
influenced by the popular XNA framework and is built on top of the [LWJGL](https://www.lwjgl.org/) (Desktop) and OpenGL ES (Android).

> :book: For practical details on how to use this framework, please check the [wiki page](https://github.com/joafalves/pixel-community/wiki).

Pixel is designed to be modular and easy to extend. Check 
[here](https://github.com/joafalves/pixel-community/wiki/E.-Extensions-Overview) for more details on how to use the 
available extensions (or how to create your own).

### Examples ##

Check the :file_folder: [demos folder](https://github.com/joafalves/pixel-community/tree/devel/demos) for examples.

##### Basic example (Drawing a Sprite) #####

```java
public class SingleSpriteDemo extends GameApp {

    private Camera2D gameCamera;
    private SpriteBatch spriteBatch;
    private Texture spriteTex;

    @Override
    public void load() {
        // the game context is available from load() on: graphics(), content(), audio(), window(), settings()
        spriteBatch = graphics().createSpriteBatch(); // created by you: you dispose it
        gameCamera = new Camera2D(context());

        // the content manager is shared by the whole game and released by the framework
        spriteTex = content().load("<texture_path>", Texture.class);
        // ... or with the built-in 'texture' method: content().loadTexture(...)
    }

    @Override
    public void update(DeltaTime delta) {
        // game update logic goes here
    }

    @Override
    public void draw(DeltaTime delta) {
        spriteBatch.begin(gameCamera.getViewMatrix(), BlendMode.NORMAL_BLEND);
        spriteBatch.draw(spriteTex, Vector2.ZERO, Color.WHITE);
        spriteBatch.end();
    }

    @Override
    public void dispose() {
        spriteBatch.dispose();
    }

    public static void main(String[] args) {
        // the same GameApp runs on other platforms through their launcher (e.g. AndroidLauncher)
        DesktopLauncher.run(new SingleSpriteDemo(), new WindowSettings("Sprite demo", 800, 600));
    }
}
```

> Looking for ECS support? Check [this built-in extension!](https://github.com/joafalves/pixel-community/wiki/E1.-ECS-(Entity-Component-System))

### Project structure ###

The framework functionality is divided into multiple modules which can be imported individually as required.

##### Root directory structure #####

    .demos/                         # Feature showroom and learning examples
    .extensions/                    # Extensions for the framework (optional)
        ├── ext-ecs                 # Entity component system extension
        ├── ext-ecs-extra           # ECS utility components
        ├── ext-log4j2              # Log4j2 extension
        ├── ext-network             # Network extension
        └── ext-tween               # Tween extension
    .modules/                       # The principal modules of the framework
        ├── commons                 # Common utility classes
        ├── content                 # Common Content classes (Texture, Font, Audio, ...)
        ├── core                    # Core module (GameApp, GameContext, GameSettings, Camera2D, ...)
        ├── graphics                # Graphics API module
        ├── math                    # Math module (Vector, Matrix, ...)
        └── pipeline                # Generic Pipeline processing module
    .platform/
        ├── android                 # Android platform implementation (OpenGL ES 3.0)
        ├── desktop                 # Desktop platform implementation (LWJGL3, OpenGL 3.3)
        └── opengl                  # Renderer shared by desktop and Android (through GLApi)
    .resources/
        └── images                  # Project resource images
    .build.gradle                   # Gradle build file
    .settings.gradle                # Gradle settings file

##### Inner module structure #####

    .modules/
        └── *module*                 # Presented file structure similar in all modules
            ├── build                # Module build directory
            │   ├── docs             # Generated documentation files (run 'javadoc' gradle task)
            │   └── libs             # Generated .jar files (run 'jar' gradle task)
            ├── src                  # Module Source folder
            │   ├── main             # Module Main Source classes
            │   └── test             # Module Test Source classes
            └── build.gradle         # Module Gradle build file (contains inner dependency definitions)

### Runtime requirements ###

- Java 17.x+

### Development requirements ###

- JDK 17 or newer to run the build (the libraries are always compiled for Java 17)
- Gradle 9.x — use the included wrapper (`./gradlew`), no local install needed

### Runtime OS compatibility ###

For desktop, the same support as the [LWJGL](https://www.lwjgl.org/) dependency, which includes:

- Windows (x86, x64, arm64)
- MacOS (x64, arm64)
- Linux (x86, x64, arm64, arm32)

> Requires OpenGL 3.3+ support.

There is also an Android platform implementation available (OpenGL ES 3.0, Android 11 / API 30+) - **Experimental**.
It uses the same renderer as desktop. Audio and touch input are not implemented on Android yet.

#### Building for Android ####

The Android modules are optional and off by default, so an installed Android SDK does not slow down or break
desktop builds. To include them, set `pixel.android=true` in one of:

- the command line: `./gradlew -Ppixel.android=true ...`
- `local.properties` (per developer, not committed): `pixel.android=true`
- the `PIXEL_ANDROID` environment variable

The SDK is taken from `sdk.dir` in `local.properties` (Android Studio writes it) or `ANDROID_HOME`; with
`pixel.android=true` and no SDK, the build stops with an error. To build, install and start the demo on a
connected device or a running emulator (add `-Pscreenshot=build/android.png` to save the screen):

```bash
./gradlew -Ppixel.android=true :pixel-demo-android-learning:androidRun
```

### FAQ ###

1. I'm unable to run Pixel on MacOS due to system error.
    - Add `-XstartOnFirstThread` as a java VM Option before running your project.
2. Is Pixel compatible with Kotlin?
    - Yes, Pixel is fully compatible with Kotlin. Check
      this [demo](https://github.com/joafalves/pixel-community/tree/devel/demos/kotlin) for an example.
3. Is Pixel available as a Maven dependency?
    - Yes, Pixel is available as a public Maven
      dependency. [Click here](https://github.com/joafalves/pixel-community/wiki/1.-Getting-Started) for more details on
      how to import using Maven or Gradle.
4. Is Pixel free?
    - Yes, Pixel is completely free to use and distribute as an application dependency.

### Who do I talk to? ###

* It's a bug or a feature request? [Please open an issue](https://github.com/joafalves/pixel-community/issues).
* Repo owner or moderator.
