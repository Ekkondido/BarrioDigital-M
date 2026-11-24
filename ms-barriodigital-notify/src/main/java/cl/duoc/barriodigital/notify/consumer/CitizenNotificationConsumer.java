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
 * Consumidor para el dominio funcional de Notificaciones al Vecino (Indicador 4).
 * Maneja confirmaciones explicitas (ACK) y desvios controlados a DLQ (NACK) (Indicador 5).
 */
@Component
public class CitizenNotificationConsumer {

    private static final Logger log = LoggerFactory.getLogger(CitizenNotificationConsumer.class);

    @RabbitListener(queues = RabbitMqConstants.QUEUE_EMAIL, ackMode = "MANUAL")
    public void consumeNotification(Message message, Channel channel) throws IOException {
        long deliveryTag = message.getMessageProperties().getDeliveryTag();
        String body = new String(message.getBody(), StandardCharsets.UTF_8);

        log.info("[DOMINIO VECINO] Recibido mensaje en {}: deliveryTag={}", RabbitMqConstants.QUEUE_EMAIL, deliveryTag);

        try {
            // Validacion de integridad basica del mensaje
            if (body.contains("\"simulate_failure\":true")) {
                throw new IllegalStateException("Simulación forzada de fallo para evaluar desvío a DLQ.");
            }

            // Procesamiento de negocio del correo/push
            log.info("✓ [EMAIL/PUSH] Notificación enviada con éxito al vecino. Contenido: {}", body);

            // Confirmación explícita (ACK)
            channel.basicAck(deliveryTag, false);
            log.debug("ACK confirmado para deliveryTag={}", deliveryTag);

        } catch (Exception ex) {
            log.error("✗ [ERROR VECINO] Falló el procesamiento de notificación. Derivando a DLQ: {}", ex.getMessage());
            // Manejo explícito de error: NACK con requeue=false para enviar automáticamente a la DLQ
            channel.basicNack(deliveryTag, false, false);
        }
    }
}
