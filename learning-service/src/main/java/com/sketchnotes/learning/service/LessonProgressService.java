package com.sketchnotes.learning.service;

import com.sketchnotes.learning.dto.UpdateProgressRequest;
import com.sketchnotes.learning.dto.enums.ProgressStatus;
import com.sketchnotes.learning.repository.CourseEnrollmentRepository;
import com.sketchnotes.learning.repository.LessonRepository;
import com.sketchnotes.learning.repository.UserLessonProgressRepository;
import com.sketchnotes.learning.service.interfaces.ILessonProgressService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class LessonProgressService implements ILessonProgressService {
    private final UserLessonProgressRepository progressRepo;
    private final LessonRepository lessonRepo;
    private final CourseEnrollmentRepository enrollRepo;

    @Override
    @Transactional
    public void updateLessonProgress(Long userId, Long courseId, Long lessonId, UpdateProgressRequest request) {
        // Kiểm tra enrollment tồn tại và đã thanh toán
        var enrollment = enrollRepo.findByUserIdAndCourse_CourseId(userId, courseId)
                .orElseThrow(() -> new RuntimeException("User is not enrolled in this course"));

        // Validate lesson belongs to course
        var lesson = lessonRepo.findById(lessonId)
                .orElseThrow(() -> new RuntimeException("Lesson not found with id: " + lessonId));
        if (!lesson.getCourse().getCourseId().equals(courseId)) {
            throw new RuntimeException("Lesson does not belong to this course");
        }

        // Upsert: INSERT if not exists, do nothing if exists (atomic, no race condition)
        progressRepo.upsertProgress(userId, courseId, lessonId);

        // Now fetch the progress (guaranteed to exist)
        var progress = progressRepo.findByUserIdAndLesson_LessonId(userId, lessonId)
                .orElseThrow(() -> new RuntimeException("Failed to get progress for lesson: " + lessonId));

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