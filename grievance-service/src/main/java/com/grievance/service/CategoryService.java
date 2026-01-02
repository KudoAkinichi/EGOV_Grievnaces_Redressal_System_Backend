package com.grievance.service;

import com.grievance.common.dto.ApiResponse;
import com.grievance.common.exception.ResourceNotFoundException;
import com.grievance.dto.CategoryRequest;
import com.grievance.model.Category;
import com.grievance.repository.CategoryRepository;
import com.grievance.repository.DepartmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final DepartmentRepository departmentRepository;

    public ApiResponse<List<Category>> getAllCategories() {
        List<Category> categories = categoryRepository.findByIsActiveTrue();
        return ApiResponse.success("Categories fetched successfully", categories);
    }

    public ApiResponse<List<Category>> getCategoriesByDepartment(Long departmentId) {
        List<Category> categories = categoryRepository.findByDepartmentIdAndIsActiveTrue(departmentId);
        return ApiResponse.success("Categories fetched successfully", categories);
    }

    @Transactional
    public ApiResponse<Category> createCategory(CategoryRequest request) {
        // Verify department exists
        if (!departmentRepository.existsById(request.getDepartmentId())) {
            return ApiResponse.error("Department not found");
        }

        // Check for duplicate category name in same department
        if (categoryRepository.existsByDepartmentIdAndName(request.getDepartmentId(), request.getName())) {
            return ApiResponse.error("Category with this name already exists in the department");
        }

        Category category = new Category();
        category.setDepartmentId(request.getDepartmentId());
        category.setName(request.getName());
        category.setDescription(request.getDescription());
        category.setIsActive(true);

        Category savedCategory = categoryRepository.save(category);
        return ApiResponse.success("Category created successfully", savedCategory);
    }

    @Transactional
    public ApiResponse<?> deleteCategory(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));

        category.setIsActive(false); // Soft delete
        categoryRepository.save(category);

        return ApiResponse.success("Category deleted successfully", null);
    }
}