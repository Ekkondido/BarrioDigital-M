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
 * Consumidor para el dominio funcional de Cuadrillas y Operaciones en Terreno (Indicador 4).
 * Maneja confirmaciones explicitas (ACK) y desvios controlados a DLQ (NACK) (Indicador 5).
 */
@Component
public class FieldCrewConsumer {

    private static final Logger log = LoggerFactory.getLogger(FieldCrewConsumer.class);

    @RabbitListener(queues = RabbitMqConstants.QUEUE_CREW, ackMode = "MANUAL")
    public void consumeCrewTicket(Message message, Channel channel) throws IOException {
        long deliveryTag = message.getMessageProperties().getDeliveryTag();
        String body = new String(message.getBody(), StandardCharsets.UTF_8);

        log.info("[DOMINIO CUADRILLA] Recibido ticket de terreno en {}: deliveryTag={}", RabbitMqConstants.QUEUE_CREW, deliveryTag);

        try {
            if (body.contains("\"simulate_failure\":true")) {
                throw new IllegalStateException("Simulación forzada de fallo en despacho de cuadrilla.");
            }

            // Simulación de asignación y generación de orden de trabajo
            log.info("✓ [CUADRILLA] Orden de visita y ticket emitidos con éxito para la cuadrilla en terreno. Mensaje: {}", body);

            // Confirmación explícita (ACK)
            channel.basicAck(deliveryTag, false);
            log.debug("ACK confirmado para ticket cuadrilla deliveryTag={}", deliveryTag);

        } catch (Exception ex) {
            log.error("✗ [ERROR CUADRILLA] No se pudo despachar el ticket. Enviando a DLQ: {}", ex.getMessage());
            // NACK explícito hacia DLQ
            channel.basicNack(deliveryTag, false, false);
        }
    }
}
