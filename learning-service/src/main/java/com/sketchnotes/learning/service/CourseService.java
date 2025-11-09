package com.sketchnotes.learning.service;

import com.sketchnotes.learning.dto.CourseDTO;
import com.sketchnotes.learning.entity.Course;
import com.sketchnotes.learning.entity.Lesson;
import com.sketchnotes.learning.mapper.CourseMapper;
import com.sketchnotes.learning.repository.CourseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CourseService {
    private final CourseRepository courseRepository;
    private final CourseMapper courseMapper;

    public List<CourseDTO> getAllCourses() {
        List<Course> courses = courseRepository.findAllWithLessons();
        return courseMapper.toDTOList(courses);
    }

    // 1. Tạo Course
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
        }

        Course saved = courseRepository.save(course);
        return courseMapper.toDTO(saved);
    }

    // 2. Lấy khóa học theo ID
    public CourseDTO getCourseById(Long id) {
        Course course = courseRepository.findByIdWithLessons(id)
                .orElseThrow(() -> new RuntimeException("Course not found with id: " + id));
        return courseMapper.toDTO(course);
    }


    // 4. Cập nhật khóa học
    public CourseDTO updateCourse(Long id, CourseDTO dto) {
        Course existingCourse = courseRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Course not found with id: " + id));

        if (dto.getTitle() != null) existingCourse.setTitle(dto.getTitle());
        if (dto.getSubtitle() != null) existingCourse.setSubtitle(dto.getSubtitle());
        if (dto.getPrice() != 0) existingCourse.setPrice(dto.getPrice());
        if (dto.getDescription() != null) existingCourse.setDescription(dto.getDescription());
        if (dto.getCategory() != null) existingCourse.setCategory(dto.getCategory());
        if (dto.getStudentCount() != 0) existingCourse.setStudent_count(dto.getStudentCount());

        existingCourse.setUpdatedAt(LocalDateTime.now());
        Course updated = courseRepository.save(existingCourse);

        return courseMapper.toDTO(updated);
    }


    // 5. Xóa khóa học
    public void deleteCourse(Long id) {
        if (!courseRepository.existsById(id)) {
            throw new RuntimeException("Course not found with id: " + id);
        }
        courseRepository.deleteById(id);
    }

    }

