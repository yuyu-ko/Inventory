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
     * 初始化：从 CSV 文件加载订单（仅加载在模拟时间范围内的订单）
     */
    @PostConstruct
    public void initialize() {
        if (useCsv) {
            loadOrdersFromCSV();
        }
    }

    /**
     * 从 CSV 文件加载订单（仅加载在模拟时间范围内的订单）
     */
    private void loadOrdersFromCSV() {
        try {
            log.info("Loading orders from CSV file: {}", csvFilePath);
            List<OrderCSVRecord> csvRecords = csvReader.readOrdersFromCSV(csvFilePath);
            
            // 按 ORDER_ID 分组，将多行合并为一个订单
            Map<String, List<OrderCSVRecord>> ordersByOrderId = csvRecords.stream()
                    .collect(Collectors.groupingBy(OrderCSVRecord::getOrderId));
            
            // 转换为订单消息并过滤在模拟时间范围内的订单
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
     * 将 CSV 记录列表（同一订单的多行）转换为订单消息
     */
    private OrderReceivedMessage convertToOrderMessage(List<OrderCSVRecord> csvRecords) {
        if (csvRecords == null || csvRecords.isEmpty()) {
            throw new IllegalArgumentException("CSV records cannot be empty");
        }
        
        // 使用第一行获取订单基本信息
        OrderCSVRecord firstRecord = csvRecords.get(0);
        
        OrderReceivedMessage message = new OrderReceivedMessage();
        message.setOrderId(firstRecord.getOrderId());
        message.setOrderType(Order.OrderType.valueOf(firstRecord.getOrderType()));
        message.setOrderPlacedTime(LocalDateTime.parse(firstRecord.getOrderPlacedTime(), dateTimeFormatter));
        message.setOrderDueTime(LocalDateTime.parse(firstRecord.getOrderDueTime(), dateTimeFormatter));
        message.setCustomerId(firstRecord.getCustomerId());
        message.setSenderId("OrderInjector");
        
        // 将多行转换为订单项列表
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
     * 定时注入订单：根据模拟时钟时间发送订单
     */
    @Scheduled(fixedDelayString = "${inventory.simulation.tick-interval-ms:1000}")
    public void injectOrders() {
        if (!useCsv || !simulationClock.isRunning()) {
            return;
        }
        injectOrdersFromCSV();
    }

    /**
     * 从 CSV 队列注入订单（使用模拟时钟时间）
     */
    private void injectOrdersFromCSV() {
        LocalDateTime currentSimTime = simulationClock.getCurrentTime();
        
        // 发送所有到期的订单
        List<OrderReceivedMessage> ordersToSend = new ArrayList<>();
        List<OrderReceivedMessage> remainingOrders = new ArrayList<>();
        
        while (!orderQueue.isEmpty()) {
            OrderReceivedMessage order = orderQueue.poll();
            if (order != null) {
                // 如果订单的下单时间已到或已过（相对于模拟时间），则发送
                if (!order.getOrderPlacedTime().isAfter(currentSimTime)) {
                    ordersToSend.add(order);
                } else {
                    remainingOrders.add(order);
                }
            }
        }
        
        // 将未到期的订单放回队列
        orderQueue.addAll(remainingOrders);
        
        // 发送到期的订单
        for (OrderReceivedMessage order : ordersToSend) {
            publishOrder(order);
        }
    }

    /**
     * 发布订单到消息队列
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
     * 手动注入订单（用于测试）
     */
    public void injectOrder(OrderReceivedMessage order) {
        order.setSenderId("OrderInjector");
        publishOrder(order);
    }
}
