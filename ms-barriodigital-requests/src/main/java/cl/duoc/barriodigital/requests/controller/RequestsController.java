package cl.duoc.barriodigital.requests.controller;

import cl.duoc.barriodigital.requests.dto.CreateRequestDto;
import cl.duoc.barriodigital.requests.dto.UpdateStatusDto;
import cl.duoc.barriodigital.requests.model.CitizenRequest;
import cl.duoc.barriodigital.requests.service.RequestsService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/requests")
public class RequestsController {

    private final RequestsService requestsService;

    public RequestsController(RequestsService requestsService) {
        this.requestsService = requestsService;
    }

    @GetMapping
    public ResponseEntity<List<CitizenRequest>> getRequests(@RequestParam(required = false) String status,
                                                           @RequestParam(required = false) String citizenEmail) {
        return ResponseEntity.ok(requestsService.getRequests(status, citizenEmail));
    }

    @GetMapping("/{id}")
    public ResponseEntity<CitizenRequest> getById(@PathVariable Long id) {
        return requestsService.getById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<CitizenRequest> createRequest(@Valid @RequestBody CreateRequestDto dto,
                                                        @RequestHeader(value = "X-User-Email", required = false) String userEmail,
                                                        @RequestHeader(value = "X-User-Name", required = false) String userName) {
        CitizenRequest created = requestsService.createRequest(dto, userEmail, userName);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<?> updateStatus(@PathVariable Long id,
                                          @Valid @RequestBody UpdateStatusDto dto,
                                          @RequestHeader(value = "X-Actor-Email", required = false) String actorEmail) {
        try {
            CitizenRequest updated = requestsService.updateStatus(id, dto, actorEmail);
            return ResponseEntity.ok(updated);
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "Bad Request", "message", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error interno", "message", e.getMessage()));
        }
    }
}
