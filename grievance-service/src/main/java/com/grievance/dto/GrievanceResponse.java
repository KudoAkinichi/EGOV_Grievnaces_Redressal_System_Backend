package com.grievance.dto;

import com.grievance.common.enums.GrievanceStatus;
import com.grievance.model.Grievance;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GrievanceResponse {

    private Long id;
    private String grievanceNumber;
    private Long citizenId;
    private Long departmentId;
    private String departmentName;
    private Long categoryId;
    private String categoryName;
    private String title;
    private String description;
    private GrievanceStatus status;
    private Long assignedOfficerId;
    private String assignedOfficerName;
    private Long escalatedToSupervisorId;
    private Grievance.Priority priority;
    private String resolutionRemarks;
    private LocalDateTime resolvedAt;
    private LocalDateTime closedAt;
    private LocalDateTime escalatedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}