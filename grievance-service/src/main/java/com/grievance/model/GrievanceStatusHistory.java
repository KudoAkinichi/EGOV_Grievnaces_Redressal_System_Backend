package com.grievance.model;

import com.grievance.common.enums.GrievanceStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "grievance_status_history")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GrievanceStatusHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long grievanceId;

    @Enumerated(EnumType.STRING)
    private GrievanceStatus oldStatus;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private GrievanceStatus newStatus;

    @Column(nullable = false)
    private Long changedBy;

    @Column(columnDefinition = "TEXT")
    private String remarks;

    @CreationTimestamp
    private LocalDateTime createdAt;
}