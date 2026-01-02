package com.grievance.dto;

import com.grievance.common.enums.GrievanceStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class StatusUpdateRequest {

    @NotNull(message = "Status is required")
    private GrievanceStatus status;

    private String remarks;
}