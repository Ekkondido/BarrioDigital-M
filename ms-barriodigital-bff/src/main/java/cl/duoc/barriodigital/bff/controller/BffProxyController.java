package cl.duoc.barriodigital.bff.controller;

import cl.duoc.barriodigital.bff.client.*;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class BffProxyController {

    private final CatalogClient catalogClient;
    private final RequestsClient requestsClient;
    private final ReportClient reportClient;
    private final AuditClient auditClient;
    private final AdminMqClient adminMqClient;

    public BffProxyController(CatalogClient catalogClient,
                              RequestsClient requestsClient,
                              ReportClient reportClient,
                              AuditClient auditClient,
                              AdminMqClient adminMqClient) {
        this.catalogClient = catalogClient;
        this.requestsClient = requestsClient;
        this.reportClient = reportClient;
        this.auditClient = auditClient;
        this.adminMqClient = adminMqClient;
    }

    // ================= CATÁLOGO =================
    @GetMapping("/catalog/procedures")
    public ResponseEntity<Object> getProcedures() {
        return ResponseEntity.ok(catalogClient.getProcedures());
    }

    @GetMapping("/catalog/procedures/{id}")
    public ResponseEntity<Object> getProcedureById(@PathVariable Long id) {
        return ResponseEntity.ok(catalogClient.getProcedureById(id));
    }

    @PostMapping("/catalog/procedures")
    public ResponseEntity<Object> createProcedure(@RequestBody Object body) {
        return ResponseEntity.ok(catalogClient.createProcedure(body));
    }

    @PutMapping("/catalog/procedures/{id}")
    public ResponseEntity<Object> updateProcedure(@PathVariable Long id, @RequestBody Object body) {
        return ResponseEntity.ok(catalogClient.updateProcedure(id, body));
    }

    // ================= TRÁMITES (REQUESTS) =================
    @GetMapping("/requests")
    public ResponseEntity<Object> getRequests(@RequestParam(required = false) String status,
                                             @RequestParam(required = false) String citizenEmail,
                                             @AuthenticationPrincipal Jwt jwt) {
        // Si el usuario es solo Vecino y no Admin/Funcionario, filtrar sus propios tramites
        boolean isCitizenOnly = jwt != null && 
                                jwt.getClaimAsStringList("roles") != null && 
                                jwt.getClaimAsStringList("roles").stream().anyMatch(r -> r.equalsIgnoreCase("Vecino")) &&
                                jwt.getClaimAsStringList("roles").stream().noneMatch(r -> r.equalsIgnoreCase("Admin") || r.equalsIgnoreCase("Funcionario"));
        
        if (isCitizenOnly && citizenEmail == null) {
            citizenEmail = jwt.getClaimAsString("preferred_username");
            if (citizenEmail == null) citizenEmail = jwt.getClaimAsString("email");
        }

        return ResponseEntity.ok(requestsClient.getRequests(status, citizenEmail));
    }

    @GetMapping("/requests/{id}")
    public ResponseEntity<Object> getRequestById(@PathVariable Long id) {
        return ResponseEntity.ok(requestsClient.getRequestById(id));
    }

    @PostMapping("/requests")
    public ResponseEntity<Object> createRequest(@RequestBody Object body, @AuthenticationPrincipal Jwt jwt) {
        String email = jwt != null ? (jwt.getClaimAsString("preferred_username") != null ? jwt.getClaimAsString("preferred_username") : jwt.getClaimAsString("email")) : "vecino@barriodigital.cl";
        String name = jwt != null ? jwt.getClaimAsString("name") : "Vecino Usuario";
        return ResponseEntity.ok(requestsClient.createRequest(body, email, name));
    }

    @PutMapping("/requests/{id}/status")
    public ResponseEntity<Object> updateStatus(@PathVariable Long id,
                                               @RequestBody Object body,
                                               @AuthenticationPrincipal Jwt jwt) {
        String actor = jwt != null ? (jwt.getClaimAsString("preferred_username") != null ? jwt.getClaimAsString("preferred_username") : jwt.getClaimAsString("email")) : "funcionario@barriodigital.cl";
        return ResponseEntity.ok(requestsClient.updateStatus(id, body, actor));
    }

    // ================= REPORTERÍA =================
    @GetMapping("/report/kpis")
    public ResponseEntity<Object> getKpis(@RequestParam(required = false, defaultValue = "last24h") String range) {
        return ResponseEntity.ok(reportClient.getKpis(range));
    }

    @GetMapping("/report/top-procedures")
    public ResponseEntity<Object> getTopProcedures(@RequestParam(required = false, defaultValue = "last7d") String range) {
        return ResponseEntity.ok(reportClient.getTopProcedures(range));
    }

    // ================= AUDITORÍA =================
    @GetMapping("/audit")
    public ResponseEntity<Object> getAuditTimeline(@RequestParam(required = false) Long requestId,
                                                  @RequestParam(required = false) String actor) {
        return ResponseEntity.ok(auditClient.getAuditTimeline(requestId, actor));
    }

    @GetMapping("/audit/request/{requestId}")
    public ResponseEntity<Object> getAuditByRequestId(@PathVariable Long requestId) {
        return ResponseEntity.ok(auditClient.getAuditByRequestId(requestId));
    }

    // ================= ADMINISTRADOR RABBITMQ (EVALUACIÓN 3) =================
    @GetMapping("/admin/mq/queues")
    public ResponseEntity<Object> listQueues() {
        return ResponseEntity.ok(adminMqClient.listQueues());
    }

    @PostMapping("/admin/mq/queues")
    public ResponseEntity<Object> createQueue(@RequestBody Object body) {
        return ResponseEntity.ok(adminMqClient.createQueue(body));
    }

    @DeleteMapping("/admin/mq/queues/{name}")
    public ResponseEntity<Object> deleteQueue(@PathVariable String name) {
        return ResponseEntity.ok(adminMqClient.deleteQueue(name));
    }

    @PostMapping("/admin/mq/queues/{name}/purge")
    public ResponseEntity<Object> purgeQueue(@PathVariable String name) {
        return ResponseEntity.ok(adminMqClient.purgeQueue(name));
    }

    @GetMapping("/admin/mq/exchanges")
    public ResponseEntity<Object> listExchanges() {
        return ResponseEntity.ok(adminMqClient.listExchanges());
    }

    @PostMapping("/admin/mq/exchanges")
    public ResponseEntity<Object> createExchange(@RequestBody Object body) {
        return ResponseEntity.ok(adminMqClient.createExchange(body));
    }

    @DeleteMapping("/admin/mq/exchanges/{name}")
    public ResponseEntity<Object> deleteExchange(@PathVariable String name) {
        return ResponseEntity.ok(adminMqClient.deleteExchange(name));
    }

    @PostMapping("/admin/mq/bindings")
    public ResponseEntity<Object> createBinding(@RequestBody Object body) {
        return ResponseEntity.ok(adminMqClient.createBinding(body));
    }

    @DeleteMapping("/admin/mq/bindings")
    public ResponseEntity<Object> deleteBinding(@RequestBody Object body) {
        return ResponseEntity.ok(adminMqClient.deleteBinding(body));
    }

    @PostMapping("/admin/mq/messages/test")
    public ResponseEntity<Object> sendTestMessage(@RequestBody Object body) {
        return ResponseEntity.ok(adminMqClient.sendTestMessage(body));
    }
}
