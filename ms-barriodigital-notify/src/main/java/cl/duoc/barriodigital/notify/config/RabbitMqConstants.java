package cl.duoc.barriodigital.notify.config;

/**
 * Constantes centralizadas para nombres de colas y exchanges en el microservicio de notificaciones (Indicador 1).
 */
public final class RabbitMqConstants {

    private RabbitMqConstants() {}

    public static final String QUEUE_EMAIL = "q.cmd.email";
    public static final String QUEUE_EMAIL_DLQ = "q.cmd.email.dlq";

    public static final String QUEUE_CREW = "q.cmd.crew";
    public static final String QUEUE_CREW_DLQ = "q.cmd.crew.dlq";

    public static final String QUEUE_CERTIFICATE = "q.cmd.certificate";
    public static final String QUEUE_CERTIFICATE_DLQ = "q.cmd.certificate.dlq";

    public static final String EXCHANGE_DIRECT = "cmd.direct";
    public static final String EXCHANGE_TOPIC = "cmd.topic";
    public static final String EXCHANGE_DLX = "cmd.dead.dlx";
}
