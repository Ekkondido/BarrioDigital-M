package cl.duoc.barriodigital.report.controller;

import cl.duoc.barriodigital.report.service.ReportService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/report")
public class ReportController {

    private final ReportService reportService;

    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    @GetMapping("/kpis")
    public ResponseEntity<Map<String, Object>> getKpis(@RequestParam(required = false, defaultValue = "last24h") String range) {
        return ResponseEntity.ok(reportService.getKpis(range));
    }

    @GetMapping("/top-procedures")
    public ResponseEntity<List<Map<String, Object>>> getTopProcedures(@RequestParam(required = false, defaultValue = "last7d") String range) {
        return ResponseEntity.ok(reportService.getTopProcedures(range));
    }
}
