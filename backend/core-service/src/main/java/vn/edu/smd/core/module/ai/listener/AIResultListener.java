package vn.edu.smd.core.module.ai.listener;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import vn.edu.smd.core.module.ai.service.AITaskService;

import java.util.Map;

/**
 * AI Result Listener
 * Lắng nghe kết quả từ AI Worker trên queue ai_result_queue
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class AIResultListener {
    
    private final AITaskService aiTaskService;
    
    /**
     * Nhận kết quả AI từ queue
     * 
     * @param message Result message từ AI Worker
     */
    @RabbitListener(queues = "ai_result_queue")
    public void receiveAIResult(Map<String, Object> message) {
        try {
            String messageId = (String) message.get("messageId");
            String action = (String) message.get("action");
            String status = (String) message.get("status");
            Integer progress = (Integer) message.get("progress");
            @SuppressWarnings("unchecked")
            Map<String, Object> result = (Map<String, Object>) message.get("result");
            String errorMessage = (String) message.get("errorMessage");
            Integer processingTimeMs = (Integer) message.get("processingTimeMs");
            
            log.info("📥 Received AI result: messageId={}, action={}, status={}", 
                     messageId, action, status);
            
            if ("SUCCESS".equals(status)) {
                log.info("✅ AI task completed successfully in {}ms", processingTimeMs);
                aiTaskService.updateTaskStatus(messageId, status, progress, result, null);
            } else if ("ERROR".equals(status)) {
                log.error("❌ AI task failed: {}", errorMessage);
                aiTaskService.updateTaskStatus(messageId, status, 0, null, errorMessage);
            } else {
                log.warn("⚠️ Unknown status: {}", status);
            }
            
        } catch (Exception e) {
            log.error("❌ Error processing AI result: {}", e.getMessage(), e);
        }
    }
}
