package cl.duoc.barriodigital.requests.config;

import cl.duoc.barriodigital.requests.model.CitizenRequest;
import cl.duoc.barriodigital.requests.model.RequestStatus;
import cl.duoc.barriodigital.requests.repository.CitizenRequestRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.LocalDateTime;
import java.util.List;

@Configuration
public class DataInitializer {

    @Bean
    CommandLineRunner initRequests(CitizenRequestRepository repository) {
        return args -> {
            if (repository.count() == 0) {
                CitizenRequest req1 = new CitizenRequest();
                req1.setTrackingNumber("REQ-2026-0001");
                req1.setProcedureId(1L);
                req1.setProcedureName("Poda de Arboles en Via Publica");
                req1.setCitizenEmail("roberto.vecino@gmail.com");
                req1.setCitizenName("Roberto Salgado");
                req1.setCitizenPhone("+56911223344");
                req1.setAddress("Av. Las Condes 1234, pasaje 3");
                req1.setDescription("Ramas de eucalipto tocando cables del alumbrado público con riesgo de corte.");
                req1.setStatus(RequestStatus.ADMITIDO);
                req1.setCreatedAt(LocalDateTime.now().minusDays(2));
                req1.setUpdatedAt(LocalDateTime.now().minusDays(1));

                CitizenRequest req2 = new CitizenRequest();
                req2.setTrackingNumber("REQ-2026-0002");
                req2.setProcedureId(2L);
                req2.setProcedureName("Reparacion de Luminaria Publica");
                req2.setCitizenEmail("vecino@barriodigital.cl");
                req2.setCitizenName("Camila Morales");
                req2.setCitizenPhone("+56998877665");
                req2.setAddress("Calle Los Olmos 567");
                req2.setDescription("Foco led apagado hace 4 noches frente a la plaza comunal.");
                req2.setStatus(RequestStatus.EN_TERRENO);
                req2.setAssignedCrew("Cuadrilla Iluminacion Oriente");
                req2.setCreatedAt(LocalDateTime.now().minusDays(3));
                req2.setUpdatedAt(LocalDateTime.now().minusHours(4));

                CitizenRequest req3 = new CitizenRequest();
                req3.setTrackingNumber("REQ-2026-0003");
                req3.setProcedureId(3L);
                req3.setProcedureName("Bacheo y Reparacion de Calzadas");
                req3.setCitizenEmail("carlos.admin@barriodigital.cl");
                req3.setCitizenName("Carlos Mendoza");
                req3.setCitizenPhone("+56944556677");
                req3.setAddress("Interseccion San Martin con Esmeralda");
                req3.setDescription("Hoyo profundo en el asfalto que genera riesgo de pinchazo para vehiculos.");
                req3.setStatus(RequestStatus.INGRESADO);
                req3.setCreatedAt(LocalDateTime.now().minusHours(5));
                req3.setUpdatedAt(LocalDateTime.now().minusHours(5));

                repository.saveAll(List.of(req1, req2, req3));
            }
        };
    }
}
