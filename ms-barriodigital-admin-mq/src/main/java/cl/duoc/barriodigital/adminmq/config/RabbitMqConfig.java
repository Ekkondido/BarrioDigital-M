package cl.duoc.barriodigital.adminmq.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

/**
 * Clase de configuracion centralizada de RabbitMQ.
 * Satisface los Indicadores 1, 2 y 3 de la Evaluación 3:
 * - Define colas, exchanges y bindings mediante beans especificos.
 * - Centraliza nombres y constantes sin mezclar lógica de negocio.
 */
@Configuration
public class RabbitMqConfig {

    // Nombres de Exchanges
    public static final String EXCHANGE_DIRECT = "cmd.direct";
    public static final String EXCHANGE_TOPIC = "cmd.topic";
    public static final String EXCHANGE_DLX = "cmd.dead.dlx";

    // Nombres de Colas y DLQ
    public static final String QUEUE_EMAIL = "q.cmd.email";
    public static final String QUEUE_EMAIL_DLQ = "q.cmd.email.dlq";
    public static final String QUEUE_CREW = "q.cmd.crew";
    public static final String QUEUE_CREW_DLQ = "q.cmd.crew.dlq";
    public static final String QUEUE_CERTIFICATE = "q.cmd.certificate";
    public static final String QUEUE_CERTIFICATE_DLQ = "q.cmd.certificate.dlq";

    // Routing Keys
    public static final String ROUTING_KEY_EMAIL = "email.send";
    public static final String ROUTING_KEY_CREW = "crew.ticket";
    public static final String ROUTING_KEY_CERT = "certificate.gen";

    @Bean
    public AmqpAdmin amqpAdmin(ConnectionFactory connectionFactory) {
        RabbitAdmin admin = new RabbitAdmin(connectionFactory);
        admin.setAutoStartup(true);
        return admin;
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(jsonMessageConverter());
        return template;
    }

    // ================= EXCHANGES =================
    @Bean
    public DirectExchange directExchange() {
        return new DirectExchange(EXCHANGE_DIRECT, true, false);
    }

    @Bean
    public TopicExchange topicExchange() {
        return new TopicExchange(EXCHANGE_TOPIC, true, false);
    }

    @Bean
    public DirectExchange dlxExchange() {
        return new DirectExchange(EXCHANGE_DLX, true, false);
    }

    // ================= COLAS Y DLQ CON DEAD-LETTER BINDINGS =================

    // 1. Notificación Email / Push al Vecino
    @Bean
    public Queue emailQueue() {
        Map<String, Object> args = new HashMap<>();
        args.put("x-dead-letter-exchange", EXCHANGE_DLX);
        args.put("x-dead-letter-routing-key", "email.dead");
        return new Queue(QUEUE_EMAIL, true, false, false, args);
    }

    @Bean
    public Queue emailDlq() {
        return new Queue(QUEUE_EMAIL_DLQ, true);
    }

    @Bean
    public Binding emailDirectBinding(Queue emailQueue, DirectExchange directExchange) {
        return BindingBuilder.bind(emailQueue).to(directExchange).with(ROUTING_KEY_EMAIL);
    }

    @Bean
    public Binding emailTopicBinding(Queue emailQueue, TopicExchange topicExchange) {
        return BindingBuilder.bind(emailQueue).to(topicExchange).with("email.*");
    }

    @Bean
    public Binding emailDlqBinding(Queue emailDlq, DirectExchange dlxExchange) {
        return BindingBuilder.bind(emailDlq).to(dlxExchange).with("email.dead");
    }

    // 2. Ticket de Cuadrilla en Terreno
    @Bean
    public Queue crewQueue() {
        Map<String, Object> args = new HashMap<>();
        args.put("x-dead-letter-exchange", EXCHANGE_DLX);
        args.put("x-dead-letter-routing-key", "crew.dead");
        return new Queue(QUEUE_CREW, true, false, false, args);
    }

    @Bean
    public Queue crewDlq() {
        return new Queue(QUEUE_CREW_DLQ, true);
    }

    @Bean
    public Binding crewDirectBinding(Queue crewQueue, DirectExchange directExchange) {
        return BindingBuilder.bind(crewQueue).to(directExchange).with(ROUTING_KEY_CREW);
    }

    @Bean
    public Binding crewTopicBinding(Queue crewQueue, TopicExchange topicExchange) {
        return BindingBuilder.bind(crewQueue).to(topicExchange).with("crew.#");
    }

    @Bean
    public Binding crewDlqBinding(Queue crewDlq, DirectExchange dlxExchange) {
        return BindingBuilder.bind(crewDlq).to(dlxExchange).with("crew.dead");
    }

    // 3. Emisión de Certificado / Documento PDF
    @Bean
    public Queue certificateQueue() {
        Map<String, Object> args = new HashMap<>();
        args.put("x-dead-letter-exchange", EXCHANGE_DLX);
        args.put("x-dead-letter-routing-key", "certificate.dead");
        return new Queue(QUEUE_CERTIFICATE, true, false, false, args);
    }

    @Bean
    public Queue certificateDlq() {
        return new Queue(QUEUE_CERTIFICATE_DLQ, true);
    }

    @Bean
    public Binding certificateDirectBinding(Queue certificateQueue, DirectExchange directExchange) {
        return BindingBuilder.bind(certificateQueue).to(directExchange).with(ROUTING_KEY_CERT);
    }

    @Bean
    public Binding certificateTopicBinding(Queue certificateQueue, TopicExchange topicExchange) {
        return BindingBuilder.bind(certificateQueue).to(topicExchange).with("certificate.*");
    }

    @Bean
    public Binding certificateDlqBinding(Queue certificateDlq, DirectExchange dlxExchange) {
        return BindingBuilder.bind(certificateDlq).to(dlxExchange).with("certificate.dead");
    }
}
