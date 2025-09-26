package com.sketchnotes.learning.service;

import com.sketchnotes.learning.entity.Course;
import com.sketchnotes.learning.entity.CourseEnrollment;
import com.sketchnotes.learning.repository.CourseEnrollmentRepository;
import com.sketchnotes.learning.repository.CourseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EnrollmentService {
    private final CourseEnrollmentRepository enrollmentRepository;
    private final CourseRepository courseRepository;

    public CourseEnrollment enroll(long courseId, long userId) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new RuntimeException("Course not found with id: " + courseId));

        CourseEnrollment enrollment = new CourseEnrollment();
        enrollment.setCourse(course);
        enrollment.setUserId(userId);
        enrollment.setEnrolledAt(LocalDateTime.now());

        return enrollmentRepository.save(enrollment);
    }

    public Map<String, List<Course>> getUserCourseStatus(long userId) {
        List<Course> allCourses = courseRepository.findAll();
        List<CourseEnrollment> enrollments = enrollmentRepository.findByUserId(userId);

        Set<Long> enrolledCourseIds = enrollments.stream()
                .map(e -> e.getCourse().getCourseId())
                .collect(Collectors.toSet());

        List<Course> registered = allCourses.stream()
                .filter(c -> enrolledCourseIds.contains(c.getCourseId()))
                .collect(Collectors.toList());

        List<Course> notRegistered = allCourses.stream()
                .filter(c -> !enrolledCourseIds.contains(c.getCourseId()))
                .collect(Collectors.toList());

        Map<String, List<Course>> result = new HashMap<>();
        result.put("registered", registered);
        result.put("notRegistered", notRegistered);
        return result;
    }

}
