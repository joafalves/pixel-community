package org.pixel.imgui;

import imgui.ImGui;
import imgui.gl3.ImGuiImplGl3;
import imgui.glfw.ImGuiImplGlfw;
import org.pixel.commons.DeltaTime;
import org.pixel.core.PixelWindow;
import org.pixel.core.GameSettings;

public class ImGuiDemo extends PixelWindow {

    private final ImGuiImplGlfw imGuiGlfw = new ImGuiImplGlfw();
    private final ImGuiImplGl3 imGuiGl3 = new ImGuiImplGl3();

    /**
     * Constructor
     *
     * @param settings
     */
    public ImGuiDemo(GameSettings settings) {
        super(settings);
    }

    @Override
    public void load() {
        ImGui.createContext();
        imGuiGlfw.init(getWindowHandle(), true);
        imGuiGl3.init();
    }

    @Override
    public void draw(DeltaTime delta) {
        imGuiGlfw.newFrame();
        ImGui.newFrame();

        ImGui.text("Hello, World!");

        ImGui.render();
        imGuiGl3.renderDrawData(ImGui.getDrawData());
    }

    @Override
    public void dispose() {
        super.dispose();

        imGuiGl3.dispose();
        imGuiGlfw.dispose();
        ImGui.destroyContext();
    }

    public static void main(String[] args) {
        final int width = 640;
        final int height = 480;
        GameSettings settings = new GameSettings(width, height);
        settings.setWindowResizable(true);
        settings.setMultisampling(2);
        settings.setVsync(true);
        settings.setDebugMode(true);
        settings.setWindowWidth(width);
        settings.setWindowHeight(height);

        PixelWindow window = new ImGuiDemo(settings);
        window.start();
    }
}
