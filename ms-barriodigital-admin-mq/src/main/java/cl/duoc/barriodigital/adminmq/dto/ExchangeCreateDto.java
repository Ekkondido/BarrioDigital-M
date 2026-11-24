package cl.duoc.barriodigital.adminmq.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class ExchangeCreateDto {

    @NotBlank(message = "El nombre del exchange es obligatorio y no puede estar vacio.")
    @Size(min = 3, max = 255, message = "El nombre del exchange debe tener entre 3 y 255 caracteres.")
    @Pattern(regexp = "^[a-zA-Z0-9._-]+$", message = "El nombre del exchange contiene caracteres invalidos.")
    private String name;

    @NotBlank(message = "El tipo de exchange es obligatorio (direct, topic, fanout, headers).")
    @Pattern(regexp = "^(direct|topic|fanout|headers)$", message = "Tipo invalido. Debe ser: direct, topic, fanout o headers.")
    private String type = "direct";

    private boolean durable = true;

    private boolean autoDelete = false;

    public ExchangeCreateDto() {
    }

    public ExchangeCreateDto(String name, String type, boolean durable, boolean autoDelete) {
        this.name = name;
        this.type = type;
        this.durable = durable;
        this.autoDelete = autoDelete;
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public boolean isDurable() { return durable; }
    public void setDurable(boolean durable) { this.durable = durable; }

    public boolean isAutoDelete() { return autoDelete; }
    public void setAutoDelete(boolean autoDelete) { this.autoDelete = autoDelete; }
}
