package com.sketchnotes.learning.service;

import com.sketchnotes.learning.dto.UpdateProgressRequest;
import com.sketchnotes.learning.dto.enums.ProgressStatus;
import com.sketchnotes.learning.entity.Course;
import com.sketchnotes.learning.entity.Lesson;
import com.sketchnotes.learning.entity.UserLessonProgress;
import com.sketchnotes.learning.repository.CourseEnrollmentRepository;
import com.sketchnotes.learning.repository.CourseRepository;
import com.sketchnotes.learning.repository.LessonRepository;
import com.sketchnotes.learning.repository.UserLessonProgressRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class LessonProgressService {
    private final UserLessonProgressRepository progressRepo;
    private final LessonRepository lessonRepo;
    private final CourseRepository courseRepo;
    private final CourseEnrollmentRepository enrollRepo;

    @Transactional
    public void updateLessonProgress(Long userId, Long courseId, Long lessonId, UpdateProgressRequest request) {
        // Kiểm tra enrollment tồn tại và đã thanh toán
        var enrollment = enrollRepo.findByUserIdAndCourse_CourseId(userId, courseId)
                .orElseThrow(() -> new RuntimeException("User is not enrolled in this course"));

        // Sử dụng getOrCreateProgress để handle race condition
        var progress = getOrCreateProgress(userId, courseId, lessonId);

        // Cập nhật trạng thái
        if (request.isCompleted()) {
            progress.setStatus(ProgressStatus.COMPLETED);
            progress.setCompletedAt(LocalDateTime.now());
        } else {
            progress.setStatus(ProgressStatus.IN_PROGRESS);
        }

        progress.setLastPosition(request.getLastPosition());
        progress.setTimeSpent(progress.getTimeSpent() + request.getTimeSpent());

        progressRepo.save(progress);

        // Tính lại % tiến độ của khóa học
        recalcCourseProgress(userId, courseId);
    }

    /**
     * Get existing progress or create new one with race condition handling.
     * If a duplicate key exception occurs during insert, retry by fetching the existing record.
     */
    private UserLessonProgress getOrCreateProgress(Long userId, Long courseId, Long lessonId) {
        // First, try to find existing progress
        var existingProgress = progressRepo.findByUserIdAndLesson_LessonId(userId, lessonId);
        if (existingProgress.isPresent()) {
            return existingProgress.get();
        }

        // If not found, try to create new progress
        try {
            UserLessonProgress newProgress = createNewProgress(userId, courseId, lessonId);
            // Save immediately to detect duplicate key early
            return progressRepo.saveAndFlush(newProgress);
        } catch (DataIntegrityViolationException e) {
            // Race condition occurred - another request already created the record
            // Retry fetching the existing record
            log.warn("Race condition detected for userId={}, lessonId={}. Fetching existing record.", userId, lessonId);
            return progressRepo.findByUserIdAndLesson_LessonId(userId, lessonId)
                    .orElseThrow(() -> new RuntimeException("Failed to get or create progress for lesson: " + lessonId));
        }
    }

    private UserLessonProgress createNewProgress(Long userId, Long courseId, Long lessonId) {
        Course course = courseRepo.findById(courseId)
                .orElseThrow(() -> new RuntimeException("Course not found with id: " + courseId));
                
        Lesson lesson = lessonRepo.findById(lessonId)
                .orElseThrow(() -> new RuntimeException("Lesson not found with id: " + lessonId));

        // Validate lesson belongs to course
        if (!lesson.getCourse().getCourseId().equals(courseId)) {
            throw new RuntimeException("Lesson does not belong to this course");
        }

        UserLessonProgress progress = new UserLessonProgress();
        progress.setUserId(userId);
        progress.setCourse(course);
        progress.setLesson(lesson);
        progress.setStatus(ProgressStatus.NOT_STARTED);
        progress.setTimeSpent(0);
        progress.setLastPosition(0);
        return progress;
    }

    private void recalcCourseProgress(Long userId, Long courseId) {
        long totalLessons = lessonRepo.countByCourse_CourseId(courseId);
        if (totalLessons == 0) {
            throw new RuntimeException("Course has no lessons");
        }
        
        long completed = progressRepo.countCompletedLessons(userId, courseId);
        
        BigDecimal percent = BigDecimal.valueOf(completed)
                .multiply(BigDecimal.valueOf(100))
                .divide(BigDecimal.valueOf(totalLessons), 2, RoundingMode.HALF_UP);

        var enrollment = enrollRepo.findByUserIdAndCourse_CourseId(userId, courseId)
                .orElseThrow(() -> new RuntimeException("Enrollment not found"));

        enrollment.setProgressPercent(percent);
        enrollRepo.save(enrollment);
    }
}