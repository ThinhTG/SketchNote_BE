package com.sketchnotes.identityservice.repository;

import com.sketchnotes.identityservice.model.Feedback;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
public interface FeedbackRepository extends JpaRepository<Feedback, Long> {
    
    // Find feedback by user and course
    Optional<Feedback> findByUserIdAndCourseId(Long userId, Long courseId);
    
    // Find feedback by user and resource
    Optional<Feedback> findByUserIdAndResourceId(Long userId, Long resourceId);
    
    // Find all feedbacks for a course, ordered by creation date (newest first)
    List<Feedback> findByCourseIdOrderByCreatedAtDesc(Long courseId);
    
    // Find all feedbacks for a resource, ordered by creation date (newest first)
    List<Feedback> findByResourceIdOrderByCreatedAtDesc(Long resourceId);
    
    // Get average rating for a course
    @Query("SELECT AVG(f.rating) FROM Feedback f WHERE f.courseId = :courseId")
    Double getAverageRatingByCourseId(@Param("courseId") Long courseId);
    
    // Get average rating for a resource
    @Query("SELECT AVG(f.rating) FROM Feedback f WHERE f.resourceId = :resourceId")
    Double getAverageRatingByResourceId(@Param("resourceId") Long resourceId);
    
    // Count feedbacks for a course
    Long countByCourseId(Long courseId);
    
    // Count feedbacks for a resource
    Long countByResourceId(Long resourceId);
    
    // Get rating distribution for a course (count per star rating)
    @Query("SELECT f.rating as rating, COUNT(f) as count FROM Feedback f WHERE f.courseId = :courseId GROUP BY f.rating")
    List<RatingCount> getRatingDistributionByCourseId(@Param("courseId") Long courseId);
    
    // Get rating distribution for a resource (count per star rating)
    @Query("SELECT f.rating as rating, COUNT(f) as count FROM Feedback f WHERE f.resourceId = :resourceId GROUP BY f.rating")
    List<RatingCount> getRatingDistributionByResourceId(@Param("resourceId") Long resourceId);
    
    // Projection interface for rating distribution
    interface RatingCount {
        Integer getRating();
        Long getCount();
    }
}
