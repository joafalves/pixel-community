package org.pixel.demo.imgui;

import imgui.ImGui;
import imgui.gl3.ImGuiImplGl3;
import imgui.glfw.ImGuiImplGlfw;
import imgui.type.ImString;
import org.pixel.commons.DeltaTime;
import org.pixel.core.WindowSettings;
import org.pixel.core.Game;

public class ImGuiDemo extends Game {

    private final ImGuiImplGlfw imGuiGlfw = new ImGuiImplGlfw();
    private final ImGuiImplGl3 imGuiGl3 = new ImGuiImplGl3();

    /**
     * Constructor
     *
     * @param settings
     */
    public ImGuiDemo(WindowSettings settings) {
        super(settings);
    }

    @Override
    public void load() {
        ImGui.createContext();
        imGuiGlfw.init(getWindowManager().getWindowHandle(), true);
        imGuiGl3.init();
    }

    @Override
    public void draw(DeltaTime delta) {
        imGuiGlfw.newFrame();
        ImGui.newFrame();

        ImString value = new ImString();

        ImGui.text("Hello, World!");
        ImGui.inputText("Some Value", value);

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
        var settings = new WindowSettings(width, height);
        settings.setWindowResizable(true);
        settings.setMultisampling(2);
        settings.setVsync(true);
        settings.setDebugMode(true);
        settings.setWindowWidth(width);
        settings.setWindowHeight(height);

        var window = new ImGuiDemo(settings);
        window.start();
    }
}
