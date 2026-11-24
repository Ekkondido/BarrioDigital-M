package cl.duoc.barriodigital.audit.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "audit_events")
public class AuditEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 64)
    private String eventId;

    private Long requestId;

    @Column(length = 32)
    private String trackingNumber;

    @Column(nullable = false, length = 64)
    private String action; // CREATE, STATUS_CHANGE, ADMISSION, DISPATCH

    @Column(nullable = false, length = 128)
    private String actor;

    @Column(length = 32)
    private String oldStatus;

    @Column(length = 32)
    private String newStatus;

    @Column(nullable = false)
    private LocalDateTime timestamp;

    @Column(length = 2000)
    private String detailsJson;

    public AuditEvent() {
    }

    public AuditEvent(Long id, String eventId, Long requestId, String trackingNumber, String action,
                      String actor, String oldStatus, String newStatus, LocalDateTime timestamp, String detailsJson) {
        this.id = id;
        this.eventId = eventId;
        this.requestId = requestId;
        this.trackingNumber = trackingNumber;
        this.action = action;
        this.actor = actor;
        this.oldStatus = oldStatus;
        this.newStatus = newStatus;
        this.timestamp = timestamp != null ? timestamp : LocalDateTime.now();
        this.detailsJson = detailsJson;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getEventId() { return eventId; }
    public void setEventId(String eventId) { this.eventId = eventId; }

    public Long getRequestId() { return requestId; }
    public void setRequestId(Long requestId) { this.requestId = requestId; }

    public String getTrackingNumber() { return trackingNumber; }
    public void setTrackingNumber(String trackingNumber) { this.trackingNumber = trackingNumber; }

    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }

    public String getActor() { return actor; }
    public void setActor(String actor) { this.actor = actor; }

    public String getOldStatus() { return oldStatus; }
    public void setOldStatus(String oldStatus) { this.oldStatus = oldStatus; }

    public String getNewStatus() { return newStatus; }
    public void setNewStatus(String newStatus) { this.newStatus = newStatus; }

    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }

    public String getDetailsJson() { return detailsJson; }
    public void setDetailsJson(String detailsJson) { this.detailsJson = detailsJson; }
}
