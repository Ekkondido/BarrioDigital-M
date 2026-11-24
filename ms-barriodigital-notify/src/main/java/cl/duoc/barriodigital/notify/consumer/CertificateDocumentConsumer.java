package cl.duoc.barriodigital.notify.consumer;

import cl.duoc.barriodigital.notify.config.RabbitMqConstants;
import com.rabbitmq.client.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * Consumidor para el dominio funcional de Documentos y Certificados Comunales (Indicador 4).
 * Maneja confirmaciones explicitas (ACK) y desvios controlados a DLQ (NACK) (Indicador 5).
 */
@Component
public class CertificateDocumentConsumer {

    private static final Logger log = LoggerFactory.getLogger(CertificateDocumentConsumer.class);

    @RabbitListener(queues = RabbitMqConstants.QUEUE_CERTIFICATE, ackMode = "MANUAL")
    public void consumeCertificateTask(Message message, Channel channel) throws IOException {
        long deliveryTag = message.getMessageProperties().getDeliveryTag();
        String body = new String(message.getBody(), StandardCharsets.UTF_8);

        log.info("[DOMINIO CERTIFICADOS] Recibida orden de certificado en {}: deliveryTag={}", 
                RabbitMqConstants.QUEUE_CERTIFICATE, deliveryTag);

        try {
            if (body.contains("\"simulate_failure\":true")) {
                throw new IllegalStateException("Simulación forzada de fallo en emisión de certificado digital.");
            }

            // Simulación de renderizado de comprobante PDF oficial
            log.info("✓ [CERTIFICADO] Documento oficial PDF generado con firma comunal. Contenido: {}", body);

            // Confirmación explícita (ACK)
            channel.basicAck(deliveryTag, false);
            log.debug("ACK confirmado para certificado deliveryTag={}", deliveryTag);

        } catch (Exception ex) {
            log.error("✗ [ERROR CERTIFICADO] Error generando documento digital. Derivando a DLQ: {}", ex.getMessage());
            channel.basicNack(deliveryTag, false, false);
        }
    }
}
