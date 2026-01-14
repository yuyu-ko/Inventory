package com.inventory.model;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderItem {
    private String sku;
    private Integer quantity;
    private String temperatureZone; // AMBIENT, CHILLED, FROZEN
}
