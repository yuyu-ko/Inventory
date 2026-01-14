package com.inventory.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "inventory_items")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class InventoryItem {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(unique = true, nullable = false)
    private String sku;
    
    private String name;
    private Integer quantity;
    private Integer reservedQuantity;
    
    private String temperatureZone; // AMBIENT, CHILLED, FROZEN
    
    private Integer lowStockThreshold;
    
    public Integer getAvailableQuantity() {
        return quantity - reservedQuantity;
    }
}
