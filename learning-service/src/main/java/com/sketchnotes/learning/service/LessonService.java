package com.sketchnotes.learning.service;

import com.sketchnotes.learning.dto.LessonDTO;
import com.sketchnotes.learning.entity.Course;
import com.sketchnotes.learning.entity.Lesson;
import com.sketchnotes.learning.mapper.LessonMapper;
import com.sketchnotes.learning.repository.CourseRepository;
import com.sketchnotes.learning.repository.LessonRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class LessonService {

    private final LessonRepository lessonRepository;
    private final CourseRepository courseRepository;
    private final LessonMapper lessonMapper;

    public LessonService(LessonRepository lessonRepository, CourseRepository courseRepository, LessonMapper lessonMapper) {
        this.lessonRepository = lessonRepository;
        this.courseRepository = courseRepository;
        this.lessonMapper = lessonMapper;
    }

    // Tạo danh sách Lesson cho một Course
    public List<Lesson> createLessonsForCourse(Long courseId, List<LessonDTO> lessonDtos) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new RuntimeException("Course not found"));

        List<Lesson> lessons = lessonMapper.toEntityList(lessonDtos);

        lessons.forEach(lesson -> {
            lesson.setCourse(course);
            lesson.setCreatedAt(LocalDateTime.now());
            lesson.setUpdatedAt(LocalDateTime.now());
        });

        return lessonRepository.saveAll(lessons);
    }

    // Lấy tất cả lesson của 1 course
    public List<Lesson> getLessonsByCourse(Long courseId) {
        return lessonRepository.findByCourse_CourseId(courseId);
    }

}


