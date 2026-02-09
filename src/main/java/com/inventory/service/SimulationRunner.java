package com.inventory.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class SimulationRunner {

    private final SimulationClock simulationClock;

    @Value("${inventory.simulation.tick-interval-ms:1000}")
    private long tickIntervalMs;

    /**
     * Scheduled tick simulation clock
     */
    @Scheduled(fixedDelayString = "${inventory.simulation.tick-interval-ms:1000}")
    public void runSimulationTick() {
        if (simulationClock.isRunning()) {
            simulationClock.tick();
        }
    }
}
