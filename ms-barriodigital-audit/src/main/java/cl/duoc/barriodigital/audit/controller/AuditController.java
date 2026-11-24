package cl.duoc.barriodigital.audit.controller;

import cl.duoc.barriodigital.audit.model.AuditEvent;
import cl.duoc.barriodigital.audit.repository.AuditEventRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/audit")
public class AuditController {

    private final AuditEventRepository auditEventRepository;

    public AuditController(AuditEventRepository auditEventRepository) {
        this.auditEventRepository = auditEventRepository;
    }

    @GetMapping
    public ResponseEntity<List<AuditEvent>> getAuditEvents(@RequestParam(required = false) Long requestId,
                                                          @RequestParam(required = false) String trackingNumber,
                                                          @RequestParam(required = false) String actor) {
        if (requestId != null) {
            return ResponseEntity.ok(auditEventRepository.findByRequestIdOrderByTimestampDesc(requestId));
        } else if (trackingNumber != null) {
            return ResponseEntity.ok(auditEventRepository.findByTrackingNumberOrderByTimestampDesc(trackingNumber));
        } else if (actor != null) {
            return ResponseEntity.ok(auditEventRepository.findByActorContainingIgnoreCaseOrderByTimestampDesc(actor));
        }
        return ResponseEntity.ok(auditEventRepository.findAllByOrderByTimestampDesc());
    }

    @GetMapping("/request/{requestId}")
    public ResponseEntity<List<AuditEvent>> getByRequestId(@PathVariable Long requestId) {
        return ResponseEntity.ok(auditEventRepository.findByRequestIdOrderByTimestampDesc(requestId));
    }
}
