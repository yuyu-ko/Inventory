package com.inventory.model;

import com.opencsv.bean.CsvBindByName;
import lombok.Data;

@Data
public class InventoryCSVRecord {
    
    @CsvBindByName(column = "SKU", required = true)
    private String sku;
    
    @CsvBindByName(column = "NAME")
    private String name;
    
    @CsvBindByName(column = "QUANTITY", required = true)
    private Integer quantity;
    
    @CsvBindByName(column = "TEMPERATURE_ZONE")
    private String temperatureZone;
    
    @CsvBindByName(column = "LOW_STOCK_THRESHOLD")
    private Integer lowStockThreshold;
}
