package com.grievance.service;

import com.grievance.common.dto.ApiResponse;
import com.grievance.common.exception.ResourceNotFoundException;
import com.grievance.dto.DepartmentRequest;
import com.grievance.model.Department;
import com.grievance.repository.DepartmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DepartmentService {

    private final DepartmentRepository departmentRepository;

    public ApiResponse<List<Department>> getAllDepartments() {
        List<Department> departments = departmentRepository.findByIsActiveTrue();
        return ApiResponse.success("Departments fetched successfully", departments);
    }

    public ApiResponse<Department> getDepartmentById(Long id) {
        Department department = departmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Department not found with id: " + id));
        return ApiResponse.success("Department fetched successfully", department);
    }

    @Transactional
    public ApiResponse<Department> createDepartment(DepartmentRequest request) {
        if (departmentRepository.existsByName(request.getName())) {
            return ApiResponse.error("Department with this name already exists");
        }

        Department department = new Department();
        department.setName(request.getName());
        department.setDescription(request.getDescription());
        department.setContactEmail(request.getContactEmail());
        department.setIsActive(true);

        Department savedDepartment = departmentRepository.save(department);
        return ApiResponse.success("Department created successfully", savedDepartment);
    }

    @Transactional
    public ApiResponse<Department> updateDepartment(Long id, DepartmentRequest request) {
        Department department = departmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Department not found"));

        // Check if name is being changed and if new name already exists
        if (!department.getName().equals(request.getName()) &&
                departmentRepository.existsByName(request.getName())) {
            return ApiResponse.error("Department with this name already exists");
        }

        department.setName(request.getName());
        department.setDescription(request.getDescription());
        department.setContactEmail(request.getContactEmail());

        Department updatedDepartment = departmentRepository.save(department);
        return ApiResponse.success("Department updated successfully", updatedDepartment);
    }

    @Transactional
    public ApiResponse<?> deleteDepartment(Long id) {
        Department department = departmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Department not found"));

        department.setIsActive(false); // Soft delete
        departmentRepository.save(department);

        return ApiResponse.success("Department deleted successfully", null);
    }
}