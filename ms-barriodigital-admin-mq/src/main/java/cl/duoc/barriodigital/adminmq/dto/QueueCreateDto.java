package cl.duoc.barriodigital.adminmq.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.util.HashMap;
import java.util.Map;

/**
 * DTO para la creacion de colas con validaciones estrictas (Indicador 8).
 */
public class QueueCreateDto {

    @NotBlank(message = "El nombre de la cola es obligatorio y no puede estar vacio.")
    @Size(min = 3, max = 255, message = "El nombre de la cola debe tener entre 3 y 255 caracteres.")
    @Pattern(regexp = "^[a-zA-Z0-9._-]+$", message = "El nombre de la cola solo puede contener letras, numeros, puntos, guiones y guiones bajos.")
    private String name;

    private boolean durable = true;

    private boolean exclusive = false;

    private boolean autoDelete = false;

    // Parametros opcionales de Dead Letter Exchange
    @Pattern(regexp = "^[a-zA-Z0-9._-]*$", message = "El dead-letter-exchange contiene caracteres invalidos.")
    private String deadLetterExchange;

    @Pattern(regexp = "^[a-zA-Z0-9._-]*$", message = "El dead-letter-routing-key contiene caracteres invalidos.")
    private String deadLetterRoutingKey;

    private Integer messageTtl; // en milisegundos

    private Integer maxLength;

    public QueueCreateDto() {
    }

    public QueueCreateDto(String name, boolean durable, boolean exclusive, boolean autoDelete) {
        this.name = name;
        this.durable = durable;
        this.exclusive = exclusive;
        this.autoDelete = autoDelete;
    }

    public Map<String, Object> toArgumentsMap() {
        Map<String, Object> args = new HashMap<>();
        if (deadLetterExchange != null && !deadLetterExchange.isBlank()) {
            args.put("x-dead-letter-exchange", deadLetterExchange.trim());
        }
        if (deadLetterRoutingKey != null && !deadLetterRoutingKey.isBlank()) {
            args.put("x-dead-letter-routing-key", deadLetterRoutingKey.trim());
        }
        if (messageTtl != null && messageTtl > 0) {
            args.put("x-message-ttl", messageTtl);
        }
        if (maxLength != null && maxLength > 0) {
            args.put("x-max-length", maxLength);
        }
        return args;
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public boolean isDurable() { return durable; }
    public void setDurable(boolean durable) { this.durable = durable; }

    public boolean isExclusive() { return exclusive; }
    public void setExclusive(boolean exclusive) { this.exclusive = exclusive; }

    public boolean isAutoDelete() { return autoDelete; }
    public void setAutoDelete(boolean autoDelete) { this.autoDelete = autoDelete; }

    public String getDeadLetterExchange() { return deadLetterExchange; }
    public void setDeadLetterExchange(String deadLetterExchange) { this.deadLetterExchange = deadLetterExchange; }

    public String getDeadLetterRoutingKey() { return deadLetterRoutingKey; }
    public void setDeadLetterRoutingKey(String deadLetterRoutingKey) { this.deadLetterRoutingKey = deadLetterRoutingKey; }

    public Integer getMessageTtl() { return messageTtl; }
    public void setMessageTtl(Integer messageTtl) { this.messageTtl = messageTtl; }

    public Integer getMaxLength() { return maxLength; }
    public void setMaxLength(Integer maxLength) { this.maxLength = maxLength; }
}
