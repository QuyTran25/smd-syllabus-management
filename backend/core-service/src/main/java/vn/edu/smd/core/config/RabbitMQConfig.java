package vn.edu.smd.core.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * RabbitMQ Configuration
 * Cấu hình exchanges, queues, bindings cho AI messaging
 */
@Configuration
public class RabbitMQConfig {
    
    // =============================================
    // CONSTANTS
    // =============================================
    
    // Exchanges
    public static final String EXCHANGE_DIRECT = "smd.direct";
    public static final String EXCHANGE_TOPIC = "smd.topic";
    
    // Queues
    public static final String QUEUE_AI_PROCESSING = "ai_processing_queue";
    public static final String QUEUE_AI_SUMMARIZE = "ai_summarize_queue";
    public static final String QUEUE_AI_RESULT = "ai_result_queue";
    public static final String QUEUE_NOTIFICATION = "notification_queue";
    
    // Routing Keys
    public static final String ROUTING_KEY_PROCESS = "ai.process";
    public static final String ROUTING_KEY_SUMMARIZE = "ai.summarize";
    public static final String ROUTING_KEY_RESULT = "ai.result";
    public static final String ROUTING_KEY_NOTIFICATION = "notification.*";
    
    // =============================================
    // EXCHANGES
    // =============================================
    
    @Bean
    public DirectExchange directExchange() {
        return new DirectExchange(EXCHANGE_DIRECT, true, false);
    }
    
    @Bean
    public TopicExchange topicExchange() {
        return new TopicExchange(EXCHANGE_TOPIC, true, false);
    }
    
    // =============================================
    // QUEUES
    // =============================================
    
    @Bean
    public Queue aiProcessingQueue() {
        return QueueBuilder.durable(QUEUE_AI_PROCESSING)
                .withArgument("x-max-priority", 5) // HIGH priority support
                .build();
    }
    
    @Bean
    public Queue aiSummarizeQueue() {
        return QueueBuilder.durable(QUEUE_AI_SUMMARIZE)
                .withArgument("x-max-priority", 3) // LOW priority
                .build();
    }
    
    @Bean
    public Queue aiResultQueue() {
        return QueueBuilder.durable(QUEUE_AI_RESULT).build();
    }
    
    @Bean
    public Queue notificationQueue() {
        return QueueBuilder.durable(QUEUE_NOTIFICATION).build();
    }
    
    // =============================================
    // BINDINGS
    // =============================================
    
    @Bean
    public Binding bindingProcessing() {
        return BindingBuilder
                .bind(aiProcessingQueue())
                .to(directExchange())
                .with(ROUTING_KEY_PROCESS);
    }
    
    @Bean
    public Binding bindingSummarize() {
        return BindingBuilder
                .bind(aiSummarizeQueue())
                .to(directExchange())
                .with(ROUTING_KEY_SUMMARIZE);
    }
    
    @Bean
    public Binding bindingResult() {
        return BindingBuilder
                .bind(aiResultQueue())
                .to(directExchange())
                .with(ROUTING_KEY_RESULT);
    }
    
    @Bean
    public Binding bindingNotification() {
        return BindingBuilder
                .bind(notificationQueue())
                .to(topicExchange())
                .with(ROUTING_KEY_NOTIFICATION);
    }
    
    // =============================================
    // MESSAGE CONVERTER & RABBIT TEMPLATE
    // =============================================
    
    /**
     * JSON message converter để serialize/deserialize DTOs
     * Cấu hình hỗ trợ Java 8 Date/Time API (Instant, LocalDateTime, etc.)
     */
    @Bean
    public Jackson2JsonMessageConverter messageConverter() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        return new Jackson2JsonMessageConverter(objectMapper);
    }
    
    /**
     * RabbitTemplate với JSON converter
     */
    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(messageConverter());
        return template;
    }
}
