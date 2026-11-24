package cl.duoc.barriodigital.adminmq.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class BindingCreateDto {

    @NotBlank(message = "El nombre de la cola destino es obligatorio.")
    @Size(min = 1, max = 255)
    private String queueName;

    @NotBlank(message = "El nombre del exchange origen es obligatorio.")
    @Size(min = 1, max = 255)
    private String exchangeName;

    @NotBlank(message = "La routing key es obligatoria.")
    @Size(min = 1, max = 255)
    private String routingKey;

    public BindingCreateDto() {
    }

    public BindingCreateDto(String queueName, String exchangeName, String routingKey) {
        this.queueName = queueName;
        this.exchangeName = exchangeName;
        this.routingKey = routingKey;
    }

    public String getQueueName() { return queueName; }
    public void setQueueName(String queueName) { this.queueName = queueName; }

    public String getExchangeName() { return exchangeName; }
    public void setExchangeName(String exchangeName) { this.exchangeName = exchangeName; }

    public String getRoutingKey() { return routingKey; }
    public void setRoutingKey(String routingKey) { this.routingKey = routingKey; }
}
