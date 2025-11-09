package com.sketchnotes.learning.repository;

import com.sketchnotes.learning.entity.CourseEnrollment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CourseEnrollmentRepository extends JpaRepository<CourseEnrollment, Long> {
    List<CourseEnrollment> findByUserId(Long userId);
    Optional<CourseEnrollment> findByCourse_CourseIdAndUserId(Long courseId, Long userId);
}