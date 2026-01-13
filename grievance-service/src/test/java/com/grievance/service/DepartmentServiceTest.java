package com.grievance.service;

import com.grievance.common.dto.ApiResponse;
import com.grievance.common.exception.ResourceNotFoundException;
import com.grievance.dto.DepartmentRequest;
import com.grievance.model.Department;
import com.grievance.repository.DepartmentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DepartmentServiceTest {

    @Mock
    private DepartmentRepository departmentRepository;

    @InjectMocks
    private DepartmentService departmentService;

    private Department department;
    private DepartmentRequest departmentRequest;

    @BeforeEach
    void setUp() {
        department = new Department();
        department.setId(1L);
        department.setName("IT Department");
        department.setDescription("IT Support");
        department.setContactEmail("it@example.com");
        department.setIsActive(true);

        departmentRequest = new DepartmentRequest();
        departmentRequest.setName("IT Department");
        departmentRequest.setDescription("IT Support");
        departmentRequest.setContactEmail("it@example.com");
    }

    @Test
    void testGetAllDepartments() {
        List<Department> departments = Arrays.asList(department);
        when(departmentRepository.findByIsActiveTrue()).thenReturn(departments);

        ApiResponse<List<Department>> response = departmentService.getAllDepartments();

        assertNotNull(response);
        assertTrue(response.getSuccess());
        assertEquals(1, response.getData().size());
        verify(departmentRepository, times(1)).findByIsActiveTrue();
    }

    @Test
    void testGetDepartmentById_Success() {
        when(departmentRepository.findById(1L)).thenReturn(Optional.of(department));

        ApiResponse<Department> response = departmentService.getDepartmentById(1L);

        assertNotNull(response);
        assertTrue(response.getSuccess());
        assertEquals("IT Department", response.getData().getName());
        verify(departmentRepository, times(1)).findById(1L);
    }

    @Test
    void testGetDepartmentById_NotFound() {
        when(departmentRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> {
            departmentService.getDepartmentById(1L);
        });

        verify(departmentRepository, times(1)).findById(1L);
    }

    @Test
    void testCreateDepartment_Success() {
        when(departmentRepository.existsByName(departmentRequest.getName())).thenReturn(false);
        when(departmentRepository.save(any(Department.class))).thenReturn(department);

        ApiResponse<Department> response = departmentService.createDepartment(departmentRequest);

        assertNotNull(response);
        assertTrue(response.getSuccess());
        assertEquals("IT Department", response.getData().getName());
        verify(departmentRepository, times(1)).existsByName(departmentRequest.getName());
        verify(departmentRepository, times(1)).save(any(Department.class));
    }

    @Test
    void testCreateDepartment_DuplicateName() {
        when(departmentRepository.existsByName(departmentRequest.getName())).thenReturn(true);

        ApiResponse<Department> response = departmentService.createDepartment(departmentRequest);

        assertNotNull(response);
        assertFalse(response.getSuccess());
        assertEquals("Department with this name already exists", response.getMessage());
        verify(departmentRepository, times(1)).existsByName(departmentRequest.getName());
        verify(departmentRepository, never()).save(any(Department.class));
    }

    @Test
    void testUpdateDepartment_NotFound() {
        when(departmentRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> {
            departmentService.updateDepartment(1L, departmentRequest);
        });

        verify(departmentRepository, times(1)).findById(1L);
    }

    @Test
    void testUpdateDepartment_DuplicateName() {
        departmentRequest.setName("New Name");
        when(departmentRepository.findById(1L)).thenReturn(Optional.of(department));
        when(departmentRepository.existsByName("New Name")).thenReturn(true);

        ApiResponse<Department> response = departmentService.updateDepartment(1L, departmentRequest);

        assertNotNull(response);
        assertFalse(response.getSuccess());
        assertEquals("Department with this name already exists", response.getMessage());
        verify(departmentRepository, times(1)).findById(1L);
        verify(departmentRepository, never()).save(any(Department.class));
    }

    @Test
    void testDeleteDepartment_Success() {
        when(departmentRepository.findById(1L)).thenReturn(Optional.of(department));
        when(departmentRepository.save(any(Department.class))).thenReturn(department);

        ApiResponse<?> response = departmentService.deleteDepartment(1L);

        assertNotNull(response);
        assertTrue(response.getSuccess());
        verify(departmentRepository, times(1)).findById(1L);
        verify(departmentRepository, times(1)).save(any(Department.class));
    }

    @Test
    void testDeleteDepartment_NotFound() {
        when(departmentRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> {
            departmentService.deleteDepartment(1L);
        });

        verify(departmentRepository, times(1)).findById(1L);
    }
}