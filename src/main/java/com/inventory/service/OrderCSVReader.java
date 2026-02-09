package com.inventory.service;

import com.inventory.model.OrderCSVRecord;
import com.opencsv.bean.CsvToBean;
import com.opencsv.bean.CsvToBeanBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Slf4j
@Service
public class OrderCSVReader {

    /**
     * Read orders from CSV file
     */
    public List<OrderCSVRecord> readOrdersFromCSV(String csvFilePath) {
        try {
            Resource resource = new ClassPathResource(csvFilePath);
            
            if (!resource.exists()) {
                log.error("CSV file not found: {}", csvFilePath);
                throw new IOException("CSV file not found: " + csvFilePath);
            }

            try (Reader reader = new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8)) {
                CsvToBean<OrderCSVRecord> csvToBean = new CsvToBeanBuilder<OrderCSVRecord>(reader)
                        .withType(OrderCSVRecord.class)
                        .withIgnoreLeadingWhiteSpace(true)
                        .withSkipLines(0) // Skip header row
                        .build();

                List<OrderCSVRecord> orders = csvToBean.parse();
                log.info("Successfully loaded {} orders from CSV file: {}", orders.size(), csvFilePath);
                return orders;
            }
        } catch (IOException e) {
            log.error("Error reading CSV file: {}", csvFilePath, e);
            throw new RuntimeException("Failed to read CSV file: " + csvFilePath, e);
        }
    }
}
