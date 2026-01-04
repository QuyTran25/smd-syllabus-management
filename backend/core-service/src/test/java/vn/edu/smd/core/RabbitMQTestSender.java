package vn.edu.smd.core;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@SpringBootApplication
public class RabbitMQTestSender {

    public static void main(String[] args) {
        SpringApplication.run(RabbitMQTestSender.class, args);
    }

    @Bean
    CommandLineRunner sendTestMessage(RabbitTemplate rabbitTemplate) {
        return args -> {
            System.out.println("🚀 Sending test message to RabbitMQ...");
            
            Map<String, Object> message = new HashMap<>();
            message.put("messageId", UUID.randomUUID().toString());
            message.put("action", "MAP_CLO_PLO");
            message.put("priority", "HIGH");
            message.put("timestamp", System.currentTimeMillis());
            message.put("userId", "test-user-123");
            
            Map<String, Object> payload = new HashMap<>();
            payload.put("syllabusId", UUID.randomUUID().toString());
            payload.put("curriculumId", UUID.randomUUID().toString());
            message.put("payload", payload);
            
            rabbitTemplate.convertAndSend(
                "smd.direct",
                "ai.process",
                message,
                msg -> {
                    msg.getMessageProperties().setPriority(5);
                    return msg;
                }
            );
            
            System.out.println("✅ Message sent successfully!");
            System.out.println("📦 Message: " + message);
            System.out.println("\n⏳ Check Python worker logs for processing...");
            
            Thread.sleep(5000); // Wait for processing
            System.exit(0);
        };
    }
}
