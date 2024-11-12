package org.pixel.demo.concept.spaceshooter;

import org.pixel.blueprint.annotation.Scheduled;
import org.pixel.blueprint.annotation.Service;

@Service
public class SpaceShooterService {

    @Scheduled(intervalMs = 1000)
    public void sampleScheduler() {
        System.out.println("This is just an example...");
    }
}
