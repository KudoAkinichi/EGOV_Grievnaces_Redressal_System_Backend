package com.feedback.service;

import com.grievance.common.dto.ApiResponse;
import com.grievance.common.exception.ResourceNotFoundException;
import com.feedback.dto.FeedbackRequest;
import com.feedback.dto.FeedbackStatsDTO;
import com.feedback.model.Feedback;
import com.feedback.repository.FeedbackRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FeedbackService {

    private final FeedbackRepository feedbackRepository;

    @Transactional
    public ApiResponse<Feedback> submitFeedback(FeedbackRequest request, Long citizenId) {
        // Check if feedback already exists for this grievance
        if (feedbackRepository.existsByGrievanceId(request.getGrievanceId())) {
            return ApiResponse.error("Feedback already submitted for this grievance");
        }

        Feedback feedback = new Feedback();
        feedback.setGrievanceId(request.getGrievanceId());
        feedback.setCitizenId(citizenId);
        feedback.setRating(request.getRating());
        feedback.setComment(request.getComment());

        Feedback savedFeedback = feedbackRepository.save(feedback);
        return ApiResponse.success("Feedback submitted successfully", savedFeedback);
    }

    public ApiResponse<Feedback> getFeedbackByGrievance(Long grievanceId) {
        Feedback feedback = feedbackRepository.findByGrievanceId(grievanceId)
                .orElseThrow(() -> new ResourceNotFoundException("Feedback not found for this grievance"));
        return ApiResponse.success("Feedback fetched successfully", feedback);
    }

    public ApiResponse<List<Feedback>> getMyFeedbacks(Long citizenId) {
        List<Feedback> feedbacks = feedbackRepository.findByCitizenId(citizenId);
        return ApiResponse.success("Feedbacks fetched successfully", feedbacks);
    }

    public ApiResponse<FeedbackStatsDTO> getFeedbackStats() {
        Double avgRating = feedbackRepository.getAverageRating();
        Long total = feedbackRepository.count();

        FeedbackStatsDTO stats = new FeedbackStatsDTO();
        stats.setAverageRating(avgRating != null ? avgRating : 0.0);
        stats.setTotalFeedbacks(total);
        stats.setRating1Count(feedbackRepository.countByRating(1));
        stats.setRating2Count(feedbackRepository.countByRating(2));
        stats.setRating3Count(feedbackRepository.countByRating(3));
        stats.setRating4Count(feedbackRepository.countByRating(4));
        stats.setRating5Count(feedbackRepository.countByRating(5));

        return ApiResponse.success("Feedback stats fetched successfully", stats);
    }

    public ApiResponse<Boolean> checkFeedbackExists(Long grievanceId) {
        boolean exists = feedbackRepository.existsByGrievanceId(grievanceId);
        return ApiResponse.success("Feedback existence checked", exists);
    }
}