package com.inventory.service;

import com.inventory.model.InventoryCSVRecord;
import com.opencsv.bean.CsvToBean;
import com.opencsv.bean.CsvToBeanBuilder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class InventoryInitializer {

    private final InventoryManager inventoryManager;

    @Value("${inventory.inventory.csv-file:data/inventory_sample.csv}")
    private String csvFilePath;

    @Value("${inventory.inventory.auto-initialize:true}")
    private boolean autoInitialize;

    @Value("${inventory.inventory.low-stock-threshold:100}")
    private int defaultLowStockThreshold;

    /**
     * Auto-initialize inventory from CSV file on application startup
     */
    @PostConstruct
    public void initializeInventoryFromCSV() {
        if (!autoInitialize) {
            log.info("Auto-initialize inventory is disabled");
            return;
        }

        try {
            log.info("Initializing inventory from CSV file: {}", csvFilePath);
            
            Resource resource = new ClassPathResource(csvFilePath);
            if (!resource.exists()) {
                log.warn("Inventory CSV file not found: {}. Inventory will be created on demand.", csvFilePath);
                return;
            }

            try (Reader reader = new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8)) {
                CsvToBean<InventoryCSVRecord> csvToBean = new CsvToBeanBuilder<InventoryCSVRecord>(reader)
                        .withType(InventoryCSVRecord.class)
                        .withIgnoreLeadingWhiteSpace(true)
                        .withSkipLines(0)
                        .build();

                List<InventoryCSVRecord> records = csvToBean.parse();
                
                int initializedCount = 0;
                for (InventoryCSVRecord record : records) {
                    String sku = record.getSku();
                    String name = record.getName() != null ? record.getName() : "Item " + sku;
                    int quantity = record.getQuantity() != null ? record.getQuantity() : 1000;
                    String temperatureZone = record.getTemperatureZone() != null && !record.getTemperatureZone().isEmpty()
                            ? record.getTemperatureZone() : "AMBIENT";
                    int lowStockThreshold = record.getLowStockThreshold() != null 
                            ? record.getLowStockThreshold() : defaultLowStockThreshold;
                    
                    inventoryManager.initializeInventoryFromCSV(sku, name, quantity, temperatureZone, lowStockThreshold);
                    initializedCount++;
                }
                
                log.info("Initialized inventory for {} SKUs from CSV file", initializedCount);
            }
        } catch (IOException e) {
            log.error("Failed to read inventory CSV file: {}", csvFilePath, e);
            log.info("Inventory will be created on demand when orders arrive");
        } catch (Exception e) {
            log.error("Failed to initialize inventory from CSV file: {}", csvFilePath, e);
            log.info("Inventory will be created on demand when orders arrive");
        }
    }
}
