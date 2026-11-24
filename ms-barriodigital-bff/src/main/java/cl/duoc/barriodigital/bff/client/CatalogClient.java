package cl.duoc.barriodigital.bff.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class CatalogClient {

    private static final Logger log = LoggerFactory.getLogger(CatalogClient.class);
    private final RestClient restClient;
    private final String catalogUrl;

    public CatalogClient(RestClient restClient, @Value("${services.catalog.url}") String catalogUrl) {
        this.restClient = restClient;
        this.catalogUrl = catalogUrl;
    }

    public Object getProcedures() {
        try {
            return restClient.get()
                    .uri(catalogUrl + "/api/catalog/procedures")
                    .accept(MediaType.APPLICATION_JSON)
                    .retrieve()
                    .body(Object.class);
        } catch (Exception e) {
            log.warn("Error comunicando con ms-barriodigital-catalog: {}", e.getMessage());
            throw new RuntimeException("No fue posible comunicarse con el servicio de catálogo: " + e.getMessage());
        }
    }

    public Object getProcedureById(Long id) {
        return restClient.get()
                .uri(catalogUrl + "/api/catalog/procedures/" + id)
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .body(Object.class);
    }

    public Object createProcedure(Object body) {
        return restClient.post()
                .uri(catalogUrl + "/api/catalog/procedures")
                .contentType(MediaType.APPLICATION_JSON)
                .body(body)
                .retrieve()
                .body(Object.class);
    }

    public Object updateProcedure(Long id, Object body) {
        return restClient.put()
                .uri(catalogUrl + "/api/catalog/procedures/" + id)
                .contentType(MediaType.APPLICATION_JSON)
                .body(body)
                .retrieve()
                .body(Object.class);
    }
}
