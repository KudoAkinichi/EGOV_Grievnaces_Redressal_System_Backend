package com.grievance.service;

import com.grievance.common.dto.ApiResponse;
import com.grievance.common.exception.ResourceNotFoundException;
import com.grievance.dto.DocumentUploadRequest;
import com.grievance.model.GrievanceDocument;
import com.grievance.repository.GrievanceDocumentRepository;
import com.grievance.repository.GrievanceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Base64;
import java.util.List;

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

        return ApiResponse.success("Document uploaded successfully", saved.getId());
    }

    public ApiResponse<List<GrievanceDocument>> getDocuments(Long grievanceId) {
        List<GrievanceDocument> documents = documentRepository.findByGrievanceId(grievanceId);
        // Don't send file data in list response
        documents.forEach(doc -> doc.setFileData(null));
        return ApiResponse.success("Documents fetched successfully", documents);
    }

    public ApiResponse<GrievanceDocument> getDocument(Long documentId) {
        GrievanceDocument document = documentRepository.findById(documentId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Document not found"));
        return ApiResponse.success("Document fetched successfully", document);
    }
    @Transactional
    public ApiResponse<?> deleteDocument(Long documentId, Long userId) {
        GrievanceDocument document = documentRepository.findById(documentId)
                .orElseThrow(() -> new ResourceNotFoundException("Document not found"));

        // Only allow deletion by uploader
        if (!document.getUploadedBy().equals(userId)) {
            return ApiResponse.error("You can only delete documents you uploaded");
        }

        documentRepository.delete(document);
        return ApiResponse.success("Document deleted successfully", null);
    }
}
