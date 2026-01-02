package com.grievance.controller;

import com.grievance.common.dto.ApiResponse;
import com.grievance.dto.*;
import com.grievance.model.GrievanceComment;
import com.grievance.model.GrievanceStatusHistory;
import com.grievance.service.DocumentService;
import com.grievance.service.GrievanceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/grievances")
@RequiredArgsConstructor
public class GrievanceController {

    private final GrievanceService grievanceService;
    private final DocumentService documentService;

    @PostMapping
    public ResponseEntity<ApiResponse<GrievanceResponse>> lodgeGrievance(
            @Valid @RequestBody GrievanceRequest request,
            @RequestHeader("X-User-Id") Long citizenId) {
        return ResponseEntity.ok(grievanceService.lodgeGrievance(request, citizenId));
    }

    @GetMapping("/my-grievances")
    public ResponseEntity<ApiResponse<Page<GrievanceResponse>>> getMyGrievances(
            @RequestHeader("X-User-Id") Long citizenId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(grievanceService.getMyGrievances(citizenId, pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<GrievanceResponse>> getGrievanceById(@PathVariable Long id) {
        return ResponseEntity.ok(grievanceService.getGrievanceById(id));
    }

    @PatchMapping("/{id}/assign/{officerId}")
    public ResponseEntity<ApiResponse<?>> assignGrievance(
            @PathVariable Long id,
            @PathVariable Long officerId) {
        return ResponseEntity.ok(grievanceService.assignGrievanceToOfficer(id, officerId));
    }

    @PostMapping("/{id}/auto-assign")
    public ResponseEntity<ApiResponse<?>> autoAssignGrievance(@PathVariable Long id) {
        // This will be called automatically after grievance lodging
        return ResponseEntity.ok(ApiResponse.success("Auto-assignment handled", null));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<ApiResponse<?>> updateStatus(
            @PathVariable Long id,
            @Valid @RequestBody StatusUpdateRequest request,
            @RequestHeader("X-User-Id") Long userId) {
        return ResponseEntity.ok(grievanceService.updateGrievanceStatus(id, request, userId));
    }

    @PostMapping("/{id}/escalate")
    public ResponseEntity<ApiResponse<?>> escalateGrievance(
            @PathVariable Long id,
            @RequestHeader("X-User-Id") Long citizenId) {
        return ResponseEntity.ok(grievanceService.escalateGrievance(id, citizenId));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<?>> withdrawGrievance(
            @PathVariable Long id,
            @RequestHeader("X-User-Id") Long citizenId) {
        return ResponseEntity.ok(grievanceService.withdrawGrievance(id, citizenId));
    }

    @GetMapping("/{id}/history")
    public ResponseEntity<ApiResponse<List<GrievanceStatusHistory>>> getHistory(@PathVariable Long id) {
        return ResponseEntity.ok(grievanceService.getGrievanceHistory(id));
    }

    @GetMapping("/dashboard/officer")
    public ResponseEntity<ApiResponse<DashboardStatsDTO>> getOfficerDashboard(
            @RequestParam Long officerId) {
        return ResponseEntity.ok(grievanceService.getOfficerDashboard(officerId));
    }

    // Comments
    @PostMapping("/{id}/comments")
    public ResponseEntity<ApiResponse<?>> addComment(
            @PathVariable Long id,
            @Valid @RequestBody CommentRequest request,
            @RequestHeader("X-User-Id") Long userId,
            @RequestHeader("X-User-Role") String role) {
        return ResponseEntity.ok(grievanceService.addComment(id, request, userId, role));
    }

    @GetMapping("/{id}/comments")
    public ResponseEntity<ApiResponse<List<GrievanceComment>>> getComments(@PathVariable Long id) {
        return ResponseEntity.ok(grievanceService.getComments(id));
    }

    // Documents
    @PostMapping("/{id}/documents")
    public ResponseEntity<ApiResponse<?>> uploadDocument(
            @PathVariable Long id,
            @RequestBody DocumentUploadRequest request,
            @RequestHeader("X-User-Id") Long userId,
            @RequestHeader("X-User-Role") String role) {
        return ResponseEntity.ok(documentService.uploadDocument(id, request, userId, role));
    }

    @GetMapping("/{id}/documents")
    public ResponseEntity<ApiResponse<?>> getDocuments(@PathVariable Long id) {
        return ResponseEntity.ok(documentService.getDocuments(id));
    }

    @GetMapping("/documents/{docId}")
    public ResponseEntity<ApiResponse<?>> getDocument(@PathVariable Long docId) {
        return ResponseEntity.ok(documentService.getDocument(docId));
    }

    @DeleteMapping("/documents/{docId}")
    public ResponseEntity<ApiResponse<?>> deleteDocument(
            @PathVariable Long docId,
            @RequestHeader("X-User-Id") Long userId) {
        return ResponseEntity.ok(documentService.deleteDocument(docId, userId));
    }
}