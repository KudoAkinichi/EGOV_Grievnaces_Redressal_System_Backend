package com.grievance.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DashboardStatsDTO {

    private Long totalGrievances;
    private Long openIssues;
    private Long assignedToMe;
    private List<Long> assignedToMeIds;
    private Long inReview;
    private Long resolved;
    private Long closed;
    private Long escalated;
    private Map<String, Long> statusBreakdown;
}