package cl.duoc.barriodigital.bff.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class AdminMqClient {

    private static final Logger log = LoggerFactory.getLogger(AdminMqClient.class);
    private final RestClient restClient;
    private final String adminMqUrl;

    public AdminMqClient(RestClient restClient, @Value("${services.admin-mq.url}") String adminMqUrl) {
        this.restClient = restClient;
        this.adminMqUrl = adminMqUrl;
    }

    public Object listQueues() {
        return restClient.get()
                .uri(adminMqUrl + "/api/admin/mq/queues")
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .body(Object.class);
    }

    public Object createQueue(Object body) {
        return restClient.post()
                .uri(adminMqUrl + "/api/admin/mq/queues")
                .contentType(MediaType.APPLICATION_JSON)
                .body(body)
                .retrieve()
                .body(Object.class);
    }

    public Object deleteQueue(String name) {
        return restClient.delete()
                .uri(adminMqUrl + "/api/admin/mq/queues/" + name)
                .retrieve()
                .body(Object.class);
    }

    public Object purgeQueue(String name) {
        return restClient.post()
                .uri(adminMqUrl + "/api/admin/mq/queues/" + name + "/purge")
                .retrieve()
                .body(Object.class);
    }

    public Object listExchanges() {
        return restClient.get()
                .uri(adminMqUrl + "/api/admin/mq/exchanges")
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .body(Object.class);
    }

    public Object createExchange(Object body) {
        return restClient.post()
                .uri(adminMqUrl + "/api/admin/mq/exchanges")
                .contentType(MediaType.APPLICATION_JSON)
                .body(body)
                .retrieve()
                .body(Object.class);
    }

    public Object deleteExchange(String name) {
        return restClient.delete()
                .uri(adminMqUrl + "/api/admin/mq/exchanges/" + name)
                .retrieve()
                .body(Object.class);
    }

    public Object createBinding(Object body) {
        return restClient.post()
                .uri(adminMqUrl + "/api/admin/mq/bindings")
                .contentType(MediaType.APPLICATION_JSON)
                .body(body)
                .retrieve()
                .body(Object.class);
    }

    public Object deleteBinding(Object body) {
        return restClient.method(org.springframework.http.HttpMethod.DELETE)
                .uri(adminMqUrl + "/api/admin/mq/bindings")
                .contentType(MediaType.APPLICATION_JSON)
                .body(body)
                .retrieve()
                .body(Object.class);
    }

    public Object sendTestMessage(Object body) {
        return restClient.post()
                .uri(adminMqUrl + "/api/admin/mq/messages/test")
                .contentType(MediaType.APPLICATION_JSON)
                .body(body)
                .retrieve()
                .body(Object.class);
    }
}
