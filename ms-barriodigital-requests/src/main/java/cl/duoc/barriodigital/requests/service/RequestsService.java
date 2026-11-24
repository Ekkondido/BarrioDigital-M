package cl.duoc.barriodigital.requests.service;

import cl.duoc.barriodigital.requests.dto.CreateRequestDto;
import cl.duoc.barriodigital.requests.dto.EventEnvelope;
import cl.duoc.barriodigital.requests.dto.UpdateStatusDto;
import cl.duoc.barriodigital.requests.model.CitizenRequest;
import cl.duoc.barriodigital.requests.model.RequestStatus;
import cl.duoc.barriodigital.requests.repository.CitizenRequestRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class RequestsService {

    private static final Logger log = LoggerFactory.getLogger(RequestsService.class);
    private final CitizenRequestRepository repository;
    private final RabbitProducerService rabbitProducer;
    private final KafkaProducerService kafkaProducer;
    private final RestClient restClient;
    private final String catalogServiceUrl;
    private final AtomicLong counter = new AtomicLong(100);

    public RequestsService(CitizenRequestRepository repository,
                           RabbitProducerService rabbitProducer,
                           KafkaProducerService kafkaProducer,
                           RestClient restClient,
                           @Value("${services.catalog.url}") String catalogServiceUrl) {
        this.repository = repository;
        this.rabbitProducer = rabbitProducer;
        this.kafkaProducer = kafkaProducer;
        this.restClient = restClient;
        this.catalogServiceUrl = catalogServiceUrl;
    }

    public List<CitizenRequest> getRequests(String status, String citizenEmail) {
        if (status != null && citizenEmail != null) {
            return repository.findByStatusAndCitizenEmail(RequestStatus.fromString(status), citizenEmail);
        } else if (status != null) {
            return repository.findByStatus(RequestStatus.fromString(status));
        } else if (citizenEmail != null) {
            return repository.findByCitizenEmail(citizenEmail);
        }
        return repository.findAll();
    }

    public Optional<CitizenRequest> getById(Long id) {
        return repository.findById(id);
    }

    public Optional<CitizenRequest> getByTrackingNumber(String trackingNumber) {
        return repository.findByTrackingNumber(trackingNumber);
    }

    @Transactional
    public CitizenRequest createRequest(CreateRequestDto dto, String citizenEmail, String citizenName) {
        String email = (citizenEmail != null && !citizenEmail.isBlank()) ? citizenEmail : dto.getCitizenEmail();
        String name = (citizenName != null && !citizenName.isBlank()) ? citizenName : dto.getCitizenName();
        if (name == null || name.isBlank()) name = "Vecino Desconocido";

        CitizenRequest request = new CitizenRequest();
        long seq = counter.incrementAndGet();
        request.setTrackingNumber(String.format("REQ-2026-%04d", seq));
        request.setProcedureId(dto.getProcedureId());
        request.setProcedureName(dto.getProcedureName() != null ? dto.getProcedureName() : "Trámite #" + dto.getProcedureId());
        request.setCitizenEmail(email);
        request.setCitizenName(name);
        request.setCitizenPhone(dto.getCitizenPhone());
        request.setAddress(dto.getAddress());
        request.setDescription(dto.getDescription());
        request.setStatus(RequestStatus.INGRESADO);
        request.setCreatedAt(LocalDateTime.now());
        request.setUpdatedAt(LocalDateTime.now());

        CitizenRequest saved = repository.save(request);

        // Publicar eventos asincronos
        Map<String, Object> payload = buildPayload(saved, "CREATE", null, email);
        EventEnvelope<Map<String, Object>> envelope = EventEnvelope.of("REQUEST_CREATED", payload);

        rabbitProducer.sendEmailNotification(envelope);
        kafkaProducer.publishRequestEvent(saved.getTrackingNumber(), envelope);
        kafkaProducer.publishAuditTimeline(saved.getTrackingNumber(), envelope);

        return saved;
    }

    @Transactional
    public CitizenRequest updateStatus(Long id, UpdateStatusDto dto, String actorEmail) {
        CitizenRequest request = repository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Trámite no encontrado con id " + id));

        RequestStatus currentStatus = request.getStatus();
        RequestStatus newStatus = RequestStatus.fromString(dto.getStatus());

        // Regla clave: "No se puede pasar a EN_TERRENO sin ADMITIR"
        if (newStatus == RequestStatus.EN_TERRENO) {
            if (currentStatus != RequestStatus.ADMITIDO && currentStatus != RequestStatus.EN_GESTION) {
                throw new IllegalStateException("Regla de negocio infringida: No se puede pasar a EN_TERRENO sin antes haber sido ADMITIDO o puesto EN_GESTION (Estado actual: " + currentStatus + ")");
            }
        }

        // Regla clave: "El cupo diario disminuye al admitir el trámite"
        if (currentStatus == RequestStatus.INGRESADO && newStatus == RequestStatus.ADMITIDO) {
            decrementCatalogQuota(request.getProcedureId());
        }

        request.setStatus(newStatus);
        if (dto.getAssignedCrew() != null && !dto.getAssignedCrew().isBlank()) {
            request.setAssignedCrew(dto.getAssignedCrew());
        }
        if (dto.getResolutionNotes() != null && !dto.getResolutionNotes().isBlank()) {
            request.setResolutionNotes(dto.getResolutionNotes());
        }
        request.setUpdatedAt(LocalDateTime.now());

        CitizenRequest updated = repository.save(request);

        // Notificaciones y eventos
        Map<String, Object> payload = buildPayload(updated, "STATUS_CHANGE", currentStatus.name(), actorEmail);
        EventEnvelope<Map<String, Object>> envelope = EventEnvelope.of("REQUEST_STATUS_UPDATED", payload);

        // Enviar notificacion por email al vecino
        rabbitProducer.sendEmailNotification(envelope);

        // Si paso a EN_TERRENO, emitir ticket de cuadrilla
        if (newStatus == RequestStatus.EN_TERRENO) {
            rabbitProducer.sendCrewTicket(envelope);
        }

        // Si paso a RESUELTO, emitir certificado
        if (newStatus == RequestStatus.RESUELTO) {
            rabbitProducer.sendCertificateGeneration(envelope);
        }

        // Enviar evento de dominio y auditoria a Kafka
        kafkaProducer.publishRequestEvent(updated.getTrackingNumber(), envelope);
        kafkaProducer.publishAuditTimeline(updated.getTrackingNumber(), envelope);

        return updated;
    }

    private void decrementCatalogQuota(Long procedureId) {
        try {
            restClient.post()
                    .uri(catalogServiceUrl + "/api/catalog/procedures/" + procedureId + "/decrement-quota")
                    .retrieve()
                    .toBodilessEntity();
            log.info("Cupo de tramite #{} descontado exitosamente en catalog-service", procedureId);
        } catch (Exception e) {
            log.warn("Alerta: No se pudo descontar cupo en catalogo: {}", e.getMessage());
            // No bloquea si es modo prueba o dev
        }
    }

    private Map<String, Object> buildPayload(CitizenRequest req, String action, String oldStatus, String actor) {
        Map<String, Object> map = new HashMap<>();
        map.put("requestId", req.getId());
        map.put("trackingNumber", req.getTrackingNumber());
        map.put("procedureId", req.getProcedureId());
        map.put("procedureName", req.getProcedureName());
        map.put("citizenEmail", req.getCitizenEmail());
        map.put("citizenName", req.getCitizenName());
        map.put("status", req.getStatus().name());
        map.put("oldStatus", oldStatus);
        map.put("assignedCrew", req.getAssignedCrew());
        map.put("action", action);
        map.put("actor", actor != null ? actor : "sistema@barriodigital.cl");
        map.put("timestamp", LocalDateTime.now().toString());
        return map;
    }
}
