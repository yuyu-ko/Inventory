package com.inventory.service;

import com.inventory.message.InventoryUpdateMessage;
import com.inventory.model.InventoryItem;
import com.inventory.repository.InventoryItemRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class InventoryManager {

    private final InventoryItemRepository inventoryRepository;

    @Value("${spring.rabbitmq.exchange.name:symbotic.simulation}")
    private String exchangeName;

    @Value("${spring.rabbitmq.topic.prefix:sim}")
    private String topicPrefix;

    @Value("${inventory.inventory.initial-stock:1000}")
    private int initialStock;

    @Value("${inventory.inventory.low-stock-threshold:100}")
    private int lowStockThreshold;

    @Value("${inventory.inventory.replenishment-quantity:500}")
    private int replenishmentQuantity;

    /**
     * Listen to inventory update messages
     */
    @RabbitListener(queues = "${spring.rabbitmq.topic.prefix:sim}.inventory.update")
    @Transactional
    public void handleInventoryUpdate(InventoryUpdateMessage message) {
        log.info("Inventory Manager: Received inventory update for SKU {}: {}", 
            message.getSku(), message.getOperation());

        InventoryItem item = getOrCreateInventoryItem(message.getSku());

        switch (message.getOperation()) {
            case "RESERVE":
                reserveInventory(item, message.getReservedQuantityChange());
                break;
            case "RELEASE":
                releaseInventory(item, message.getReservedQuantityChange());
                break;
            case "DEDUCT":
                deductInventory(item, message.getQuantityChange());
                break;
            case "REPLENISH":
                replenishInventory(item, message.getQuantityChange());
                break;
            default:
                log.warn("Inventory Manager: Unknown operation {}", message.getOperation());
        }

        // Check if replenishment is needed
        checkAndReplenish(item);
    }

    /**
     * Get or create inventory item
     */
    private InventoryItem getOrCreateInventoryItem(String sku) {
        Optional<InventoryItem> optional = inventoryRepository.findBySku(sku);
        if (optional.isPresent()) {
            return optional.get();
        }

        // Create new inventory item
        InventoryItem item = new InventoryItem();
        item.setSku(sku);
        item.setName("Item " + sku);
        item.setQuantity(initialStock);
        item.setReservedQuantity(0);
        item.setTemperatureZone("AMBIENT");
        item.setLowStockThreshold(lowStockThreshold);
        
        return inventoryRepository.save(item);
    }

    /**
     * Reserve inventory
     */
    private void reserveInventory(InventoryItem item, Integer quantity) {
        if (quantity == null || quantity <= 0) return;

        int available = item.getAvailableQuantity();
        if (available >= quantity) {
            item.setReservedQuantity(item.getReservedQuantity() + quantity);
            inventoryRepository.save(item);
            log.info("Inventory Manager: Reserved {} units of SKU {}. Available: {}", 
                quantity, item.getSku(), item.getAvailableQuantity());
        } else {
            log.warn("Inventory Manager: Insufficient stock for SKU {}. Requested: {}, Available: {}", 
                item.getSku(), quantity, available);
        }
    }

    /**
     * Release reserved inventory
     */
    private void releaseInventory(InventoryItem item, Integer quantity) {
        if (quantity == null || quantity <= 0) return;

        int currentReserved = item.getReservedQuantity();
        int releaseAmount = Math.min(quantity, currentReserved);
        item.setReservedQuantity(currentReserved - releaseAmount);
        inventoryRepository.save(item);
        
        log.info("Inventory Manager: Released {} units of SKU {}. Reserved: {}", 
            releaseAmount, item.getSku(), item.getReservedQuantity());
    }

    /**
     * Deduct inventory
     */
    private void deductInventory(InventoryItem item, Integer quantity) {
        if (quantity == null || quantity <= 0) return;

        int currentReserved = item.getReservedQuantity();
        int deductFromReserved = Math.min(quantity, currentReserved);
        int deductFromStock = quantity - deductFromReserved;

        item.setReservedQuantity(currentReserved - deductFromReserved);
        item.setQuantity(item.getQuantity() - deductFromStock);
        inventoryRepository.save(item);
        
        log.info("Inventory Manager: Deducted {} units of SKU {}. Stock: {}, Reserved: {}", 
            quantity, item.getSku(), item.getQuantity(), item.getReservedQuantity());
    }

    /**
     * Replenish inventory
     */
    private void replenishInventory(InventoryItem item, Integer quantity) {
        if (quantity == null || quantity <= 0) {
            quantity = replenishmentQuantity; // Use default replenishment quantity
        }

        item.setQuantity(item.getQuantity() + quantity);
        inventoryRepository.save(item);
        
        log.info("Inventory Manager: Replenished {} units of SKU {}. Total stock: {}", 
            quantity, item.getSku(), item.getQuantity());
    }

    /**
     * Check and auto-replenish
     */
    private void checkAndReplenish(InventoryItem item) {
        if (item.getQuantity() <= item.getLowStockThreshold()) {
            log.warn("Inventory Manager: Low stock detected for SKU {}. Current: {}, Threshold: {}", 
                item.getSku(), item.getQuantity(), item.getLowStockThreshold());
            
            // Auto-replenish
            replenishInventory(item, replenishmentQuantity);
        }
    }

    /**
     * Query inventory (auto-create if not exists)
     */
    public InventoryItem getInventory(String sku) {
        return getOrCreateInventoryItem(sku);
    }

    /**
     * Initialize inventory (for testing and API)
     */
    public void initializeInventory(String sku, int quantity, String temperatureZone) {
        InventoryItem item = getOrCreateInventoryItem(sku);
        item.setQuantity(quantity);
        item.setTemperatureZone(temperatureZone);
        inventoryRepository.save(item);
    }

    /**
     * Initialize inventory from CSV (supports full fields)
     */
    public void initializeInventoryFromCSV(String sku, String name, int quantity, String temperatureZone, int lowStockThreshold) {
        Optional<InventoryItem> optional = inventoryRepository.findBySku(sku);
        InventoryItem item;
        
        if (optional.isPresent()) {
            item = optional.get();
            // Update existing inventory
            item.setName(name);
            item.setQuantity(quantity);
            item.setTemperatureZone(temperatureZone);
            item.setLowStockThreshold(lowStockThreshold);
        } else {
            // Create new inventory item
            item = new InventoryItem();
            item.setSku(sku);
            item.setName(name);
            item.setQuantity(quantity);
            item.setReservedQuantity(0);
            item.setTemperatureZone(temperatureZone);
            item.setLowStockThreshold(lowStockThreshold);
        }
        
        inventoryRepository.save(item);
        log.debug("Initialized inventory for SKU {}: quantity={}, zone={}", sku, quantity, temperatureZone);
    }
}
