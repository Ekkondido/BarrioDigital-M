package cl.duoc.barriodigital.adminmq.service;

import cl.duoc.barriodigital.adminmq.config.RabbitMqConfig;
import cl.duoc.barriodigital.adminmq.dto.BindingCreateDto;
import cl.duoc.barriodigital.adminmq.dto.ExchangeCreateDto;
import cl.duoc.barriodigital.adminmq.dto.QueueCreateDto;
import cl.duoc.barriodigital.adminmq.dto.QueueInfoDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Exchange;
import org.springframework.amqp.core.FanoutExchange;
import org.springframework.amqp.core.HeadersExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Servicio dedicado para encapsular toda la administracion de RabbitMQ (Indicador 7).
 * Los controladores consumen estos metodos de alto nivel sin interactuar
 * directamente con la infraestructura interna de Spring AMQP.
 */
@Service
public class RabbitAdminService {

    private static final Logger log = LoggerFactory.getLogger(RabbitAdminService.class);
    private final AmqpAdmin amqpAdmin;
    private final RabbitTemplate rabbitTemplate;

    // Registro en memoria de colas y exchanges gestionados
    private final Set<String> registeredQueues = ConcurrentHashMap.newKeySet();
    private final Set<String> registeredExchanges = ConcurrentHashMap.newKeySet();

    public RabbitAdminService(AmqpAdmin amqpAdmin, RabbitTemplate rabbitTemplate) {
        this.amqpAdmin = amqpAdmin;
        this.rabbitTemplate = rabbitTemplate;

        // Registrar colas del caso BarrioDigital por defecto
        registeredQueues.addAll(List.of(
                RabbitMqConfig.QUEUE_EMAIL,
                RabbitMqConfig.QUEUE_EMAIL_DLQ,
                RabbitMqConfig.QUEUE_CREW,
                RabbitMqConfig.QUEUE_CREW_DLQ,
                RabbitMqConfig.QUEUE_CERTIFICATE,
                RabbitMqConfig.QUEUE_CERTIFICATE_DLQ
        ));

        registeredExchanges.addAll(List.of(
                RabbitMqConfig.EXCHANGE_DIRECT,
                RabbitMqConfig.EXCHANGE_TOPIC,
                RabbitMqConfig.EXCHANGE_DLX
        ));
    }

    // ================= GESTION DE COLAS =================

    public QueueInfoDto createQueue(QueueCreateDto dto) {
        log.info("Creando cola dinamica en RabbitMQ: {}", dto.getName());

        Map<String, Object> args = dto.toArgumentsMap();
        Queue queue = new Queue(dto.getName().trim(), dto.isDurable(), dto.isExclusive(), dto.isAutoDelete(), args);
        amqpAdmin.declareQueue(queue);
        registeredQueues.add(queue.getName());

        return getQueueInfo(queue.getName());
    }

    public boolean deleteQueue(String queueName) {
        log.warn("Eliminando cola de RabbitMQ: {}", queueName);
        boolean deleted = amqpAdmin.deleteQueue(queueName.trim());
        if (deleted) {
            registeredQueues.remove(queueName.trim());
        }
        return deleted;
    }

    public boolean purgeQueue(String queueName) {
        log.info("Purgando mensajes acumulados en cola: {}", queueName);
        try {
            amqpAdmin.purgeQueue(queueName.trim(), false);
            return true;
        } catch (Exception e) {
            log.error("Error al purgar cola {}: {}", queueName, e.getMessage());
            return false;
        }
    }

    public List<QueueInfoDto> listQueues() {
        List<QueueInfoDto> list = new ArrayList<>();
        for (String qName : registeredQueues) {
            list.add(getQueueInfo(qName));
        }
        return list;
    }

    public QueueInfoDto getQueueInfo(String queueName) {
        Properties props = amqpAdmin.getQueueProperties(queueName);
        int messageCount = 0;
        int consumerCount = 0;

        if (props != null) {
            Object msgObj = props.get(RabbitAdmin.QUEUE_MESSAGE_COUNT);
            if (msgObj instanceof Number) {
                messageCount = ((Number) msgObj).intValue();
            }
            Object consumerObj = props.get(RabbitAdmin.QUEUE_CONSUMER_COUNT);
            if (consumerObj instanceof Number) {
                consumerCount = ((Number) consumerObj).intValue();
            }
        }

        boolean isDlq = queueName.endsWith(".dlq") || queueName.contains("dead");
        return new QueueInfoDto(queueName, messageCount, consumerCount, true, false, isDlq, Map.of());
    }

    // ================= GESTION DE EXCHANGES =================

    public void createExchange(ExchangeCreateDto dto) {
        String name = dto.getName().trim();
        String type = dto.getType().trim().toLowerCase();
        log.info("Creando exchange dinamico en RabbitMQ: name={}, type={}", name, type);

        Exchange exchange;
        switch (type) {
            case "topic":
                exchange = new TopicExchange(name, dto.isDurable(), dto.isAutoDelete());
                break;
            case "fanout":
                exchange = new FanoutExchange(name, dto.isDurable(), dto.isAutoDelete());
                break;
            case "headers":
                exchange = new HeadersExchange(name, dto.isDurable(), dto.isAutoDelete());
                break;
            case "direct":
            default:
                exchange = new DirectExchange(name, dto.isDurable(), dto.isAutoDelete());
                break;
        }

        amqpAdmin.declareExchange(exchange);
        registeredExchanges.add(name);
    }

    public boolean deleteExchange(String exchangeName) {
        log.warn("Eliminando exchange de RabbitMQ: {}", exchangeName);
        boolean deleted = amqpAdmin.deleteExchange(exchangeName.trim());
        if (deleted) {
            registeredExchanges.remove(exchangeName.trim());
        }
        return deleted;
    }

    public Set<String> listExchanges() {
        return Collections.unmodifiableSet(registeredExchanges);
    }

    // ================= GESTION DE BINDINGS =================

    public void createBinding(BindingCreateDto dto) {
        log.info("Creando binding: cola={}, exchange={}, routingKey={}", 
                dto.getQueueName(), dto.getExchangeName(), dto.getRoutingKey());

        Binding binding = new Binding(
                dto.getQueueName().trim(),
                Binding.DestinationType.QUEUE,
                dto.getExchangeName().trim(),
                dto.getRoutingKey().trim(),
                null
        );

        amqpAdmin.declareBinding(binding);
    }

    public void deleteBinding(BindingCreateDto dto) {
        log.warn("Eliminando binding: cola={}, exchange={}, routingKey={}", 
                dto.getQueueName(), dto.getExchangeName(), dto.getRoutingKey());

        Binding binding = new Binding(
                dto.getQueueName().trim(),
                Binding.DestinationType.QUEUE,
                dto.getExchangeName().trim(),
                dto.getRoutingKey().trim(),
                null
        );

        amqpAdmin.removeBinding(binding);
    }

    // ================= ENVIO DE MENSAJES DE PRUEBA =================

    public void sendTestMessage(String exchange, String routingKey, Map<String, Object> payload) {
        log.info("Publicando mensaje de prueba: exchange={}, routingKey={}", exchange, routingKey);
        rabbitTemplate.convertAndSend(exchange, routingKey, payload);
    }
}
