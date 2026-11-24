package cl.duoc.barriodigital.audit.consumer;

import cl.duoc.barriodigital.audit.model.AuditEvent;
import cl.duoc.barriodigital.audit.repository.AuditEventRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Map;

@Component
public class AuditKafkaConsumer {

    private static final Logger log = LoggerFactory.getLogger(AuditKafkaConsumer.class);
    private final AuditEventRepository auditEventRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public AuditKafkaConsumer(AuditEventRepository auditEventRepository) {
        this.auditEventRepository = auditEventRepository;
    }

    @KafkaListener(topics = {"requests.events", "audit.timeline"}, groupId = "audit-consumer-group", autoStartup = "${spring.kafka.consumer.auto-startup:true}")
    public void consumeAuditEvent(String message) {
        try {
            log.info("--> [Kafka audit] Persistiendo evento de auditoria: {}", message);
            Map<?, ?> root = objectMapper.readValue(message, Map.class);
            String eventId = (String) root.get("eventId");
            String type = (String) root.get("type");

            Map<?, ?> payload = (Map<?, ?>) root.get("payload");
            if (payload != null) {
                Long requestId = payload.get("requestId") != null ? Long.valueOf(payload.get("requestId").toString()) : null;
                String trackingNumber = (String) payload.get("trackingNumber");
                String action = (String) payload.get("action");
                String actor = (String) payload.get("actor");
                String oldStatus = (String) payload.get("oldStatus");
                String newStatus = (String) payload.get("status");

                AuditEvent event = new AuditEvent(
                        null,
                        eventId != null ? eventId : "evt-" + System.currentTimeMillis(),
                        requestId,
                        trackingNumber,
                        action != null ? action : (type != null ? type : "UNKNOWN"),
                        actor != null ? actor : "system",
                        oldStatus,
                        newStatus,
                        LocalDateTime.now(),
                        message
                );
                auditEventRepository.save(event);
                log.info("✓ Evento de auditoria registrado en base de datos para trámite {}", trackingNumber);
            }
        } catch (Exception e) {
            log.warn("Error al procesar/persistir evento en auditoria: {}", e.getMessage());
        }
    }
}
