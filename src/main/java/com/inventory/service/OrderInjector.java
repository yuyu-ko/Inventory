package com.inventory.service;

import com.inventory.message.OrderReceivedMessage;
import com.inventory.model.Order;
import com.inventory.model.OrderCSVRecord;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderInjector {

    private final RabbitTemplate rabbitTemplate;
    private final OrderCSVReader csvReader;
    private final SimulationClock simulationClock;

    @Value("${spring.rabbitmq.exchange.name:symbotic.simulation}")
    private String exchangeName;

    @Value("${spring.rabbitmq.topic.prefix:sim}")
    private String topicPrefix;

    @Value("${inventory.order-injector.csv-file:data/orders_sample.csv}")
    private String csvFilePath;

    @Value("${inventory.order-injector.use-csv:true}")
    private boolean useCsv;

    private final Queue<OrderReceivedMessage> orderQueue = new ConcurrentLinkedQueue<>();
    private final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    /**
     * Initialize: Load orders from CSV file (only load orders within simulation time range)
     */
    @PostConstruct
    public void initialize() {
        if (useCsv) {
            loadOrdersFromCSV();
        }
    }

    /**
     * Load orders from CSV file (only load orders within simulation time range)
     */
    private void loadOrdersFromCSV() {
        try {
            log.info("Loading orders from CSV file: {}", csvFilePath);
            List<OrderCSVRecord> csvRecords = csvReader.readOrdersFromCSV(csvFilePath);
            
            // Group by ORDER_ID, merge multiple rows into one order
            Map<String, List<OrderCSVRecord>> ordersByOrderId = csvRecords.stream()
                    .collect(Collectors.groupingBy(OrderCSVRecord::getOrderId));
            
            // Convert to order messages and filter orders within simulation time range
            List<OrderReceivedMessage> orders = ordersByOrderId.values().stream()
                    .map(this::convertToOrderMessage)
                    .filter(order -> simulationClock.isTimeInRange(order.getOrderPlacedTime()))
                    .sorted(Comparator.comparing(OrderReceivedMessage::getOrderPlacedTime))
                    .collect(Collectors.toList());
            
            orderQueue.addAll(orders);
            log.info("Loaded {} orders from CSV file (within simulation time range)", orders.size());
        } catch (Exception e) {
            log.error("Failed to load orders from CSV file: {}", csvFilePath, e);
        }
    }

    /**
     * Convert CSV record list (multiple rows for same order) to order message
     */
    private OrderReceivedMessage convertToOrderMessage(List<OrderCSVRecord> csvRecords) {
        if (csvRecords == null || csvRecords.isEmpty()) {
            throw new IllegalArgumentException("CSV records cannot be empty");
        }
        
        // Use first row to get basic order information
        OrderCSVRecord firstRecord = csvRecords.get(0);
        
        OrderReceivedMessage message = new OrderReceivedMessage();
        message.setOrderId(firstRecord.getOrderId());
        message.setOrderType(Order.OrderType.valueOf(firstRecord.getOrderType()));
        message.setOrderPlacedTime(LocalDateTime.parse(firstRecord.getOrderPlacedTime(), dateTimeFormatter));
        message.setOrderDueTime(LocalDateTime.parse(firstRecord.getOrderDueTime(), dateTimeFormatter));
        message.setCustomerId(firstRecord.getCustomerId());
        message.setSenderId("OrderInjector");
        
        // Convert multiple rows to order item list
        List<OrderReceivedMessage.OrderItemDTO> items = csvRecords.stream()
                .map(record -> {
                    OrderReceivedMessage.OrderItemDTO dto = new OrderReceivedMessage.OrderItemDTO();
                    dto.setSku(record.getSku());
                    dto.setQuantity(record.getQuantity());
                    dto.setTemperatureZone(
                        record.getTemperatureZone() != null && !record.getTemperatureZone().isEmpty()
                            ? record.getTemperatureZone()
                            : "AMBIENT"
                    );
                    return dto;
                })
                .collect(Collectors.toList());
        
        message.setItems(items);
        return message;
    }

    /**
     * Scheduled order injection: Send orders based on simulation clock time
     */
    @Scheduled(fixedDelayString = "${inventory.simulation.tick-interval-ms:1000}")
    public void injectOrders() {
        if (!useCsv || !simulationClock.isRunning()) {
            return;
        }
        injectOrdersFromCSV();
    }

    /**
     * Inject orders from CSV queue (using simulation clock time)
     */
    private void injectOrdersFromCSV() {
        LocalDateTime currentSimTime = simulationClock.getCurrentTime();
        
        // Send all due orders
        List<OrderReceivedMessage> ordersToSend = new ArrayList<>();
        List<OrderReceivedMessage> remainingOrders = new ArrayList<>();
        
        while (!orderQueue.isEmpty()) {
            OrderReceivedMessage order = orderQueue.poll();
            if (order != null) {
                // If order's placed time has arrived or passed (relative to simulation time), send it
                if (!order.getOrderPlacedTime().isAfter(currentSimTime)) {
                    ordersToSend.add(order);
                } else {
                    remainingOrders.add(order);
                }
            }
        }
        
        // Put non-due orders back to queue
        orderQueue.addAll(remainingOrders);
        
        // Send due orders
        for (OrderReceivedMessage order : ordersToSend) {
            publishOrder(order);
        }
    }

    /**
     * Publish order to message queue
     */
    private void publishOrder(OrderReceivedMessage order) {
        try {
            String routingKey = topicPrefix + ".order.received";
            rabbitTemplate.convertAndSend(exchangeName, routingKey, order);
            log.info("[{}] {} received", simulationClock.formatTime(simulationClock.getCurrentTime()), order.getOrderId());
        } catch (Exception e) {
            log.error("[{}] Failed to publish order {}", simulationClock.formatTime(simulationClock.getCurrentTime()), order.getOrderId(), e);
        }
    }

    /**
     * Manually inject order (for testing)
     */
    public void injectOrder(OrderReceivedMessage order) {
        order.setSenderId("OrderInjector");
        publishOrder(order);
    }
}
