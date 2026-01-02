package com.grievance.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CommentRequest {

    @NotBlank(message = "Message is required")
    private String message;

    private Boolean isInternal = false;
}