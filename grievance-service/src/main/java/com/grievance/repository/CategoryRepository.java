package com.grievance.repository;

import com.grievance.model.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

    List<Category> findByDepartmentId(Long departmentId);

    List<Category> findByDepartmentIdAndIsActiveTrue(Long departmentId);

    List<Category> findByIsActiveTrue();

    boolean existsByDepartmentIdAndName(Long departmentId, String name);
}