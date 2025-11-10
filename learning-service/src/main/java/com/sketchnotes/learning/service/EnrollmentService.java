package com.sketchnotes.learning.service;

import com.sketchnotes.learning.client.IdentityClient;
import com.sketchnotes.learning.client.TransactionResponse;
import com.sketchnotes.learning.client.TransactionType;
import com.sketchnotes.learning.dto.ApiResponse;
import com.sketchnotes.learning.dto.CourseDTO;
import com.sketchnotes.learning.dto.EnrollmentDTO;
import com.sketchnotes.learning.dto.RetryPaymentRequest;
import com.sketchnotes.learning.dto.enums.EnrollmentStatus;
import com.sketchnotes.learning.entity.Course;
import com.sketchnotes.learning.entity.CourseEnrollment;
import com.sketchnotes.learning.mapper.CourseMapper;
import com.sketchnotes.learning.mapper.EnrollmentMapper;
import com.sketchnotes.learning.repository.CourseEnrollmentRepository;
import com.sketchnotes.learning.repository.CourseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    private final EnrollmentMapper enrollmentMapper;
    private final CourseMapper courseMapper;
    private final IdentityClient identityClient;

    public EnrollmentDTO enroll(long courseId, long userId) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new RuntimeException("Course not found with id: " + courseId));
        // Kiểm tra xem user đã đăng ký khóa học này chưa
        if (enrollmentRepository.findByCourse_CourseIdAndUserId(courseId, userId).isPresent()) {
            throw new RuntimeException("User already enrolled in this course");
        }
        CourseEnrollment enrollment = new CourseEnrollment();
        enrollment.setCourse(course);
        enrollment.setUserId(userId);
        enrollment.setEnrolledAt(LocalDateTime.now());
        enrollment.setStatus(EnrollmentStatus.ENROLLED);
        CourseEnrollment saved = enrollmentRepository.save(enrollment);
        // Tăng studentCount của course lên 1 khi enroll thành công
        course.setStudentCount(course.getStudentCount() + 1);
        courseRepository.save(course);
        return enrollmentMapper.toDTO(enrollment);
    }

    public Map<String, List<CourseDTO>> getUserCourseStatus(long userId) {
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

        Map<String, List<CourseDTO>> result = new HashMap<>();
        result.put("registered", courseMapper.toDTOList(registered));
        result.put("notRegistered", courseMapper.toDTOList(notRegistered));
        return result;
    }


}
