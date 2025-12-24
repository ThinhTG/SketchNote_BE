package com.sketchnotes.learning.service;

import com.sketchnotes.learning.dto.CourseDTO;
import com.sketchnotes.learning.entity.Course;
import com.sketchnotes.learning.entity.Lesson;
import com.sketchnotes.learning.exception.ErrorCode;
import com.sketchnotes.learning.mapper.CourseMapper;
import com.sketchnotes.learning.repository.CourseEnrollmentRepository;
import com.sketchnotes.learning.repository.CourseRepository;
import com.sketchnotes.learning.service.interfaces.ICourseService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CourseService implements ICourseService {
    private final CourseRepository courseRepository;
    private final CourseMapper courseMapper;
    private final CourseEnrollmentRepository enrollmentRepository;

    @Override
    public List<CourseDTO> getAllCourses() {
        List<Course> courses = courseRepository.findAllWithLessons();
        return courseMapper.toDTOList(courses);
    }

    @Override
    public CourseDTO createCourse(CourseDTO dto) {
        Course course = courseMapper.toEntity(dto);
        course.setCreatedAt(LocalDateTime.now());
        course.setUpdatedAt(LocalDateTime.now());

        // Gán course reference cho tất cả lessons và set timestamps
        if (course.getLessons() != null) {
            LocalDateTime now = LocalDateTime.now();
            for (Lesson lesson : course.getLessons()) {
                lesson.setCourse(course);
                lesson.setCreatedAt(now);
                lesson.setUpdatedAt(now);
            }
            // Tự động tính totalDuration dựa trên tổng duration của các lesson
            course.updateTotalDuration();
        }

        Course saved = courseRepository.save(course);
        return courseMapper.toDTO(saved);
    }

    @Override
    public CourseDTO getCourseById(Long id) {
        Course course = courseRepository.findByIdWithLessons(id)
                .orElseThrow(() -> new RuntimeException("Course not found with id: " + id));
        return courseMapper.toDTO(course);
    }

    @Override
    public CourseDTO updateCourse(Long id, CourseDTO dto) {
        Course existingCourse = courseRepository.findByIdWithLessons(id)
                .orElseThrow(() -> new RuntimeException("Course not found with id: " + id));
        
        // Kiểm tra xem khóa học đã có người enroll chưa
        if (enrollmentRepository.existsByCourse_CourseId(id)) {
            throw new RuntimeException(ErrorCode.COURSE_HAS_ENROLLMENTS.getMessage());
        }

        if (dto.getTitle() != null) existingCourse.setTitle(dto.getTitle());
        if (dto.getSubtitle() != null) existingCourse.setSubtitle(dto.getSubtitle());
        if (dto.getPrice() != 0) existingCourse.setPrice(dto.getPrice());
        if (dto.getDescription() != null) existingCourse.setDescription(dto.getDescription());
        if (dto.getCategory() != null) existingCourse.setCategory(dto.getCategory());
    if (dto.getStudentCount() != 0) existingCourse.setStudentCount(dto.getStudentCount());
        
        // Tự động tính lại totalDuration
        existingCourse.updateTotalDuration();
        existingCourse.setUpdatedAt(LocalDateTime.now());
        Course updated = courseRepository.save(existingCourse);

        return courseMapper.toDTO(updated);
    }

    @Override
    public void deleteCourse(Long id) {
        if (!courseRepository.existsById(id)) {
            throw new RuntimeException("Course not found with id: " + id);
        }
        
        // Kiểm tra xem khóa học đã có người enroll chưa
        if (enrollmentRepository.existsByCourse_CourseId(id)) {
            throw new RuntimeException(ErrorCode.COURSE_HAS_ENROLLMENTS.getMessage());
        }
        
        courseRepository.deleteById(id);
    }

    @Override
    public List<CourseDTO> getEnrolledCourses(Long userId) {
        List<Course> enrolledCourses = courseRepository.findEnrolledCoursesByUserId(userId);
        return courseMapper.toDTOList(enrolledCourses);
    }

    @Override
    public List<CourseDTO> getNotEnrolledCourses(Long userId) {
        List<Course> notEnrolledCourses = courseRepository.findNotEnrolledCoursesByUserId(userId);
        return courseMapper.toDTOList(notEnrolledCourses);
    }

    @Override
    public void updateCourseRating(Long courseId, Double avgRating, Integer ratingCount) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new RuntimeException("Course not found with id: " + courseId));
        
        course.setAvgRating(avgRating);
        course.setRatingCount(ratingCount);
        course.setUpdatedAt(LocalDateTime.now());
        
        courseRepository.save(course);
    }

    @Override
        
        CourseDTO dto = new CourseDTO();
        dto.setCourseId(course.getCourseId());
        dto.setAvgRating(course.getAvgRating());
        dto.setRatingCount(course.getRatingCount());
        return dto;
    }
}

