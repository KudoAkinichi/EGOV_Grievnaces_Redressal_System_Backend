package com.grievance.controller;

import com.grievance.common.dto.ApiResponse;
import com.grievance.common.enums.GrievanceStatus;
import com.grievance.dto.*;
import com.grievance.model.GrievanceComment;
import com.grievance.model.GrievanceStatusHistory;
import com.grievance.service.DocumentService;
import com.grievance.service.GrievanceService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GrievanceControllerTest {

    @Mock
    private GrievanceService grievanceService;

    @Mock
    private DocumentService documentService;

    @InjectMocks
    private GrievanceController grievanceController;

    private GrievanceRequest grievanceRequest;
    private GrievanceResponse grievanceResponse;
    private StatusUpdateRequest statusUpdateRequest;
    private CommentRequest commentRequest;

    @BeforeEach
    void setUp() {
        grievanceRequest = new GrievanceRequest();
        grievanceRequest.setDepartmentId(1L);
        grievanceRequest.setCategoryId(1L);
        grievanceRequest.setTitle("Test Grievance");
        grievanceRequest.setDescription("Test Description");

        grievanceResponse = GrievanceResponse.builder()
                .id(1L)
                .grievanceNumber("GRV-2026-000001")
                .citizenId(1L)
                .departmentId(1L)
                .categoryId(1L)
                .title("Test Grievance")
                .description("Test Description")
                .status(GrievanceStatus.SUBMITTED)
                .createdAt(LocalDateTime.now())
                .build();

        statusUpdateRequest = new StatusUpdateRequest();
        statusUpdateRequest.setStatus(GrievanceStatus.IN_REVIEW);
        statusUpdateRequest.setRemarks("Under review");

        commentRequest = new CommentRequest();
        commentRequest.setMessage("Test comment");
        commentRequest.setIsInternal(false);
    }

    @Test
    void testLodgeGrievance() {
        ApiResponse<GrievanceResponse> apiResponse = ApiResponse.success("Success", grievanceResponse);

        when(grievanceService.lodgeGrievance(any(GrievanceRequest.class), eq(1L)))
                .thenReturn(apiResponse);

        ResponseEntity<ApiResponse<GrievanceResponse>> response =
                grievanceController.lodgeGrievance(grievanceRequest, 1L);

        assertNotNull(response);
        assertEquals(200, response.getStatusCodeValue());
        assertNotNull(response.getBody());
        assertEquals("GRV-2026-000001", response.getBody().getData().getGrievanceNumber());
        verify(grievanceService, times(1)).lodgeGrievance(any(GrievanceRequest.class), eq(1L));
    }

    @Test
    void testGetMyGrievances() {
        Page<GrievanceResponse> page = new PageImpl<>(Arrays.asList(grievanceResponse));
        ApiResponse<Page<GrievanceResponse>> apiResponse = ApiResponse.success("Success", page);

        when(grievanceService.getMyGrievances(eq(1L), any())).thenReturn(apiResponse);

        ResponseEntity<ApiResponse<Page<GrievanceResponse>>> response =
                grievanceController.getMyGrievances(1L, 0, 10);

        assertNotNull(response);
        assertEquals(200, response.getStatusCodeValue());
        verify(grievanceService, times(1)).getMyGrievances(eq(1L), any());
    }

    @Test
    void testGetAllGrievances() {
        Page<GrievanceResponse> page = new PageImpl<>(Arrays.asList(grievanceResponse));
        ApiResponse<Page<GrievanceResponse>> apiResponse = ApiResponse.success("Success", page);

        when(grievanceService.getAllGrievances(anyLong(), anyLong(), any(), any()))
                .thenReturn(apiResponse);

        ResponseEntity<ApiResponse<Page<GrievanceResponse>>> response =
                grievanceController.getAllGrievances(1L, 1L, GrievanceStatus.SUBMITTED, 0, 10);

        assertNotNull(response);
        assertEquals(200, response.getStatusCodeValue());
        verify(grievanceService, times(1)).getAllGrievances(anyLong(), anyLong(), any(), any());
    }

    @Test
    void testGetGrievanceById() {
        ApiResponse<GrievanceResponse> apiResponse = ApiResponse.success("Success", grievanceResponse);

        when(grievanceService.getGrievanceById(1L)).thenReturn(apiResponse);

        ResponseEntity<ApiResponse<GrievanceResponse>> response =
                grievanceController.getGrievanceById(1L);

        assertNotNull(response);
        assertEquals(200, response.getStatusCodeValue());
        verify(grievanceService, times(1)).getGrievanceById(1L);
    }


    @Test
    void testGetHistory() {
        List<GrievanceStatusHistory> history = new ArrayList<>();
        ApiResponse<List<GrievanceStatusHistory>> apiResponse = ApiResponse.success("Success", history);

        when(grievanceService.getGrievanceHistory(1L)).thenReturn(apiResponse);

        ResponseEntity<ApiResponse<List<GrievanceStatusHistory>>> response =
                grievanceController.getHistory(1L);

        assertNotNull(response);
        assertEquals(200, response.getStatusCodeValue());
        verify(grievanceService, times(1)).getGrievanceHistory(1L);
    }

    @Test
    void testGetOfficerDashboard() {
        DashboardStatsDTO stats = new DashboardStatsDTO();
        stats.setOpenIssues(5L);
        stats.setAssignedToMe(3L);
        ApiResponse<DashboardStatsDTO> apiResponse = ApiResponse.success("Success", stats);

        when(grievanceService.getOfficerDashboard(1L)).thenReturn(apiResponse);

        ResponseEntity<ApiResponse<DashboardStatsDTO>> response =
                grievanceController.getOfficerDashboard(1L);

        assertNotNull(response);
        assertEquals(200, response.getStatusCodeValue());
        verify(grievanceService, times(1)).getOfficerDashboard(1L);
    }


    @Test
    void testGetComments() {
        List<GrievanceComment> comments = new ArrayList<>();
        ApiResponse<List<GrievanceComment>> apiResponse = ApiResponse.success("Success", comments);

        when(grievanceService.getComments(1L)).thenReturn(apiResponse);

        ResponseEntity<ApiResponse<List<GrievanceComment>>> response =
                grievanceController.getComments(1L);

        assertNotNull(response);
        assertEquals(200, response.getStatusCodeValue());
        verify(grievanceService, times(1)).getComments(1L);
    }

    @Test
    void testGetDepartmentWiseReport() {
        Map<String, Object> report = new HashMap<>();
        ApiResponse<Map<String, Object>> apiResponse = ApiResponse.success("Success", report);

        when(grievanceService.getDepartmentWiseReport()).thenReturn(apiResponse);

        ResponseEntity<ApiResponse<Map<String, Object>>> response =
                grievanceController.getDepartmentWiseReport();

        assertNotNull(response);
        assertEquals(200, response.getStatusCodeValue());
        verify(grievanceService, times(1)).getDepartmentWiseReport();
    }

    @Test
    void testGetCategoryWiseReport() {
        Map<String, Object> report = new HashMap<>();
        ApiResponse<Map<String, Object>> apiResponse = ApiResponse.success("Success", report);

        when(grievanceService.getCategoryWiseReport()).thenReturn(apiResponse);

        ResponseEntity<ApiResponse<Map<String, Object>>> response =
                grievanceController.getCategoryWiseReport();

        assertNotNull(response);
        assertEquals(200, response.getStatusCodeValue());
        verify(grievanceService, times(1)).getCategoryWiseReport();
    }

    @Test
    void testGetAverageResolutionTime() {
        Map<String, Object> report = new HashMap<>();
        ApiResponse<Map<String, Object>> apiResponse = ApiResponse.success("Success", report);

        when(grievanceService.getAverageResolutionTime()).thenReturn(apiResponse);

        ResponseEntity<ApiResponse<Map<String, Object>>> response =
                grievanceController.getAverageResolutionTime();

        assertNotNull(response);
        assertEquals(200, response.getStatusCodeValue());
        verify(grievanceService, times(1)).getAverageResolutionTime();
    }
}