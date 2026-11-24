package cl.duoc.barriodigital.catalog.controller;

import cl.duoc.barriodigital.catalog.model.ProcedureType;
import cl.duoc.barriodigital.catalog.service.CatalogService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/catalog/procedures")
public class CatalogController {

    private final CatalogService catalogService;

    public CatalogController(CatalogService catalogService) {
        this.catalogService = catalogService;
    }

    @GetMapping
    public ResponseEntity<List<ProcedureType>> getAllProcedures() {
        return ResponseEntity.ok(catalogService.getAllProcedures());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProcedureType> getById(@PathVariable Long id) {
        return catalogService.getById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<ProcedureType> create(@Valid @RequestBody ProcedureType procedure) {
        ProcedureType created = catalogService.create(procedure);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProcedureType> update(@PathVariable Long id, @Valid @RequestBody ProcedureType procedure) {
        return catalogService.update(id, procedure)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/{id}/decrement-quota")
    public ResponseEntity<Map<String, Object>> decrementQuota(@PathVariable Long id) {
        boolean success = catalogService.decrementQuota(id);
        if (success) {
            return ResponseEntity.ok(Map.of("success", true, "message", "Cupo decrementado con exito"));
        } else {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("success", false, "message", "No hay cupos disponibles para este tipo de tramite"));
        }
    }

    @PostMapping("/reset-quotas")
    public ResponseEntity<Map<String, Object>> resetQuotas() {
        catalogService.resetDailyQuotas();
        return ResponseEntity.ok(Map.of("success", true, "message", "Cupos diarios restablecidos"));
    }
}
