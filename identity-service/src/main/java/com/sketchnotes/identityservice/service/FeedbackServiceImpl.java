package com.sketchnotes.identityservice.service;

import com.sketchnotes.identityservice.client.LearningServiceClient;
import com.sketchnotes.identityservice.client.OrderServiceClient;
import com.sketchnotes.identityservice.dtos.ApiResponse;
import com.sketchnotes.identityservice.dtos.request.FeedbackRequest;
import com.sketchnotes.identityservice.dtos.response.*;
import com.sketchnotes.identityservice.exception.*;
import com.sketchnotes.identityservice.model.Feedback;
import com.sketchnotes.identityservice.model.User;
import com.sketchnotes.identityservice.repository.FeedbackRepository;
import com.sketchnotes.identityservice.repository.IUserRepository;
import com.sketchnotes.identityservice.service.interfaces.FeedbackService;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class FeedbackServiceImpl implements FeedbackService {
    
    private final FeedbackRepository feedbackRepository;
    private final IUserRepository userRepository;
    private final LearningServiceClient learningServiceClient;
    private final OrderServiceClient orderServiceClient;
    
    private static final int MIN_PROGRESS_FOR_RATING = 30;
    private static final int MIN_PROGRESS_FOR_COMMENT = 100;
    
    @Override
    @Transactional
    public FeedbackResponse createOrUpdateCourseFeedback(Long userId, FeedbackRequest request) {
        log.info("Creating/updating course feedback for user {} and course {}", userId, request.getCourseId());
        
        // Validate user exists
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
        
        // Validate enrollment and get progress
        CourseEnrollmentResponse enrollment = validateCourseEnrollment(userId, request.getCourseId());
        int progress = enrollment.getProgressPercent().intValue();
        
        // Validate progress requirements
        if (progress < MIN_PROGRESS_FOR_RATING) {
            throw new InsufficientProgressException(progress, MIN_PROGRESS_FOR_RATING);
        }
        
        // Validate comment requirements
        if (request.getComment() != null && !request.getComment().trim().isEmpty()) {
            if (progress < MIN_PROGRESS_FOR_COMMENT) {
                throw new CommentNotAllowedException(progress);
            }
        }
        
        // Check if feedback already exists
        Optional<Feedback> existingFeedback = feedbackRepository.findByUserIdAndCourseId(userId, request.getCourseId());
        
        Feedback feedback;
        if (existingFeedback.isPresent()) {
            // Update existing feedback
            feedback = existingFeedback.get();
            feedback.setRating(request.getRating());
            feedback.setComment(request.getComment());
            feedback.setProgressWhenSubmitted(progress);
            feedback.setIsEdited(true);
            log.info("Updating existing feedback with id {}", feedback.getId());
        } else {
            // Create new feedback
            feedback = Feedback.builder()
                    .user(user)
                    .courseId(request.getCourseId())
                    .rating(request.getRating())
                    .comment(request.getComment())
                    .progressWhenSubmitted(progress)
                    .isEdited(false)
                    .isVerified(true)
                    .build();
            log.info("Creating new feedback for course {}", request.getCourseId());
        }
        
        feedback = feedbackRepository.save(feedback);
        return mapToFeedbackResponse(feedback);
    }
    
    @Override
    @Transactional
    public FeedbackResponse createOrUpdateResourceFeedback(Long userId, FeedbackRequest request) {
        log.info("Creating/updating resource feedback for user {} and resource {}", userId, request.getResourceId());
        
        // Validate user exists
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
        
        // Validate purchase
        validateResourcePurchase(userId, request.getResourceId());
        
        // Check if feedback already exists
        Optional<Feedback> existingFeedback = feedbackRepository.findByUserIdAndResourceId(userId, request.getResourceId());
        
        Feedback feedback;
        if (existingFeedback.isPresent()) {
            // Update existing feedback
            feedback = existingFeedback.get();
            feedback.setRating(request.getRating());
            feedback.setComment(request.getComment());
            feedback.setIsEdited(true);
            log.info("Updating existing feedback with id {}", feedback.getId());
        } else {
            // Create new feedback
            feedback = Feedback.builder()
                    .user(user)
                    .resourceId(request.getResourceId())
                    .rating(request.getRating())
                    .comment(request.getComment())
                    .isEdited(false)
                    .isVerified(true)
                    .build();
            log.info("Creating new feedback for resource {}", request.getResourceId());
        }
        
        feedback = feedbackRepository.save(feedback);
        return mapToFeedbackResponse(feedback);
    }
    
    @Override
    public FeedbackStatsResponse getCourseFeedbackStats(Long courseId) {
        log.info("Getting feedback stats for course {}", courseId);
        
        List<Feedback> feedbacks = feedbackRepository.findByCourseIdOrderByCreatedAtDesc(courseId);
        Long totalFeedbacks = feedbackRepository.countByCourseId(courseId);
        Double averageRating = feedbackRepository.getAverageRatingByCourseId(courseId);
        
        // Get rating distribution
        List<FeedbackRepository.RatingCount> ratingCounts = feedbackRepository.getRatingDistributionByCourseId(courseId);
        Map<Integer, Long> ratingDistribution = new HashMap<>();
        
        // Initialize all ratings (1-5) with 0 count
        for (int i = 1; i <= 5; i++) {
            ratingDistribution.put(i, 0L);
        }
        
        // Fill in actual counts
        for (FeedbackRepository.RatingCount rc : ratingCounts) {
            ratingDistribution.put(rc.getRating(), rc.getCount());
        }
        
        List<FeedbackResponse> feedbackResponses = feedbacks.stream()
                .map(this::mapToFeedbackResponse)
                .collect(Collectors.toList());
        
        return FeedbackStatsResponse.builder()
                .totalFeedbacks(totalFeedbacks)
                .averageRating(averageRating != null ? Math.round(averageRating * 100.0) / 100.0 : 0.0)
                .ratingDistribution(ratingDistribution)
                .feedbacks(feedbackResponses)
                .build();
    }
    
    @Override
    public FeedbackStatsResponse getResourceFeedbackStats(Long resourceId) {
        log.info("Getting feedback stats for resource {}", resourceId);
        
        List<Feedback> feedbacks = feedbackRepository.findByResourceIdOrderByCreatedAtDesc(resourceId);
        Long totalFeedbacks = feedbackRepository.countByResourceId(resourceId);
        Double averageRating = feedbackRepository.getAverageRatingByResourceId(resourceId);
        
        // Get rating distribution
        List<FeedbackRepository.RatingCount> ratingCounts = feedbackRepository.getRatingDistributionByResourceId(resourceId);
        Map<Integer, Long> ratingDistribution = new HashMap<>();
        
        // Initialize all ratings (1-5) with 0 count
        for (int i = 1; i <= 5; i++) {
            ratingDistribution.put(i, 0L);
        }
        
        // Fill in actual counts
        for (FeedbackRepository.RatingCount rc : ratingCounts) {
            ratingDistribution.put(rc.getRating(), rc.getCount());
        }
        
        List<FeedbackResponse> feedbackResponses = feedbacks.stream()
                .map(this::mapToFeedbackResponse)
                .collect(Collectors.toList());
        
        return FeedbackStatsResponse.builder()
                .totalFeedbacks(totalFeedbacks)
                .averageRating(averageRating != null ? Math.round(averageRating * 100.0) / 100.0 : 0.0)
                .ratingDistribution(ratingDistribution)
                .feedbacks(feedbackResponses)
                .build();
    }
    
    @Override
    public FeedbackResponse getUserCourseFeedback(Long userId, Long courseId) {
        log.info("Getting user {} feedback for course {}", userId, courseId);
        
        return feedbackRepository.findByUserIdAndCourseId(userId, courseId)
                .map(this::mapToFeedbackResponse)
                .orElse(null);
    }
    
    @Override
    public FeedbackResponse getUserResourceFeedback(Long userId, Long resourceId) {
        log.info("Getting user {} feedback for resource {}", userId, resourceId);
        
        return feedbackRepository.findByUserIdAndResourceId(userId, resourceId)
                .map(this::mapToFeedbackResponse)
                .orElse(null);
    }
    
    // Helper methods
    
    private CourseEnrollmentResponse validateCourseEnrollment(Long userId, Long courseId) {
        try {
            ApiResponse<CourseEnrollmentResponse> response = learningServiceClient.getEnrollment(userId, courseId);
            
            if (response == null || response.getResult() == null) {
                throw new NotEnrolledException(userId, courseId);
            }
            
            return response.getResult();
        } catch (FeignException.NotFound e) {
            log.error("User {} is not enrolled in course {}", userId, courseId);
            throw new NotEnrolledException(userId, courseId);
        } catch (FeignException e) {
            log.error("Error calling learning-service: {}", e.getMessage());
            throw new RuntimeException("Failed to validate course enrollment", e);
        }
    }
    
    private void validateResourcePurchase(Long userId, Long resourceId) {
        try {
            ApiResponse<UserResourceResponse> response = orderServiceClient.getUserResource(userId, resourceId);
            
            if (response == null || response.getResult() == null) {
                throw new NotPurchasedException(userId, resourceId);
            }
            
            UserResourceResponse userResource = response.getResult();
            if (!userResource.getActive()) {
                throw new NotPurchasedException("Resource purchase is not active");
            }
        } catch (FeignException.NotFound e) {
            log.error("User {} has not purchased resource {}", userId, resourceId);
            throw new NotPurchasedException(userId, resourceId);
        } catch (FeignException e) {
            log.error("Error calling order-service: {}", e.getMessage());
            throw new RuntimeException("Failed to validate resource purchase", e);
        }
    }
    
    private FeedbackResponse mapToFeedbackResponse(Feedback feedback) {
        User user = feedback.getUser();
        
        return FeedbackResponse.builder()
                .id(feedback.getId())
                .userId(user.getId())
                .userFullName(user.getFirstName() + " " + user.getLastName())
                .userAvatarUrl(user.getAvatarUrl())
                .courseId(feedback.getCourseId())
                .resourceId(feedback.getResourceId())
                .rating(feedback.getRating())
                .comment(feedback.getComment())
                .progressWhenSubmitted(feedback.getProgressWhenSubmitted())
                .isEdited(feedback.getIsEdited())
                .createdAt(feedback.getCreatedAt())
                .updatedAt(feedback.getUpdatedAt())
                .build();
    }
}
