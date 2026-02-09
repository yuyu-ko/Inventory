package com.inventory.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/health")
@RequiredArgsConstructor
public class HealthController {

    private final ConnectionFactory connectionFactory;

    @GetMapping
    public ResponseEntity<Map<String, Object>> health() {
        Map<String, Object> status = new HashMap<>();
        status.put("status", "UP");
        status.put("application", "Inventory Simulator");
        
        // Check RabbitMQ connection
        try {
            var connection = connectionFactory.createConnection();
            boolean rabbitmqConnected = connection.isOpen();
            status.put("rabbitmq", rabbitmqConnected ? "CONNECTED" : "DISCONNECTED");
            if (rabbitmqConnected) {
                connection.close();
            }
        } catch (Exception e) {
            status.put("rabbitmq", "DISCONNECTED");
            status.put("rabbitmqError", e.getMessage());
            log.warn("RabbitMQ connection check failed: {}", e.getMessage());
        }
        
        return ResponseEntity.ok(status);
    }
}
