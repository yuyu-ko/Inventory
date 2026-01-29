package com.inventory.service;

import com.inventory.message.InventoryUpdateMessage;
import com.inventory.message.OrderProcessedMessage;
import com.inventory.message.OrderReceivedMessage;
import com.inventory.model.Order;
import com.inventory.model.OrderItem;
import com.inventory.repository.OrderRepository;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderManager {

    private final OrderRepository orderRepository;
    private final InventoryManager inventoryManager;
    private final RabbitTemplate rabbitTemplate;
    private final SimulationClock simulationClock;
    private final MeterRegistry meterRegistry;

    @Value("${spring.rabbitmq.exchange.name:symbotic.simulation}")
    private String exchangeName;

    @Value("${spring.rabbitmq.topic.prefix:sim}")
    private String topicPrefix;

    /**
     * 监听订单接收消息
     */
    @RabbitListener(queues = "${spring.rabbitmq.topic.prefix:sim}.order.received")
    @Transactional
    public void handleOrderReceived(OrderReceivedMessage message) {
        meterRegistry.counter("orders_received_total").increment();
        Timer.Sample sample = Timer.start(meterRegistry);
        String currentTime = simulationClock.formatTime(simulationClock.getCurrentTime());

        // 記錄訂單接收日誌
        log.info("ORDER_RECEIVED | orderId={} | orderType={} | customerId={} | itemsCount={} | placedTime={} | dueTime={}", 
            message.getOrderId(), 
            message.getOrderType(),
            message.getCustomerId(),
            message.getItems().size(),
            message.getOrderPlacedTime(),
            message.getOrderDueTime());

        try {
            // 1. 创建订单实体
            Order order = createOrderFromMessage(message);
            order.setStatus(Order.OrderStatus.RECEIVED);
            order = orderRepository.save(order);

            // 2. 检查库存并预留
            boolean inventoryAvailable = checkAndReserveInventory(order, message.getItems());

            if (inventoryAvailable) {
                // 3. 处理订单
                processOrder(order);
                meterRegistry.counter("orders_processed_total", "status", "SUCCESS").increment();
            } else {
                // 库存不足，标记为失败
                order.setStatus(Order.OrderStatus.CANCELLED);
                orderRepository.save(order);
                
                OrderProcessedMessage processedMessage = new OrderProcessedMessage();
                processedMessage.setOrderId(order.getOrderId());
                processedMessage.setStatus("FAILED");
                processedMessage.setProcessedTime(simulationClock.getCurrentTime());
                processedMessage.setMessage("Insufficient inventory");
                publishOrderProcessed(processedMessage);

                meterRegistry.counter("orders_processed_total", "status", "FAILED").increment();
                
                // 記錄訂單失敗日誌（結構化格式）
                String itemsDetail = order.getItems().stream()
                    .map(item -> String.format("%s:%d", item.getSku(), item.getQuantity()))
                    .collect(Collectors.joining(","));
                log.warn("ORDER_FAILED | orderId={} | reason=INSUFFICIENT_INVENTORY | items=[{}] | time={}", 
                    order.getOrderId().toLowerCase(), 
                    itemsDetail,
                    currentTime);
            }

        } catch (Exception e) {
            log.error("ORDER_ERROR | orderId={} | error={} | time={}", 
                message.getOrderId(), 
                e.getMessage(),
                currentTime, 
                e);
            meterRegistry.counter("orders_processed_total", "status", "ERROR").increment();
        } finally {
            sample.stop(Timer.builder("orders_processing_time")
                    .description("Time taken to process an order end-to-end")
                    .register(meterRegistry));
        }
    }

    /**
     * 从消息创建订单实体
     */
    private Order createOrderFromMessage(OrderReceivedMessage message) {
        Order order = new Order();
        order.setOrderId(message.getOrderId());
        order.setOrderType(convertOrderType(message.getOrderType()));
        order.setOrderPlacedTime(message.getOrderPlacedTime());
        order.setOrderDueTime(message.getOrderDueTime());
        order.setCustomerId(message.getCustomerId());
        order.setStatus(Order.OrderStatus.PENDING);

        // 转换订单项
        List<OrderItem> items = message.getItems().stream()
            .map(item -> new OrderItem(
                item.getSku(),
                item.getQuantity(),
                item.getTemperatureZone()
            ))
            .collect(Collectors.toList());
        order.setItems(items);

        return order;
    }

    /**
     * 转换订单类型
     */
    private Order.OrderType convertOrderType(Order.OrderType type) {
        return type; // 已经是正确的类型
    }

    /**
     * 检查并预留库存
     */
    private boolean checkAndReserveInventory(Order order, List<OrderReceivedMessage.OrderItemDTO> items) {
        boolean allAvailable = true;

        for (OrderReceivedMessage.OrderItemDTO item : items) {
            // 检查库存（如果不存在会自动创建）
            var inventoryItem = inventoryManager.getInventory(item.getSku());
            if (inventoryItem.getAvailableQuantity() < item.getQuantity()) {
                log.warn("[{}] Insufficient inventory for SKU {} in order {}. Available: {}, Requested: {}", 
                    simulationClock.formatTime(simulationClock.getCurrentTime()),
                    item.getSku(), order.getOrderId(), 
                    inventoryItem.getAvailableQuantity(), item.getQuantity());
                allAvailable = false;
                break;
            }

            // 发送预留库存消息
            InventoryUpdateMessage updateMessage = new InventoryUpdateMessage();
            updateMessage.setSku(item.getSku());
            updateMessage.setReservedQuantityChange(item.getQuantity());
            updateMessage.setOperation("RESERVE");
            updateMessage.setOrderId(order.getOrderId());

            String routingKey = topicPrefix + ".inventory.update";
            rabbitTemplate.convertAndSend(exchangeName, routingKey, updateMessage);
        }

        return allAvailable;
    }

    /**
     * 处理订单
     */
    private void processOrder(Order order) {
        String currentTime = simulationClock.formatTime(simulationClock.getCurrentTime());
        
        // 更新订单状态
        order.setStatus(Order.OrderStatus.PROCESSING);
        orderRepository.save(order);
        
        log.info("ORDER_PROCESSING | orderId={} | status=PROCESSING | time={}", 
            order.getOrderId().toLowerCase(), currentTime);

        // 扣除库存
        for (OrderItem item : order.getItems()) {
            InventoryUpdateMessage updateMessage = new InventoryUpdateMessage();
            updateMessage.setSku(item.getSku());
            updateMessage.setQuantityChange(item.getQuantity());
            updateMessage.setOperation("DEDUCT");
            updateMessage.setOrderId(order.getOrderId());

            String routingKey = topicPrefix + ".inventory.update";
            rabbitTemplate.convertAndSend(exchangeName, routingKey, updateMessage);
            
            log.debug("ORDER_INVENTORY_DEDUCT | orderId={} | sku={} | quantity={} | zone={}", 
                order.getOrderId().toLowerCase(),
                item.getSku(),
                item.getQuantity(),
                item.getTemperatureZone());
        }

        // 完成订单
        order.setStatus(Order.OrderStatus.COMPLETED);
        orderRepository.save(order);

        // 发布订单处理完成消息
        OrderProcessedMessage processedMessage = new OrderProcessedMessage();
        processedMessage.setOrderId(order.getOrderId());
        processedMessage.setStatus("COMPLETED");
        processedMessage.setProcessedTime(simulationClock.getCurrentTime());
        processedMessage.setMessage("Order processed successfully");
        publishOrderProcessed(processedMessage);

        // 輸出結構化訂單處理完成日誌
        String itemsDetail = order.getItems().stream()
            .map(item -> String.format("%s:%d", item.getSku(), item.getQuantity()))
            .collect(Collectors.joining(","));
        log.info("ORDER_COMPLETED | orderId={} | orderType={} | customerId={} | items=[{}] | status=COMPLETED | time={}", 
            order.getOrderId().toLowerCase(),
            order.getOrderType(),
            order.getCustomerId(),
            itemsDetail,
            currentTime);
    }

    /**
     * 发布订单处理消息
     */
    private void publishOrderProcessed(OrderProcessedMessage message) {
        try {
            String routingKey = topicPrefix + ".order.processed";
            rabbitTemplate.convertAndSend(exchangeName, routingKey, message);
            log.debug("ORDER_PROCESSED_PUBLISHED | orderId={} | status={} | routingKey={}", 
                message.getOrderId(),
                message.getStatus(),
                routingKey);
        } catch (Exception e) {
            log.error("ORDER_PROCESSED_PUBLISH_FAILED | orderId={} | error={} | time={}", 
                message.getOrderId(), 
                e.getMessage(),
                simulationClock.formatTime(simulationClock.getCurrentTime()), 
                e);
        }
    }

    /**
     * 查询订单
     */
    public Order getOrder(String orderId) {
        return orderRepository.findByOrderId(orderId).orElse(null);
    }

    /**
     * 获取所有订单
     */
    public List<Order> getAllOrders() {
        return orderRepository.findAll();
    }
}
