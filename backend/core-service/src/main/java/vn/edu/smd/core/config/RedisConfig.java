package vn.edu.smd.core.config;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.BasicPolymorphicTypeValidator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import vn.edu.smd.core.dto.TaskStatusDTO;

/**
 * Redis Configuration
 * 
 * Configures RedisTemplate with proper JSON serialization
 * to avoid serialization errors when storing/retrieving objects.
 * 
 * Key Features:
 * - Jackson2JsonRedisSerializer for type-safe JSON serialization
 * - Polymorphic type handling for Map<String, Object>
 * - String keys for readability in Redis CLI
 */
@Configuration
public class RedisConfig {
    
    /**
     * RedisTemplate for TaskStatusDTO
     * 
     * This template is specifically configured for AI task status caching.
     * Uses Jackson2JsonRedisSerializer to convert TaskStatusDTO to/from JSON.
     */
    @Bean
    public RedisTemplate<String, TaskStatusDTO> taskStatusRedisTemplate(
            RedisConnectionFactory connectionFactory) {
        
        RedisTemplate<String, TaskStatusDTO> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);
        
        // String serializer for keys (easier debugging in Redis CLI)
        StringRedisSerializer stringSerializer = new StringRedisSerializer();
        template.setKeySerializer(stringSerializer);
        template.setHashKeySerializer(stringSerializer);
        
        // Jackson JSON serializer for values
        ObjectMapper mapper = new ObjectMapper();
        
        // Enable polymorphic type handling for Map<String, Object>
        BasicPolymorphicTypeValidator validator = BasicPolymorphicTypeValidator.builder()
                .allowIfBaseType(Object.class)
                .build();
        
        mapper.activateDefaultTyping(
                validator,
                ObjectMapper.DefaultTyping.NON_FINAL,
                JsonTypeInfo.As.PROPERTY
        );
        
        // Use constructor-based serializer (non-deprecated)
        Jackson2JsonRedisSerializer<TaskStatusDTO> serializer = 
                new Jackson2JsonRedisSerializer<>(mapper, TaskStatusDTO.class);
        
        template.setValueSerializer(serializer);
        template.setHashValueSerializer(serializer);
        
        template.afterPropertiesSet();
        return template;
    }
    
    /**
     * Generic RedisTemplate for other use cases
     * 
     * This is a fallback template for non-TaskStatus caching needs.
     */
    @Bean
    public RedisTemplate<String, Object> redisTemplate(
            RedisConnectionFactory connectionFactory) {
        
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);
        
        // String serializer for keys
        StringRedisSerializer stringSerializer = new StringRedisSerializer();
        template.setKeySerializer(stringSerializer);
        template.setHashKeySerializer(stringSerializer);
        
        // Jackson JSON serializer for values
        ObjectMapper mapper = new ObjectMapper();
        
        BasicPolymorphicTypeValidator validator = BasicPolymorphicTypeValidator.builder()
                .allowIfBaseType(Object.class)
                .build();
        
        mapper.activateDefaultTyping(
                validator,
                ObjectMapper.DefaultTyping.NON_FINAL,
                JsonTypeInfo.As.PROPERTY
        );
        
        // Use constructor-based serializer (non-deprecated)
        Jackson2JsonRedisSerializer<Object> serializer = 
                new Jackson2JsonRedisSerializer<>(mapper, Object.class);
        
        template.setValueSerializer(serializer);
        template.setHashValueSerializer(serializer);
        
        template.afterPropertiesSet();
        return template;
    }
}
