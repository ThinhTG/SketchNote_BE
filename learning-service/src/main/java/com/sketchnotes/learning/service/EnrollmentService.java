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
        enrollment.setStatus(EnrollmentStatus.PENDING);
        enrollment.setPaymentStatus("PENDING_PAYMENT");
        CourseEnrollment saved = enrollmentRepository.save(enrollment);

        try {
            // Gọi API thanh toán từ identity service
            ApiResponse<TransactionResponse> paymentResponse = identityClient.chargeCourse(
                    userId,
                    course.getPrice(),
                    "Payment for course: " + course.getTitle(),
                    TransactionType.COURSE_PAYMENT
            );

            if (paymentResponse.getResult() == null) {
                enrollment.setStatus(EnrollmentStatus.CANCELLED);
                enrollment.setPaymentStatus("PAYMENT_FAILED");
                enrollmentRepository.save(enrollment);
                throw new RuntimeException("Payment failed: " + paymentResponse.getMessage());
            }

            // Thanh toán thành công
            enrollment.setStatus(EnrollmentStatus.ENROLLED);
            enrollment.setPaymentStatus("PAYMENT_SUCCESS");
            enrollment = enrollmentRepository.save(enrollment);

            // Tăng studentCount của course lên 1 khi enroll thành công
            course.setStudentCount(course.getStudentCount() + 1);
            courseRepository.save(course);

            return enrollmentMapper.toDTO(enrollment);
            
        } catch (Exception e) {
            // Xử lý lỗi và rollback nếu cần
            enrollment.setStatus(EnrollmentStatus.CANCELLED);
            enrollment.setPaymentStatus("PAYMENT_FAILED");
            enrollmentRepository.save(enrollment);
            throw new RuntimeException("Failed to process enrollment: " + e.getMessage());
        }
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

    // Lấy danh sách các khóa học thanh toán thất bại
    public List<EnrollmentDTO> getFailedPayments(Long userId) {
        List<CourseEnrollment> failedEnrollments = enrollmentRepository.findFailedPaymentsByUserId(userId);
        return failedEnrollments.stream()
                .map(enrollmentMapper::toDTO)
                .collect(Collectors.toList());
    }

    // Thử thanh toán lại một khóa học
    @Transactional
    public EnrollmentDTO retryPayment(Long userId, RetryPaymentRequest request) {
        CourseEnrollment enrollment = enrollmentRepository.findById(request.getEnrollmentId())
                .orElseThrow(() -> new RuntimeException("Enrollment not found"));

        // Kiểm tra xem enrollment có phải của user này không
        if (enrollment.getUserId() != userId) {
            throw new RuntimeException("Unauthorized to retry this payment");
        }

        // Kiểm tra xem payment status có phải PAYMENT_FAILED không
        if (!"PAYMENT_FAILED".equals(enrollment.getPaymentStatus())) {
            throw new RuntimeException("This enrollment is not in failed payment status");
        }

        try {
            // Gọi API thanh toán từ identity service
            ApiResponse<TransactionResponse> paymentResponse = identityClient.chargeCourse(
                    userId,
                    enrollment.getCourse().getPrice(),
                    "Retry payment for course: " + enrollment.getCourse().getTitle(),
                    TransactionType.COURSE_PAYMENT
            );

            if (paymentResponse.getResult() == null) {
                enrollment.setPaymentStatus("PAYMENT_FAILED");
                enrollmentRepository.save(enrollment);
                throw new RuntimeException("Payment failed: " + paymentResponse.getMessage());
            }

            // Thanh toán thành công
            enrollment.setStatus(EnrollmentStatus.ENROLLED);
            enrollment.setPaymentStatus("PAYMENT_SUCCESS");
            enrollment = enrollmentRepository.save(enrollment);
            
            // Tăng student count của course
            Course course = enrollment.getCourse();
            course.setStudentCount(course.getStudentCount() + 1);
            courseRepository.save(course);

            return enrollmentMapper.toDTO(enrollment);
            
        } catch (Exception e) {
            enrollment.setPaymentStatus("PAYMENT_FAILED");
            enrollmentRepository.save(enrollment);
            throw new RuntimeException("Failed to process payment: " + e.getMessage());
        }
    }

}
