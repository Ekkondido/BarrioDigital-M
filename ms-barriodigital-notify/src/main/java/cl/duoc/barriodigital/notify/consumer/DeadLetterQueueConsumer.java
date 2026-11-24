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
 * Consumidor para el monitoreo y trazabilidad de las Dead Letter Queues (DLQ) (Indicadores 4 y 5).
 * Registra formalmente en logs los mensajes que no pudieron ser entregados tras los fallos.
 */
@Component
public class DeadLetterQueueConsumer {

    private static final Logger log = LoggerFactory.getLogger(DeadLetterQueueConsumer.class);

    @RabbitListener(queues = {
            RabbitMqConstants.QUEUE_EMAIL_DLQ,
            RabbitMqConstants.QUEUE_CREW_DLQ,
            RabbitMqConstants.QUEUE_CERTIFICATE_DLQ
    }, ackMode = "MANUAL")
    public void processDeadLetterMessage(Message message, Channel channel) throws IOException {
        long deliveryTag = message.getMessageProperties().getDeliveryTag();
        String queue = message.getMessageProperties().getConsumerQueue();
        String body = new String(message.getBody(), StandardCharsets.UTF_8);

        // Registro exhaustivo en logs exigido por la pauta
        log.error("================================================================================");
        log.error("🚨 [ALERTA DLQ] MENSAJE NO ENTREGADO DETECTADO EN COLA MUERTA: {}", queue);
        log.error("   * DeliveryTag: {}", deliveryTag);
        log.error("   * Headers: {}", message.getMessageProperties().getHeaders());
        log.error("   * Payload fallido: {}", body);
        log.error("================================================================================");

        // Confirmar la recepcion en la DLQ para que no quede en bucle, asegurando su persistencia en logs
        channel.basicAck(deliveryTag, false);
    }
}
