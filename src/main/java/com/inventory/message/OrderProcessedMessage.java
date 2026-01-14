package com.inventory.message;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderProcessedMessage implements Serializable {
    private String orderId;
    private String status; // PROCESSING, COMPLETED, FAILED
    private LocalDateTime processedTime;
    private String message;
}
