package com.grievance.controller;

import com.grievance.common.dto.ApiResponse;
import com.grievance.dto.CategoryRequest;
import com.grievance.dto.DepartmentRequest;
import com.grievance.model.Category;
import com.grievance.model.Department;
import com.grievance.service.CategoryService;
import com.grievance.service.DepartmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/departments")
@RequiredArgsConstructor
public class DepartmentController {

    private final DepartmentService departmentService;
    private final CategoryService categoryService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<Department>>> getAllDepartments() {
        return ResponseEntity.ok(departmentService.getAllDepartments());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Department>> getDepartmentById(@PathVariable Long id) {
        return ResponseEntity.ok(departmentService.getDepartmentById(id));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Department>> createDepartment(
            @Valid @RequestBody DepartmentRequest request) {
        return ResponseEntity.ok(departmentService.createDepartment(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Department>> updateDepartment(
            @PathVariable Long id,
            @Valid @RequestBody DepartmentRequest request) {
        return ResponseEntity.ok(departmentService.updateDepartment(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<?>> deleteDepartment(@PathVariable Long id) {
        return ResponseEntity.ok(departmentService.deleteDepartment(id));
    }

    // Category endpoints under department
    @GetMapping("/{id}/categories")
    public ResponseEntity<ApiResponse<List<Category>>> getCategoriesByDepartment(@PathVariable Long id) {
        return ResponseEntity.ok(categoryService.getCategoriesByDepartment(id));
    }

    @PostMapping("/{id}/categories")
    public ResponseEntity<ApiResponse<Category>> createCategory(
            @PathVariable Long id,
            @Valid @RequestBody CategoryRequest request) {
        request.setDepartmentId(id); // Override with path variable
        return ResponseEntity.ok(categoryService.createCategory(request));
    }

    @GetMapping("/categories")
    public ResponseEntity<ApiResponse<List<Category>>> getAllCategories() {
        return ResponseEntity.ok(categoryService.getAllCategories());
    }
}