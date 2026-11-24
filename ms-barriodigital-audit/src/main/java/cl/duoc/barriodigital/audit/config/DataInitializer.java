package cl.duoc.barriodigital.audit.config;

import cl.duoc.barriodigital.audit.model.AuditEvent;
import cl.duoc.barriodigital.audit.repository.AuditEventRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Configuration
public class DataInitializer {

    @Bean
    CommandLineRunner initAuditData(AuditEventRepository repository) {
        return args -> {
            if (repository.count() == 0) {
                LocalDateTime now = LocalDateTime.now();
                repository.saveAll(List.of(
                    new AuditEvent(null, UUID.randomUUID().toString(), 1L, "REQ-2026-0001", "INGRESADO",
                            "carlos.vecino@gmail.com", null, "INGRESADO", now.minusHours(5),
                            "{\"channel\":\"web-portal\",\"ip\":\"190.161.45.12\"}"),
                    new AuditEvent(null, UUID.randomUUID().toString(), 1L, "REQ-2026-0001", "ADMITIDO",
                            "m.gonzalez@barriodigital.cl (Funcionario)", "INGRESADO", "ADMITIDO", now.minusHours(4),
                            "{\"notes\":\"Documentacion completa, cupo validado\"}"),
                    new AuditEvent(null, UUID.randomUUID().toString(), 1L, "REQ-2026-0001", "EN_TERRENO",
                            "m.gonzalez@barriodigital.cl (Funcionario)", "ADMITIDO", "EN_TERRENO", now.minusHours(2),
                            "{\"assignedCrew\":\"Cuadrilla #2 - Despeje Vial\"}"),
                    new AuditEvent(null, UUID.randomUUID().toString(), 2L, "REQ-2026-0002", "INGRESADO",
                            "ana.perez@vecinos.cl", null, "INGRESADO", now.minusHours(3),
                            "{\"channel\":\"web-portal\",\"ip\":\"186.105.88.2\"}")
                ));
            }
        };
    }
}
