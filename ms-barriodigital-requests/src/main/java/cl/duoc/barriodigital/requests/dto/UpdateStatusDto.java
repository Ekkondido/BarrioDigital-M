package cl.duoc.barriodigital.requests.dto;

import jakarta.validation.constraints.NotBlank;

public class UpdateStatusDto {

    @NotBlank(message = "El nuevo estado es obligatorio")
    private String status;

    private String assignedCrew;

    private String resolutionNotes;

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getAssignedCrew() { return assignedCrew; }
    public void setAssignedCrew(String assignedCrew) { this.assignedCrew = assignedCrew; }

    public String getResolutionNotes() { return resolutionNotes; }
    public void setResolutionNotes(String resolutionNotes) { this.resolutionNotes = resolutionNotes; }
}
