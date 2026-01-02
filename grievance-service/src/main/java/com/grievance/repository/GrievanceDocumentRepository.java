package com.grievance.repository;

import com.grievance.model.GrievanceDocument;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GrievanceDocumentRepository extends JpaRepository<GrievanceDocument, Long> {

    List<GrievanceDocument> findByGrievanceId(Long grievanceId);
}