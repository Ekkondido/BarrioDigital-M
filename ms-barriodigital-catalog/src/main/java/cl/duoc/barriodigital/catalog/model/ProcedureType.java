package cl.duoc.barriodigital.catalog.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Entity
@Table(name = "procedure_types")
public class ProcedureType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "El codigo es obligatorio")
    @Column(nullable = false, unique = true)
    private String code;

    @NotBlank(message = "El nombre del tramite es obligatorio")
    @Column(nullable = false)
    private String name;

    @Column(length = 1000)
    private String description;

    @NotNull(message = "El cupo diario es obligatorio")
    @Min(value = 0, message = "El cupo diario no puede ser negativo")
    @Column(nullable = false)
    private Integer dailyQuota;

    @NotNull
    @Min(0)
    @Column(nullable = false)
    private Integer availableQuota;

    @Column(length = 2000)
    private String requirements;

    @Column(nullable = false)
    private Boolean active = true;

    public ProcedureType() {
    }

    public ProcedureType(Long id, String code, String name, String description, Integer dailyQuota, Integer availableQuota, String requirements, Boolean active) {
        this.id = id;
        this.code = code;
        this.name = name;
        this.description = description;
        this.dailyQuota = dailyQuota;
        this.availableQuota = availableQuota;
        this.requirements = requirements;
        this.active = active != null ? active : true;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getDailyQuota() {
        return dailyQuota;
    }

    public void setDailyQuota(Integer dailyQuota) {
        this.dailyQuota = dailyQuota;
    }

    public Integer getAvailableQuota() {
        return availableQuota;
    }

    public void setAvailableQuota(Integer availableQuota) {
        this.availableQuota = availableQuota;
    }

    public String getRequirements() {
        return requirements;
    }

    public void setRequirements(String requirements) {
        this.requirements = requirements;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }
}
