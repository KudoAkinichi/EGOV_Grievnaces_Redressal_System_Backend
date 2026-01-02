package com.grievance.repository;

import com.grievance.common.enums.GrievanceStatus;
import com.grievance.model.Grievance;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface GrievanceRepository extends JpaRepository<Grievance, Long> {

    Optional<Grievance> findByGrievanceNumber(String grievanceNumber);

    Page<Grievance> findByCitizenId(Long citizenId, Pageable pageable);

    Page<Grievance> findByAssignedOfficerId(Long officerId, Pageable pageable);

    Page<Grievance> findByDepartmentId(Long departmentId, Pageable pageable);

    Page<Grievance> findByStatus(GrievanceStatus status, Pageable pageable);

    @Query("SELECT g FROM Grievance g WHERE g.citizenId = :citizenId AND g.status = :status")
    List<Grievance> findByCitizenIdAndStatus(@Param("citizenId") Long citizenId,
                                             @Param("status") GrievanceStatus status);

    @Query("SELECT g FROM Grievance g WHERE g.assignedOfficerId = :officerId AND g.status = :status")
    List<Grievance> findByAssignedOfficerIdAndStatus(@Param("officerId") Long officerId,
                                                     @Param("status") GrievanceStatus status);

    @Query("SELECT COUNT(g) FROM Grievance g WHERE g.assignedOfficerId = :officerId AND g.status NOT IN ('CLOSED', 'RESOLVED')")
    Long countActiveGrievancesByOfficer(@Param("officerId") Long officerId);

    @Query("SELECT g FROM Grievance g WHERE g.status = 'RESOLVED' AND g.autoEscalationTime < :currentTime")
    List<Grievance> findGrievancesForAutoEscalation(@Param("currentTime") LocalDateTime currentTime);

    @Query("SELECT COUNT(g) FROM Grievance g WHERE g.status = :status")
    Long countByStatus(@Param("status") GrievanceStatus status);

    @Query("SELECT COUNT(g) FROM Grievance g WHERE g.departmentId = :deptId")
    Long countByDepartmentId(@Param("deptId") Long deptId);

    @Query("SELECT COUNT(g) FROM Grievance g WHERE g.departmentId = :deptId AND g.status = :status")
    Long countByDepartmentIdAndStatus(@Param("deptId") Long deptId, @Param("status") GrievanceStatus status);
}