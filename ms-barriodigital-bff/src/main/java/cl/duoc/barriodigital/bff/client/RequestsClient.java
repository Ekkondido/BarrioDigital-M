package cl.duoc.barriodigital.bff.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class RequestsClient {

    private static final Logger log = LoggerFactory.getLogger(RequestsClient.class);
    private final RestClient restClient;
    private final String requestsUrl;

    public RequestsClient(RestClient restClient, @Value("${services.requests.url}") String requestsUrl) {
        this.restClient = restClient;
        this.requestsUrl = requestsUrl;
    }

    public Object getRequests(String status, String citizenEmail) {
        try {
            return restClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .scheme("http")
                            .host(requestsUrl.replace("http://", "").split(":")[0])
                            .port(Integer.parseInt(requestsUrl.split(":")[2]))
                            .path("/api/requests")
                            .queryParam("status", status)
                            .queryParam("citizenEmail", citizenEmail)
                            .build())
                    .accept(MediaType.APPLICATION_JSON)
                    .retrieve()
                    .body(Object.class);
        } catch (Exception e) {
            // Alternativa directa con string
            return restClient.get()
                    .uri(requestsUrl + "/api/requests" + 
                         (status != null ? "?status=" + status : "") + 
                         (citizenEmail != null ? (status != null ? "&" : "?") + "citizenEmail=" + citizenEmail : ""))
                    .accept(MediaType.APPLICATION_JSON)
                    .retrieve()
                    .body(Object.class);
        }
    }

    public Object getRequestById(Long id) {
        return restClient.get()
                .uri(requestsUrl + "/api/requests/" + id)
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .body(Object.class);
    }

    public Object createRequest(Object body, String userEmail, String userName) {
        return restClient.post()
                .uri(requestsUrl + "/api/requests")
                .header("X-User-Email", userEmail != null ? userEmail : "vecino@barriodigital.cl")
                .header("X-User-Name", userName != null ? userName : "Vecino Usuario")
                .contentType(MediaType.APPLICATION_JSON)
                .body(body)
                .retrieve()
                .body(Object.class);
    }

    public Object updateStatus(Long id, Object body, String actorEmail) {
        return restClient.put()
                .uri(requestsUrl + "/api/requests/" + id + "/status")
                .header("X-Actor-Email", actorEmail != null ? actorEmail : "funcionario@barriodigital.cl")
                .contentType(MediaType.APPLICATION_JSON)
                .body(body)
                .retrieve()
                .body(Object.class);
    }
}
