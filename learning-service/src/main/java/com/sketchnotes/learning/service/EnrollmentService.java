package com.sketchnotes.learning.service;

import com.sketchnotes.learning.entity.Course;
import com.sketchnotes.learning.entity.CourseEnrollment;
import com.sketchnotes.learning.repository.CourseEnrollmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class EnrollmentService {
    private final CourseEnrollmentRepository enrollmentRepository;

    public CourseEnrollment enroll(long courseId, long userId) {
        CourseEnrollment enrollment = new CourseEnrollment();
        enrollment.setCourse(new Course(courseId));
        enrollment.setUserId(userId);
        enrollment.setEnrolledAt(LocalDateTime.now());
        return enrollmentRepository.save(enrollment);
    }
}
