package com.grievance.auth.repository;

import com.grievance.auth.model.User;
import com.grievance.common.enums.Role;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    boolean existsByAadhaarNumber(String aadhaarNumber);

    List<User> findByRole(Role role);

    List<User> findByDepartmentId(Long departmentId);

    Page<User> findByRole(Role role, Pageable pageable);

    Page<User> findByDepartmentId(Long departmentId, Pageable pageable);

    @Query("SELECT u FROM User u WHERE u.role = :role AND u.departmentId = :deptId AND u.isActive = true")
    List<User> findActiveOfficersByDepartment(@Param("role") Role role, @Param("deptId") Long deptId);

    @Query("SELECT u FROM User u WHERE u.isActive = true")
    Page<User> findAllActive(Pageable pageable);
}