package com.sketchnotes.learning.repository;

import com.sketchnotes.learning.entity.CourseEnrollment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CourseEnrollmentRepository extends JpaRepository<CourseEnrollment, Long> {
    List<CourseEnrollment> findByUserId(Long userId);
    Optional<CourseEnrollment> findByCourse_CourseIdAndUserId(Long courseId, Long userId);
    Optional<CourseEnrollment> findByUserIdAndCourse_CourseId(Long userId, Long courseId);
    
    // Tìm tất cả enrollment có status PAYMENT_FAILED của một user
    @Query("SELECT e FROM CourseEnrollment e LEFT JOIN FETCH e.course WHERE e.userId = :userId AND e.paymentStatus = 'PAYMENT_FAILED'")
    List<CourseEnrollment> findFailedPaymentsByUserId(Long userId);
}