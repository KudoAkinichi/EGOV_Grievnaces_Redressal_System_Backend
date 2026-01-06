package com.grievance.service;

import com.grievance.common.dto.ApiResponse;
import com.grievance.common.exception.ResourceNotFoundException;
import com.grievance.dto.DocumentResponse;
import com.grievance.dto.DocumentUploadRequest;
import com.grievance.model.GrievanceDocument;
import com.grievance.repository.GrievanceDocumentRepository;
import com.grievance.repository.GrievanceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Base64;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DocumentService {

    private final GrievanceDocumentRepository documentRepository;
    private final GrievanceRepository grievanceRepository;

    @Transactional
    public ApiResponse<?> uploadDocument(Long grievanceId, DocumentUploadRequest request,
                                         Long userId, String role) {
        // Verify grievance exists
        if (!grievanceRepository.existsById(grievanceId)) {
            throw new ResourceNotFoundException("Grievance not found");
        }

        // Decode Base64 file data
        byte[] fileData = Base64.getDecoder().decode(request.getFileDataBase64());

        GrievanceDocument document = new GrievanceDocument();
        document.setGrievanceId(grievanceId);
        document.setFileName(request.getFileName());
        document.setFileType(request.getFileType());
        document.setFileSize((long) fileData.length);
        document.setFileData(fileData);
        document.setUploadedBy(userId);
        document.setUploadedByRole(GrievanceDocument.UploadedByRole.valueOf(role));

        GrievanceDocument saved = documentRepository.save(document);

        return ApiResponse.success("Document uploaded successfully",
                DocumentResponse.fromEntity(saved));
    }

    public ApiResponse<List<DocumentResponse>> getDocuments(Long grievanceId) {
        List<GrievanceDocument> documents = documentRepository.findByGrievanceId(grievanceId);

        // Convert to DocumentResponse with Base64 encoded data
        List<DocumentResponse> responses = documents.stream()
                .map(DocumentResponse::fromEntity)
                .collect(Collectors.toList());

        return ApiResponse.success("Documents fetched successfully", responses);
    }

    public ApiResponse<DocumentResponse> getDocument(Long documentId) {
        GrievanceDocument document = documentRepository.findById(documentId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Document not found"));

        return ApiResponse.success("Document fetched successfully",
                DocumentResponse.fromEntity(document));
    }

    @Transactional
    public ApiResponse<?> deleteDocument(Long documentId, Long userId) {
        GrievanceDocument document = documentRepository.findById(documentId)
                .orElseThrow(() -> new ResourceNotFoundException("Document not found"));

        documentRepository.deleteById(documentId);
        return ApiResponse.success("Document deleted successfully", null);
    }
}