package com.grievance.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "grievance_documents")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GrievanceDocument {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long grievanceId;

    @Column(nullable = false)
    private String fileName;

    private String fileType;

    private Long fileSize;

    @Lob
    @Column(columnDefinition = "LONGBLOB")
    private byte[] fileData;

    @Column(nullable = false)
    private Long uploadedBy;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UploadedByRole uploadedByRole;

    @CreationTimestamp
    private LocalDateTime createdAt;

    public enum UploadedByRole {
        CITIZEN, DEPT_OFFICER, SUPERVISOR
    }
}