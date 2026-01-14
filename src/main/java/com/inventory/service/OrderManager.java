package com.inventory.service;

import com.inventory.message.InventoryUpdateMessage;
import com.inventory.message.OrderProcessedMessage;
import com.inventory.message.OrderReceivedMessage;
import com.inventory.model.Order;
import com.inventory.model.OrderItem;
import com.inventory.repository.OrderRepository;
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
                
                log.info("[{}] {} failed - insufficient inventory", 
                    simulationClock.formatTime(simulationClock.getCurrentTime()), 
                    order.getOrderId().toLowerCase());
            }

        } catch (Exception e) {
            log.error("[{}] Error processing order {}", 
                simulationClock.formatTime(simulationClock.getCurrentTime()), 
                message.getOrderId(), e);
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
        // 更新订单状态
        order.setStatus(Order.OrderStatus.PROCESSING);
        orderRepository.save(order);

        // 扣除库存
        for (OrderItem item : order.getItems()) {
            InventoryUpdateMessage updateMessage = new InventoryUpdateMessage();
            updateMessage.setSku(item.getSku());
            updateMessage.setQuantityChange(item.getQuantity());
            updateMessage.setOperation("DEDUCT");
            updateMessage.setOrderId(order.getOrderId());

            String routingKey = topicPrefix + ".inventory.update";
            rabbitTemplate.convertAndSend(exchangeName, routingKey, updateMessage);
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

        // 输出格式化的日志
        log.info("[{}] {} completed successfully", 
            simulationClock.formatTime(simulationClock.getCurrentTime()), 
            order.getOrderId().toLowerCase());
    }

    /**
     * 发布订单处理消息
     */
    private void publishOrderProcessed(OrderProcessedMessage message) {
        try {
            String routingKey = topicPrefix + ".order.processed";
            rabbitTemplate.convertAndSend(exchangeName, routingKey, message);
        } catch (Exception e) {
            log.error("[{}] Failed to publish order processed message for {}", 
                simulationClock.formatTime(simulationClock.getCurrentTime()), 
                message.getOrderId(), e);
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
