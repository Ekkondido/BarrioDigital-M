package cl.duoc.barriodigital.report.consumer;

import cl.duoc.barriodigital.report.service.ReportService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class ReportEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(ReportEventConsumer.class);
    private final ReportService reportService;

    public ReportEventConsumer(ReportService reportService) {
        this.reportService = reportService;
    }

    @KafkaListener(topics = "requests.events", groupId = "report-consumer-group", autoStartup = "${spring.kafka.consumer.auto-startup:true}")
    public void consumeEvent(String message) {
        log.info("--> [Kafka requests.events] Mensaje recibido en report-svc: {}", message);
        reportService.processKafkaEvent(message);
    }
}
