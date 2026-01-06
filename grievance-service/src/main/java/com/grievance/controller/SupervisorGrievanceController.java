// grievance-service/src/main/java/com/grievance/controller/SupervisorGrievanceController.java
package com.grievance.controller;

import com.grievance.common.dto.ApiResponse;
import com.grievance.common.enums.GrievanceStatus;
import com.grievance.dto.GrievanceResponse;
import com.grievance.service.GrievanceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/supervisor/grievances")  // ✅ Remove /api prefix (gateway handles it)
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = {"http://localhost:4200", "http://localhost:4201"})
public class SupervisorGrievanceController {

    private final GrievanceService grievanceService;

    /**
     * Get all grievances for supervisor's department
     * GET /supervisor/grievances?page=0&size=10
     */
    @GetMapping
    public ResponseEntity<ApiResponse<Page<GrievanceResponse>>> getDepartmentGrievances(
            @RequestHeader(value = "X-Department-ID", required = false) Long departmentId,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        log.info("📥 Fetching grievances - Department: {}, Status: {}, Page: {}, Size: {}",
                departmentId, status, page, size);

        // ✅ Use default department if not provided
        if (departmentId == null) {
            departmentId = 1L;
        }

        Pageable pageable = PageRequest.of(page, size);
        Page<GrievanceResponse> grievances;

        if (status != null && !status.isBlank() && !status.equalsIgnoreCase("all")) {
            try {
                grievances = grievanceService.getDepartmentGrievancesByStatus(
                        departmentId,
                        GrievanceStatus.valueOf(status.toUpperCase()),
                        pageable
                );
            } catch (IllegalArgumentException e) {
                log.warn("Invalid status: {}", status);
                grievances = grievanceService.getDepartmentGrievances(departmentId, pageable);
            }
        } else {
            grievances = grievanceService.getDepartmentGrievances(departmentId, pageable);
        }

        log.info("✅ Returning {} grievances", grievances.getTotalElements());
        return ResponseEntity.ok(
                ApiResponse.success("Department grievances fetched successfully", grievances)
        );
    }

    /**
     * Get all grievances (no filter)
     * GET /supervisor/grievances/all?page=0&size=10
     */
    @GetMapping("/all")
    public ResponseEntity<ApiResponse<Page<GrievanceResponse>>> getAllGrievances(
            @RequestHeader(value = "X-Department-ID", required = false) Long departmentId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        log.info("📥 Fetching ALL department grievances - Department: {}", departmentId);

        if (departmentId == null) {
            departmentId = 1L;
        }

        Pageable pageable = PageRequest.of(page, size);
        Page<GrievanceResponse> grievances = grievanceService.getDepartmentGrievances(departmentId, pageable);

        log.info("✅ Returning {} grievances", grievances.getTotalElements());
        return ResponseEntity.ok(
                ApiResponse.success("All department grievances fetched", grievances)
        );
    }

    /**
     * Get escalated grievances only
     * GET /supervisor/grievances/escalated?page=0&size=10
     */
    @GetMapping("/escalated")
    public ResponseEntity<ApiResponse<Page<GrievanceResponse>>> getEscalatedGrievances(
            @RequestHeader(value = "X-Department-ID", required = false) Long departmentId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        log.info("📥 Fetching ESCALATED grievances - Department: {}", departmentId);

        if (departmentId == null) {
            departmentId = 1L;
        }

        Pageable pageable = PageRequest.of(page, size);
        Page<GrievanceResponse> grievances = grievanceService.getDepartmentGrievancesByStatus(
                departmentId,
                GrievanceStatus.ESCALATED,
                pageable
        );

        log.info("✅ Returning {} escalated grievances", grievances.getTotalElements());
        return ResponseEntity.ok(
                ApiResponse.success("Escalated grievances fetched", grievances)
        );
    }

    /**
     * Get assigned grievances only
     * GET /supervisor/grievances/assigned?page=0&size=10
     */
    @GetMapping("/assigned")
    public ResponseEntity<ApiResponse<Page<GrievanceResponse>>> getAssignedGrievances(
            @RequestHeader(value = "X-Department-ID", required = false) Long departmentId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        log.info("📥 Fetching ASSIGNED grievances - Department: {}", departmentId);

        if (departmentId == null) {
            departmentId = 1L;
        }

        Pageable pageable = PageRequest.of(page, size);
        Page<GrievanceResponse> grievances = grievanceService.getDepartmentGrievancesByStatus(
                departmentId,
                GrievanceStatus.ASSIGNED,
                pageable
        );

        log.info("✅ Returning {} assigned grievances", grievances.getTotalElements());
        return ResponseEntity.ok(
                ApiResponse.success("Assigned grievances fetched", grievances)
        );
    }

    /**
     * Get in-review grievances only
     * GET /supervisor/grievances/in-review?page=0&size=10
     */
    @GetMapping("/in-review")
    public ResponseEntity<ApiResponse<Page<GrievanceResponse>>> getInReviewGrievances(
            @RequestHeader(value = "X-Department-ID", required = false) Long departmentId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        log.info("📥 Fetching IN-REVIEW grievances - Department: {}", departmentId);

        if (departmentId == null) {
            departmentId = 1L;
        }

        Pageable pageable = PageRequest.of(page, size);
        Page<GrievanceResponse> grievances = grievanceService.getDepartmentGrievancesByStatus(
                departmentId,
                GrievanceStatus.IN_REVIEW,
                pageable
        );

        log.info("✅ Returning {} in-review grievances", grievances.getTotalElements());
        return ResponseEntity.ok(
                ApiResponse.success("In-review grievances fetched", grievances)
        );
    }

    /**
     * Get grievance by ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<GrievanceResponse>> getGrievanceById(@PathVariable Long id) {
        return ResponseEntity.ok(grievanceService.getGrievanceById(id));
    }
}