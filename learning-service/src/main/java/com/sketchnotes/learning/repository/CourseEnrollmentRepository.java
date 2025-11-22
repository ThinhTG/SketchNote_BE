package com.sketchnotes.learning.repository;

import com.sketchnotes.learning.entity.CourseEnrollment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

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

    @Query("SELECT ce.course.courseId, COUNT(ce) as enrollmentCount FROM CourseEnrollment ce GROUP BY ce.course.courseId ORDER BY enrollmentCount DESC")
    List<Object[]> findTopSellingCourses();

    @Query("SELECT COUNT(ce) FROM CourseEnrollment ce")
    long countTotalEnrollments();
}