package com.grievance.controller;

import com.grievance.common.dto.ApiResponse;
import com.grievance.dto.CategoryRequest;
import com.grievance.dto.DepartmentRequest;
import com.grievance.model.Category;
import com.grievance.model.Department;
import com.grievance.service.CategoryService;
import com.grievance.service.DepartmentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DepartmentControllerTest {

    @Mock
    private DepartmentService departmentService;

    @Mock
    private CategoryService categoryService;

    @InjectMocks
    private DepartmentController departmentController;

    private Department department;
    private DepartmentRequest departmentRequest;
    private Category category;
    private CategoryRequest categoryRequest;

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

        category = new Category();
        category.setId(1L);
        category.setDepartmentId(1L);
        category.setName("Network Issues");
        category.setDescription("Network related problems");
        category.setIsActive(true);

        categoryRequest = new CategoryRequest();
        categoryRequest.setDepartmentId(1L);
        categoryRequest.setName("Network Issues");
        categoryRequest.setDescription("Network related problems");
    }

    @Test
    void testGetAllDepartments() {
        List<Department> departments = Arrays.asList(department);
        ApiResponse<List<Department>> apiResponse = ApiResponse.success("Success", departments);

        when(departmentService.getAllDepartments()).thenReturn(apiResponse);

        ResponseEntity<ApiResponse<List<Department>>> response = departmentController.getAllDepartments();

        assertNotNull(response);
        assertEquals(200, response.getStatusCodeValue());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().getData().size());
        verify(departmentService, times(1)).getAllDepartments();
    }

    @Test
    void testGetDepartmentById() {
        ApiResponse<Department> apiResponse = ApiResponse.success("Success", department);

        when(departmentService.getDepartmentById(1L)).thenReturn(apiResponse);

        ResponseEntity<ApiResponse<Department>> response = departmentController.getDepartmentById(1L);

        assertNotNull(response);
        assertEquals(200, response.getStatusCodeValue());
        assertNotNull(response.getBody());
        assertEquals("IT Department", response.getBody().getData().getName());
        verify(departmentService, times(1)).getDepartmentById(1L);
    }

    @Test
    void testCreateDepartment() {
        ApiResponse<Department> apiResponse = ApiResponse.success("Created", department);

        when(departmentService.createDepartment(any(DepartmentRequest.class))).thenReturn(apiResponse);

        ResponseEntity<ApiResponse<Department>> response = departmentController.createDepartment(departmentRequest);

        assertNotNull(response);
        assertEquals(200, response.getStatusCodeValue());
        assertNotNull(response.getBody());
        assertEquals("IT Department", response.getBody().getData().getName());
        verify(departmentService, times(1)).createDepartment(any(DepartmentRequest.class));
    }

    @Test
    void testUpdateDepartment() {
        ApiResponse<Department> apiResponse = ApiResponse.success("Updated", department);

        when(departmentService.updateDepartment(eq(1L), any(DepartmentRequest.class))).thenReturn(apiResponse);

        ResponseEntity<ApiResponse<Department>> response = departmentController.updateDepartment(1L, departmentRequest);

        assertNotNull(response);
        assertEquals(200, response.getStatusCodeValue());
        verify(departmentService, times(1)).updateDepartment(eq(1L), any(DepartmentRequest.class));
    }

    @Test
    void testGetCategoriesByDepartment() {
        List<Category> categories = Arrays.asList(category);
        ApiResponse<List<Category>> apiResponse = ApiResponse.success("Success", categories);

        when(categoryService.getCategoriesByDepartment(1L)).thenReturn(apiResponse);

        ResponseEntity<ApiResponse<List<Category>>> response = departmentController.getCategoriesByDepartment(1L);

        assertNotNull(response);
        assertEquals(200, response.getStatusCodeValue());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().getData().size());
        verify(categoryService, times(1)).getCategoriesByDepartment(1L);
    }

    @Test
    void testCreateCategory() {
        ApiResponse<Category> apiResponse = ApiResponse.success("Created", category);

        when(categoryService.createCategory(any(CategoryRequest.class))).thenReturn(apiResponse);

        ResponseEntity<ApiResponse<Category>> response = departmentController.createCategory(1L, categoryRequest);

        assertNotNull(response);
        assertEquals(200, response.getStatusCodeValue());
        assertEquals(1L, categoryRequest.getDepartmentId());
        verify(categoryService, times(1)).createCategory(any(CategoryRequest.class));
    }

    @Test
    void testGetAllCategories() {
        List<Category> categories = Arrays.asList(category);
        ApiResponse<List<Category>> apiResponse = ApiResponse.success("Success", categories);

        when(categoryService.getAllCategories()).thenReturn(apiResponse);

        ResponseEntity<ApiResponse<List<Category>>> response = departmentController.getAllCategories();

        assertNotNull(response);
        assertEquals(200, response.getStatusCodeValue());
        verify(categoryService, times(1)).getAllCategories();
    }
}