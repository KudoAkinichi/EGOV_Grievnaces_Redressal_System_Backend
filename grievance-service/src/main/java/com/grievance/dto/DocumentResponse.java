package com.grievance.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.Base64;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DocumentResponse {
    private Long id;
    private Long grievanceId;
    private String fileName;
    private String fileType;
    private Long fileSize;
    private String fileDataBase64;
    private Long uploadedBy;
    private String uploadedByRole;
    private LocalDateTime createdAt;

    // Factory method to convert from entity
    public static DocumentResponse fromEntity(com.grievance.model.GrievanceDocument entity) {
        String base64Data = entity.getFileData() != null
                ? Base64.getEncoder().encodeToString(entity.getFileData())
                : null;

        return DocumentResponse.builder()
                .id(entity.getId())
                .grievanceId(entity.getGrievanceId())
                .fileName(entity.getFileName())
                .fileType(entity.getFileType())
                .fileSize(entity.getFileSize())
                .fileDataBase64(base64Data)
                .uploadedBy(entity.getUploadedBy())
                .uploadedByRole(entity.getUploadedByRole().toString())
                .createdAt(entity.getCreatedAt())
                .build();
    }
}