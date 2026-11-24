package cl.duoc.barriodigital.requests.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Entity
@Table(name = "citizen_requests")
public class CitizenRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 32)
    private String trackingNumber; // Ejemplo: REQ-2026-0001

    @NotNull
    @Column(nullable = false)
    private Long procedureId;

    @Column(nullable = false)
    private String procedureName;

    @NotBlank
    @Column(nullable = false)
    private String citizenEmail;

    @NotBlank
    @Column(nullable = false)
    private String citizenName;

    @Column(length = 20)
    private String citizenPhone;

    @NotBlank
    @Column(nullable = false)
    private String address;

    @Column(length = 2000)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private RequestStatus status = RequestStatus.INGRESADO;

    @Column(length = 100)
    private String assignedCrew;

    @Column(length = 1000)
    private String resolutionNotes;

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(nullable = false)
    private LocalDateTime updatedAt = LocalDateTime.now();

    public CitizenRequest() {
    }

    public CitizenRequest(Long id, String trackingNumber, Long procedureId, String procedureName,
                          String citizenEmail, String citizenName, String citizenPhone,
                          String address, String description, RequestStatus status,
                          String assignedCrew, String resolutionNotes,
                          LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.trackingNumber = trackingNumber;
        this.procedureId = procedureId;
        this.procedureName = procedureName;
        this.citizenEmail = citizenEmail;
        this.citizenName = citizenName;
        this.citizenPhone = citizenPhone;
        this.address = address;
        this.description = description;
        this.status = status != null ? status : RequestStatus.INGRESADO;
        this.assignedCrew = assignedCrew;
        this.resolutionNotes = resolutionNotes;
        this.createdAt = createdAt != null ? createdAt : LocalDateTime.now();
        this.updatedAt = updatedAt != null ? updatedAt : LocalDateTime.now();
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTrackingNumber() { return trackingNumber; }
    public void setTrackingNumber(String trackingNumber) { this.trackingNumber = trackingNumber; }

    public Long getProcedureId() { return procedureId; }
    public void setProcedureId(Long procedureId) { this.procedureId = procedureId; }

    public String getProcedureName() { return procedureName; }
    public void setProcedureName(String procedureName) { this.procedureName = procedureName; }

    public String getCitizenEmail() { return citizenEmail; }
    public void setCitizenEmail(String citizenEmail) { this.citizenEmail = citizenEmail; }

    public String getCitizenName() { return citizenName; }
    public void setCitizenName(String citizenName) { this.citizenName = citizenName; }

    public String getCitizenPhone() { return citizenPhone; }
    public void setCitizenPhone(String citizenPhone) { this.citizenPhone = citizenPhone; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public RequestStatus getStatus() { return status; }
    public void setStatus(RequestStatus status) { this.status = status; }

    public String getAssignedCrew() { return assignedCrew; }
    public void setAssignedCrew(String assignedCrew) { this.assignedCrew = assignedCrew; }

    public String getResolutionNotes() { return resolutionNotes; }
    public void setResolutionNotes(String resolutionNotes) { this.resolutionNotes = resolutionNotes; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
