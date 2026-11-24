package cl.duoc.barriodigital.bff.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class ReportClient {

    private static final Logger log = LoggerFactory.getLogger(ReportClient.class);
    private final RestClient restClient;
    private final String reportUrl;

    public ReportClient(RestClient restClient, @Value("${services.report.url}") String reportUrl) {
        this.restClient = restClient;
        this.reportUrl = reportUrl;
    }

    public Object getKpis(String range) {
        return restClient.get()
                .uri(reportUrl + "/api/report/kpis" + (range != null ? "?range=" + range : ""))
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .body(Object.class);
    }

    public Object getTopProcedures(String range) {
        return restClient.get()
                .uri(reportUrl + "/api/report/top-procedures" + (range != null ? "?range=" + range : ""))
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .body(Object.class);
    }
}
