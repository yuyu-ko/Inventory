package com.inventory.controller;

import com.inventory.model.InventoryItem;
import com.inventory.service.InventoryManager;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/inventory")
@RequiredArgsConstructor
public class InventoryController {

    private final InventoryManager inventoryManager;

    @GetMapping("/{sku}")
    public ResponseEntity<InventoryItem> getInventory(@PathVariable String sku) {
        InventoryItem item = inventoryManager.getInventory(sku);
        if (item != null) {
            return ResponseEntity.ok(item);
        }
        return ResponseEntity.notFound().build();
    }

    @PostMapping("/initialize")
    public ResponseEntity<String> initializeInventory(
            @RequestParam String sku,
            @RequestParam int quantity,
            @RequestParam(required = false, defaultValue = "AMBIENT") String temperatureZone) {
        inventoryManager.initializeInventory(sku, quantity, temperatureZone);
        return ResponseEntity.ok("Inventory initialized for SKU: " + sku);
    }
}
