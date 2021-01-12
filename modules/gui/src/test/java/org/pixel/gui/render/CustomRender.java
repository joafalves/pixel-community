/*
 * This software is available under Apache License
 * Copyright (c) 2020
 */

package org.pixel.gui.render;

import org.lwjgl.Version;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.opengl.GL;
import org.lwjgl.system.MemoryStack;
import org.pixel.graphics.Color;
import org.pixel.graphics.render.NvgRenderEngine;
import org.pixel.graphics.render.RenderEngine2D;

import java.nio.IntBuffer;

import static org.lwjgl.glfw.Callbacks.glfwFreeCallbacks;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.system.MemoryUtil.NULL;

public class CustomRender {

    private static final int SWIDTH = 640;
    private static final int SHEIGHT = 480;

    // The window handle
    private long window;
    private RenderEngine2D renderEngine;

    public void run() {
        System.out.println("Hello LWJGL " + Version.getVersion() + "!");

        init();
        loop();

        // Free the window callbacks and destroy the window
        glfwFreeCallbacks(window);
        glfwDestroyWindow(window);

        // Terminate GLFW and free the error callback
        glfwTerminate();
        glfwSetErrorCallback(null).free();
    }

    private void init() {
        // Setup an error callback. The default implementation
        // will print the error message in System.err.
        GLFWErrorCallback.createPrint(System.err).set();

        // Initialize GLFW. Most GLFW functions will not work before doing this.
        if (!glfwInit())
            throw new IllegalStateException("Unable to initialize GLFW");

        // Configure GLFW
        glfwDefaultWindowHints(); // optional, the current window hints are already the default
        glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE); // the window will stay hidden after creation
        glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE); // the window will be resizable

        // Create the window
        window = glfwCreateWindow(SWIDTH, SHEIGHT, "Hello World!", NULL, NULL);
        if (window == NULL)
            throw new RuntimeException("Failed to create the GLFW window");

        // Setup a key callback. It will be called every time a key is pressed, repeated or released.
        glfwSetKeyCallback(window, (window, key, scancode, action, mods) -> {
            if (key == GLFW_KEY_ESCAPE && action == GLFW_RELEASE)
                glfwSetWindowShouldClose(window, true); // We will detect this in the rendering loop
        });

        // Get the thread stack and push a new frame
        try (MemoryStack stack = stackPush()) {
            IntBuffer pWidth = stack.mallocInt(1); // int*
            IntBuffer pHeight = stack.mallocInt(1); // int*

            // Get the window size passed to glfwCreateWindow
            glfwGetWindowSize(window, pWidth, pHeight);

            // Get the resolution of the primary monitor
            GLFWVidMode vidmode = glfwGetVideoMode(glfwGetPrimaryMonitor());

            // Center the window
            glfwSetWindowPos(
                    window,
                    (vidmode.width() - pWidth.get(0)) / 2,
                    (vidmode.height() - pHeight.get(0)) / 2
            );
        } // the stack frame is popped automatically

        // Make the OpenGL context current
        glfwMakeContextCurrent(window);
        // Enable v-sync
        glfwSwapInterval(1);

        // Make the window visible
        glfwShowWindow(window);

        GL.createCapabilities();

        renderEngine = new NvgRenderEngine(SWIDTH, SHEIGHT);
    }

    private void loop() {
        // This line is critical for LWJGL's interoperation with GLFW's
        // OpenGL context, or any context that is managed externally.
        // LWJGL detects the context that is current in the current thread,
        // creates the GLCapabilities instance and makes the OpenGL
        // bindings available for use.
        GL.createCapabilities();

        // Set the clear color
        glClearColor(.90f, .90f, .90f, 1.0f);

        // Run the rendering loop until the user has attempted to close
        // the window or has pressed the ESCAPE key.
        while (!glfwWindowShouldClose(window)) {
            glClear(GL_COLOR_BUFFER_BIT | GL_STENCIL_BUFFER_BIT); // clear the framebuffer

            renderEngine.begin();

            /*renderEngine.strokeWidth(4.0f);
            renderEngine.strokeColor(Color.BLACK);

            renderEngine.beginPath();
            renderEngine.moveTo(10, 10);
            renderEngine.lineTo(20, 20);
            renderEngine.lineTo(100, 20);
            renderEngine.lineTo(110, 10);
            renderEngine.quadraticCurveTo(80, 0, 50, 10);
            renderEngine.closePath();
            renderEngine.stroke();

            renderEngine.beginPath();
            //renderEngine.roundedRectangle(10, 50, 100, 100, 8f);

            renderEngine.circle(SWIDTH * 0.5F, SHEIGHT * 0.5f, 100);
            renderEngine.strokeWidth(8.f);
            renderEngine.stroke();
            renderEngine.fillColor(Color.RED);
            renderEngine.fill();

            renderEngine.beginPath();
            renderEngine.moveTo(130, 40);
            renderEngine.bezierCurveTo(170, 40, 160, 200, 200, 200);
            renderEngine.stroke();*/


            renderEngine.translate(10, 10);
            renderEngine.scissor(0, 0, 10, 10);
            renderEngine.beginPath();
            renderEngine.rectangle(0, 0, 100, 100);
            renderEngine.fillColor(Color.YELLOW);
            renderEngine.fill();

            renderEngine.end();

            // swap the color buffers
            glfwSwapBuffers(window);

            // Poll for window events. The key callback above will only be
            // invoked during this call.
            glfwPollEvents();
        }
    }

    public static void main(String[] args) {
        new CustomRender().run();
    }
}
