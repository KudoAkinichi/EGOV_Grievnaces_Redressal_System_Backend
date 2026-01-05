package com.feedback.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FeedbackStatsDTO {
    private Double averageRating;
    private Long totalFeedbacks;
    private Long rating1Count;
    private Long rating2Count;
    private Long rating3Count;
    private Long rating4Count;
    private Long rating5Count;
}