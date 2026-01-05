package com.feedback.controller;

import com.grievance.common.dto.ApiResponse;
import com.feedback.dto.FeedbackRequest;
import com.feedback.dto.FeedbackStatsDTO;
import com.feedback.model.Feedback;
import com.feedback.service.FeedbackService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/feedbacks")
@RequiredArgsConstructor
public class FeedbackController {

    private final FeedbackService feedbackService;

    @PostMapping
    public ResponseEntity<ApiResponse<Feedback>> submitFeedback(
            @Valid @RequestBody FeedbackRequest request,
            @RequestHeader("X-User-Id") Long citizenId) {
        return ResponseEntity.ok(feedbackService.submitFeedback(request, citizenId));
    }

    @GetMapping("/grievance/{grievanceId}")
    public ResponseEntity<ApiResponse<Feedback>> getFeedbackByGrievance(@PathVariable Long grievanceId) {
        return ResponseEntity.ok(feedbackService.getFeedbackByGrievance(grievanceId));
    }

    @GetMapping("/grievance/{grievanceId}/exists")
    public ResponseEntity<ApiResponse<Boolean>> checkFeedbackExists(@PathVariable Long grievanceId) {
        return ResponseEntity.ok(feedbackService.checkFeedbackExists(grievanceId));
    }

    @GetMapping("/my-feedbacks")
    public ResponseEntity<ApiResponse<List<Feedback>>> getMyFeedbacks(
            @RequestHeader("X-User-Id") Long citizenId) {
        return ResponseEntity.ok(feedbackService.getMyFeedbacks(citizenId));
    }

    @GetMapping("/stats")
    public ResponseEntity<ApiResponse<FeedbackStatsDTO>> getFeedbackStats() {
        return ResponseEntity.ok(feedbackService.getFeedbackStats());
    }
}