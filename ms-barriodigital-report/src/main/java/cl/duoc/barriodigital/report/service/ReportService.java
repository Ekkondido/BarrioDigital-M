package cl.duoc.barriodigital.report.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class ReportService {

    private static final Logger log = LoggerFactory.getLogger(ReportService.class);
    private final ObjectMapper objectMapper = new ObjectMapper();

    // Metricas en tiempo real agregadas desde Kafka
    private final Map<String, AtomicInteger> statusCounters = new ConcurrentHashMap<>();
    private final Map<String, AtomicInteger> procedureCounters = new ConcurrentHashMap<>();
    private final Map<String, AtomicInteger> hourlyRequests = new ConcurrentHashMap<>();

    public ReportService() {
        // Inicializar datos representativos para KPIs comunales
        statusCounters.put("INGRESADO", new AtomicInteger(14));
        statusCounters.put("ADMITIDO", new AtomicInteger(8));
        statusCounters.put("EN_GESTION", new AtomicInteger(5));
        statusCounters.put("EN_TERRENO", new AtomicInteger(9));
        statusCounters.put("RESUELTO", new AtomicInteger(42));
        statusCounters.put("RECHAZADO", new AtomicInteger(3));

        procedureCounters.put("Luminaria Pública", new AtomicInteger(32));
        procedureCounters.put("Poda de Árboles", new AtomicInteger(24));
        procedureCounters.put("Bacheo de Calles", new AtomicInteger(18));
        procedureCounters.put("Retiro de Escombros", new AtomicInteger(15));
        procedureCounters.put("Control de Plagas", new AtomicInteger(8));

        hourlyRequests.put("08:00", new AtomicInteger(4));
        hourlyRequests.put("09:00", new AtomicInteger(9));
        hourlyRequests.put("10:00", new AtomicInteger(15));
        hourlyRequests.put("11:00", new AtomicInteger(18));
        hourlyRequests.put("12:00", new AtomicInteger(12));
        hourlyRequests.put("13:00", new AtomicInteger(7));
        hourlyRequests.put("14:00", new AtomicInteger(8));
        hourlyRequests.put("15:00", new AtomicInteger(14));
        hourlyRequests.put("16:00", new AtomicInteger(11));
    }

    public void processKafkaEvent(String jsonEvent) {
        try {
            Map<?, ?> map = objectMapper.readValue(jsonEvent, Map.class);
            Map<?, ?> payload = (Map<?, ?>) map.get("payload");
            if (payload != null) {
                String status = (String) payload.get("status");
                String procedureName = (String) payload.get("procedureName");
                if (status != null) {
                    statusCounters.computeIfAbsent(status, k -> new AtomicInteger(0)).incrementAndGet();
                }
                if (procedureName != null) {
                    procedureCounters.computeIfAbsent(procedureName, k -> new AtomicInteger(0)).incrementAndGet();
                }
                String currentHour = LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:00"));
                hourlyRequests.computeIfAbsent(currentHour, k -> new AtomicInteger(0)).incrementAndGet();
                log.info("KPIs actualizados desde evento Kafka: status={}, procedure={}", status, procedureName);
            }
        } catch (Exception e) {
            log.warn("No se pudo procesar mensaje de reporte: {}", e.getMessage());
        }
    }

    public Map<String, Object> getKpis(String range) {
        Map<String, Object> result = new HashMap<>();
        result.put("range", range != null ? range : "last24h");
        result.put("averageResolutionHours", 28.5); // 28.5 hrs promedio
        result.put("activeRequestsTotal", statusCounters.getOrDefault("INGRESADO", new AtomicInteger(0)).get()
                + statusCounters.getOrDefault("ADMITIDO", new AtomicInteger(0)).get()
                + statusCounters.getOrDefault("EN_GESTION", new AtomicInteger(0)).get()
                + statusCounters.getOrDefault("EN_TERRENO", new AtomicInteger(0)).get());
        result.put("resolvedTotal", statusCounters.getOrDefault("RESUELTO", new AtomicInteger(0)).get());
        result.put("rejectedTotal", statusCounters.getOrDefault("RECHAZADO", new AtomicInteger(0)).get());

        Map<String, Integer> statuses = new HashMap<>();
        statusCounters.forEach((k, v) -> statuses.put(k, v.get()));
        result.put("statuses", statuses);

        Map<String, Integer> hourly = new TreeMap<>();
        hourlyRequests.forEach((k, v) -> hourly.put(k, v.get()));
        result.put("requestsPerHour", hourly);

        return result;
    }

    public List<Map<String, Object>> getTopProcedures(String range) {
        int total = procedureCounters.values().stream().mapToInt(AtomicInteger::get).sum();
        List<Map<String, Object>> list = new ArrayList<>();
        procedureCounters.forEach((name, count) -> {
            Map<String, Object> item = new HashMap<>();
            item.put("procedureName", name);
            item.put("count", count.get());
            item.put("percentage", total > 0 ? Math.round((count.get() * 100.0) / total) : 0);
            list.add(item);
        });
        list.sort((a, b) -> Integer.compare((Integer) b.get("count"), (Integer) a.get("count")));
        return list;
    }
}
