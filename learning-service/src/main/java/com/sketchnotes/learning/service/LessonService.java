package com.sketchnotes.learning.service;

import com.sketchnotes.learning.dto.LessonDTO;
import com.sketchnotes.learning.entity.Course;
import com.sketchnotes.learning.entity.Lesson;
import com.sketchnotes.learning.exception.ErrorCode;
import com.sketchnotes.learning.mapper.LessonMapper;
import com.sketchnotes.learning.repository.CourseEnrollmentRepository;
import com.sketchnotes.learning.repository.CourseRepository;
import com.sketchnotes.learning.repository.LessonRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class LessonService {

    private final LessonRepository lessonRepository;
    private final CourseRepository courseRepository;
    private final CourseEnrollmentRepository enrollmentRepository;
    private final LessonMapper lessonMapper;

    public LessonService(LessonRepository lessonRepository, 
                         CourseRepository courseRepository, 
                         CourseEnrollmentRepository enrollmentRepository,
                         LessonMapper lessonMapper) {
        this.lessonRepository = lessonRepository;
        this.courseRepository = courseRepository;
        this.enrollmentRepository = enrollmentRepository;
        this.lessonMapper = lessonMapper;
    }

    // Tạo danh sách Lesson cho một Course
    @Transactional
    public List<LessonDTO> createLessonsForCourse(Long courseId, List<LessonDTO> lessonDtos) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new RuntimeException(ErrorCode.COURSE_NOT_FOUND.getMessage()));
        
        // Kiểm tra xem khóa học đã có người enroll chưa
        if (enrollmentRepository.existsByCourse_CourseId(courseId)) {
            throw new RuntimeException(ErrorCode.COURSE_HAS_ENROLLMENTS.getMessage());
        }

        List<Lesson> lessons = lessonMapper.toEntityList(lessonDtos);

        lessons.forEach(lesson -> {
            lesson.setCourse(course);
            lesson.setCreatedAt(LocalDateTime.now());
            lesson.setUpdatedAt(LocalDateTime.now());
        });

        List<Lesson> saved = lessonRepository.saveAll(lessons);
        
        // Cập nhật totalDuration của course
        course.getLessons().addAll(saved);
        course.updateTotalDuration();
        courseRepository.save(course);
        
        return lessonMapper.toDTOList(saved);
    }

    // Lấy tất cả lesson của 1 course
    public List<LessonDTO> getLessonsByCourse(Long courseId) {
        List<Lesson> lessons = lessonRepository.findByCourse_CourseId(courseId);
        return lessonMapper.toDTOList(lessons);
    }

    // Lấy một lesson theo ID
    public LessonDTO getLessonById(Long lessonId) {
        Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new RuntimeException(ErrorCode.LESSON_NOT_FOUND.getMessage()));
        return lessonMapper.toDTO(lesson);
    }

    // Cập nhật lesson
    @Transactional
    public LessonDTO updateLesson(Long lessonId, LessonDTO dto) {
        Lesson existingLesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new RuntimeException(ErrorCode.LESSON_NOT_FOUND.getMessage()));
        
        // Kiểm tra xem khóa học đã có người enroll chưa
        Long courseId = existingLesson.getCourse().getCourseId();
        if (enrollmentRepository.existsByCourse_CourseId(courseId)) {
            throw new RuntimeException(ErrorCode.COURSE_HAS_ENROLLMENTS.getMessage());
        }

        // Cập nhật các trường nếu có giá trị mới
        if (dto.getTitle() != null) {
            existingLesson.setTitle(dto.getTitle());
        }
        if (dto.getDescription() != null) {
            existingLesson.setDescription(dto.getDescription());
        }
        if (dto.getVideoUrl() != null) {
            existingLesson.setVideoUrl(dto.getVideoUrl());
        }
        if (dto.getDuration() != null) {
            existingLesson.setDuration(dto.getDuration());
        }
        if (dto.getOrderIndex() != null) {
            existingLesson.setOrderIndex(dto.getOrderIndex());
        }
        
        existingLesson.setUpdatedAt(LocalDateTime.now());
        Lesson updated = lessonRepository.save(existingLesson);
        
        // Cập nhật lại totalDuration của course
        Course course = existingLesson.getCourse();
        course.updateTotalDuration();
        courseRepository.save(course);
        
        return lessonMapper.toDTO(updated);
    }

    // Xóa lesson
    @Transactional
    public void deleteLesson(Long lessonId) {
        Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new RuntimeException(ErrorCode.LESSON_NOT_FOUND.getMessage()));
        
        // Kiểm tra xem khóa học đã có người enroll chưa
        Long courseId = lesson.getCourse().getCourseId();
        if (enrollmentRepository.existsByCourse_CourseId(courseId)) {
            throw new RuntimeException(ErrorCode.COURSE_HAS_ENROLLMENTS.getMessage());
        }
        
        Course course = lesson.getCourse();
        course.getLessons().remove(lesson);
        
        lessonRepository.delete(lesson);
        
        // Cập nhật lại totalDuration của course
        course.updateTotalDuration();
        courseRepository.save(course);
    }

}


