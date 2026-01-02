package com.grievance.repository;

import com.grievance.model.GrievanceStatusHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GrievanceStatusHistoryRepository extends JpaRepository<GrievanceStatusHistory, Long> {

    List<GrievanceStatusHistory> findByGrievanceIdOrderByCreatedAtDesc(Long grievanceId);
}