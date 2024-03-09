/*
 * This software is available under Apache License
 * Copyright (c) 2020
 */

package org.pixel.gui;

import static org.lwjgl.opengl.GL11C.GL_ONE_MINUS_SRC_ALPHA;
import static org.lwjgl.opengl.GL11C.GL_SRC_ALPHA;
import static org.lwjgl.opengl.GL11C.glBlendFunc;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import org.pixel.commons.DeltaTime;
import org.pixel.commons.lifecycle.Disposable;
import org.pixel.commons.lifecycle.Loadable;
import org.pixel.commons.lifecycle.Updatable;
import org.pixel.commons.logger.Logger;
import org.pixel.commons.logger.LoggerFactory;
import org.pixel.commons.util.IOUtils;
import org.pixel.core.GameWindow;
import org.pixel.core.WindowEventListener;
import org.pixel.core.WindowMode;
import org.pixel.graphics.render.NvgRenderEngine;
import org.pixel.graphics.render.RenderBuffer;
import org.pixel.gui.common.UIContext;
import org.pixel.gui.component.UIComponent;
import org.pixel.gui.style.Style;
import org.pixel.gui.style.StyleFactory;
import org.pixel.math.Rectangle;
import org.pixel.math.SizeInt;

public class UIView implements Updatable, Loadable, Disposable, WindowEventListener {

    private static final Logger LOG = LoggerFactory.getLogger(UIView.class);
    private static final String DEFAULT_STYLE_FILEPATH = "engine/style/default.css";
    private static final int DEFAULT_FPS = 60;

    private final UIContext context;
    private final RenderBuffer renderBuffer;
    private final GameWindow window;
    private UIScene scene;
    private int targetFps;
    private float elapsed;

    /**
     * Constructor
     */
    public UIView(GameWindow window) {
        this.window = window;
        this.window.addWindowEventListener(this);
        this.targetFps = DEFAULT_FPS;
        this.elapsed = 0.f;
        this.renderBuffer = new RenderBuffer(new Rectangle(0, 0, window.getWindowFrameWidth(), window.getWindowFrameHeight()));
        this.context = UIContext.builder()
                .renderEngine(new NvgRenderEngine(window.getVirtualWidth(), window.getVirtualHeight()))
                .windowDimensions(window.getWindowDimensions())
                .build();
        this.context.getRenderEngine().setPixelRatio(window.getWindowDimensions().getPixelRatio());
        this.loadStyle(DEFAULT_STYLE_FILEPATH);
    }

    /**
     * @param filepath
     */
    public void loadStyle(String filepath) {
        Style style = StyleFactory.getStyle(filepath);
        if (style == null) {
            LOG.warn("Could not load style on the given filepath '{}'.", filepath);
            return;
        }

        this.context.setStyle(style);
    }

    /**
     * @param componentClass
     * @param <T>
     * @return
     */
    public <T> T createComponent(Class<T> componentClass) {
        if (UIComponent.class.isAssignableFrom(componentClass) && !(Modifier.isAbstract(componentClass.getModifiers()))) {
            try {
                T instance = componentClass.getDeclaredConstructor().newInstance();
                ((UIComponent) instance).setContext(context);

                return instance;

            } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
                LOG.error("Exception caught!", e);
            }
        }

        return null;
    }

    /**
     * @return
     */
    public UIScene createScene() {
        UIScene uiScene = new UIScene();
        uiScene.setContext(context);
        return uiScene;
    }

    /**
     * Setup packed fonts
     */
    private void setupPackedFonts() {
        context.getRenderEngine().addFont(IOUtils.loadFile("engine/fonts/roboto-regular.ttf"), "roboto");
    }

    /**
     * Load function
     */
    @Override
    public void load() {
        setupPackedFonts();
    }

    /**
     * Update function
     *
     * @param delta
     */
    @Override
    public void update(DeltaTime delta) {
        if (scene != null) {
            scene.update(delta);
        }
    }

    /**
     * Draw function
     *
     * @param delta
     */
    public void draw(DeltaTime delta) {
        elapsed += delta.getElapsed();
        if (scene != null && elapsed > 1.0 / targetFps) {
            elapsed = 0;
            // capture the GUI drawing data to the render buffer:
            // glBlendFuncSeparate(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA, GL_ONE, GL_ONE);
            // glBlendFuncSeparate(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA, GL_ONE, GL_ONE_MINUS_SRC_ALPHA);
            renderBuffer.begin();
            scene.draw(delta);
            renderBuffer.end();

            if (context.getRenderEngine() instanceof NvgRenderEngine) {
                glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
            }
        }

        // draw whatever is in the render buffer
        renderBuffer.draw();
    }

    /**
     * @return
     */
    public SizeInt getViewportSize() {
        return window.getViewportSize();
    }

    /**
     * @return
     */
    public Style getStyle() {
        return this.context.getStyle();
    }

    /**
     * @param style
     */
    public void setStyle(Style style) {
        this.context.setStyle(style);
    }

    /**
     * @param scene
     */
    public void setScene(UIScene scene) {
        this.scene = scene;
        this.scene.setContext(this.context);
    }

    /**
     * Dispose function
     */
    @Override
    public void dispose() {
        window.removeWindowEventListener(this);
        context.getRenderEngine().dispose();
    }

    /**
     * @return
     */
    public int getTargetFps() {
        return targetFps;
    }

    /**
     * @param targetFps
     */
    public void setTargetFps(int targetFps) {
        this.targetFps = targetFps;
    }

    /**
     * Triggers when the window size changes
     *
     * @param newWidth
     * @param newHeight
     */
    @Override
    public void windowSizeChanged(int newWidth, int newHeight) {
        this.context.getRenderEngine().setPixelRatio(window.getWindowDimensions().getPixelRatio());
        this.context.getRenderEngine().setViewport(0, 0, window.getVirtualWidth(), window.getVirtualHeight());
        this.renderBuffer.setSourceArea(0, 0, window.getWindowFrameWidth(), window.getWindowFrameHeight());
    }

    /**
     * Triggers when the window mode changes
     *
     * @param windowMode
     */
    @Override
    public void windowModeChanged(WindowMode windowMode) {

    }
}
