package com.grievance.dto;

import lombok.Data;

@Data
public class DocumentUploadRequest {
    private String fileName;
    private String fileType;
    private String fileDataBase64; // Base64 encoded file data
}