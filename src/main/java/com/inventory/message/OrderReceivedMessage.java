package com.inventory.message;

import com.inventory.model.Order;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderReceivedMessage implements Serializable {
    private String orderId;
    private Order.OrderType orderType;
    private LocalDateTime orderPlacedTime;
    private LocalDateTime orderDueTime;
    private List<OrderItemDTO> items;
    private String customerId;
    private String senderId;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OrderItemDTO implements Serializable {
        private String sku;
        private Integer quantity;
        private String temperatureZone;
    }
}
