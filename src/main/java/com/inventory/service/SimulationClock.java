package com.inventory.service;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Slf4j
@Component
@Getter
public class SimulationClock {

    @Value("${inventory.simulation.sim-start-time:2024-01-13T08:00:00}")
    private String simStartTimeStr;

    @Value("${inventory.simulation.sim-end-time:2024-01-13T18:00:00}")
    private String simEndTimeStr;

    @Value("${inventory.simulation.tick-seconds:1}")
    private int tickSeconds;

    @Value("${inventory.simulation.speed-factor:1.0}")
    private double speedFactor;

    private LocalDateTime simStartTime;
    private LocalDateTime simEndTime;
    private LocalDateTime currentSimTime;
    private boolean isRunning = false;
    private final DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss");

    @PostConstruct
    public void initialize() {
        simStartTime = LocalDateTime.parse(simStartTimeStr);
        simEndTime = LocalDateTime.parse(simEndTimeStr);
        currentSimTime = simStartTime;
        isRunning = true;
        
        log.info("=== Simulation Clock Initialized ===");
        log.info("Simulation Start Time: {}", simStartTime);
        log.info("Simulation End Time: {}", simEndTime);
        log.info("Tick Interval: {} seconds", tickSeconds);
        log.info("Speed Factor: {}x", speedFactor);
        log.info("===================================");
    }

    /**
     * Simulation clock tick (called externally)
     */
    public void tick() {
        if (!isRunning) {
            return;
        }

        // Calculate actual seconds to add based on speedFactor
        int secondsToAdd = (int) (tickSeconds * speedFactor);
        currentSimTime = currentSimTime.plusSeconds(secondsToAdd);

        // Check if end time is reached
        if (currentSimTime.isAfter(simEndTime) || currentSimTime.equals(simEndTime)) {
            isRunning = false;
            log.info("=== Simulation Ended at {} ===", formatTime(currentSimTime));
        }
    }

    /**
     * Get current simulation time
     */
    public LocalDateTime getCurrentTime() {
        return currentSimTime;
    }

    /**
     * Check if simulation is running
     */
    public boolean isRunning() {
        return isRunning && !currentSimTime.isAfter(simEndTime);
    }

    /**
     * Format time for log output
     */
    public String formatTime(LocalDateTime time) {
        return time.format(timeFormatter);
    }

    /**
     * Format time for log output (with date)
     */
    public String formatDateTime(LocalDateTime time) {
        return time.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }

    /**
     * Check if specified time is within simulation time range
     */
    public boolean isTimeInRange(LocalDateTime time) {
        return !time.isBefore(simStartTime) && !time.isAfter(simEndTime);
    }

    /**
     * Get simulation progress (0.0 to 1.0)
     */
    public double getProgress() {
        long totalSeconds = java.time.Duration.between(simStartTime, simEndTime).getSeconds();
        long elapsedSeconds = java.time.Duration.between(simStartTime, currentSimTime).getSeconds();
        return Math.min(1.0, Math.max(0.0, (double) elapsedSeconds / totalSeconds));
    }
}
