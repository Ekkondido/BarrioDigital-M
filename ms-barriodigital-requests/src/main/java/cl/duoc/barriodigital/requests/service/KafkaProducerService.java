package cl.duoc.barriodigital.requests.service;

import cl.duoc.barriodigital.requests.dto.EventEnvelope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class KafkaProducerService {

    private static final Logger log = LoggerFactory.getLogger(KafkaProducerService.class);
    public static final String TOPIC_REQUESTS_EVENTS = "requests.events";
    public static final String TOPIC_AUDIT_TIMELINE = "audit.timeline";

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public KafkaProducerService(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void publishRequestEvent(String key, EventEnvelope<Map<String, Object>> envelope) {
        try {
            kafkaTemplate.send(TOPIC_REQUESTS_EVENTS, key, envelope);
            log.info("Evento de dominio publicado a Kafka [{}]: key={}, eventId={}", TOPIC_REQUESTS_EVENTS, key, envelope.getEventId());
        } catch (Exception e) {
            log.warn("No se pudo publicar a Kafka [{}]: {}", TOPIC_REQUESTS_EVENTS, e.getMessage());
        }
    }

    public void publishAuditTimeline(String key, EventEnvelope<Map<String, Object>> envelope) {
        try {
            kafkaTemplate.send(TOPIC_AUDIT_TIMELINE, key, envelope);
            log.info("Evento de auditoria publicado a Kafka [{}]: key={}, eventId={}", TOPIC_AUDIT_TIMELINE, key, envelope.getEventId());
        } catch (Exception e) {
            log.warn("No se pudo publicar a Kafka [{}]: {}", TOPIC_AUDIT_TIMELINE, e.getMessage());
        }
    }
}
