package com.sketchnotes.learning.service;

import com.sketchnotes.learning.client.IdentityClient;
import com.sketchnotes.learning.dto.response.TransactionResponse;
import com.sketchnotes.learning.dto.response.WalletResponse;
import com.sketchnotes.learning.dto.ApiResponse;
import com.sketchnotes.learning.dto.CourseDTO;
import com.sketchnotes.learning.dto.EnrollmentDTO;
import com.sketchnotes.learning.dto.enums.EnrollmentStatus;
import com.sketchnotes.learning.entity.Course;
import com.sketchnotes.learning.entity.CourseEnrollment;
import com.sketchnotes.learning.exception.AppException;
import com.sketchnotes.learning.exception.ErrorCode;
import com.sketchnotes.learning.mapper.CourseMapper;
import com.sketchnotes.learning.mapper.EnrollmentMapper;
import com.sketchnotes.learning.repository.CourseEnrollmentRepository;
import com.sketchnotes.learning.repository.CourseRepository;
import com.sketchnotes.learning.repository.UserLessonProgressRepository;
import com.sketchnotes.learning.service.interfaces.IEnrollmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EnrollmentService implements IEnrollmentService {
    private final CourseEnrollmentRepository enrollmentRepository;
    private final CourseRepository courseRepository;
    private final EnrollmentMapper enrollmentMapper;
    private final CourseMapper courseMapper;
    private final IdentityClient identityClient;
    private final UserLessonProgressRepository progressRepo;

    @Override
    public EnrollmentDTO enroll(long courseId, long userId) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new RuntimeException("Course not found with id: " + courseId));

        // Check if user is already enrolled in this course
        if (enrollmentRepository.findByCourse_CourseIdAndUserId(courseId, userId).isPresent()) {
            throw new RuntimeException("User already enrolled in this course");
        }

            ApiResponse<WalletResponse> walletResponse =
                    identityClient.getWalletByUserId(userId);
            
            if (walletResponse == null || walletResponse.getResult() == null) {
                throw new RuntimeException("Unable to retrieve wallet information");
            }
            
            WalletResponse wallet = walletResponse.getResult();
            
            // Check if wallet has sufficient balance
            if (wallet.getBalance().compareTo(java.math.BigDecimal.valueOf(course.getPrice())) < 0) {
                throw new AppException(ErrorCode.INSUFFICIENT_BALANCE );
            }

        try {
            // Call wallet service to charge course fee
            ApiResponse<TransactionResponse> chargeResponse =
                    identityClient.chargeCourse(BigDecimal.valueOf(course.getPrice()));
            
            if (chargeResponse == null || chargeResponse.getCode() != 200) {
                String errorMsg = chargeResponse != null ? chargeResponse.getMessage() : "Unknown error";
                throw new RuntimeException("Failed to charge course fee: " + errorMsg);
            }
            CourseEnrollment enrollment = new CourseEnrollment();
            enrollment.setCourse(course);
            enrollment.setUserId(userId);
            enrollment.setEnrolledAt(LocalDateTime.now());
            enrollment.setStatus(EnrollmentStatus.ENROLLED);
            enrollmentRepository.save(enrollment);

            course.setStudentCount(course.getStudentCount() + 1);
            courseRepository.save(course);

            return enrollmentMapper.toDTO(enrollment);

        } catch (Exception e) {
            throw new RuntimeException("Failed to process enrollment: " + e.getMessage());
        }
    }

    @Override
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

    @Override
    public List<EnrollmentDTO> getEnrollmentsByUser(Long userId) {
        List<CourseEnrollment> enrollments = enrollmentRepository.findByUserId(userId);
        return enrollments.stream()
                .map(e -> {
                    EnrollmentDTO dto = enrollmentMapper.toDTO(e);
                    // populate per-lesson progress for this user
                    if (dto.getCourse() != null && dto.getCourse().getLessons() != null) {
                        dto.getCourse().getLessons().forEach(lessonDto -> {
                            var progOpt = progressRepo.findByUserIdAndLesson_LessonId(userId, lessonDto.getLessonId());
                            if (progOpt.isPresent()) {
                                var prog = progOpt.get();
                                lessonDto.setLessonProgressStatus(prog.getStatus());
                                lessonDto.setLastPosition(prog.getLastPosition());
                                lessonDto.setTimeSpent(prog.getTimeSpent());
                                lessonDto.setCompletedAt(prog.getCompletedAt());
                            } else {
                                lessonDto.setLessonProgressStatus(com.sketchnotes.learning.dto.enums.ProgressStatus.NOT_STARTED);
                                lessonDto.setLastPosition(0);
                                lessonDto.setTimeSpent(0);
                                lessonDto.setCompletedAt(null);
                            }
                        });
                    }
                    return dto;
                })
                .collect(Collectors.toList());
    }

    @Override
    public EnrollmentDTO getEnrollmentByUserAndCourse(Long userId, Long courseId) {
        CourseEnrollment enrollment = enrollmentRepository.findByUserIdAndCourse_CourseId(userId, courseId)
                .orElseThrow(() -> new RuntimeException("Enrollment not found for user and course"));

        EnrollmentDTO dto = enrollmentMapper.toDTO(enrollment);

        // populate per-lesson progress for this user
        if (dto.getCourse() != null && dto.getCourse().getLessons() != null) {
            dto.getCourse().getLessons().forEach(lessonDto -> {
                var progOpt = progressRepo.findByUserIdAndLesson_LessonId(userId, lessonDto.getLessonId());
                if (progOpt.isPresent()) {
                    var prog = progOpt.get();
                    lessonDto.setLessonProgressStatus(prog.getStatus());
                    lessonDto.setLastPosition(prog.getLastPosition());
                    lessonDto.setTimeSpent(prog.getTimeSpent());
                    lessonDto.setCompletedAt(prog.getCompletedAt());
                } else {
                    lessonDto.setLessonProgressStatus(com.sketchnotes.learning.dto.enums.ProgressStatus.NOT_STARTED);
                    lessonDto.setLastPosition(0);
                    lessonDto.setTimeSpent(0);
                    lessonDto.setCompletedAt(null);
                }
            });
        }

        return dto;
    }
}
