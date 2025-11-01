package org.pixel.demo.learning.decs.event;

public class WaveCompleteEvent {
    private final int waveNumber;

    public WaveCompleteEvent(int waveNumber) {
        this.waveNumber = waveNumber;
    }

    public int getWaveNumber() {
        return waveNumber;
    }
}
