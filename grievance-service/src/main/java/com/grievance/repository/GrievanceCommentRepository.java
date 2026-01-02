package com.grievance.repository;

import com.grievance.model.GrievanceComment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GrievanceCommentRepository extends JpaRepository<GrievanceComment, Long> {

    List<GrievanceComment> findByGrievanceIdOrderByCreatedAtAsc(Long grievanceId);
}