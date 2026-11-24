package cl.duoc.barriodigital.bff.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class AuditClient {

    private static final Logger log = LoggerFactory.getLogger(AuditClient.class);
    private final RestClient restClient;
    private final String auditUrl;

    public AuditClient(RestClient restClient, @Value("${services.audit.url}") String auditUrl) {
        this.restClient = restClient;
        this.auditUrl = auditUrl;
    }

    public Object getAuditTimeline(Long requestId, String actor) {
        return restClient.get()
                .uri(auditUrl + "/api/audit" + 
                     (requestId != null ? "?requestId=" + requestId : "") +
                     (actor != null ? (requestId != null ? "&" : "?") + "actor=" + actor : ""))
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .body(Object.class);
    }

    public Object getAuditByRequestId(Long requestId) {
        return restClient.get()
                .uri(auditUrl + "/api/audit/request/" + requestId)
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .body(Object.class);
    }
}
