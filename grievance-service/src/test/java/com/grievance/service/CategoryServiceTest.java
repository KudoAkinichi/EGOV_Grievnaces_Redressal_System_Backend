package com.grievance.service;

import com.grievance.common.dto.ApiResponse;
import com.grievance.common.exception.ResourceNotFoundException;
import com.grievance.dto.CategoryRequest;
import com.grievance.model.Category;
import com.grievance.repository.CategoryRepository;
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
class CategoryServiceTest {

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private DepartmentRepository departmentRepository;

    @InjectMocks
    private CategoryService categoryService;

    private Category category;
    private CategoryRequest categoryRequest;

    @BeforeEach
    void setUp() {
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
    void testGetAllCategories() {
        List<Category> categories = Arrays.asList(category);
        when(categoryRepository.findByIsActiveTrue()).thenReturn(categories);

        ApiResponse<List<Category>> response = categoryService.getAllCategories();

        assertNotNull(response);
        assertTrue(response.getSuccess());
        assertEquals(1, response.getData().size());
        verify(categoryRepository, times(1)).findByIsActiveTrue();
    }

    @Test
    void testGetCategoriesByDepartment() {
        List<Category> categories = Arrays.asList(category);
        when(categoryRepository.findByDepartmentIdAndIsActiveTrue(1L)).thenReturn(categories);

        ApiResponse<List<Category>> response = categoryService.getCategoriesByDepartment(1L);

        assertNotNull(response);
        assertTrue(response.getSuccess());
        assertEquals(1, response.getData().size());
        verify(categoryRepository, times(1)).findByDepartmentIdAndIsActiveTrue(1L);
    }

    @Test
    void testCreateCategory_Success() {
        when(departmentRepository.existsById(1L)).thenReturn(true);
        when(categoryRepository.existsByDepartmentIdAndName(1L, "Network Issues")).thenReturn(false);
        when(categoryRepository.save(any(Category.class))).thenReturn(category);

        ApiResponse<Category> response = categoryService.createCategory(categoryRequest);

        assertNotNull(response);
        assertTrue(response.getSuccess());
        assertEquals("Network Issues", response.getData().getName());
        verify(departmentRepository, times(1)).existsById(1L);
        verify(categoryRepository, times(1)).existsByDepartmentIdAndName(1L, "Network Issues");
        verify(categoryRepository, times(1)).save(any(Category.class));
    }

    @Test
    void testCreateCategory_DepartmentNotFound() {
        when(departmentRepository.existsById(1L)).thenReturn(false);

        ApiResponse<Category> response = categoryService.createCategory(categoryRequest);

        assertNotNull(response);
        assertFalse(response.getSuccess());
        assertEquals("Department not found", response.getMessage());
        verify(departmentRepository, times(1)).existsById(1L);
        verify(categoryRepository, never()).save(any(Category.class));
    }

    @Test
    void testCreateCategory_DuplicateName() {
        when(departmentRepository.existsById(1L)).thenReturn(true);
        when(categoryRepository.existsByDepartmentIdAndName(1L, "Network Issues")).thenReturn(true);

        ApiResponse<Category> response = categoryService.createCategory(categoryRequest);

        assertNotNull(response);
        assertFalse(response.getSuccess());
        assertEquals("Category with this name already exists in the department", response.getMessage());
        verify(departmentRepository, times(1)).existsById(1L);
        verify(categoryRepository, times(1)).existsByDepartmentIdAndName(1L, "Network Issues");
        verify(categoryRepository, never()).save(any(Category.class));
    }

    @Test
    void testDeleteCategory_Success() {
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(categoryRepository.save(any(Category.class))).thenReturn(category);

        ApiResponse<?> response = categoryService.deleteCategory(1L);

        assertNotNull(response);
        assertTrue(response.getSuccess());
        verify(categoryRepository, times(1)).findById(1L);
        verify(categoryRepository, times(1)).save(any(Category.class));
    }

    @Test
    void testDeleteCategory_NotFound() {
        when(categoryRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> {
            categoryService.deleteCategory(1L);
        });

        verify(categoryRepository, times(1)).findById(1L);
        verify(categoryRepository, never()).save(any(Category.class));
    }
}