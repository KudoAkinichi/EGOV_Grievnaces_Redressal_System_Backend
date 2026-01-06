package com.grievance.service;

import com.grievance.common.dto.ApiResponse;
import com.grievance.common.enums.GrievanceStatus;
import com.grievance.common.exception.ResourceNotFoundException;
import com.grievance.common.exception.UnauthorizedException;
import com.grievance.dto.*;
import com.grievance.messaging.RabbitMQProducer;
import com.grievance.model.*;
import com.grievance.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class GrievanceService {

    private final GrievanceRepository grievanceRepository;
    private final DepartmentRepository departmentRepository;
    private final CategoryRepository categoryRepository;
    private final GrievanceStatusHistoryRepository statusHistoryRepository;
    private final GrievanceCommentRepository commentRepository;
    private final AssignmentService assignmentService;
    private final RabbitMQProducer rabbitMQProducer;

    private static final int AUTO_ESCALATION_HOURS = 72; // 3 days

    @Transactional
    public ApiResponse<GrievanceResponse> lodgeGrievance(GrievanceRequest request, Long citizenId) {
        // Verify department and category exist
        Department department = departmentRepository.findById(request.getDepartmentId())
                .orElseThrow(() -> new ResourceNotFoundException("Department not found"));

        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));

        // Generate unique grievance number
        String grievanceNumber = generateGrievanceNumber();

        Grievance grievance = new Grievance();
        grievance.setGrievanceNumber(grievanceNumber);
        grievance.setCitizenId(citizenId);
        grievance.setDepartmentId(request.getDepartmentId());
        grievance.setCategoryId(request.getCategoryId());
        grievance.setTitle(request.getTitle());
        grievance.setDescription(request.getDescription());
        grievance.setStatus(GrievanceStatus.SUBMITTED);
        grievance.setPriority(Grievance.Priority.MEDIUM);

        Grievance savedGrievance = grievanceRepository.save(grievance);

        // Record status history
        recordStatusChange(savedGrievance.getId(), null, GrievanceStatus.SUBMITTED, citizenId, "Grievance lodged");

        // Auto-assign to officer with least load
        Long assignedOfficerId = assignmentService.getOfficerWithLeastLoad(department.getId());
        if (assignedOfficerId != null) {
            assignGrievanceToOfficer(savedGrievance.getId(), assignedOfficerId);
        }

        // Send notification to citizen
        sendNotification(citizenId, "Grievance Lodged Successfully",
                String.format("Your grievance %s has been lodged successfully and assigned to an officer.",
                        grievanceNumber));

        GrievanceResponse response = mapToResponse(savedGrievance, department.getName(), category.getName(), null);
        return ApiResponse.success("Grievance lodged successfully", response);
    }

    @Transactional
    public ApiResponse<?> assignGrievanceToOfficer(Long grievanceId, Long officerId) {
        Grievance grievance = grievanceRepository.findById(grievanceId)
                .orElseThrow(() -> new ResourceNotFoundException("Grievance not found"));

        GrievanceStatus oldStatus = grievance.getStatus();
        grievance.setAssignedOfficerId(officerId);
        grievance.setStatus(GrievanceStatus.ASSIGNED);
        grievance.setAutoEscalationTime(LocalDateTime.now().plusHours(AUTO_ESCALATION_HOURS));

        grievanceRepository.save(grievance);

        // Record status change
        recordStatusChange(grievanceId, oldStatus, GrievanceStatus.ASSIGNED, officerId,
                "Grievance assigned to officer");

        // Send notification to officer
        sendNotification(officerId, "New Grievance Assigned",
                String.format("Grievance %s has been assigned to you.", grievance.getGrievanceNumber()));

        return ApiResponse.success("Grievance assigned successfully", null);
    }

    @Transactional
    public ApiResponse<?> updateGrievanceStatus(Long grievanceId, StatusUpdateRequest request, Long userId) {
        Grievance grievance = grievanceRepository.findById(grievanceId)
                .orElseThrow(() -> new ResourceNotFoundException("Grievance not found"));

        GrievanceStatus oldStatus = grievance.getStatus();
        grievance.setStatus(request.getStatus());
        grievance.setResolutionRemarks(request.getRemarks());

        if (request.getStatus() == GrievanceStatus.RESOLVED) {
            grievance.setResolvedAt(LocalDateTime.now());
        } else if (request.getStatus() == GrievanceStatus.CLOSED) {
            grievance.setClosedAt(LocalDateTime.now());
        }

        grievanceRepository.save(grievance);

        // Record status change
        recordStatusChange(grievanceId, oldStatus, request.getStatus(), userId, request.getRemarks());

        // Send notification to citizen
        sendNotification(grievance.getCitizenId(), "Grievance Status Updated",
                String.format("Your grievance %s status has been updated to %s",
                        grievance.getGrievanceNumber(), request.getStatus()));

        return ApiResponse.success("Status updated successfully", null);
    }

    @Transactional
    public ApiResponse<?> escalateGrievance(Long grievanceId, Long citizenId) {
        Grievance grievance = grievanceRepository.findById(grievanceId)
                .orElseThrow(() -> new ResourceNotFoundException("Grievance not found"));

        // Verify citizen owns this grievance
        if (!grievance.getCitizenId().equals(citizenId)) {
            throw new UnauthorizedException("You can only escalate your own grievances");
        }

        // Verify grievance is in RESOLVED status
        if (grievance.getStatus() != GrievanceStatus.RESOLVED) {
            return ApiResponse.error("Only resolved grievances can be escalated");
        }

        GrievanceStatus oldStatus = grievance.getStatus();
        grievance.setStatus(GrievanceStatus.ESCALATED);
        grievance.setEscalatedAt(LocalDateTime.now());

        // Get supervisor for the department (you would fetch this from Auth Service)
        // For now, we'll set it to null and handle in frontend
        grievance.setEscalatedToSupervisorId(null); // TODO: Fetch supervisor from Auth Service

        grievanceRepository.save(grievance);

        // Record status change
        recordStatusChange(grievanceId, oldStatus, GrievanceStatus.ESCALATED, citizenId,
                "Grievance escalated by citizen");

        // Send notification to supervisor
        // sendNotification(supervisorId, "Grievance Escalated", ...);

        return ApiResponse.success("Grievance escalated successfully", null);
    }

    public ApiResponse<Page<GrievanceResponse>> getMyGrievances(Long citizenId, Pageable pageable) {
        Page<Grievance> grievances = grievanceRepository.findByCitizenId(citizenId, pageable);
        Page<GrievanceResponse> responses = grievances.map(g -> {
            Department dept = departmentRepository.findById(g.getDepartmentId()).orElse(null);
            Category cat = categoryRepository.findById(g.getCategoryId()).orElse(null);
            return mapToResponse(g,
                    dept != null ? dept.getName() : null,
                    cat != null ? cat.getName() : null,
                    null);
        });
        return ApiResponse.success("Grievances fetched successfully", responses);
    }

    public ApiResponse<Page<GrievanceResponse>> getAllGrievances(Long departmentId, Long categoryId,
                                                                 GrievanceStatus status, Pageable pageable) {
        // Use Specification for dynamic filtering
        Page<Grievance> grievances;

        if (departmentId != null && categoryId != null && status != null) {
            // Complex query - add custom repository method
            grievances = grievanceRepository.findByDepartmentIdAndCategoryIdAndStatus(
                    departmentId, categoryId, status, pageable);
        } else if (departmentId != null) {
            grievances = grievanceRepository.findByDepartmentId(departmentId, pageable);
        } else if (status != null) {
            grievances = grievanceRepository.findByStatus(status, pageable);
        } else {
            grievances = grievanceRepository.findAll(pageable);
        }

        // Map to response
        Page<GrievanceResponse> responses = grievances.map(g -> mapToResponse(g, null, null, null));
        return ApiResponse.success("Grievances fetched successfully", responses);
    }

    public ApiResponse<GrievanceResponse> getGrievanceById(Long grievanceId) {
        Grievance grievance = grievanceRepository.findById(grievanceId)
                .orElseThrow(() -> new ResourceNotFoundException("Grievance not found"));

        Department dept = departmentRepository.findById(grievance.getDepartmentId()).orElse(null);
        Category cat = categoryRepository.findById(grievance.getCategoryId()).orElse(null);

        GrievanceResponse response = mapToResponse(grievance,
                dept != null ? dept.getName() : null,
                cat != null ? cat.getName() : null,
                null);

        return ApiResponse.success("Grievance fetched successfully", response);
    }

    public ApiResponse<List<GrievanceStatusHistory>> getGrievanceHistory(Long grievanceId) {
        List<GrievanceStatusHistory> history = statusHistoryRepository
                .findByGrievanceIdOrderByCreatedAtDesc(grievanceId);
        return ApiResponse.success("History fetched successfully", history);
    }

    public ApiResponse<DashboardStatsDTO> getOfficerDashboard(Long officerId) {
        List<Grievance> assignedGrievances = grievanceRepository
                .findByAssignedOfficerIdAndStatus(officerId, GrievanceStatus.ASSIGNED);

        List<Grievance> inReviewGrievances = grievanceRepository
                .findByAssignedOfficerIdAndStatus(officerId, GrievanceStatus.IN_REVIEW);

        List<Grievance> resolvedGrievances = grievanceRepository
                .findByAssignedOfficerIdAndStatus(officerId, GrievanceStatus.RESOLVED);

        List<Grievance> closedGrievances = grievanceRepository
                .findByAssignedOfficerIdAndStatus(officerId, GrievanceStatus.CLOSED);

        List<Long> assignedIds = assignedGrievances.stream()
                .map(Grievance::getId)
                .collect(Collectors.toList());

        DashboardStatsDTO stats = new DashboardStatsDTO();
        stats.setOpenIssues((long) (assignedGrievances.size() + inReviewGrievances.size()));
        stats.setAssignedToMe((long) assignedGrievances.size());
        stats.setAssignedToMeIds(assignedIds);
        stats.setInReview((long) inReviewGrievances.size());
        stats.setResolved((long) resolvedGrievances.size());
        stats.setClosed((long) closedGrievances.size());

        return ApiResponse.success("Dashboard stats fetched successfully", stats);
    }

    public ApiResponse<Map<String, Object>> getDepartmentWiseReport() {
        List<Department> departments = departmentRepository.findAll();
        Map<String, Object> report = new HashMap<>();

        for (Department dept : departments) {
            Map<String, Long> deptStats = new HashMap<>();
            deptStats.put("total", grievanceRepository.countByDepartmentId(dept.getId()));
            deptStats.put("open", grievanceRepository.countByDepartmentIdAndStatus(dept.getId(), GrievanceStatus.ASSIGNED));
            deptStats.put("resolved", grievanceRepository.countByDepartmentIdAndStatus(dept.getId(), GrievanceStatus.RESOLVED));
            deptStats.put("closed", grievanceRepository.countByDepartmentIdAndStatus(dept.getId(), GrievanceStatus.CLOSED));
            report.put(dept.getName(), deptStats);
        }

        return ApiResponse.success("Department-wise report generated", report);
    }

    public ApiResponse<Map<String, Object>> getAverageResolutionTime() {
        List<Grievance> resolvedGrievances = grievanceRepository.findByStatus(GrievanceStatus.RESOLVED, Pageable.unpaged()).getContent();

        if (resolvedGrievances.isEmpty()) {
            return ApiResponse.success("No resolved grievances yet", Map.of("averageHours", 0));
        }

        long totalHours = resolvedGrievances.stream()
                .filter(g -> g.getResolvedAt() != null)
                .mapToLong(g -> java.time.Duration.between(g.getCreatedAt(), g.getResolvedAt()).toHours())
                .sum();

        double avgHours = (double) totalHours / resolvedGrievances.size();

        Map<String, Object> report = new HashMap<>();
        report.put("averageHours", avgHours);
        report.put("totalResolved", resolvedGrievances.size());

        return ApiResponse.success("Average resolution time calculated", report);
    }

    public ApiResponse<Map<String, Object>> getCategoryWiseReport() {
        List<Category> categories = categoryRepository.findAll();
        Map<String, Object> report = new HashMap<>();

        for (Category category : categories) {
            Map<String, Long> stats = new HashMap<>();
            stats.put("total", grievanceRepository.countByCategoryId(category.getId()));
            stats.put("open", grievanceRepository.countByCategoryIdAndStatus(category.getId(), GrievanceStatus.ASSIGNED));
            stats.put("resolved", grievanceRepository.countByCategoryIdAndStatus(category.getId(), GrievanceStatus.RESOLVED));
            stats.put("closed", grievanceRepository.countByCategoryIdAndStatus(category.getId(), GrievanceStatus.CLOSED));

            report.put(category.getName(), stats);
        }

        return ApiResponse.success("Category-wise report generated", report);
    }


    @Transactional
    public ApiResponse<?> addComment(Long grievanceId, CommentRequest request, Long userId, String role) {
        Grievance grievance = grievanceRepository.findById(grievanceId)
                .orElseThrow(() -> new ResourceNotFoundException("Grievance not found"));

        GrievanceComment comment = new GrievanceComment();
        comment.setGrievanceId(grievanceId);
        comment.setSenderId(userId);
        comment.setSenderRole(GrievanceComment.SenderRole.valueOf(role));
        comment.setMessage(request.getMessage());
        comment.setIsInternal(request.getIsInternal());

        commentRepository.save(comment);

        return ApiResponse.success("Comment added successfully", null);
    }

    public ApiResponse<List<GrievanceComment>> getComments(Long grievanceId) {
        List<GrievanceComment> comments = commentRepository
                .findByGrievanceIdOrderByCreatedAtAsc(grievanceId);
        return ApiResponse.success("Comments fetched successfully", comments);
    }

    @Transactional
    public ApiResponse<?> withdrawGrievance(Long grievanceId, Long citizenId) {
        Grievance grievance = grievanceRepository.findById(grievanceId)
                .orElseThrow(() -> new ResourceNotFoundException("Grievance not found"));

        if (!grievance.getCitizenId().equals(citizenId)) {
            throw new UnauthorizedException("You can only withdraw your own grievances");
        }

        if (grievance.getStatus() != GrievanceStatus.SUBMITTED) {
            return ApiResponse.error("Only submitted grievances can be withdrawn");
        }

        grievance.setStatus(GrievanceStatus.CLOSED);
        grievance.setClosedAt(LocalDateTime.now());
        grievanceRepository.save(grievance);

        recordStatusChange(grievanceId, GrievanceStatus.SUBMITTED, GrievanceStatus.CLOSED,
                citizenId, "Withdrawn by citizen");

        return ApiResponse.success("Grievance withdrawn successfully", null);
    }

    @Transactional(readOnly = true)
    public ApiResponse<Page<Grievance>> getDepartmentGrievances(
            Long departmentId,
            String status,
            int page,
            int size
    ) {
        if (departmentId == null) {
            return ApiResponse.success("No department assigned", Page.empty());
        }

        Pageable pageable = PageRequest.of(page, size);
        Page<Grievance> grievances;

        if (status != null && !status.isBlank()) {
            grievances = grievanceRepository.findByDepartmentIdAndStatus(
                    departmentId,
                    GrievanceStatus.valueOf(status),
                    pageable
            );
        } else {
            grievances = grievanceRepository.findByDepartmentId(
                    departmentId,
                    pageable
            );
        }

        return ApiResponse.success("Department grievances fetched successfully", grievances);
    }


    // Helper methods
    private String generateGrievanceNumber() {
        String year = String.valueOf(LocalDateTime.now().getYear());
        long count = grievanceRepository.count() + 1;
        return String.format("GRV-%s-%06d", year, count);
    }

    private void recordStatusChange(Long grievanceId, GrievanceStatus oldStatus,
                                    GrievanceStatus newStatus, Long changedBy, String remarks) {
        GrievanceStatusHistory history = new GrievanceStatusHistory();
        history.setGrievanceId(grievanceId);
        history.setOldStatus(oldStatus);
        history.setNewStatus(newStatus);
        history.setChangedBy(changedBy);
        history.setRemarks(remarks);
        statusHistoryRepository.save(history);
    }

    private void sendNotification(Long userId, String subject, String body) {
        Map<String, String> emailData = new HashMap<>();
        emailData.put("userId", userId.toString());
        emailData.put("subject", subject);
        emailData.put("body", body);
        rabbitMQProducer.sendNotification(emailData);
    }

    private GrievanceResponse mapToResponse(Grievance grievance, String deptName,
                                            String catName, String officerName) {
        GrievanceResponse response = new GrievanceResponse();
        response.setId(grievance.getId());
        response.setGrievanceNumber(grievance.getGrievanceNumber());
        response.setCitizenId(grievance.getCitizenId());
        response.setDepartmentId(grievance.getDepartmentId());
        response.setDepartmentName(deptName);
        response.setCategoryId(grievance.getCategoryId());
        response.setCategoryName(catName);
        response.setTitle(grievance.getTitle());
        response.setDescription(grievance.getDescription());
        response.setStatus(grievance.getStatus());
        response.setAssignedOfficerId(grievance.getAssignedOfficerId());
        response.setAssignedOfficerName(officerName);
        response.setEscalatedToSupervisorId(grievance.getEscalatedToSupervisorId());
        response.setPriority(grievance.getPriority());
        response.setResolutionRemarks(grievance.getResolutionRemarks());
        response.setResolvedAt(grievance.getResolvedAt());
        response.setClosedAt(grievance.getClosedAt());
        response.setEscalatedAt(grievance.getEscalatedAt());
        response.setCreatedAt(grievance.getCreatedAt());
        response.setUpdatedAt(grievance.getUpdatedAt());
        return response;
    }

    // In GrievanceService.java - add these methods

    @Transactional(readOnly = true)
    public Page<GrievanceResponse> getDepartmentGrievances(Long departmentId, Pageable pageable) {
        log.info("Fetching all grievances for department: {}", departmentId);
        Page<Grievance> grievances = grievanceRepository.findByDepartmentId(departmentId, pageable);
        return grievances.map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public Page<GrievanceResponse> getDepartmentGrievancesByStatus(
            Long departmentId,
            GrievanceStatus status,
            Pageable pageable
    ) {
        log.info("Fetching grievances - Department: {}, Status: {}", departmentId, status);
        Page<Grievance> grievances = grievanceRepository.findByDepartmentIdAndStatus(
                departmentId,
                status,
                pageable
        );
        return grievances.map(this::toResponse);
    }

    private GrievanceResponse toResponse(Grievance grievance) {
        return GrievanceResponse.builder()
                .id(grievance.getId())
                .grievanceNumber(grievance.getGrievanceNumber())
                .citizenId(grievance.getCitizenId())
                .departmentId(grievance.getDepartmentId())
                .categoryId(grievance.getCategoryId())
                .title(grievance.getTitle())
                .description(grievance.getDescription())
                .status(grievance.getStatus())
                .assignedOfficerId(grievance.getAssignedOfficerId())
                .priority(grievance.getPriority())
                .createdAt(grievance.getCreatedAt())
                .updatedAt(grievance.getUpdatedAt())
                .build();
    }
}