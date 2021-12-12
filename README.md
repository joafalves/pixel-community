![Pixel - Java Game Framework](/resources/images/logo-read-me.png)

![](https://img.shields.io/badge/platform-Windows%20%7C%20Linux%20%7C%20MacOS-lightgrey) ![](https://img.shields.io/badge/java-%3E%3D%208-green)

# README #

### What is this repository for? ###

This repository contains the Pixel Java Framework and associated modules and dependencies. 
**At the moment this software is in Development Stage and not ready for production use. Feel free to experiment and help this project grow.** 

### Description ###

The Pixel Framework aims to provide a high performance and lightweight OpenGL (lwjgl-glfw) 2D game development workflow. 
Directly inspired in existing frameworks such as XNA, this framework it's not strictly imposing and enables personal development workflows.

:book: For practical details on how to use this framework, please check our [wiki page]()  (under construction) 

### Examples ##

Check the :file_folder: demos folder for more examples and development approaches.

##### Drawing a Sprite #####

```java
public class SingleSpriteDemo extends PixelWindow {

    private Camera2D gameCamera;
    private ContentManager content;
    private SpriteBatch spriteBatch;

    private Texture spriteTex;
    private Vector2 spritePos;

    public SingleSpriteDemo(GameSettings settings) {
        super(settings);
        setBackgroundColor(Color.BLACK);
    }

    @Override
    public void load() {
        // game related changes & definitions
        gameCamera = new Camera2D(this);
        gameCamera.setOrigin(Vector2.zero());

        // general game instances
        content = new ContentManager();
        spriteBatch = new SpriteBatch();

        // load texture into memory
        spriteTex = content.load("images/earth-48x48.png", Texture.class);

        // related sprite properties
        spritePos = new Vector2(getVirtualWidth() / 2f, getVirtualHeight() / 2f);
    }

    @Override
    public void update(DeltaTime delta) {
        // game update logic goes here
    }

    @Override
    public void draw(DeltaTime delta) {
        // begin the spritebatch phase:
        spriteBatch.begin(gameCamera.getViewMatrix(), BlendMode.NORMAL_BLEND);

        // sprite definition for this drawing phase:
        spriteBatch.draw(spriteTex, spritePos, Color.WHITE, Vector2.HALF, 3f);

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
    .modules/
        ├── commons                 # Common utility classes
        ├── content                 # Resource modules
        ├── core                    # Main module, contains principal classes
        ├── gui                     # Graphical interface module (WIP)
        ├── input                   # Input module
        ├── physics                 # Physics module (WIP)
        └── math                    # Math module
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

### Third-party libs ###

    lwjgl 3.x       (runtime)
    org.json 2021+  (runtime)
    junit 5.x       (test)
   
### Setup Requirements ###

1. Java 11+ (up-to version 17)
2. Gradle 7.x+

### FAQ ###

**I'm unable to run Pixel on MacOS due to system error**

Add `-XstartOnFirstThread` as a java VM Option before running your project.

### Who do I talk to? ###

* Repo owner or admin
* Other community or team contact
