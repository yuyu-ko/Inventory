package com.inventory.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class RabbitMQConfig {

    @Value("${spring.rabbitmq.exchange.name:symbotic.simulation}")
    private String exchangeName;

    @Value("${spring.rabbitmq.topic.prefix:sim}")
    private String topicPrefix;

    // Exchange
    @Bean
    public TopicExchange simulationExchange() {
        return new TopicExchange(exchangeName);
    }

    // Queues
    @Bean
    public Queue orderReceivedQueue() {
        return QueueBuilder.durable(topicPrefix + ".order.received").build();
    }

    @Bean
    public Queue inventoryUpdateQueue() {
        return QueueBuilder.durable(topicPrefix + ".inventory.update").build();
    }

    @Bean
    public Queue orderProcessedQueue() {
        return QueueBuilder.durable(topicPrefix + ".order.processed").build();
    }

    // Bindings
    @Bean
    public Binding orderReceivedBinding() {
        return BindingBuilder
                .bind(orderReceivedQueue())
                .to(simulationExchange())
                .with(topicPrefix + ".order.received");
    }

    @Bean
    public Binding inventoryUpdateBinding() {
        return BindingBuilder
                .bind(inventoryUpdateQueue())
                .to(simulationExchange())
                .with(topicPrefix + ".inventory.update");
    }

    @Bean
    public Binding orderProcessedBinding() {
        return BindingBuilder
                .bind(orderProcessedQueue())
                .to(simulationExchange())
                .with(topicPrefix + ".order.processed");
    }

    // Message Converter with Java 8 Time support
    @Bean
    @Primary
    public MessageConverter jsonMessageConverter() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        
        Jackson2JsonMessageConverter converter = new Jackson2JsonMessageConverter(objectMapper);
        return converter;
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(jsonMessageConverter());
        
        // 添加连接监听器
        connectionFactory.addConnectionListener(connection -> {
            if (connection.isOpen()) {
                System.out.println("✅ RabbitMQ connection established");
            }
        });
        
        return template;
    }

    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(ConnectionFactory connectionFactory) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(jsonMessageConverter());
        factory.setPrefetchCount(800);
        return factory;
    }
}
