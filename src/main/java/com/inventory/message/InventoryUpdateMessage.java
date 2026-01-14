package com.inventory.message;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InventoryUpdateMessage implements Serializable {
    private String sku;
    private Integer quantityChange;
    private Integer reservedQuantityChange;
    private String operation; // RESERVE, RELEASE, DEDUCT, REPLENISH
    private String orderId; // Optional: related order ID
}
