![Pixel - Java Game Framework]()

![](https://img.shields.io/badge/platform-Windows%20%7C%20Linux%20%7C%20MacOS-lightgrey) ![](https://img.shields.io/badge/java-%3E%3D%208-green)

# README #

### What is this repository for? ###

This repository contains the Pixel Java Framework and associated modules and dependencies. 
**At the moment this software is in Development Stage and not ready for production use.** 

### Description ###

The Pixel Framework aims to provide a high performance and lightweight OpenGL (lwjgl-glfw) 2D game development workflow. 
Directly inspired in existing frameworks such as XNA, this framework it's not strictly imposing and empowers personal development preferences.

:book: For practical details on how to use this framework, please check our [wiki page]()   

### Examples ##

##### Drawing text #####

```java
public class SpriteBatchFontGame extends Game {

    private Font font;
    private SpriteBatch spriteBatch;
    private KeyboardState lastKeyboardState;

    private String text;

    /**
     * Constructor
     *
     * @param gameSettings
     */
    public SpriteBatchFontGame(GameSettings gameSettings) {
        super(gameSettings);
    }

    /**
     * Load and initialize game assets
     */
    @Override
    public void load() {
        gameCamera.setOrigin(Vector2.zero());
        text = "The quick brown fox jumps over the lazy dog\nType something on the keyboard:\n";
        spriteBatch = new SpriteBatch();
        setBackgroundColor(Color.BLACK);

        font = new Font("./common/fonts/DejaVuSansMono.ttf", 24, 1);
        font.setVerticalSpacing(0);
    }

    /**
     * Update game logic
     *
     * @param delta
     */
    @Override
    public void update(double delta) {
        KeyboardState keyboardState = Keyboard.getState();
        keyboardState.downKeys().forEach(key -> {
            // add pressed keys to existing text
            if (lastKeyboardState != null && lastKeyboardState.isKeyUp(key)) {
                if (key.equals(Keys.ENTER.getValue())) text += '\n';
                else text += Character.toString(key);
            }
        });

        lastKeyboardState = keyboardState; // update last keyboard info for state comparison on the next cycle
    }

    /**
     * Draw game scene
     *
     * @param delta
     */
    @Override
    public void draw(double delta) {
        spriteBatch.begin(gameCamera.getMatrix());
        spriteBatch.putText(font, text, new Vector2(100, 100), Color.WHITE, 24);
        spriteBatch.end();
    }

    /**
     * Dispose game instances
     */
    @Override
    public void dispose() {
        font.dispose();
        spriteBatch.dispose();
    }
}
```

### Project Structure ###

The framework base functions are divided into different modules (bundle will be available including 
the most demanded features) which can be imported as required.

##### Root Directory Structure #####

    .build/                         # Bundle .jar files (run 'bundle' gradle task)
    .modules/
        ├── commons                 # Common utility classes
        ├── content                 # Resource modules
        ├── core                    # Main module, contains principal classes
        ├── examples                # Example demos
        ├── gui                     # Graphical interface module
        ├── input                   # Input module
        └── math                    # Math module
    .resources/
        └── images                  # Project resource images
    .build.gradle                   # Gradle build file
    .settings.gradle                # Gradle settings file
    
##### Module Structure #####

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

    lwjgl 3.x       (runtime required)
    org.json 2018+  (runtime required)
    junit 4.x       (test required)
   
### Setup Requirements ###

1. Java 8+ (tested up to version 12)
2. Gradle 4.x+

### FAQ ###

**I'm unable to run Pixel on MacOS due to system error**

Add `-XstartOnFirstThread` as a java VM Option before running your project.

### Who do I talk to? ###

* Repo owner or admin
* Other community or team contact
