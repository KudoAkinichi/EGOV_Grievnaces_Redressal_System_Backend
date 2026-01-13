package com.grievance.model;

import com.grievance.common.enums.GrievanceStatus;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class ModelTests {

    @Test
    void testGrievanceModel() {
        Grievance grievance = new Grievance();
        grievance.setId(1L);
        grievance.setGrievanceNumber("GRV-2026-000001");
        grievance.setCitizenId(1L);
        grievance.setDepartmentId(1L);
        grievance.setCategoryId(1L);
        grievance.setTitle("Test Grievance");
        grievance.setDescription("Test Description");
        grievance.setStatus(GrievanceStatus.SUBMITTED);
        grievance.setPriority(Grievance.Priority.MEDIUM);
        grievance.setAssignedOfficerId(2L);
        grievance.setEscalatedToSupervisorId(3L);
        grievance.setResolutionRemarks("Resolved");

        LocalDateTime now = LocalDateTime.now();
        grievance.setResolvedAt(now);
        grievance.setClosedAt(now);
        grievance.setEscalatedAt(now);
        grievance.setAutoEscalationTime(now);
        grievance.setCreatedAt(now);
        grievance.setUpdatedAt(now);

        assertEquals(1L, grievance.getId());
        assertEquals("GRV-2026-000001", grievance.getGrievanceNumber());
        assertEquals(1L, grievance.getCitizenId());
        assertEquals(1L, grievance.getDepartmentId());
        assertEquals(1L, grievance.getCategoryId());
        assertEquals("Test Grievance", grievance.getTitle());
        assertEquals("Test Description", grievance.getDescription());
        assertEquals(GrievanceStatus.SUBMITTED, grievance.getStatus());
        assertEquals(Grievance.Priority.MEDIUM, grievance.getPriority());
        assertEquals(2L, grievance.getAssignedOfficerId());
        assertEquals(3L, grievance.getEscalatedToSupervisorId());
        assertEquals("Resolved", grievance.getResolutionRemarks());
        assertNotNull(grievance.getResolvedAt());
        assertNotNull(grievance.getClosedAt());
        assertNotNull(grievance.getEscalatedAt());
    }

    @Test
    void testGrievanceAllArgsConstructor() {
        LocalDateTime now = LocalDateTime.now();
        Grievance grievance = new Grievance(
                1L, "GRV-2026-000001", 1L, 1L, 1L,
                "Test", "Description", GrievanceStatus.SUBMITTED,
                2L, 3L, Grievance.Priority.HIGH, "Remarks",
                now, now, now, now, now, now
        );

        assertNotNull(grievance);
        assertEquals(1L, grievance.getId());
        assertEquals(Grievance.Priority.HIGH, grievance.getPriority());
    }

    @Test
    void testDepartmentModel() {
        Department department = new Department();
        department.setId(1L);
        department.setName("IT Department");
        department.setDescription("IT Support");
        department.setContactEmail("it@example.com");
        department.setIsActive(true);

        LocalDateTime now = LocalDateTime.now();
        department.setCreatedAt(now);
        department.setUpdatedAt(now);

        assertEquals(1L, department.getId());
        assertEquals("IT Department", department.getName());
        assertEquals("IT Support", department.getDescription());
        assertEquals("it@example.com", department.getContactEmail());
        assertTrue(department.getIsActive());
        assertNotNull(department.getCreatedAt());
        assertNotNull(department.getUpdatedAt());
    }

    @Test
    void testCategoryModel() {
        Category category = new Category();
        category.setId(1L);
        category.setDepartmentId(1L);
        category.setName("Network Issues");
        category.setDescription("Network problems");
        category.setIsActive(true);

        LocalDateTime now = LocalDateTime.now();
        category.setCreatedAt(now);

        assertEquals(1L, category.getId());
        assertEquals(1L, category.getDepartmentId());
        assertEquals("Network Issues", category.getName());
        assertEquals("Network problems", category.getDescription());
        assertTrue(category.getIsActive());
        assertNotNull(category.getCreatedAt());
    }

    @Test
    void testGrievanceCommentModel() {
        GrievanceComment comment = new GrievanceComment();
        comment.setId(1L);
        comment.setGrievanceId(1L);
        comment.setSenderId(1L);
        comment.setSenderRole(GrievanceComment.SenderRole.CITIZEN);
        comment.setMessage("Test comment");
        comment.setIsInternal(false);

        LocalDateTime now = LocalDateTime.now();
        comment.setCreatedAt(now);

        assertEquals(1L, comment.getId());
        assertEquals(1L, comment.getGrievanceId());
        assertEquals(1L, comment.getSenderId());
        assertEquals(GrievanceComment.SenderRole.CITIZEN, comment.getSenderRole());
        assertEquals("Test comment", comment.getMessage());
        assertFalse(comment.getIsInternal());
        assertNotNull(comment.getCreatedAt());
    }

    @Test
    void testGrievanceDocumentModel() {
        GrievanceDocument document = new GrievanceDocument();
        document.setId(1L);
        document.setGrievanceId(1L);
        document.setFileName("test.pdf");
        document.setFileType("application/pdf");
        document.setFileSize(1024L);
        document.setFileData(new byte[]{1, 2, 3});
        document.setUploadedBy(1L);
        document.setUploadedByRole(GrievanceDocument.UploadedByRole.CITIZEN);

        LocalDateTime now = LocalDateTime.now();
        document.setCreatedAt(now);

        assertEquals(1L, document.getId());
        assertEquals(1L, document.getGrievanceId());
        assertEquals("test.pdf", document.getFileName());
        assertEquals("application/pdf", document.getFileType());
        assertEquals(1024L, document.getFileSize());
        assertNotNull(document.getFileData());
        assertEquals(1L, document.getUploadedBy());
        assertEquals(GrievanceDocument.UploadedByRole.CITIZEN, document.getUploadedByRole());
        assertNotNull(document.getCreatedAt());
    }

    @Test
    void testGrievanceStatusHistoryModel() {
        GrievanceStatusHistory history = new GrievanceStatusHistory();
        history.setId(1L);
        history.setGrievanceId(1L);
        history.setOldStatus(GrievanceStatus.SUBMITTED);
        history.setNewStatus(GrievanceStatus.ASSIGNED);
        history.setChangedBy(1L);
        history.setRemarks("Assigned to officer");

        LocalDateTime now = LocalDateTime.now();
        history.setCreatedAt(now);

        assertEquals(1L, history.getId());
        assertEquals(1L, history.getGrievanceId());
        assertEquals(GrievanceStatus.SUBMITTED, history.getOldStatus());
        assertEquals(GrievanceStatus.ASSIGNED, history.getNewStatus());
        assertEquals(1L, history.getChangedBy());
        assertEquals("Assigned to officer", history.getRemarks());
        assertNotNull(history.getCreatedAt());
    }

    @Test
    void testGrievancePriorityEnum() {
        assertEquals(Grievance.Priority.LOW, Grievance.Priority.valueOf("LOW"));
        assertEquals(Grievance.Priority.MEDIUM, Grievance.Priority.valueOf("MEDIUM"));
        assertEquals(Grievance.Priority.HIGH, Grievance.Priority.valueOf("HIGH"));
        assertEquals(Grievance.Priority.CRITICAL, Grievance.Priority.valueOf("CRITICAL"));
    }

    @Test
    void testCommentSenderRoleEnum() {
        assertEquals(GrievanceComment.SenderRole.CITIZEN,
                GrievanceComment.SenderRole.valueOf("CITIZEN"));
        assertEquals(GrievanceComment.SenderRole.DEPT_OFFICER,
                GrievanceComment.SenderRole.valueOf("DEPT_OFFICER"));
        assertEquals(GrievanceComment.SenderRole.SUPERVISOR,
                GrievanceComment.SenderRole.valueOf("SUPERVISOR"));
    }

    @Test
    void testDocumentUploadedByRoleEnum() {
        assertEquals(GrievanceDocument.UploadedByRole.CITIZEN,
                GrievanceDocument.UploadedByRole.valueOf("CITIZEN"));
        assertEquals(GrievanceDocument.UploadedByRole.DEPT_OFFICER,
                GrievanceDocument.UploadedByRole.valueOf("DEPT_OFFICER"));
        assertEquals(GrievanceDocument.UploadedByRole.SUPERVISOR,
                GrievanceDocument.UploadedByRole.valueOf("SUPERVISOR"));
    }
}