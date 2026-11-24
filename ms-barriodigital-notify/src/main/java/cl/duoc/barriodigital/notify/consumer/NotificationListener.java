package cl.duoc.barriodigital.notify.consumer;

/**
 * Los consumidores han sido modularizados por dominio funcional en clases separadas (Indicador 4):
 * - CitizenNotificationConsumer (q.cmd.email)
 * - FieldCrewConsumer (q.cmd.crew)
 * - CertificateDocumentConsumer (q.cmd.certificate)
 * - DeadLetterQueueConsumer (q.cmd.*.dlq)
 */
public class NotificationListener {
    // Modularizado en clases específicas por dominio
}
