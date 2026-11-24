package cl.duoc.barriodigital.requests.service;

import cl.duoc.barriodigital.requests.config.RabbitMqConfig;
import cl.duoc.barriodigital.requests.dto.EventEnvelope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class RabbitProducerService {

    private static final Logger log = LoggerFactory.getLogger(RabbitProducerService.class);
    private final RabbitTemplate rabbitTemplate;

    public RabbitProducerService(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void sendEmailNotification(EventEnvelope<Map<String, Object>> envelope) {
        try {
            rabbitTemplate.convertAndSend(RabbitMqConfig.EXCHANGE_DIRECT, RabbitMqConfig.ROUTING_KEY_EMAIL, envelope);
            log.info("Comando de notificacion enviado a RabbitMQ (email.send): {}", envelope.getEventId());
        } catch (Exception e) {
            log.warn("No se pudo publicar a RabbitMQ [email]: {}", e.getMessage());
        }
    }

    public void sendCrewTicket(EventEnvelope<Map<String, Object>> envelope) {
        try {
            rabbitTemplate.convertAndSend(RabbitMqConfig.EXCHANGE_DIRECT, RabbitMqConfig.ROUTING_KEY_CREW, envelope);
            log.info("Comando de ticket cuadrilla enviado a RabbitMQ (crew.ticket): {}", envelope.getEventId());
        } catch (Exception e) {
            log.warn("No se pudo publicar a RabbitMQ [crew]: {}", e.getMessage());
        }
    }

    public void sendCertificateGeneration(EventEnvelope<Map<String, Object>> envelope) {
        try {
            rabbitTemplate.convertAndSend(RabbitMqConfig.EXCHANGE_DIRECT, RabbitMqConfig.ROUTING_KEY_CERT, envelope);
            log.info("Comando de certificado enviado a RabbitMQ (certificate.gen): {}", envelope.getEventId());
        } catch (Exception e) {
            log.warn("No se pudo publicar a RabbitMQ [cert]: {}", e.getMessage());
        }
    }
}
