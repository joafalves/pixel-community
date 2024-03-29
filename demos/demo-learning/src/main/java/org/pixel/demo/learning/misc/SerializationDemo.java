package org.pixel.demo.learning.misc;

import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.pixel.commons.util.DataSerializer;
import org.pixel.core.PixelWindow;
import org.pixel.core.WindowSettings;
import org.pixel.demo.learning.common.DemoGame;
import org.pixel.math.Vector2;

public class SerializationDemo extends DemoGame {

    private static final String DATA_PATH = "./game.save";

    @NoArgsConstructor
    @AllArgsConstructor
    private static class SaveData implements Serializable {

        private Vector2 position;
        private int gold;
    }

    public SerializationDemo(WindowSettings settings) {
        super(settings);
    }

    @Override
    public void load() {
        // hypothetical scenario of storing a save data...
        var gameData = new SaveData(new Vector2(100, 100), 5000);
        DataSerializer.write(gameData, DATA_PATH);

        // hypothetical scenario of loading a save data...
        var gameDataLoad = DataSerializer.read(DATA_PATH, SaveData.class);
        if (gameDataLoad != null) {
            log.info("Loaded Game Data (Position: {}; Gold: {}).", gameDataLoad.position, gameDataLoad.gold);
        } else {
            log.warn("Failed to load Game Data!");
        }
    }

    public static void main(String[] args) {
        WindowSettings settings = new WindowSettings(300, 300);
        settings.setVsync(true);

        PixelWindow window = new SerializationDemo(settings);
        window.start();
    }
}
