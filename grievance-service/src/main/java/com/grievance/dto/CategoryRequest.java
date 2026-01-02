package com.grievance.dto;

import jakarta.validation.constraints.NotBlank;
//import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CategoryRequest {
    // removing this here     @NotNull(message = "Department ID is required")
    private Long departmentId;

    @NotBlank(message = "Category name is required")
    private String name;

    private String description;
}