package com.inventory.model;

import com.opencsv.bean.CsvBindByName;
import lombok.Data;

@Data
public class OrderCSVRecord {
    
    @CsvBindByName(column = "ORDER_ID", required = true)
    private String orderId;
    
    @CsvBindByName(column = "ORDER_TYPE", required = true)
    private String orderType;
    
    @CsvBindByName(column = "ORDER_PLACED_TIME", required = true)
    private String orderPlacedTime;
    
    @CsvBindByName(column = "ORDER_DUE_TIME", required = true)
    private String orderDueTime;
    
    @CsvBindByName(column = "CUSTOMER_ID", required = true)
    private String customerId;
    
    @CsvBindByName(column = "SKU", required = true)
    private String sku;
    
    @CsvBindByName(column = "QUANTITY", required = true)
    private Integer quantity;
    
    @CsvBindByName(column = "TEMPERATURE_ZONE")
    private String temperatureZone;
}
