package org.pixel.demo.imgui;

import imgui.ImGui;
import imgui.ImGuiStyle;
import imgui.flag.ImGuiCol;
import imgui.flag.ImGuiWindowFlags;
import imgui.gl3.ImGuiImplGl3;
import imgui.glfw.ImGuiImplGlfw;
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

        applyGlobalStyles();
    }

    @Override
    public void draw(DeltaTime delta) {
        super.draw(delta);
        imGuiGlfw.newFrame();
        ImGui.newFrame();

        ImGui.setNextWindowPos(0, 0);
        ImGui.setNextWindowSize(getWindowManager().getWindowWidth(), getWindowManager().getWindowHeight());

        ImGui.pushStyleColor(ImGuiCol.WindowBg, ImGui.getColorU32(0.2f, 0.2f, 0.2f, 1.0f)); // #333 in RGB
        ImGui.begin("Pixel Engine", ImGuiWindowFlags.NoDecoration | ImGuiWindowFlags.NoMove
                | ImGuiWindowFlags.NoResize | ImGuiWindowFlags.NoBringToFrontOnFocus | ImGuiWindowFlags.NoCollapse);

        // draw a main menu bar with a file menu
        if (ImGui.beginMainMenuBar()) {
            if (ImGui.beginMenu("File")) {
                if (ImGui.menuItem("Exit")) {
                    dispose();
                }
                ImGui.endMenu();
            }
            if (ImGui.beginMenu("Edit")) {
                if (ImGui.menuItem("Placeholder 1")) {
                    // Placeholder action
                }
                ImGui.endMenu();
            }
            ImGui.endMainMenuBar();
        }

        ImGui.end();
        ImGui.popStyleColor();

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

    public void applyGlobalStyles() {
        ImGuiStyle style = ImGui.getStyle();

        style.setColor(ImGuiCol.Button, ImGui.getColorU32(0.2f, 0.5f, 0.2f, 1.0f));
        style.setColor(ImGuiCol.ButtonHovered, ImGui.getColorU32(0.3f, 0.6f, 0.3f, 1.0f));
        style.setColor(ImGuiCol.ButtonActive, ImGui.getColorU32(0.1f, 0.4f, 0.1f, 1.0f));

        // Customize other style variables
        style.setFrameRounding(8.0f);   // Rounded corners for buttons
        style.setFramePadding(10.0f, 10.0f);  // Padding inside buttons
    }

    public static void main(String[] args) {
        final int width = 640;
        final int height = 480;
        var settings = new WindowSettings(width, height);
        settings.setWindowResizable(true);
        settings.setMultisampling(2);
        settings.setVsync(true);
        settings.setDevMode(false);
        settings.setWindowWidth(width);
        settings.setWindowHeight(height);

        var window = new ImGuiDemo(settings);
        window.start();
    }
}
