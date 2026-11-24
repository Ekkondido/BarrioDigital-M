package cl.duoc.barriodigital.adminmq.controller;

import cl.duoc.barriodigital.adminmq.dto.BindingCreateDto;
import cl.duoc.barriodigital.adminmq.dto.ExchangeCreateDto;
import cl.duoc.barriodigital.adminmq.dto.QueueCreateDto;
import cl.duoc.barriodigital.adminmq.dto.QueueInfoDto;
import cl.duoc.barriodigital.adminmq.service.RabbitAdminService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Controlador REST para la administracion de RabbitMQ (Indicador 6).
 * Sigue buenas practicas REST con rutas claras, verbos HTTP y DTOs validados.
 */
@RestController
@RequestMapping("/api/admin/mq")
public class RabbitAdminController {

    private final RabbitAdminService adminService;

    public RabbitAdminController(RabbitAdminService adminService) {
        this.adminService = adminService;
    }

    // ================= ENDPOINTS DE COLAS =================

    @GetMapping("/queues")
    public ResponseEntity<List<QueueInfoDto>> listQueues() {
        return ResponseEntity.ok(adminService.listQueues());
    }

    @GetMapping("/queues/{name}")
    public ResponseEntity<QueueInfoDto> getQueue(@PathVariable String name) {
        return ResponseEntity.ok(adminService.getQueueInfo(name));
    }

    @PostMapping("/queues")
    public ResponseEntity<QueueInfoDto> createQueue(@Valid @RequestBody QueueCreateDto dto) {
        QueueInfoDto created = adminService.createQueue(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @DeleteMapping("/queues/{name}")
    public ResponseEntity<Map<String, Object>> deleteQueue(@PathVariable String name) {
        boolean deleted = adminService.deleteQueue(name);
        if (deleted) {
            return ResponseEntity.ok(Map.of("message", "Cola eliminada exitosamente", "queue", name));
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", "La cola no existe o no pudo ser eliminada", "queue", name));
        }
    }

    @PostMapping("/queues/{name}/purge")
    public ResponseEntity<Map<String, Object>> purgeQueue(@PathVariable String name) {
        boolean purged = adminService.purgeQueue(name);
        return ResponseEntity.ok(Map.of("message", "Cola purgada exitosamente", "success", purged));
    }

    // ================= ENDPOINTS DE EXCHANGES =================

    @GetMapping("/exchanges")
    public ResponseEntity<Set<String>> listExchanges() {
        return ResponseEntity.ok(adminService.listExchanges());
    }

    @PostMapping("/exchanges")
    public ResponseEntity<Map<String, Object>> createExchange(@Valid @RequestBody ExchangeCreateDto dto) {
        adminService.createExchange(dto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of("message", "Exchange creado exitosamente", "exchange", dto.getName(), "type", dto.getType()));
    }

    @DeleteMapping("/exchanges/{name}")
    public ResponseEntity<Map<String, Object>> deleteExchange(@PathVariable String name) {
        boolean deleted = adminService.deleteExchange(name);
        if (deleted) {
            return ResponseEntity.ok(Map.of("message", "Exchange eliminado exitosamente", "exchange", name));
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", "El exchange no existe o no pudo ser eliminado", "exchange", name));
        }
    }

    // ================= ENDPOINTS DE BINDINGS =================

    @PostMapping("/bindings")
    public ResponseEntity<Map<String, Object>> createBinding(@Valid @RequestBody BindingCreateDto dto) {
        adminService.createBinding(dto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of("message", "Binding creado exitosamente", "queue", dto.getQueueName(),
                        "exchange", dto.getExchangeName(), "routingKey", dto.getRoutingKey()));
    }

    @DeleteMapping("/bindings")
    public ResponseEntity<Map<String, Object>> deleteBinding(@Valid @RequestBody BindingCreateDto dto) {
        adminService.deleteBinding(dto);
        return ResponseEntity.ok(Map.of("message", "Binding eliminado exitosamente"));
    }

    // ================= ENDPOINTS DE PRUEBAS ASINCRONAS =================

    @PostMapping("/messages/test")
    public ResponseEntity<Map<String, Object>> sendTestMessage(@RequestBody Map<String, Object> body) {
        String exchange = (String) body.getOrDefault("exchange", "cmd.direct");
        String routingKey = (String) body.getOrDefault("routingKey", "email.send");
        @SuppressWarnings("unchecked")
        Map<String, Object> payload = (Map<String, Object>) body.getOrDefault("payload", Map.of("test", true));

        adminService.sendTestMessage(exchange, routingKey, payload);
        return ResponseEntity.ok(Map.of("message", "Mensaje de prueba enviado exitosamente",
                "exchange", exchange, "routingKey", routingKey));
    }
}
