![Pixel - Java Game Framework](/resources/images/logo-read-me.png)

![](https://img.shields.io/badge/platform-Windows%20%7C%20Linux%20%7C%20MacOS-lightgrey) ![](https://img.shields.io/badge/java-%3E%3D%2011-green)

# README #

### What is this repository for? ###

This repository contains the Pixel Java Framework and associated modules/dependencies. 
**At the moment this software is in Development Stage and not ready for production use. Feel free to experiment and help this project grow.** 

### Description ###

The Pixel Framework aims to provide a high performance and lightweight OpenGL 2D game development workflow. 
It is heavily influenced by the popular XNA framework and is built on top of the [LWJGL](https://www.lwjgl.org/) and [GLFW](https://www.glfw.org/) projects.

:book: For practical details on how to use this framework, please check our [wiki page](https://github.com/joafalves/pixel-community/wiki)  (under construction) 

### Examples ##

Check the :file_folder: demos folder for more examples and development approaches.

##### Drawing a Sprite #####

```java
public class SingleSpriteDemo extends PixelWindow {

    private Camera2D gameCamera;
    private ContentManager content;
    private SpriteBatch spriteBatch;

    private Texture spriteTex;

    public SingleSpriteDemo(GameSettings settings) {
        super(settings);
        setBackgroundColor(Color.BLACK);
    }

    @Override
    public void load() {
        // load up of resources and game managers/utilities
        gameCamera = new Camera2D(this);
        content = new ContentManager();
        spriteBatch = new SpriteBatch();

        // example of loading a texture into memory:
        spriteTex = content.load("<texture_path>", Texture.class); 
    }

    @Override
    public void update(DeltaTime delta) {
        // game update logic goes here
    }

    @Override
    public void draw(DeltaTime delta) {
        // begin the spritebatch phase:
        spriteBatch.begin(gameCamera.getViewMatrix(), BlendMode.NORMAL_BLEND);

        // sprite draw/put for this drawing phase:
        spriteBatch.draw(spriteTex, Vector2.ZERO, Color.WHITE);

        // end and draw all sprites stored:
        spriteBatch.end();
    }
}
```

### Project Structure ###

The framework functionality is divided into multiple modules which can be imported individually as required.

##### Root Directory Structure #####

    .build/                         # Bundle .jar files (run 'bundle' gradle task)
    .demos/                         # Feature showroom and learning examples
    .extensions/                    # Extensions for the framework
        ├── ext-gui                 # GUI extension *WIP*
        ├── ext-ldtk                # LDTK extension
        ├── ext-log4j2              # Log4j2 extension
        └── ext-physics             # Physics extension *WIP*
    .modules/
        ├── commons                 # Common utility classes
        ├── core                    # Main module, contains principal classes
        ├── input                   # Input module (Keyboard, Gamepad, Mouse)
        └── math                    # Math module (Vector, Matrix, etc)
    .resources/
        └── images                  # Project resource images
    .build.gradle                   # Gradle build file
    .settings.gradle                # Gradle settings file
    
##### Inner Module Structure #####

    .modules/
        └── *module*                 # Presented file structure similar in all modules
            ├── build                # Module build directory
            │   ├── docs             # Generated documentation files (run 'javadoc' gradle task)
            │   └── libs             # Generated .jar files (run 'jar' gradle task)
            ├── src                  # Module Source folder
            │   ├── main             # Module Main Source classes
            │   └── test             # Module Test Source classes
            └── build.gradle         # Module Gradle build file (contains inner dependency definitions)
   
### Setup Requirements ###

1. Java 11+ (up-to version 17)
2. Gradle 7.x+

### FAQ ###

**I'm unable to run Pixel on MacOS due to system error**

Add `-XstartOnFirstThread` as a java VM Option before running your project.

### Who do I talk to? ###

* Repo owner or admin
* Other community or team contact
