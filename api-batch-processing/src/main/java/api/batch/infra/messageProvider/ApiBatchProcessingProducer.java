package api.batch.infra.messageProvider;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class ApiBatchProcessingProducer {

    private final KafkaTemplate<String, String> kafkaTemplate;

    @Value("${kafka.topic.name}")
    private String TOPIC_NAME;

    public ApiBatchProcessingProducer(KafkaTemplate<String, String> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    private void sendMessage(String message) {
        kafkaTemplate.send(TOPIC_NAME, message);
        log.info("Sending Message: ");
    }
}
